package com.region.lis.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.region.lis.client.CountyLisClient;
import com.region.lis.common.Result;
import com.region.lis.common.ResultCode;
import com.region.lis.dto.OrderUploadDTO;
import com.region.lis.entity.Report;
import com.region.lis.entity.TestItemMapping;
import com.region.lis.entity.TestOrder;
import com.region.lis.entity.TestResult;
import com.region.lis.mapper.ReportMapper;
import com.region.lis.mapper.TestItemMappingMapper;
import com.region.lis.mapper.TestOrderMapper;
import com.region.lis.mapper.TestResultMapper;
import com.region.lis.util.DesensitizeUtil;
import com.region.lis.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LisService {

    @Autowired private TestOrderMapper testOrderMapper;
    @Autowired private TestItemMappingMapper testItemMappingMapper;
    @Autowired private TestResultMapper testResultMapper;
    @Autowired private ReportMapper reportMapper;
    @Autowired private CountyLisClient countyLisClient;
    @Autowired private LogService logService;
    /**
     * 乡镇卫生院上传医嘱（接收参数）
     */
    public Result<String> upload(OrderUploadDTO dto) {
        // 1. 校验参数
        if (dto.getOrderNo() == null || dto.getTownshipId() == null) {
            return Result.fail(400, "医嘱号或机构ID不能为空");
        }

        // 2. 幂等：查医嘱是否已存在
        TestOrder exist = testOrderMapper.selectOne(
                new LambdaQueryWrapper<TestOrder>().eq(TestOrder::getOrderNo, dto.getOrderNo())
        );
        if (exist != null) {
            return Result.fail(ResultCode.ORDER_DUPLICATE, "医嘱重复上传：" + dto.getOrderNo());
        }

        // 3. 查对码
        TestItemMapping mapping = testItemMappingMapper.selectOne(
                new LambdaQueryWrapper<TestItemMapping>()
                        .eq(TestItemMapping::getTownshipCode, dto.getItemCode())
                        .eq(TestItemMapping::getTownshipId, dto.getTownshipId())
        );
        if (mapping == null) {
            return Result.fail(ResultCode.ITEM_NOT_MAPPED, "项目未对码：" + dto.getItemCode());
        }

        // 4. 入库
        TestOrder order = new TestOrder();
        order.setOrderNo(dto.getOrderNo());
        order.setPatientName(dto.getPatientName());
        order.setIdCard(dto.getIdCard());
        order.setPhone(dto.getPhone());
        order.setTownshipId(dto.getTownshipId());
        order.setItemCode(dto.getItemCode());
        order.setCountyCode(mapping.getCountyCode());
        order.setStatus(0);
        order.setRetryCount(0);
        testOrderMapper.insert(order);

        // 5. 日志：申请接收
        logService.record("申请接收", dto.getOrderNo(), null,
                "乡镇卫生院", "接收检验申请", RequestUtil.getIp());

        // 6. 推送县医院 LIS
        boolean success = countyLisClient.uploadOrder(
                order.getOrderNo(), mapping.getCountyCode(), order.getPatientName()
        );

        order.setStatus(success ? 1 : 4);
        testOrderMapper.updateById(order);

        // 7. 日志：转发
        logService.record("转发", order.getOrderNo(), null,
                "系统", "转发县医院LIS：" + mapping.getCountyCode(), RequestUtil.getIp());

        return success ? Result.success("上传成功") : Result.fail(500, "推送LIS失败");
    }

    /**
     * 批量上传
     */
    public Result<String> uploadBatch(List<OrderUploadDTO> list) {
        int success = 0, fail = 0;
        for (OrderUploadDTO dto : list) {
            Result<String> r = upload(dto);
            if (r.getCode() == 200) {
                success++;
            } else {
                fail++;
            }
        }
        return Result.success("成功" + success + "条，失败" + fail + "条");
    }
    /**
     * 第一步：项目对码 + 上传医嘱
     */
    public Result<String> uploadOrders() {
        List<TestOrder> orders = testOrderMapper.selectList(
                new LambdaQueryWrapper<TestOrder>().eq(TestOrder::getStatus, 0)
        );

        for (TestOrder order : orders) {
            // 日志：申请接收
//            logService.record("申请接收", order.getOrderNo(), null,
//                    "乡镇卫生院", "接收检验申请", "127.0.0.1");
            logService.record("申请接收", order.getOrderNo(), null,
                    "乡镇卫生院", "接收检验申请", RequestUtil.getIp());
            // 对码
            TestItemMapping mapping = testItemMappingMapper.selectOne(
                    new LambdaQueryWrapper<TestItemMapping>()
                            .eq(TestItemMapping::getTownshipCode, order.getItemCode())
                            .eq(TestItemMapping::getTownshipId, order.getTownshipId())
            );

            if (mapping == null) {
                log.error("❌ 项目未对码：{}", order.getItemCode());
                order.setStatus(4);
                testOrderMapper.updateById(order);
                logService.record("转发", order.getOrderNo(), null,
                        "系统", "项目未对码：" + order.getItemCode(),  RequestUtil.getIp());
                return Result.fail(ResultCode.ITEM_NOT_MAPPED, "项目未对码：" + order.getItemCode());
            }

            order.setCountyCode(mapping.getCountyCode());
            testOrderMapper.updateById(order);

            // 上传LIS
            boolean success = countyLisClient.uploadOrder(
                    order.getOrderNo(), mapping.getCountyCode(), order.getPatientName()
            );

            order.setStatus(success ? 1 : 4);
            testOrderMapper.updateById(order);

            // 日志：转发
            logService.record("转发", order.getOrderNo(), null,
                    "系统", "转发县医院LIS：" + mapping.getCountyCode(),  RequestUtil.getIp());
        }
        return Result.success("上传完成");
    }
    @Autowired
    private HrService hrService;
    /**
     * 第二步：合管检验 + 危机值判断
     */
    @Transactional
    public Result<String> syncResults() {
        List<TestOrder> orders = testOrderMapper.selectList(
                new LambdaQueryWrapper<TestOrder>().eq(TestOrder::getStatus, 1)
        );

        // 合管：同一患者、同一卫生院，合并出报告
//        Map<String, List<TestOrder>> grouped = orders.stream()
//                .collect(Collectors.groupingBy(o -> o.getPatientName() + "_" + o.getTownshipId()));
        Map<String, List<TestOrder>> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> buildPatientGroupKey(
                                o.getIdCard(),
                                o.getTownshipId()
                        )
                ));
        for (Map.Entry<String, List<TestOrder>> entry : grouped.entrySet()) {
            List<TestOrder> group = entry.getValue();
//            System.out.println(entry.toString());
            log.info("开始处理合管组：key={}, 医嘱数量={}", entry.getKey(), group.size());
//            log.info(entry.toString());
            // 生成报告
            Report report = new Report();
            report.setReportId("RPT" + System.currentTimeMillis());
            report.setOrderNos(group.stream().map(TestOrder::getOrderNo).collect(Collectors.joining(",")));
            report.setPatientName(group.get(0).getPatientName());
            report.setTownshipId(group.get(0).getTownshipId());
            report.setStatus(0);
//            report.setReportImageUrl("/report/" + p1);
            report.setReportImageUrl("/report/p1.png");

            reportMapper.insert(report);

            for (TestOrder order : group) {
                // 模拟LIS出结果
                String result = countyLisClient.getResult(order.getOrderNo());

                TestResult tr = new TestResult();
                tr.setOrderNo(order.getOrderNo());
                tr.setResultValue(result);
                tr.setIsCritical(result.contains("危急") ? 1 : 0);
                tr.setStatus(0);
                // 加互认标识
                String hrFlag = hrService.judgeHrFlag(order.getCountyCode(), result, tr.getIsCritical());
                tr.setHrFlag(hrFlag);
                tr.setHrReason(hrService.getHrReason(hrFlag, tr.getIsCritical()));
                testResultMapper.insert(tr);
// ===== 临时事务测试代码 =====
//                if ("ORD004".equals(order.getOrderNo())) {
//                    throw new RuntimeException("事务测试：ORD004故意失败");
//                }
// ===== 测试代码结束 =====
                // 危机值告警
                if (tr.getIsCritical() == 1) {
                    log.error("🚨 危机值告警：医嘱={}, 结果={}", order.getOrderNo(), result);
                    logService.record("接收报告", order.getOrderNo(), report.getReportId(),
                            "系统", "危机值告警：" + result, RequestUtil.getIp());
                }

//                order.setReportId(String.valueOf(report.getId()));
                order.setReportId(report.getReportId());
//                System.out.println(String.valueOf(report.getId())+order.getId());
//                log.info(String.valueOf(report.getId()) + order.getId());
                order.setStatus(2);
                testOrderMapper.updateById(order);

                // 日志：接收报告
                logService.record("接收报告", order.getOrderNo(), report.getReportId(),
                        "县医院LIS", "接收报告：" + result, RequestUtil.getIp());
            }
        }
        return Result.success("同步完成");
    }

    /**
     * 第三步：结果回传乡镇，统一JSON
     */
    public Result<String> returnResults11() {
        List<TestResult> results = testResultMapper.selectList(
                new LambdaQueryWrapper<TestResult>().eq(TestResult::getStatus, 0)
        );

        for (TestResult result : results) {
            // 构造统一JSON
            Map<String, Object> json = new HashMap<>();
            json.put("orderNo", result.getOrderNo());
            json.put("resultValue", result.getResultValue());
            json.put("isCritical", result.getIsCritical());
            json.put("reportImageUrl", "/images/" + result.getOrderNo() + ".jpg");

            log.info("📥 回传乡镇卫生院：{}", json);

            result.setStatus(1);
            testResultMapper.updateById(result);

            // 日志：回写乡镇
            logService.record("回写乡镇", result.getOrderNo(), null,
                    "系统", "回写结果", RequestUtil.getIp());

            System.out.println(json.toString()+"日志：回写乡镇");
        }
        return Result.success("回传完成");
    }
    public Result<List<Map<String, Object>>> returnResults12() {
        List<TestResult> results = testResultMapper.selectList(
                new LambdaQueryWrapper<TestResult>().eq(TestResult::getStatus, 0)
        );

        List<Map<String, Object>> returnList = new ArrayList<>();

        for (TestResult result : results) {
            // 查医嘱，拿患者信息
            TestOrder order = testOrderMapper.selectOne(
                    new LambdaQueryWrapper<TestOrder>()
                            .eq(TestOrder::getOrderNo, result.getOrderNo())
            );

            // 构造返回 JSON
            Map<String, Object> json = new HashMap<>();
            json.put("orderNo", result.getOrderNo());
            json.put("resultValue", result.getResultValue());
            json.put("isCritical", result.getIsCritical());
//            json.put("reportImageUrl", "/images/" + result.getOrderNo() + ".jpg");
//            json.put("reportImageUrl", "/images/" +   result.getReportUrl() + ".jpg");
            Report report = reportMapper.selectOne(
                    new LambdaQueryWrapper<Report>()
                            .eq(Report::getReportId, order.getReportId())
            );

            json.put("reportImageUrl", report != null ? report.getReportImageUrl() : null);
            // 患者信息脱敏
            if (order != null) {
                Map<String, Object> patient = new HashMap<>();
                patient.put("name", DesensitizeUtil.name(order.getPatientName()));
                patient.put("idCard", DesensitizeUtil.idCard(order.getIdCard()));
                patient.put("phone", DesensitizeUtil.phone(order.getPhone()));
                json.put("patient", patient);
            }

            returnList.add(json);

            // 更新状态
            result.setStatus(1);
            testResultMapper.updateById(result);

            // 日志
            logService.record("回写乡镇", result.getOrderNo(), null,
                    "系统", "回写结果", RequestUtil.getIp());
        }

        return Result.success(returnList);
    }
    public Result<List<Map<String, Object>>> returnResults() {

        // 1. 一次查询所有待回写结果
        List<TestResult> results = testResultMapper.selectList(
                new LambdaQueryWrapper<TestResult>()
                        .eq(TestResult::getStatus, 0)
        );

        if (results.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 一次性查询所有相关医嘱
        List<String> orderNos = results.stream()
                .map(TestResult::getOrderNo)
                .filter(orderNo -> orderNo != null && !orderNo.isBlank())
                .distinct()
                .collect(Collectors.toList());

        List<TestOrder> orders = testOrderMapper.selectList(
                new LambdaQueryWrapper<TestOrder>()
                        .in(TestOrder::getOrderNo, orderNos)
        );

        // 3. 转成 Map，后面通过 orderNo 直接查，不再访问数据库
        Map<String, TestOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(
                        TestOrder::getOrderNo,
                        order -> order,
                        (a, b) -> a
                ));

        // 4. 收集所有 reportId
        List<String> reportIds = orders.stream()
                .map(TestOrder::getReportId)
                .filter(reportId -> reportId != null && !reportId.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // 5. 一次性查询所有报告
        List<Report> reports = reportIds.isEmpty()
                ? new ArrayList<>()
                : reportMapper.selectList(
                new LambdaQueryWrapper<Report>()
                        .in(Report::getReportId, reportIds)
        );

        // 6. 转成 Map，后面通过 reportId 直接查
        Map<String, Report> reportMap = reports.stream()
                .collect(Collectors.toMap(
                        Report::getReportId,
                        report -> report,
                        (a, b) -> a
                ));

        List<Map<String, Object>> returnList = new ArrayList<>();

        // 7. 内存组装结果，不再循环查询数据库
        for (TestResult result : results) {

            TestOrder order = orderMap.get(result.getOrderNo());

            Map<String, Object> json = new HashMap<>();
            json.put("orderNo", result.getOrderNo());
            json.put("resultValue", result.getResultValue());
            json.put("isCritical", result.getIsCritical());

            if (order != null) {

                Report report = reportMap.get(order.getReportId());

                json.put(
                        "reportImageUrl",

                        report != null ? report.getReportImageUrl() : null
                );

                Map<String, Object> patient = new HashMap<>();
                patient.put("name", DesensitizeUtil.name(order.getPatientName()));
                patient.put("idCard", DesensitizeUtil.idCard(order.getIdCard()));
                patient.put("phone", DesensitizeUtil.phone(order.getPhone()));

                json.put("patient", patient);

            } else {
                // 找不到医嘱时，也不要让整个回写任务直接 NPE
                json.put("reportImageUrl", null);
            }

            returnList.add(json);

            // 更新回写状态
            result.setStatus(1);
            testResultMapper.updateById(result);

            logService.record(
                    "回写乡镇",
                    result.getOrderNo(),
                    order != null ? order.getReportId() : null,
                    "系统",
                    "回写结果",
                    RequestUtil.getIp()
            );
        }

        return Result.success(returnList);
    }
    /**
     * 构造脱敏后的患者信息
     */
    private Map<String, Object> buildPatientInfo(TestOrder order) {
        Map<String, Object> patient = new HashMap<>();
        patient.put("name", DesensitizeUtil.name(order.getPatientName()));
        patient.put("idCard", DesensitizeUtil.idCard(order.getIdCard()));
        patient.put("phone", DesensitizeUtil.phone(order.getPhone()));
        return patient;
    }


    private String buildPatientGroupKey(String idCard, String townshipId) {

        if (idCard == null || idCard.isBlank()) {
            throw new IllegalArgumentException("患者身份证号不能为空");
        }

        if (townshipId == null || townshipId.isBlank()) {
            throw new IllegalArgumentException("卫生院ID不能为空");
        }

        return idCard + "_" + townshipId;
    }
}
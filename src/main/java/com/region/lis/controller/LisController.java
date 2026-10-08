package com.region.lis.controller;

import com.region.lis.common.Result;
import com.region.lis.dto.OrderUploadDTO;
import com.region.lis.service.LisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 区域检验平台 —— 医嘱/检验/回传 核心接口
 */
@RestController
@RequestMapping("/lis")
public class LisController {

    @Autowired
    private LisService lisService;

    /**
     * 乡镇卫生院上传单条检验医嘱
     */
    @PostMapping("/order/upload")
    public Result<String> uploadOrder(@RequestBody OrderUploadDTO dto) {
        return lisService.upload(dto);
    }

    /**
     * 批量上传检验医嘱
     */
    @PostMapping("/order/upload-batch")
    public Result<String> uploadBatch(@Valid @RequestBody List<OrderUploadDTO> list) {
        return lisService.uploadBatch(list);
    }

    /**
     * 手动触发：轮询县医院 LIS 拉取检验结果并同步
     */
    @PostMapping("/result/sync")
    public Result<String> syncResults() {
        return lisService.syncResults();
    }

    /**
     * 回传检验结果给乡镇卫生院（返回结构化 JSON，含患者脱敏信息）
     */
    @PostMapping("/result/return")
    public Result<List<Map<String, Object>>> returnResults() {
        return lisService.returnResults();
    }

    /**
     * 一键跑完整流程（调试/手动触发用）：推送医嘱 → 同步结果 → 回传
     */
    @PostMapping("/pipeline/all")
    public Result<List<Map<String, Object>>> executeFullPipeline() {
        lisService.uploadOrders();
        lisService.syncResults();
        return lisService.returnResults();
    }
}
package com.region.lis.controller;

import com.region.lis.common.Result;
import com.region.lis.dto.OrderUploadDTO;
import com.region.lis.service.LisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lis")
public class LisController {

    @Autowired
    private LisService lisService;

    /**
     * 乡镇卫生院上传医嘱（接收参数）
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody OrderUploadDTO dto) {
        return lisService.upload(dto);
    }

    /**
     * 批量上传
     */
    @PostMapping("/upload-batch")
    public Result<String> uploadBatch(@RequestBody List<OrderUploadDTO> list) {
        return lisService.uploadBatch(list);
    }

    @PostMapping("/sync")
    public String sync() {
        lisService.syncResults();
        return "同步完成";
    }

    @PostMapping("/return")
    public String returnResult() {
        lisService.returnResults();
        return "回传完成";
    }
    @PostMapping("/return2")
    public Result<List<Map<String, Object>>> returnResult2() {
        return lisService.returnResults();
    }
    @PostMapping("/all")
    public String all() {
        lisService.uploadOrders();
        lisService.syncResults();
        lisService.returnResults();
        return "全流程完成";
    }
}
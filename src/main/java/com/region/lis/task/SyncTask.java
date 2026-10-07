package com.region.lis.task;

import com.region.lis.service.LisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncTask {

    @Autowired
    private LisService lisService;

    // 每30秒上传医嘱
//    @Scheduled(fixedDelay = 30000)
    public void upload() {
        log.info("⏰ 上传医嘱任务开始");
        lisService.uploadOrders();
        log.info("⏰ 上传医嘱任务结束");
    }

    // 每30秒同步结果
    @Scheduled(fixedDelay = 30000)
    public void sync() {
        log.info("⏰ 同步结果任务开始");
        lisService.syncResults();
        log.info("⏰ 同步结果任务结束");
    }

    // 每30秒回传结果
//    @Scheduled(fixedDelay = 30000)
    public void returnResult() {
        log.info("⏰ 回传结果任务开始");
        lisService.returnResults();
        log.info("⏰ 回传结果任务结束");
    }
}
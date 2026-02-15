package com.cool.server.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HotPostTask {
    
    private static final Logger log = LoggerFactory.getLogger(HotPostTask.class);

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void updateHotPosts() {
        log.info("开始更新热门帖子...");
    }
}

package com.linghang.backend.mywust_basic.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class LogController {
    private static final Logger log= LoggerFactory.getLogger("logController");
    @RequestMapping("/log")
    public void setLog(){
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.trace("trace");
    }
}

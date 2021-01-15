package com.sunfield.springboot.tps.controller;

import com.sunfield.springboot.tps.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class TestController {

    /**
     * 测试接口
     * @return
     */
    @RateLimit(limit = 100)
    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("调用成功");
    }

}

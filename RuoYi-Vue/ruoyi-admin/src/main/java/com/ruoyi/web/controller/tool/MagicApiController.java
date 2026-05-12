package com.ruoyi.web.controller.tool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/magic/api")
public class MagicApiController {

    @GetMapping("/test/ping")
    public String ping() {
        return "pong";
    }
}

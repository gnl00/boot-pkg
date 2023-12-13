package com.demo.lite.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LiteController
 *
 * @author gnl
 * @since 2023/5/8
 */
@RestController
public class LiteController {

    @GetMapping("/lite")
    public String lite() {
        return "lite is running";
    }
}

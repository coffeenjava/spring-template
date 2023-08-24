package com.my.api.controller;

import com.my.api.service.MultiDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/db")
@RestController
public class MultiDbController {

    private final MultiDbService service;

    @GetMapping
    public void test() {
        service.repositoryTest();
    }
}

package com.brian.api.controller;

import com.brian.api.repository.dto.TestMemberAndDetailDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/hello")
@RestController
public class HelloController {

    @GetMapping
    public String hello(@RequestParam(required = false) String name, @RequestParam LocalDate date) {
        System.out.println(date);
        if (name == null) return "hello~!";
        return "hello. "+name;
    }

    @GetMapping("/list")
    public List<String> getList() {
        return Arrays.asList("1","2","3");
    }

    @GetMapping("/attribute")
    public void attribute(@Valid @ModelAttribute TestMemberAndDetailDto request) {
        System.out.println(request.getName());
    }

    @PostMapping("/body")
    public TestMemberAndDetailDto body(@Valid @RequestBody TestMemberAndDetailDto request) {
        System.out.println(request.getName());
        return request;
    }
}

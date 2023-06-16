package com.brian.api.repository.dto;

import com.brian.api.common.model.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TestMemberAndDetailDto implements BaseRequest {

    private Long id;

    private String name;

    private String phone;

    private String address;

    @Override
    public void validate() {
        System.out.println("validate");
    }
}

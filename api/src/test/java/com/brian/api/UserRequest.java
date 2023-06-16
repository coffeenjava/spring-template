package com.brian.api;

import com.brian.api.common.annotation.CopyAlias;
import com.brian.api.common.model.BaseModel;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRequest implements BaseModel {

    private String name;

    @CopyAlias("userPassword")
    private String password;


    public static void main(String[] args) {
        UserRequest request = UserRequest.builder()
                .name("coffeenjava")
                .password("1234").build();

        UserDto userDto = request.copyTo(new UserDto());

        System.out.println(userDto);
    }
}

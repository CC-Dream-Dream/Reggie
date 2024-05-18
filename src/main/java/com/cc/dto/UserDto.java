package com.cc.dto;

import com.cc.pojo.User;
import lombok.Data;

@Data
public class UserDto extends User {
    private String code;
}

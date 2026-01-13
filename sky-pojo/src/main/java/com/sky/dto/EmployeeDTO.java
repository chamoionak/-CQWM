package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
//DTO通常用于封装前端传递过来的参数
@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}

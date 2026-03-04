package com.cool.pojo.dto;

import lombok.Data;

@Data
public class RefreshTokenDTO {
    private String refreshToken;


    private String deviceId;
}

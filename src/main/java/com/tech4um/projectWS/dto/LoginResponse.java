package com.tech4um.projectWS.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private String email;
}

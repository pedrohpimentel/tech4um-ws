package com.tech4um.projectWS.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdAt;
}

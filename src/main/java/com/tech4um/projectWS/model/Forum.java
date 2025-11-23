package com.tech4um.projectWS.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "forums")
public class Forum {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private Long createdAt = System.currentTimeMillis();

}

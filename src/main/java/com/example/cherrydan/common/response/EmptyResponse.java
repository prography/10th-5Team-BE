package com.example.cherrydan.common.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class EmptyResponse {
    @JsonValue
    public Map<String, Object> toJson() {
        return new HashMap<>();
    }
} 
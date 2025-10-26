package com.tastenfood.FoodApp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int statusCode; //eg: "200","400"
    private String message; //additional information about the response
    private T data; //actual data payload
    private Map<String, Serializable> meta;
}

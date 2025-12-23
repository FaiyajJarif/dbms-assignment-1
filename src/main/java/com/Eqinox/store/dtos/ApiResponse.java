package com.Eqinox.store.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ApiResponse {
    private boolean success;
    private String message;

    public static ApiResponse success(String msg) {
        return new ApiResponse(true, msg);
    }

    public static ApiResponse failure(String msg) {
        return new ApiResponse(false, msg);
    }
}

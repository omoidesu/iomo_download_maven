package com.omoi.iomo_download.entity.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author omoi
 * @date 2024/1/11
 */
@Data
@AllArgsConstructor
public class ResponseEntity {
    private Integer code;
    private String message;

    public static ResponseEntity success(String message) {
        return new ResponseEntity(200, message);
    }
}

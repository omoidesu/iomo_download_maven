package com.omoi.iomo_download.entity.vo;

import lombok.Data;

/**
 * @author omoi
 * @date 2024/1/13
 */
@Data
public class KookResponse {
    private Integer code;
    private String message;
    private KV data;

    @Data
    public static class KV {
        private String url;
    }
}

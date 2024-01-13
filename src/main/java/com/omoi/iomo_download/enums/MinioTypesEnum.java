package com.omoi.iomo_download.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author omoi
 * @date 2024/1/11
 */
@AllArgsConstructor
@Getter
public enum MinioTypesEnum {
    MAP("map"),
    BP("bp");

    private final String type;
}

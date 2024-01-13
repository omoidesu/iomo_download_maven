package com.omoi.iomo_download.entity.vo.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author omoi
 * @date 2024/1/11
 */
@Data
public class UploadMusicRequest {
    /** 谱面集id */
    @NotBlank(message = "setId不能为空")
    @NotNull(message = "setId不能为空")
    @Digits(integer = 10, fraction = 0, message = "setId必须为数字")
    @Positive(message = "setId必须为正数")
    private String setId;

    /** 音乐文件名 */
    @NotBlank(message = "musicFile不能为空")
    private String musicFile;
}
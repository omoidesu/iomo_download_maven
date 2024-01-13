package com.omoi.iomo_download.entity.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author omoi
 * @date 2024/1/13
 */
@Data
public class BpRequest {
    @NotBlank(message = "osuName不能为空")
    private String osuName;

    @NotEmpty(message = "setIdList不能为空")
    @Size(min = 1, max = 50, message = "setIdList长度必须在1-50之间")
    private List<String> setIdList;
}

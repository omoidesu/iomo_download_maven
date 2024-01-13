package com.omoi.iomo_download.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * @author omoi
 * @date 2024/1/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadInfo {
    private String setId;
    private Path filePath;
}

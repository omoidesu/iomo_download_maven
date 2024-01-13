package com.omoi.iomo_download.service;

import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author omoi
 * @date 2024/1/10
 */
public interface MinioService {
    /**
     * 上传文件至minio
     *
     * @param fileName 文件名
     * @param input    文件流
     * @param size     文件大小
     * @param type     文件类型
     * @param keyword  关键字
     * @return 外部下载地址
     */
    String uploadFile(String fileName, InputStream input, long size, String type, String keyword) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}

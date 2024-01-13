package com.omoi.iomo_download.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.omoi.iomo_download.config.MinioConfig;
import com.omoi.iomo_download.entity.dto.MinioResource;
import com.omoi.iomo_download.service.MinioResourceService;
import com.omoi.iomo_download.service.MinioService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author omoi
 * @date 2024/1/10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioConfig minioConfig;
    private final MinioResourceService minioResourceService;

    private MinioClient client = null;
    private static final Long PART_SIZE = 5 * 1024 * 1024L;

    @PostConstruct
    private void init() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (ObjectUtil.isNotNull(client)) {
            return;
        }

        client = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build())) {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
        }
    }

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
    @Override
    public String uploadFile(String fileName, InputStream input, long size, String type, String keyword) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PutObjectArgs object = PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(fileName)
                .stream(input, size, PART_SIZE)
                .build();

        client.putObject(object);

        String downloadUrl = CharSequenceUtil.format("{}/{}/{}", minioConfig.getUrl(), minioConfig.getBucketName(), fileName);

        MinioResource resource = new MinioResource();
        resource.setType(type);
        resource.setKeyword(keyword);
        resource.setUrl(downloadUrl);
        minioResourceService.save(resource);

        return downloadUrl;
    }
}

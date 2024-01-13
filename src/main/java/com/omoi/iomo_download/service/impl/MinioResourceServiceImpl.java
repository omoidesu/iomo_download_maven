package com.omoi.iomo_download.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.omoi.iomo_download.entity.dto.MinioResource;
import com.omoi.iomo_download.mapper.MinioResourceMapper;
import com.omoi.iomo_download.service.MinioResourceService;
import org.springframework.stereotype.Service;

/**
 * @author omoi
 * @date 2024/1/10
 */
@Service
public class MinioResourceServiceImpl extends ServiceImpl<MinioResourceMapper, MinioResource> implements MinioResourceService {
}

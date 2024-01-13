package com.omoi.iomo_download.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.omoi.iomo_download.entity.dto.OsuCookie;
import com.omoi.iomo_download.mapper.UserCookieMapper;
import com.omoi.iomo_download.service.UserCookieService;
import org.springframework.stereotype.Service;

/**
 * @author omoi
 * @date 2024/1/10
 */
@Service
public class UserCookieServiceImpl extends ServiceImpl<UserCookieMapper, OsuCookie> implements UserCookieService {
}

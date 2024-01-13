package com.omoi.iomo_download.service;

import java.util.List;

/**
 * @author omoi
 * @date 2024/1/1
 */
public interface OsuService {
    /**
     * 下载谱面并上传minio
     *
     * @param setId 谱面集id
     * @return minio下载地址
     */
    String downloadMap(String setId);

    /**
     * 上传音乐文件到kook
     *
     * @param setId     谱面集id
     * @param musicFile 音乐文件名
     * @return kook下载地址
     */
    String uploadMusic(String setId, String musicFile);

    /**
     * 批量下载bp并上传至minio
     *
     * @param osuUserName osu用户名
     * @param setIdList   谱面集id列表
     * @return minio下载地址
     */
    String createBpPack(String osuUserName, List<String> setIdList);
}

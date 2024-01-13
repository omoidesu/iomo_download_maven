package com.omoi.iomo_download.entity.dto;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author omoi
 * @date 2024/1/11
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "osu_cookie")
@TableName("osu_cookie")
public class OsuCookie {
    @Id
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Column(name = "session", columnDefinition = "text comment 'session'")
    private String session;

    @Column(name = "token", columnDefinition = "text comment 'token'")
    private String token;

    @Column(name = "expire_at", columnDefinition = "bigint comment '过期时间'")
    private Long expireAt;

    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}

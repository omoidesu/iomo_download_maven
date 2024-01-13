package com.omoi.iomo_download.entity.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

/**
 * @author omoi
 * @date 2024/1/11
 */
@Entity
@Table(name = "minio_resource")
@Data
public class MinioResource {
    @Id
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Column(name = "type", columnDefinition = "varchar(255) comment '类型'")
    private String type;

    @Column(name = "keyword", columnDefinition = "varchar(255) comment '关键字'")
    private String keyword;

    @Column(name = "url", columnDefinition = "varchar(255) comment '外部地址'")
    private String url;

    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
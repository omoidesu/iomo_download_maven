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
@Table(name = "osu_audio")
@Data
public class OsuAudio {
    @Id
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Column(name = "set_id", columnDefinition = "varchar(255) comment 'set id'")
    private String setId;

    @Column(name = "kook_url", columnDefinition = "varchar(255) comment 'kook url'")
    private String kookUrl;

    @Column(name = "create_time", columnDefinition = "date comment '创建时间'")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
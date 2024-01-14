package com.omoi.iomo_download.entity.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@Entity
@Table(name = "osz_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OszFile {
    @Id
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Column(name = "set_id", columnDefinition = "varchar(10) comment '谱面集id'")
    private String setId;

    @Column(name = "path", columnDefinition = "varchar(255) comment '文件路径'")
    private String path;

    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
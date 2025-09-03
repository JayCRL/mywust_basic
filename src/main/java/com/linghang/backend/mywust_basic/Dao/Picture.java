package com.linghang.backend.mywust_basic.Dao;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体类，对应数据库表（假设表名为file）
 */
@Data
@TableName("picture") // 若表名与类名不同，请修改为实际表名
public class Picture {

    /**
     * 主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Integer pid;

    /**
     * 文件URL地址
     */
    private String url;

    /**
     * 文件状态
     * 可根据实际业务定义，例如：1-正常，2-审核中，3-禁用等
     */
    private Integer status;

    /**
     * 删除标记
     * 通常用于逻辑删除：0-未删除，1-已删除
     */
    private Integer ifdelete;

    /**
     * 创建者ID（非空）
     * 关联上传用户的ID
     */
    private Integer createdId;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime uploadTime;

    /**
     * 状态更新时间
     * 数据库已配置自动更新，代码层面禁止手动更新
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime statusUpdatedTime;
}


package com.linghang.backend.mywust_basic.Dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime; // 推荐使用JDK8+的时间类，替代java.util.Date
import java.util.Date;

/**
 * 通知实体类（与数据库notice表映射）
 */
@TableName("notice") // 指定对应数据库表名（若类名与表名一致可省略，此处显式声明更清晰）
public class Notice {

    /**
     * 主键ID（对应数据库id字段）
     * - type = IdType.AUTO：自增策略（与数据库auto_increment匹配）
     * - @TableId：标记为主键字段
     */
    @TableId(type = IdType.AUTO)
    private Integer id; // 数据库id字段为int类型，实体类用Integer匹配（避免long类型冗余）

    /**
     * 通知标题（对应数据库title字段）
     * - 数据库字段为varchar(255)，实体类用String匹配
     * - 允许为null（数据库YES），无需额外注解
     */
    private String title;

    /**
     * 通知内容（对应数据库context字段）
     * - 注意：数据库字段是context，实体类属性名需与之一致（避免拼写错误）
     */
    private String context;

    /**
     * 通知状态（对应数据库status字段）
     * - 数据库为int类型，实体类用Integer匹配（支持null）
     * - 可补充枚举注释：例如 1=已发布，0=未发布，-1=已删除
     */
    private Integer status;
    /**
     * 发布平台（对应数据库platform字段）
     * - 数据库为int类型，实体类用Integer匹配（支持null）
     * - 可补充枚举注释：例如 1=ios，2=安卓
     */
    private Integer platform;
    /**
     * 发布范围（对应数据库platform字段）
     * - 数据库为int类型，实体类用Integer匹配（支持null）
     * - 可补充枚举注释：例如 1-22 学院名
     */
    private Integer catogories;

    /**
     * 创建时间（对应数据库createdAt字段）
     * - @TableField(fill = FieldFill.INSERT)：插入时自动填充（需配合元对象处理器）
     * - 数据库字段为datetime，实体类用LocalDateTime（JDK8+推荐，兼容时区）
     */
    @TableField(value="createdAt",fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 创建人ID（对应数据库createdId字段）
     * - 数据库为bigint类型，实体类用Long匹配
     * - 通常存储创建者的用户ID（如管理员ID、学生ID等）
     */
    @TableField(value="createdId",fill = FieldFill.INSERT)

    private Long createdId;
    /**
     * 更新时间（对应数据库updatedAt字段）
     * - 数据库已配置：DEFAULT CURRENT_TIMESTAMP + on update CURRENT_TIMESTAMP
     * - @TableField(updateStrategy = FieldStrategy.NEVER)：禁止代码层面手动更新（完全依赖数据库自动更新）
     * - 若希望代码层面也能触发更新，可改为 @TableField(fill = FieldFill.INSERT_UPDATE)（需元对象处理器）
     */
    @TableField(value="updatedAt",updateStrategy = FieldStrategy.NEVER)
    private Date updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public Integer getCatogories() {
        return catogories;
    }

    public void setCatogories(Integer catogories) {
        this.catogories = catogories;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedId() {
        return createdId;
    }

    public void setCreatedId(Long createdId) {
        this.createdId = createdId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
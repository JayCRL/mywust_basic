package com.linghang.backend.mywust_basic.dto;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

public class NoticeDto {
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

    public NoticeDto(String title, String context, Integer status, Integer platform, Integer catogories) {
        this.title = title;
        this.context = context;
        this.status = status;
        this.platform = platform;
        this.catogories = catogories;
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
}

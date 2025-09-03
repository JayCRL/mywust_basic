package com.linghang.backend.mywust_basic.Dao;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;

/**
 * 操作日志实体类
 * 对应数据库表：operation_log
 */
public class OperationLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     * 操作内容
     */
    private String operateContent;

    // 无参构造函数
    public OperationLog() {
    }

    // 带参构造函数（不含id，因为id是自增的）
    public OperationLog(String operatorId, Date operateTime, String operateContent) {
        this.operatorId = operatorId;
        this.operateTime = operateTime;
        this.operateContent = operateContent;
    }

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateContent() {
        return operateContent;
    }

    public void setOperateContent(String operateContent) {
        this.operateContent = operateContent;
    }

    // toString方法，便于日志打印和调试
    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", operatorId='" + operatorId + '\'' +
                ", operateTime=" + operateTime +
                ", operateContent='" + operateContent + '\'' +
                '}';
    }
}

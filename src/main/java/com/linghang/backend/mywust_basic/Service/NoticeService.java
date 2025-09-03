package com.linghang.backend.mywust_basic.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.linghang.backend.mywust_basic.Dao.Notice;
import com.linghang.backend.mywust_basic.Mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoticeService {
    @Autowired
    NoticeMapper noticeMapper;
    //获取所有通知
    List<Notice> list() {
        // 直接调用MyBatis-Plus提供的selectList方法，不设置查询条件即查询所有
        return noticeMapper.selectList(null);
    }

    //获取已发布的通知
    List<Notice> listPublishedNotice() {
        // 创建查询条件构造器
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 设置查询条件：status = 1（已发布状态）
        queryWrapper.eq("status", 1);
        // 可以根据需要添加排序条件，例如按发布时间降序
        queryWrapper.orderByDesc("publish_time");
        // 执行查询
        return noticeMapper.selectList(queryWrapper);
    }
    //添加新通知
    int addNotice(Notice notice){
        return  noticeMapper.insert(notice);
    }
    /**
     * 自动删除状态为-1的通知
     * @return 删除的记录数
     */
    int autoDeleteInvalidNotices() {
        // 创建删除条件构造器
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 设置条件：status = -1
        queryWrapper.eq("status", -1).orderByDesc("publish_time");
        // 执行删除操作
        return noticeMapper.delete(queryWrapper);
    }

    //忽略通知
    boolean ignoreNotice(Long id) {
        if (id == null) {
            return false; // 处理空ID的情况
        }
        // 创建更新条件构造器
        UpdateWrapper<Notice> updateWrapper = new UpdateWrapper<>();
        // 设置更新条件：id等于指定ID
        updateWrapper.eq("id", id);
        // 设置要更新的字段：status = 0
        updateWrapper.set("status", 0);
        // 执行更新操作
        int rows = noticeMapper.update(null, updateWrapper);
        // 返回是否更新成功（更新了至少一行数据）
        return rows > 0;
    }

    //批量发布通知
    boolean publishedNotices(List<Long> ids) {
        // 创建更新条件构造器
        UpdateWrapper<Notice> updateWrapper = new UpdateWrapper<>();
        // 设置更新条件：id 在指定的 ids 列表中
        updateWrapper.in("id", ids);
        // 设置要更新的字段：status = 1
        updateWrapper.set("status", 1);
        // 执行批量更新操作
        int rows = noticeMapper.update(null, updateWrapper);
        // 返回是否更新成功（至少更新了一行数据）
        return rows > 0;
    }
}

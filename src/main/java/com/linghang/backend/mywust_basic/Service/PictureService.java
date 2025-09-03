package com.linghang.backend.mywust_basic.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.linghang.backend.mywust_basic.Dao.Notice;
import com.linghang.backend.mywust_basic.Dao.Picture;
import com.linghang.backend.mywust_basic.Mapper.PictureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PictureService {
    @Autowired
    PictureMapper pictureMapper;

   public int addPicture(Picture picture){
       return pictureMapper.insert(picture);
    }
    //查询图片信息
    public Picture getPicture(Long pid) {
        // 创建查询条件构造器
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 添加条件：id等于pid，且ifDelete为false
        queryWrapper.eq("id", pid)
                .eq("ifdelete", false); // 假设数据库字段名为if_delete，若实体类属性与表字段映射正确，也可直接用属性名ifDelete
        // 根据条件查询单条记录
        return pictureMapper.selectOne(queryWrapper);
    }

    //通过图片
    public int accpetPictures(List<Long> pictureIds){
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("pid",pictureIds);
        updateWrapper.set("status",1);
        return pictureMapper.update(null,updateWrapper);
    }
    public int ignorePictures(List<Long> pictureIds){
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("pid",pictureIds);
        updateWrapper.set("status",0);
        return pictureMapper.update(null,updateWrapper);
    }
    public int deletePictures(List<Long> pictureIds){
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("pid",pictureIds);
        updateWrapper.set("ifdelete",1);
        return pictureMapper.update(null,updateWrapper);
    }
    public List<Picture> list(){
        // 创建查询条件构造器
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 设置查询条件：status = 1（已发布状态）
        queryWrapper.eq("ifdelete", 0);
        // 可以根据需要添加排序条件，例如按发布时间降序
        queryWrapper.orderByDesc("uploadTime");
       return pictureMapper.selectList(queryWrapper);
   }
}

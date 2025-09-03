package com.linghang.backend.mywust_basic.Service;
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
    //通过图片
    public int accpetPictures(List<Long> pictureIds){
        UpdateWrapper<Picture> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("pid",pictureIds);
        updateWrapper.set("status",1);
        return pictureMapper.update(null,updateWrapper);
    }

}

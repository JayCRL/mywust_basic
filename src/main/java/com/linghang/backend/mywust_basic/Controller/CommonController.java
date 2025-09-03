package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Dao.Picture;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Mapper.PictureMapper;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Service.PictureService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.AliOssUtil;
import com.linghang.backend.mywust_basic.Utils.R;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.date.DateTime.now;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private TokenService tokenService;
    @Autowired
    PictureService pictureService;
    @Autowired
    OperationService operationService;
    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        logger.info("图片上传：{}",file);
        String filePath = aliOssUtil.upload(file);
        String userName=getUserNameFromSecurityContext();
        Picture picture=new Picture();
        //上传者id
        picture.setCreatedId(Integer.valueOf(userName));
        //是否被删除
        picture.setIfdelete(0);
        //上传时间
        picture.setUploadTime(LocalDateTime.now());
        //图片网络地址
        picture.setUrl(filePath);
        //已上传未发布
        picture.setStatus(0);
        //添加图片
        pictureService.addPicture(picture);
        return R.success(filePath);
    }

    //未加权限控制 只是加了日志生成
    @PostMapping("/AcceptPicture")
    public R<String> acceptPicture(List<Long> picturesId){
    int number= pictureService.accpetPictures(picturesId);
        if(number!=0){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("通过图片审核 更新行数："+number);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            return R.success("success！！！更新行数："+number);
        }else{
            return R.failure(300,"error操作失败");
        }
    }

    //从请求头和缓存redis获取UserName
    private String getUserNameFromSecurityContext() {
        Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userInfo instanceof UserInfo userInfo1) {
            return tokenService.getUid(userInfo1.getToken());
        }
        return null;
    }
}
package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Dao.Picture;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
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
public class PictureController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private TokenService tokenService;
    @Autowired
    PictureService pictureService;
    @Autowired
    OperationService operationService;
    static {
        logger.info("轮播图控制器注册成功！！！");
    }
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
    @PostMapping("/acceptPicture")
    public R<String> acceptPicture(List<Long> picturesId){
    int number= pictureService.accpetPictures(picturesId);
        if(number!=0){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("通过图片审核 更新行数："+number);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            operationService.addOperationLog(operationLog);
            if(operationLog.getId()!=null){
                logger.info("通过图片审核操作写入日志成功 userName："+getUserNameFromSecurityContext());
            }
            return R.success("success！！！更新行数："+number);
        }else{
            return R.failure(300,"error操作失败");
        }
    }
    @PostMapping("/ignorePicture")
    public R<String> ignorePicture(List<Long> picturesId){
        int number= pictureService.ignorePictures(picturesId);
        if(number!=0){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("撤销图片发布 更新行数："+number);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            operationService.addOperationLog(operationLog);
            if(operationLog.getId()!=null){
                logger.info("撤消图片操作写入日志成功 userName："+getUserNameFromSecurityContext());
            }
            return R.success("success！！！更新行数："+number);
        }else{
            return R.failure(300,"error操作失败");
        }
    }
    //删除图片
    @PostMapping("/deletePicture")
    public R<String> deletePicture(List<Long> picturesId){
        int number= pictureService.deletePictures(picturesId);
        if(number!=0){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("删除图片 更新行数："+number);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            operationService.addOperationLog(operationLog);
            if(operationLog.getId()!=null){
                logger.info("删除图片操作写入日志成功 userName："+getUserNameFromSecurityContext());
            }
            return R.success("success！！！更新行数："+number);
        }else{
            return R.failure(300,"error操作失败");
        }
    }
    //列出图片
    @PostMapping("/listPictures")
    public R<List<Picture>> listPictures(){
        List<Picture> pictures=pictureService.list();
       if(!pictures.isEmpty()){
           return  R.success(pictures);
       }else{
           return R.failure(300,"查询结果为空");
       }
    }
    //获取图片详细
    @PostMapping("/getPictureDetail")
    public R<Picture> getPictureDetail(Long pid){
       Picture picture=pictureService.getPicture(pid);
       if(picture!=null){
           return R.success(picture);
       }else{
           return R.failure(300,"not found");
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
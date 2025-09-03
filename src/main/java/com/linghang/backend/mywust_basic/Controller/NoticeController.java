package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.Notice;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Service.NoticeService;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.R;
import com.linghang.backend.mywust_basic.dto.NoticeDto;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;
import static cn.hutool.core.date.DateTime.now;
@RestController
@RequestMapping("/operationLog")
public class NoticeController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    OperationService operationService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    private TokenService tokenService;
    static {
        logger.info("通知控制器注册成功！！！");
    }
    @GetMapping("/listAll")
    R<List<Notice>> listAll(){
        List<Notice> list=noticeService.list();
        return  R.success(list);
    }

    //查看已发布的通知
    @GetMapping("/list/published")
    R<List<Notice>> listPublishedNotice(){
        return R.success(noticeService.listPublishedNotice());
    }

    //查看已发布的安卓端通知
    @GetMapping("/list/published")
    R<List<Notice>> listPublishedNoticeButIos(){
        return R.success(noticeService.listPublishedNoticeButIos());
    }

    //查看已发布的通知
    @GetMapping("/list/published")
    R<List<Notice>> listPublishedNoticeButAndoid(){
        return R.success(noticeService.listPublishedNoticeButIos());
    }

    //撤销通知并写入日志
    @PostMapping("/ignore")
    R<String> ignoreNotice( @Parameter(description = "通知ID") @PathVariable Long id){
        if(noticeService.ignoreNotice(id)){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("撤回通知 userName："
                    +getUserNameFromSecurityContext()+"通知id:"+id);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            operationService.addOperationLog(operationLog);
            if(operationLog.getId()!=null){
                logger.info("撤回通知操作写入日志成功 userName："+getUserNameFromSecurityContext()+"通知id:"+id);
            }
            return R.success("success");
        }else{
            return R.failure(300,"error");
        }
    }

    //添加通知并写入日志
    @PostMapping("/addNotice")
    R<String> addNotice(@RequestBody NoticeDto noticeDto){
        Notice notice=new Notice();
        notice.setCatogories(noticeDto.getCatogories());
        notice.setContext(noticeDto.getContext());
        notice.setPlatform(noticeDto.getPlatform());
        notice.setStatus(0);
        notice.setCreatedId(Long.valueOf(Objects.requireNonNull(getUserNameFromSecurityContext())));
        noticeService.addNotice(notice);
        if(notice.getId()!=null){
            OperationLog operationLog=new OperationLog();
            operationLog.setOperateContent("添加通知 userName："+getUserNameFromSecurityContext()+"通知id:"+notice.getId());
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(getUserNameFromSecurityContext());
            operationService.addOperationLog(operationLog);
            if(operationLog.getId()!=null){
                logger.info("添加通知操作写入日志成功 userName："+getUserNameFromSecurityContext()+"通知id:"+notice.getId());
            }
            return R.success("success");
        }else{
            return R.failure(300,"Error");
        }
    }

    private void recordOperationLog(String operator, List<Long> noticeIds) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperateContent(String.format("发布通知，用户：%s，通知IDs：%s", operator, noticeIds));
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(operator); // 注意：operatorId通常是用户ID，这里如果存用户名需确认字段设计
            operationService.addOperationLog(operationLog);
        } catch (Exception e) {
            logger.error("记录操作日志失败，用户：{}，通知IDs：{}", operator, noticeIds, e);
        }
    }

    //发布通知并写入日志
    @PostMapping("/publishedNotices")
    R<String> publishedNotices(@RequestBody List<Long> ids){
        if(noticeService.publishedNotices(ids)){
            recordOperationLog(getUserNameFromSecurityContext(),ids);
            return R.success("success");
        }else{
            return R.failure(300,"Error");
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

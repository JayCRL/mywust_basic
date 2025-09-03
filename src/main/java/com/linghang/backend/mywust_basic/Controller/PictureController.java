package com.linghang.backend.mywust_basic.Controller;

import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Dao.Picture;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Service.PictureService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.AliOssUtil;
import com.linghang.backend.mywust_basic.Utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static cn.hutool.core.date.DateTime.now;

/**
 * 图片管理控制器
 * 负责处理图片上传、审核、删除等管理操作
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Tag(name = "图片管理接口", description = "提供图片上传、审核、删除等管理功能接口")
public class PictureController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private OperationService operationService;

    static {
        logger.info("轮播图控制器注册成功！");
    }

    /**
     * 图片上传接口
     * 将图片上传至阿里云OSS，并保存图片信息到数据库
     *
     * @param file 上传的图片文件
     * @return 包含图片URL的响应结果
     */
    @PostMapping("/upload")
    @Operation(summary = "图片上传", description = "将图片上传至阿里云OSS，返回图片访问URL并保存记录")
    @Parameter(name = "file", description = "图片文件", required = true)
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        logger.info("图片上传：{}", file.getOriginalFilename());

        try {
            // 上传图片到OSS
            String filePath = aliOssUtil.upload(file);
            String userName = getUserNameFromSecurityContext();

            // 构建图片实体
            Picture picture = new Picture();
            picture.setCreatedId(Long.valueOf(userName));  // 设置上传者ID
            picture.setIfdelete(0);  // 未删除状态
            picture.setUploadTime(LocalDateTime.now());  // 上传时间
            picture.setUrl(filePath);  // 图片URL
            picture.setStatus(0);  // 未发布状态

            // 保存图片记录
            pictureService.addPicture(picture);
            return R.success(filePath);
        } catch (Exception e) {
            logger.error("图片上传失败", e);
            return R.failure(500, "图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 图片审核通过接口
     * 批量审核通过图片，更新图片状态
     *
     * @param picturesId 图片ID列表
     * @return 审核结果
     */
    @PostMapping("/acceptPicture")
    @Operation(summary = "图片审核通过", description = "批量审核通过图片，更新图片状态为已发布")
    @Parameter(name = "picturesId", description = "需要审核通过的图片ID列表", required = true)
    public R<String> acceptPicture(@RequestBody List<Long> picturesId) {
        if (picturesId == null || picturesId.isEmpty()) {
            logger.warn("图片审核通过失败：图片ID列表为空");
            return R.failure(400, "图片ID列表不能为空");
        }

        try {
            int number = pictureService.accpetPictures(picturesId);
            if (number > 0) {
                // 记录操作日志
                recordOperationLog("通过图片审核", number);
                return R.success("操作成功！更新行数：" + number);
            } else {
                return R.failure(300, "操作失败：未找到对应图片或无需更新");
            }
        } catch (Exception e) {
            logger.error("图片审核通过失败", e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 撤销图片发布接口
     * 批量撤销已发布的图片，更新图片状态
     *
     * @param picturesId 图片ID列表
     * @return 操作结果
     */
    @PostMapping("/ignorePicture")
    @Operation(summary = "撤销图片发布", description = "批量撤销已发布的图片，更新图片状态为未发布")
    @Parameter(name = "picturesId", description = "需要撤销发布的图片ID列表", required = true)
    public R<String> ignorePicture(@RequestBody List<Long> picturesId) {
        if (picturesId == null || picturesId.isEmpty()) {
            logger.warn("撤销图片发布失败：图片ID列表为空");
            return R.failure(400, "图片ID列表不能为空");
        }

        try {
            int number = pictureService.ignorePictures(picturesId);
            if (number > 0) {
                // 记录操作日志
                recordOperationLog("撤销图片发布", number);
                return R.success("操作成功！更新行数：" + number);
            } else {
                return R.failure(300, "操作失败：未找到对应图片或无需更新");
            }
        } catch (Exception e) {
            logger.error("撤销图片发布失败", e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 删除图片接口
     * 批量删除图片记录（逻辑删除）
     *
     * @param picturesId 图片ID列表
     * @return 操作结果
     */
    @PostMapping("/deletePicture")
    @Operation(summary = "删除图片", description = "批量逻辑删除图片记录")
    @Parameter(name = "picturesId", description = "需要删除的图片ID列表", required = true)
    public R<String> deletePicture(@RequestBody List<Long> picturesId) {
        if (picturesId == null || picturesId.isEmpty()) {
            logger.warn("删除图片失败：图片ID列表为空");
            return R.failure(400, "图片ID列表不能为空");
        }

        try {
            int number = pictureService.deletePictures(picturesId);
            if (number > 0) {
                // 记录操作日志
                recordOperationLog("删除图片", number);
                return R.success("操作成功！更新行数：" + number);
            } else {
                return R.failure(300, "操作失败：未找到对应图片或无需更新");
            }
        } catch (Exception e) {
            logger.error("删除图片失败", e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 图片列表查询接口
     * 查询所有图片记录
     *
     * @return 包含图片列表的响应结果
     */
    @PostMapping("/listPictures")
    @Operation(summary = "查询图片列表", description = "查询系统中所有图片记录")
    public R<List<Picture>> listPictures() {
        try {
            List<Picture> pictures = pictureService.list();
            return R.success(pictures);
        } catch (Exception e) {
            logger.error("查询图片列表失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 图片详情查询接口
     * 根据图片ID查询图片详细信息
     *
     * @param pid 图片ID
     * @return 包含图片详情的响应结果
     */
    @PostMapping("/getPictureDetail")
    @Operation(summary = "查询图片详情", description = "根据图片ID查询图片的详细信息")
    @Parameter(name = "pid", description = "图片ID", required = true)
    public R<Picture> getPictureDetail(@RequestParam("pid") Long pid) {
        if (pid == null) {
            logger.warn("查询图片详情失败：图片ID为空");
            return R.failure(400, "图片ID不能为空");
        }

        try {
            Picture picture = pictureService.getPicture(pid);
            if (picture != null) {
                return R.success(picture);
            } else {
                return R.failure(300, "未找到对应图片");
            }
        } catch (Exception e) {
            logger.error("查询图片详情失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 记录操作日志
     * 封装操作日志记录逻辑，减少代码冗余
     *
     * @param operation 操作名称
     * @param number 影响行数
     */
    private void recordOperationLog(String operation, int number) {
        try {
            String userName = getUserNameFromSecurityContext();
            OperationLog operationLog = new OperationLog();
            operationLog.setOperateContent(operation + " 更新行数：" + number);
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(userName);

            operationService.addOperationLog(operationLog);

            if (operationLog.getId() != null) {
                logger.info("{}操作写入日志成功 userName：{}", operation, userName);
            }
        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
            // 日志记录失败不影响主业务流程，仅记录错误日志
        }
    }

    /**
     * 从Security上下文获取用户名
     * 封装用户信息获取逻辑，便于复用和维护
     *
     * @return 用户名
     */
    private String getUserNameFromSecurityContext() {
        try {
            Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userInfo instanceof UserInfo userInfo1) {
                return tokenService.getUid(userInfo1.getToken());
            }
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
        }
        return null;
    }
}

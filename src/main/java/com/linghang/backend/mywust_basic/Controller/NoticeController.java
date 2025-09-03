package com.linghang.backend.mywust_basic.Controller;

import com.linghang.backend.mywust_basic.Dao.Notice;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Service.NoticeService;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.R;
import com.linghang.backend.mywust_basic.dto.NoticeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.hutool.core.date.DateTime.now;

/**
 * 通知控制器
 * 负责处理通知的增删改查、发布与撤回等操作
 */
@RestController
@RequestMapping("/operationLog")
@Tag(name = "通知管理接口", description = "提供通知的查询、添加、发布、撤回等功能接口")
public class NoticeController {
    private static final Logger logger = LoggerFactory.getLogger(NoticeController.class);

    @Autowired
    private OperationService operationService;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private TokenService tokenService;

    static {
        logger.info("通知控制器注册成功！");
    }

    /**
     * 查询所有通知
     *
     * @return 包含所有通知列表的响应结果
     */
    @GetMapping("/listAll")
    @Operation(summary = "查询所有通知", description = "返回系统中所有通知，包括已发布和未发布的")
    public R<List<Notice>> listAll() {
        try {
            List<Notice> list = noticeService.list();
            return R.success(list);
        } catch (Exception e) {
            logger.error("查询所有通知失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已发布的通知
     *
     * @return 包含已发布通知列表的响应结果
     */
    @GetMapping("/list/published")
    @Operation(summary = "查询已发布的通知", description = "返回所有平台已发布的通知")
    public R<List<Notice>> listPublishedNotice() {
        try {
            return R.success(noticeService.listPublishedNotice());
        } catch (Exception e) {
            logger.error("查询已发布通知失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已发布的苹果端通知
     *
     * @return 包含已发布苹果端通知列表的响应结果
     */
    @GetMapping("/list/publishedButIos")
    @Operation(summary = "查询已发布的苹果端通知", description = "返回仅苹果端已发布的通知")
    public R<List<Notice>> listPublishedNoticeButIos() {
        try {
            return R.success(noticeService.listPublishedNoticeButIos());
        } catch (Exception e) {
            logger.error("查询已发布的苹果端通知失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询已发布的安卓端通知
     *
     * @return 包含已发布安卓端通知列表的响应结果
     */
    @GetMapping("/list/publishedButAndroid")
    @Operation(summary = "查询已发布的安卓端通知", description = "返回仅安卓端已发布的通知")
    public R<List<Notice>> listPublishedNoticeButAndroid() {
        try {
            // 修复原代码中的复制粘贴错误，调用正确的安卓端方法
            return R.success(noticeService.listPublishedNoticeButAndroid());
        } catch (Exception e) {
            logger.error("查询已发布的安卓端通知失败", e);
            return R.failure(500, "查询失败：" + e.getMessage());
        }
    }

    /**
     * 撤回通知
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @PostMapping("/ignore")
    @Operation(summary = "撤回通知", description = "根据通知ID撤回已发布的通知，并记录操作日志")
    @Parameter(name = "id", description = "需要撤回的通知ID", required = true)
    public R<String> ignoreNotice(@RequestParam Long id) {
        if (id == null) {
            logger.warn("撤回通知失败：通知ID为空");
            return R.failure(400, "通知ID不能为空");
        }

        try {
            if (noticeService.ignoreNotice(id)) {
                String userName = getUserNameFromSecurityContext();
                OperationLog operationLog = new OperationLog();
                operationLog.setOperateContent("撤回通知 userName：" + userName + " 通知id:" + id);
                operationLog.setOperateTime(now());
                operationLog.setOperatorId(userName);
                operationService.addOperationLog(operationLog);

                if (operationLog.getId() != null) {
                    logger.info("撤回通知操作写入日志成功 userName：{} 通知id:{}", userName, id);
                }
                return R.success("success");
            } else {
                return R.failure(300, "撤回通知失败：未找到对应通知或状态异常");
            }
        } catch (Exception e) {
            logger.error("撤回通知失败，通知ID：{}", id, e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 添加通知
     *
     * @param noticeDto 通知DTO对象
     * @return 操作结果
     */
    @PostMapping("/addNotice")
    @Operation(summary = "添加通知", description = "创建新通知并记录操作日志，初始状态为未发布")
    @Parameter(name = "noticeDto", description = "通知信息DTO", required = true)
    public R<String> addNotice(@RequestBody NoticeDto noticeDto) {
        if (noticeDto == null) {
            logger.warn("添加通知失败：通知信息为空");
            return R.failure(400, "通知信息不能为空");
        }

        try {
            String userName = getUserNameFromSecurityContext();
            if (userName == null) {
                return R.failure(401, "未获取到当前用户信息");
            }

            Notice notice = new Notice();
            notice.setCatogories(noticeDto.getCatogories());
            notice.setContext(noticeDto.getContext());
            notice.setPlatform(noticeDto.getPlatform());
            notice.setTitle(noticeDto.getTitle());
            notice.setCreatedAt(now());
            notice.setStatus(0); // 初始状态：未发布
            notice.setCreatedId(Long.valueOf(userName));
            noticeService.addNotice(notice);

            if (notice.getId() != null) {
                OperationLog operationLog = new OperationLog();
                operationLog.setOperateContent("添加通知 userName：" + userName + " 通知id:" + notice.getId());
                operationLog.setOperateTime(now());
                operationLog.setOperatorId(userName);
                operationService.addOperationLog(operationLog);

                if (operationLog.getId() != null) {
                    logger.info("添加通知操作写入日志成功 userName：{} 通知id:{}", userName, notice.getId());
                }
                return R.success("success");
            } else {
                return R.failure(300, "添加通知失败：数据库操作未返回ID");
            }
        } catch (NumberFormatException e) {
            logger.error("用户ID格式转换失败：{}", getUserNameFromSecurityContext(), e);
            return R.failure(400, "用户ID格式错误");
        } catch (Exception e) {
            logger.error("添加通知失败", e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 发布通知
     *
     * @param ids 通知ID列表
     * @return 操作结果
     */
    @PostMapping("/publishedNotices")
    @Operation(summary = "发布通知", description = "批量发布通知，并记录操作日志")
    @Parameter(name = "ids", description = "需要发布的通知ID列表", required = true)
    public R<String> publishedNotices(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("发布通知失败：通知ID列表为空");
            return R.failure(400, "通知ID列表不能为空");
        }

        try {
            if (noticeService.publishedNotices(ids)) {
                String userName = getUserNameFromSecurityContext();
                recordOperationLog(userName, ids);
                return R.success("success");
            } else {
                return R.failure(300, "发布通知失败：未找到对应通知或状态异常");
            }
        } catch (Exception e) {
            logger.error("发布通知失败，通知IDs：{}", ids, e);
            return R.failure(500, "操作失败：" + e.getMessage());
        }
    }

    /**
     * 记录操作日志
     *
     * @param operator  操作人
     * @param noticeIds 通知ID列表
     */
    private void recordOperationLog(String operator, List<Long> noticeIds) {
        try {
            if (operator == null || noticeIds == null || noticeIds.isEmpty()) {
                logger.warn("记录操作日志失败：参数不完整");
                return;
            }

            OperationLog operationLog = new OperationLog();
            operationLog.setOperateContent(String.format("发布通知，用户：%s，通知IDs：%s", operator, noticeIds));
            operationLog.setOperateTime(now());
            operationLog.setOperatorId(operator);
            operationService.addOperationLog(operationLog);
        } catch (Exception e) {
            logger.error("记录操作日志失败，用户：{}，通知IDs：{}", operator, noticeIds, e);
        }
    }

    /**
     * 从Security上下文获取用户名
     *
     * @return 用户名，获取失败返回null
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

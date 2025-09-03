package com.linghang.backend.mywust_basic.Controller;

import cn.wustlinghang.mywust.core.parser.graduate.GraduateCourseTableParser;
import cn.wustlinghang.mywust.core.parser.graduate.GraduateStudentInfoPageParser;
import cn.wustlinghang.mywust.core.parser.graduate.GraduateTrainingPlanPageParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradCourseTableParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradStudentInfoPageParser;
import cn.wustlinghang.mywust.core.parser.undergraduate.UndergradTrainingPlanPageParser;
import cn.wustlinghang.mywust.core.request.service.auth.GraduateLogin;
import cn.wustlinghang.mywust.core.request.service.auth.UndergraduateLogin;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateCourseTableApiService;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateStudentInfoApiService;
import cn.wustlinghang.mywust.core.request.service.graduate.GraduateTrainingPlanApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradCourseTableApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradStudentInfoApiService;
import cn.wustlinghang.mywust.core.request.service.undergraduate.UndergradTrainingPlanApiService;
import cn.wustlinghang.mywust.core.util.WustRequester;
import cn.wustlinghang.mywust.data.common.Course;
import cn.wustlinghang.mywust.data.common.StudentInfo;
import cn.wustlinghang.mywust.network.RequestClientOption;
import com.linghang.backend.mywust_basic.Entity.UnderGraduateLoginA;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "研究生接口", description = "提供课程、成绩、考试等功能接口")

@RestController
@RequestMapping("/GraduatedController")
public class GraduatedController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);

    private final GraduateLogin graduateLogin;
    private final WustRequester wustRequester;

    @Autowired
    private TokenService tokenService;
    GraduateStudentInfoApiService MystudentInfoApiService;
    GraduateStudentInfoPageParser StudentInfoparser;

    @Autowired
    public GraduatedController(WustRequester wustRequester,
                               GraduateLogin graduateLogin) {
        this.wustRequester = wustRequester;
        this.graduateLogin = graduateLogin;
        MystudentInfoApiService=new GraduateStudentInfoApiService(wustRequester);
        StudentInfoparser=new GraduateStudentInfoPageParser();
    }
    //登录系统并将个人信息及账号密码缓存到redis数据库中 过期时间为3天
    @Operation(summary = "登录研究生系统", description = "返回登录成功后的 Cookie")
    @PostMapping("/login")
    public R<String> login(@RequestBody UnderGraduateLoginA loginA) {
        try {
            String username = loginA.getUsername();
            String password = loginA.getPassword();
            // 获取登录后的 Cookie
            String cookie = graduateLogin.getLoginCookie(username, password, null);
            // 生成 JWT Token 并缓存 cookie
            String token = tokenService.createToken(username, cookie);
            try {
                String page = MystudentInfoApiService.getPage(cookie, null);
                StudentInfo studentInfo = StudentInfoparser.parse(page);
                tokenService.createUidToken(token,loginA.getUsername());
                tokenService.createName(token,studentInfo.getName());
                if (tokenService.setPassword(username,password)) logger.info("学号："+username+"学生缓存成功！！！");
            }catch (Exception e){
                logger.error(String.valueOf(e));
                return R.failure(500,"error");
            }
            return R.success(token);  //cr 返回JWT Token给客户端
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }
    @Operation(summary = "获取所有课程表", description = "获取此学期课程信息")
    @GetMapping("/GraduateGetCourses")
    R<List<Course>> GraduateGetCourses(){
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        try {
            GraduateCourseTableApiService courseTableApiService=new GraduateCourseTableApiService(wustRequester);
            GraduateCourseTableParser courseTableParser=new GraduateCourseTableParser();
            String page=courseTableApiService.getPage(cookie,null);
            List<Course> courseList = courseTableParser.parse(page);
            return R.success(courseList);
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,e.getMessage());
        }
    }
    @Operation(summary = "获取个人信息", description = "返回获取的个人信息")
    @GetMapping("/getStudentInfo")
    public R<StudentInfo> getStudentInfo()  {
        if(!IsValidCookie()){
            RefreshCookie();
        }
        String cookie = getCookieFromSecurityContext();
        GraduateStudentInfoApiService studentInfoApiService=new GraduateStudentInfoApiService(wustRequester);
        GraduateStudentInfoPageParser parser=new GraduateStudentInfoPageParser();
        try {
            String page = studentInfoApiService.getPage(cookie,null);
            StudentInfo studentInfo = parser.parse(page);
            return R.success(studentInfo);
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,"error");
        }
    }

    @Operation(summary = "获取培养方案", description = "返回 HTML 格式的培养方案内容")
    @GetMapping("/GraduateTrainingPlan")
    public R<String> GetTrainningPlan() {
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        if (cookie == null) return R.failure(401, "Unauthorized");
        try {
            GraduateTrainingPlanApiService service = new GraduateTrainingPlanApiService(wustRequester);
            GraduateTrainingPlanPageParser parser = new GraduateTrainingPlanPageParser();
            String page = service.getPage(cookie,null);
            String plan = parser.parse(page);
            return R.success(plan);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }
    private String getTokenFromSecurityContext() {
        Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userInfo instanceof UserInfo userInfo1) {
            return userInfo1.getToken();
        }
        return null;
    }
    private String getCookieFromSecurityContext() {
        Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userInfo instanceof UserInfo userInfo1) {
            return userInfo1.getCookie();
        }
        return null;
    }
    boolean IsValidCookie(){
        String cookie = getCookieFromSecurityContext();
        UndergradStudentInfoApiService studentInfoApiService=new UndergradStudentInfoApiService(wustRequester);
        UndergradStudentInfoPageParser parser=new UndergradStudentInfoPageParser();
        try {
            String page = studentInfoApiService.getPage(cookie);
            parser.parse(page);
            return true;
        }catch (Exception e){
            logger.error(tokenService.getUid(getTokenFromSecurityContext())+": "+ e);
            return false;
        }
    }
    boolean RefreshCookie(){
        String token=getTokenFromSecurityContext();
        try {
            String username = tokenService.getUid(token);
            String password = tokenService.getPassword(username);
            // 获取登录后的 Cookie
            String cookie = graduateLogin.getLoginCookie(username, password, null);
            // 生成 JWT Token 并缓存 cookie
            tokenService.refreshTokenInfo(token, cookie);
            RefreshCookieFromSecurityContext();
            return true;
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return false;
        }
    }
    private boolean RefreshCookieFromSecurityContext() {
        // 1. 获取当前认证对象
        UsernamePasswordAuthenticationToken oldAuth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        // 2. 获取旧的UserInfo并更新字段（假设UserInfo有setter方法）
        UserInfo userInfo = (UserInfo) oldAuth.getPrincipal();
        String newCookie=tokenService.getUserFromToken(userInfo.getToken());
        userInfo.setCookie(newCookie); // 只更新需要变更的字段（如cookie）
        // 3. 用更新后的UserInfo创建新令牌，复用旧令牌的其他属性
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                userInfo,               // 新的UserInfo
                oldAuth.getCredentials(), // 复用旧的凭证（你的场景中是null）
                oldAuth.getAuthorities()  // 复用旧的权限（你的场景中是null）
        );
        // 4. 保留原有的请求细节（IP、会话信息），或用新请求重新构建（建议保留旧的）
        newAuth.setDetails(oldAuth.getDetails()); // 直接复用旧的details
        // 5. 重新设置到安全上下文
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return true;
    }
}

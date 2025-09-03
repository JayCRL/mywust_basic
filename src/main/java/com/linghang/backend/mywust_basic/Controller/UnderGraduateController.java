package com.linghang.backend.mywust_basic.Controller;
import cn.wustlinghang.mywust.core.parser.undergraduate.*;
import cn.wustlinghang.mywust.core.request.service.auth.UndergraduateLogin;
import cn.wustlinghang.mywust.core.request.service.undergraduate.*;
import cn.wustlinghang.mywust.core.util.WustRequester;
import cn.wustlinghang.mywust.data.common.Course;
import cn.wustlinghang.mywust.data.common.Score;
import cn.wustlinghang.mywust.data.common.StudentInfo;
import cn.wustlinghang.mywust.exception.ApiException;
import cn.wustlinghang.mywust.exception.ParseException;
import com.linghang.backend.mywust_basic.Entity.UnderGraduateLoginA;
import com.linghang.backend.mywust_basic.Entity.UserInfo;
import com.linghang.backend.mywust_basic.Service.TokenService;
import com.linghang.backend.mywust_basic.Utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@Tag(name = "本科生接口", description = "提供课程、成绩、考试等功能接口")
@RestController
@RequestMapping("/UnderGraduateStudent")
public class UnderGraduateController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    private TokenService tokenService;
    private final WustRequester wustRequester;
    private final UndergraduateLogin undergraduateLogin;
    UndergradStudentInfoApiService MystudentInfoApiService;
    UndergradStudentInfoPageParser StudentInfoparser;
    //自动注入
    static {
        logger.info("本科生控制器注册成功！！！");
    }
    @Value("${WustHelper.term}")
    String this_term;
    @Autowired
    public UnderGraduateController(WustRequester wustRequester,
                                   UndergraduateLogin undergraduateLogin) {
        this.wustRequester = wustRequester;
        this.undergraduateLogin = undergraduateLogin;
        MystudentInfoApiService=new UndergradStudentInfoApiService(wustRequester);
         StudentInfoparser=new UndergradStudentInfoPageParser();
    }
    //登录系统并将个人信息及账号密码缓存到redis数据库中 过期时间为3天
    @Operation(summary = "登录本科生系统", description = "返回登录成功后的 Cookie")
    @PostMapping("/login")
    public R<String> login(@RequestBody UnderGraduateLoginA loginA) {
        try {
            String username = loginA.getUsername();
            String password = loginA.getPassword();
            // 获取登录后的 Cookie
            String cookie = undergraduateLogin.getLoginCookie(username, password, null);
            // 生成 JWT Token 并缓存 cookie
            String token = tokenService.createToken(username, cookie);
            try {
                String page = MystudentInfoApiService.getPage(cookie);
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
    @Operation(summary = "获取个人信息", description = "返回获取的个人信息")
    @GetMapping("/getStudentInfo")
    public R<StudentInfo> getStudentInfo()  {
        if(!IsValidCookie()){
            RefreshCookie();
        }
        String cookie = getCookieFromSecurityContext();
        UndergradStudentInfoApiService studentInfoApiService=new UndergradStudentInfoApiService(wustRequester);
        UndergradStudentInfoPageParser parser=new UndergradStudentInfoPageParser();
        try {
            String page = studentInfoApiService.getPage(cookie);
            StudentInfo studentInfo = parser.parse(page);
            return R.success(studentInfo);
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,"error");
        }
    }
    //查学期课程
    @Operation(summary = "获取成绩", description = "获取当前用户所有已出成绩")
    @GetMapping("/UnderGraduateGetScore")
    public R<List<Score>> getScores() {
        String cookie = getCookieFromSecurityContext();
        return getListR(cookie);
    }
    @Operation(summary = "获取所有课程表", description = "获取此学期课程信息")
    @GetMapping("/UnderGraduateGetCourses")
    R<List<Course>> UnderGraduateGetCourses(){
        if(!IsValidCookie()){
            if(!RefreshCookie()){
             return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        try {
            UndergradCourseTableApiService courseTableApiService=new UndergradCourseTableApiService(wustRequester);
            UndergradCourseTableParser courseTableParser=new UndergradCourseTableParser();
            String page=courseTableApiService.getPage(this_term,cookie);
            List<Course> courseList = courseTableParser.parse(page);
            return R.success(courseList);
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,e.getMessage());
        }
    }
    @Operation(summary = "获取单天课程表", description = "获取某天/某周的课程信息")
    @GetMapping("/UnderGraduateGetSingleCourses")
    R<List<Course>> GetSingleCourses(@RequestParam("date") String data){
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        try {
            List<Course> courseList=getSingleCourseList(data,getCookieFromSecurityContext());
            return R.success(courseList);
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,e.getMessage());
        }
    }

    @Operation(summary = "获取培养方案", description = "返回 HTML 格式的培养方案内容")
    @GetMapping("/UnderGraduateTrainingPlan")
    public R<String> GetTrainningPlan() {
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        if (cookie == null) return R.failure(401, "Unauthorized");
        try {
            UndergradTrainingPlanApiService service = new UndergradTrainingPlanApiService(wustRequester);
            UndergradTrainingPlanPageParser parser = new UndergradTrainingPlanPageParser();
            String page = service.getPage(cookie);
            String plan = parser.parse(page);
            return R.success(plan);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取学分修读情况", description = "获取总学分和通识选修等修读信息")
    @GetMapping("/GetCreditStatus")
    public R<Map<String, CreditStatusParser.CourseInfo>> GetCreditStatus() {
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        if (cookie == null) return R.failure(401, "Unauthorized");
        try {
            String page= CreditStatusPageGet.GetPage(cookie);
            Map<String, CreditStatusParser.CourseInfo> stringStringMap=CreditStatusParser.Parse(page);
            return R.success(stringStringMap);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }
    @Operation(summary = "获取毕业要求情况", description = "获取毕业要求及完成情况")
    @GetMapping("/GetGraduateRequireParse")
    public R<Map<String, Object>> GetGraduateRequireParse() {
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        String cookie = getCookieFromSecurityContext();
        if (cookie == null) return R.failure(401, "Unauthorized");
        try {
            String page= CreditStatusPageGet.GetPage(cookie);
            Map<String, Object> graduateRequire= GraduateRequireParser.Parse(page);
            return R.success(graduateRequire);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }

    @Operation(summary = "获取考试安排", description = "返回指定学期的考试安排")
    @GetMapping("/GetExam")
    R<String> GetExam(){
        if(!IsValidCookie()){
            if(!RefreshCookie()){
                return R.failure(400,"账号密码失效,请尝试重新登陆");
            }
        }
        try {
            String cookie = getCookieFromSecurityContext();
            return R.success(ExamFetcher.GetExamPage(this_term,cookie));
        }catch (Exception e){
            logger.error(String.valueOf(e));
            return R.failure(500,e.getMessage());
        }
    }
    private String getCookieFromSecurityContext() {
        Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userInfo instanceof UserInfo userInfo1) {
            return userInfo1.getCookie();
        }
        return null;
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

    private String getTokenFromSecurityContext() {
        Object userInfo = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userInfo instanceof UserInfo userInfo1) {
            return userInfo1.getToken();
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
            String cookie = undergraduateLogin.getLoginCookie(username, password, null);
            // 生成 JWT Token 并缓存 cookie
            tokenService.refreshTokenInfo(token, cookie);
            RefreshCookieFromSecurityContext();
            return true;
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return false;
        }
    }
    //获取成绩表
    @NotNull
    private R<List<Score>> getListR(String cookie) {
        try {
            UndergradScoreApiService apiService = new UndergradScoreApiService(wustRequester);
            UndergradScoreParser parser = new UndergradScoreParser();
            String page = apiService.getPage(cookie);
            List<Score> scoreList = parser.parse(page);
            return R.success(scoreList);
        } catch (Exception e) {
            logger.error(String.valueOf(e));
            return R.failure(500, e.getMessage());
        }
    }
    List<Course> getSingleCourseList(String data,String cookie) throws IOException, ApiException, ParseException {
        UndergradSingleWeekCourseApiService undergradSingleWeekCourseApiService=new UndergradSingleWeekCourseApiService(wustRequester);
        UndergradSingleWeekCourseParser parser=new UndergradSingleWeekCourseParser();
        String page=undergradSingleWeekCourseApiService.getPage(data,cookie);
        return parser.parse(page);
    }


}

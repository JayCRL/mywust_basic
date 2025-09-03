package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Tag(name = "日志管理接口", description = "提供日志查看等操作的接口")
@RequestMapping("/operationLog")
public class LogController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    OperationService oprationService;
    static {
        logger.info("日志控制器注册成功！！！");
    }
    //日志的查看
    /**
     * 日志的查看
     *
     * @return 日志集合List<OperationLog></>
     */
    @GetMapping("/list")
    @Operation(summary = "查看日志", description = "查看操作日志")
    public R<List<OperationLog>> list(){
        return  R.success(oprationService.list());
    }
}

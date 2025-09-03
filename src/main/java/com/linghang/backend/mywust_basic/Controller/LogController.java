package com.linghang.backend.mywust_basic.Controller;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Service.OperationService;
import com.linghang.backend.mywust_basic.Utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/operationLog")
public class LogController {
    private final static Logger logger= LoggerFactory.getLogger(LogController.class);
    @Autowired
    OperationService oprationService;
    static {
        logger.info("日志控制器注册成功！！！");
    }
    //日志的查看
    @RequestMapping("/list")
    public R<List<OperationLog>> list(){
        return  R.success(oprationService.list());
    }
}

package com.linghang.backend.mywust_basic.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Dao.Picture;
import com.linghang.backend.mywust_basic.Mapper.OperationMapper;
import io.swagger.v3.oas.models.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationService {
    @Autowired
    OperationMapper operationMapper;
    public   int addOperationLog(OperationLog operationLog){
       return operationMapper.insert(operationLog);
    }
    public List<OperationLog> list(){
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("operate_time");
        return operationMapper.selectList(queryWrapper);
    }
}

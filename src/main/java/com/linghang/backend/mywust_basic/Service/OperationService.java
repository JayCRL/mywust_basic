package com.linghang.backend.mywust_basic.Service;

import com.linghang.backend.mywust_basic.Dao.OperationLog;
import com.linghang.backend.mywust_basic.Mapper.OperationMapper;
import io.swagger.v3.oas.models.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationService {
    @Autowired
    OperationMapper operationMapper;
    public   int AddOperationLog(OperationLog operationLog){
       return operationMapper.insert(operationLog);
    }
}

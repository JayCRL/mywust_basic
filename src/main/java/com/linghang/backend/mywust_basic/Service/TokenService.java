package com.linghang.backend.mywust_basic.Service;

import com.linghang.backend.mywust_basic.Utils.AesEncryptor;
import com.linghang.backend.mywust_basic.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class TokenService {
    @Autowired
    AesEncryptor aesEncryptor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtils jwtUtils;
    public TokenService(RedisTemplate<String, Object> redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }
    // 生成Token并缓存用户获取的cookie，缓存过期时间7天
    public String createToken(String userId, Object cookie) {
        String token = jwtUtils.generateToken(userId);
        redisTemplate.opsForValue().set("Wust_basic_token:" + token, cookie, 7, TimeUnit.DAYS);
        createUidToken(token,userId);
        return token;
    }
    /**
     * 刷新token关联的用户信息（不重新创建token，不改变过期时间）
     * @param token 已存在的token
     * @param newUserInfo 新的用户信息（需要更新的值）
     * @return 是否刷新成功（true：token存在且更新成功；false：token不存在）
     */
    public boolean refreshTokenInfo(String token, Object newUserInfo) {
        String redisKey = "Wust_basic_token:" + token;
        // 1. 检查token是否存在（避免更新不存在的token）
        Boolean exists = redisTemplate.hasKey(redisKey);
        if(Boolean.FALSE.equals(exists)){
            return false;
        }
        // 2. 更新Redis中存储的userInfo，不指定过期时间（保留原有过期时间）
        redisTemplate.opsForValue().set(redisKey, newUserInfo);
        return true;
    }
    public String getUserFromToken(String token) {
        return (String) redisTemplate.opsForValue().get("Wust_basic_token:"+token);
    }
    //学号缓存
    public boolean createUidToken(String token,String username){
        redisTemplate.opsForValue().set("LingHangToken:"+token,username,7, TimeUnit.DAYS);
        return true;
    }

    public String getUid(String token){
        return (String) redisTemplate.opsForValue().get("LingHangToken:"+token);
    }
    //姓名缓存
    public boolean createName(String token,String username){
        redisTemplate.opsForValue().set("Student_name:"+token,username,7, TimeUnit.DAYS);
        return true;
    }
    public boolean setPassword(String username,String password) throws Exception {
        password= aesEncryptor.encrypt(password);
        redisTemplate.opsForValue().set("Student_number:"+username,password,7, TimeUnit.DAYS);
        return true;
    }
    public String getPassword(String username) throws Exception {
        return aesEncryptor.decrypt((String)redisTemplate.opsForValue().get("Student_number:"+username));
    }
    public String getName(String token) {
        return (String) redisTemplate.opsForValue().get("Student_name:"+token);
    }
    // 根据token从Redis获取用户信息
    // 删除Redis中保存的token，实现注销
    public void deleteToken(String token) {
        redisTemplate.delete("Wust_basic_token:" + token);
        deleteUid(token);
        deleteName(token);
    }
    public void deleteUid(String token) {
        redisTemplate.delete("LingHangToken:" + token);
    }
    public void deleteName(String token) {
        redisTemplate.delete("Student_name:" + token);
    }

}

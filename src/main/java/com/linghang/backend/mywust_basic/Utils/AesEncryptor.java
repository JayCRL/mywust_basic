package com.linghang.backend.mywust_basic.Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/**
 * AES加密解密工具类（GCM模式，256位密钥），支持从配置文件注入密钥
 */
@Component // 让Spring管理该类的实例
public class AesEncryptor {

    // GCM模式参数：IV长度12字节，认证标签长度16字节
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private final SecretKey secretKey;

    // 从配置文件注入Base64格式的AES密钥（通过@Value注解）
    public AesEncryptor(@Value("${aes.secret}") String base64Key) {
        // 验证密钥有效性（Base64解码后必须是32字节，符合AES-256要求）
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES密钥必须是32字节（256位），Base64解码后长度为：" + keyBytes.length);
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 加密方法
     * @param plaintext 明文（如用户密码）
     * @return 加密后的字符串（格式：IV + 密文 + 认证标签，Base64编码）
     */
    public String encrypt(String plaintext) throws Exception {
        // 生成随机IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        // 加密
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 拼接IV、密文、认证标签（GCM模式中，认证标签会附加在密文末尾）
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 解密方法
     * @param ciphertext 加密后的字符串（Base64编码）
     * @return 解密后的明文（如用户密码）
     */
    public String decrypt(String ciphertext) throws Exception {
        // 解码
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        // 拆分IV和密文（含认证标签）
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertextBytes = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertextBytes, 0, ciphertextBytes.length);

        // 初始化解密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        // 解密
        byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }
}

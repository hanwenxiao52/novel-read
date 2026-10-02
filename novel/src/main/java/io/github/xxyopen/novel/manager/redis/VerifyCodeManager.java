package io.github.xxyopen.novel.manager.redis;

import io.github.xxyopen.novel.core.constant.CacheConsts;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 验证码 管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyCodeManager {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 生成邮箱验证码，并放入 Redis 中，key 为邮箱地址
     */
    public String genEmailVerifyCode(String email) {
        String verifyCode = genRandomVerifyCode(6);
        stringRedisTemplate.opsForValue().set(CacheConsts.EMAIL_VERIFY_CODE_CACHE_KEY + email,
            verifyCode, Duration.ofMinutes(5));
        return verifyCode;
    }

    /**
     * 校验邮箱验证码
     */
    public boolean emailVerifyCodeOk(String email, String verifyCode) {
        return Objects.equals(stringRedisTemplate.opsForValue()
            .get(CacheConsts.EMAIL_VERIFY_CODE_CACHE_KEY + email), verifyCode);
    }

    /**
     * 从 Redis 中删除邮箱验证码
     */
    public void removeEmailVerifyCode(String email) {
        stringRedisTemplate.delete(CacheConsts.EMAIL_VERIFY_CODE_CACHE_KEY + email);
    }

    /**
     * 生成随机数字验证码
     */
    private String genRandomVerifyCode(int num) {
        StringBuilder verifyCode = new StringBuilder();
        for (int i = 0; i < num; i++) {
            verifyCode.append(ThreadLocalRandom.current().nextInt(10));
        }
        return verifyCode.toString();
    }

}

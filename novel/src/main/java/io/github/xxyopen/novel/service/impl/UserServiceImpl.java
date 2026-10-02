package io.github.xxyopen.novel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.xxyopen.novel.core.common.constant.CommonConsts;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.DatabaseConsts;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.core.util.JwtUtils;
import io.github.xxyopen.novel.dao.entity.UserBookshelf;
import io.github.xxyopen.novel.dao.entity.UserFeedback;
import io.github.xxyopen.novel.dao.entity.UserInfo;
import io.github.xxyopen.novel.dao.mapper.UserBookshelfMapper;
import io.github.xxyopen.novel.dao.mapper.UserFeedbackMapper;
import io.github.xxyopen.novel.dao.mapper.UserInfoMapper;
import io.github.xxyopen.novel.dao.dto.UserInfoUptDTO;
import io.github.xxyopen.novel.dao.dto.UserLoginDTO;
import io.github.xxyopen.novel.dao.dto.UserRegisterDTO;
import io.github.xxyopen.novel.dao.dto.UserSendEmailCodeDTO;
import io.github.xxyopen.novel.dao.vo.UserInfoVO;
import io.github.xxyopen.novel.dao.vo.UserLoginVO;
import io.github.xxyopen.novel.dao.vo.UserRegisterVO;
import io.github.xxyopen.novel.manager.message.EmailCodeMailSender;
import io.github.xxyopen.novel.manager.redis.VerifyCodeManager;
import io.github.xxyopen.novel.service.UserService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 会员模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    private final VerifyCodeManager verifyCodeManager;

    private final EmailCodeMailSender emailCodeMailSender;

    private final UserFeedbackMapper userFeedbackMapper;

    private final UserBookshelfMapper userBookshelfMapper;

    private final JwtUtils jwtUtils;

    @Override
    public RestResp<Void> sendEmailCode(UserSendEmailCodeDTO dto) {
        // 校验邮箱是否已注册
        LambdaQueryWrapper<UserInfo> mapper = new LambdaQueryWrapper<>();
        mapper.eq(UserInfo::getEmail, dto.getEmail());
        if (userInfoMapper.exists(mapper)) {
            // 邮箱已注册
            throw new BusinessException(ErrorCodeEnum.USER_EMAIL_EXIST);
        }

        // 生成邮箱验证码并存入 Redis
        String code = verifyCodeManager.genEmailVerifyCode(dto.getEmail());

        // 发送验证码到邮箱
        if (!emailCodeMailSender.sendEmailCode(dto.getEmail(), code)) {
            // 邮件发送失败
            throw new BusinessException(ErrorCodeEnum.USER_EMAIL_SEND_ERROR);
        }

        return RestResp.ok();
    }

    @Override
    public RestResp<UserRegisterVO> register(UserRegisterDTO dto) {
        // 校验邮箱验证码是否正确
        if (!verifyCodeManager.emailVerifyCodeOk(dto.getEmail(), dto.getVelCode())) {
            // 邮箱验证码校验失败
            throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
        }

        // 校验邮箱是否已注册
        LambdaQueryWrapper<UserInfo> emailMapper = new LambdaQueryWrapper<>();
        emailMapper.eq(UserInfo::getEmail, dto.getEmail());
        if (userInfoMapper.exists(emailMapper)) {
            // 邮箱已注册
            throw new BusinessException(ErrorCodeEnum.USER_EMAIL_EXIST);
        }

        // 校验用户名是否已存在
        LambdaQueryWrapper<UserInfo> usernameMapper = new LambdaQueryWrapper<>();
        usernameMapper.eq(UserInfo::getUsername, dto.getUsername());
        if (userInfoMapper.exists(usernameMapper)) {
            // 用户名已存在
            throw new BusinessException(ErrorCodeEnum.USER_NAME_EXIST);
        }

        // 注册成功，保存用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(
            DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        userInfo.setUsername(dto.getUsername());
        userInfo.setEmail(dto.getEmail());
        userInfo.setNickName(dto.getUsername());
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfo.setSalt("0");
        userInfoMapper.insert(userInfo);

        // 删除邮箱验证码
        verifyCodeManager.removeEmailVerifyCode(dto.getEmail());

        // 生成JWT 并返回
        return RestResp.ok(
            UserRegisterVO.builder()
                .token(jwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
                .uid(userInfo.getId())
                .build()
        );

    }

    @Override
    public RestResp<UserLoginVO> login(UserLoginDTO dto) {
        // 查询用户信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }

        // 判断密码是否正确
        if (!Objects.equals(userInfo.getPassword()
            , DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            // 密码错误
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        // 登录成功，生成JWT并返回
        return RestResp.ok(UserLoginVO.builder()
            .token(jwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
            .uid(userInfo.getId())
            .nickName(userInfo.getNickName()).build());
    }

    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptDTO dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setUserSex(dto.getUserSex());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> saveFeedback(Long userId, String content) {
        UserFeedback userFeedback = new UserFeedback();
        userFeedback.setUserId(userId);
        userFeedback.setContent(content);
        userFeedback.setCreateTime(LocalDateTime.now());
        userFeedback.setUpdateTime(LocalDateTime.now());
        userFeedbackMapper.insert(userFeedback);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteFeedback(Long userId, Long id) {
        QueryWrapper<UserFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
            .eq(DatabaseConsts.UserFeedBackTable.COLUMN_USER_ID, userId);
        userFeedbackMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getBookshelfStatus(Long userId, String bookId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        return RestResp.ok(
            userBookshelfMapper.selectCount(queryWrapper) > 0
                ? CommonConsts.YES
                : CommonConsts.NO
        );
    }

    @Override
    public RestResp<UserInfoVO> getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return RestResp.ok(UserInfoVO.builder()
            .nickName(userInfo.getNickName())
            .userSex(userInfo.getUserSex())
            .userPhoto(userInfo.getUserPhoto())
            .build());
    }
}

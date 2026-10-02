package io.github.xxyopen.novel.manager.message;

import io.github.xxyopen.novel.core.config.MailProperties;
import io.github.xxyopen.novel.core.constant.MessageSenderTypeConsts;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * 邮箱验证码发送器
 * <p>
 * 用户注册时向指定邮箱发送验证码，此时用户还未入库，所以直接指定收件人邮箱地址
 *
 * @author xiongxiaoyang
 * @date 2024/1/1
 */
@Component(value = MessageSenderTypeConsts.EMAIL_CODE_MAIL_SENDER)
@EnableConfigurationProperties(MailProperties.class)
public class EmailCodeMailSender extends AbstractMailSender {

    public EmailCodeMailSender(MailProperties mailProperties, JavaMailSender mailSender) {
        super(mailProperties, mailSender);
    }

    /**
     * 发送邮箱验证码
     *
     * @param toEmail 收件人邮箱地址
     * @param code    验证码
     * @return 发送成功返回 true，失败返回 false
     */
    public boolean sendEmailCode(String toEmail, String code) {
        String title = getTitleTemplate();
        String content = resolveContent(getContentTemplate(), code);
        return sendMail(toEmail, title, content);
    }

    @Override
    protected String getTitleTemplate() {
        return "小说精品屋-邮箱验证码";
    }

    @Override
    protected String getContentTemplate() {
        return """
                <div>
                    你正在注册小说精品屋账号，本次的邮箱验证码为：
                </div>
                <div style="padding: 10px 0 50px 0; text-align: center;">
                    <span style="font-size: 24px; font-weight: bold; color: #0274be;">{}</span>
                </div>
                <div>
                    验证码 5 分钟内有效，请勿泄露给他人。
                </div>
            """;
    }

}

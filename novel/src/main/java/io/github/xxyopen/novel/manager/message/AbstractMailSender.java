package io.github.xxyopen.novel.manager.message;

import io.github.xxyopen.novel.core.config.MailProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * 抽象的邮件消息发送者
 *
 * @author xiongxiaoyang
 * @date 2023/3/24
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMailSender extends AbstractMessageSender {

    private final MailProperties mailProperties;

    private final JavaMailSender mailSender;

    @Override
    protected void sendMessage(Long toUserId, String messageTitle, String messageContent) {
        // TODO 根据消息接收方的用户ID查询出消息接收方的邮件地址
        String toEmail = "xxyopen@foxmail.com";
        // 开始发送邮件
        sendMail(toEmail, messageTitle, messageContent);
    }

    /**
     * 发送邮件到指定邮箱（注册等场景下用户还未入库，直接指定收件人邮箱地址）
     *
     * @param toEmail        收件人邮箱地址
     * @param messageTitle   消息标题
     * @param messageContent 消息内容
     * @return 发送成功返回 true，失败返回 false
     */
    protected boolean sendMail(String toEmail, String messageTitle, String messageContent) {
        log.info("发送 HTML 邮件开始：{},{},{}", toEmail, messageTitle, messageContent);
        // 使用 MimeMessage，MIME 协议
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        // MimeMessageHelper 帮助我们设置更丰富的内容
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(new InternetAddress(mailProperties.username(), mailProperties.nickname(), "UTF-8"));
            helper.setTo(toEmail);
            helper.setSubject(messageTitle);
            // 第二个参数 true 代表支持 html
            helper.setText(messageContent, true);
            mailSender.send(message);
            log.info("发送 HTML 邮件 to {} 成功", toEmail);
            return true;
        } catch (Exception e) {
            // 邮件发送失败不会重试
            log.error("发送 HTML 邮件 to {} 失败", toEmail, e);
            return false;
        }
    }

}

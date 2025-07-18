package com.duoduo.phoneshop.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 邮件工具类
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Component
public class EmailUtil {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     * @return 发送结果
     */
    public boolean sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("邮件发送成功，收件人: {}", to);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败，收件人: " + to, e);
            return false;
        }
    }

    /**
     * 发送HTML邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content HTML内容
     * @return 发送结果
     */
    public boolean sendHtmlEmail(String to, String subject, String content) {
        try {
            org.springframework.mail.javamail.MimeMessageHelper helper =
                    new org.springframework.mail.javamail.MimeMessageHelper(
                            mailSender.createMimeMessage(), true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(helper.getMimeMessage());
            log.info("HTML邮件发送成功，收件人: {}", to);
            return true;
        } catch (Exception e) {
            log.error("HTML邮件发送失败，收件人: " + to, e);
            return false;
        }
    }
}
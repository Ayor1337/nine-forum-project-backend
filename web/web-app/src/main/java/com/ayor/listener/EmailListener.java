package com.ayor.listener;

import com.ayor.entity.message.EmailVerifyMessage;
import com.ayor.mail.EmailHtmlTemplates;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.internet.MimeMessage;

@RequiredArgsConstructor
@RabbitListener(queues = "mail.queue")
@Component
@Slf4j
public class EmailListener {

    private final JavaMailSender mailSender;

    private final EmailHtmlTemplates emailHtmlTemplates;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${nine-forum.public-base-url}")
    private String publicBaseUrl;


    /**
     * 消费邮件验证消息并发送验证邮件。
     *
     * @param emailVerifyMessage 邮件验证消息
     * @param message RabbitMQ 消息体
     * @param channel RabbitMQ 通道
     */
    @RabbitHandler
    public void onMessage(EmailVerifyMessage emailVerifyMessage,
                          Message message,
                          Channel channel) {
        try {
            if (message ==  null)
                return;

            String email = emailVerifyMessage.getEmail();
            String token = emailVerifyMessage.getToken();

            MimeMessage sendMessage = switch (emailVerifyMessage.getType()) {
                case REGISTER ->
                    createRegisterVerifyMessage(email, token);
            };
            mailSender.send(sendMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.warn("发送邮件失败：{}", e.getMessage(), e);
            try {
                if (message != null) {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                }
            } catch (Exception nackException) {
                log.warn("拒绝邮件消息失败：{}", nackException.getMessage(), nackException);
            }
        }


    }

    /**
     * 构造注册验证邮件。
     *
     * @param email 收件人邮箱
     * @param token 验证 Token
     * @return 邮件对象
     */
    private MimeMessage createRegisterVerifyMessage(String email, String token) throws Exception {
        String verifyUrl = UriComponentsBuilder
                .fromUriString(publicBaseUrl)
                .path("/api/auth/register-verifications")
                .queryParam("email", email)
                .queryParam("token", token)
                .build()
                .toUriString();

        return createHtmlMessage("验证你的邮箱 · Nine Forum", emailHtmlTemplates.registerVerify(verifyUrl), email);
    }

    /**
     * 构造 HTML 邮件内容。
     *
     * @param title 邮件标题
     * @param html 邮件 HTML 正文
     * @param email 收件人邮箱
     * @return 邮件对象
     */
    private MimeMessage createHtmlMessage(String title, String html, String email) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setSubject(title);
        helper.setText(html, true);
        helper.setTo(email);
        helper.setFrom(username, "Nine Forum");

        return message;
    }
}

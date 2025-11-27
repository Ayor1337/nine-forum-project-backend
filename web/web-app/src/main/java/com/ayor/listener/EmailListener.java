package com.ayor.listener;

import com.ayor.entity.message.EmailVerifyMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@RabbitListener(queues = "mail.queue")
@Component
@Slf4j
public class EmailListener {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;


    @RabbitHandler
    public void onMessage(EmailVerifyMessage emailVerifyMessage,
                          Message message,
                          Channel channel) {
        try {
            String email = emailVerifyMessage.getEmail();
            String token = emailVerifyMessage.getToken();

            SimpleMailMessage sendMessage = switch (emailVerifyMessage.getType()) {
                case REGISTER ->
                    createMessage("还原来到 Nine 论坛", "请点击以下链接进行验证：" + "http://localhost:9966/api/auth/verify?email=" + email + "&token=" + token, email);
            };
            if (message ==  null)
                return;
            mailSender.send(sendMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            try {
                channel.queuePurge("mail");
            } catch (Exception purgeException) {
                log.warn("清空邮件队列失败：{}", purgeException.getMessage());
            }
        }


    }

    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);

        return message;
    }
}

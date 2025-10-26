package com.ayor.ws;

import com.alibaba.fastjson2.JSON;
import com.ayor.entity.wsMessage.ChatBoardResultMessage;
import com.ayor.entity.wsMessage.ResultMessage;
import com.ayor.entity.wsPojo.ChatBoardUser;
import com.ayor.service.TopicChatService;
import com.ayor.util.MessageUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/chatboard")
@Component
public class ChatBoardEndpoint {

    public static Map<ChatBoardUser, ChatBoardEndpoint> onLineUsers = new ConcurrentHashMap<>();

    private Session session;

    private String fromUser;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        this.session = session;
        int topicId = 0;
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        if (requestParameterMap.containsKey("topic_id")) {
            topicId = Integer.parseInt(requestParameterMap.get("topic_id").get(0));
        }
        UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) session.getUserPrincipal();
        String username = principal.getName();
        this.fromUser = username;
        ChatBoardUser chatBoardUser = new ChatBoardUser(username, topicId);
        onLineUsers.put(chatBoardUser, this);
    }

    private void broadcastAllUsers(String message, int topicId) {
        onLineUsers.keySet().forEach(username -> {
            if (username.getTopicId() != topicId) {
                return;
            }
            ChatBoardEndpoint endpoint = onLineUsers.get(username);
            try {
                endpoint.session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
        });
    }

    private Set<ChatBoardUser> getNames(){
        return onLineUsers.keySet();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        ChatBoardResultMessage<String> resultMessage = JSON.parseObject(message, ChatBoardResultMessage.class);
        String data = resultMessage.getMessage();
        Integer topicId = resultMessage.getTopicId();
        String sendMessage = MessageUtils.getMessage(false, fromUser, data);
        broadcastAllUsers(sendMessage, topicId);
    }

    @OnClose
    public void onClose(Session session) {

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket发生错误", throwable);
    }

}

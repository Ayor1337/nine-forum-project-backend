package com.ayor.ws;

import com.alibaba.fastjson2.JSON;
import com.ayor.entity.wsMessage.ResultMessage;
import com.ayor.util.MessageUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/chat")
@Component
public class ChatEndpoint {

    // 用来存储每一个客户端对象对应的 ChatEndpoint 对象
    public static Map<String, ChatEndpoint> onLineUsers = new ConcurrentHashMap<>();

    private Session session;

    private String fromUser;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        this.session = session;
        UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) session.getUserPrincipal();
        String username = principal.getName();
        onLineUsers.put(username, this);
        this.fromUser = username;
        String message = MessageUtils.getMessage(true, username, getNames());
        broadcastAllUsers(message);
    }

    private void broadcastAllUsers(String message) {
        onLineUsers.keySet().forEach(username -> {
            ChatEndpoint endpoint = onLineUsers.get(username);
            try {
                endpoint.session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
        });
    }

    private Set<String> getNames(){
        return onLineUsers.keySet();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        ResultMessage<String> resultMessage = JSON.parseObject(message, ResultMessage.class);
        String data = resultMessage.getMessage();
        String toUser = resultMessage.getToUser();
        String sendMessage = MessageUtils.getMessage(false, fromUser, data);
        onLineUsers.get(toUser).session.getBasicRemote().sendText(sendMessage);
    }

    @OnClose
    public void onClose(Session session) {

    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket发生错误", throwable);
    }
}
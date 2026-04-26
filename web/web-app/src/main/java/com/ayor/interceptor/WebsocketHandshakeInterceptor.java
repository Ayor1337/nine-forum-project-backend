package com.ayor.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 在 WebSocket 握手时记录请求路径和来源。
     *
     * @param request 服务端请求
     * @param response 服务端响应
     * @param wsHandler WebSocket 处理器
     * @param attributes 握手属性
     * @return 始终返回 true
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        attributes.put("endpointPath", request.getURI().getPath());
        attributes.put("origin", request.getHeaders().getOrigin());
        return true;
    }

    /**
     * WebSocket 握手完成后的回调。
     *
     * @param request 服务端请求
     * @param response 服务端响应
     * @param wsHandler WebSocket 处理器
     * @param exception 握手异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }
}

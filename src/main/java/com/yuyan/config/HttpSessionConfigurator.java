package com.yuyan.config;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author lucky
 * @date 2024/1/15
 */
@Component
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    //获取HttpSession对象
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null){
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        }
    }
}

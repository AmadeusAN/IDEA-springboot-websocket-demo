package com.anhun.util;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUtil {
    /**
     * 记录当前在线的Session
     */
    private static final Map<String, Session> ONLINE_SESSION = new ConcurrentHashMap<>();

    /**
     * 添加session
     *
     * @param userId
     * @param session
     */
    public static void addSession(String userId, Session session) {
        // 此处只允许一个用户的session链接。一个用户的多个连接，我们视为无效。
        ONLINE_SESSION.putIfAbsent(userId, session);
    }

    /**
     * 关闭session
     *
     * @param userId
     */
    public static void removeSession(String userId) {
        ONLINE_SESSION.remove(userId);
    }

    /**
     * 给单个用户推送消息
     *
     * @param session
     * @param message
     */
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }

        // 同步
        RemoteEndpoint.Async async = session.getAsyncRemote();
        async.sendText(message);
    }

    /**
     * 向所有在线人发送消息
     *
     * @param message
     */
    public static void sendMessageForAll(String message) {
        //jdk8 新方法
        ONLINE_SESSION.forEach((sessionId, session) -> sendMessage(session, message));
    }
}
package com.anhun.controller;

import com.alibaba.fastjson.JSON;
import com.anhun.entity.Event;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@ServerEndpoint("/chat/{userId}")
public class WebSocketController {

    public static Logger log = Logger.getLogger("WebSocketController.class");

    private static Map<Integer, Session> webSocketMap = new HashMap<>();

    private Session session;        // 表示当次连接的 session

    private User user;


    private static UserMapper userMapper;
    private static EventMapper eventMapper;

    @Autowired
    public void setUserMapper(UserMapper usermapper) {
        userMapper = usermapper;
    }

    @Autowired
    public void setEventMapper(EventMapper eventmapper) {
        eventMapper = eventmapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") int userId) {
        this.session = session;
        log.info("userID = " + userId + " 登录");
        if (webSocketMap.containsKey(userId)) {
        } else {
            webSocketMap.put(userId, this.session);

            String feelbackmsg = "{\"message\":\"您已上线\",\"state\":3}";
            session.getAsyncRemote().sendText(feelbackmsg);
            log.info("仿写的JSON 字符串 : " + feelbackmsg);
            log.info("已将 userId = " + userId + "的用户 session 添加进 map 中");
            user = userMapper.findUserById(userId);
            if (user == null) {
                log.info("user 为 null");
            } else {
                log.info("新用户加入");
//                broadcast(user.getName(), "已进入房间，当前在线人数为：" + webSocketMap.size());
            }
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") int userId) {
//        broadcast(user.getName(), "已退出房间");
        webSocketMap.remove(userId);
        log.info("userID = " + userId + "的用户登出，已将其 session 移除");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("wenSocketController 发生错误");
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Event event;
        try {
            event = (Event) objectMapper.readValue(message, Event.class);
            event.setSendTime(new Date());
            event.setFromName(user.getName());
            log.info(event.toString());

            if (event.getState() == 2) {
//                单聊
                log.info("检测到单聊信息");
                int toId = event.getToId();
//                对方在线上，直接发送
                if (webSocketMap.containsKey(toId)) {
                    log.info("检测到对方在线，直接发送");
                    Session tarsession = webSocketMap.get(toId);
                    tarsession.getAsyncRemote().sendText(message);
                    eventMapper.addEvent(event);
                } else {
//                    对方不在线，存放入数据库
                    eventMapper.addEvent(event);
                }
                session.getAsyncRemote().sendText(message);

            } else if (event.getState() == 4) {
//                表示为群发消息，用于消息广场
                log.info("检测到群发消息");
                StringBuffer buffer = new StringBuffer(message);
                buffer.deleteCharAt(message.length() - 1);
                buffer.append(",\"fromName\":\"" + user.getName() + "\"}");
                broadcast(buffer.toString());
                eventMapper.addEvent(event);
            } else if (event.getState() == 5) {
//                群组消息，用于群组聊天
                log.info("检测到群组消息");
                List<User> groupuser = userMapper.findUsersbyGroupId(event.getToId());
                List<Integer> groupUserId = groupuser.stream().map(User::getId).collect(Collectors.toList());
                eventMapper.addEvent(event);
                for (int i : groupUserId) {
                    if (webSocketMap.containsKey(i)) {
//                        对方在线上
                        Session tarsession = webSocketMap.get(i);
                        tarsession.getAsyncRemote().sendText(JSON.toJSONString(event));
                    }
                }
            }

        } catch (JsonParseException e) {
            log.info("JSON 解析失败");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 群发消息
     */
    public void broadcast(String JSONmessage) {
        for (Session session : webSocketMap.values()) {
            session.getAsyncRemote().sendText(JSONmessage);
        }
    }
}

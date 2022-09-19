package com.anhun.entity;

import java.util.Date;

/**
 * <P>state : 6 表示为群组申请信息，此时 fromId 为申请者 Id，<code>toId</code> 表示为群组管理员 ID </P>
 * <P>state : 5 表示为群组消息，此时 <code>fromId</code> 为消息发送者 ID ，<code>toId</code>为群组 ID</P>
 */
public class Event {
    private int eventId;
    private int fromId;
    private String fromName;
    private int toId;       // 普通单聊消息和申请消息时为对应用户ID，群发消息为0，群组消息为 gorupId
    private String toName;
    private int state;      // 1 为好友申请事件 2 为历史单聊消息事件 3 为系统消息 4 为群发消息 5 为群组消息 6 为申请进入群组消息
    private String message;
    private Date sendTime;

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", fromId=" + fromId +
                ", fromName='" + fromName + '\'' +
                ", toId=" + toId +
                ", toName='" + toName + '\'' +
                ", state=" + state +
                ", message='" + message + '\'' +
                '}';
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Event(int fromId, String fromName, int toId, int state, String message, Date sendTime) {
        this.fromId = fromId;
        this.fromName = fromName;
        this.toId = toId;
        this.state = state;
        this.message = message;
        this.sendTime = sendTime;
    }

//    public Event(int eventId, int fromId, String fromName, int toId, int state, String message, Date sendTime) {
//        this.eventId = eventId;
//        this.fromId = fromId;
//        this.fromName = fromName;
//        this.toId = toId;
//        this.state = state;
//        this.message = message;
//        this.sendTime = sendTime;
//    }

    public Event() {

    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
}

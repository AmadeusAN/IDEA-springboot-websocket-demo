package com.anhun.mapper;

import com.anhun.entity.Event;
import com.anhun.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface EventMapper {
    int addEvent(Event event);

    Event findEventById(int id);

    /**
     * 查找普通好友申请事件
     * state 仅为 1
     *
     * @param from
     * @param to
     * @return
     */
    Event findApplyEvent(int from, int to);

    /**
     * 查找是否存在有特定的群组申请
     *
     * @param from
     * @param to
     * @param name
     * @return
     */
    Event findApplyGroupEvent(int from, int to, String name);

    /**
     * 通用的查找事件方法
     *
     * @param from
     * @param to
     * @param state
     * @return
     */
    Event findEvent(int from, int to, int state);

    /**
     * <P>处理两个申请事件，<strong>好友申请</strong>与<strong>群组申请</strong></P>
     *
     * @param user
     * @return
     */
    List<Event> findPendingEvent(User user);

    /**
     * 聊天记录是双向的
     *
     * @param fromId
     * @param toId
     * @return
     */
    List<Event> findHistoryMessage(int fromId, int toId);

    /**
     * 搜寻消息广场历史消息
     *
     * @return
     */
    List<Event> findGroundHistoryMessage();

    /**
     * 搜寻特定群组消息
     *
     * @param groupId
     * @return
     */
    List<Event> findGroupHistroyMessage(int groupId);

    int alterEventSendTime(int from, int to, Date time);

    int insertFriendship(Event event);

    /**
     * 添加 <strong>用户与群组</strong>关系
     * @param event
     * @return
     */
    int insertUserGroup(Event event);

    int deleteEventById(int id);

}

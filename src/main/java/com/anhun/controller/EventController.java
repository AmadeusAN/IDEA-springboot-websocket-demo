package com.anhun.controller;

import com.anhun.entity.Event;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.GroupMapper;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

@Controller
public class EventController {

    private Logger log = Logger.getLogger("EventController.class");

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/applyforfriend/{targetId}")
    @ResponseBody
    public String applyfrined(@PathVariable Integer targetId, HttpServletRequest request) throws ParseException {
        User senduser = (User) request.getSession().getAttribute("loginuser");
        User touser = (User) userMapper.findUserById(targetId);
        int res = userMapper.findfriendshipifExist(senduser.getId(), touser.getId());
        if (res == 1) {
            return "你们已经是好友了";
        } else {
            Event ifexist = eventMapper.findApplyEvent(senduser.getId(), targetId);
            if (ifexist != null) {
                log.info("检测到该好友请求已存在");
                eventMapper.alterEventSendTime(senduser.getId(), targetId, new Date());
                return "已经发送过好友请求了";
            } else {
                if (targetId == senduser.getId()) {
//                试图添加自己为好友
                    return "不能添加自己为好友";
                } else {
                    Event event = new Event(senduser.getId(), senduser.getName(), targetId, 1, "新的好友申请", new Date());
                    int res2 = eventMapper.addEvent(event);
                    if (res2 == 1) {
                        return "成功发送好友请求";
                    } else {
                        return "请求发生错误";
                    }
                }
            }
        }
    }

    /**
     * 通用处理事件，包括好友申请和群组加入申请
     *
     * @param eventId
     * @return
     */
    @RequestMapping("/dealevent/{eventId}")
    @ResponseBody
    public String dealEvent(@PathVariable Integer eventId) {

        Event event = eventMapper.findEventById(eventId);

//        处理添加好友事件
        if (event.getState() == 1) {
            log.info("处理添加好友事件");
            int res = eventMapper.insertFriendship(event);
            if (res > 0) {
                eventMapper.deleteEventById(eventId);
                log.info("将成功执行的事件从数据库中删除，事件ID为 " + event.getEventId());
                return "成功添加好友";
            } else {
                return "error";
            }
        } else {
//            处理群组申请事件
            log.info("处理申请加入群组事件");
            int groupId = groupMapper.findGroupIdByGroupNamewithManagerId(event.getToName(), event.getToId());
            event.setToId(groupId);

//           注意此处将 event的 toId 修改为 groupId 以便能正确添加进 usergroup 中，原本的含义是指 managerId
            int res = eventMapper.insertUserGroup(event);
            if (res > 0) {
                eventMapper.deleteEventById(eventId);
                log.info("将成功执行的事件从数据库中删除，事件ID为 " + event.getEventId());
                return "同意其进入群组";
            } else {
                return "error";
            }
        }


    }
}

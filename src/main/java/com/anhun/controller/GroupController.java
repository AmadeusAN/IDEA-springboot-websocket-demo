package com.anhun.controller;

import com.anhun.entity.Event;
import com.anhun.entity.Group;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.GroupMapper;
import com.anhun.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class GroupController {

    private Logger log = Logger.getLogger("GroupController.class");

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    /**
     * <p>用于主页跳转搜索群组页面</p>
     *
     * @param model
     * @return
     */
    @RequestMapping("/tosearchgroup")
    public String toSearchgroup(Model model) {
        return "searchgroup";
    }

    /**
     * <p>搜索群组并返回相关结果</p>
     *
     * @param groupName
     * @param model
     * @return
     */
    @RequestMapping("/searchgroup/{groupName}")
    public String searchgroup(@PathVariable String groupName, @RequestParam(value = "start", defaultValue = "1") Integer start, Model model) {
        PageHelper.startPage(start, 3);
        List<Group> groups = groupMapper.findGroupsByGroupName(groupName);
        PageInfo<Group> groupPageInfo = new PageInfo<>(groups);
        model.addAttribute("groupName", groupName);
        model.addAttribute("groupPageInfo", groupPageInfo);
        return "grouplist";
    }

    @RequestMapping("/initGroupList")
    public String initGroupList(@RequestParam(value = "start", defaultValue = "1") Integer start, Model model) {
//        设置页码与每页的大小
        PageHelper.startPage(start, 3);

        List<Group> groups = groupMapper.searchPopularGrouplist();

        PageInfo<Group> groupPageInfo = new PageInfo<>(groups);

        model.addAttribute("groupPageInfo", groupPageInfo);

        return "populargrouplist";
    }

    @RequestMapping("/toInsertgroup")
    public String toInsertGroup(Model model) {
        Group group = new Group();
        group.setGroupName("默认群组名称");
        group.setGroupDescript("默认群组描述");
        log.info("此时已经将 group 添加进模板元素中，且 group = " + group.toString());
        model.addAttribute("group", group);
        return "insertgroup";
    }

    @RequestMapping("/togroupinfo/{groupId}")
    public String togroupinfo(@PathVariable Integer groupId, Model model, HttpSession session) {
        Group group = groupMapper.findGroupById(groupId);
        model.addAttribute("group", group);

        User user = (User) session.getAttribute("loginuser");
        model.addAttribute("user", user);

        User manager = userMapper.findUserById(group.getManagerId());
        model.addAttribute("manager", manager);

        return "groupinfo";
    }

    @RequestMapping("/togroupmemberlist/{groupId}")
    public String togroupmemberlist(@PathVariable Integer groupId, Model model) {
        List<User> groupuserlist = userMapper.findUsersbyGroupId(groupId);
        model.addAttribute("userlist", groupuserlist);

        return "groupmemberlist";
    }


    @RequestMapping("/tryinsertgroup")
    @ResponseBody
    public String insertGroup(@ModelAttribute("group") Group group, HttpSession session) throws FileNotFoundException {
        log.info("后台接收到的 group 数据为 " + group.toString());
//        log.info("后台接收到的单独的数据为 groupName = " + groupName + " groupDescript = " + groupDescript);
        User manager = (User) session.getAttribute("loginuser");
        group.setManagerId(manager.getId());

        int exist = groupMapper.findGroupIfExist(group.getGroupName(), manager.getId());
        if (exist == 1) {
            return "该群组已存在";
        } else {
            int res = groupMapper.insertGroup(group);
            int res1 = groupMapper.insertusergroup(manager.getId(), group.getGroupId());
//        return "直接返回的数据";
            if (res == 1 && res1 == 1) {
                return "成功创建群组";
            } else {
                return "创建群组失败";
            }
        }
    }

    @RequestMapping("/sendgroupapplication/{managerId}/{groupId}")
    @ResponseBody
    public String sendgroupapplication(@PathVariable Integer managerId, @PathVariable Integer groupId, HttpSession session) {
        User user = (User) session.getAttribute("loginuser");
        Group group = groupMapper.findGroupById(groupId);

//        检查当前用户是否已经在该群组中
        int ifinside = groupMapper.findIfUserisingroup(user.getId(), groupId);
        if (ifinside == 1) {
            return "您已经在该群组中";
        } else {
//        检查是否已经发送过群组申请
            Event event = eventMapper.findApplyGroupEvent(user.getId(), group.getManagerId(), group.getGroupName());
            if (event != null) {
                return "已经发送过请求了";
            } else {
                Event newevent = new Event();
                newevent.setFromId(user.getId());
                newevent.setFromName(user.getName());
                newevent.setToId(managerId);
                newevent.setToName(group.getGroupName());
                newevent.setState(6);
                newevent.setSendTime(new Date());
                newevent.setMessage("请求加入群组 ID = " + group.getGroupId() + " 群组名称为 :" + group.getGroupName());
                int res = eventMapper.addEvent(newevent);
                if (res == 1) {
                    log.info("添加群组申请");
                    return "成功发送请求，请等待管理员审核";
                } else {
                    log.info("群组申请提交失败");
                    return "申请失败，服务器异常";
                }

            }
        }
    }

    @PostMapping("/groupinfo/submithheadimg/{groupId}")
    @ResponseBody
    public Map<String, Object> updateheadimg(MultipartFile file, @PathVariable Integer groupId, HttpSession session) {
        HashMap<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("loginuser");
        if (file != null) {
            log.info("file.getOriginalFilename = " + file.getOriginalFilename());
            log.info("file.getName() = " + file.getName());
            log.info("file.getContentType() = " + file.getContentType());
            log.info("file.getSize() = " + file.getSize() + "");

            try {
//                获取类路径下的静态资源路径，具体在项目路径的 target/classes/ 路径下
                String path = ResourceUtils.getURL("classpath:").getPath() + "static/img/groupimg";
                File savefile = new File(path);

//                修改文件名是应该前端上传的文件名都为 BLOB
                String newfilename = groupId + "_group_profile_picture" + ".jpg";
                File targetfile = new File(path, newfilename);

//                删除旧的头像文件
                if (Files.exists(targetfile.toPath())) {
                    Files.delete(targetfile.toPath());
                }

//                没有目录则创建
                if (!savefile.exists() || !savefile.isDirectory()) {
                    savefile.mkdirs();
                }
//                文件保存至数据库中
                int res = groupMapper.updateGroupProfilePicture(groupId, file.getBytes());
                if (res == 1) {
                    result.put("msg", "OK");
                    result.put("imgpath", "/img/groupimg/" + newfilename);
                } else {
                    result.put("msg", "文件保存失败");
                }

//                文件移动到指定目录
                file.transferTo(targetfile);
                log.info("转存文件目录的目标路径为 " + path);

                return result;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result.put("msg", "error");
                return result;
            } catch (IOException IOe) {
                IOe.printStackTrace();
                result.put("msg", "文件保存失败");
                return result;
            }
        } else {
            result.put("msg", "上传文件为空");
            return result;
        }
    }

}

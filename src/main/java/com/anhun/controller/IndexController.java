package com.anhun.controller;

import com.anhun.entity.Event;
import com.anhun.entity.Group;
import com.anhun.entity.ProfilePicture;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.GroupMapper;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    private Logger log = Logger.getLogger("IndexController.class");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private GroupMapper groupMapper;

    @RequestMapping("/")
    public String start(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping("/findFriends/{id}")
    public String findFriends(@PathVariable Integer id, Model model) {
        if (id != null) {
            List<User> friends = userMapper.findFirendListById(id);
            model.addAttribute("friendlist", friends);
            return "friendlist";
        } else {
            return "error";
        }
    }

    /**
     * 根据 session 中记录的 userid ，将登录用户的头像生成在指定目录
     *
     * @param session
     */
    @RequestMapping("/finduserProfilepicture")
    @ResponseBody
    public void finduserProfilepicture(HttpSession session) {
        log.info("初次加载项目时该处可能会报错");
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            User user = (User) session.getAttribute("loginuser");

            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }

            String filename = user.getId() + "_profile_picture.jpg";
            File imgfile = new File(path, filename);

//            头像保存在服务器目录中
            if (Files.exists(imgfile.toPath())) {
                log.info("头像在目录缓存中");
            } else {
                byte[] file = userMapper.findUserProfilePictureById(user.getId()).getImg();
                fos = new FileOutputStream(imgfile);
                bos = new BufferedOutputStream(fos);
                bos.write(file);
                log.info("头像未在缓存目录中，已重新下载");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据 id 读取数据库中的头像图片，用于 <strong>生成好友列表</strong> 和 <strong>初始化对话内容</strong>
     *
     * @param id
     */
    @RequestMapping("/finduserProfilePicture/{id}")
    @ResponseBody
    public void finduserProfilepictureByid(@PathVariable Integer id) {
        //            加载目标用户头像
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            String filename = id + "_profile_picture.jpg";
            File imgfile = new File(path, filename);
//            头像保存在服务器目录中
            if (Files.exists(imgfile.toPath())) {
                log.info("id为 " + id + " 的头像在目录缓存中");
            } else {
                ProfilePicture picture = userMapper.findUserProfilePictureById(id);
                if (picture != null) {
                    byte[] file = picture.getImg();
                    fos = new FileOutputStream(imgfile);
                    bos = new BufferedOutputStream(fos);
                    bos.write(file);
                    log.info("id为 " + id + " 的头像未在缓存目录中，已重新下载");
                } else {
                    log.info("id为 " + id + " 的用户未设置头像，已采用默认头像");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 根据登录用户 id 查询其加入或管理的群组，将群组头像加载到服务器的指定位置
     *
     * @return
     */
    @RequestMapping("/loadgroupimg")
    @ResponseBody
    public String loadgroupimg(HttpSession session) {
        User user = (User) session.getAttribute("loginuser");
        int id = user.getId();
        List<Group> groups = groupMapper.findGroupByUserId(id);

        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img/groupimg";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            for (Group group : groups) {
                String filename = group.getGroupId() + "_group_profile_picture.jpg";
                File imgfile = new File(path, filename);
//            图片保存在服务器目录中
                if (Files.exists(imgfile.toPath())) {
                    log.info("group_id为 " + id + " 的头像在目录缓存中");
                } else {
                    byte[] file = group.getGroupImg();
                    fos = new FileOutputStream(imgfile);
                    bos = new BufferedOutputStream(fos);
                    bos.write(file);
                    log.info("group_id为 " + id + " 的头像未在缓存目录中，已重新下载");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "群组头像加载完成";
        }
    }


    @RequestMapping("/checkUser/{id}")
    public String checkuser(@PathVariable Integer id, Model model) {

        User user = userMapper.findUserById(id);
        if (user != null) {
            model.addAttribute("user", user);
            return "userinfo";
        } else {
            return "error";
        }
    }

    @RequestMapping("/checkUserById")
    public String checkuserById(@RequestParam("id") Integer id, Model model, HttpSession session) {
        User user = userMapper.findUserById(id);
        if (user != null) {
            model.addAttribute("user", user);
            return "userinfo";
        } else {
            model.addAttribute("msg", "查无此人");
            model.addAttribute("friendlist", userMapper.findFirendListById(((User) session.getAttribute("loginuser")).getId()));
            return "friendlist";
        }
    }

    @ResponseBody
    @RequestMapping("/getuseridfromsession")
    public int getuserId(HttpSession session) {
        System.out.println("尝试获取用户id ");
        User user = (User) session.getAttribute("loginuser");
        System.out.println("user id = " + user.getId());
        return user.getId();
    }

    /**
     * 用于跳转到用户主界面
     *
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("/toIndex")
    public String toindex(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginuser");
        model.addAttribute("eventlist", eventMapper.findPendingEvent(user));
        model.addAttribute("user", user);
        List<User> friendList = userMapper.findFirendListById(user.getId());
        model.addAttribute("friendList", friendList);

        int id = user.getId();
        List<Group> groups = groupMapper.findGroupByUserId(id);
        model.addAttribute("grouplist", groups);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            log.info("加载用户所属群组头像");
            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img/groupimg";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            for (Group group : groups) {
                String filename = group.getGroupId() + "_group_profile_picture.jpg";
                File imgfile = new File(path, filename);
//            图片保存在服务器目录中
                if (Files.exists(imgfile.toPath())) {
                    log.info("group_id为 " + group.getGroupId() + " 的头像在目录缓存中");
                } else {
                    if (group.getGroupImg() != null) {
                        byte[] file = group.getGroupImg();
                        fos = new FileOutputStream(imgfile);
                        bos = new BufferedOutputStream(fos);
                        bos.write(file);
                        log.info("group_id为 " + group.getGroupId() + " 的头像未在缓存目录中，已重新下载");
                    } else {
                        log.info("group_id为 " + group.getGroupId() + " 为自定义头像，使用默认头像");
                    }
                }
            }

            log.info("加载用户好友头像");
            String path2 = ResourceUtils.getURL("classpath:").getPath() + "static/img";
            savefile = new File(path2);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            for (User friend : friendList) {
                String filename = friend.getId() + "_profile_picture.jpg";
                File imgfile = new File(path2, filename);
//            图片保存在服务器目录中
                if (Files.exists(imgfile.toPath())) {
                    log.info("id为 " + friend.getId() + " 的用户头像在目录缓存中");
                } else {
                    if (friend.getPicture() != null) {
                        byte[] file = friend.getPicture().getImg();
                        fos = new FileOutputStream(imgfile);
                        bos = new BufferedOutputStream(fos);
                        bos.write(file);
                        log.info("id为 " + friend.getId() + " 的头像未在缓存目录中，已重新下载");
                    } else {
                        log.info("id为 " + friend.getId() + " 的用户未设置头像，已使用默认头像");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "index";
    }

    @RequestMapping("/toChatground")
    public String toGround(HttpSession session, Model model) {

//        传递页面必要的值
        User user = (User) session.getAttribute("loginuser");
        List<Event> historymessage = eventMapper.findGroundHistoryMessage();
        Collections.reverse(historymessage);
        Set<Integer> ids = historymessage.stream().map(x -> x.getFromId()).collect(Collectors.toSet());
        log.info(ids.toString());


        model.addAttribute("historymsg", historymessage);
        model.addAttribute("jsonhistroymsgid", ids);

        model.addAttribute("user", user);
        return "chatground";
    }

    @RequestMapping("/toprivatechat/{id}")
    public String toprivatechat(@PathVariable Integer id, HttpSession session, Model model) {
        User touser = userMapper.findUserById(id);
        User fromuser = (User) session.getAttribute("loginuser");
        if (touser != null) {
            List<Event> events = eventMapper.findHistoryMessage(fromuser.getId(), touser.getId());
            Collections.reverse(events);

            model.addAttribute("user", touser);
            model.addAttribute("myself", fromuser);
            model.addAttribute("historymessage", events);

//            加载目标用户头像
            BufferedOutputStream bos = null;
            FileOutputStream fos = null;
            try {
                String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
                File savefile = new File(path);
                if (!savefile.exists() || !savefile.isDirectory()) {
                    savefile.mkdirs();
                }
                String filename = id + "_profile_picture.jpg";
                File imgfile = new File(path, filename);
//            头像保存在服务器目录中
                if (Files.exists(imgfile.toPath())) {
                    log.info("对方头像在目录缓存中");
                } else {
                    ProfilePicture picture = userMapper.findUserProfilePictureById(id);
                    if (picture != null) {
                        byte[] file = picture.getImg();
                        fos = new FileOutputStream(imgfile);
                        bos = new BufferedOutputStream(fos);
                        bos.write(file);
                        log.info("对方头像未在缓存目录中，已重新下载");
                    } else {
                        log.info("对方未设置头像，已采用默认头像");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException IOe) {
                IOe.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return "privatechat";
        } else {
            model.addAttribute("msg", "发生错误");
            return "error";
        }
    }

    @RequestMapping("/togroupchat/{groupId}")
    public String togroupchat(@PathVariable Integer groupId, Model model, HttpSession session) {

//        当前登录用户
        User loginuser = (User) session.getAttribute("loginuser");

        Group group = groupMapper.findGroupById(groupId);
        model.addAttribute("group", group);

        List<Event> grouphistorymsg = eventMapper.findGroupHistroyMessage(groupId);
        Collections.reverse(grouphistorymsg);

        model.addAttribute("historymessage", grouphistorymsg);
        model.addAttribute("myself", loginuser);

        List<User> useringroup = userMapper.findUsersbyGroupId(groupId);

//        加载组内每个人的头像


        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            for (User user : useringroup) {
                String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
                File savefile = new File(path);
                if (!savefile.exists() || !savefile.isDirectory()) {
                    savefile.mkdirs();
                }
                String filename = user.getId() + "_profile_picture.jpg";
                File imgfile = new File(path, filename);
//            头像保存在服务器目录中
                if (Files.exists(imgfile.toPath())) {
                    log.info("对方头像在目录缓存中");
                } else {
                    if (user.getPicture().getImg() != null) {
                        byte[] file = userMapper.findUserProfilePictureById(user.getId()).getImg();
                        fos = new FileOutputStream(imgfile);
                        bos = new BufferedOutputStream(fos);
                        bos.write(file);
                        log.info("对方头像未在缓存目录中，已重新下载");
                    } else {
                        log.info("目标id为 " + user.getId() + " 的用户未设置头像，使用默认头像");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "groupchat";
    }

    @RequestMapping("/tosearchfriend")
    public String tosearchfriend() {
        return "searchfriend";
    }

    @RequestMapping("/touserinfo/{userid}")
    public String touserinfo(@PathVariable Integer userid, Model model) {
        User user = userMapper.findUserById(userid);
        model.addAttribute("user", user);
        return "userinfo";
    }

    @RequestMapping("/initfriendlist")
    public String initfriendlist(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loginuser");
        List<User> friendlist = userMapper.findFirendListById(user.getId());
        model.addAttribute("msg", "我的好友");
        model.addAttribute("friendlist", friendlist);
        return "friendlist";
    }

    @RequestMapping("/searchfriend")
    public String searchfriend(@RequestParam Integer id, @RequestParam String name, Model model) {
        if (name.equals("") || name.trim().equals("")) name = null;
        log.info("搜寻好友参数 id = " + id + " name = " + name);
//        将参数封装进 POJO 类
        if (id == 0 && name == null) {
            model.addAttribute("msg", "查无此人");
        } else {
//            去掉用户输入的包围空格
            if (name != null) name = name.trim();
            User user = new User();
            user.setId(id);
            user.setName(name);
            List<User> friendlist = userMapper.findFriendsByIdOrName(user);
            if (friendlist.isEmpty()) {
                model.addAttribute("msg", "查无此人");
            } else {
                model.addAttribute("msg", "查询结果");
            }
            model.addAttribute("friendlist", friendlist);
        }
        return "friendlist";
    }
}


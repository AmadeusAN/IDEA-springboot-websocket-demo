package com.anhun.controller;

import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.logging.Logger;

@Controller
public class LoginController {

    private static Logger log = Logger.getLogger("LoginController.class");

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    @RequestMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @RequestMapping("/registeruser")
    public String registeruser(@ModelAttribute("user") User user, HttpServletRequest request, Model model) {
        User exist = userMapper.findUserByAccount(user.getAccount());
        if (exist != null) {
            model.addAttribute("msg", "该用户已存在");
            return "register";
        } else {
            int result = userMapper.insertUser(user);
            if (result == 0) {
                model.addAttribute("msg", "服务器繁忙，注册是失败");
                return "register";
            } else {
                userMapper.updateUserLastLogin(user.getId(), new Date());
                request.getSession().setAttribute("loginuser", user);
                model.addAttribute("eventlist", eventMapper.findPendingEvent(user));
                return "index";
            }
        }

    }

    @RequestMapping("/tologin")
    public String tologin(Model model) {
        User user = new User();
        user.setName("用户姓名");
        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping("/trylogin")
    public String trylogin(@Valid @ModelAttribute("user") User user, BindingResult br, HttpServletRequest request, Model model) {
        if (br.hasErrors()) {
            FieldError error = br.getFieldError();
            model.addAttribute("msg", error.getDefaultMessage());
            log.info("登录的数据为 :" + user.toString());
            model.addAttribute("user", user);
            return "login";
        }


        User target = userMapper.findUserByAccount(user.getAccount());
        if (target != null && target.getPassword().equals(user.getPassword())) {
            model.addAttribute("user", target);
            request.getSession().setAttribute("loginuser", target);
            return "redirect:/toIndex";
        } else {
            model.addAttribute("msg", "账号或密码错误");
            return "login";
        }
    }


    @RequestMapping("/loginout")
    public String loginout(Model model, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("loginuser");
        Date loginouttime = new Date();
        int res = userMapper.updateUserLastLogin(user.getId(), loginouttime);
        if (res == 0) {
            return "error";
        } else {
            request.getSession().removeAttribute("loginuser");
            model.addAttribute("user", new User());
            return "login";
        }
    }
}

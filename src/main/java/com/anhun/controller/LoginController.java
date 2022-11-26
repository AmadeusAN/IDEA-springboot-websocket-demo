package com.anhun.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Controller
public class LoginController {

    private static Logger log = Logger.getLogger("LoginController.class");

    private String vertifycode;
    private LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 70);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("/")
    public String start(Model model, HttpServletRequest request, HttpServletResponse response, @CookieValue(name = "userUUID", required = false) String userUUID) throws FileNotFoundException {
        log.info("URI = " + request.getRequestURI());
        if (userUUID == null) {
            log.info("此时未添加 UUID ");
            Cookie cookie = new Cookie("userUUID", UUID.randomUUID().toString());
//            cookie 3 分钟后失效
            cookie.setMaxAge(180);

            response.addCookie(cookie);
            log.info("为用户添加 UUID =" + cookie.getValue());
        } else {
            log.info("UUID 已存在:" + userUUID);
        }
//        将 UUID 发送至模板，用于找到对应的验证码图片
        model.addAttribute("userUUID", userUUID);

//        生成验证码图片
        generateVertifyImg(userUUID);

        model.addAttribute("user", new User());
        return "login";
    }

    /**
     * <p>生成验证码图片，并返回生成文件名，该文件名由 {@code UUID} 组成</p>
     *
     * @param userUUID
     * @return
     */
    public String generateVertifyImg(String userUUID) {
        lineCaptcha.createCode();
        try {
            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            String filename = userUUID + ".png";
            File imgfile = new File(path, filename);
            lineCaptcha.write(imgfile);
            log.info("新更新的验证码路径为：" + imgfile.getPath());
            String vertifycode = lineCaptcha.getCode();
            log.info("新更新的验证码为：" + vertifycode);

//            键值对 3 分钟后失效
            redisTemplate.opsForValue().set(userUUID, vertifycode, 3, TimeUnit.MINUTES);
            log.info("redis 中键值对已更新");

            return imgfile.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "验证码更新错误";
        }
    }

    /**
     * <p>前端通过 {@code Ajax} 请求后端重新生成验证码</p>
     *
     * @return
     */
    @RequestMapping("/login/reloadvertifyimg")
    @ResponseBody
    public String reloadVertifyImg(@CookieValue(name = "userUUID") String userUUID) {
        generateVertifyImg(userUUID);
        return "验证码更新操作完成";
    }

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

//    @RequestMapping("/tologin")
//    public String tologin(Model model) {
//        User user = new User();
//        user.setName("用户姓名");
//        model.addAttribute("user", new User());
//        return "login";
//    }

    @RequestMapping("/trylogin")
    public String trylogin(@Valid @ModelAttribute("user") User user, BindingResult br, @RequestParam("vertifycode") String vertifycode, HttpServletRequest request, @CookieValue(name = "userUUID", required = false) String userUUID, Model model) {
        String correntCode = null;
        if (userUUID == null) {
            log.info("登录时发现 UUID 未存在");
            return "error";
        } else {
            log.info("拟登录用户的UUID为:" + userUUID);
            log.info("其保存在 redis 中的验证码为:" + redisTemplate.opsForValue().get(userUUID));
            correntCode = redisTemplate.opsForValue().get(userUUID);
        }
        if (br.hasErrors()) {
            FieldError error = br.getFieldError();
            model.addAttribute("msg", error.getDefaultMessage());
            log.info("登录的数据为 :" + user.toString());
            model.addAttribute("user", user);
            return "login";
        }


        User target = userMapper.findUserByAccount(user.getAccount());
        log.info("接收到用户传递过来的验证码为：" + vertifycode);
        if (target != null && target.getPassword().equals(user.getPassword())) {
            if (vertifycode.equals(correntCode)) {
                model.addAttribute("user", target);
                request.getSession().setAttribute("loginuser", target);
                return "redirect:/toIndex";
            } else {
                model.addAttribute("msg", "验证码错误或失效");
                return "login";
            }
        } else {
            model.addAttribute("msg", "账号或密码错误");
            return "login";
        }
    }


    @RequestMapping("/loginout")
    public String loginout(Model model, HttpServletRequest request, @CookieValue(name = "userUUID") String userUUID) {
        User user = (User) request.getSession().getAttribute("loginuser");
        Date loginouttime = new Date();
        int res = userMapper.updateUserLastLogin(user.getId(), loginouttime);
        if (res == 0) {
            return "error";
        } else {
            request.getSession().removeAttribute("loginuser");
            model.addAttribute("user", new User());
            model.addAttribute("userUUID", userUUID);
            return "login";
        }
    }
}

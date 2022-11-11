package com.anhun.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.anhun.entity.User;
import com.anhun.mapper.EventMapper;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.logging.Logger;

@Controller
public class LoginController {

    private static Logger log = Logger.getLogger("LoginController.class");

    private String vertifycode;
    private LineCaptcha lineCaptcha;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    @RequestMapping("/")
    public String start(Model model, HttpServletRequest request) throws FileNotFoundException {
        log.info("URI = " + request.getRequestURI());

//        生成验证码图片
        lineCaptcha = CaptchaUtil.createLineCaptcha(200, 70);

        String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
        File savefile = new File(path);
        if (!savefile.exists() || !savefile.isDirectory()) {
            savefile.mkdirs();
        }
        String filename = "vertifycode.png";
        File imgfile = new File(path, filename);
        lineCaptcha.write(imgfile);
        vertifycode = lineCaptcha.getCode();
        log.info("验证码图片的存放路径为：" + imgfile.getPath());
        log.info("验证码对应的代码为：" + vertifycode);


        model.addAttribute("user", new User());
        return "login";
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

    /**
     * <p>前端通过 {@code Ajax} 请求后端重新生成验证码</p>
     *
     * @return
     */
    @RequestMapping("/login/reloadvertifyimg")
    @ResponseBody
    public String reloadVertifyImg() {
        lineCaptcha.createCode();
        try {

            String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
            File savefile = new File(path);
            if (!savefile.exists() || !savefile.isDirectory()) {
                savefile.mkdirs();
            }
            String filename = "vertifycode.png";
            File imgfile = new File(path, filename);
            lineCaptcha.write(imgfile);
            log.info("新更新的验证码路径为：" + imgfile.getPath());
            vertifycode = lineCaptcha.getCode();
            log.info("新更新的验证码为：" + vertifycode);

            return imgfile.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return "验证码更新错误";
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
    public String trylogin(@Valid @ModelAttribute("user") User user, BindingResult br, @RequestParam("vertifycode") String vertifycode, HttpServletRequest request, Model model) {
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
            if (vertifycode.equals(this.vertifycode)) {
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

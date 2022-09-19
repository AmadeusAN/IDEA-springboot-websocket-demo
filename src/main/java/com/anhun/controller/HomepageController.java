package com.anhun.controller;

import com.anhun.entity.User;
import com.anhun.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
public class HomepageController {

    private static Logger log = Logger.getLogger("HomepagController.class");

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/homepage/submithheadimg")
    @ResponseBody
    public Map<String, Object> updateheadimg(MultipartFile file, HttpSession session, Model model, HttpServletResponse response) {
        HashMap<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("loginuser");
        if (file != null) {
            log.info("file.getOriginalFilename = " + file.getOriginalFilename());
            log.info("file.getName() = " + file.getName());
            log.info("file.getContentType() = " + file.getContentType());
            log.info("file.getSize() = " + file.getSize() + "");

            try {
//                获取类路径下的静态资源路径，具体在项目路径的 target/classes/ 路径下
                String path = ResourceUtils.getURL("classpath:").getPath() + "static/img";
                File savefile = new File(path);

//                修改文件名是应该前端上传的文件名都为 BLOB
                String newfilename = user.getId() + "_profile_picture" + ".jpg";
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
                int res = userMapper.updateUserProfilePicture(user.getId(), file.getBytes());
                if (res == 1) {
                    result.put("msg", "OK");
                    result.put("imgpath", "/img/" + newfilename);
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

    @RequestMapping("/homepage")
    public String tohomepage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginuser");
        model.addAttribute("user", user);
        return "homepage";
    }
}

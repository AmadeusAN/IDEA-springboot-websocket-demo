package com.anhun.interceptor;

import com.anhun.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.logging.Logger;

/**
 * <p>简单拦截器，当 session 中没有用户的登录信息时（即loginuser) 将请求转发至登录页面</p>
 */
public class LoginInterceptor implements HandlerInterceptor {
    private final Logger log = Logger.getLogger("LoginInterceptor.class");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("拦截器接收到的 uri = " + uri);

        if (uri.equals("/")) return true;
        if (uri.indexOf("/login") >= 0) return true;
        if (uri.indexOf("/trylogin") >= 0) return true;
        if (uri.indexOf("/register") >= 0) return true;
        if (uri.indexOf("/registeruser") >= 0) return true;
        if (uri.endsWith(".png") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".map")) return true;
        if (uri.indexOf("/error") >= 0) return true;

//        if(uri.indexOf("/toIndex")>=0) return true;

        HttpSession session = request.getSession();
        User loginuser = (User) session.getAttribute("loginuser");
        if (loginuser != null) {
            return true;
        }

        request.getRequestDispatcher("/").forward(request, response);
        log.info("未放行此时请求，已转发至 login 页面");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

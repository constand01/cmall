package com.cmall.interceptors;

import annotations.LoginRequired;
import com.alibaba.fastjson.JSON;
import util.CookieUtil;
import com.cmall.util.HttpclientUtil;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            //拦截代码
            //判断被拦截的请求的访问的方法的注解（是否需要拦截）
            HandlerMethod hm = null;
            //获取执行方法上的注解
             if(handler instanceof  HandlerMethod){
                 hm = (HandlerMethod) handler;
            }else{
                return true;
             }
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);



            //是否拦截
            if(methodAnnotation==null){
                return true;
            }

            String token="";
            String oldToken= CookieUtil.getCookieValue(request,"oldToken",true);

            if (StringUtils.isNotBlank(oldToken)){
                token=oldToken;

            }

            String newToken=request.getParameter("token");
            if (StringUtils.isNotBlank(newToken)){
                token=newToken;
            }

            boolean loginSeccess=methodAnnotation.loginSeccess();

            //调用验证中心进行验证
            String success="false";
            Map<String,String> successMap=new HashMap<>();
            if (StringUtils.isNotBlank(token)) {
                String ip=request.getHeader("r-forwarded-for"); //通过nginx转发的客户端ip
                if(org.apache.commons.lang3.StringUtils.isBlank(ip)) {
                    ip= request.getRemoteAddr();//从reuqest获取ip地址
                    if(org.apache.commons.lang3.StringUtils.isBlank(ip)){
                        ip="127.0.0.1";
                    }
                }
              String  successJson=HttpclientUtil.doGet("http://passport.cmall.com:8085/verify?token=" + token+"&currentIp="+ip);
              successMap=JSON.parseObject(successJson, Map.class);
               success=successMap.get("status");
            }

            if (loginSeccess){
                //必须成功登陆才能使用
               if (!success.equals("success")){
                   //重定向回passport登陆

                   StringBuffer requestURL=request.getRequestURL();
                   response.sendRedirect("http://passport.cmall.com:8085/index?ReturnUrl="+requestURL);
                   return false;
               }else{
                   //验证通过，覆盖Cookie中的token
                   request.setAttribute("memberId",successMap.get("memberId"));
                   request.setAttribute("nickName",successMap.get("nickName"));
                   if(StringUtils.isNotBlank(token)){
                       CookieUtil.setCookie(request,response,"oldtoken",token,60*60*2,true);
                   }
               }

            }else{
                //无需登陆也可使用,但必须验证
                if (success.equals("success")){
                    //需要将token携带的用户信息写入
                    request.setAttribute("memberId",successMap.get("memberId"));
                    request.setAttribute("nickName",successMap.get("nickName"));


                    //向Cookie覆盖值
                    if (StringUtils.isNotBlank(token)){
                        CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);
                    }

                }
            }

            return true;
        }
}

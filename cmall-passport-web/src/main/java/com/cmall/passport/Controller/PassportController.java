package com.cmall.passport.Controller;

import com.alibaba.fastjson.JSON;
import com.cmall.bean.UmsMember;
import com.cmall.service.UserService;
import com.cmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {



    @Reference
    UserService userService;


    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request) {

        //授权码换取access_token

        //通过code  授权码请求公式
        //https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE

        String s3= "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("client_id","2602921770");
        paramMap.put("client_secret","7c436a3b94f93850bfbdc4764971d6a4");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.cmall.com:8085/vlogin");
        paramMap.put("code",code);

        String access_token_json= HttpclientUtil.doPost(s3,paramMap);

        Map<String,String> access_map= JSON.parseObject(access_token_json,Map.class);

        //access_token 换取用户信息

        String uid=(String)access_map.get("uid");
        String access_token=(String)access_map.get("access_token");

        String user_url="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;

        String user_json=HttpclientUtil.doGet(user_url);

        Map<String,String> user_map= JSON.parseObject(user_json,Map.class);


        //将用户信息保存数据库 ，用户类型设置为微博用
        UmsMember umsMember1= new UmsMember();
        umsMember1.setSourceType("1");
        umsMember1.setAccessCode(code);
        umsMember1.setAccessToken(access_token);

        String souid=String.valueOf(user_map.get("id"));

        umsMember1.setSourceUid(souid);
        umsMember1.setCity((String)user_map.get("location"));
        umsMember1.setNickname((String)user_map.get("screen_name"));

        String g="0";
        String  gender=user_map.get("gender");
        if (gender.equals("n")){
            g="1";
        }
        umsMember1.setGender(g);

        UmsMember umsCheck=new UmsMember();
        umsCheck.setSourceUid(umsMember1.getSourceUid());
        UmsMember umsMemberCheck=userService.checkOauthUser(umsCheck);//检查该用户社交用户以前是否登录过系统


        if(umsMemberCheck==null) {
            umsMember1=  userService.addOauthUser(umsMember1);
        }else{
            umsMember1=umsMemberCheck;
        }


        //生成jwt的token，并且重定向到某一个页面，携带该token
        String token=null;
        String memberId=umsMember1.getId();//rpc的主键返回策略失效
        String nickName=umsMember1.getNickname();
        Map<String,Object> userMap=new HashMap<>();
        userMap.put("memberId",memberId);//是保存到数据库后，主键返回策略生成的id
        userMap.put("nickName",nickName);

        String ip=request.getHeader("x-forwarded-for"); //通过nginx转发的客户端ip
        if(StringUtils.isBlank(ip)) {
            ip= request.getRemoteAddr();//从reuqest获取ip地址
            if(StringUtils.isBlank(ip)){
                ip="127.0.0.1";
            }
        }

        //按照设计的算法对参数进行加密得到token
        token=JwtUtil.encode("2020cmall",userMap,ip);

        //将token存入redis一份
        userService.addJestToken(token,memberId);




        return "redirect:http://search.cmall.com:8083/index?token="+token;
    }



    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token,String currentIp) {


        Map<String,String> map=new HashMap<>();



       Map<String,Object> decode= JwtUtil.decode(token,"2020cmall",currentIp);
        if (decode!=null) {
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickName", (String) decode.get("nickName"));
        }else {
            map.put("status", "fail");
        }
        //通过jwt校验token
        return JSON.toJSONString(map);

    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token="";

        //调用用户服务，验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null) {
            //登录成功

            //用jwt生成token
            String memberId=umsMemberLogin.getId();
            String nickName=umsMemberLogin.getNickname();
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("nickName",nickName);

            String ip=request.getHeader("x-forwarded-for"); //通过nginx转发的客户端ip
            if(StringUtils.isBlank(ip)) {
                ip= request.getRemoteAddr();//从reuqest获取ip地址
                if(StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }

            //按照设计的算法对参数进行加密得到token
            token=JwtUtil.encode("2020cmall",userMap,ip);

            //将token存入redis一份
            userService.addJestToken(token,memberId);

        } else {
            //登陆失败
           token="fail";
        }
        return token;

    }


    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap) {


        modelMap.put("ReturnUrl", ReturnUrl);
        return "index";

    }

}

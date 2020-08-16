package com.cmall.passport.Controller;

import com.alibaba.fastjson.JSON;
import com.cmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;


public class TestOauth2 {


    public static String getCode(){

        //App Key：2602921770
        //App Secret：7c436a3b94f93850bfbdc4764971d6a4
        //
        //
        //授权回调页：http://passport.cmall.com:8085/vlogin
        //取消授权回调页：http://passport.cmall.com:8085/vlogout

        String s1= HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2602921770&response_type=code&redirect_uri=http://passport.cmall.com:8085/vlogin");


        //http://passport.cmall.com:8085/vlogin?code=acd6285bcd788987e86edcca084b974a
        String s2="http://passport.cmall.com:8085/vlogin?code=99c904be111673d61cfcfe32db01183f";
        return s1;
    }

    public static String getAccessCode(){
        //通过code  授权码请求公式
        //https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE

        String s3= "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("client_id","2602921770");
        paramMap.put("client_secret","7c436a3b94f93850bfbdc4764971d6a4");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.cmall.com:8085/vlogin");
        paramMap.put("code","1463b0cee82dff50a61828e91cddbf6a");

        String access_token= HttpclientUtil.doPost(s3,paramMap);

        Map<String,String> access_map= JSON.parseObject(access_token,Map.class);
        return access_map.get("access_token");
    }
    public static Map getUserInfo(){
        //用access_token查询用户信息

        String s4="https://api.weibo.com/2/users/show.json?access_token=2.0014mtqGMraJqCa06f77eaa80D1EBL&uid=1";

        String user_json=HttpclientUtil.doGet(s4);

        Map<String,String> user_map= JSON.parseObject(user_json,Map.class);

        return user_map;
    }




    public static void main(String[] args) {
        getCode();


    }
}

package com.cmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cmall.bean.OmsOrder;
import com.cmall.bean.UmsMember;
import com.cmall.bean.UmsMemberReceiveAddress;
import com.cmall.service.UserService;
import com.cmall.user.mapper.GetReceiAddressByMemberIdMapper;
import com.cmall.user.mapper.GetUserOrderByIdMapper;
import com.cmall.user.mapper.UserMapper;
import com.cmall.util.RedisUtil;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    GetUserOrderByIdMapper getUserSorderByIdMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    GetReceiAddressByMemberIdMapper getReceiAddressByMemberIdMapper;


    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> userList = userMapper.selectAll();
        return userList;
    }

    @Override
    public List<OmsOrder> getUserOrderById(String userId) {

        OmsOrder sorder = new OmsOrder();
        sorder.setMemberId(userId);
        // List<Sorder> getUserOrderById=getUserOrderByIdMapper.selectByExample(sorder);//Example 中的对象应为example，不能为实体类
        return getUserSorderByIdMapper.select(sorder);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info");

                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }

            UmsMember umsMemberFromDb = loginFromDb(umsMember);
            if (umsMemberFromDb != null) {
                jedis.setex("user:" + umsMember.getPassword()+umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberFromDb));

            }
            return umsMemberFromDb;
        } finally {
            if(jedis!=null)
            jedis.close();
        }
    }

    @Override
    public void addJestToken(String token, String memberId) {

        Jedis jedis=redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",60*60*2,token);

        jedis.close();


    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
      UmsMember umsMember=  userMapper.selectOne(umsCheck);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiAddressByMemberId(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);

        List<UmsMemberReceiveAddress>umsMemberReceiveAddresses= getReceiAddressByMemberIdMapper.select(umsMemberReceiveAddress);


        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMemberReceiveAddress getReceiAddressById(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress=new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);

        UmsMemberReceiveAddress umsMemberReceiveAddress1=getReceiAddressByMemberIdMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDb(UmsMember umsMember) {
       List<UmsMember>umsMembers= userMapper.select(umsMember);
       if (umsMembers.size()!=0){
           UmsMember umsMember1=umsMembers.get(0);
           return umsMember1;
       }else{
        return null;}
    }
}


package com.cmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cmall.bean.OmsCartItem;
import com.cmall.cart.mapper.OmsCartItemMapper;
import com.cmall.service.CartService;
import com.cmall.util.RedisUtil;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;



    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);

        OmsCartItem omsCartItem1=omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) {
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updatCart(OmsCartItem omsCartItemFromDb) {

        Example e=new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id",omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb,e);


    }

    @Override
    public void flushCatrCache(String memberId) {

        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem>  omsCartItems= omsCartItemMapper.select(omsCartItem);

        //同步到缓存中
        Jedis jedis=redisUtil.getJedis();

        Map<String,String> map=new HashMap<>();

        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));

            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));


        }

        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",map);


        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {

        Jedis jedis=null;
        List<OmsCartItem> omsCartItems=new ArrayList<>();
        try {
            jedis=redisUtil.getJedis();

            List<String> hvals=jedis.hvals("user:"+userId+":cart");

            for (String hval : hvals) {

                OmsCartItem omsCartItem=JSON.parseObject(hval,OmsCartItem.class);
                omsCartItems.add(omsCartItem);

            }


        }catch (Exception e){
            //处理异常,记录日志
          //  e.printStackTrace();
          //  String message=e.getMessage();
          //  logService.addErrolog(message);
            return null;
        }finally {
            jedis.close();
        }


        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e=new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);


        //缓存同步
        flushCatrCache(omsCartItem.getMemberId());

    }
}

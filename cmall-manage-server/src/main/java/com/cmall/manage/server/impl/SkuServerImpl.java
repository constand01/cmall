package com.cmall.manage.server.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cmall.bean.PmsSkuAttrValue;
import com.cmall.bean.PmsSkuImage;
import com.cmall.bean.PmsSkuInfo;
import com.cmall.bean.PmsSkuSaleAttrValue;
import com.cmall.manage.server.mapper.PmsSkuAttrValueMapper;
import com.cmall.manage.server.mapper.PmsSkuImageMapper;
import com.cmall.manage.server.mapper.PmsSkuInfoMapper;
import com.cmall.manage.server.mapper.PmsSkuSaleAttrValueMapper;
import com.cmall.service.SkuService;
import com.cmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServerImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入skuinfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        //插入平台属性关联
        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : pmsSkuAttrValues) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }
        //插入销售属性关联
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuSaleAttrValues) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }


        //插入图片信息

        List<PmsSkuImage> pmsSkuImages = pmsSkuInfo.getSkuImageList();

        for (PmsSkuImage pmsSkuImage : pmsSkuImages) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }


    }

    public PmsSkuInfo getSkuByIdfromdd(String skuId) {
        //sku的商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);

        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);

        return skuInfo;
    }


    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存

        Jedis jedis = redisUtil.getJedis();
        // 查询缓存
        String skukey = "sku:" + skuId + "info";
        String skuJson = jedis.get(skukey);

        if (StringUtils.isNotBlank(skuJson)) {
            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            //如果缓存中没有，再去查询mysql

            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 1000);  //拿到锁的线程有10秒的过期时间
            if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                //设置成功，有权在10秒内访问数据库
                pmsSkuInfo = getSkuByIdfromdd(skuId);
                if (pmsSkuInfo != null) {
                    //mysql查询结果存入redis
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                } else {
                    //数据库中不存在
                    //为了防止缓存穿透，null或者字符串赋值给reids
                    jedis.setex("sku" + skuId + ":info", 60 * 3, JSON.toJSONString(""));
                }


                //在访问mysql后，将sql的分布锁释放
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)) {
                    //  jedis.eval(“lua”)可用lua脚本，在查询你到key的同时删除该key，防止高并发下的意外发生
                    jedis.del("sku:" + skuId + ":lock");  //用token确认删除的是自己的sku的锁
                }


            } else {
                //设置失败,自旋（该线程在睡眠几秒后，重新尝试访问本方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }


        }


        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySku(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selecttSkuSaleAttrValueListBySku(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();


        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean b=false;
        PmsSkuInfo pmsSkuInfo=new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);

        PmsSkuInfo pmsSkuInfo1=pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        BigDecimal price=pmsSkuInfo1.getPrice();
        if (price.compareTo(productPrice)==0){
            b=true;

        }

        return b;
    }
}

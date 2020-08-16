package com.cmall.cart.Controller;


import annotations.LoginRequired;
import com.alibaba.fastjson.JSON;
import com.cmall.bean.OmsCartItem;
import com.cmall.bean.PmsSkuInfo;
import com.cmall.service.CartService;
import com.cmall.service.SkuService;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @RequestMapping("checkCart")
    @LoginRequired(loginSeccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName"); //request.getAttribute("memberId");
        //调用服务修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);

        //将最新的数据从缓存中取出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);


        modelMap.put("cartList", omsCartItems);


        //被勾选的商品的总额
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", totalAmount);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSeccess = false)
    public String catList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        List<OmsCartItem> omsCartItems = new ArrayList<>();

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");


        if (StringUtils.isNotBlank(memberId)) {
            //已经登录查询db
            omsCartItems = cartService.cartList(memberId);
        } else {
            //没有登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {

                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);

            }
        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList", omsCartItems);
        //被勾选商品的总价格
       // if (omsCartItems!=null&&omsCartItems.size()>=0) {
        if(!omsCartItems.isEmpty()) {
            BigDecimal totalAmount = getTotalAmount(omsCartItems);
            modelMap.put("totalAmount", totalAmount);
        }
        return "cartList";

    }


    @RequestMapping("addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        //调用商品服务查询商品信息

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);


        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("11111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));


        //判断用户是否登录
        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");

        if (StringUtils.isBlank(memberId)) {
            //用户没有登录


            //cookie里原有的数据

            omsCartItems.add(omsCartItem);

            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);


            if (StringUtils.isBlank(cartListCookie)) {
                //cookie为空
                omsCartItems.add(omsCartItem);
            } else {
                //cookie不为空
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exist = if_cart_exit(omsCartItems, omsCartItem);
                if (exist) {
                    //之前添加过，更新购物车添加数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            // cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                        }
                    }
                } else {
                    //之前没有添加，新增当前的购物车
                    omsCartItems.add(omsCartItem);
                }

            }


            //更新cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);


        } else {
            //用户已经登录

            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId, skuId);


            if (omsCartItemFromDb == null) {
                //该用户没有添加过该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("testxiaoming");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);

            } else {
                //该用户添加过该商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updatCart(omsCartItemFromDb);
            }

            //同步缓存
            cartService.flushCatrCache(memberId);

        }


        //

        return "redirect:/success.html";
    }

    private boolean if_cart_exit(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {

        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();


            if (productSkuId.equals(omsCartItem.getProductSkuId())) {

                b = true;
            }

        }

        return true;
    }

    public BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }

        }
        return totalAmount;
    }
}

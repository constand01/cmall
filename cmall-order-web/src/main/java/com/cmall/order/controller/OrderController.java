package com.cmall.order.controller;

import annotations.LoginRequired;
import com.cmall.bean.OmsCartItem;
import com.cmall.bean.OmsOrder;
import com.cmall.bean.OmsOrderItem;
import com.cmall.bean.UmsMemberReceiveAddress;
import com.cmall.service.CartService;
import com.cmall.service.OrderService;
import com.cmall.service.SkuService;
import com.cmall.service.UserService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;


    @RequestMapping("submitOrder")
    @LoginRequired(loginSeccess =true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");

        //检查交易码
        String success=orderService.checkCode(memberId,tradeCode);

        if (success.equals("success")) {

            //订单对象
            List<OmsOrderItem> omsOrderItems=new ArrayList<>();
            OmsOrder omsOrder=new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount();运费支付后，再生成该信息
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickName);
            omsOrder.setNote("快点发货");
            String outTradeNo="cmall";
            outTradeNo=outTradeNo+System.currentTimeMillis();//将毫秒时间戳接到外部订单号。
            SimpleDateFormat sdf=new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo=outTradeNo+sdf.format(new Date());//将时间字符串拼接到外部订单号
            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress=userService.getReceiAddressById(receiveAddressId);
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());

            //当前日期加一天
            Calendar c= Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date time=c.getTime();
            omsOrder.setReceiveTime(time);

            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setTotalAmount(totalAmount);

            //根据用户id获得要购买的商品列表（购物车）和总价格
           List <OmsCartItem> omsCartItems= cartService.cartList(memberId);


            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem.getIsChecked().equals("1")){
                    //获得订单详情列表
                    OmsOrderItem omsOrderItem=new OmsOrderItem();
                    //验价
                   boolean b= skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (b==false){

                        ModelAndView nv=new ModelAndView("tradeFail");
                        return nv;
                    }
                    //验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setOrderSn("");//外部订单号，用来和其他系统交互，防止重复
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");//在仓库中的skuid
                    omsOrderItem.setOrderSn(outTradeNo);
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItems.add(omsOrderItem);
                }
            }

            omsOrder.setOmsOrderItems(omsOrderItems);

            //将订单和订单详情写入数据库

            //删除购物车的对应商品
              orderService.saveOrder(omsOrder);

            //重定向到支付系统

            ModelAndView nv=new ModelAndView("redirect:http://payment.cmall.com:8087/index");
            nv.addObject("outTradeNo",outTradeNo);
            nv.addObject("totalAmount",totalAmount);
            return nv;
        }else {

            ModelAndView nv=new ModelAndView("tradeFail");
            return nv;
        }


    }


        @RequestMapping("toTrade")
    @LoginRequired(loginSeccess =true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickName = (String) request.getAttribute("nickName");

        //地址集合
        List<UmsMemberReceiveAddress> receiveAddresses = userService.getReceiAddressByMemberId(memberId);


        //将购物车的集合转化为页面的结算清单集合
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            //每循环一个购物车对象，就封装一个商品的详情到OmsOrderItems
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());

                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("totalAmount",getTotalAmount(omsCartItems));
       modelMap.put("userAddressList",receiveAddresses);


       //生成交易码，为了在提交订单的时候做交易码的校验
        String tradeCode=orderService.getTradeCode(memberId);
         modelMap.put("tradeCode",tradeCode);
        return "trade";
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

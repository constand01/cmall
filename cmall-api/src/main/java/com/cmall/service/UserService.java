package com.cmall.service;

import com.cmall.bean.OmsOrder;
import com.cmall.bean.UmsMember;
import com.cmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    List<OmsOrder> getUserOrderById(String userId);

    UmsMember login(UmsMember umsMember);

    void addJestToken(String token, String memberId);

    public UmsMember addOauthUser(UmsMember umsMember);

   public UmsMember checkOauthUser(UmsMember umsCheck);

   public List<UmsMemberReceiveAddress> getReceiAddressByMemberId(String memberId);

   public UmsMemberReceiveAddress getReceiAddressById(String receiveAddressId);
}

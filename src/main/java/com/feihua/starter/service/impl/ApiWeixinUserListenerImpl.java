package com.feihua.starter.service.impl;

import com.feihua.framework.base.modules.user.dto.BaseUserDto;
import com.feihua.wechat.common.api.ApiWeixinUserListener;
import com.feihua.wechat.common.dto.WeixinUserDto;
import org.springframework.stereotype.Service;

/**
 * 实现用户发发现监听，这里不处理
 * Created by yangwei
 * Created at 2018/7/24 18:22
 */
@Service
public class ApiWeixinUserListenerImpl implements ApiWeixinUserListener{
    @Override
    public void onAddWexinUser(WeixinUserDto weixinUserDto) {

    }
}

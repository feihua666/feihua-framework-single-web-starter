package com.feihua.starter.frameworksupport;

import com.feihua.framework.base.modules.user.api.ApiBaseUserPoService;
import com.feihua.framework.base.modules.user.dto.BaseUserAddParamDto;
import com.feihua.framework.base.modules.user.dto.BaseUserAuthDto;
import com.feihua.framework.base.modules.user.po.BaseUserPo;
import com.feihua.framework.constants.DictEnum;
import com.feihua.framework.rest.service.AccountServiceImpl;
import com.feihua.framework.shiro.pojo.token.WxMiniProgramToken;
import com.feihua.framework.shiro.pojo.token.WxPlatformToken;
import com.feihua.utils.properties.PropertiesUtils;
import com.feihua.wechat.ParamsDto;
import com.feihua.wechat.common.api.ApiWeixinAccountPoService;
import com.feihua.wechat.common.dto.WeixinAccountDto;
import com.feihua.wechat.common.po.WeixinAccountPo;
import com.feihua.wechat.miniprogram.api.ApiMiniProgramService;
import com.feihua.wechat.miniprogram.dto.LoginCredentialsDto;
import feihua.jdbc.api.pojo.BasePo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;

/**
 * Created by yangwei
 * Created at 2018/7/26 22:09
 */
@Service("accountService")
public class MyAccountServiceImpl extends AccountServiceImpl {

    @Autowired
    private ApiMiniProgramService apiMiniProgramService;
    @Autowired
    private ApiBaseUserPoService apiBaseUserPoService;
    @Autowired
    private ApiWeixinAccountPoService apiWeixinAccountPoService;

    @Override
    public AuthenticationToken createToken(javax.servlet.ServletRequest servletRequest, DictEnum.LoginType loginType, String loginClient) {
        AuthenticationToken token = null;
        switch (loginType) {
            // 小程序，支持多个小程序
            case WX_MINIPROGRAM:

                String code = servletRequest.getParameter("code");
                String type = servletRequest.getParameter("type");

                String nickname = servletRequest.getParameter("nickname");
                String photo = servletRequest.getParameter("photo");
                String gender = servletRequest.getParameter("gender");

                WeixinAccountPo weixinPo = new WeixinAccountPo();
                weixinPo.setDelFlag(BasePo.YesNo.N.name());
                weixinPo.setStatus(BasePo.YesNo.Y.name());
                weixinPo.setWhich(type);
                weixinPo.setType("weixin_miniprogram");
                List<WeixinAccountDto> weixinAccountDtos = apiWeixinAccountPoService.selectList(weixinPo);

                ParamsDto paramsDto = new ParamsDto();
                if (weixinAccountDtos != null && weixinAccountDtos.size() > 0) {
                    paramsDto.setAppId(weixinAccountDtos.get(0).getAppid());
                    paramsDto.setSecret(weixinAccountDtos.get(0).getAppsecret());
                } else {
                    paramsDto.setAppId(PropertiesUtils.getProperty("miniprogram." + type + ".appid"));
                    paramsDto.setSecret(PropertiesUtils.getProperty("miniprogram." + type + ".secret"));
                }

                LoginCredentialsDto credentialsDto = apiMiniProgramService.fetchLoginCredentials(code, paramsDto);
                WxMiniProgramToken openidToken = new WxMiniProgramToken();
                openidToken.setOpenid(credentialsDto.getOpenid());

                BaseUserAuthDto userAuthDto = super.findUserAuthByToken(openidToken);
                if (userAuthDto == null) {
                    BaseUserAddParamDto baseUserAddParamDto = new BaseUserAddParamDto();
                    baseUserAddParamDto.setIdentifier(openidToken.getOpenid());
                    baseUserAddParamDto.setIdentityType(DictEnum.LoginType.WX_MINIPROGRAM.name());
                    baseUserAddParamDto.setCurrentUserId(BasePo.DEFAULT_USER_ID);
                    baseUserAddParamDto.setNickname(nickname);
                    baseUserAddParamDto.setPhoto(photo);
                    baseUserAddParamDto.setLocked(BasePo.YesNo.N.name());
                    baseUserAddParamDto.setGender(gender);
                    BaseUserPo baseUserPo = apiBaseUserPoService.addUser(baseUserAddParamDto);

                }

                token = openidToken;
                break;
            case WX_PLATFORM:
                String which = servletRequest.getParameter("type");
                // 该key是提前放好的，请调用相关接口获取openid，并设置该key
                String openid = (String) SecurityUtils.getSubject().getSession().getAttribute("publickplatform_openid_" + which);
                WxPlatformToken wxPlatformToken = new WxPlatformToken();
                wxPlatformToken.setOpenid(openid);
                token = wxPlatformToken;
                break;
        }
        return token;
    }

    @Override
    public boolean validateCaptchaWhenLogin(ServletRequest servletRequest, ServletResponse servletResponse) {
        String loginClient = super.resolveLoginClient(servletRequest);
        if(DictEnum.LoginClientType.pc.name().equals(loginClient)){
            return true;
        }
       return false;
    }
}

package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class Userservice implements UserService {
    @Autowired
    JwtProperties jwtProperties;
private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
@Autowired
 private WeChatProperties weChatProperties;
@Autowired
private UserMapper userMapper;
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {


        Map map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        JSONObject jsonObject = (JSONObject) JSON.parse(json);
        String openid = jsonObject.getString("openid");
        String sessionKey = jsonObject.getString("session_key");
        if (openid == null) {
            throw new LoginFailedException("微信登录失败");
        }
        //判断是否是新用户
        User byOpenid = userMapper.getByOpenid(openid);
        if (byOpenid == null) {
            byOpenid=User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(byOpenid);

        }

        return byOpenid;

    }
}

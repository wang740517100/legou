package cn.wangkf.auth.service.impl;

import cn.wangkf.auth.client.UserClient;
import cn.wangkf.auth.entity.UserInfo;
import cn.wangkf.auth.properties.JwtProperties;
import cn.wangkf.auth.service.AuthService;
import cn.wangkf.auth.utils.JwtUtils;
import cn.wangkf.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    /**
     * 用户授权
     * @param username
     * @param password
     * @return
     */
    @Override
    public String authentication(String username, String password) {

        try{
            //1.调用微服务查询用户信息
            User user = this.userClient.queryUser(username,password);
            //2.查询结果为空，则直接返回null
            if (user == null){
                return null;
            }
            //3.查询结果不为空，则生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                    properties.getPrivateKey(), properties.getExpire());
            return token;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

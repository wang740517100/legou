package cn.wangkf.auth.controller;

import cn.wangkf.auth.entity.UserInfo;
import cn.wangkf.auth.properties.JwtProperties;
import cn.wangkf.auth.service.AuthService;
import cn.wangkf.auth.utils.JwtUtils;
import cn.wangkf.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @Feature: 登录授权
 */
@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties properties;

    /**
     * 登录授权
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        //1.登录校验
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        System.out.println(token);
        //2.将token写入cookie，并指定httpOnly为true，防止通过js获取和修改
        CookieUtils.setCookie(request, response, properties.getCookieName(), token, properties.getCookieMaxAge(), true);

        return ResponseEntity.ok().build();
    }

    /**
     * 用户验证
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LG_TOKEN") String token, HttpServletRequest request,
                                               HttpServletResponse response){
        try{
            //1.从token中解析token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,this.properties.getPublicKey());
            //2.解析成功要重新刷新token
            token = JwtUtils.generateToken(userInfo, this.properties.getPrivateKey(), this.properties.getExpire());
            //3.更新Cookie中的token
            CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getCookieMaxAge());
            //4.解析成功返回用户信息
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            e.printStackTrace();
        }
        //5.出现异常,相应401
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}

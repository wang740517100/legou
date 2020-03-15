package cn.wangkf.user.controller;

import cn.wangkf.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import cn.wangkf.user.pojo.User;

import javax.management.RuntimeErrorException;
import javax.validation.Valid;
import java.util.stream.Collectors;


@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户数据检查
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable(value = "type") Integer type){
        Boolean result = this.userService.checkData(data,type);
        if (result == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(result);
    }


    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity senVerifyCode(@RequestParam("phone") String phone){
        Boolean result = this.userService.sendVerifyCode(phone);
        if (result == null || !result){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult res, @RequestParam("code") String code) throws RuntimeErrorException {
        //自定义错误
        //if (res.hasFieldErrors()) {
            /*throw new RuntimeErrorException(res.getFieldErrors().stream().
                    map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));*/
        //}

        Boolean result = this.userService.register(user, code);
        if(result == null || !result){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户验证
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username, @RequestParam("password") String password){
        User user = this.userService.queryUser(username,password);
        if (user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(user);
    }

}

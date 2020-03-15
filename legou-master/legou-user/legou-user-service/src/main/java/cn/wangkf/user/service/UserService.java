package cn.wangkf.user.service;

import cn.wangkf.user.pojo.User;


public interface UserService {
    /**
     * 检查用户名和手机号是否可用
     * @param data
     * @param type
     * @return
     */
    Boolean checkData(String data, Integer type);

    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    Boolean sendVerifyCode(String phone);

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    Boolean register(User user, String code);

    /**
     * 用户验证
     * @param username
     * @param password
     * @return
     */
    User queryUser(String username, String password);
}

package cn.wangkf.user.service.impl;

import cn.wangkf.user.utils.CodecUtils;
import cn.wangkf.user.mapper.UserMapper;
import cn.wangkf.user.pojo.User;
import cn.wangkf.user.service.UserService;
import cn.wangkf.common.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "user:code:phone";

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Boolean checkData(String data, Integer type) {
        User user = new User();
        switch (type){
            case 1 :
                user.setUsername(data);
                break;
            case 2 :
                user.setPhone(data);
                break;
            default:
                return null;
        }
        return userMapper.selectCount(user) == 0;
    }

    /**
     * 发送短信验证码
     * @param phone
     */
    @Override
    public Boolean sendVerifyCode(String phone) {

        //1.生成验证码
        String code = NumberUtils.generateCode(6);

        try {
            Map<String,String> msg = new HashMap<>();
            msg.put("phone", phone);
            msg.put("code", code);
            //2.发送短信
            amqpTemplate.convertAndSend("legou.sms.exchange", "sms.verify.code", msg);

            //3.将code存入redis
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);

            return true;

        }catch (Exception e){
            logger.error("发送短信失败。phone：{}，code：{}",phone,code);
            return false;
        }
    }

    @Override
    public Boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        //1.从redis中取出验证码
        String codeCache = stringRedisTemplate.opsForValue().get(key);
        //2.检查验证码是否正确
        if(!codeCache.equals(code)){
            //不正确，返回
            return false;
        }
        user.setId(null);
        user.setCreated(new Date());

        //3.密码加密
        //String encodePassword = CodecUtils.passwordBcryptEncode(user.getUsername().trim(), user.getPassword().trim());
        //盐值算法
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        String encodePassword = CodecUtils.md5Hex(user.getPassword(), salt);

        user.setPassword(encodePassword);
        //4.写入数据库
        boolean result = userMapper.insertSelective(user) == 1;
        //5.如果注册成功，则删掉redis中的code
        if (result){
            try{
                stringRedisTemplate.delete(KEY_PREFIX + user.getPhone());
            }catch (Exception e){
                logger.error("删除缓存验证码失败，code:{}",code,e);
            }
        }
        return result;
    }

    /**
     * 用户验证
     * @param username
     * @param password
     * @return
     */
    @Override
    public User queryUser(String username, String password) {
        //1.查询
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);

        //2.校验用户名
        if (user == null){
            return null;
        }
        //3. 校验密码
        //boolean result = CodecUtils.passwordConfirm(username + password,user.getPassword());
        boolean result = StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password, user.getSalt()));
        if (!result){
            return null;
        }

        //4.用户名密码都正确
        return user;
    }
}

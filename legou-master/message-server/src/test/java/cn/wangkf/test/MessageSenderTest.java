package cn.wangkf.test;

import cn.wangkf.pojo.SmsProperties;
import cn.wangkf.utils.SmsUtils;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by wangk on 2019-01-20.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageSenderTest {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties smsProperties;


    @Test
    public void codeSender(){
        try {
            SendSmsResponse response = this.smsUtils.sendSms("17607191204", "20120403", smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate());
        }catch (ClientException e){
            return;
        }}


}

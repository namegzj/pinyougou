package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service(interfaceClass = UserService.class)
public class UserServiceImpl extends BaseServiceImpl<TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itcastSmsQueue;

    @Value("${signName}")
    private String signName;
    @Value("${templateCode}")
    private String templateCode;

    @Override
    public PageResult search(Integer page, Integer rows, TbUser user) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(user.get***())){
            criteria.andLike("***", "%" + user.get***() + "%");
        }*/

        List<TbUser> list = userMapper.selectByExample(example);
        PageInfo<TbUser> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void sendSmsCode(String phone) {
        //1、生成6位数字随机数
        String code = (long)(Math.random() * 1000000) + "";
        System.out.println("发送的验证码为：" + code);
        //2、存入redis并设置过期时间5分钟
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(10, TimeUnit.MINUTES);

        //3、发送信息到MQ
        jmsTemplate.send(itcastSmsQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage map = session.createMapMessage();
                map.setString("mobile",phone);
                map.setString("signName",signName);
                map.setString("templateCode",templateCode);
                map.setString("templateParam","{\"code\":" + code + "}");
                return map;
            }
        });
    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        String code = (String) redisTemplate.boundValueOps(phone).get();
        if (smsCode.equals(code)) {
            redisTemplate.delete(phone);
            return true;
        }
        return false;
    }
}

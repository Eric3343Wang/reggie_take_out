package com.tm.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tm.reggie.common.R;
import com.tm.reggie.entity.User;
import com.tm.reggie.service.UserService;
import com.tm.reggie.utils.SMSUtils;
import com.tm.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 取货验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}",code);
            //调用阿里云提供得短信服务
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //将生成得验证码保存到redis中
            redisTemplate.opsForValue().set(phone+"_code",code,5, TimeUnit.MINUTES);
            return R.success("验证码发送成功"+code);
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();

        //从redis中获取保存得验证码比对
        Object sessionPhone = redisTemplate.opsForValue().get(phone + "_code");

        if(sessionPhone != null && sessionPhone.equals(code)){
            //登陆成功
            //判断当前用户是否为新用户，如果是新用户自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //注册
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //id放入session
            session.setAttribute("user",user.getId());
            //用户登录成功，删除redis中得验证码
            redisTemplate.delete(phone + "_code");

            return R.success(user);
        }
        return R.error("登陆失败");
    }
}

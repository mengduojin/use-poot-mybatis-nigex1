package com.controller;

import com.alibaba.fastjson.JSON;
import com.po.Dto;
import com.po.ItripUser;
import com.service.ItripUseService;
import com.util.*;
import com.util.vo.ItripTokenVO;
import com.util.vo.ItripUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping(value = "/api")
public class usesController {
    Jedis jedis=new Jedis("127.0.0.1",6379);
    @Autowired
    private ItripUseService itripUseService;//注入biz业务层
    //手机注册
    @RequestMapping(value = "/registerbyphone")
    public Dto registerbyphone(HttpServletRequest request, HttpServletResponse response, @RequestBody ItripUserVO itripUserVO){
        System.out.println("手机注册");

        if (itripUserVO!=null&&!itripUserVO.equals("")){//如果通过前台页面传进来的不是null或者不是空值
            System.out.println("0000");
         //通过用户名在数据库查询这个用户的对象
            ItripUser olduser=itripUseService.getitripusesbycode(itripUserVO.getUserCode());

         if(olduser==null) { //该用户没有注册，请先去注册
             System.out.println("11111");
             ItripUser itripUser = new ItripUser();
             String md5passwd = MD5Util.getMd5(itripUserVO.getUserPassword(), 32);
             itripUser.setUsercode(itripUserVO.getUserCode());//拿到邮件或者手机号的注册类型
             itripUser.setUsername(itripUserVO.getUserName());
             itripUser.setUserpassword(md5passwd);
             itripUser.setUsertype(0);
             itripUser.setActivated(0);
             Date date = new Date();
             itripUser.setModifydate(date);
             int aoo = itripUseService.count();
             itripUser.setCreatedby((long) aoo);
             ;
             boolean flag = itripUseService.save(itripUser);
             if (flag) {
                 //获取短信验证码
                 String mail = SMSUtil.testcheck(itripUser.getUsercode());
                 jedis.setex(itripUser.getUsercode(), 120, mail); //存入redis的缓存中
                 return DtoUtil.returnSuccess("注册成功");
             } else {
                 return DtoUtil.returnFail("注册失败", ErrorCode.AUTH_AUTHENTICATION_FAILED);
             }
         }else {//用户已经注册   第一种注册激活，第二种注册未激活
             System.out.println("1213");
             String smssk=null;
             smssk=jedis.get(itripUserVO.getUserCode());

             if(olduser.getActivated()==0&&smssk==null){//注册未激活
                 System.out.println("1211111");
                 String stttem=SMSUtil.testcheck(itripUserVO.getUserCode());
                  jedis.setex(itripUserVO.getUserCode(), 120, stttem); //存入redis的缓存中
                 System.out.println("该用户已注册未激活");
                 return DtoUtil.returnFail("该用户已注册未激活，激活短信已发送", ErrorCode.AUTH_AUTHENTICATION_FAILED);
              }else {
                 System.out.println("激活失败，该用户已经激活");
                 return DtoUtil.returnFail("注册失败，该用户已经注册过", ErrorCode.AUTH_AUTHENTICATION_FAILED);
             }
         }
        }else {
            return DtoUtil.returnFail("注册信息为空，请返回重新注册", ErrorCode.AUTH_AUTHENTICATION_FAILED);
        }

    }
    @RequestMapping(value = "/validatephone")
    public Dto validatephone(HttpServletRequest request,HttpServletResponse response,String user,String code){
        System.out.println("手机验证激活方式");
        if(jedis.get(user)!=null){  //jedis中有缓存
                if (jedis.get(user).equals(code)){//验证码验证
                    boolean flag=itripUseService.updatevalidatephone(user);
                     if(flag){
                         return DtoUtil.returnSuccess("激活成功");
                     }else{
                         System.out.println("激活失败");
                     }
                }
        }else {
            return DtoUtil.returnFail("激活失败,验证码过期",ErrorCode.AUTH_AUTHENTICATION_FAILED);
        }
        return null;

    }
    //直接激活已经有的账号
    @RequestMapping(value="/activate")
    public Dto activate(HttpServletRequest request,HttpServletResponse response,String user,String code){
        ItripUser olduser=itripUseService.getitripusesbycode(user);
        if(user!=null) {
            if(jedis.get(user)!=null){
                if (jedis.get(user).equals(code)){//验证码验证
                    boolean flag=itripUseService.updatevalidatephone(user);
                    if(flag){
                        return DtoUtil.returnSuccess("激活成功");
                    }else{
                        System.out.println("激活失败");
                    }
                }
            }else {//没有验证码  ，从新发送验证码
                if(user.indexOf("@")!=-1){
                    String emailck = EmailUtil.emailregister(olduser);
                    jedis.setex(olduser.getUsercode(), 120, emailck); //存入redis的缓存中
                    return DtoUtil.returnFail("该用户已注册，未激活,验证码已下发", ErrorCode.AUTH_AUTHENTICATION_FAILED);
                }else {
                    String mail = SMSUtil.testcheck(user);
                    jedis.setex(user, 120, mail); //存入redis的缓存中
                    return DtoUtil.returnFail("该用户已注册，未激活,验证码已下发", ErrorCode.AUTH_AUTHENTICATION_FAILED);
                }
                }
        }else {
            return DtoUtil.returnFail("账号不存在",ErrorCode.AUTH_AUTHENTICATION_FAILED);
        }

        return null;
    }
    //邮箱是否重复
    @RequestMapping(value = "/ckusr")
    public Dto ckusr(HttpServletRequest request,HttpServletResponse response,String name){
        System.out.println("验证邮箱是否重复");
        ItripUser olduser=itripUseService.getitripusesbycode(name);
        System.out.println("查询邮箱中uses对象是"+olduser);
        if(olduser==null){
          return DtoUtil.returnSuccess("邮箱可以使用");
        }else{
            return DtoUtil.returnFail("此邮箱已经注册","33231");
        }

    }
    //邮箱注册
    @RequestMapping(value = "/doregister")
    public Dto doregister(HttpServletRequest request,HttpServletResponse response,@RequestBody ItripUserVO itripUserVO){
       if(itripUserVO!=null&&!itripUserVO.equals("")){//从前台传过来不是空或者null
           ItripUser itripUser = new ItripUser();
           String md5passwd = MD5Util.getMd5(itripUserVO.getUserPassword(), 32);
           itripUser.setUsercode(itripUserVO.getUserCode());//拿到邮件或者手机号的注册类型
           itripUser.setUsername(itripUserVO.getUserName());
           itripUser.setUserpassword(md5passwd);
           itripUser.setUsertype(0);
           itripUser.setActivated(0);
           Date date = new Date();
           itripUser.setModifydate(date);
           int aoo = itripUseService.count();
           itripUser.setCreatedby((long) aoo);
           ;
           boolean flag = itripUseService.save(itripUser);
           if (flag){
               //获取短信验证码
               String emailck = EmailUtil.emailregister(itripUser);
               jedis.setex(itripUser.getUsercode(), 120, emailck); //存入redis的缓存中
               return DtoUtil.returnSuccess("注册成功");
           }else {
               return DtoUtil.returnFail("注册失败",ErrorCode.AUTH_AUTHENTICATION_FAILED);
           }
       }
        return null;
    }
    @RequestMapping(value = "/dologin")
   public Dto dologin(HttpServletRequest request,HttpServletResponse response,String name,String password){
      if(EmptyUtils.isNotEmpty(name)&&EmptyUtils.isNotEmpty(password)) {
          ItripUser olduser = itripUseService.dologein(new ItripUser(name.trim(),MD5Util.getMd5(password.trim(),32)));
        if(EmptyUtils.isNotEmpty(olduser)){   //生成token
              String token=TokenUtil.getTokenGenerator(request.getHeader("user-agent"),olduser);
              //request.getHeader("user-agent")拿浏览器的请求 通过请求头http获取浏览器标识
              if(token.startsWith("token:PC-")){
                  jedis.setex(name,3600,token);
                  //使用token 存储用户数据，为后面的信息做用户操作
                  String strjson= JSON.toJSONString(olduser);
                  System.out.println("存储于jedis的strjson"+strjson.toString());
                  jedis.setex(token,3600,strjson);
                  ItripTokenVO itripTokenVO=new ItripTokenVO(token,Calendar.getInstance().getTimeInMillis()+60*60*1000,Calendar.getInstance().getTimeInMillis());
                  return DtoUtil.returnDataSuccess(itripTokenVO);
              }
        }else {
            return DtoUtil.returnFail("登录失败,账号错误","1002");
        }
      }else {
          return DtoUtil.returnFail("账号为空","1000");
      }
      return null;
    }
    @RequestMapping(value = "logout")
 public Dto logout(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始注销");
        String token=request.getHeader("token");
        if(!TokenUtil.validate(request.getHeader("user-agent"),token)){
          return DtoUtil.returnFail("token失效,请从新登录","12334");
        }
        try {
            TokenUtil.delete(token);
            return DtoUtil.returnSuccess("注销成功");
        } catch (Exception e) {
            return DtoUtil.returnFail(e.getMessage(),"12345");
        }
 }
}


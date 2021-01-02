package com.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.po.ItripUser;
import cz.mallat.uasparser.UserAgentInfo;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TokenUtil {
    private static String tokenPrefix = "token:";//统一加入 token前缀标识、
    //生成token的头部分
    private static Jedis jedis = new Jedis("127.0.0.1", 6379);
    //导入redis
    public String getTokenPrefix() {
        return tokenPrefix;
    }
//上面的get set方法
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    /***
     * @param agent Http头中的user-agent信息
     * @param us 用户信息
     * @return Token格式<br/>
     * 	PC：“前缀PC-USERCODE-USERID-CREATIONDATE-RONDEM[6位]”
     *  <br/>
     *  Android：“前缀ANDROID-USERCODE-USERID-CREATIONDATE-RONDEM[6位]”
     *  生成token
     */                                     //传入一个us的对象
    public static String getTokenGenerator(String agent, ItripUser us) {
        try {
            UserAgentInfo userAgentInfo = UserAgentUtil.getUasParser().parse(agent);
            //通过你的信息工具类来获取到底是pc端 还是手机端
            System.out.println("请求头信息来自来个浏览器"+userAgentInfo.toString());
            StringBuilder sb = new StringBuilder();
            //搞一个字符串准备拼token
            System.out.print("是前缀=");
            sb.append(tokenPrefix);//统一前缀   第一个数据是token；
            //tokenPrefix token：前缀
            if (userAgentInfo.getDeviceType().equals(UserAgentInfo.UNKNOWN)) {
               //从userAgentInfo取请求类型出来  equals  判断是不是正常请求项
                System.out.println("44444444");
                if (UserAgentUtil.CheckAgent(agent)) {
                    //判断请求项
                    System.out.print("是移动端=");
                    sb.append("MOBILE-");//是移动端 加移动   token：MOBILE-
                } else {
                    System.out.print("是pc端=");
                    sb.append("PC-");//是pc端 加pc   token：PC-
                }
            } else if (userAgentInfo.getDeviceType()//在请求项类型上
                    .equals("Personal computer")) {//判断是不是个人电脑
                System.out.print("是个人pc端=");
                sb.append("PC-");
            } else {
                System.out.print("是移动端=");
                sb.append("MOBILE-");
            }
            System.out.print("密码=");
                sb.append(MD5Util.getMd5(us.getUsercode(), 32) + "-");//加密用户名称、
            System.out.print("用户id=");
                sb.append(us.getId() + "-");//用户的id
            System.out.print("系统当前时间=");
                sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                        + "-");//系统的当前时间
            System.out.print("加密当前浏览器=");
                sb.append(MD5Util.getMd5(agent, 6));// 识别客户端的简化实现——6位MD5码
            //对浏览器的类型进行的加密
            System.out.println("tokenUtil生成：=："+sb);
            return sb.toString();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

       return  null;
    }
    /*token时间验证*/
    public static boolean validate(String agent, String token) {

        if (jedis.get(token)==null) {// token不存在
            return false;
        }
        try {
            Date TokenGenTime;// token生成时间
            String agentMD5;
            String[] tokenDetails = token.split("-");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            TokenGenTime = formatter.parse(tokenDetails[3]);
            long passed = Calendar.getInstance().getTimeInMillis()
                    - TokenGenTime.getTime();
            if(passed>60*60*1000)
                return false;
            agentMD5 = tokenDetails[4];
            if(MD5Util.getMd5(agent, 6).equals(agentMD5))
                return true;
        } catch (ParseException e) {
            return false;
        }
        return false;
    }
   /*删除token*/
    public static void delete(String token) {
        if (jedis.get(token)!=null) {
            jedis.del(token);
        }
    }
    /*置换token*/
   /* public static String replaceToken(String agent, String token)
            throws TokenValidationFailedException {

        // 验证旧token是否有效
        if (jedis.get(token)==null) {// token不存在
            throw new TokenValidationFailedException("未知的token或 token已过期");// 终止置换
        }
        Date TokenGenTime;// token生成时间
        try {
            String[] tokenDetails = token.split("-");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            TokenGenTime = formatter.parse(tokenDetails[3]);
        } catch (ParseException e) {
            throw new TokenValidationFailedException("token格式错误:" + token);
        }

        long passed = Calendar.getInstance().getTimeInMillis()
                - TokenGenTime.getTime();// token已产生时间
        if (passed <  1000) {// 置换保护期内
            throw new TokenValidationFailedException("token处于置换保护期内，剩余"
                    + (1000 - passed) / 1000
                    + "(s),禁止置换");
        }
        // 置换token
        String newToken = "";
       List<ItripUser> listuser = JSON.parseArray(jedis.get(token).toString(),ItripUser.class);
       ItripUser user=listuser.get(0);
        long ttl = jedis.ttl(token);// token有效期（剩余秒数 ）
        if (ttl > 0 || ttl == -1) {// 兼容手机与PC端的token在有效期
            newToken = getTokenGenerator(agent, user);
            //this.save(newToken, user);// 缓存新token
            //将token存入redis(因为将来取值时需要返回对象所以转成json存进去)
            String strJson = JSON.toJSONString(user);
            jedis.setex(newToken,7200,strJson);
            jedis.setex(token, 7200,
                    JSON.toJSONString(user));// 2分钟后旧token过期，注意手机端由永久有效变为2分钟（REPLACEMENT_DELAY默认值）后失效
        } else {// 其它未考虑情况，不予置换
            throw new TokenValidationFailedException("当前token的过期时间异常,禁止置换");
        }
        return newToken;
    }*/
}

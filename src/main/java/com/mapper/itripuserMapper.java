package com.mapper;

import com.po.ItripUser;

public interface itripuserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ItripUser record);//用手机注册或者用邮箱添加

    int insertSelective(ItripUser record);

    ItripUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ItripUser record);

    int updateByPrimaryKey(ItripUser record);

    int count();

    ItripUser getitripusesbycode(String userCode);

    int updatevalidatephone(String userCode);

    ItripUser dologein(ItripUser itripUser);
}
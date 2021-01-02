package com.service;

import com.po.ItripUser;

public interface ItripUseService {
    public boolean save(ItripUser itripUser);
    public int count();
    public ItripUser getitripusesbycode(String userCode);
    public boolean updatevalidatephone(String userCode);
    public ItripUser dologein(ItripUser itripUser);
}

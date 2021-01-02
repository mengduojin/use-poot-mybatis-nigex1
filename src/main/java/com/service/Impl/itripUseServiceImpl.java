package com.service.Impl;

import com.mapper.itripuserMapper;
import com.po.ItripUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class itripUseServiceImpl implements com.service.ItripUseService {
    @Autowired
    private itripuserMapper itripusermapper;

    @Override
    public boolean save(ItripUser itripUser) {
      int code=itripusermapper.insert(itripUser);
      if(code>0){
        return true;
      }
        return false;
    }

    @Override
    public int count() {
        return itripusermapper.count()+1;
    }

    @Override
    public ItripUser getitripusesbycode(String userCode) {
        return itripusermapper.getitripusesbycode(userCode);
    }

    @Override
    public boolean updatevalidatephone(String userCode) {
        int code=itripusermapper.updatevalidatephone(userCode);
        if(code>0){
            return true;
        }
        return false;
    }

    @Override
    public ItripUser dologein(ItripUser itripUser) {
        return itripusermapper.dologein(itripUser);
    }
}

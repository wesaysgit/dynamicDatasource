package com.wesays.dynamic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wesays.dynamic.entity.TUser;
import com.wesays.dynamic.mapper.TUserMapper;
import com.wesays.dynamic.service.IUserService;
import com.wesays.dynamic.uitl.IdWorker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    private final IdWorker worker = new IdWorker();

    @Resource
    private TUserMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUser user) {
        long id = worker.nextId();
        user.setId(id);
        int res = mapper.insert(user);
        if ("1".equals(user.getUsername())) {
            throw new RuntimeException("异常抛出，事务需要回滚");
        }
        return res;
    }

    @Override
    public int del(TUser user) {
        return mapper.deleteById(user.getId());
    }

    @Override
    @Transactional
    public int upd(TUser user) {
        int res = mapper.updateById(user);
        if ("32".equals(user.getUsername())) {
            int i = 1 / 0;
        }
        return res;
    }

    @Override
    public TUser one(TUser user) {
        return mapper.selectById(user.getId());
    }

    @Override
    public List<TUser> all() {
        QueryWrapper<TUser> queryWrapper = new QueryWrapper<>();
        return mapper.selectList(queryWrapper);
    }
}

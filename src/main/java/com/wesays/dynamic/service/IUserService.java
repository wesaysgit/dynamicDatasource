package com.wesays.dynamic.service;

import com.wesays.dynamic.entity.TUser;

import java.util.List;

public interface IUserService {

    int insert(TUser user);

    int del(TUser user);

    int upd(TUser user);

    TUser one(TUser user);

    List<TUser> all();
}

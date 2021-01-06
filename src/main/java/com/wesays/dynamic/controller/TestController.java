package com.wesays.dynamic.controller;

import com.wesays.dynamic.entity.TUser;
import com.wesays.dynamic.service.IUserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class TestController {

    @Resource
    private IUserService service;

    @RequestMapping("/add")
    public int add(@RequestBody TUser user) {
        return service.insert(user);
    }

    @RequestMapping("/one")
    public TUser one(@RequestBody Map user) {
        Integer id = (Integer) user.get("id");
        TUser tUser = TUser.builder().id(Long.parseLong(String.valueOf(id))).build();
        return service.one(tUser);
    }

    @RequestMapping("/del")
    public int del(@RequestBody TUser user) {
        return service.del(user);
    }

    @RequestMapping("/upd")
    public int upd(@RequestBody TUser user) {
        return service.upd(user);
    }

    @RequestMapping("/all")
    public List<TUser> all() {
       return service.all();
    }
}

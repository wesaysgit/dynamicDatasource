package com.wesays.dynamic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_user")
@Data
public class TUser implements Serializable {

    @TableId("id")
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

}
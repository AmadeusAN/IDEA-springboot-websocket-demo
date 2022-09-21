package com.anhun.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

public class User {
    private int id;
    private String name;

    @NotBlank(message = "账号不能为空")
    private String account;

    @Size(min = 3, max = 20, message = "密码必须在3个字符到20个字符之间")
    private String password;

    private List<User> friendList;

    private Date lastLogin;

    private ProfilePicture picture;

    public ProfilePicture getPicture() {
        return picture;
    }

    public void setPicture(ProfilePicture picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name='" + name + '\'' + ", account='" + account + '\'' + ", password='" + password + '\'' + ", lastLogin=" + lastLogin + '}';
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

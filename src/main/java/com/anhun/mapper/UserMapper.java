package com.anhun.mapper;

import com.anhun.entity.ProfilePicture;
import com.anhun.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {
    User findUserByAccount(String account);

    User findUserById(int id);

    int insertUser(User user);

    List<User> findFirendListById(int id);

    ProfilePicture findUserProfilePictureById(int id);

    int updateUserLastLogin(int id, Date date);

    int updateUserProfilePicture(int id, byte[] img);

    List<User> findUsersbyGroupId(int id);
}

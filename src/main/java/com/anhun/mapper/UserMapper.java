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

    /**
     * <p>寻找特定id 用户的所有好友</p>
     *
     * @param id
     * @return
     */
    List<User> findFirendListById(int id);

    /**
     * 根据包含在 <code>user</code> 中的 id 值或者 name 值进行搜寻
     *
     * @param user
     * @return
     */
    List<User> findFriendsByIdOrName(User user);

    /**
     * <p>寻找是否存在该好友关系</p>
     *
     * @param idv
     * @param idw
     * @return
     */
    int findfriendshipifExist(int idv, int idw);

    ProfilePicture findUserProfilePictureById(int id);

    int updateUserLastLogin(int id, Date date);

    int updateUserProfilePicture(int id, byte[] img);

    List<User> findUsersbyGroupId(int id);
}

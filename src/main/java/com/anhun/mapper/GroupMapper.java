package com.anhun.mapper;

import com.anhun.entity.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupMapper {

    Group findGroupById(int id);

    /**
     * <p>查询用户所属的所有群组</p>
     *
     * @param id
     * @return
     */
    List<Group> findGroupByUserId(int id);

    /**
     * <p>查询某用户是否创建了同名的群组</p>
     * <p><strong>保证一个用户只能创建不同名的群组，但多个用户可以创建同名的群组</strong></p>
     *
     * @param groupName
     * @param managerId
     * @return
     */
    int findGroupIfExist(String groupName, int managerId);

    /**
     * 寻找群组人数最多的若干群组
     * <strong>暂未开启分页功能，因此暂时只查询三个</strong>
     *
     * @return
     */
    List<Group> searchPopularGrouplist();

    /**
     * <p>根据群组名称查询群组id</p>
     * <p>用于 <code>event.toId</code>保存的是管理员的 ID ，因此在添加进 <code>usergrouop</code> 数据库表之前仍需要查询出群组的ID </p>
     *
     * @param groupName
     * @return
     */
    int findGroupIdByGroupNamewithManagerId(String groupName, int managerId);

    /**
     * <p>查询用户是否在群组中</p>
     *
     * @param userId
     * @param groupId
     * @return
     */
    int findIfUserisingroup(int userId, int groupId);

    /**
     * <p>根据用户上传的字符串匹配群组名称</p>
     * @param groupName
     * @return
     */
    List<Group> findGroupsByGroupName(String groupName);

    int insertGroup(Group group);

    int insertusergroup(int userId, int groupId);

    /**
     * 更新群组头像
     *
     * @param groupId
     * @param img
     * @return
     */
    int updateGroupProfilePicture(int groupId, byte[] img);
}

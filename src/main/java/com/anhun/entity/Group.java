package com.anhun.entity;

import java.util.Arrays;

public class Group {
    private int groupId;
    private String groupName;
    private String groupDescript;
    private int managerId;
    private int memberCount;
    private byte[] groupImg;



    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", groupDescript='" + groupDescript + '\'' +
                ", managerId=" + managerId +
                ", memberCount=" + memberCount +
//                ", groupImg=" + Arrays.toString(groupImg) +
                '}';
    }

    public Group() {
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescript() {
        return groupDescript;
    }

    public void setGroupDescript(String groupDescript) {
        this.groupDescript = groupDescript;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public byte[] getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(byte[] groupImg) {
        this.groupImg = groupImg;
    }
}

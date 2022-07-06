package com.mr_w.resourceplus.model.groups;


import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;

public class GroupModel implements Serializable {

    private String groupID;
    private String Status;
    private String GroupName;
    private Bitmap GroupImage;
    private String CreatorID;
    private List<String> members;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public Bitmap getGroupImage() {
        return GroupImage;
    }

    public void setGroupImage(Bitmap groupImage) {
        GroupImage = groupImage;
    }

    public String getCreatorID() {
        return CreatorID;
    }

    public void setCreatorID(String creatorID) {
        CreatorID = creatorID;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}

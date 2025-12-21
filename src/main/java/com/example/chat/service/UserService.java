package com.example.chat.service;

import com.example.chat.common.model.User;
import com.example.chat.common.model.Group;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface UserService {
    User login(String userId, String password, WebSocketSession session);
    void logout(String userId);

    Group createGroup(String groupName, String owner, List<String> initialMembers);
    boolean setGroupAdmin(String operator, String groupId, String targetUser);
    boolean removeGroupAdmin(String operator, String groupId, String targetUser);
    boolean dissolveGroup(String groupId, String operator);
    
    // 群成员管理
    boolean joinGroup(String groupId, String userId);
    boolean leaveGroup(String groupId, String userId);
    List<Group> getUserGroups(String userId);

    void updateAvatar(String userId, String newAvatar);
    void updateUsername(String userId, String newUsername);
    boolean updateGroupName(String groupId, String operator,String newGroupName);

    // 发送好友申请
    boolean sendFriendRequest(String fromUser, String toUser);
    // 接受好友申请
    boolean acceptFriendRequest(String fromUser, String toUser);

    boolean rejectFriendRequest(String fromUser, String toUser);
    
    // 删除好友
    boolean removeFriend(String userId, String friendId);

    boolean kickUser(String adminId, String targetUserId);

    boolean muteUser(String adminId, String targetUserId, long durationMillis);

    // 群聊成员管理
    boolean muteGroupUser(String operator, String groupId, String targetUserId, long durationMillis);
    boolean kickGroupUser(String operator, String groupId, String targetUserId);

}
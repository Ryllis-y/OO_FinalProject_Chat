package com.example.chat.service.impl;

import com.example.chat.common.model.Group;
import com.example.chat.common.model.User;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User login(String userId, String password, WebSocketSession session) {
        User user = DataCenter.USERS.get(userId);

        // ===== 1. 注册 or 校验密码 =====
        if (user == null) {
            user = new User(userId);
            user.setPassword(password);

            // 钦定管理员
            if ("admin".equals(userId)) {
                user.setRole("ADMIN");
            }

            DataCenter.USERS.put(userId, user);
        } else {
            if (!user.getPassword().equals(password)) {
                throw new IllegalArgumentException("密码错误");
            }
        }

        // ===== 2. 记录新会话 =====
        // 注意：多端顶号逻辑在 LoginHandler 中统一处理，这里只负责绑定session
        DataCenter.ONLINE_USERS.put(userId, session);

        return user;
    }
    @Override
    public void logout(String userId) {
        if (userId != null) {
            DataCenter.ONLINE_USERS.remove(userId);
        }
    }

    @Override
    public Group createGroup(String groupName, String owner, List<String> initialMembers) {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("群名称不能为空");
        }
        if (owner == null || owner.isEmpty()) {
            throw new IllegalArgumentException("群主不能为空");
        }

        // 生成唯一群ID（UUID）
        String groupId = java.util.UUID.randomUUID().toString();

        Group group = new Group();
        group.setGroupId(groupId);
        group.setGroupName(groupName);
        group.setOwner(owner);

        // 初始化成员列表
        List<String> members = new CopyOnWriteArrayList<>();
        members.add(owner); // 群主必须加入成员列表
        if (initialMembers != null) {
            for (String member : initialMembers) {
                // 过滤空值、重复的用户名（简单判断）
                if (member != null && !member.isEmpty() && !members.contains(member)) {
                    members.add(member);
                }
            }
        }
        group.setMembers(members);

        // 保存到内存群组集合
        DataCenter.GROUPS.put(groupId, group);

        return group;
    }
    @Override
    public boolean updateGroupName(String groupId, String operator, String newGroupName) {
        if (groupId == null || operator == null) return false;
        if (newGroupName == null || newGroupName.trim().isEmpty()) {
            throw new IllegalArgumentException("群名称不能为空");
        }

        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) return false;

        if (!operator.equals(group.getOwner())) {
            throw new SecurityException("只有群主可以修改群名称");
        }

        group.setGroupName(newGroupName.trim());
        return true;
    }
    @Override
    public boolean setGroupAdmin(String operator, String groupId, String targetUser) {
        if (groupId == null || groupId.isEmpty()) {
            return false;
        }
        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) {
            return false;
        }
        if(!group.getOwner().equals(operator)) {
            return false;
        }
        if(!group.getMembers().contains(targetUser)) {
            return false;
        }
        group.getAdmins().add(targetUser);
        return true;
    }
    @Override
    public boolean removeGroupAdmin(String operator, String groupId, String targetUser) {
        if (groupId == null || groupId.isEmpty()) {
            return false;
        }
        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) {
            return false;
        }
        if(!group.getOwner().equals(operator)) {
            return false;
        }
        group.getAdmins().remove(targetUser);
        return true;
    }
    @Override
    public boolean dissolveGroup(String groupId, String operator) {
        if (groupId == null || operator == null) {
            return false;
        }
        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) {
            return false;
        }

        if(!operator.equals(group.getOwner())) {
            return false;
        }
        DataCenter.GROUPS.remove(groupId);
        return true;
    }

    @Override
    public void updateAvatar(String userId, String newAvatar) {
        User user = DataCenter.USERS.get(userId);
        if (user != null) {
            user.setAvatar(newAvatar);
        } else {
            throw new IllegalArgumentException("用户不存在");
        }
    }
    @Override
    public void updateUsername(String userId, String newUsername) {
        User user = DataCenter.USERS.get(userId);
        if (user != null) {
            user.setUsername(newUsername);
        } else {
            throw new IllegalArgumentException("用户不存在");
        }
    }
    @Override
    public boolean sendFriendRequest(String fromUser, String toUser) {
        if (fromUser == null || toUser == null || fromUser.equals(toUser)) return false;

        User from = DataCenter.USERS.get(fromUser);
        User to = DataCenter.USERS.get(toUser);
        if (from == null || to == null) return false;

        if (from.getFriends().contains(toUser)) {
            return false; // 已经是好友了
        }

        // 如果已经发过申请，不重复发
        if (to.getFriendRequests().contains(fromUser)) {
            return false;
        }

        to.getFriendRequests().add(fromUser);
        return true;
    }
    @Override
    public boolean acceptFriendRequest(String toUser, String fromUser) {
        if (toUser == null || fromUser == null) return false;

        User to = DataCenter.USERS.get(toUser);
        User from = DataCenter.USERS.get(fromUser);
        if (to == null || from == null) return false;

        // 确认请求存在
        if (!to.getFriendRequests().contains(fromUser)) {
            return false;
        }

        // 双向添加好友
        to.getFriends().add(fromUser);
        from.getFriends().add(toUser);

        // 移除申请
        to.getFriendRequests().remove(fromUser);
        return true;
    }
    @Override
    public boolean rejectFriendRequest(String toUser, String fromUser) {
        if (toUser == null || fromUser == null) return false;

        User to = DataCenter.USERS.get(toUser);
        if (to == null) return false;

        return to.getFriendRequests().remove(fromUser);
    }
    @Override

    public boolean kickUser(String adminId, String targetUserId) {
        User admin = DataCenter.USERS.get(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new SecurityException("无权限操作");
        }

        WebSocketSession session = DataCenter.ONLINE_USERS.get(targetUserId);
        if (session == null) return false;

        try {
            session.sendMessage(new TextMessage("你已被管理员踢下线"));
            session.close();
        } catch (Exception ignored) {}

        DataCenter.ONLINE_USERS.remove(targetUserId);
        return true;
    }
    @Override
    public boolean muteUser(String adminId, String targetUserId, long durationMillis) {
        User admin = DataCenter.USERS.get(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new SecurityException("无权限操作");
        }

        User target = DataCenter.USERS.get(targetUserId);
        if (target == null) return false;

        target.setMuteEndTime(System.currentTimeMillis() + durationMillis);
        return true;
    }

    @Override
    public boolean joinGroup(String groupId, String userId) {
        if (groupId == null || userId == null || userId.isEmpty()) {
            return false;
        }
        
        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) {
            return false;
        }
        
        // 检查用户是否已经在群中
        if (group.getMembers().contains(userId)) {
            return false; // 已经在群中
        }
        
        // 检查用户是否存在
        if (!DataCenter.USERS.containsKey(userId)) {
            return false;
        }
        
        group.getMembers().add(userId);
        return true;
    }

    @Override
    public boolean leaveGroup(String groupId, String userId) {
        if (groupId == null || userId == null || userId.isEmpty()) {
            return false;
        }
        
        Group group = DataCenter.GROUPS.get(groupId);
        if (group == null) {
            return false;
        }
        
        // 群主不能退群（需要先解散群）
        if (group.getOwner().equals(userId)) {
            return false;
        }
        
        // 移除成员
        boolean removed = group.getMembers().remove(userId);
        
        // 如果该用户是管理员，也要从管理员列表中移除
        if (removed) {
            group.getAdmins().remove(userId);
        }
        
        return removed;
    }

    @Override
    public List<Group> getUserGroups(String userId) {
        List<Group> userGroups = new java.util.ArrayList<>();
        
        if (userId == null || userId.isEmpty()) {
            return userGroups;
        }
        
        // 遍历所有群组，找到用户所在的群
        for (Group group : DataCenter.GROUPS.values()) {
            if (group.getMembers().contains(userId)) {
                userGroups.add(group);
            }
        }
        
        return userGroups;
    }

}
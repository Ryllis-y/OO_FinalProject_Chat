package com.example.chat.service.impl;

import com.example.chat.common.model.Group;
import com.example.chat.common.model.User;
import com.example.chat.repository.DataCenter;
import com.example.chat.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User login(String username, String password) {
        User user = DataCenter.USERS.get(username);

        if (user == null) {
            // 新用户注册
            user = new User(username);
            user.setPassword(password);

            // --- 核心修改: 钦定管理员 ---
            if ("admin".equals(username)) { // 这里可以改你自己喜欢的名字
                user.setRole("ADMIN");
            }
            // ------------------------

            DataCenter.USERS.put(username, user);
            return user;
        } else {
            // 老用户验密... (略)
            if (!user.getPassword().equals(password)) {
                throw new IllegalArgumentException("密码错误");
            }
            return user;
        }
    }
    @Override
    public Group createGroup(String groupName, String owner, List<String> initialMembers) {
        return null; // 暂时返回 null
    }
}
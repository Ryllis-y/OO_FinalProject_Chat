# Admin 管理员权限测试指南

## 📋 Admin 权限说明

### 权限识别机制

**代码位置：** `src/main/java/com/example/chat/service/impl/UserServiceImpl.java`

```java
// 在 login 方法中
if ("admin".equals(userId)) {
    user.setRole("ADMIN");
}
```

**说明：**
- 用户名为 `admin` 的账号**自动拥有系统管理员权限**
- 系统会自动将 `admin` 账号的 `role` 字段设置为 `"ADMIN"`
- 其他用户的 `role` 默认为 `"USER"`

### Admin 权限功能

根据代码分析，admin 系统管理员拥有以下权限：

#### ✅ 已完全实现的功能

1. ✅ **撤回任何消息** (`RECALL_MSG`)
   - 管理员可以撤回任何用户发送的消息（包括私聊和群聊）
   - 不受2分钟时间限制（代码中有管理员权限检查）
   - **前端已实现**：有撤回按钮，管理员可以撤回任何消息

2. ✅ **前端显示标识**
   - 在用户列表中显示 👑 标识
   - 用户名旁显示"管理员"标签（金色）
   - **前端已实现**：标识显示正常

#### ⚠️ 后端已实现但前端未实现的功能

3. ⚠️ **全局踢出在线用户** (`kickUser`)
   - 后端 `UserServiceImpl.kickUser()` 方法已实现
   - **但是：没有对应的 Handler 和前端UI**
   - 因此当前无法通过 WebSocket 调用此功能

4. ⚠️ **全局禁言用户** (`muteUser`)
   - 后端 `UserServiceImpl.muteUser()` 方法已实现
   - **但是：没有对应的 Handler 和前端UI**
   - 因此当前无法通过 WebSocket 调用此功能

---

## 🧪 测试步骤

### 前置准备

1. **启动服务器**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **准备测试账号**
   - 账号1：`admin` / 密码：`admin123`（或其他密码）
   - 账号2：`alice` / 密码：`123456`
   - 账号3：`bob` / 密码：`123456`

3. **准备浏览器窗口**
   - 窗口1：登录 `admin`
   - 窗口2：登录 `alice`
   - 窗口3：登录 `bob`

---

## 测试用例 1：Admin 标识显示

### 测试目的
验证 admin 账号在前端正确显示管理员标识

### 操作步骤

1. **窗口1：登录 admin**
   - 打开浏览器，访问 `http://localhost:8080`
   - 输入用户名：`admin`
   - 输入密码：`admin123`（首次登录可以是任意密码）
   - 点击"登录"

2. **观察左侧用户信息区域**
   - 查看用户名下方应该显示："管理员"（金色文字）
   - **代码位置：** `app.js` 第 387-388 行
   ```javascript
   const roleText = role === 'ADMIN' ? '管理员' : '普通用户';
   document.getElementById('currentUserRole').textContent = roleText;
   ```

3. **窗口2：登录 alice**
   - 在另一个窗口登录 `alice`

4. **在 alice 窗口查看在线用户列表**
   - 切换到"在线用户"标签
   - 找到 `admin` 用户
   - **应该看到：** `admin` 用户名旁边有 👑 标识
   - **代码位置：** `app.js` 第 1204 行
   ```javascript
   ${isAdmin ? ' <span style="color: #ffd700;">👑</span>' : ''}
   ```

### 预期结果

✅ admin 用户名旁显示 👑 标识  
✅ admin 用户信息区域显示"管理员"标签（金色）

---

## 测试用例 2：撤回任何消息（管理员权限）

### 测试目的
验证 admin 可以撤回任何用户发送的消息

### 操作步骤

1. **窗口2（alice）：发送私聊消息**
   - alice 向 bob 发送一条消息："这是一条测试消息"
   - 记录消息的发送时间（超过2分钟后测试）

2. **窗口1（admin）：撤回消息**
   - admin 进入与 alice 或 bob 的私聊
   - 或者进入包含该消息的群聊
   - 找到 alice 发送的消息
   - 鼠标悬停在消息上
   - 点击"撤回"按钮

3. **观察结果**
   - **代码位置：** `src/main/java/com/example/chat/handler/action/impl/Recall_MsgHandler.java` 第 134-138 行
   ```java
   User user = DataCenter.USERS.get(operator);
   if (user != null && user.isAdmin()) {
       return true;  // 管理员可以撤回任何消息
   }
   ```

### 预期结果

✅ admin 可以撤回任何消息（不受2分钟限制）  
✅ 消息从所有用户的界面中消失  
✅ 显示"消息已撤回"提示

### 对比测试：普通用户撤回

1. **窗口2（alice）：尝试撤回自己的消息**
   - 发送消息后立即撤回：✅ 成功
   - 发送消息后等待超过2分钟再撤回：❌ 失败，提示"消息已超过2分钟，无法撤回"

2. **窗口2（alice）：尝试撤回其他人的消息**
   - ❌ 失败，无撤回按钮或提示"无权撤回"

---

## 测试用例 3：全局踢出在线用户

### 测试目的
验证 admin 可以踢出任何在线用户

### 操作步骤

1. **窗口2（alice）：保持在线**
   - alice 保持登录状态
   - 可以发送一些消息证明在线

2. **窗口1（admin）：查看在线用户**
   - 切换到"在线用户"标签
   - 确认可以看到 `alice` 在线

3. **窗口1（admin）：踢出 alice**
   - 注意：**当前前端可能没有实现踢出用户的UI按钮**
   - 需要通过**后端直接调用**或**开发者工具**测试
   
   **方法A：通过开发者工具（Console）**
   ```javascript
   // 在 admin 窗口的浏览器控制台中执行
   app.send({
       action: 'KICK_USER',  // 注意：需要确认是否有这个action
       params: {
           targetUser: 'alice'
       }
   });
   ```

   **方法B：直接调用后端方法测试**
   - 代码位置：`src/main/java/com/example/chat/service/impl/UserServiceImpl.java` 第 246-262 行
   ```java
   public boolean kickUser(String adminId, String targetUserId) {
       User admin = DataCenter.USERS.get(adminId);
       if (admin == null || !admin.isAdmin()) {
           throw new SecurityException("无权限操作");
       }
       // ... 踢出逻辑
   }
   ```

4. **观察窗口2（alice）**
   - alice 应该收到提示："你已被管理员踢下线"
   - WebSocket 连接被关闭
   - 页面可能显示"连接断开"

### ⚠️ 重要说明

**当前状态：此功能未完全实现**

- 后端 `UserServiceImpl.kickUser()` 方法已实现
- **但是：没有对应的 Handler（如 `Kick_UserHandler.java`）**
- 因此无法通过 WebSocket Action 调用
- 前端也没有相应的UI按钮

**如果要实现此功能，需要：**
1. 创建 `Kick_UserHandler.java` 实现 `BaseActionHandler`
2. 在前端在线用户列表中添加管理按钮（仅admin可见）
3. 添加前端调用逻辑

### 预期结果（如果实现后）

✅ admin 可以踢出任何在线用户  
✅ 被踢用户收到提示并断开连接  
✅ 被踢用户从在线用户列表中移除

---

## 测试用例 4：全局禁言用户

### 测试目的
验证 admin 可以全局禁言任何用户

### 操作步骤

1. **窗口2（alice）：正常发送消息**
   - alice 向 bob 发送消息："测试消息1"
   - 确认消息正常发送

2. **窗口1（admin）：禁言 alice**
   - 注意：**当前前端可能没有实现全局禁言的UI按钮**
   - 需要通过**开发者工具**或**后端直接调用**测试
   
   **方法A：通过开发者工具（Console）**
   ```javascript
   // 在 admin 窗口的浏览器控制台中执行
   app.send({
       action: 'MUTE_USER',  // 注意：需要确认是否有这个action
       params: {
           targetUser: 'alice',
           duration: 300000  // 5分钟（毫秒）
       }
   });
   ```

   **方法B：直接调用后端方法测试**
   - 代码位置：`src/main/java/com/example/chat/service/impl/UserServiceImpl.java` 第 268-279 行
   ```java
   public boolean muteUser(String adminId, String targetUserId, long durationMillis) {
       User admin = DataCenter.USERS.get(adminId);
       if (admin == null || !admin.isAdmin()) {
           throw new SecurityException("无权限操作");
       }
       target.setMuteEndTime(System.currentTimeMillis() + durationMillis);
       return true;
   }
   ```

3. **窗口2（alice）：尝试发送消息**
   - alice 尝试发送消息："测试消息2"
   - 应该失败或被过滤

4. **观察结果**
   - 输入框可能被禁用
   - 显示提示："您已被管理员禁言，剩余 X 分 X 秒解除"
   - 或者消息无法发送

### ⚠️ 重要说明

**当前状态：此功能未完全实现**

- 后端 `UserServiceImpl.muteUser()` 方法已实现
- **但是：没有对应的 Handler（如 `Mute_UserHandler.java`）**
- 因此无法通过 WebSocket Action 调用
- 前端也没有相应的UI按钮

**如果要实现此功能，需要：**
1. 创建 `Mute_UserHandler.java` 实现 `BaseActionHandler`
2. 在前端在线用户列表中添加禁言按钮（仅admin可见）
3. 添加前端调用逻辑和禁言状态显示

### 预期结果（如果实现后）

✅ admin 可以全局禁言任何用户  
✅ 被禁言用户无法发送消息  
✅ 显示禁言剩余时间

---

## 测试用例 5：Admin 与群主/群管理员权限对比

### 测试目的
验证 admin 系统管理员权限与群主/群管理员权限的区别

### 权限对比表

| 功能 | 系统管理员(admin) | 群主 | 群管理员 |
|------|------------------|------|----------|
| 撤回任何消息 | ✅ | ✅（仅群内） | ❌ |
| 全局踢出用户 | ✅ | ❌ | ❌ |
| 全局禁言用户 | ✅ | ❌ | ❌ |
| 群内踢人 | ❌（无此功能） | ✅ | ✅ |
| 群内禁言 | ❌（无此功能） | ✅ | ✅ |
| 设置群管理员 | ❌（无此功能） | ✅ | ❌ |
| 解散群组 | ❌（无此功能） | ✅ | ❌ |

### 操作步骤

1. **创建群组**
   - alice 创建一个群组"测试群"
   - alice 自动成为群主

2. **设置群管理员**
   - alice 将 bob 设为群管理员

3. **测试权限区别**

   **测试1：撤回消息权限**
   - admin 可以撤回群内任何消息 ✅
   - alice（群主）可以撤回群内任何消息 ✅
   - bob（群管理员）只能撤回自己发送的消息 ❌

   **测试2：群内管理权限**
   - admin **不能**在群内踢人或禁言（没有这个功能）❌
   - alice（群主）可以在群内踢人和禁言 ✅
   - bob（群管理员）可以在群内踢人和禁言普通成员 ✅

---

## 📝 注意事项

### 1. 前端UI实际情况

**当前已实现：**
- ✅ Admin 标识显示（👑 和"管理员"标签）
- ✅ 撤回任何消息功能（已实现，前端有撤回按钮）

**当前未实现：**
- ❌ **全局踢人功能**：后端有 `kickUser` 方法，但**没有对应的 Handler 和前端UI**
- ❌ **全局禁言功能**：后端有 `muteUser` 方法，但**没有对应的 Handler 和前端UI**

**说明：**
- 后端的 `UserService.kickUser()` 和 `UserService.muteUser()` 方法已实现
- 但是**没有对应的 ActionHandler**（如 `Kick_UserHandler.java` 或 `Mute_UserHandler.java`）
- 因此这些功能目前**无法通过 WebSocket 调用**
- 前端也没有相应的UI按钮

**如果前端没有对应的UI按钮：**

可以通过以下方式测试：

1. **浏览器开发者工具（Console）**
   ```javascript
   // 发送 WebSocket 消息
   app.send({
       action: 'ACTION_NAME',
       params: {
           // 参数
       }
   });
   ```

2. **直接测试后端方法**
   - 编写单元测试
   - 或通过 IDE 的调试功能直接调用方法

### 2. 代码位置总结

| 功能 | 代码位置 |
|------|---------|
| Admin 权限识别 | `UserServiceImpl.java:26-28` |
| Admin 标识显示 | `app.js:387-388, 1204` |
| 撤回消息权限检查 | `Recall_MsgHandler.java:134-138` |
| 全局踢人 | `UserServiceImpl.java:246-262` |
| 全局禁言 | `UserServiceImpl.java:268-279` |

### 3. 测试建议

1. **重点测试：**
   - ✅ Admin 标识显示（最容易验证）
   - ✅ 撤回任何消息功能（前端已实现）

2. **可选测试：**
   - ❓ 全局踢人（如果后端实现了对应的Handler）
   - ❓ 全局禁言（如果后端实现了对应的Handler）

3. **如果功能未完全实现：**
   - ✅ **撤回任何消息**：已完全实现，可以正常测试
   - ⚠️ **全局踢人和禁言**：后端方法已实现，但缺少 Handler 和前端UI
   - 可以说明这是**系统管理员权限的预留接口**
   - 展示代码中的权限检查逻辑（`UserServiceImpl.kickUser()` 和 `muteUser()` 方法）
   - 说明如果要完全实现，需要创建对应的 Handler 和前端UI

---

## ✅ 快速测试清单

### 当前可以实现的功能

- [x] Admin 登录后显示"管理员"标签
- [x] 在线用户列表中 admin 显示 👑 标识
- [x] Admin 可以撤回任何消息（不受2分钟限制）
- [x] 普通用户只能撤回自己的消息（2分钟内）
- [x] Admin 无法在群内踢人/禁言（这是群主/群管理员的功能）

### 当前未实现的功能（后端有方法但无Handler和前端UI）

- [ ] Admin 全局踢出用户（需要创建 Handler 和前端UI）
- [ ] Admin 全局禁言用户（需要创建 Handler 和前端UI）

**注意：** 这两项功能的后端逻辑已实现，但缺少 Handler 和前端UI，因此当前无法测试。

---

**测试完成后，请记录测试结果和发现的问题！** 🎯


# WebSocket JSON 测试用例

在 http://www.jsons.cn/websocket/ 测试时，直接复制下面的 JSON 发送即可。

**连接地址**: `ws://localhost:8080/chat`

---

## 📝 测试步骤

1. 在网站上输入连接地址：`ws://localhost:8080/chat`
2. 点击连接
3. 按照下面的顺序依次发送 JSON 消息（按 Ctrl+A 全选，Ctrl+C 复制，粘贴到发送框）

---

## 1️⃣ 用户登录（必先执行）

### 新用户注册（首次登录）
```json
{
  "action": "LOGIN",
  "params": {
    "username": "alice",
    "password": "123456"
  }
}
```

### 已存在用户登录
```json
{
  "action": "LOGIN",
  "params": {
    "username": "alice",
    "password": "123456"
  }
}
```

### 管理员登录
```json
{
  "action": "LOGIN",
  "params": {
    "username": "admin",
    "password": "admin123"
  }
}
```

**预期响应**:
```json
{
  "type": "LOGIN_RESP",
  "code": 200,
  "msg": "ok",
  "data": {
    "userId": "...",
    "username": "alice",
    "role": "USER",
    ...
  }
}
```

---

## 2️⃣ 群聊相关功能

### 创建群组
```json
{
  "action": "CREATE_GROUP",
  "params": {
    "groupName": "测试群组",
    "initialMembers": ["bob", "charlie"]
  }
}
```

**预期响应**:
```json
{
  "type": "GROUP_CREATED",
  "code": 200,
  "msg": "ok",
  "data": {
    "groupId": "...",
    "groupName": "测试群组",
    "owner": "alice",
    "members": ["alice", "bob", "charlie"]
  }
}
```

### 加入群组
```json
{
  "action": "JOIN_GROUP",
  "params": {
    "groupId": "替换为实际的群组ID"
  }
}
```

### 退出群组
```json
{
  "action": "LEAVE_GROUP",
  "params": {
    "groupId": "替换为实际的群组ID"
  }
}
```

### 发送群聊消息
```json
{
  "action": "SEND_GROUP",
  "params": {
    "groupId": "替换为实际的群组ID",
    "content": "大家好！",
    "atUsers": ["bob"]
  }
}
```

### 获取用户群组列表
```json
{
  "action": "GET_GROUPS",
  "params": {}
}
```

### 获取群成员列表
```json
{
  "action": "GET_GROUP_MEMBERS",
  "params": {
    "groupId": "替换为实际的群组ID"
  }
}
```

---

## 3️⃣ 发送私聊消息

**前提**: 需要两个用户都已登录（在两个不同的浏览器标签页或设备上）

### 基础私聊消息
```json
{
  "action": "SEND_PRIVATE",
  "params": {
    "targetUser": "bob",
    "content": "Hello Bob, how are you?"
  }
}
```

### 带 @ 提醒的私聊消息
```json
{
  "action": "SEND_PRIVATE",
  "params": {
    "targetUser": "bob",
    "content": "Hey @bob, are you there?",
    "atUsers": ["bob"]
  }
}
```

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "msgId": "...",
    "fromUser": "alice",
    "toUser": "bob",
    "content": "Hello Bob, how are you?",
    "timestamp": 1234567890123,
    ...
  }
}
```

---

## 3️⃣ 获取在线用户列表

```json
{
  "action": "GET_ONLINE",
  "params": {}
}
```

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "users": ["alice", "bob", "admin"]
  }
}
```

---

## 4️⃣ 消息撤回

**前提**: 先发送一条消息，获取 msgId，然后在 2 分钟内撤回

```json
{
  "action": "RECALL_MSG",
  "params": {
    "msgId": "替换为实际的消息ID"
  }
}
```

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "msgId": "...",
    "recalledBy": "alice"
  }
}
```

---

## 5️⃣ 标记消息已读

**前提**: 先收到一条消息，获取其 msgId

```json
{
  "action": "MSG_READ",
  "params": {
    "msgId": "替换为实际的消息ID"
  }
}
```

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "msgId": "...",
    "readBy": "bob"
  }
}
```

---

## 6️⃣ 消息反应（点赞/点踩等）

**前提**: 先收到一条消息，获取其 msgId

### 点赞
```json
{
  "action": "MSG_REACT",
  "params": {
    "msgId": "替换为实际的消息ID",
    "reactType": "like"
  }
}
```

### 点踩
```json
{
  "action": "MSG_REACT",
  "params": {
    "msgId": "替换为实际的消息ID",
    "reactType": "dislike"
  }
}
```

### 其他反应类型
```json
{
  "action": "MSG_REACT",
  "params": {
    "msgId": "替换为实际的消息ID",
    "reactType": "heart"
  }
}
```

支持的反应类型: `like`, `dislike`, `heart`, `laugh`, `sad`, `angry`

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "msgId": "...",
    "reactType": "like",
    "isAdd": true,
    "count": 1
  }
}
```

---

## 7️⃣ 获取历史消息

### 获取与特定用户的聊天历史
```json
{
  "action": "GET_HISTORY",
  "params": {
    "targetUser": "bob",
    "limit": 20
  }
}
```

### 分页获取（获取更早的消息）
```json
{
  "action": "GET_HISTORY",
  "params": {
    "targetUser": "bob",
    "limit": 20,
    "before": "替换为上一页最后一条消息的msgId"
  }
}
```

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "messages": [
      {
        "msgId": "...",
        "fromUser": "alice",
        "toUser": "bob",
        "content": "...",
        "timestamp": 1234567890123
      },
      ...
    ],
    "hasMore": true,
    "nextCursor": "..."
  }
}
```

---

## 8️⃣ 管理员功能

### 踢出用户
```json
{
  "action": "KICK_USER",
  "params": {
    "targetUser": "bob"
  }
}
```

**前提**: 当前用户必须是 `admin`

### 禁言用户
```json
{
  "action": "MUTE_USER",
  "params": {
    "targetUser": "bob",
    "duration": 3600000
  }
}
```

`duration` 单位是毫秒，上面的例子是 1 小时（3600000 毫秒）

**预期响应**:
```json
{
  "type": "SUCCESS",
  "code": 200,
  "msg": "ok",
  "data": {
    "targetUser": "bob",
    "muteEndTime": 1234567890123
  }
}
```

---

## 9️⃣ 心跳包

```json
{
  "action": "HEARTBEAT",
  "params": {}
}
```

**预期响应**: 通常没有响应（服务端不做处理，只是为了保持连接）

---

## ❌ 错误测试用例

### 测试错误密码
```json
{
  "action": "LOGIN",
  "params": {
    "username": "alice",
    "password": "wrong_password"
  }
}
```

**预期响应**:
```json
{
  "type": "ERROR",
  "code": 500,
  "msg": "密码错误"
}
```

### 测试未登录发送消息
```json
{
  "action": "SEND_PRIVATE",
  "params": {
    "targetUser": "bob",
    "content": "This should fail"
  }
}
```

**预期响应**:
```json
{
  "type": "ERROR",
  "code": 500,
  "msg": "请先登录"
}
```

### 测试目标用户不在线
```json
{
  "action": "SEND_PRIVATE",
  "params": {
    "targetUser": "nonexistent",
    "content": "This should fail"
  }
}
```

**预期响应**:
```json
{
  "type": "ERROR",
  "code": 500,
  "msg": "用户 nonexistent 不在线"
}
```

### 测试参数错误
```json
{
  "action": "SEND_PRIVATE",
  "params": {
    "content": "Missing targetUser"
  }
}
```

**预期响应**:
```json
{
  "type": "ERROR",
  "code": 500,
  "msg": "参数错误：缺少 targetUser 或 content"
}
```

---

## 🎯 完整测试流程示例

### 流程 1: 基础聊天流程

1. **用户 A 登录**
   ```json
   {"action": "LOGIN", "params": {"username": "alice", "password": "123456"}}
   ```

2. **用户 B 登录**（在另一个标签页或设备）
   ```json
   {"action": "LOGIN", "params": {"username": "bob", "password": "123456"}}
   ```

3. **用户 A 发送消息给 B**
   ```json
   {"action": "SEND_PRIVATE", "params": {"targetUser": "bob", "content": "Hello Bob!"}}
   ```

4. **用户 B 标记已读**（使用上一步返回的 msgId）
   ```json
   {"action": "MSG_READ", "params": {"msgId": "替换为实际的msgId"}}
   ```

5. **用户 B 回复**
   ```json
   {"action": "SEND_PRIVATE", "params": {"targetUser": "alice", "content": "Hi Alice!"}}
   ```

### 流程 2: 管理员测试流程

1. **管理员登录**
   ```json
   {"action": "LOGIN", "params": {"username": "admin", "password": "admin123"}}
   ```

2. **普通用户登录**（在另一个标签页）
   ```json
   {"action": "LOGIN", "params": {"username": "bob", "password": "123456"}}
   ```

3. **管理员禁言用户**
   ```json
   {"action": "MUTE_USER", "params": {"targetUser": "bob", "duration": 3600000}}
   ```

4. **管理员踢出用户**
   ```json
   {"action": "KICK_USER", "params": {"targetUser": "bob"}}
   ```

---

## 💡 使用提示

1. **连接地址**: `ws://localhost:8080/chat`
   - 如果服务器在其他机器，改为：`ws://服务器IP:8080/chat`

2. **JSON 格式**: 确保 JSON 格式正确，不要有多余的逗号

3. **消息顺序**: 
   - 必须先登录才能发送消息
   - 两个用户都要登录才能私聊

4. **msgId 替换**: 
   - 所有需要 `msgId` 的操作，都要先发送/收到消息，获取实际的 msgId
   - 在响应中查找 `data.msgId` 字段

5. **多用户测试**: 
   - 建议打开两个浏览器标签页，分别登录不同用户
   - 或使用多个设备同时测试

---

## 🔍 常见问题

**Q: 为什么发送消息后没有收到响应？**
A: 检查是否已登录，以及目标用户是否在线。

**Q: 为什么撤回失败？**
A: 可能超过了 2 分钟的时间限制，或者消息不存在。

**Q: 为什么管理员功能失败？**
A: 确保当前登录的用户名是 `admin`（注意大小写）。

**Q: 如何获取 msgId？**
A: 发送消息成功后，响应中的 `data.msgId` 字段就是消息ID。




# 聊天室系统 - 开发文档 (v2.0)

**版本**: 2.0
**适用端**: Java 后端 + 前端 (Web/App)
**核心技术**: Spring Boot WebSocket, ThreadPool, In-Memory Data Structure (ConcurrentHashMap)

---

## 1. 项目功能概览 (Features)

本项目是一个不依赖数据库、基于纯内存存储的高性能即时通讯系统。

### 1.1 用户与权限模块
*   **注册与登录**: 无号自动注册，有号验证密码（密码加密存储，传输时不泄露）。
*   **多端顶号**: 同一账号在异地登录，旧连接会被强制踢下线。
*   **在线状态**: 实时查看在线用户列表。
*   **管理员权限 (RBAC)**:
    *   用户名为 `admin` 的账号自动拥有管理员权限。
    *   管理员拥有 **金名牌标识**。
    *   管理员可以 **强制踢人**。
    *   管理员可以 **禁言用户**（指定时长）。

### 1.2 核心聊天模块
*   **消息类型**: 支持单聊、群聊。
*   **消息路由**: 基于线程池的异步并行广播，支持高并发。
*   **历史漫游**:
    *   登录成功后，自动推送最近 20 条未读/群消息。
    *   支持 **游标翻页** (`GET_HISTORY`) 获取更早的消息。
*   **消息撤回**: 支持 2 分钟内的消息撤回，并在所有端同步删除。

### 1.3 高级交互体验
*   **强提醒 (@Mention)**: 发送消息支持 @某人，后端解析并透传。
*   **状态反馈**:
    *   **已读回执**: 发送者可以看到谁读了自己的消息。
    *   **正在输入**: 对方打字时，顶部显示 "对方正在输入..."。
*   **情感互动**: 支持对消息进行 **点赞 / 点踩**，实时更新计数。

---

## 2. API 接口文档 (WebSocket 协议)

### 2.1 连接配置
*   **URL**: `ws://{server_ip}:8080/chat`
*   **格式**: 全程 JSON 交互。
*   **时间戳**: 所有时间字段均为 13 位毫秒级 Unix Timestamp (`Long`)。

### 2.2 数据包基础结构

**客户端发送 (Request):**
```json
{
  "action": "指令类型 (String)",
  "params": { ... }
}
```

**服务端响应 (Response):**
```json
{
  "type": "事件类型 (String)",
  "code": 200,
  "msg": "ok",
  "data": { ... }
}
```

---

### 3. 客户端指令集 (Request)

#### 3.1 基础指令

| Action | 参数 Params | 说明 |
| :--- | :--- | :--- |
| **LOGIN** | `{ "username": "Tom", "password": "123" }` | 无号自动注册；有号校验密码；admin账号自动获得管理员权限。 |
| **LOGOUT** | `{}` | 主动下线，断开连接。 |
| **GET_ONLINE** | `{}` | 获取在线用户列表（包含角色信息）。 |
| **HEARTBEAT** | `{}` | 心跳包，建议每 30s 发送一次。 |

#### 3.2 消息发送

**发送私聊 (SEND_PRIVATE) / 群聊 (SEND_GROUP)**
```json
{
  "action": "SEND_GROUP", // 或 SEND_PRIVATE
  "params": {
    "targetUser": "Group1", // 接收者ID 或 群ID
    "content": "Hello @Jerry",
    "type": "text",
    "atUsers": ["Jerry"]    // [新增] 被 @ 的用户列表
  }
}
```

#### 3.3 历史记录与状态

**获取历史消息 (GET_HISTORY)** - 支持翻页
```json
{
  "action": "GET_HISTORY",
  "params": {
    "beforeTime": 1710000000000 // 获取这个时间点之前的 20 条。首次加载不传此字段。
  }
}
```

**发送已读回执 (MSG_READ)**
```json
{
  "action": "MSG_READ",
  "params": { "msgId": "msg-uuid-123" }
}
```

**正在输入 (TYPING_START)**
```json
{
  "action": "TYPING_START",
  "params": { "targetUser": "Jerry" }
}
```

**消息点赞/点踩 (MSG_REACT)**
```json
{
  "action": "MSG_REACT",
  "params": {
    "msgId": "msg-uuid-123",
    "reactType": "like" // 或 "dislike"、"heart" 等
  }
}
```

**撤回消息 (RECALL_MSG)**
```json
{
  "action": "RECALL_MSG",
  "params": { "msgId": "msg-uuid-123" }
}
```

#### 3.4 管理员指令 (权限不足会报错)

**强制踢人 (KICK_USER)**
```json
{
  "action": "KICK_USER",
  "params": { "targetUser": "BadGuy" }
}
```

**禁言用户 (MUTE_USER)**
```json
{
  "action": "MUTE_USER",
  "params": {
    "targetUser": "BadGuy",
    "duration": 60 // 禁言时长(秒)
  }
}
```

---

### 4. 服务端事件集 (Response / Event)

前端需监听 `onmessage` 并根据 `type` 分发处理。

#### 4.1 核心业务事件

**登录成功 (LOGIN_RESP)**
```json
{
  "type": "LOGIN_RESP",
  "data": {
    "username": "admin",
    "role": "ADMIN",      // [新增] 身份标识
    "avatar": "...",
    "muteEndTime": 0
    // 注意：password 字段已被 @JsonIgnore 隐藏
  }
}
```

**收到新消息 (EVENT_CHAT_MSG)**
```json
{
  "type": "EVENT_CHAT_MSG",
  "data": {
    "msgId": "uuid-gen-by-server",
    "fromUser": "Tom",
    "toUser": "Group1",
    "isGroup": true,
    "content": "Hello",
    "timestamp": 1710000000000,
    "atUsers": ["Jerry"],        // [新增] 需要高亮显示
    "reactions": {},             // [新增] 初始为空
    "readBy": []                 // [新增] 初始为空
  }
}
```

**在线列表更新 (ONLINE_LIST)**
```json
{
  "type": "ONLINE_LIST",
  "data": [
    { "username": "admin", "role": "ADMIN" }, // 前端可根据 role 渲染徽章/踢人按钮
    { "username": "Tom", "role": "USER" }
  ]
}
```

#### 4.2 交互反馈事件

**对方已读通知 (EVENT_MSG_READ)**
```json
{
  "type": "EVENT_MSG_READ",
  "data": {
    "msgId": "uuid-123",
    "reader": "Jerry",
    "readCount": 5
  }
}
```

**点赞状态更新 (EVENT_MSG_REACT)**
```json
{
  "type": "EVENT_MSG_REACT",
  "data": {
    "msgId": "uuid-123",
    "reactType": "like",
    "operator": "Jerry",
    "isAdd": true,   // true=新增点赞, false=取消点赞
    "count": 10      // 当前总数
  }
}
```

**对方正在输入 (EVENT_TYPING)**
```json
{
  "type": "EVENT_TYPING",
  "data": "Tom" // 谁正在输入
}
```

**消息撤回通知 (EVENT_MSG_RECALLED)**
```json
{
  "type": "EVENT_MSG_RECALLED",
  "data": { "recalledMsgId": "uuid-123", "operator": "Tom" }
}
```

#### 4.3 系统与错误

**系统通知 (SYS_NOTICE)**
*   用于管理员操作反馈，如 "已踢出用户 xxx"。

**错误提示 (ERROR)**
*   如 "权限不足"、"您处于禁言状态"、"密码错误"。

---

## 5. 前端开发注意事项

1.  **管理员 UI**: 请根据 `GET_ONLINE` 返回的 `role === 'ADMIN'` 字段，决定是否显示【踢人/禁言】按钮。
2.  **安全性**: 切勿在前端保存用户的明文密码。
3.  **时间处理**: 所有 `timestamp` 建议转换为 `HH:mm` 格式显示。
4.  **已读逻辑**: 当一条消息进入屏幕可视区域时，才发送 `MSG_READ`，不要一股脑全发。
5.  **数据去重**: 后端 `HISTORY_LIST` 推送的消息可能与本地已有的重复（例如网络波动），前端建议根据 `msgId` 做去重处理。

---

**这份文档代表了你项目的最终形态。你可以自信地拿着它去进行后续的开发和展示！**
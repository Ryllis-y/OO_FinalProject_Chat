// WebSocket连接管理
class ChatApp {
    constructor() {
        this.ws = null;
        this.currentUser = null;
        this.currentTarget = null; // 当前聊天对象 {type: 'private'|'group', id: 'userId/groupId'}
        this.contacts = new Map(); // 联系人列表 {userId: {name, lastMsg, timestamp}}
        this.groups = new Map(); // 群组列表 {groupId: {name, lastMsg, timestamp}}
        this.onlineUsers = new Map(); // 在线用户列表
        this.messages = new Map(); // 消息历史 {targetId: [messages]}
        this.heartbeatInterval = null;
        this.muteCheckInterval = null; // 禁言状态检查定时器
        this.atMentions = new Map(); // 被@提醒 {groupId: Set<msgId>}
        this.groupMembers = new Map(); // 群成员缓存 {groupId: {members, owner, admins}}
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.switchTab('contacts');
    }

    // 设置事件监听
    setupEventListeners() {
        // 标签切换
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const tab = e.target.dataset.tab;
                this.switchTab(tab);
            });
        });

        // 搜索
        document.getElementById('searchInput').addEventListener('input', (e) => {
            this.filterContacts(e.target.value);
        });

        // 发送消息
        document.getElementById('sendBtn').addEventListener('click', () => this.sendMessage());
        document.getElementById('messageInput').addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // 创建群组
        document.getElementById('createGroupBtn').addEventListener('click', () => {
            document.getElementById('createGroupModal').classList.remove('hidden');
        });

        // 退出登录
        document.getElementById('logoutBtn').addEventListener('click', () => {
            if (confirm('确定要退出登录吗？')) {
                this.logout();
            }
        });

        // 查看群成员
        document.getElementById('viewMembersBtn').addEventListener('click', () => {
            if (this.currentTarget && this.currentTarget.type === 'group') {
                this.getGroupMembers(this.currentTarget.id);
            }
        });

        // 关闭信息面板
        document.getElementById('closeInfoBtn').addEventListener('click', () => {
            document.getElementById('infoPanel').classList.add('hidden');
        });

        // @按钮（群聊中显示）
        document.getElementById('atBtn').addEventListener('click', () => {
            if (this.currentTarget && this.currentTarget.type === 'group') {
                this.showAtMemberList(this.currentTarget.id);
            }
        });

        // 删除好友按钮
        document.getElementById('deleteFriendBtn').addEventListener('click', () => {
            if (this.currentTarget && this.currentTarget.type === 'private') {
                this.deleteFriend(this.currentTarget.id);
            }
        });
    }

    // 连接WebSocket
    connect() {
        const wsUrl = 'ws://localhost:8080/chat';
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('WebSocket连接已建立');
            // 如果已经登录，连接建立后加载初始数据
            // 否则等待登录响应
        };

        this.ws.onmessage = (event) => {
            try {
                const response = JSON.parse(event.data);
                this.handleServerMessage(response);
            } catch (e) {
                console.error('解析消息失败:', e);
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket错误:', error);
            this.showError('连接错误，请刷新页面重试');
        };

        this.ws.onclose = () => {
            console.log('WebSocket连接已关闭');
            if (this.currentUser) {
                this.showError('连接已断开，正在重连...');
                setTimeout(() => this.connect(), 3000);
            }
        };

        // 启动心跳
        this.startHeartbeat();
    }

    // 启动心跳
    startHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
        }
        this.heartbeatInterval = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.send({
                    action: 'HEARTBEAT',
                    params: {}
                });
            }
        }, 30000); // 每30秒发送一次心跳
    }

    // 发送消息到服务器
    send(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(data));
        } else {
            console.error('WebSocket未连接');
            this.showError('连接已断开，请刷新页面');
        }
    }

    // 处理服务器消息
    handleServerMessage(response) {
        console.log('收到服务器消息:', response);

        switch (response.type) {
            case 'LOGIN_RESP':
                this.handleLoginResponse(response);
                break;
            case 'EVENT_CHAT_MSG':
                console.log('收到 EVENT_CHAT_MSG 事件:', response);
                console.log('消息数据:', response.data);
                this.handleChatMessage(response.data);
                break;
            case 'EVENT_MSG_RECALLED':
                this.handleMessageRecalled(response.data);
                break;
            case 'EVENT_MSG_REACT':
                this.handleMessageReaction(response.data);
                break;
            case 'ERROR':
                this.showError(response.msg || '操作失败');
                break;
            case 'SYS_NOTICE':
                if (response.code === 400 && response.msg.includes('强制下线')) {
                    alert('您的账号在另一地点登录，您已被强制下线');
                    this.logout();
                } else {
                    this.showError(response.msg || '系统通知');
                }
                break;
            case 'GROUP_CREATED':
                this.handleGroupCreated(response.data);
                break;
            case 'GROUP_JOINED':
                this.handleGroupJoined(response.data);
                break;
            case 'GROUP_UPDATED':
                this.handleGroupUpdated(response.data);
                break;
            case 'GROUP_DISSOLVED':
                this.handleGroupDissolved(response.data);
                break;
            case 'GROUP_KICKED':
                this.handleGroupKicked(response.data);
                break;
            case 'GROUP_MUTE_NOTICE':
                this.handleGroupMuteNotice(response.data);
                break;
            case 'GROUP_KICK_NOTICE':
                this.handleGroupKickNotice(response.data);
                break;
            case 'FRIEND_REQUEST_ACCEPTED':
                // 处理好友申请被接受的通知（申请方收到）
                this.handleFriendRequestAccepted(response.data);
                break;
            case 'FRIEND_REQUEST_NOTICE':
                // 处理新好友申请通知
                this.handleFriendRequestNotice(response.data);
                break;
            case 'FRIEND_REMOVED':
                // 处理好友关系已解除的通知
                this.handleFriendRemoved(response.data);
                break;
            case 'FRIEND_LIST_UPDATED':
                // 处理好友列表已更新的通知（删除好友后触发）
                this.getFriends();
                break;
            case 'ONLINE_LIST':
                // 在线用户列表响应，data是数组
                if (Array.isArray(response.data)) {
                    this.handleOnlineUsers({users: response.data});
                }
                break;
            case 'SUCCESS':
                // 处理成功响应，可能是历史消息、群组列表或其他操作
                if (response.data && response.data.groups) {
                    // 群组列表响应
                    this.handleGroupsList(response.data);
                } else if (response.data && response.data.friends) {
                    // 好友列表响应
                    this.handleFriendsList(response.data);
                } else if (response.data && response.data.messages) {
                    // 历史消息响应
                    this.handleHistory(response.data);
                } else if (response.data && response.data.friendRequests) {
                    // 好友申请列表响应
                    this.handleFriendRequestsList(response.data);
                } else if (response.data && Array.isArray(response.data)) {
                    // 兼容直接返回数组的情况（如在线用户列表）
                    this.handleOnlineUsers({users: response.data});
                } else if (response.data && response.data.message && typeof response.data.message === 'string' && 
                           (response.data.message.includes('接受') || response.data.message.includes('拒绝')) &&
                           response.data.fromUser) {
                    // 接受/拒绝好友申请响应
                    this.showSuccess(response.data.message || '操作成功');
                    // 刷新好友申请列表
                    setTimeout(() => {
                        this.send({
                            action: 'GET_FRIEND_REQUESTS',
                            params: {}
                        });
                    }, 200);
                    // 如果接受了申请，刷新好友列表
                    if (response.data.message.includes('接受')) {
                        setTimeout(() => {
                            this.getFriends();
                        }, 300);
                    }
                } else if (response.data && response.data.groupId && response.data.message) {
                    if (response.data.message.includes('退出群组')) {
                        // 退出群组成功响应
                        this.handleLeaveGroupSuccess(response.data);
                    } else if (response.data.message.includes('解散')) {
                        // 解散群组成功响应
                        this.handleDissolveGroupSuccess(response.data);
                    } else if (response.data.message.includes('踢出')) {
                        // 踢出群成员成功响应
                        this.handleKickGroupUserSuccess(response.data);
                    } else if (response.data.message && typeof response.data.message === 'string' && response.data.message.includes('禁言')) {
                        // 禁言成功响应
                        this.showSuccess(response.data.message || response.msg || '禁言成功');
                        // 如果被禁言的是当前用户，更新禁言状态
                        if (response.data.targetUser && this.currentUser && 
                            (this.currentUser.userId === response.data.targetUser || this.currentUser.username === response.data.targetUser)) {
                            // 计算禁言结束时间（durationMinutes * 60 * 1000 毫秒）
                            const durationMinutes = response.data.durationMinutes || 10;
                            this.currentUser.muteEndTime = Date.now() + (durationMinutes * 60 * 1000);
                            this.startMuteCheck();
                            this.updateMuteStatus();
                        }
                        // 如果是当前群组，刷新成员列表
                        if (response.data.groupId && this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === response.data.groupId) {
                            // 延迟刷新，确保后端已经更新完成
                            setTimeout(() => {
                                this.getGroupMembers(response.data.groupId);
                            }, 200);
                        }
                    } else if (response.data && response.data.friendId && 
                               response.data.message && typeof response.data.message === 'string' && 
                               response.data.message.includes('删除好友')) {
                        // 删除好友成功响应
                        const friendId = response.data.friendId;
                        this.showSuccess(response.data.message || '已删除好友');
                        
                        // 立即从联系人列表中移除
                        if (friendId && this.contacts.has(friendId)) {
                            this.contacts.delete(friendId);
                        }
                        
                        // 清除该好友的消息历史
                        if (friendId && this.messages.has(friendId)) {
                            this.messages.delete(friendId);
                        }
                        
                        // 如果当前正在与该好友聊天，切换到初始状态
                        if (this.currentTarget && this.currentTarget.type === 'private' && this.currentTarget.id === friendId) {
                            this.currentTarget = null;
                            this.updateChatHeader();
                            this.renderMessages();
                        }
                        
                        // 重新获取好友列表（确保数据同步，这会重新填充this.contacts）
                        // handleFriendsList会自动更新contacts和UI
                        this.getFriends();
                    } else if (response.data.message && typeof response.data.message === 'string' && 
                               response.data.message.includes('好友申请已发送')) {
                        // 发送好友申请成功响应
                        this.showSuccess(response.data.message || '好友申请已发送');
                        // 刷新在线用户列表（更新按钮状态）
                        setTimeout(() => {
                            this.renderOnlineUsers();
                        }, 200);
                    } else if ((response.data.message && typeof response.data.message === 'string' && response.data.message.includes('管理员')) || response.data.action) {
                        // 设置/取消管理员成功响应
                        this.showSuccess(response.data.message || response.msg || '操作成功');
                        // 如果是当前群组，刷新成员列表
                        if (response.data.groupId && this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === response.data.groupId) {
                            // 延迟刷新，确保后端已经更新完成
                            setTimeout(() => {
                                this.getGroupMembers(response.data.groupId);
                            }, 200);
                        }
                    }
                } else if (typeof response.data === 'string' && (response.data.includes('禁言') || response.data.includes('管理员'))) {
                    // 处理字符串类型的响应（兼容旧格式）
                    this.showSuccess(response.data);
                } else if (response.msg && (response.msg.includes('禁言') || response.msg.includes('踢出') || response.msg.includes('管理员'))) {
                    // 处理成功消息（可能msg字段有内容）
                    this.showSuccess(response.msg);
                    if (response.data && typeof response.data === 'object' && response.data.groupId && this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === response.data.groupId) {
                        this.getGroupMembers(response.data.groupId);
                    }
                }
                break;
            case 'GROUP_MEMBERS_RESP':
                this.handleGroupMembers(response.data);
                break;
            default:
                console.log('未知消息类型:', response.type);
        }
    }

    // 处理登录响应
    handleLoginResponse(response) {
        if (response.code === 200 && response.data) {
            this.currentUser = response.data;
            this.updateUserInfo();
            document.getElementById('loginPanel').classList.add('hidden');
            document.getElementById('chatPanel').classList.remove('hidden');
            // 启动禁言状态检查
            this.startMuteCheck();
            // WebSocket 连接已经在登录前建立了，直接加载初始数据
            // 但如果连接还没建立好，等待一下
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.loadInitialData();
            } else {
                // 如果连接还没建立，等待连接建立后再加载
                const checkConnection = setInterval(() => {
                    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                        clearInterval(checkConnection);
                        this.loadInitialData();
                    }
                }, 100);
                // 5秒后超时
                setTimeout(() => clearInterval(checkConnection), 5000);
            }
        } else {
            this.showError(response.msg || '登录失败');
        }
    }

    // 更新用户信息显示
    updateUserInfo() {
        if (this.currentUser) {
            const username = this.currentUser.username || this.currentUser.userId;
            document.getElementById('currentUserName').textContent = username;
            document.getElementById('currentUserInitial').textContent = username.charAt(0).toUpperCase();
            const role = this.currentUser.role || 'USER';
            const roleText = role === 'ADMIN' ? '管理员' : '普通用户';
            document.getElementById('currentUserRole').textContent = roleText;
            document.getElementById('currentUserRole').className = 'user-role ' + role.toLowerCase();
        }
    }

    // 加载初始数据
    loadInitialData() {
        // 获取好友列表（恢复联系人列表）
        this.getFriends();
        // 获取在线用户列表（用于更新联系人状态）
        this.getOnlineUsers();
        // 获取群组列表
        this.getGroups();
        // 检查好友申请（用于显示小红点）
        this.checkFriendRequests();
    }

    // 检查好友申请（用于更新小红点）
    checkFriendRequests() {
        this.send({
            action: 'GET_FRIEND_REQUESTS',
            params: {}
        });
    }

    // 获取在线用户列表
    getOnlineUsers() {
        this.send({
            action: 'GET_ONLINE',
            params: {}
        });
    }

    // 处理在线用户列表
    handleOnlineUsers(data) {
        console.log('处理在线用户列表:', data);
        if (data && data.users && Array.isArray(data.users)) {
            const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
            
            // 创建新的在线用户Set
            const newOnlineUserIds = new Set();
            
            // 更新在线用户列表
            this.onlineUsers.clear();
            data.users.forEach(user => {
                // 使用 userId 或 username 作为 key
                const userId = user.userId || user.username;
                if (!userId) {
                    console.warn('用户数据缺少 userId 和 username:', user);
                    return;
                }
                
                if (userId !== currentUserId) {
                    this.onlineUsers.set(userId, user);
                    newOnlineUserIds.add(userId);
                }
            });
            
            // 更新联系人的在线状态（不自动添加新联系人）
            // 只更新已有联系人的在线状态
            this.contacts.forEach((contact, userId) => {
                if (newOnlineUserIds.has(userId)) {
                    contact.isOnline = true;
                } else {
                    // 如果联系人不在新的在线列表中，设置为离线
                    contact.isOnline = false;
                }
            });
            
            console.log('在线用户数量:', this.onlineUsers.size);
            this.renderContacts();
            
            // 如果当前正在与某个用户聊天，更新聊天头部的在线状态
            if (this.currentTarget && this.currentTarget.type === 'private') {
                this.updateChatHeader();
            }
            
            // 如果当前在"在线用户"标签页，更新显示
            const onlineTabBtn = document.querySelector('.tab-btn[data-tab="online"]');
            if (onlineTabBtn && onlineTabBtn.classList.contains('active')) {
                this.renderOnlineUsers();
            }
        } else {
            console.warn('在线用户列表数据格式不正确:', data);
        }
    }

    // 获取好友列表
    getFriends() {
        this.send({
            action: 'GET_FRIENDS',
            params: {}
        });
    }

    // 获取群组列表
    getGroups() {
        this.send({
            action: 'GET_GROUPS',
            params: {}
        });
    }

    // 处理好友列表
    handleFriendsList(data) {
        if (data && data.friends && Array.isArray(data.friends)) {
            // 清空当前联系人列表
            this.contacts.clear();
            
            // 添加好友到联系人列表
            data.friends.forEach(friend => {
                const userId = friend.userId || friend.username;
                if (userId) {
                    this.contacts.set(userId, {
                        name: friend.username || userId,
                        lastMsg: friend.lastMsg || '',
                        timestamp: friend.lastMsgTime || 0,
                        isOnline: friend.isOnline || false
                    });
                }
            });
            
            // 更新在线状态
            this.updateFriendsOnlineStatus();
            
            // 渲染联系人列表和在线用户列表（确保"已添加"状态更新为加号）
            this.renderContacts();
            this.renderOnlineUsers();
            
            console.log('好友列表加载完成，共 ' + data.friends.length + ' 个好友');
        }
    }

    // 更新好友在线状态
    updateFriendsOnlineStatus() {
        this.contacts.forEach((contact, userId) => {
            contact.isOnline = this.onlineUsers.has(userId);
        });
    }

    // 处理群组列表
    handleGroupsList(data) {
        if (data && data.groups) {
            data.groups.forEach(group => {
                if (!this.groups.has(group.groupId)) {
                    this.groups.set(group.groupId, {
                        name: group.groupName,
                        lastMsg: '',
                        timestamp: 0,
                        group: group
                    });
                }
            });
            this.renderGroups();
        }
    }

    // 处理群组创建
    handleGroupCreated(data) {
        if (data) {
            this.groups.set(data.groupId, {
                name: data.groupName,
                lastMsg: '',
                timestamp: 0,
                group: data
            });
            this.renderGroups();
            document.getElementById('createGroupModal').classList.add('hidden');
            this.showSuccess('群组创建成功');
            // 自动切换到该群组
            this.selectChatTarget('group', data.groupId);
        }
    }

    // 处理加入群组（包括被拉入群组）
    handleGroupJoined(data) {
        if (data && data.groupId) {
            // 添加到群组列表
            if (!this.groups.has(data.groupId)) {
                this.groups.set(data.groupId, {
                    name: data.groupName,
                    lastMsg: '',
                    timestamp: 0,
                    group: data
                });
                this.renderGroups();
                console.log('已加入群组: ' + data.groupName);
            }
        }
    }

    // 处理群组更新（成员变化）
    handleGroupUpdated(data) {
        if (data && data.groupId) {
            // 更新群组信息
            if (this.groups.has(data.groupId)) {
                const existingGroup = this.groups.get(data.groupId);
                // 更新群组对象，保留其他信息
                if (existingGroup.group) {
                    // 合并更新
                    if (data.members) existingGroup.group.members = data.members;
                    if (data.admins) existingGroup.group.admins = data.admins;
                    if (data.owner) existingGroup.group.owner = data.owner;
                    if (data.groupName) existingGroup.group.groupName = data.groupName;
                } else {
                    existingGroup.group = data;
                }
                if (data.groupName) existingGroup.name = data.groupName;
                
                // 同时更新缓存
                this.groupMembers.set(data.groupId, {
                    members: data.members || existingGroup.group.members || [],
                    owner: data.owner || existingGroup.group.owner,
                    admins: data.admins || existingGroup.group.admins || []
                });
                
                this.renderGroups();
                console.log('群组信息已更新: ' + (data.groupName || existingGroup.name));
                
                // 如果当前正在查看这个群组的成员列表，重新渲染成员列表
                const infoPanel = document.getElementById('infoPanel');
                if (!infoPanel.classList.contains('hidden') && 
                    this.currentTarget && 
                    this.currentTarget.type === 'group' && 
                    this.currentTarget.id === data.groupId) {
                    // 重新获取并渲染成员列表
                    this.getGroupMembers(data.groupId);
                }
            }
        }
    }

    // 退出群组
    leaveGroup(groupId) {
        if (!groupId) return;
        
        if (confirm('确定要退出这个群组吗？')) {
            this.send({
                action: 'LEAVE_GROUP',
                params: { groupId: groupId }
            });
        }
    }

    // 处理退出群组成功
    handleLeaveGroupSuccess(data) {
        const groupId = data.groupId;
        
        // 从群组列表中移除
        if (this.groups.has(groupId)) {
            this.groups.delete(groupId);
            this.renderGroups();
        }
        
        // 关闭信息面板
        document.getElementById('infoPanel').classList.add('hidden');
        
        // 如果当前正在查看这个群组，切换到其他聊天
        if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
            this.currentTarget = null;
            this.updateChatHeader();
            this.renderMessages();
        }
        
        // 清除该群组的消息历史
        if (this.messages.has(groupId)) {
            this.messages.delete(groupId);
        }
        
        console.log('已退出群组: ' + data.groupName);
        // 显示成功提示（使用简单的alert，可以后续改进为更好的UI提示）
        alert('已成功退出群组: ' + data.groupName);
    }

    // 处理群组解散事件
    handleGroupDissolved(data) {
        if (data && data.groupId) {
            const groupId = data.groupId;
            const groupName = data.groupName || groupId;

            // 从群组列表中移除
            if (this.groups.has(groupId)) {
                this.groups.delete(groupId);
                this.renderGroups();
            }

            // 关闭信息面板
            document.getElementById('infoPanel').classList.add('hidden');

            // 如果当前正在查看这个群组，切换到其他聊天
            if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
                this.currentTarget = null;
                this.updateChatHeader();
                this.renderMessages(); // 清空消息列表
            }

            // 清除该群组的消息历史
            if (this.messages.has(groupId)) {
                this.messages.delete(groupId);
            }

            this.showSuccess('群组 "' + groupName + '" 已被解散');
            console.log('群组已解散: ' + groupName);
        }
    }

    // 解散群组
    dissolveGroup(groupId) {
        if (!groupId) return;

        if (confirm('确定要解散这个群组吗？解散后所有成员都将被移出群组，且无法恢复。')) {
            this.send({
                action: 'DISSOLVE_GROUP',
                params: { groupId: groupId }
            });
        }
    }

    // 处理被踢出群组
    handleGroupKicked(data) {
        if (data && data.groupId) {
            const groupId = data.groupId;
            const groupName = data.groupName || groupId;

            // 从群组列表中移除
            if (this.groups.has(groupId)) {
                this.groups.delete(groupId);
                this.renderGroups();
            }

            // 关闭信息面板
            document.getElementById('infoPanel').classList.add('hidden');

            // 如果当前正在查看这个群组，切换到其他聊天
            if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
                this.currentTarget = null;
                this.updateChatHeader();
                this.renderMessages();
            }

            // 清除该群组的消息历史
            if (this.messages.has(groupId)) {
                this.messages.delete(groupId);
            }

            this.showError('您已被移出群组: ' + groupName);
            console.log('被踢出群组: ' + groupName);
        }
    }

    // 处理踢出群成员成功
    handleKickGroupUserSuccess(data) {
        if (data && data.groupId) {
            this.showSuccess(data.message || '已成功将用户踢出群组');
            // 刷新成员列表
            if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === data.groupId) {
                this.getGroupMembers(data.groupId);
            }
        }
    }

    // 处理解散群组成功
    handleDissolveGroupSuccess(data) {
        const groupId = data.groupId;
        const groupName = data.groupName || groupId;

        // 从群组列表中移除
        if (this.groups.has(groupId)) {
            this.groups.delete(groupId);
            this.renderGroups();
        }

        // 关闭信息面板
        document.getElementById('infoPanel').classList.add('hidden');

        // 如果当前正在查看这个群组，切换到其他聊天
        if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
            this.currentTarget = null;
            this.updateChatHeader();
            this.renderMessages(); // 清空消息列表
        }

        // 清除该群组的消息历史
        if (this.messages.has(groupId)) {
            this.messages.delete(groupId);
        }

        this.showSuccess('群组 "' + groupName + '" 已成功解散');
        console.log('群组已解散: ' + groupName);
    }

    // 处理群成员列表
    handleGroupMembers(data) {
        if (data && data.members) {
            const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
            const isOwner = data.owner === currentUserId;
            const isAdmin = data.admins && data.admins.includes(currentUserId);
            const content = document.getElementById('infoContent');
            
            content.innerHTML = `
                <h4>${data.groupName || '群组成员'}</h4>
                <ul class="member-list">
                    ${data.members.map(member => {
                        const memberIsOwner = data.owner === member;
                        const memberIsAdmin = data.admins && data.admins.includes(member);
                        const isCurrentUser = member === currentUserId;
                        // 群主可以管理所有人（除了自己），管理员可以管理普通成员（不能管理群主和其他管理员）
                        const canManage = !isCurrentUser && (
                            isOwner || 
                            (isAdmin && !memberIsOwner && !memberIsAdmin)
                        );
                        
                        return `
                            <li class="member-item">
                                <div class="member-avatar">${member.charAt(0).toUpperCase()}</div>
                                <div class="member-name">
                                    ${memberIsOwner ? '<span style="color: #ffd700; font-weight: bold;">群主</span> ' : ''}
                                    ${memberIsAdmin && !memberIsOwner ? '<span style="color: #4caf50; font-weight: bold;">群聊管理员</span> ' : ''}
                                    ${member}
                                </div>
                                ${canManage ? `
                                    <div class="member-manage-actions">
                                        <button class="btn-manage-small" onclick="app.showManageMenu('${data.groupId}', '${member}', ${memberIsOwner}, ${memberIsAdmin})">管理</button>
                                    </div>
                                ` : ''}
                            </li>
                        `;
                    }).join('')}
                </ul>
                ${!isOwner ? `
                    <div class="member-actions">
                        <button id="leaveGroupBtn" class="btn-leave-group" onclick="app.leaveGroup('${data.groupId}')">
                            退出群聊
                        </button>
                    </div>
                ` : `
                    <div class="member-actions">
                        <button id="dissolveGroupBtn" class="btn-leave-group" onclick="app.dissolveGroup('${data.groupId}')">
                            解散群聊
                        </button>
                        <p class="leave-group-hint">群主不能退出群组，请使用解散群组功能</p>
                    </div>
                `}
            `;
            document.getElementById('infoTitle').textContent = '群组成员';
            document.getElementById('infoPanel').classList.remove('hidden');
        }
    }

    // 显示管理菜单
    showManageMenu(groupId, targetUser, isOwner, isAdmin) {
        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        const group = this.groups.get(groupId);
        const isCurrentUserOwner = group && group.group && group.group.owner === currentUserId;
        const isCurrentUserAdmin = group && group.group && group.group.admins && group.group.admins.includes(currentUserId);
        
        const actions = [];
        
        // 禁言功能：群主和管理员都有
        actions.push({
            label: '禁言',
            action: () => {
                const minutes = prompt(`请输入禁言时长（分钟）:`, '10');
                if (minutes && !isNaN(minutes) && parseInt(minutes) > 0) {
                    this.muteGroupUser(groupId, targetUser, parseInt(minutes));
                }
            }
        });
        
        // 踢出功能：群主和管理员都有
        actions.push({
            label: '踢出群聊',
            action: () => {
                if (confirm(`确定要将 ${targetUser} 踢出群聊吗？`)) {
                    this.kickGroupUser(groupId, targetUser);
                }
            }
        });
        
        // 设置/取消管理员：只有群主有
        if (isCurrentUserOwner) {
            if (isAdmin) {
                actions.push({
                    label: '取消管理员',
                    action: () => {
                        if (confirm(`确定要取消 ${targetUser} 的管理员权限吗？`)) {
                            this.setGroupAdmin(groupId, targetUser, false);
                        }
                    }
                });
            } else {
                actions.push({
                    label: '设为管理员',
                    action: () => {
                        if (confirm(`确定要将 ${targetUser} 设为管理员吗？`)) {
                            this.setGroupAdmin(groupId, targetUser, true);
                        }
                    }
                });
            }
        }
        
        // 显示操作菜单 - 修复：使用闭包来正确传递参数
        const menuHtml = actions.map((action, index) => {
            // 创建闭包来保存action函数
            const actionFunc = action.action;
            const actionId = `manageAction_${Date.now()}_${index}`;
            // 将action函数保存到全局对象中
            window[actionId] = function() {
                actionFunc();
                delete window[actionId];
            };
            return `<button class="manage-menu-item" onclick="app.closeManageMenu(); window['${actionId}']();">${action.label}</button>`;
        }).join('');
        
        const menuDiv = document.createElement('div');
        menuDiv.className = 'manage-menu';
        menuDiv.innerHTML = `
            <div class="manage-menu-content">
                <h4>管理 ${targetUser}</h4>
                ${menuHtml}
                <button class="manage-menu-item" onclick="app.closeManageMenu()">取消</button>
            </div>
        `;
        menuDiv.id = 'manageMenu';
        document.body.appendChild(menuDiv);
        
        // 关闭菜单
        menuDiv.addEventListener('click', (e) => {
            if (e.target === menuDiv) {
                this.closeManageMenu();
            }
        });
    }

    // 关闭管理菜单
    closeManageMenu() {
        const menu = document.getElementById('manageMenu');
        if (menu) {
            menu.remove();
        }
    }

    // 禁言群成员
    muteGroupUser(groupId, targetUser, durationMinutes) {
        this.send({
            action: 'MUTE_GROUP_USER',
            params: {
                groupId: groupId,
                targetUser: targetUser,
                durationMinutes: durationMinutes
            }
        });
    }

    // 踢出群成员
    kickGroupUser(groupId, targetUser) {
        this.send({
            action: 'KICK_GROUP_USER',
            params: {
                groupId: groupId,
                targetUser: targetUser
            }
        });
    }

    // 设置/取消管理员
    setGroupAdmin(groupId, targetUser, isSet) {
        this.send({
            action: 'SET_GROUP_ADMIN',
            params: {
                groupId: groupId,
                targetUser: targetUser,
                action: isSet ? 'set' : 'remove'
            }
        });
    }

    // 显示@成员选择列表
    showAtMemberList(groupId) {
        const group = this.groups.get(groupId);
        if (!group) {
            this.showError('无法获取群组信息');
            return;
        }
        
        // 如果还没有群组详细信息，先获取
        if (!group.group) {
            // 先获取群成员列表
            this.getGroupMembers(groupId);
            // 延迟显示菜单（等待成员列表返回）
            setTimeout(() => {
                const updatedGroup = this.groups.get(groupId);
                if (updatedGroup && updatedGroup.group) {
                    this.showAtMemberList(groupId);
                } else {
                    this.showError('获取群成员信息失败，请稍后重试');
                }
            }, 500);
            return;
        }

        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        const isOwner = group.group.owner === currentUserId;
        const members = group.group.members || [];
        const owner = group.group.owner;
        const admins = group.group.admins || [];

        // 创建成员选择菜单
        const menuDiv = document.createElement('div');
        menuDiv.className = 'manage-menu';
        menuDiv.id = 'atMemberMenu';
        
        let memberListHtml = '';
        
        // 群主可以@所有人
        if (isOwner) {
            memberListHtml += `
                <div class="at-member-item" onclick="app.selectAtMember('@所有人'); app.closeAtMemberMenu();">
                    <span class="at-member-name" style="color: #ffd700; font-weight: bold;">@所有人</span>
                </div>
            `;
        }
        
        // 显示所有成员
        members.forEach(member => {
            if (member === currentUserId) return; // 不显示自己
            const memberIsOwner = member === owner;
            const memberIsAdmin = admins.includes(member);
            const badge = memberIsOwner ? '<span style="color: #ffd700;">群主</span> ' : 
                         (memberIsAdmin ? '<span style="color: #4caf50;">管理员</span> ' : '');
            memberListHtml += `
                <div class="at-member-item" onclick="app.selectAtMember('@${member}'); app.closeAtMemberMenu();">
                    <span class="at-member-name">${badge}${member}</span>
                </div>
            `;
        });

        menuDiv.innerHTML = `
            <div class="manage-menu-content" style="max-height: 400px; overflow-y: auto;">
                <h4>选择@成员</h4>
                ${memberListHtml || '<div style="padding: 10px; text-align: center; color: #888;">暂无成员</div>'}
                <button class="manage-menu-item" onclick="app.closeAtMemberMenu()">取消</button>
            </div>
        `;
        
        document.body.appendChild(menuDiv);
        
        // 点击外部关闭
        menuDiv.addEventListener('click', (e) => {
            if (e.target === menuDiv) {
                this.closeAtMemberMenu();
            }
        });
    }

    // 关闭@成员选择菜单
    closeAtMemberMenu() {
        const menu = document.getElementById('atMemberMenu');
        if (menu) {
            menu.remove();
        }
    }

    // 选择@成员
    selectAtMember(atText) {
        const input = document.getElementById('messageInput');
        const currentValue = input.value;
        const cursorPos = input.selectionStart || currentValue.length;
        
        // 在光标位置插入@文本
        const atFormatted = atText + ' ';
        const newValue = currentValue.slice(0, cursorPos) + atFormatted + currentValue.slice(cursorPos);
        input.value = newValue;
        input.focus();
        
        // 设置光标位置到@文本之后
        const newCursorPos = cursorPos + atFormatted.length;
        input.setSelectionRange(newCursorPos, newCursorPos);
        
        // 添加键盘事件监听，实现整体删除
        this.setupAtInputHandler(input);
    }

    // 设置@输入处理（实现整体删除）
    setupAtInputHandler(input) {
        // 移除旧的监听器（如果存在）
        if (input._atHandlerSet) return;
        input._atHandlerSet = true;
        
        input.addEventListener('keydown', (e) => {
            // 如果按下Backspace，检查是否需要整体删除@文本
            if (e.key === 'Backspace' && !e.shiftKey && !e.ctrlKey && !e.metaKey) {
                const cursorPos = input.selectionStart;
                if (cursorPos > 0) {
                    const textBeforeCursor = input.value.slice(0, cursorPos);
                    // 匹配 @所有人 或 @username 格式（查找最后一个@到光标之间的内容）
                    const atMatch = textBeforeCursor.match(/@(?:所有人|\w+)\s*$/);
                    if (atMatch) {
                        e.preventDefault();
                        // 删除整个@文本
                        const startPos = cursorPos - atMatch[0].length;
                        input.value = input.value.slice(0, startPos) + input.value.slice(cursorPos);
                        input.setSelectionRange(startPos, startPos);
                    }
                }
            }
        }, true);
    }

    // 渲染联系人列表
    renderContacts() {
        const list = document.getElementById('contactsList');
        const searchTerm = document.getElementById('searchInput').value.toLowerCase();
        
        let html = '';
        this.contacts.forEach((contact, userId) => {
            // 过滤掉群ID（群ID通常是UUID格式，长度较长）
            // 更安全的做法：检查是否在groups Map中，如果在就是群ID，不应该显示在联系人列表
            if (this.groups.has(userId)) {
                console.warn('发现群ID错误地出现在联系人列表中，已过滤: ' + userId);
                return; // 跳过群ID
            }
            
            if (searchTerm && !contact.name.toLowerCase().includes(searchTerm)) {
                return;
            }
            const isActive = this.currentTarget && 
                           this.currentTarget.type === 'private' && 
                           this.currentTarget.id === userId;
            html += `
                <div class="contact-item ${isActive ? 'active' : ''}" 
                     onclick="app.selectChatTarget('private', '${userId}')">
                    <div class="contact-avatar">${contact.name.charAt(0).toUpperCase()}</div>
                    <div class="contact-info">
                        <div class="contact-name ${contact.isOnline ? 'online' : 'offline'}">${contact.name}</div>
                        <div class="contact-last-msg">${contact.lastMsg || '暂无消息'}</div>
                    </div>
                    ${contact.isOnline ? '<span class="contact-badge online">在线</span>' : '<span class="contact-badge offline">离线</span>'}
                </div>
            `;
        });
        
        if (html === '') {
            html = '<div style="padding: 20px; text-align: center; color: #888;">暂无联系人</div>';
        }
        
        list.innerHTML = html;
    }

    // 渲染群组列表
    renderGroups() {
        const list = document.getElementById('groupsList');
        const searchTerm = document.getElementById('searchInput').value.toLowerCase();
        
        let html = '';
        this.groups.forEach((group, groupId) => {
            if (searchTerm && !group.name.toLowerCase().includes(searchTerm)) {
                return;
            }
            const isActive = this.currentTarget && 
                           this.currentTarget.type === 'group' && 
                           this.currentTarget.id === groupId;
            
            // 检查是否有@提醒
            const hasAtMention = group.hasAtMention || (this.atMentions.has(groupId) && this.atMentions.get(groupId).size > 0);
            
            html += `
                <div class="contact-item ${isActive ? 'active' : ''} ${hasAtMention ? 'at-mentioned' : ''}" 
                     onclick="app.selectChatTarget('group', '${groupId}')">
                    <div class="contact-avatar group">群</div>
                    <div class="contact-info">
                        <div class="contact-name">
                            ${group.name}
                            ${hasAtMention ? '<span style="color: red; margin-left: 5px;">[有人@我]</span>' : ''}
                        </div>
                        <div class="contact-last-msg">${group.lastMsg || '暂无消息'}</div>
                    </div>
                </div>
            `;
        });
        
        list.innerHTML = '<button id="createGroupBtn" class="btn-create-group">+ 创建群组</button>' + html;
        document.getElementById('createGroupBtn').addEventListener('click', () => {
            document.getElementById('createGroupModal').classList.remove('hidden');
        });
    }

    // 渲染在线用户列表
    renderOnlineUsers() {
        const list = document.getElementById('onlineUsersList');
        let html = '';
        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        const searchTerm = document.getElementById('searchInput').value.toLowerCase().trim();
        
        this.onlineUsers.forEach((user, userId) => {
            // 排除当前用户
            const userIdToCompare = user.userId || user.username || userId;
            if (userIdToCompare === currentUserId) return;
            
            const isAdmin = user.role === 'ADMIN';
            const displayName = user.username || userId;
            
            // 搜索过滤
            if (searchTerm && !displayName.toLowerCase().includes(searchTerm)) {
                return;
            }
            
            // 检查是否已经是好友
            const isFriend = this.contacts.has(userIdToCompare);
            html += `
                <div class="contact-item">
                    <div class="contact-avatar" onclick="app.selectChatTarget('private', '${userIdToCompare}')">${displayName.charAt(0).toUpperCase()}</div>
                    <div class="contact-info" onclick="app.selectChatTarget('private', '${userIdToCompare}')">
                        <div class="contact-name">
                            ${this.escapeHtml(displayName)}
                            ${isAdmin ? ' <span style="color: #ffd700;">👑</span>' : ''}
                        </div>
                        <div class="contact-last-msg">${isFriend ? '已添加' : '在线'}</div>
                    </div>
                    ${!isFriend ? `<button class="btn-add-friend-icon" onclick="event.stopPropagation(); app.sendFriendRequest('${this.escapeHtml(userIdToCompare)}')" title="添加好友">+</button>` : ''}
                </div>
            `;
        });
        
        if (html === '') {
            html = '<div style="padding: 20px; text-align: center; color: #888;">暂无其他在线用户</div>';
        }
        
        list.innerHTML = html;
    }

    // 过滤联系人
    filterContacts(searchTerm) {
        const activeTab = document.querySelector('.tab-btn.active').dataset.tab;
        if (activeTab === 'contacts') {
            this.renderContacts();
        } else if (activeTab === 'groups') {
            this.renderGroups();
        } else if (activeTab === 'online') {
            this.renderOnlineUsers();
        }
    }

    // 切换标签
    switchTab(tab) {
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        
        document.querySelector(`.tab-btn[data-tab="${tab}"]`).classList.add('active');
        
        if (tab === 'contacts') {
            document.getElementById('contactsList').classList.add('active');
            this.renderContacts();
        } else if (tab === 'groups') {
            document.getElementById('groupsList').classList.add('active');
            this.renderGroups();
        } else if (tab === 'online') {
            document.getElementById('onlineUsersList').classList.add('active');
            this.renderOnlineUsers();
        }
    }

    // 选择聊天对象
    selectChatTarget(type, id) {
        this.currentTarget = { type, id };
        
        // 如果切换到群组，清除该群组的@提醒
        if (type === 'group' && this.atMentions.has(id)) {
            this.atMentions.delete(id);
            const group = this.groups.get(id);
            if (group) {
                group.hasAtMention = false;
            }
        }
        
        this.updateChatHeader();
        this.loadMessages();
        this.renderContacts();
        this.renderGroups();
        // 更新禁言状态
        this.updateMuteStatus();
        
        // 显示/隐藏相关按钮
        const atBtn = document.getElementById('atBtn');
        const viewMembersBtn = document.getElementById('viewMembersBtn');
        if (type === 'group') {
            atBtn.classList.remove('hidden');
            viewMembersBtn.classList.remove('hidden');
        } else {
            atBtn.classList.add('hidden');
            viewMembersBtn.classList.add('hidden');
        }
    }

    // 更新聊天头部
    updateChatHeader() {
        if (!this.currentTarget) {
            document.getElementById('targetName').textContent = '选择一个聊天对象';
            document.getElementById('targetStatus').textContent = '';
            // 隐藏所有操作按钮
            document.getElementById('atBtn').classList.add('hidden');
            document.getElementById('viewMembersBtn').classList.add('hidden');
            document.getElementById('deleteFriendBtn').classList.add('hidden');
            return;
        }

        if (this.currentTarget.type === 'private') {
            const contact = this.contacts.get(this.currentTarget.id);
            const name = contact ? contact.name : this.currentTarget.id;
            document.getElementById('targetName').textContent = name;
            document.getElementById('targetAvatar').textContent = name.charAt(0).toUpperCase();
            const statusElement = document.getElementById('targetStatus');
            // 获取在线状态：如果联系人在contacts中，使用contact.isOnline；否则从onlineUsers中查找
            const isOnline = contact ? contact.isOnline : (this.onlineUsers.has(this.currentTarget.id));
            statusElement.textContent = isOnline ? '在线' : '离线';
            // 应用相应的CSS类
            statusElement.className = 'target-status ' + (isOnline ? 'online' : 'offline');
            // 隐藏群聊相关按钮，显示私聊相关按钮
            document.getElementById('atBtn').classList.add('hidden');
            document.getElementById('viewMembersBtn').classList.add('hidden');
            // 显示删除好友按钮（如果对方是好友）
            const deleteFriendBtn = document.getElementById('deleteFriendBtn');
            if (contact) {
                deleteFriendBtn.classList.remove('hidden');
            } else {
                deleteFriendBtn.classList.add('hidden');
            }
        } else if (this.currentTarget.type === 'group') {
            const group = this.groups.get(this.currentTarget.id);
            const name = group ? group.name : this.currentTarget.id;
            document.getElementById('targetName').textContent = name;
            document.getElementById('targetAvatar').textContent = '群';
            document.getElementById('targetStatus').textContent = '群聊';
            // 显示群聊相关按钮（所有群成员都可以使用@功能）
            document.getElementById('atBtn').classList.remove('hidden');
            document.getElementById('viewMembersBtn').classList.remove('hidden');
            // 隐藏私聊相关按钮
            document.getElementById('deleteFriendBtn').classList.add('hidden');
        }
    }

    // 加载消息
    loadMessages() {
        if (!this.currentTarget) return;
        
        const targetId = this.currentTarget.id;
        if (!this.messages.has(targetId)) {
            this.messages.set(targetId, []);
            // 请求历史消息
            this.getHistory();
        }
        this.renderMessages();
    }

    // 获取历史消息
    getHistory() {
        if (!this.currentTarget) return;
        
        const targetId = this.currentTarget.id;
        const messageList = this.messages.get(targetId) || [];
        const beforeTime = messageList.length > 0 ? messageList[0].timestamp : undefined;
        
        const params = this.currentTarget.type === 'group' 
            ? { groupId: targetId }
            : { targetUser: targetId };
        
        if (beforeTime) {
            params.beforeTime = beforeTime;
        }
        
        this.send({
            action: 'GET_HISTORY',
            params: params
        });
    }

    // 撤回消息
    recallMessage(msgId) {
        if (!msgId) return;
        
        if (confirm('确定要撤回这条消息吗？')) {
            this.send({
                action: 'RECALL_MSG',
                params: { msgId: msgId }
            });
        }
    }

    // 处理消息撤回事件
    handleMessageRecalled(data) {
        if (!data || !data.recalledMsgId) return;
        
        const msgId = data.recalledMsgId;
        const isGroup = data.isGroup;
        const operator = data.operator; // 撤回操作者
        const fromUser = data.fromUser; // 原始发送者
        
        // 确定targetId（群聊或私聊）
        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        let targetId;
        if (isGroup) {
            targetId = data.toUser; // 群ID
        } else {
            // 私聊：使用对方用户ID作为targetId
            if (fromUser === currentUserId) {
                targetId = data.toUser; // 我是发送者，对方是接收者
            } else {
                targetId = fromUser; // 我是接收者，对方是发送者
            }
        }
        
        // 标记消息为已撤回，并保存撤回信息
        const messageList = this.messages.get(targetId) || [];
        const message = messageList.find(m => m.msgId === msgId);
        if (message) {
            message.recalled = true;
            message.recalledBy = operator; // 记录撤回操作者
            message.originalFromUser = fromUser; // 保存原始发送者（可能已经被修改）
            // 清空消息内容，不再显示原始内容
            message.originalContent = message.content;
            message.content = '';
            
            // 如果正在查看这个聊天，重新渲染
            if (this.currentTarget && 
                ((isGroup && this.currentTarget.id === targetId) ||
                 (!isGroup && this.currentTarget.id === targetId))) {
                this.renderMessages();
            }
        }
    }

    // 显示反应菜单
    showReactionMenu(msgId) {
        const reactions = ['👍', '👎', '❤️', '😂', '😢', '😠'];
        const reactTypes = ['like', 'dislike', 'heart', 'laugh', 'sad', 'angry'];
        
        // 简单的反应菜单（可以用更好的UI实现）
        const reaction = prompt(`选择反应:\n${reactions.map((emoji, i) => `${i + 1}. ${emoji} ${reactTypes[i]}`).join('\n')}\n输入数字(1-6):`);
        const index = parseInt(reaction) - 1;
        if (index >= 0 && index < reactTypes.length) {
            this.handleReaction(msgId, reactTypes[index]);
        }
    }

    // 处理消息反应
    handleReaction(msgId, reactType) {
        if (!msgId || !reactType) return;
        
        this.send({
            action: 'MSG_REACT',
            params: {
                msgId: msgId,
                reactType: reactType
            }
        });
    }

    // 处理消息反应事件
    handleMessageReaction(data) {
        if (!data || !data.msgId) return;
        
        const msgId = data.msgId;
        const reactType = data.reactType;
        const isAdd = data.isAdd;
        const count = data.count;
        
        // 更新消息的反应数据
        this.messages.forEach((messageList, targetId) => {
            const message = messageList.find(m => m.msgId === msgId);
            if (message) {
                if (!message.reactions) {
                    message.reactions = {};
                }
                if (!message.reactions[reactType]) {
                    message.reactions[reactType] = [];
                }
                
                if (isAdd) {
                    if (!message.reactions[reactType].includes(data.operator)) {
                        message.reactions[reactType].push(data.operator);
                    }
                } else {
                    message.reactions[reactType] = message.reactions[reactType].filter(u => u !== data.operator);
                    if (message.reactions[reactType].length === 0) {
                        delete message.reactions[reactType];
                    }
                }
                
                // 如果正在查看这个聊天，重新渲染
                if (this.currentTarget && this.currentTarget.id === targetId) {
                    this.renderMessages();
                }
            }
        });
    }

    // 获取反应表情
    getReactEmoji(reactType) {
        const emojiMap = {
            'like': '👍',
            'dislike': '👎',
            'heart': '❤️',
            'laugh': '😂',
            'sad': '😢',
            'angry': '😠'
        };
        return emojiMap[reactType] || reactType;
    }

    // 处理历史消息
    handleHistory(data) {
        if (data && data.messages && this.currentTarget) {
            const targetId = this.currentTarget.id;
            const existingMessages = this.messages.get(targetId) || [];
            // 将新消息插入到开头（历史消息是倒序的）
            const newMessages = Array.isArray(data.messages) ? data.messages : [];
            newMessages.reverse().forEach(msg => {
                if (!existingMessages.find(m => m.msgId === msg.msgId)) {
                    existingMessages.unshift(msg);
                }
            });
            this.messages.set(targetId, existingMessages);
            this.renderMessages();
            
            // 滚动到顶部（加载历史消息时）
            if (newMessages.length > 0) {
                const messageListEl = document.getElementById('messageList');
                messageListEl.scrollTop = 0;
            }
        }
    }

    // 发送消息
    sendMessage() {
        if (!this.currentTarget) {
            this.showError('请先选择聊天对象');
            return;
        }

        const input = document.getElementById('messageInput');
        const content = input.value.trim();
        
        if (!content) {
            return;
        }

        // 检查是否被禁言（只在群聊中检查）
        if (this.currentTarget && this.currentTarget.type === 'group' && this.isMuted()) {
            return; // updateMuteStatus 已经显示了提示
        }

        // 提取@用户列表（支持@所有人）
        const atUsers = [];
        // 匹配 @所有人 或 @username
        const atMatches = content.match(/@(?:所有人|(\w+))/g);
        if (atMatches) {
            atMatches.forEach(match => {
                if (match === '@所有人') {
                    atUsers.push('@所有人');
                } else {
                    const username = match.substring(1);
                    if (username && !atUsers.includes(username)) {
                        atUsers.push(username);
                    }
                }
            });
        }

        const params = {
            content: content
        };

        if (this.currentTarget.type === 'group') {
            params.groupId = this.currentTarget.id;
            if (atUsers.length > 0) {
                params.atUsers = atUsers;
            }
            this.send({
                action: 'SEND_GROUP',
                params: params
            });
        } else {
            params.targetUser = this.currentTarget.id;
            if (atUsers.length > 0) {
                params.atUsers = atUsers;
            }
            this.send({
                action: 'SEND_PRIVATE',
                params: params
            });
        }

        input.value = '';
        this.updateInputHint('');
    }

    // 处理聊天消息
    handleChatMessage(message) {
        if (!message) {
            console.error('handleChatMessage: message is null or undefined');
            return;
        }

        console.log('handleChatMessage 收到消息:', message);
        console.log('message.isGroup:', message.isGroup, 'typeof:', typeof message.isGroup);
        console.log('消息的所有字段:', Object.keys(message));

        // 确保 message.isGroup 字段存在（防止后端忘记设置）
        if (message.isGroup === undefined) {
            console.error('收到消息缺少 isGroup 字段:', message);
            console.error('消息的完整内容:', JSON.stringify(message, null, 2));
            // 直接返回，不处理
            return;
        }

        // 确定消息应该存储在哪个targetId下
        // 对于群聊：targetId = groupId (message.toUser)
        // 对于私聊：targetId = 对方用户ID
        //   - 如果我是发送者：targetId = toUser (接收者)
        //   - 如果我是接收者：targetId = fromUser (发送者)
        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        let targetId;
        if (message.isGroup) {
            // 群聊消息：targetId 就是群ID
            targetId = message.toUser; // 群ID
            console.log('收到群聊消息:', {
                fromUser: message.fromUser,
                groupId: message.toUser,
                content: message.content.substring(0, 20) + '...',
                currentUserId: currentUserId
            });
        } else {
            // 私聊：使用对方用户ID作为targetId
            if (message.fromUser === currentUserId) {
                targetId = message.toUser; // 我是发送者，对方是接收者
            } else {
                targetId = message.fromUser; // 我是接收者，对方是发送者
            }
            console.log('收到私聊消息:', {
                fromUser: message.fromUser,
                toUser: message.toUser,
                targetId: targetId,
                currentUserId: currentUserId
            });
        }
        
        // 更新联系人/群组的最后一条消息
        if (message.isGroup) {
            // 群聊消息：只更新群组信息，不添加到联系人
            const group = this.groups.get(message.toUser);
            if (group) {
                group.lastMsg = message.content.substring(0, 30);
                group.timestamp = message.timestamp;
                
                // 检查是否被@
                if (message.atUsers && message.atUsers.length > 0) {
                    const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
                    const isMentioned = message.atUsers.includes('@所有人') || message.atUsers.includes(currentUserId);
                    if (isMentioned && message.fromUser !== currentUserId) {
                        // 被@了，记录提醒
                        if (!this.atMentions.has(message.toUser)) {
                            this.atMentions.set(message.toUser, new Set());
                        }
                        this.atMentions.get(message.toUser).add(message.msgId);
                        group.hasAtMention = true;
                    }
                }
            } else {
                // 如果群组不在列表中，说明可能还没加载，先不处理
                console.warn('收到群聊消息，但群组不在列表中: ' + message.toUser);
            }
        } else {
            // 私聊消息：只更新已存在的好友的联系人信息（联系人列表只显示好友）
            const contact = this.contacts.get(targetId);
            if (contact) {
                // 只更新已存在的好友的信息
                contact.lastMsg = message.content.substring(0, 30);
                contact.timestamp = message.timestamp;
                // 更新在线状态（如果对方在线）
                contact.isOnline = this.onlineUsers.has(targetId);
            }
            // 如果不是好友，不添加到联系人列表，但仍然保存消息以便查看历史
        }

        // 添加到消息列表
        if (!this.messages.has(targetId)) {
            this.messages.set(targetId, []);
        }
        const messageList = this.messages.get(targetId);
        // 避免重复添加
        if (!messageList.find(m => m.msgId === message.msgId)) {
            messageList.push(message);
            
            // 如果当前正在查看这个聊天，立即渲染
            if (this.currentTarget) {
                let shouldRender = false;
                if (message.isGroup) {
                    // 群聊消息：检查当前目标是否是同一个群组
                    shouldRender = this.currentTarget.type === 'group' && 
                                  this.currentTarget.id === targetId; // targetId就是群ID
                } else {
                    // 私聊消息：检查当前目标是否是同一个用户
                    shouldRender = this.currentTarget.type === 'private' && 
                                  this.currentTarget.id === targetId;
                }
                
                if (shouldRender) {
                    console.log('立即渲染消息，当前target:', this.currentTarget, '消息targetId:', targetId);
                    this.renderMessages();
                }
            }
        }

        // 更新列表显示（群聊消息只更新群组列表，私聊消息只更新联系人列表）
        if (message.isGroup) {
            // 群聊消息：只更新群组列表，不更新联系人列表
            this.renderGroups();
        } else {
            // 私聊消息：只更新联系人列表，不更新群组列表
            this.renderContacts();
        }
    }

    // 渲染消息列表
    renderMessages() {
        const messageListEl = document.getElementById('messageList');
        if (!this.currentTarget) {
            messageListEl.innerHTML = '<div class="empty-state"><p>选择一个聊天对象开始聊天</p></div>';
            return;
        }

        const targetId = this.currentTarget.id;
        const messages = this.messages.get(targetId) || [];

        if (messages.length === 0) {
            messageListEl.innerHTML = '<div class="empty-state"><p>暂无消息</p></div>';
            return;
        }

        let html = '';
        const currentUserId = this.currentUser ? (this.currentUser.userId || this.currentUser.username) : null;
        
        messages.forEach(msg => {
            // 处理系统消息（禁言、踢人等）
            if (msg.isSystemMessage) {
                const fullTime = this.formatFullTime(msg.timestamp);
                html += `
                    <div class="message-item">
                        <div class="message-system">${this.escapeHtml(msg.content)}</div>
                        <div class="message-meta">
                            <span class="message-time">${fullTime}</span>
                        </div>
                    </div>
                `;
                return;
            }
            
            if (!msg || msg.recalled) {
                // 被撤回的消息显示特殊提示：显示"某某撤回了一条消息"
                const originalFromUser = msg.originalFromUser || msg.fromUser;
                
                // 判断显示文本：始终显示原始发送者是谁撤回的
                let recallText;
                if (originalFromUser === currentUserId) {
                    // 如果是我自己发送的消息被撤回
                    recallText = '你撤回了一条消息';
                } else {
                    // 如果是别人发送的消息被撤回
                    // 显示原始发送者名称 + "撤回了一条消息"
                    const senderName = originalFromUser || '某人';
                    recallText = senderName + ' 撤回了一条消息';
                }
                
                // 对于被撤回的消息，也显示完整时间
                const fullTime = this.formatFullTime(msg.timestamp);
                html += `
                    <div class="message-item">
                        <div class="message-recall">${this.escapeHtml(recallText)}</div>
                        <div class="message-meta">
                            <span class="message-time">${fullTime}</span>
                        </div>
                    </div>
                `;
                return;
            }
            const isOwn = msg.fromUser === currentUserId;
            
            // 对于群聊消息，始终显示发送者名称（即使是自己发送的）
            // 对于私聊消息，只有别人的消息才显示发送者名称
            let senderName;
            let showSenderName = false;
            if (msg.isGroup) {
                // 群聊：始终显示发送者名称
                senderName = isOwn ? '我' : (msg.fromUser || '未知');
                showSenderName = true; // 群聊消息始终显示发送者名称
            } else {
                // 私聊：只有别人的消息才显示发送者名称
                senderName = isOwn ? '我' : (msg.fromUser || '未知');
                showSenderName = !isOwn; // 私聊消息只有别人的才显示发送者名称
            }
            
            const senderInitial = senderName.charAt(0).toUpperCase();
            const time = this.formatFullTime(msg.timestamp); // 直接显示完整时间
            const reactions = msg.reactions || {};
            
            // 渲染反应
            let reactionsHtml = '';
            Object.entries(reactions).forEach(([reactType, users]) => {
                if (users && users.length > 0) {
                    const reactEmoji = this.getReactEmoji(reactType);
                    const isReacted = users.includes(currentUserId);
                    reactionsHtml += `
                        <span class="reaction-item ${isReacted ? 'reacted' : ''}" 
                              onclick="app.handleReaction('${msg.msgId}', '${reactType}')">
                            ${reactEmoji} ${users.length}
                        </span>
                    `;
                }
            });

            html += `
                <div class="message-item ${isOwn ? 'own' : ''}" data-msg-id="${msg.msgId}">
                    <div class="message-avatar">${senderInitial}</div>
                    <div class="message-content-wrapper">
                        ${showSenderName ? `<div class="message-sender">${senderName}</div>` : ''}
                        <div class="message-bubble">
                            <div class="message-text">${this.formatMessageContent(msg.content, msg.atUsers)}</div>
                        </div>
                        ${reactionsHtml ? `<div class="message-reactions">${reactionsHtml}</div>` : ''}
                        <div class="message-meta">
                            <span class="message-time">${time}</span>
                            ${isOwn ? `
                                <div class="message-actions">
                                    <button class="btn-action" onclick="app.recallMessage('${msg.msgId}')">撤回</button>
                                    <button class="btn-action" onclick="app.showReactionMenu('${msg.msgId}')">反应</button>
                                </div>
                            ` : `
                                <div class="message-actions">
                                    <button class="btn-action" onclick="app.showReactionMenu('${msg.msgId}')">反应</button>
                                </div>
                            `}
                        </div>
                    </div>
                </div>
            `;
        });

        messageListEl.innerHTML = html;
        // 滚动到底部
        messageListEl.scrollTop = messageListEl.scrollHeight;
    }

    // 格式化时间（简短显示）
    formatTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) {
            return '刚刚';
        } else if (diff < 3600000) {
            return Math.floor(diff / 60000) + '分钟前';
        } else if (diff < 86400000) {
            return Math.floor(diff / 3600000) + '小时前';
        } else {
            return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5);
        }
    }

    // 格式化完整时间（用于悬停提示）
    formatFullTime(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        // 格式：YYYY-MM-DD HH:MM:SS
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    // HTML转义
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 获取群成员
    getGroupMembers(groupId) {
        this.send({
            action: 'GET_GROUP_MEMBERS',
            params: { groupId: groupId }
        });
    }

    // 退出登录
    logout() {
        if (this.ws) {
            this.ws.close();
        }
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
        }
        this.currentUser = null;
        this.currentTarget = null;
        this.contacts.clear();
        this.groups.clear();
        this.messages.clear();
        document.getElementById('chatPanel').classList.add('hidden');
        document.getElementById('loginPanel').classList.remove('hidden');
        document.getElementById('loginForm').reset();
    }

    // 显示错误消息
    showError(msg) {
        const errorEl = document.getElementById('loginError');
        if (errorEl) {
            errorEl.textContent = msg;
            errorEl.classList.add('show');
            setTimeout(() => errorEl.classList.remove('show'), 5000);
        } else {
            alert(msg);
        }
    }

    // 显示成功消息
    showSuccess(msg) {
        // 简单实现，可以后续改进
        console.log('成功:', msg);
        if (typeof msg === 'string') {
            alert(msg);
        }
    }

    // 格式化消息内容（高亮@文本）
    formatMessageContent(content, atUsers) {
        if (!content) return '';
        
        let formatted = this.escapeHtml(content);
        
        // 高亮@所有人
        formatted = formatted.replace(/@所有人/g, '<span class="at-mention">@所有人</span>');
        
        // 高亮@用户名
        if (atUsers && atUsers.length > 0) {
            atUsers.forEach(username => {
                if (username !== '@所有人') {
                    const escapedUsername = this.escapeHtml(username);
                    const regex = new RegExp(`@${escapedUsername.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`, 'g');
                    formatted = formatted.replace(regex, `<span class="at-mention">@${escapedUsername}</span>`);
                }
            });
        } else {
            // 如果没有atUsers，尝试匹配所有@username格式
            formatted = formatted.replace(/@(\w+)/g, '<span class="at-mention">@$1</span>');
        }
        
        return formatted;
    }

    // 更新输入提示
    updateInputHint(hint) {
        document.getElementById('inputHint').textContent = hint;
    }

    // 显示好友申请
    showFriendRequests() {
        document.getElementById('friendRequestModal').classList.remove('hidden');
        // 获取好友申请列表
        this.send({
            action: 'GET_FRIEND_REQUESTS',
            params: {}
        });
    }

    // 处理好友申请列表
    handleFriendRequestsList(data) {
        const listEl = document.getElementById('friendRequestList');
        // 更新小红点状态
        const badge = document.getElementById('friendRequestBadge');
        if (badge) {
            if (data && data.friendRequests && data.friendRequests.length > 0) {
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
        
        if (!data || !data.friendRequests || data.friendRequests.length === 0) {
            listEl.innerHTML = '<div style="padding: 20px; text-align: center; color: #888;">暂无好友申请</div>';
            return;
        }

        let html = '';
        data.friendRequests.forEach(fromUser => {
            html += `
                <div style="padding: 15px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-weight: 500; margin-bottom: 5px;">${this.escapeHtml(fromUser)}</div>
                        <div style="font-size: 12px; color: #888;">想要添加您为好友</div>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button class="btn-primary" onclick="app.acceptFriendRequest('${this.escapeHtml(fromUser)}')" style="padding: 5px 20px; font-size: 14px; width: auto; min-width: 70px;">接受</button>
                        <button class="btn-secondary" onclick="app.rejectFriendRequest('${this.escapeHtml(fromUser)}')" style="padding: 5px 20px; font-size: 14px; min-width: 70px;">拒绝</button>
                    </div>
                </div>
            `;
        });
        listEl.innerHTML = html;
    }

    // 接受好友申请
    acceptFriendRequest(fromUser) {
        this.send({
            action: 'ACCEPT_FRIEND_REQUEST',
            params: {
                fromUser: fromUser
            }
        });
    }

    // 拒绝好友申请
    rejectFriendRequest(fromUser) {
        this.send({
            action: 'REJECT_FRIEND_REQUEST',
            params: {
                fromUser: fromUser
            }
        });
    }

    // 发送好友申请
    sendFriendRequest(toUser) {
        if (!toUser) return;
        
        if (confirm(`确定要添加 ${toUser} 为好友吗？`)) {
            this.send({
                action: 'SEND_FRIEND_REQUEST',
                params: {
                    toUser: toUser
                }
            });
        }
    }

    // 处理好友申请被接受的通知（申请方收到）
    handleFriendRequestAccepted(data) {
        if (!data || !data.toUser) return;
        
        // 立即刷新好友列表（因为现在双方是好友了），不需要等待用户确认
        this.getFriends();
        
        // 显示通知（但不阻塞）
        this.showSuccess(data.message || data.toUser + ' 接受了您的好友申请');
    }

    // 处理新好友申请通知
    handleFriendRequestNotice(data) {
        if (!data || !data.fromUser) return;
        
        // 显示小红点
        this.updateFriendRequestBadge(true);
        
        // 显示通知
        this.showSuccess(data.message || data.fromUser + ' 想要添加您为好友');
    }

    // 删除好友
    deleteFriend(friendId) {
        if (!friendId) return;
        
        if (confirm('确定要删除该好友吗？删除后双方将不再显示在对方的联系人列表中。')) {
            this.send({
                action: 'DELETE_FRIEND',
                params: {
                    friendId: friendId
                }
            });
        }
    }

    // 处理好友关系已解除的通知（对方删除了我）
    handleFriendRemoved(data) {
        if (!data || !data.userId) return;
        
        const userId = data.userId;
        
        // 清除该用户的消息历史
        if (this.messages.has(userId)) {
            this.messages.delete(userId);
        }
        
        // 如果当前正在与该用户聊天，切换到初始状态
        if (this.currentTarget && this.currentTarget.type === 'private' && this.currentTarget.id === userId) {
            this.currentTarget = null;
            this.updateChatHeader();
            this.renderMessages();
        }
        
        // 重新获取好友列表（这会触发handleFriendsList，从后端获取最新列表并更新contacts）
        // handleFriendsList会清空contacts并重新填充，所以会自动移除被删除的好友
        this.getFriends();
        
        // 显示通知
        this.showError(data.message || userId + ' 已解除与您的好友关系');
    }

    // 更新好友申请小红点
    updateFriendRequestBadge(show) {
        const badge = document.getElementById('friendRequestBadge');
        if (badge) {
            if (show === true) {
                badge.style.display = 'block';
            } else if (show === false) {
                badge.style.display = 'none';
            }
            // 如果show未指定，检查当前状态
        }
    }

    // HTML转义
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // 检查是否被禁言
    isMuted() {
        if (!this.currentUser || !this.currentUser.muteEndTime) {
            return false;
        }
        return this.currentUser.muteEndTime > Date.now();
    }

    // 启动禁言状态检查定时器
    startMuteCheck() {
        // 清除之前的定时器
        if (this.muteCheckInterval) {
            clearInterval(this.muteCheckInterval);
        }
        
        // 每秒检查一次禁言状态
        this.muteCheckInterval = setInterval(() => {
            this.updateMuteStatus();
        }, 1000);
    }

    // 更新禁言状态显示
    updateMuteStatus() {
        const messageInput = document.getElementById('messageInput');
        const sendBtn = document.getElementById('sendBtn');
        const inputHint = document.getElementById('inputHint');
        
        if (!messageInput || !sendBtn || !inputHint) {
            return;
        }

        // 只在群聊中检查禁言状态
        if (!this.currentTarget || this.currentTarget.type !== 'group') {
            messageInput.disabled = false;
            sendBtn.disabled = false;
            inputHint.textContent = '';
            return;
        }

        if (this.isMuted()) {
            const remaining = this.currentUser.muteEndTime - Date.now();
            const minutes = Math.floor(remaining / 60000);
            const seconds = Math.floor((remaining % 60000) / 1000);
            
            messageInput.disabled = true;
            sendBtn.disabled = true;
            inputHint.textContent = `您已被禁言！还剩${minutes}分${seconds}秒解除`;
            inputHint.style.color = '#ff4444';
        } else {
            messageInput.disabled = false;
            sendBtn.disabled = false;
            inputHint.textContent = '';
        }
    }

    // 处理群聊禁言系统消息
    handleGroupMuteNotice(data) {
        if (!data || !data.groupId) return;
        
        const groupId = data.groupId;
        const message = {
            msgId: 'system_' + Date.now() + '_' + Math.random(),
            fromUser: '系统',
            toUser: groupId,
            content: data.message || (data.operator + ' 禁言了 ' + data.targetUser + ' ' + (data.durationMinutes || 0) + ' 分钟'),
            timestamp: Date.now(),
            isGroup: true,
            isSystemMessage: true
        };

        // 添加到消息历史
        if (!this.messages.has(groupId)) {
            this.messages.set(groupId, []);
        }
        this.messages.get(groupId).push(message);

        // 如果当前正在查看这个群组，立即渲染
        if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
            this.renderMessages();
        }

        // 如果被禁言的是当前用户，更新禁言状态
        if (data.targetUser && this.currentUser && 
            (this.currentUser.userId === data.targetUser || this.currentUser.username === data.targetUser)) {
            const durationMinutes = data.durationMinutes || 10;
            this.currentUser.muteEndTime = Date.now() + (durationMinutes * 60 * 1000);
            this.updateMuteStatus();
        }
    }

    // 处理群聊踢人系统消息
    handleGroupKickNotice(data) {
        if (!data || !data.groupId) return;
        
        const groupId = data.groupId;
        const message = {
            msgId: 'system_' + Date.now() + '_' + Math.random(),
            fromUser: '系统',
            toUser: groupId,
            content: data.message || (data.operator + ' 将 ' + data.targetUser + ' 踢出了群聊'),
            timestamp: Date.now(),
            isGroup: true,
            isSystemMessage: true
        };

        // 添加到消息历史
        if (!this.messages.has(groupId)) {
            this.messages.set(groupId, []);
        }
        this.messages.get(groupId).push(message);

        // 如果当前正在查看这个群组，立即渲染
        if (this.currentTarget && this.currentTarget.type === 'group' && this.currentTarget.id === groupId) {
            this.renderMessages();
        }
    }
}

// 全局应用实例
let app;

// 登录处理
function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !password) {
        document.getElementById('loginError').textContent = '请输入用户名和密码';
        document.getElementById('loginError').classList.add('show');
        return;
    }

    if (!app) {
        app = new ChatApp();
    }

    // 先连接WebSocket
    app.connect();
    
    // 等待连接建立后发送登录消息
    setTimeout(() => {
        app.send({
            action: 'LOGIN',
            params: {
                username: username,
                password: password
            }
        });
    }, 500);
}

// 创建群组
function createGroup() {
    const groupName = document.getElementById('groupNameInput').value.trim();
    const initialMembersInput = document.getElementById('initialMembersInput').value.trim();

    if (!groupName) {
        alert('请输入群组名称');
        return;
    }

    const params = { groupName: groupName };
    if (initialMembersInput) {
        params.initialMembers = initialMembersInput.split(',').map(s => s.trim()).filter(s => s);
    }

    app.send({
        action: 'CREATE_GROUP',
        params: params
    });
}

// 关闭创建群组弹窗
function closeCreateGroupModal() {
    document.getElementById('createGroupModal').classList.add('hidden');
    document.getElementById('groupNameInput').value = '';
    document.getElementById('initialMembersInput').value = '';
}

// 加入群组
function joinGroup() {
    const groupId = document.getElementById('joinGroupIdInput').value.trim();
    if (!groupId) {
        alert('请输入群组ID');
        return;
    }

    app.send({
        action: 'JOIN_GROUP',
        params: { groupId: groupId }
    });

    closeJoinGroupModal();
}

// 关闭加入群组弹窗
function closeJoinGroupModal() {
    document.getElementById('joinGroupModal').classList.add('hidden');
    document.getElementById('joinGroupIdInput').value = '';
}

// 关闭好友申请弹窗
function closeFriendRequestModal() {
    document.getElementById('friendRequestModal').classList.add('hidden');
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    app = new ChatApp();
});


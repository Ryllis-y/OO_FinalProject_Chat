# Diff Details

Date : 2025-12-21 16:23:10

Directory /Users/ryllis/IdeaProjects/chat

Total : 27 files,  478 codes, 235 comments, -104 blanks, all 609 lines

[Summary](results.md) / [Details](details.md) / [Diff Summary](diff.md) / Diff Details

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [FIX\_JAVA\_VERSION.md](/FIX_JAVA_VERSION.md) | Markdown | -71 | 0 | -35 | -106 |
| [INSTALL\_JAVA21.md](/INSTALL_JAVA21.md) | Markdown | -125 | 0 | -66 | -191 |
| [src/main/java/com/example/chat/handler/action/impl/Accept\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Accept_FriendRequestHandler.java) | Java | 66 | 8 | 17 | 91 |
| [src/main/java/com/example/chat/handler/action/impl/Dissolve\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Dissolve_GroupHandler.java) | Java | 94 | 13 | 22 | 129 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_FriendRequestsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_FriendRequestsHandler.java) | Java | 38 | 5 | 12 | 55 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_FriendsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_FriendsHandler.java) | Java | 69 | 9 | 15 | 93 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_GroupsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_GroupsHandler.java) | Java | 27 | 5 | 4 | 36 |
| [src/main/java/com/example/chat/handler/action/impl/Kick\_GroupUserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Kick_GroupUserHandler.java) | Java | 135 | 21 | 29 | 185 |
| [src/main/java/com/example/chat/handler/action/impl/Mute\_GroupUserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Mute_GroupUserHandler.java) | Java | 91 | 11 | 21 | 123 |
| [src/main/java/com/example/chat/handler/action/impl/Reject\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Reject_FriendRequestHandler.java) | Java | 44 | 4 | 13 | 61 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_FriendRequestHandler.java) | Java | 70 | 8 | 18 | 96 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_PrivateHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_PrivateHandler.java) | Java | -2 | 0 | -1 | -3 |
| [src/main/java/com/example/chat/handler/action/impl/Set\_GroupAdminHandler.java](/src/main/java/com/example/chat/handler/action/impl/Set_GroupAdminHandler.java) | Java | 86 | 13 | 20 | 119 |
| [src/main/java/com/example/chat/service/UserService.java](/src/main/java/com/example/chat/service/UserService.java) | Java | 2 | 1 | 1 | 4 |
| [src/main/java/com/example/chat/service/impl/UserServiceImpl.java](/src/main/java/com/example/chat/service/impl/UserServiceImpl.java) | Java | 54 | 22 | 16 | 92 |
| [src/main/resources/static/app.js](/src/main/resources/static/app.js) | JavaScript | 699 | 121 | 106 | 926 |
| [src/main/resources/static/index.html](/src/main/resources/static/index.html) | HTML | 3 | 0 | 0 | 3 |
| [src/main/resources/static/style.css](/src/main/resources/static/style.css) | PostCSS | 141 | 0 | 26 | 167 |
| [使用文档.md](/%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3.md) | Markdown | 463 | 0 | 151 | 614 |
| [修复在线用户列表问题.md](/%E4%BF%AE%E5%A4%8D%E5%9C%A8%E7%BA%BF%E7%94%A8%E6%88%B7%E5%88%97%E8%A1%A8%E9%97%AE%E9%A2%98.md) | Markdown | -66 | 0 | -31 | -97 |
| [修复说明-在线列表和私聊消息.md](/%E4%BF%AE%E5%A4%8D%E8%AF%B4%E6%98%8E-%E5%9C%A8%E7%BA%BF%E5%88%97%E8%A1%A8%E5%92%8C%E7%A7%81%E8%81%8A%E6%B6%88%E6%81%AF.md) | Markdown | -132 | 0 | -51 | -183 |
| [修复说明-撤回消息显示.md](/%E4%BF%AE%E5%A4%8D%E8%AF%B4%E6%98%8E-%E6%92%A4%E5%9B%9E%E6%B6%88%E6%81%AF%E6%98%BE%E7%A4%BA.md) | Markdown | -123 | 0 | -50 | -173 |
| [修复说明-群组列表和历史消息.md](/%E4%BF%AE%E5%A4%8D%E8%AF%B4%E6%98%8E-%E7%BE%A4%E7%BB%84%E5%88%97%E8%A1%A8%E5%92%8C%E5%8E%86%E5%8F%B2%E6%B6%88%E6%81%AF.md) | Markdown | -195 | 0 | -61 | -256 |
| [前端开发选项.md](/%E5%89%8D%E7%AB%AF%E5%BC%80%E5%8F%91%E9%80%89%E9%A1%B9.md) | Markdown | -133 | 0 | -55 | -188 |
| [完整测试指南.md](/%E5%AE%8C%E6%95%B4%E6%B5%8B%E8%AF%95%E6%8C%87%E5%8D%97.md) | Markdown | -392 | 0 | -151 | -543 |
| [快速测试.sh](/%E5%BF%AB%E9%80%9F%E6%B5%8B%E8%AF%95.sh) | Shell Script | -61 | -6 | -8 | -75 |
| [群聊功能实现说明.md](/%E7%BE%A4%E8%81%8A%E5%8A%9F%E8%83%BD%E5%AE%9E%E7%8E%B0%E8%AF%B4%E6%98%8E.md) | Markdown | -304 | 0 | -66 | -370 |

[Summary](results.md) / [Details](details.md) / [Diff Summary](diff.md) / Diff Details
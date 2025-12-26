# Details

Date : 2025-12-26 21:26:33

Directory /Users/ryllis/IdeaProjects/chat

Total : 78 files,  15119 codes, 1006 comments, 4041 blanks, all 20166 lines

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [.mvn/wrapper/maven-wrapper.properties](/.mvn/wrapper/maven-wrapper.properties) | Java Properties | 3 | 0 | 1 | 4 |
| [APIdoc.md](/APIdoc.md) | Markdown | 230 | 0 | 50 | 280 |
| [README\_RUN.md](/README_RUN.md) | Markdown | 86 | 0 | 35 | 121 |
| [README\_架构图生成.md](/README_%E6%9E%B6%E6%9E%84%E5%9B%BE%E7%94%9F%E6%88%90.md) | Markdown | 57 | 0 | 30 | 87 |
| [User.md](/User.md) | Markdown | 19 | 0 | 18 | 37 |
| [admin权限测试指南.md](/admin%E6%9D%83%E9%99%90%E6%B5%8B%E8%AF%95%E6%8C%87%E5%8D%97.md) | Markdown | 313 | 0 | 110 | 423 |
| [architecture\_mermaid.md](/architecture_mermaid.md) | Markdown | 79 | 0 | 24 | 103 |
| [draw\_architecture.py](/draw_architecture.py) | Python | 132 | 26 | 25 | 183 |
| [mvnw.cmd](/mvnw.cmd) | Batch | 139 | 26 | 25 | 190 |
| [pom.xml](/pom.xml) | XML | 121 | 2 | 5 | 128 |
| [requirements.txt](/requirements.txt) | pip requirements | 2 | 0 | 3 | 5 |
| [src/main/java/com/example/chat/ChatApplication.java](/src/main/java/com/example/chat/ChatApplication.java) | Java | 9 | 0 | 5 | 14 |
| [src/main/java/com/example/chat/common/model/Group.java](/src/main/java/com/example/chat/common/model/Group.java) | Java | 24 | 1 | 8 | 33 |
| [src/main/java/com/example/chat/common/model/Message.java](/src/main/java/com/example/chat/common/model/Message.java) | Java | 26 | 4 | 6 | 36 |
| [src/main/java/com/example/chat/common/model/User.java](/src/main/java/com/example/chat/common/model/User.java) | Java | 29 | 7 | 15 | 51 |
| [src/main/java/com/example/chat/common/packet/WsRequest.java](/src/main/java/com/example/chat/common/packet/WsRequest.java) | Java | 8 | 0 | 2 | 10 |
| [src/main/java/com/example/chat/common/packet/WsResponse.java](/src/main/java/com/example/chat/common/packet/WsResponse.java) | Java | 16 | 1 | 5 | 22 |
| [src/main/java/com/example/chat/config/AppConfig.java](/src/main/java/com/example/chat/config/AppConfig.java) | Java | 25 | 7 | 6 | 38 |
| [src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java](/src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java) | Java | 28 | 12 | 9 | 49 |
| [src/main/java/com/example/chat/config/WebSocketConfig.java](/src/main/java/com/example/chat/config/WebSocketConfig.java) | Java | 19 | 2 | 4 | 25 |
| [src/main/java/com/example/chat/handler/ChatHandler.java](/src/main/java/com/example/chat/handler/ChatHandler.java) | Java | 109 | 29 | 19 | 157 |
| [src/main/java/com/example/chat/handler/HandlerRegistry.java](/src/main/java/com/example/chat/handler/HandlerRegistry.java) | Java | 37 | 26 | 10 | 73 |
| [src/main/java/com/example/chat/handler/action/ActionHandler.java](/src/main/java/com/example/chat/handler/action/ActionHandler.java) | Java | 6 | 8 | 1 | 15 |
| [src/main/java/com/example/chat/handler/action/BaseActionHandler.java](/src/main/java/com/example/chat/handler/action/BaseActionHandler.java) | Java | 40 | 21 | 11 | 72 |
| [src/main/java/com/example/chat/handler/action/impl/Accept\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Accept_FriendRequestHandler.java) | Java | 66 | 8 | 19 | 93 |
| [src/main/java/com/example/chat/handler/action/impl/Create\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Create_GroupHandler.java) | Java | 89 | 13 | 20 | 122 |
| [src/main/java/com/example/chat/handler/action/impl/Delete\_FriendHandler.java](/src/main/java/com/example/chat/handler/action/impl/Delete_FriendHandler.java) | Java | 89 | 13 | 22 | 124 |
| [src/main/java/com/example/chat/handler/action/impl/Dissolve\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Dissolve_GroupHandler.java) | Java | 94 | 13 | 22 | 129 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_FriendRequestsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_FriendRequestsHandler.java) | Java | 38 | 5 | 14 | 57 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_FriendsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_FriendsHandler.java) | Java | 69 | 9 | 15 | 93 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_GroupMembersHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_GroupMembersHandler.java) | Java | 58 | 7 | 15 | 80 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_GroupsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_GroupsHandler.java) | Java | 62 | 10 | 16 | 88 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_HistoryHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_HistoryHandler.java) | Java | 92 | 14 | 16 | 122 |
| [src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java](/src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java) | Java | 17 | 4 | 4 | 25 |
| [src/main/java/com/example/chat/handler/action/impl/Join\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Join_GroupHandler.java) | Java | 56 | 7 | 15 | 78 |
| [src/main/java/com/example/chat/handler/action/impl/Kick\_GroupUserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Kick_GroupUserHandler.java) | Java | 135 | 21 | 29 | 185 |
| [src/main/java/com/example/chat/handler/action/impl/Kick\_UserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Kick_UserHandler.java) | Java | 89 | 14 | 21 | 124 |
| [src/main/java/com/example/chat/handler/action/impl/Leave\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Leave_GroupHandler.java) | Java | 90 | 15 | 22 | 127 |
| [src/main/java/com/example/chat/handler/action/impl/LoginHandler.java](/src/main/java/com/example/chat/handler/action/impl/LoginHandler.java) | Java | 108 | 28 | 29 | 165 |
| [src/main/java/com/example/chat/handler/action/impl/Msg\_ReactHandler.java](/src/main/java/com/example/chat/handler/action/impl/Msg_ReactHandler.java) | Java | 168 | 53 | 44 | 265 |
| [src/main/java/com/example/chat/handler/action/impl/Mute\_GroupUserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Mute_GroupUserHandler.java) | Java | 91 | 11 | 21 | 123 |
| [src/main/java/com/example/chat/handler/action/impl/Mute\_UserHandler.java](/src/main/java/com/example/chat/handler/action/impl/Mute_UserHandler.java) | Java | 86 | 12 | 22 | 120 |
| [src/main/java/com/example/chat/handler/action/impl/Recall\_MsgHandler.java](/src/main/java/com/example/chat/handler/action/impl/Recall_MsgHandler.java) | Java | 147 | 48 | 39 | 234 |
| [src/main/java/com/example/chat/handler/action/impl/Reject\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Reject_FriendRequestHandler.java) | Java | 44 | 4 | 15 | 63 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_FriendRequestHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_FriendRequestHandler.java) | Java | 70 | 8 | 20 | 98 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_GroupHandler.java) | Java | 97 | 12 | 22 | 131 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_PrivateHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_PrivateHandler.java) | Java | 73 | 12 | 18 | 103 |
| [src/main/java/com/example/chat/handler/action/impl/Set\_GroupAdminHandler.java](/src/main/java/com/example/chat/handler/action/impl/Set_GroupAdminHandler.java) | Java | 86 | 13 | 22 | 121 |
| [src/main/java/com/example/chat/repository/DataCenter.java](/src/main/java/com/example/chat/repository/DataCenter.java) | Java | 13 | 4 | 6 | 23 |
| [src/main/java/com/example/chat/service/MessageService.java](/src/main/java/com/example/chat/service/MessageService.java) | Java | 6 | 1 | 2 | 9 |
| [src/main/java/com/example/chat/service/UserService.java](/src/main/java/com/example/chat/service/UserService.java) | Java | 27 | 5 | 13 | 45 |
| [src/main/java/com/example/chat/service/impl/MessageServiceImpl.java](/src/main/java/com/example/chat/service/impl/MessageServiceImpl.java) | Java | 70 | 18 | 23 | 111 |
| [src/main/java/com/example/chat/service/impl/UserServiceImpl.java](/src/main/java/com/example/chat/service/impl/UserServiceImpl.java) | Java | 338 | 44 | 75 | 457 |
| [src/main/java/com/example/chat/util/SensitiveWordFilter.java](/src/main/java/com/example/chat/util/SensitiveWordFilter.java) | Java | 123 | 43 | 32 | 198 |
| [src/main/resources/application.properties](/src/main/resources/application.properties) | Java Properties | 4 | 5 | 5 | 14 |
| [src/main/resources/static/app.js](/src/main/resources/static/app.js) | JavaScript | 1,929 | 310 | 269 | 2,508 |
| [src/main/resources/static/index.html](/src/main/resources/static/index.html) | HTML | 155 | 21 | 16 | 192 |
| [src/main/resources/static/style.css](/src/main/resources/static/style.css) | PostCSS | 883 | 14 | 156 | 1,053 |
| [src/test/java/com/example/chat/ChatApplicationTests.java](/src/test/java/com/example/chat/ChatApplicationTests.java) | Java | 9 | 0 | 5 | 14 |
| [test-client.html](/test-client.html) | HTML | 294 | 0 | 31 | 325 |
| [websocket-json-tests.md](/websocket-json-tests.md) | Markdown | 504 | 0 | 102 | 606 |
| [代码职责划分说明.md](/%E4%BB%A3%E7%A0%81%E8%81%8C%E8%B4%A3%E5%88%92%E5%88%86%E8%AF%B4%E6%98%8E.md) | Markdown | 275 | 2 | 92 | 369 |
| [使用文档.md](/%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3.md) | Markdown | 463 | 0 | 151 | 614 |
| [前端使用说明.md](/%E5%89%8D%E7%AB%AF%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.md) | Markdown | 118 | 0 | 39 | 157 |
| [功能分析与测试指南.md](/%E5%8A%9F%E8%83%BD%E5%88%86%E6%9E%90%E4%B8%8E%E6%B5%8B%E8%AF%95%E6%8C%87%E5%8D%97.md) | Markdown | 486 | 0 | 124 | 610 |
| [多电脑连接指南.md](/%E5%A4%9A%E7%94%B5%E8%84%91%E8%BF%9E%E6%8E%A5%E6%8C%87%E5%8D%97.md) | Markdown | 346 | 4 | 129 | 479 |
| [异常处理机制说明.md](/%E5%BC%82%E5%B8%B8%E5%A4%84%E7%90%86%E6%9C%BA%E5%88%B6%E8%AF%B4%E6%98%8E.md) | Markdown | 442 | 0 | 122 | 564 |
| [敏感词过滤功能说明.md](/%E6%95%8F%E6%84%9F%E8%AF%8D%E8%BF%87%E6%BB%A4%E5%8A%9F%E8%83%BD%E8%AF%B4%E6%98%8E.md) | Markdown | 286 | 0 | 88 | 374 |
| [现场展示操作步骤.md](/%E7%8E%B0%E5%9C%BA%E5%B1%95%E7%A4%BA%E6%93%8D%E4%BD%9C%E6%AD%A5%E9%AA%A4.md) | Markdown | 373 | 0 | 166 | 539 |
| [答辩大纲.md](/%E7%AD%94%E8%BE%A9%E5%A4%A7%E7%BA%B2.md) | Markdown | 187 | 0 | 64 | 251 |
| [答辩展示文档.md](/%E7%AD%94%E8%BE%A9%E5%B1%95%E7%A4%BA%E6%96%87%E6%A1%A3.md) | Markdown | 350 | 0 | 119 | 469 |
| [答辩问题与回答-精简版.md](/%E7%AD%94%E8%BE%A9%E9%97%AE%E9%A2%98%E4%B8%8E%E5%9B%9E%E7%AD%94-%E7%B2%BE%E7%AE%80%E7%89%88.md) | Markdown | 457 | 0 | 188 | 645 |
| [答辩问题与回答.md](/%E7%AD%94%E8%BE%A9%E9%97%AE%E9%A2%98%E4%B8%8E%E5%9B%9E%E7%AD%94.md) | Markdown | 701 | 0 | 267 | 968 |
| [群聊功能完整测试步骤.md](/%E7%BE%A4%E8%81%8A%E5%8A%9F%E8%83%BD%E5%AE%8C%E6%95%B4%E6%B5%8B%E8%AF%95%E6%AD%A5%E9%AA%A4.md) | Markdown | 468 | 0 | 134 | 602 |
| [设计模式说明.md](/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F%E8%AF%B4%E6%98%8E.md) | Markdown | 404 | 0 | 115 | 519 |
| [面向对象大作业开发文档.md](/%E9%9D%A2%E5%90%91%E5%AF%B9%E8%B1%A1%E5%A4%A7%E4%BD%9C%E4%B8%9A%E5%BC%80%E5%8F%91%E6%96%87%E6%A1%A3.md) | Markdown | 1,114 | 0 | 378 | 1,492 |
| [项目结构设计图.md](/%E9%A1%B9%E7%9B%AE%E7%BB%93%E6%9E%84%E8%AE%BE%E8%AE%A1%E5%9B%BE.md) | Markdown | 219 | 0 | 25 | 244 |
| [项目配置详解.md](/%E9%A1%B9%E7%9B%AE%E9%85%8D%E7%BD%AE%E8%AF%A6%E8%A7%A3.md) | Markdown | 504 | 9 | 176 | 689 |

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)
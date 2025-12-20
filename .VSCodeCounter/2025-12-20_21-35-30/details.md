# Details

Date : 2025-12-20 21:35:30

Directory /Users/ryllis/IdeaProjects/chat

Total : 47 files,  4910 codes, 468 comments, 1251 blanks, all 6629 lines

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [.mvn/wrapper/maven-wrapper.properties](/.mvn/wrapper/maven-wrapper.properties) | Java Properties | 3 | 0 | 1 | 4 |
| [APIdoc.md](/APIdoc.md) | Markdown | 230 | 0 | 50 | 280 |
| [FIX\_JAVA\_VERSION.md](/FIX_JAVA_VERSION.md) | Markdown | 71 | 0 | 35 | 106 |
| [FIX\_LOMBOK\_ERROR.md](/FIX_LOMBOK_ERROR.md) | Markdown | 96 | 0 | 44 | 140 |
| [INSTALL\_JAVA21.md](/INSTALL_JAVA21.md) | Markdown | 125 | 0 | 66 | 191 |
| [README\_RUN.md](/README_RUN.md) | Markdown | 86 | 0 | 35 | 121 |
| [User.md](/User.md) | Markdown | 19 | 0 | 18 | 37 |
| [mvnw.cmd](/mvnw.cmd) | Batch | 139 | 26 | 25 | 190 |
| [pom.xml](/pom.xml) | XML | 112 | 2 | 5 | 119 |
| [src/main/java/com/example/chat/ChatApplication.java](/src/main/java/com/example/chat/ChatApplication.java) | Java | 9 | 0 | 5 | 14 |
| [src/main/java/com/example/chat/common/model/Group.java](/src/main/java/com/example/chat/common/model/Group.java) | Java | 24 | 1 | 8 | 33 |
| [src/main/java/com/example/chat/common/model/Message.java](/src/main/java/com/example/chat/common/model/Message.java) | Java | 24 | 4 | 6 | 34 |
| [src/main/java/com/example/chat/common/model/User.java](/src/main/java/com/example/chat/common/model/User.java) | Java | 29 | 7 | 15 | 51 |
| [src/main/java/com/example/chat/common/packet/WsRequest.java](/src/main/java/com/example/chat/common/packet/WsRequest.java) | Java | 8 | 0 | 2 | 10 |
| [src/main/java/com/example/chat/common/packet/WsResponse.java](/src/main/java/com/example/chat/common/packet/WsResponse.java) | Java | 16 | 1 | 5 | 22 |
| [src/main/java/com/example/chat/config/AppConfig.java](/src/main/java/com/example/chat/config/AppConfig.java) | Java | 25 | 7 | 6 | 38 |
| [src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java](/src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java) | Java | 28 | 12 | 9 | 49 |
| [src/main/java/com/example/chat/config/WebSocketConfig.java](/src/main/java/com/example/chat/config/WebSocketConfig.java) | Java | 19 | 2 | 4 | 25 |
| [src/main/java/com/example/chat/handler/ChatHandler.java](/src/main/java/com/example/chat/handler/ChatHandler.java) | Java | 359 | 100 | 72 | 531 |
| [src/main/java/com/example/chat/handler/HandlerRegistry.java](/src/main/java/com/example/chat/handler/HandlerRegistry.java) | Java | 37 | 26 | 10 | 73 |
| [src/main/java/com/example/chat/handler/action/ActionHandler.java](/src/main/java/com/example/chat/handler/action/ActionHandler.java) | Java | 6 | 8 | 1 | 15 |
| [src/main/java/com/example/chat/handler/action/BaseActionHandler.java](/src/main/java/com/example/chat/handler/action/BaseActionHandler.java) | Java | 40 | 21 | 11 | 72 |
| [src/main/java/com/example/chat/handler/action/impl/Create\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Create_GroupHandler.java) | Java | 59 | 6 | 16 | 81 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_GroupMembersHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_GroupMembersHandler.java) | Java | 54 | 6 | 15 | 75 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_GroupsHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_GroupsHandler.java) | Java | 35 | 5 | 12 | 52 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_HistoryHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_HistoryHandler.java) | Java | 64 | 9 | 15 | 88 |
| [src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java](/src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java) | Java | 17 | 4 | 4 | 25 |
| [src/main/java/com/example/chat/handler/action/impl/Join\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Join_GroupHandler.java) | Java | 51 | 6 | 13 | 70 |
| [src/main/java/com/example/chat/handler/action/impl/Leave\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Leave_GroupHandler.java) | Java | 59 | 8 | 16 | 83 |
| [src/main/java/com/example/chat/handler/action/impl/LoginHandler.java](/src/main/java/com/example/chat/handler/action/impl/LoginHandler.java) | Java | 76 | 21 | 22 | 119 |
| [src/main/java/com/example/chat/handler/action/impl/Msg\_ReactHandler.java](/src/main/java/com/example/chat/handler/action/impl/Msg_ReactHandler.java) | Java | 168 | 53 | 44 | 265 |
| [src/main/java/com/example/chat/handler/action/impl/Msg\_ReadHandler.java](/src/main/java/com/example/chat/handler/action/impl/Msg_ReadHandler.java) | Java | 77 | 14 | 22 | 113 |
| [src/main/java/com/example/chat/handler/action/impl/Recall\_MsgHandler.java](/src/main/java/com/example/chat/handler/action/impl/Recall_MsgHandler.java) | Java | 151 | 48 | 41 | 240 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_GroupHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_GroupHandler.java) | Java | 94 | 13 | 23 | 130 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_PrivateHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_PrivateHandler.java) | Java | 74 | 11 | 18 | 103 |
| [src/main/java/com/example/chat/repository/DataCenter.java](/src/main/java/com/example/chat/repository/DataCenter.java) | Java | 13 | 4 | 6 | 23 |
| [src/main/java/com/example/chat/service/MessageService.java](/src/main/java/com/example/chat/service/MessageService.java) | Java | 6 | 1 | 2 | 9 |
| [src/main/java/com/example/chat/service/UserService.java](/src/main/java/com/example/chat/service/UserService.java) | Java | 24 | 3 | 11 | 38 |
| [src/main/java/com/example/chat/service/impl/MessageServiceImpl.java](/src/main/java/com/example/chat/service/impl/MessageServiceImpl.java) | Java | 60 | 16 | 20 | 96 |
| [src/main/java/com/example/chat/service/impl/UserServiceImpl.java](/src/main/java/com/example/chat/service/impl/UserServiceImpl.java) | Java | 254 | 18 | 51 | 323 |
| [src/main/resources/application.properties](/src/main/resources/application.properties) | Java Properties | 4 | 5 | 5 | 14 |
| [src/test/java/com/example/chat/ChatApplicationTests.java](/src/test/java/com/example/chat/ChatApplicationTests.java) | Java | 9 | 0 | 5 | 14 |
| [test-client.html](/test-client.html) | HTML | 294 | 0 | 31 | 325 |
| [websocket-json-tests.md](/websocket-json-tests.md) | Markdown | 504 | 0 | 102 | 606 |
| [功能分析与测试指南.md](/%E5%8A%9F%E8%83%BD%E5%88%86%E6%9E%90%E4%B8%8E%E6%B5%8B%E8%AF%95%E6%8C%87%E5%8D%97.md) | Markdown | 486 | 0 | 124 | 610 |
| [群聊功能完整测试步骤.md](/%E7%BE%A4%E8%81%8A%E5%8A%9F%E8%83%BD%E5%AE%8C%E6%95%B4%E6%B5%8B%E8%AF%95%E6%AD%A5%E9%AA%A4.md) | Markdown | 468 | 0 | 134 | 602 |
| [群聊功能实现说明.md](/%E7%BE%A4%E8%81%8A%E5%8A%9F%E8%83%BD%E5%AE%9E%E7%8E%B0%E8%AF%B4%E6%98%8E.md) | Markdown | 304 | 0 | 66 | 370 |

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)
# Details

Date : 2025-12-20 16:50:39

Directory /Users/ryllis/IdeaProjects/chat

Total : 32 files,  2083 codes, 426 comments, 504 blanks, all 3013 lines

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [.mvn/wrapper/maven-wrapper.properties](/.mvn/wrapper/maven-wrapper.properties) | Java Properties | 3 | 0 | 1 | 4 |
| [APIdoc.md](/APIdoc.md) | Markdown | 230 | 0 | 50 | 280 |
| [User.md](/User.md) | Markdown | 19 | 0 | 18 | 37 |
| [mvnw.cmd](/mvnw.cmd) | Batch | 139 | 26 | 25 | 190 |
| [pom.xml](/pom.xml) | XML | 114 | 2 | 5 | 121 |
| [src/main/java/com/example/chat/ChatApplication.java](/src/main/java/com/example/chat/ChatApplication.java) | Java | 9 | 0 | 5 | 14 |
| [src/main/java/com/example/chat/common/model/Group.java](/src/main/java/com/example/chat/common/model/Group.java) | Java | 24 | 1 | 8 | 33 |
| [src/main/java/com/example/chat/common/model/Message.java](/src/main/java/com/example/chat/common/model/Message.java) | Java | 24 | 4 | 6 | 34 |
| [src/main/java/com/example/chat/common/model/User.java](/src/main/java/com/example/chat/common/model/User.java) | Java | 29 | 7 | 15 | 51 |
| [src/main/java/com/example/chat/common/packet/WsRequest.java](/src/main/java/com/example/chat/common/packet/WsRequest.java) | Java | 8 | 0 | 2 | 10 |
| [src/main/java/com/example/chat/common/packet/WsResponse.java](/src/main/java/com/example/chat/common/packet/WsResponse.java) | Java | 16 | 1 | 3 | 20 |
| [src/main/java/com/example/chat/config/AppConfig.java](/src/main/java/com/example/chat/config/AppConfig.java) | Java | 25 | 7 | 6 | 38 |
| [src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java](/src/main/java/com/example/chat/config/AuthHandshakeInterceptor.java) | Java | 28 | 12 | 9 | 49 |
| [src/main/java/com/example/chat/config/WebSocketConfig.java](/src/main/java/com/example/chat/config/WebSocketConfig.java) | Java | 19 | 2 | 4 | 25 |
| [src/main/java/com/example/chat/handler/ChatHandler.java](/src/main/java/com/example/chat/handler/ChatHandler.java) | Java | 365 | 114 | 72 | 551 |
| [src/main/java/com/example/chat/handler/HandlerRegistry.java](/src/main/java/com/example/chat/handler/HandlerRegistry.java) | Java | 32 | 23 | 10 | 65 |
| [src/main/java/com/example/chat/handler/action/ActionHandler.java](/src/main/java/com/example/chat/handler/action/ActionHandler.java) | Java | 6 | 8 | 1 | 15 |
| [src/main/java/com/example/chat/handler/action/BaseActionHandler.java](/src/main/java/com/example/chat/handler/action/BaseActionHandler.java) | Java | 40 | 21 | 11 | 72 |
| [src/main/java/com/example/chat/handler/action/impl/Get\_HistoryHandler.java](/src/main/java/com/example/chat/handler/action/impl/Get_HistoryHandler.java) | Java | 64 | 9 | 15 | 88 |
| [src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java](/src/main/java/com/example/chat/handler/action/impl/HeartbeatHandler.java) | Java | 17 | 4 | 4 | 25 |
| [src/main/java/com/example/chat/handler/action/impl/LoginHandler.java](/src/main/java/com/example/chat/handler/action/impl/LoginHandler.java) | Java | 75 | 19 | 23 | 117 |
| [src/main/java/com/example/chat/handler/action/impl/Msg\_ReactHandler.java](/src/main/java/com/example/chat/handler/action/impl/Msg_ReactHandler.java) | Java | 168 | 53 | 44 | 265 |
| [src/main/java/com/example/chat/handler/action/impl/Msg\_ReadHandler.java](/src/main/java/com/example/chat/handler/action/impl/Msg_ReadHandler.java) | Java | 77 | 14 | 22 | 113 |
| [src/main/java/com/example/chat/handler/action/impl/Recall\_MsgHandler.java](/src/main/java/com/example/chat/handler/action/impl/Recall_MsgHandler.java) | Java | 151 | 48 | 41 | 240 |
| [src/main/java/com/example/chat/handler/action/impl/Send\_PrivateHandler.java](/src/main/java/com/example/chat/handler/action/impl/Send_PrivateHandler.java) | Java | 74 | 11 | 18 | 103 |
| [src/main/java/com/example/chat/repository/DataCenter.java](/src/main/java/com/example/chat/repository/DataCenter.java) | Java | 13 | 4 | 6 | 23 |
| [src/main/java/com/example/chat/service/MessageService.java](/src/main/java/com/example/chat/service/MessageService.java) | Java | 6 | 1 | 2 | 9 |
| [src/main/java/com/example/chat/service/UserService.java](/src/main/java/com/example/chat/service/UserService.java) | Java | 21 | 2 | 10 | 33 |
| [src/main/java/com/example/chat/service/impl/MessageServiceImpl.java](/src/main/java/com/example/chat/service/impl/MessageServiceImpl.java) | Java | 60 | 16 | 20 | 96 |
| [src/main/java/com/example/chat/service/impl/UserServiceImpl.java](/src/main/java/com/example/chat/service/impl/UserServiceImpl.java) | Java | 214 | 12 | 38 | 264 |
| [src/main/resources/application.properties](/src/main/resources/application.properties) | Java Properties | 4 | 5 | 5 | 14 |
| [src/test/java/com/example/chat/ChatApplicationTests.java](/src/test/java/com/example/chat/ChatApplicationTests.java) | Java | 9 | 0 | 5 | 14 |

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)
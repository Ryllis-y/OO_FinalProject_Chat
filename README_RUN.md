# 如何运行 Spring Boot 项目

## ⚠️ 重要提示

**不能直接用 `javac` 编译单个 Java 文件！**

Spring Boot 项目需要：
- 管理所有依赖（Maven/Gradle）
- 编译整个项目
- 加载 Spring 配置和自动配置

## ✅ 正确的运行方式

### 方式 1: 使用 VSCode 任务（最简单）

1. 按 `Cmd+Shift+P` (Mac) 或 `Ctrl+Shift+P` (Windows/Linux)
2. 输入 "Tasks: Run Task"
3. 选择 "Maven: Spring Boot Run"
4. 或者直接按 `Cmd+Shift+B` (Mac) 或 `Ctrl+Shift+B` (Windows/Linux)

### 方式 2: 使用终端命令

在项目根目录 `/Users/ryllis/IdeaProjects/chat` 运行：

```bash
# 运行项目
./mvnw spring-boot:run

# 或者如果已安装 Maven
mvn spring-boot:run
```

### 方式 3: 使用 VSCode Java 扩展（推荐）

1. 安装扩展：
   - Java Extension Pack (必装)
   - Spring Boot Extension Pack (推荐)

2. 打开 `src/main/java/com/example/chat/ChatApplication.java`

3. 点击 `main` 方法上方的运行按钮，或按 `F5`

### 方式 4: 打包后运行

```bash
# 1. 打包项目
./mvnw clean package

# 2. 运行生成的 jar 文件
java -jar target/chat-0.0.1-SNAPSHOT.jar
```

## 📝 为什么不能直接用 javac？

当你在终端运行：
```bash
javac ChatApplication.java
```

会遇到以下问题：
- ❌ 找不到 Spring Boot 依赖包
- ❌ 找不到项目中的其他类（如 Message, User 等）
- ❌ 无法加载 Spring 配置
- ❌ 无法启动 Spring Boot 应用上下文

## 🛠️ 项目结构

```
chat/
├── pom.xml              # Maven 配置文件（包含所有依赖）
├── src/
│   └── main/
│       └── java/
│           └── com/example/chat/
│               └── ChatApplication.java  # 主入口
└── target/              # 编译输出目录（Maven 自动生成）
```

## 🔍 检查 Java 版本

确保你使用的是 Java 24：

```bash
java -version
# 应该显示：java version "24"
```

## 🐛 常见问题

### 问题：找不到 Maven
如果 `./mvnw` 不起作用，尝试：
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### 问题：端口被占用
如果 8080 端口被占用，修改 `src/main/resources/application.properties`：
```properties
server.port=8081
```

## ✅ 成功运行的标志

当你看到类似以下输出时，说明运行成功：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.0)

... (更多日志)

Started ChatApplication in X.XXX seconds
```


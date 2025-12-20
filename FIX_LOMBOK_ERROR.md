# 修复 Lombok 编译错误

## 问题分析

从错误信息看，有两个主要问题：

1. **找不到 Lombok 生成的方法**（如 `builder()`, `getAction()`, `getParams()` 等）
   - 这表示 Lombok 注解处理器在编译时没有正确运行

2. **`TypeTag :: UNKNOWN` 错误**
   - 这表示 Lombok 与 Java 编译器版本不兼容
   - 通常发生在 Java 版本不匹配时

## 根本原因

**系统实际使用的 Java 版本可能与项目配置不一致。**

项目配置为 Java 21，但如果系统默认使用 Java 24，会导致：
- Lombok 无法正确工作
- 编译器版本不匹配

## 解决方案

### 方案 1: 检查并设置正确的 Java 版本（推荐）

1. **检查当前 Java 版本**：
   ```bash
   java -version
   javac -version
   ```

2. **检查 JAVA_HOME**：
   ```bash
   echo $JAVA_HOME
   ```

3. **如果显示的是 Java 24，需要切换到 Java 21**：

   **macOS (使用 Homebrew)**：
   ```bash
   # 安装 Java 21（如果还没有）
   brew install openjdk@21
   
   # 设置 JAVA_HOME
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   
   # 验证
   java -version  # 应该显示 java version "21.x.x"
   ```

   **macOS (手动设置)**：
   ```bash
   # 查找 Java 21 路径
   /usr/libexec/java_home -V
   
   # 设置 JAVA_HOME（替换为实际路径）
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
   ```

4. **永久设置（添加到 `~/.zshrc` 或 `~/.bash_profile`）**：
   ```bash
   echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
   source ~/.zshrc
   ```

5. **清理并重新编译**：
   ```bash
   ./mvnw clean compile
   ```

### 方案 2: 如果必须在 IntelliJ IDEA 中运行

1. **打开项目设置**：`File` → `Project Structure` → `Project`
   - 确保 `SDK` 设置为 Java 21
   - 确保 `Language Level` 设置为 `21`

2. **检查模块设置**：`File` → `Project Structure` → `Modules`
   - 确保 `Language Level` 设置为 `21`

3. **启用注解处理**：`File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - ✅ 勾选 `Enable annotation processing`

4. **重新加载 Maven 项目**：右键 `pom.xml` → `Maven` → `Reload Project`

5. **清理并重新构建**：`Build` → `Rebuild Project`

### 方案 3: 如果必须在 VSCode 中运行

1. **安装 Java Extension Pack**（如果还没有）
2. **设置 Java 版本**：
   - 按 `Cmd+Shift+P` (Mac) 或 `Ctrl+Shift+P` (Windows/Linux)
   - 输入 "Java: Configure Java Runtime"
   - 选择 Java 21

3. **清理并重新编译**：
   ```bash
   ./mvnw clean compile
   ```

## 验证修复

运行以下命令验证：

```bash
# 1. 检查 Java 版本
java -version

# 2. 检查 Maven 使用的 Java 版本
./mvnw -version

# 3. 清理并编译
./mvnw clean compile

# 4. 如果编译成功，运行项目
./mvnw spring-boot:run
```

如果编译成功，应该不会再看到 `找不到符号` 或 `TypeTag :: UNKNOWN` 错误。

## 常见问题

### Q: 为什么不能使用 Java 24？

A: Spring Boot 3.3.0 虽然支持 Java 21+，但：
- Lombok 1.18.30（Spring Boot 3.3.0 默认版本）可能不完全支持 Java 24
- Java 24 太新，很多工具链还不完全兼容
- Java 21 是 LTS 版本，更稳定可靠

### Q: 如何检查项目实际使用的 Java 版本？

A: 运行 `./mvnw -version`，查看 "Java version" 那一行。

### Q: 我已经设置了 JAVA_HOME，但 Maven 还是使用错误的版本？

A: 确保：
1. 终端中 `echo $JAVA_HOME` 显示正确的路径
2. `java -version` 显示正确的版本
3. 重启终端或 IDE


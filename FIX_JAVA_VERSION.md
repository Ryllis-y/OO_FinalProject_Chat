# 修复 Java 版本切换问题

## 问题原因

你只设置了 `JAVA_HOME`，但没有更新 `PATH`。

系统仍然在使用 `/usr/bin/java`（Java 24），因为 `/usr/bin` 在 PATH 中，而 Java 21 的 bin 目录不在 PATH 中。

## 解决方案

### 在当前终端中临时切换（立即生效）

运行以下命令：

```bash
# 1. 设置 JAVA_HOME（使用完整路径）
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

# 2. 将 Java 21 的 bin 目录添加到 PATH 的最前面（关键步骤！）
export PATH="$JAVA_HOME/bin:$PATH"

# 3. 验证
java -version
# 应该显示：openjdk version "21.x.x"

# 4. 验证 javac
javac -version
# 应该显示：javac 21.x.x
```

### 永久设置（推荐）

编辑 `~/.zshrc` 文件：

```bash
vim ~/.zshrc
```

在文件末尾添加：

```bash
# Java 21 配置
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
```

保存后运行：

```bash
source ~/.zshrc
java -version
```

### 验证所有设置

```bash
# 1. 检查 Java 版本
java -version
# 应该显示：openjdk version "21.x.x"

# 2. 检查 javac 版本
javac -version
# 应该显示：javac 21.x.x

# 3. 检查 JAVA_HOME
echo $JAVA_HOME
# 应该显示：/opt/homebrew/opt/openjdk@21

# 4. 检查 which java
which java
# 应该显示：/opt/homebrew/opt/openjdk@21/bin/java

# 5. 检查 Maven 使用的 Java 版本
./mvnw -version
# 查看 "Java version" 行，应该显示 21.x.x

# 6. 清理并编译项目
./mvnw clean compile
```

## 关键点

**重要**：必须同时设置 `JAVA_HOME` 和 `PATH`！

- `JAVA_HOME`：告诉工具（如 Maven）Java 的安装位置
- `PATH`：告诉系统在哪里找到 `java` 和 `javac` 命令

只设置 `JAVA_HOME` 是不够的，因为 `java` 命令仍然会使用 PATH 中第一个找到的 Java（通常是 `/usr/bin/java`）。

## 如果还是不行

如果设置后 `java -version` 仍然显示 Java 24，检查：

```bash
# 1. 检查 PATH 中 Java 21 是否在最前面
echo $PATH | grep openjdk@21

# 2. 检查是否有其他 Java 在 PATH 前面
echo $PATH

# 3. 直接测试 Java 21 的路径
/opt/homebrew/opt/openjdk@21/bin/java -version
```

如果 Java 21 的 bin 目录不在 PATH 最前面，系统会优先使用 PATH 中更早的 Java。

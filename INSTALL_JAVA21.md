# 安装 Java 21 指南

## 快速安装（使用 Homebrew）

由于你已经安装了 Homebrew，安装 Java 21 非常简单：

### 1. 安装 Java 21

在终端中运行：

```bash
brew install openjdk@21
```

### 2. 设置 JAVA_HOME（临时，仅当前终端会话）

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

如果上面命令失败（说明 Java 21 路径还没注册），可以手动设置：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
```

### 3. 更新 PATH（临时）

```bash
export PATH="$JAVA_HOME/bin:$PATH"
```

### 4. 验证安装

```bash
java -version
```

应该显示：
```
openjdk version "21.x.x" ...
```

### 5. 验证 Maven 使用的 Java 版本

```bash
./mvnw -version
```

查看 "Java version" 那一行，应该显示 Java 21。

### 6. 清理并重新编译项目

```bash
./mvnw clean compile
```

如果编译成功，运行项目：

```bash
./mvnw spring-boot:run
```

---

## 永久设置（可选）

如果想让 Java 21 成为默认版本，可以将其添加到 shell 配置文件：

### 对于 zsh（macOS 默认）

```bash
# 编辑 ~/.zshrc
nano ~/.zshrc

# 添加以下内容：
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# 保存并重新加载
source ~/.zshrc
```

### 对于 bash

```bash
# 编辑 ~/.bash_profile
nano ~/.bash_profile

# 添加以下内容：
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# 保存并重新加载
source ~/.bash_profile
```

---

## 如果 Homebrew 安装失败

### 方法 1: 使用 SDKMAN（推荐）

```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash

# 重新打开终端或运行
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 安装 Java 21
sdk install java 21-open

# 设置为默认
sdk default java 21-open
```

### 方法 2: 从 Oracle/OpenJDK 官网下载

1. 访问：https://jdk.java.net/21/
2. 下载 macOS 版本（.dmg 文件）
3. 安装并按照提示设置 JAVA_HOME

### 方法 3: 使用 Azul Zulu（企业级，免费）

```bash
brew install --cask zulu@21
```

然后设置：
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
```

---

## 验证所有设置

运行以下命令确认一切正常：

```bash
# 1. 检查 Java 版本
java -version

# 2. 检查 JAVA_HOME
echo $JAVA_HOME

# 3. 检查 javac 版本
javac -version

# 4. 检查 Maven 使用的 Java 版本
./mvnw -version

# 5. 清理并编译项目
./mvnw clean compile
```

---

## 常见问题

### Q: 安装后 `java -version` 仍然显示 Java 24？

A: 确保：
1. `JAVA_HOME` 指向 Java 21 的路径
2. `PATH` 中 `$JAVA_HOME/bin` 在最前面
3. 重新打开终端或运行 `source ~/.zshrc`

### Q: `/usr/libexec/java_home -v 21` 找不到？

A: 可能需要先运行：
```bash
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

或者直接使用完整路径设置 `JAVA_HOME`。

### Q: 安装需要多长时间？

A: 通常 1-5 分钟，取决于网络速度。

---

## 下一步

安装完 Java 21 并设置好后，请：
1. 验证 `java -version` 显示 Java 21
2. 运行 `./mvnw clean compile` 编译项目
3. 如果编译成功，运行 `./mvnw spring-boot:run` 启动项目


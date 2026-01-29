# 建议的开发命令

## Maven 构建命令

### 编译项目
```bash
mvn clean compile
```
清理并编译整个多模块项目

### 打包项目
```bash
mvn clean package
```
编译、测试并打包应用为 JAR 文件

### 跳过测试打包
```bash
mvn clean package -DskipTests
```

### 安装到本地仓库
```bash
mvn clean install
```
用于多模块项目,将模块安装到本地 Maven 仓库

## 运行应用

### 运行主论坛应用 (web-app)
```bash
cd web/web-app
mvn spring-boot:run
```
启动后访问: http://localhost:9966

### 运行管理后台 (web-admin)
```bash
cd web/web-admin
mvn spring-boot:run
```
启动后访问: http://localhost:9977

### 使用 Maven Wrapper (推荐)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Unix/Linux/Mac
./mvnw spring-boot:run
```

## 测试命令

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=TestClassName
```

### 运行特定测试方法
```bash
mvn test -Dtest=TestClassName#testMethodName
```

## 依赖管理

### 查看依赖树
```bash
mvn dependency:tree
```

### 更新依赖
```bash
mvn versions:display-dependency-updates
```

## Git 命令 (常用)

### 查看状态
```bash
git status
```

### 提交更改
```bash
git add .
git commit -m "commit message"
```

### 推送到远程
```bash
git push origin branch-name
```

## Windows 系统命令

### 目录操作
```bash
# 列出目录内容
dir

# 切换目录
cd path\to\directory

# 创建目录
mkdir directory-name

# 删除目录
rmdir /s directory-name
```

### 文件操作
```bash
# 查看文件内容
type filename

# 复制文件
copy source destination

# 删除文件
del filename
```

### 进程管理
```bash
# 查看端口占用
netstat -ano | findstr :9966

# 结束进程
taskkill /PID process_id /F
```

### 查找文件
```bash
# 在当前目录及子目录中查找文件
dir /s /b filename
```

## 数据库相关

### 连接 MySQL
```bash
mysql -h localhost -P 16033 -u root -p
```
数据库名: nine_forum

### 启动外部服务 (假设使用 Docker)
```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps
```

## 环境变量设置

### Windows (临时)
```bash
set MYSQL_PASSWORD=your_password
```

### Windows (PowerShell)
```powershell
$env:MYSQL_PASSWORD="your_password"
```

## 清理命令

### 清理 Maven 构建产物
```bash
mvn clean
```

### 清理并删除所有 target 目录
```bash
# Windows
for /d /r . %d in (target) do @if exist "%d" rd /s /q "%d"
```

## 日志查看

### 查看应用日志 (如果配置了日志文件)
```bash
type logs\application.log
```

### 实时查看日志 (PowerShell)
```powershell
Get-Content logs\application.log -Wait -Tail 50
```

## 代码格式化

目前项目未配置统一的格式化工具。建议:
- 在 IDE (如 IntelliJ IDEA) 中配置 Java 代码格式化规则
- 使用 IDE 的自动格式化功能 (Ctrl+Alt+L)

## Linting

目前项目未配置专门的 linting 工具。建议:
- 依赖 IDE 的代码检查功能
- 可以考虑集成 Checkstyle 或 SpotBugs

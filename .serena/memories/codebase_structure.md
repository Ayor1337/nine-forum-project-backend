# 代码库结构

## 模块架构
项目采用 Maven 多模块架构,分为三个主要模块:

```
nine-forum-project/
├── common/          # 公共模块 - 共享工具和配置
├── model/           # 模型模块 - 领域模型和 DTO
└── web/             # Web 模块
    ├── web-app/     # 主论坛应用 (端口 9966)
    └── web-admin/   # 管理后台 (端口 9977)
```

## common 模块结构
```
common/src/main/java/com/ayor/
├── config/          # 配置类 (Redis, Security, WebSocket 等)
├── minio/           # MinIO 文件存储相关
├── result/          # 统一结果封装
└── util/            # 工具类
```

## model 模块结构
```
model/src/main/java/com/ayor/
├── entity/          # 实体类
│   ├── app/         # 应用实体
│   │   ├── dto/     # 数据传输对象
│   │   └── vo/      # 视图对象
│   └── ...
└── type/            # 类型定义
```

## web-app 模块结构 (主应用)
```
web/web-app/src/main/java/com/ayor/
├── controller/      # REST 控制器
│   └── exception/   # 异常处理控制器
├── service/         # 服务接口
│   └── impl/        # 服务实现
├── mapper/          # MyBatis Mapper 接口
├── config/          # 应用配置
├── filter/          # 过滤器
├── interceptor/     # 拦截器
└── listener/        # 事件监听器 (RabbitMQ 等)

web/web-app/src/main/resources/
├── application.yml  # 应用配置
└── mapper/          # MyBatis XML 映射文件
```

## web-admin 模块结构 (管理后台)
类似于 web-app,但专注于管理功能
- 端口: 9977
- 简化的配置 (无 Elasticsearch 等)

## 包命名规范
- 基础包: `com.ayor`
- 控制器: `com.ayor.controller`
- 服务层: `com.ayor.service` (接口) 和 `com.ayor.service.impl` (实现)
- 数据访问: `com.ayor.mapper`
- 实体类: `com.ayor.entity`

## 分层架构
```
Controller (REST API)
    ↓
Service (业务逻辑)
    ↓
Mapper (数据访问)
    ↓
Database (MySQL)
```

## 关键目录说明
- `.docker/`: Docker 相关配置
- `.serena/`: Serena 工具配置和记忆文件
- `target/`: Maven 编译输出目录
- `CLAUDE.md`: Claude Code 项目指南
- `STOMP.md`: WebSocket STOMP 协议文档
- `websocket-*.md`: WebSocket 问题和解决方案文档

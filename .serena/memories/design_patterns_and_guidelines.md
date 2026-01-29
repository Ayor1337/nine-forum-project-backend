# 设计模式和指导原则

## 核心原则

### KISS 原则 (Keep It Simple, Stupid)
- 保持代码简单直接
- 避免过度设计和不必要的抽象
- 优先选择简单的解决方案

### 第一性原理思维
- 从问题的本质出发
- 避免盲目照搬模式
- 根据实际需求选择合适的技术方案

## 常用设计模式

### 1. 依赖注入 (Dependency Injection)
Spring 框架的核心模式:
```java
@Service
@RequiredArgsConstructor  // Lombok 生成构造器
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    // 通过构造器注入依赖
}
```

### 2. 接口-实现分离
Service 层使用接口定义契约:
```java
// 接口定义
public interface AccountService {
    UserVO getUserInfo(Integer userId);
}

// 实现类
@Service
public class AccountServiceImpl implements AccountService {
    @Override
    public UserVO getUserInfo(Integer userId) {
        // 实现
    }
}
```

### 3. DTO/VO 模式
数据传输对象分离:
- **DTO (Data Transfer Object)**: 用于接收客户端数据
- **VO (Value Object)**: 用于返回给客户端的数据
- **Entity**: 数据库实体

### 4. Repository 模式
使用 MyBatis Mapper 作为数据访问层:
```java
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    // 自定义查询方法
}
```

### 5. 统一结果封装
使用统一的响应格式:
```java
// 在 common/result 包中
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

### 6. AOP (面向切面编程)
用于横切关注点:
- 日志记录
- 事务管理
- 权限验证
- 异常处理

## 架构指导原则

### 分层架构
严格遵循三层架构:
```
Controller (表示层)
    ↓ 调用
Service (业务层)
    ↓ 调用
Mapper/Repository (数据访问层)
```

**原则**:
- Controller 不直接访问 Mapper
- Service 不处理 HTTP 相关逻辑
- Mapper 只负责数据访问

### 模块化设计
- **common**: 通用工具和配置,被其他模块依赖
- **model**: 数据模型,独立于业务逻辑
- **web-app**: 主应用业务逻辑
- **web-admin**: 管理功能,与 web-app 分离

### 单一职责原则 (SRP)
- 每个类只负责一个功能领域
- Service 类按业务领域划分 (AccountService, PostService 等)
- 避免创建"万能类"

## 安全最佳实践

### JWT 认证模式
- 无状态认证
- 令牌过期机制 (7天)
- 每次请求验证令牌
- 敏感操作需要额外验证

### 密码安全
- 使用 BCrypt 加密密码
- 永不存储明文密码
- 密码强度验证

### 权限控制
- 基于角色的访问控制 (RBAC)
- 方法级权限注解
- WebSocket 连接也需验证

## 异步处理模式

### RabbitMQ 消息队列
用于:
- 邮件发送
- 消息通知
- 数据同步
- 长时间任务

**原则**:
- 不阻塞主流程
- 手动确认模式保证可靠性
- 合理设置队列和交换机

### Redis 缓存策略
- 热点数据缓存
- 会话管理
- 计数器
- 分布式锁

**缓存策略**:
- TTL 设置合理 (默认 24 小时)
- 缓存失效策略
- 避免缓存穿透和雪崩

## WebSocket 实时通信

### STOMP 协议
- 点对点消息
- 主题订阅
- 消息广播

**最佳实践**:
- 连接认证
- 消息安全验证
- 异常处理和重连机制

## 数据库设计原则

### MyBatis-Plus 使用
- 利用内置 CRUD 方法
- 复杂查询使用 XML 映射
- 乐观锁处理并发

### 查询优化
- 避免 SELECT *
- 使用索引
- 分页查询大数据集
- 避免 N+1 查询问题

## 异常处理

### 统一异常处理
- 使用 `@ControllerAdvice` 全局异常处理
- 自定义业务异常
- 返回统一错误格式

### 日志记录
- 使用 SLF4J + Logback
- 合理的日志级别
- 敏感信息脱敏

## 开发工作流

### 渐进式开发
1. 理解需求
2. 设计方案
3. 编写代码
4. 测试验证
5. 代码审查
6. 部署上线

### 多轮迭代
- 小步快跑
- 快速反馈
- 持续改进

## 代码审查要点

- [ ] 是否遵循 KISS 原则
- [ ] 是否有安全隐患
- [ ] 是否有性能问题
- [ ] 是否符合项目架构
- [ ] 是否有足够的测试
- [ ] 代码是否易于维护

## 避免的反模式

### ❌ 过度工程
- 不要为"可能的需求"编码
- 不要过早优化
- 不要创建过多的抽象层

### ❌ 上帝类 (God Class)
- 避免单个类承担过多职责
- 合理拆分大类

### ❌ 硬编码
- 配置应该外部化
- 使用配置文件或环境变量
- 魔法数字应该定义为常量

### ❌ 重复代码 (DRY 原则)
- 提取公共方法
- 使用工具类
- 但不要过度抽象

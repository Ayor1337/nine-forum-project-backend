# 代码风格和约定

## 编程语言
- **Java 17**
- **文件编码**: UTF-8

## 代码风格特点

### 注解驱动开发
- 大量使用 Spring 注解 (`@RestController`, `@Service`, `@Autowired` 等)
- Lombok 注解减少样板代码 (`@Data`, `@Slf4j`, `@RequiredArgsConstructor` 等)

### 命名约定

#### 类命名
- **Controller**: `*Controller.java` (如 `AuthorizeController`)
- **Service 接口**: `*Service.java` (如 `AccountService`)
- **Service 实现**: `*ServiceImpl.java` (如 `AccountServiceImpl`)
- **Mapper 接口**: `*Mapper.java`
- **实体类**: 简单名词 (如 `Account`, `Post`, `Thread`)
- **DTO**: `*DTO.java`
- **VO**: `*VO.java`

#### 方法命名
- 使用驼峰命名法 (camelCase)
- 常见前缀:
  - `get*`: 查询方法
  - `update*`: 更新方法
  - `insert*`: 插入方法
  - `exists*`: 存在性检查
  - `load*`: 加载方法

#### 字段命名
- 使用驼峰命名法
- 依赖注入字段使用具体类型名 (如 `accountService`, `accountMapper`)

### 依赖注入
- 优先使用构造器注入 (通过 Lombok 的 `@RequiredArgsConstructor`)
- 字段注入使用 `@Autowired` 注解

### 接口与实现分离
- Service 层采用接口 + 实现类模式
- 接口定义在 `service` 包
- 实现类在 `service.impl` 包

### 分层架构原则
- **Controller**: 只处理 HTTP 请求/响应,调用 Service
- **Service**: 包含业务逻辑
- **Mapper**: 数据访问层,对应数据库操作

### 异常处理
- 使用专门的异常处理控制器 (见 `controller/exception/`)
- 统一结果封装 (在 `common/result` 包中)

### 配置管理
- 使用 `application.yml` 进行配置
- 敏感信息通过环境变量注入 (如 `${MYSQL_PASSWORD}`)
- 配置类使用 `@Configuration` 注解

### MyBatis 约定
- Mapper 接口与 XML 映射文件分离
- XML 文件位于 `resources/mapper/` 目录
- 使用 MyBatis-Plus 提供的基础 CRUD 方法

### Spring Security
- 基于 JWT 的无状态认证
- 角色和权限分离
- WebSocket 连接也需要安全验证

### 注释和文档
- 关键业务逻辑添加注释
- 使用 JavaDoc 注释公共 API
- 复杂逻辑需要解释性注释

## 代码质量要求
- 遵循 KISS 原则 (Keep It Simple, Stupid)
- 避免过度工程化
- 保持代码简洁和可维护性
- 注重性能优化 (使用 Redis 缓存等)

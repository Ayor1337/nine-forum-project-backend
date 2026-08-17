# 数据库规范

项目使用 MySQL 与 MyBatis-Plus。POJO 和公共类型主要在 `model`，各 Web 应用按领域拥有 Mapper；`web-app` 的 `config/MybatisPlusConfig.java` 注册 MySQL 分页拦截器。

## 持久化模式

- 新表或共享实体使用 `model/src/main/java/com/ayor/entity/pojo/`；应用专属 DTO、VO 仍放各自 Web 模块。
- Mapper 接口放对应应用的 `mapper/`；只有接口表达不清的复杂查询才添加 XML，并与接口在同一应用保持对应，例如 `web/web-app/src/main/resources/mapper/PermissionMapper.xml`。
- 分页接口使用项目的 `PageEntity<T>` 返回，而不是在 Controller 手工拼装页码数据；HTTP 返回仍包在 `Result<T>` 中，参见 `ThreadController#getThreadsByTopicId`。
- 对余额、库存、会话等并发敏感操作，沿用现有业务的唯一约束、条件更新或行锁方式；不能只做“先读再写”的无保护校验。具体约束见 `docs/database.md`。

## Schema 与迁移

当前没有 Flyway/Liquibase 迁移体系。结构变更必须同步：

1. 更新 `.docker/image/mysql/nine_forum_schema.sql`，保证新环境完整初始化。
2. 在 `docs/sql/` 添加可用于已有环境的增量 SQL；既有文件按日期加业务名命名，例如 `20260725_credit.sql`。
3. 更新 POJO、Mapper/Mapper XML、DTO、VO、相关 Service 测试及 `docs/database.md`。

不要仅改增量 SQL 或仅改 Java 模型，否则新环境与存量环境会漂移。

## 约束与删除语义

- 依靠数据库唯一约束防重复关系，如权限、收藏、关注/拉黑与凭据；不要只依赖接口层预检。
- `is_deleted`、`status`、`visibility`、`scope` 等字段均有业务含义，删除是否物理执行须以对应 Service 为准。
- 变更索引、外键、状态枚举或软删除逻辑时，先核对 `docs/database.md` 的关系和索引说明，并补齐对应测试。

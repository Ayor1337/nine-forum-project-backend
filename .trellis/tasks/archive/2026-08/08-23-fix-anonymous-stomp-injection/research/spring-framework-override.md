# Research: Spring Framework version override

- Query: 确定 Spring Framework 6.2.19 安全升级在本多模块 Maven 仓库中的最佳依赖覆盖位置、覆盖范围与兼容性风险。
- Scope: mixed
- Date: 2026-08-23

## Findings

### 当前依赖管理链

- 根 `pom.xml` 继承 `spring-boot-starter-parent:3.5.5`，并作为 `common`、`model`、`web` 聚合/父 POM。见 `pom.xml:5-28`。
- `web` 是 `web-app` 与 `web-admin` 的父 POM，并在两端继承的图中提供 `spring-boot-starter-web` 等依赖。见 `web/pom.xml:6-73`。
- 两个 Web 应用都声明 `spring-boot-starter-websocket`：用户端 `web/web-app/pom.xml:80-83`，管理端 `web/web-admin/pom.xml:42-45`；因此任一仅在 `web-app` 放置的覆盖都不能满足“整个 Spring Framework 统一升级”的验收。
- 根 POM 当前没有 `spring-framework.version` 属性，也没有直接声明任何 `org.springframework:spring-*` 模块版本。见 `pom.xml:36-86`。
- 已安装的 Spring Boot 3.5.5 BOM 明确管理 `<spring-framework.version>6.2.10</spring-framework.version>`（本机 Maven 缓存 `C:/Users/ayor/.m2/repository/org/springframework/boot/spring-boot-dependencies/3.5.5/spring-boot-dependencies-3.5.5.pom:191`），与任务 PRD 的已确认事实一致。

### 推荐放置点与形式

- **推荐：在根 `pom.xml` 的现有 `<properties>` 中新增一次 `<spring-framework.version>6.2.19</spring-framework.version>`。** 根 POM 已是所有模块共同父级，Spring Boot 的依赖 BOM 将以该 version property 管理整个 Spring Framework 族；这种覆盖能同时作用于 `spring-websocket`、`spring-messaging`、`spring-core`、`spring-context`、`spring-web` 等，而无需逐模块显式版本。
- Spring Boot 官方文档将 `spring-framework.version` 列为可覆盖的受管版本属性，且说明完整坐标由 `spring-boot-dependencies` 管理。外部参考：[Spring Boot 3.5 version properties](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/properties.html)。
- Spring 官方发布公告确认 Spring Framework 6.2.19 已发布并修复 `CVE-2026-41838`（WebSocket 模块可预测 session ID），同时提示其很可能是 6.2.x 最后一个 OSS 版本。外部参考：[Spring Framework 6.2.19 release](https://spring.io/blog/2026/06/08/spring-framework-7-0-8-and-6-2-19-available-now/)。

### 不推荐的替代方案

- 不要只在 `web/web-app/pom.xml` 直接覆盖 `spring-websocket` 或 `spring-messaging`：会留下同一运行时 Spring 模块版本不一致的风险，并遗漏管理端 WebSocket。
- 不要在根 `<dependencyManagement>` 为少数 `spring-*` artifact 分别写版本：Spring Framework 模块是紧密协作的发布组，局部覆盖容易把 transitive graph 分裂；需求也明确禁止此做法。
- 不要仅升级 Spring Boot parent 以“顺带”获得修复，除非同时评估 Boot 的其他 managed dependency 升级。此任务已指定 Framework 6.2.19，根属性覆盖的变更面更小且可验证。

### 验证与兼容性风险

- 覆盖后应在根目录执行 Maven 的 dependency tree/effective POM 检查，分别验证 `web-app` 与 `web-admin` 中 `org.springframework:spring-websocket`、`spring-messaging` 及其他 `spring-*` 都解析为 6.2.19；仅通过编译不能证明不存在旧 transitive 版本。
- 6.2.19 是同一 6.2 minor line 的补丁升级，预期源码兼容；但它也是该 OSS 线的末版，官方建议规划 7.0.x 迁移。因此仍需运行两个 Web 模块的测试，重点检查 WebSocket/STOMP、Spring Security Messaging 和 WebMvc。
- WebSocket 实现本身正是 CVE 修复触及区域；升级与本任务的握手/入站授权改动叠加时，失败应归因到“版本兼容”还是“授权策略”分别测试，避免用一次宽泛集成失败掩盖问题。

## Files found

- `pom.xml` — Spring Boot 3.5.5 父 POM、全仓聚合和现有共享版本属性；推荐的唯一覆盖位置。
- `web/pom.xml` — 两个 Web 应用共同父 POM，证明模块继承路径。
- `web/web-app/pom.xml` — 用户端 WebSocket starter 与 Security Messaging 依赖。
- `web/web-admin/pom.xml` — 管理端 WebSocket starter 依赖。
- `C:/Users/ayor/.m2/repository/org/springframework/boot/spring-boot-dependencies/3.5.5/spring-boot-dependencies-3.5.5.pom` — 本机只读 BOM 证据，确认 Boot 3.5.5 的默认 Framework 版本为 6.2.10。

## Related specs

- `.trellis/spec/backend/index.md` — 根 POM 聚合 `common`、`model`、`web`，两个 Web 应用独立启动。
- `.trellis/spec/backend/directory-structure.md` — `web -> common/model` 依赖方向及模块边界。
- `.trellis/spec/backend/quality-guidelines.md` — 共享配置/依赖变更应运行对应受影响 Maven 模块测试。

## Concurrent-change addendum

- 最终只读核对时，根 `pom.xml` 已在本调研期间出现 `<spring-framework.version>6.2.19</spring-framework.version>`（`pom.xml:36-46`）。这正是上文推荐的全仓覆盖位置；调研代理没有修改该 POM。
- 覆盖仍需由执行/检查阶段以 effective POM 或 dependency tree 证明两个 Web 模块的所有 `org.springframework:spring-*` 实际解析版本一致，尤其是 `spring-websocket` 和 `spring-messaging`。

## Caveats / Not Found

- 本次仅做只读调研，未运行 Maven dependency tree 或测试，以避免在任务 `research/` 之外产生构建输出；“当前解析版本”根据本机已缓存的 Boot BOM 与 POM 继承关系确认，尚未用有效 POM命令复核。
- Boot 3.5.16 的当前文档已要求 Framework 6.2.19 或更高，但仓库锁定的是 3.5.5；该事实不能替代对本项目 3.5.5 + 属性覆盖组合的测试验证。外部参考：[Spring Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)。

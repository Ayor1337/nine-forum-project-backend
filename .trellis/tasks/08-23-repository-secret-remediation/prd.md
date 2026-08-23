# 处置仓库历史 SMTP 凭据

## 目标

让用户端真实本地配置不再受 Git 跟踪，以安全示例配置替代，并从远端 Git 历史中移除真实配置文件。

## 已确认事实

- SEC-05 报告确认 SMTP 凭据的指纹存在于 37 个历史提交中。
- 用户要求保留本地 `web/web-app/src/main/resources/application.yml` 的真实开发配置，但不得上传远端；仓库改为提供 `application.example.yml`。
- 历史中暴露的旧 SMTP 密码已经过期，不再构成可用凭据；历史重写用于清理仓库记录。
- 当前项目定位为本地开发项目；本地 Docker 数据库凭据不在本任务范围内。
- 用户端与管理端共用 JWT 的设计用于共享登录态；不在本任务中拆分 JWT 签名密钥。
- 仓库配置了 GitHub `origin`，已知引用包括 `main`、`develope` 和 `feature/spotify`；历史重写会改变相关提交 ID。
- 当前仓库仍跟踪用户端 `application.yml`，且 `.gitignore` 尚无该文件的精确忽略规则。
- 凭据的实际值不写入示例、任务文档、提交信息或测试输出。

## 需求

- R1：恢复本地用户端 `application.yml`，并以精确 `.gitignore` 规则确保它不再受 Git 跟踪。
- R2：提交结构一致但不含真实凭据的 `application.example.yml`，README 说明复制为本地配置后再启动。
- R3：从所有目标分支和标签历史中移除 `web/web-app/src/main/resources/application.yml` 路径，使远端可达历史不再包含该文件。
- R4：历史重写必须在隔离镜像中执行，不改写当前含未提交文件的工作区；强推前保留可恢复备份。
- R5：提供现有克隆仓库/协作者的重新克隆或强制同步说明。

## 验收条件

- [ ] 本地用户端 `application.yml` 存在、包含所需本机配置且被 Git 忽略，不出现在索引或待提交文件中。
- [ ] `application.example.yml` 可解析、结构完整且不含真实 SMTP 凭据。
- [ ] 新克隆可按 README 从示例复制出本地配置。
- [ ] 重写后的所有目标分支与标签均不包含用户端 `application.yml` 历史路径。
- [ ] 已验证重写后的仓库可以正常检出，并提供重新克隆/强制同步说明及回滚点。
- [ ] 不修改本地 Docker 凭据、JWT 共享登录设计或无关审计报告改动。

## 范围外

- 更换本地 Docker、数据库、MinIO、RabbitMQ 等开发环境凭据。
- 修改用户端与管理端的 JWT 共享登录设计。
- 将管理端 `web/web-admin/src/main/resources/application.yml` 改为本地忽略文件。
- 处理未在本任务证据中确认的其他历史秘密。

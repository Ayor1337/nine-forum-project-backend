# 本地配置与秘密规范

## 1. 适用范围 / 触发条件

修改 Spring Boot 配置、数据库/JWT/MinIO/RabbitMQ/SMTP 凭据、示例配置或 Git 忽略规则时适用。目标是允许本地开发保存实际值，同时保证 Git 当前树、示例、日志和审查输出不包含这些值。

## 2. 文件与命令签名

- 本地用户端配置：`web/web-app/src/main/resources/application.yml`，必须存在于开发机但不受 Git 跟踪。
- 用户端示例配置：`web/web-app/src/main/resources/application.example.yml`，必须受 Git 跟踪且不得含真实凭据。
- 初始化命令（PowerShell）：

```powershell
Copy-Item web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
```

- 初始化命令（Bash）：

```bash
cp web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
```

## 3. 配置契约

- `.gitignore` 必须精确包含 `/web/web-app/src/main/resources/application.yml`，不得忽略 `application.example.yml` 或管理端配置。
- 本地文件与示例文件必须具有相同 YAML 键集合；示例的敏感键只能使用空环境变量占位符或明确的非秘密示例值。
- 新增、删除或重命名配置键时必须同步两个文件；审查只比较键、类型和占位符规则，不打印本地值。
- 任何凭据扫描只能输出文件名、命中数量或摘要，不能输出匹配文本。

## 4. 验证与错误矩阵

| 条件 | 结果 |
| --- | --- |
| 本地 `application.yml` 未被忽略或仍在索引中 | 拒绝提交 |
| 示例 YAML 无法解析，或键集合与本地文件不同 | 拒绝提交 |
| 示例敏感键复用了本地值 | 视为秘密泄露，立即移除并检查历史 |
| 新克隆缺少本地配置 | 按 README 复制示例后填写本机值 |
| 检查命令会打印配置值 | 改为计数、摘要或仅键名检查 |

## 5. Good / Base / Bad Cases

- Good：本地文件存在且被忽略，示例受跟踪、键完整、敏感值为空占位符。
- Base：新克隆只有示例文件，开发者复制后填写本机配置再启动。
- Bad：把本地文件强制加入 Git，或直接复制真实文件作为示例。

## 6. 必需测试

- `git check-ignore`：断言本地配置被精确规则命中。
- `git ls-files`：断言本地配置未跟踪、示例配置已跟踪或位于待提交集合。
- YAML 解析：断言两个文件都可解析，键集合相等。
- 敏感键检查：断言示例未复用本地敏感值；输出仅含计数。
- `./mvnw.cmd -pl web/web-app -am test`：断言模块及依赖测试通过。

## 7. Wrong vs Correct

### Wrong

```text
web/web-app/src/main/resources/application.yml  # 含本机值且受 Git 跟踪
```

### Correct

```text
web/web-app/src/main/resources/application.yml          # 本机保留、Git 忽略
web/web-app/src/main/resources/application.example.yml  # 安全示例、Git 跟踪
```

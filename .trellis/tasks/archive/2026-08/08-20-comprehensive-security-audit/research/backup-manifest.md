# 动态安全验证数据库备份清单

- 备份时间：2026-08-20 10:58:25（Asia/Taipei）
- 来源：本机 Docker 容器 `nine-mysql` 中的 `nine_forum` 数据库
- 方式：`mysqldump` 单事务逻辑备份，包含数据库、表结构、数据、触发器、存储过程和事件
- GTID：已使用 `--set-gtid-purged=OFF`，备份中无全局 GTID 写入语句
- 文件：`.trellis/tasks/08-20-comprehensive-security-audit/backups/nine_forum-20260820-105825-portable.sql`
- 大小：111130 字节
- SHA-256：`8561562E3514C213B0AD5674874AFDE76FD7E9407633F379152D90B51AA69072`
- 结构检查：44 条 `CREATE TABLE`，41 条 `INSERT INTO`
- 版本控制：任务备份目录已加入根 `.gitignore`

说明：本次只验证备份文件非空、SQL 结构存在及校验值；未执行恢复演练，以免在审计开始前修改数据库状态。

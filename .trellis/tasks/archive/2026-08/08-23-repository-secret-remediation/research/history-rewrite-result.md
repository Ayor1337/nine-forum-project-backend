# Git 历史重写结果

## 远端引用

- `develope`：重写后 `c8667376663ac28229f9c6637216d8caa122de61`。
- `main`：重写后追加安全示例配置提交，最终为 `dd7417be0d57a380a45129e9ef680f3cff64bf21`。
- `feature/spotify`：重写后追加安全示例配置提交，最终为 `2e23ae767e2165f490d9ff5c2971c0dc92981a52`。
- 标签：无。

三个分支使用逐引用租约与原子强推更新；强推前远端引用与盘点快照一致。

## 验证结果

- 从 GitHub 全新克隆验证成功。
- 全部远端可达历史中，`web/web-app/src/main/resources/application.yml` 路径涉及提交数为 `0`。
- 重写后的 `develope` 不跟踪本地配置，跟踪一份 `application.example.yml`。
- 默认分支 `main` 与 `feature/spotify` 随后以普通快进提交补齐同一份安全示例、精确忽略规则和 README 复制说明。
- 最终从 GitHub 全新克隆验证三个远端分支均满足：真实配置历史路径数 `0`、当前真实配置跟踪数 `0`、示例配置跟踪数 `1`。
- 重写前后 `develope` 最终文件树哈希一致；变化仅为提交历史。
- 本地 `develope` 与 `main` 指针已更新为重写后的远端提交。
- 无关审计报告移动保持未提交，未包含在任务提交或强推内容中。

## 回滚点

- 远端原始镜像：`.trellis/tasks/08-23-repository-secret-remediation/backups/remote-before-rewrite-v2.git`。
- 本地原始 `develope` bundle：`.trellis/tasks/08-23-repository-secret-remediation/backups/local-develope-before-rewrite-v2.bundle`。

备份目录受 `.gitignore` 保护且包含清理前历史，不得提交或公开。现有克隆应重新克隆；确需保留本地工作的协作者应先备份工作，再基于新远端提交重新建立分支，避免把旧历史合并回仓库。

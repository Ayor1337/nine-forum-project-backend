测试2

## 本地配置

用户端真实配置仅保存在本机，不受 Git 跟踪。首次启动前，请复制示例配置：

```powershell
Copy-Item web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
```

```bash
cp web/web-app/src/main/resources/application.example.yml web/web-app/src/main/resources/application.yml
```

然后在本地 `application.yml` 中填写开发配置，不要将该文件强制加入 Git。

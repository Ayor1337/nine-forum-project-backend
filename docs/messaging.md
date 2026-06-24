# 消息与实时通信说明

NineForum 同时使用 RabbitMQ 和 WebSocket/STOMP。RabbitMQ 负责跨应用异步事件，STOMP 负责面向浏览器或管理端页面的实时推送。

## RabbitMQ 配置

| 应用 | exchange | queue | routing key | 用途 |
| --- | --- | --- | --- | --- |
| `web-app` | `mail.direct` | `mail.queue` | `mail` | 注册邮件验证。 |
| `web-app` / `web-admin` | `report.direct` | `report.queue` | `report.created` | 用户端创建举报，管理端接收举报事件。 |
| `web-app` / `web-admin` | `page-broadcast.direct` | `page-broadcast.queue` | `page-broadcast.changed` | 管理端变更页面广播，用户端推送页面广播变更。 |
| `web-admin` | `broadcast.direct` | `broadcast.queue` | `broadcast` | 管理端发送用户广播，用户端转为系统通知。 |

消息转换器使用 `Jackson2JsonMessageConverter`。当前监听器确认模式配置为 `manual`，处理失败时应关注消息确认和重投行为。

## RabbitMQ 监听器

| 应用 | 监听器 | 队列 | 行为 |
| --- | --- | --- | --- |
| `web-app` | `EmailListener` | `mail.queue` | 处理邮件发送。 |
| `web-app` | `BroadcastListener` | `broadcast.queue` | 接收管理端用户广播并推送系统通知。 |
| `web-app` | `PageBroadcastEventListener` | `page-broadcast.queue` | 接收页面广播变更并转发到 STOMP 目的地。 |
| `web-admin` | `ReportListener` | `report.queue` | 接收举报创建事件。 |

## 用户端 STOMP

用户端 STOMP 端点：

- `/chatboard`
- `/chat`
- `/system`

消息代理配置：

- 应用目的地前缀：`/app`
- 用户目的地前缀：`/user`
- simple broker：`/broadcast`、`/transfer`、`/notif`、`/verify`

常见推送目的地：

| 目的地 | 用途 |
| --- | --- |
| `/broadcast/topic/{topicId}` | 话题聊天室消息广播。 |
| `/verify/{jwtId}` | 注册邮箱验证结果。 |
| `/user/{accountId}/notif/system` | 用户系统消息。 |
| `/user/{accountId}/notif/mention` | 提及消息。 |
| `/user/{accountId}/notif/follow` | 关注用户发布主题帖后的关注动态消息。 |
| `/user/{accountId}/notif/unread` | 总未读消息。 |
| `/user/{accountId}/notif/unread/{type}` | 指定类型未读消息。 |
| `/user/{accountId}/notif/unread-overview` | 未读消息概览。 |
| `/user/{accountId}/notif/unread/whisper` | 私信未读消息。 |

页面广播目的地由 `PageBroadcastEventListener` 根据广播作用域生成，调用方应以该监听器实现为准。

## 管理端 STOMP

管理端 STOMP 端点：

- `/reports`

消息代理配置：

- 应用目的地前缀：`/app`
- simple broker：`/topic`

举报实时推送目的地：

- `/topic/reports`

## 定时任务

| 应用 | 任务 | cron | 行为 |
| --- | --- | --- | --- |
| `web-app` | `accountStatistics` | `0 0 0 * * *` | 每天更新用户统计。 |
| `web-app` | `threadStatistics` | `0 0 * * * *` | 每小时更新帖子统计。 |
| `web-app` | `topicStatistics` | `0 0 * * * *` | 每小时更新话题统计。 |
| `web-admin` | `refreshDashboardActivities` | `0 */5 * * * *` | 每 5 分钟刷新仪表盘动态。 |

用户端和管理端任务都会在启动后立即执行一次相关刷新逻辑。

## 维护要求

- 新增队列、交换机、routing key 或监听器时，同步更新本文件。
- 新增 STOMP 端点或推送目的地时，同步更新本文件和前端订阅约定。
- 修改 RabbitMQ ack 策略时，需要补充失败、重复投递和幂等处理说明。

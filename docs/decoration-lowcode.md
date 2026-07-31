# 装扮低代码平台设计方案（头像框 / 头衔 / 徽章）

> **状态**：后端已实施（decoration 表、web-admin 装扮管理接口、商品绑定校验、web-app 配置下发），admin 前端编辑器尚未实施。现状以 `database.md`、`api.md` 与代码为准，本文档保留渲染协议与前端方案作为契约参考。

## 概述

将头像框、头衔、徽章三类装扮的"编辑"能力做成 web-admin 中的低代码平台：管理员通过可视化设计器产出**结构化 JSON 配置**，存储于独立的 `decoration` 表，经"草稿 → 发布"流转后，由 `shop_item.decoration_id` 绑定上架售卖；用户端接口随装备信息下发已发布配置，前端按统一渲染协议动态渲染，逐步替代现有的 `itemKey` 前端硬编码素材映射。

**本期交付物**：仅本设计文档，本阶段不编写业务代码。后续按文档分别在后端仓库与 admin 前端仓库实施。

## 已确认的关键决策

| 决策点 | 结论 |
|---|---|
| 存储模型 | 独立 `decoration` 表，`shop_item` 增加可空外键 `decoration_id` |
| 定制能力 | 三类装扮各自的结构化 JSON 配置（不允许自由代码片段） |
| 发布流程 | DRAFT → PUBLISHED → ARCHIVED，发布后用户端才可见 |
| 分发途径 | 仅商城绑定（用户购买绑定装扮的商品后获得） |
| 前端 | 已有独立 admin 前端仓库，本方案描述页面/组件/渲染协议，技术栈以前端仓库为准 |

## 现状背景

- 三类装扮当前是 `shop_item` 商品（`ShopItemType` 的 `BADGE` / `AVATAR_FRAME` / `TITLE`），`item_key` 字段仅作"前端素材映射用"，样式与素材全部硬编码在用户前端。
- web-admin 已有 `ShopController`（`/api/shop`）支持商品 CRUD 与购买记录查询，`ShopItemDTO` 尚无装扮配置字段。
- 图片素材上传可复用 common 模块图片管线：`Base64Upload` + `ImageStorageService.storeImageBase64Image(upload, "前缀/")`（ImageProcessor + MinIO），现有 `avatar/`、`banner/` 等前缀先例。
- 用户端装备查询集中在 `UserItemMapper`（`selectByAccountId`、`selectEquippedByAccountId`、`selectEquippedAvatarFrame`、`selectEquippedBadge`），头像接口返回 `UserAvatarVO`（含头像框、徽章）。

## 一、数据模型设计

### `decoration` 表（新建）

| 列 | 类型 | 说明 |
|---|---|---|
| `decoration_id` | int PK 自增 | 装扮 ID |
| `decoration_key` | varchar(64) 唯一 | 命名规则沿用现有 `item_key` 校验：`^[a-z0-9][a-z0-9_-]{0,63}$` |
| `name` | varchar(64) | 装扮名称（title 类型即头衔文本） |
| `description` | varchar(512) | 描述 |
| `type` | varchar(32) | `badge` / `avatar_frame` / `title`（复用 `ShopItemType` 三个值） |
| `status` | tinyint | 1=DRAFT，2=PUBLISHED，3=ARCHIVED |
| `draft_config` | json | 编辑中的配置 |
| `published_config` | json | 已发布配置（用户端只读此字段） |
| `version` | int | 乐观锁 |
| `published_at` | datetime | 最近发布时间 |
| `created_by` | int | 创建管理员账号 ID |
| `create_time` / `update_time` | datetime | 标准时间戳 |
| `is_deleted` | tinyint(1) | 软删除 |

**发布动作** = 将 `draft_config` 复制到 `published_config` 并置 PUBLISHED、刷新 `published_at`；归档后不可再被新商品绑定，已绑定商品与已持有用户不受影响。

### `shop_item` 变更

- 新增可空列 `decoration_id`（FK → `decoration`）。
- 校验规则：`item_type` 为三类装扮且填了 `decoration_id` 时，装扮必须存在、已发布、且 `type` 与 `item_type` 一致；非装扮类商品不允许绑定。
- **兼容性**：`decoration_id` 为空的老商品维持现状，用户端回退到 `itemKey` 硬编码渲染。

### 迁移脚本

实施时按仓库惯例新增 `docs/sql/<日期>_decoration.sql`，包含建表与 `shop_item` 加列语句，并同步更新 `docs/database.md` 与 `.docker/image/mysql/nine_forum_schema.sql`。

## 二、渲染协议（前后端契约，配置 JSON 规范）

头像框 / 徽章已升级为 `schemaVersion=2`（尺寸全部改为相对头像边长的比例，形状类配置移除，固定圆形），头衔保持 `schemaVersion=1`；v1 头像框 / 徽章协议已废弃，无兼容 / 迁移逻辑。配置结构：

- **avatar_frame**

```json
{
  "schemaVersion": 2,
  "mode": "image | css",
  "imageUrl": "可选，mode=image 时必填",
  "border": { "width": 0.08, "color": "#ffd700 或 gradient 对象" },
  "animation": { "type": "none | rotate | pulse", "durationMs": 2000 },
  "scale": 1.1
}
```

- **title**：文本取商品/装扮 `name`，配置只描述样式

```json
{
  "schemaVersion": 1,
  "color": "#ffffff",
  "gradient": { "from": "#ff8a00", "to": "#da1b60", "direction": "90deg" },
  "background": { "color": "#000000 或 gradient 对象", "radius": 8, "padding": "2px 8px" },
  "fontWeight": 700,
  "glow": { "color": "#ffd700", "blur": 8 }
}
```

- **badge**

```json
{
  "schemaVersion": 2,
  "mode": "icon | image",
  "iconKey": "mode=icon 时必填，前端内置图标集",
  "imageUrl": "mode=image 时必填",
  "color": "#ffffff",
  "background": "#3b82f6",
  "size": 0.4
}

服务端对配置做结构校验（按类型校验必填 / 枚举 / 取值范围），不信任前端提交的任意字段；徽章 `iconKey` 图标集由前端内置维护，后端只存 key 不校验取值。关键取值约束：`avatar_frame.border.width` 为 `0 < width ≤ 0.5` 的比例，`scale` 为 `1.0~1.5`，`animation.durationMs` 为 `300~10000` 毫秒；`badge.size` 为 `0 < size ≤ 1` 的比例；`schemaVersion` 按类型强制精确版本（头像框 / 徽章 = 2，头衔 = 1）。

## 三、后端 API 设计（web-admin）

沿用 `Result` / `PageEntity` / `@PreAuthorize("isAuthenticated()")` 约定，新增 `DecorationController`（`/api/decorations`）：

| 接口 | 说明 |
|---|---|
| `POST /api/decorations` | 创建装扮（初始 DRAFT） |
| `PUT /api/decorations/{id}` | 保存草稿配置（仅 DRAFT / PUBLISHED 可编辑；编辑已发布项时只改 `draft_config`，不影响线上） |
| `POST /api/decorations/{id}/publish` | 发布：`draft_config` → `published_config`，状态置 PUBLISHED |
| `POST /api/decorations/{id}/archive` | 归档 |
| `DELETE /api/decorations/{id}` | 软删除（仅 DRAFT 可删） |
| `GET /api/decorations` | 分页查询（name 模糊 / type / status 过滤） |
| `GET /api/decorations/{id}` | 详情（含 draft 与 published 两份配置） |
| `POST /api/decorations/assets` | 素材上传：复用 `Base64Upload` + `ImageStorageService.storeImageBase64Image(upload, "decoration/")`，返回可引用的 `imageUrl` |

`ShopItemDTO` 增加可空 `decorationId` 字段，创建 / 更新商品时执行上述绑定校验。

## 四、用户端改动（web-app）

- `UserItemVO` 及装备查询（`UserItemMapper` 各 SQL）LEFT JOIN `decoration`，返回 `decorationConfig`（即 `published_config`）。
- 头像查询接口（`UserAvatarVO` / `getUserAvatar`）附带已装备头像框、徽章的 `published_config`；头衔文本与样式同理下发。
- 回退逻辑：`decoration_id` 为空时不下发 config，用户前端继续走 `itemKey` 硬编码映射——新旧商品并存无感知。

## 五、admin 前端方案（设计描述，实施在前端仓库）

- **装扮列表页**：类型 / 状态筛选、新建、编辑、发布、归档入口。
- **设计器页**：按类型加载对应表单组件 `AvatarFrameDesigner` / `TitleDesigner` / `BadgeDesigner`；左侧结构化表单（取色器、渐变、尺寸、动画、素材选择 / 上传），右侧**实时预览画布**（头像框预览叠加示例头像、头衔预览示例昵称、徽章预览示例卡片）。
- **渲染协议复用**：预览渲染器与用户端前端的 `DecorationRenderer` 消费同一份 JSON 协议（见下节），保证"所见即所得"。
- **商城商品表单**：三类装扮商品增加"绑定装扮"下拉（仅列已发布且类型匹配的装扮）。

## 六、web-app 用户前端渲染规范（设计描述，实施在用户前端仓库）

### 配置下发点

| 接口 | 字段 | 说明 |
| --- | --- | --- |
| `GET /api/shop/my-items`（背包分页） | `UserItemVO.decorationConfig` | 已发布配置 JSON 文本，未绑定装扮的商品为空 |
| `GET /api/shop/users/{account_id}/decorations`（已装备装饰） | `UserItemVO.decorationConfig` | 同上 |
| `GET /api/users/{user_id}/avatar`（头像查询，返回 `UserAvatarVO`） | `avatarFrameConfig` / `badgeConfig` | 已装备头像框、徽章的已发布配置 |

头衔没有独立查询入口：文本与配置均从已装备列表（`name` + `decorationConfig`）中取。

### `DecorationRenderer` 组件规范

1. **解析**：将 config JSON 文本解析为对象，读取 `schemaVersion` 分发到对应版本的渲染实现；遇到未知 `schemaVersion`（高于前端支持的版本）时降级到 `itemKey` 硬编码映射，保证向后兼容。
2. **回退策略**：config 为空（老商品 / 解析失败 / 未知版本）时走现有 `itemKey` → 硬编码素材映射逻辑，新旧商品并存无感知。
3. **头像框**：`mode=image` 时将 `imageUrl` 作为覆盖层叠加在头像外层；`mode=css` 时将 `border`（宽度 / 颜色 / 渐变 / 圆角）映射为头像外圈样式，`animation`（rotate / pulse + `durationMs`）映射为 CSS 动画，`scale` 表示框相对头像直径的放大倍数。
4. **头衔**：文本取装备项 `name`；`color` 或 `gradient`（`from` / `to` / `direction`）映射文字颜色或渐变，`background` 映射底色块（颜色 / 渐变 / 圆角 / 内边距），`fontWeight` 映射字重，`glow` 映射文字外发光。
5. **徽章**：`mode=icon` 时按 `iconKey` 查前端内置图标集并以 `color` 着色，`mode=image` 时加载 `imageUrl`；`shape`（circle / square / hex）决定裁剪形状，`background` 为底色，`size` 为边长（px）。
6. **协议复用**：与 admin 设计器的预览渲染器消费同一份配置结构与字段语义，建议两端共享同一份渲染实现（或按本文档逐字段对齐），保证"所见即所得"；渲染实现只读本文档定义的字段，忽略未知字段。

### 验收要点（用户前端）

- 绑定装扮的商品装备后按 config 渲染，与 admin 设计器预览一致。
- 未绑定装扮的老商品渲染行为与现状完全一致。
- 装扮归档 / 下架后，已装备用户的展示不受影响（仍按已下发的 `published_config` 渲染）。

## 七、测试与验收

**后端单测**（实施阶段补充）：

- `DecorationServiceImplTest`：CRUD、发布状态机（DRAFT→PUBLISHED→ARCHIVED 合法流转与非法流转拒绝）、配置结构校验、乐观锁。
- `ShopServiceImplTest` 扩展：绑定校验（未发布 / 类型不匹配 / 非装扮类型拒绝）。
- `UserItemMapper` / `AccountServiceImpl` 相关测试：config 下发与空 decoration 回退。

**端到端验收场景**：

1. 管理员设计头像框 → 保存草稿 → 发布 → 创建商品绑定 → 用户购买装备 → 用户端按 config 渲染。
2. 已发布装扮继续编辑草稿：线上展示不变，再次发布后生效。
3. 老商品（无 `decoration_id`）渲染行为与现状一致。

## 明确假设

- 文档落点为 `docs/decoration-lowcode.md`（仓库 docs 为扁平结构，无 design 子目录）。
- 徽章 `iconKey` 图标集由前端内置维护，后端只存 key 不校验取值。
- JSON 配置草案中的字段名 / 取值范围为初版约定，实施时前后端按文档对齐后冻结。1

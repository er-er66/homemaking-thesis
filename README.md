# 家政服务平台（Homemaking）

一个前后端分离的家政服务预约平台：普通用户在线浏览服务套餐、下单预约；家政人员接单 / 接受派单、与用户在线沟通、查看收入；管理员在后台管理用户、家政人员、套餐、订单与结算。

后端 Spring Boot 3 + MyBatis + MySQL，前端 Vue 3 + Vite + Element Plus，聊天走 WebSocket，图片存阿里云 OSS。

---

## 功能概览

| 角色 | 主要能力 |
| --- | --- |
| 普通用户 | 注册 / 登录、浏览服务套餐、下单预约（支持多套餐与数量）、上传订单图片、订单列表与详情、取消订单、与家政人员聊天、个人资料与地址管理 |
| 家政人员（员工） | 登录、查看可接订单、接单 / 拒单、查看我的订单、与用户聊天、查看收入与结算记录 |
| 管理员 | 用户 / 家政人员 / 管理员账号管理（分页 + 多条件查询、启用禁用、改名）、服务套餐增删改查与上架下架、订单列表与详情、派单、聊天消息查看与搜索、家政人员收入结算 |

### 业务要点

- **套餐与订单解耦**：下单时选择的套餐写入中间表 `homemaking_order_package`，保存下单时的价格快照，后续调价不影响历史订单；订单金额由套餐明细计算得出。
- **套餐上下架**：一条 SQL 原子翻转状态，避免「先查后改」的并发覆盖；上下架后主动清理 Redis 详情缓存，并写入空值标识防止缓存穿透。
- **缓存**：套餐详情缓存到 Redis，命中缓存时仍会复核「上架状态 + 逻辑删除 + 有效期」。
- **收入结算**：订单完成后按 `staff.income.ratio`（默认 0.8）计入家政人员收入，等待 `staff.settle.delay-days`（默认 7）天后由 Quartz 定时任务（每天 01:00）转为可结算金额。
- **密码**：统一走 BCrypt 存储与校验，兼容历史明文数据的渐进迁移。
- **鉴权**：JWT 令牌 + 过滤器校验；前端路由守卫按角色区分用户 / 家政人员 / 管理员入口。

---

## 技术栈

### 后端

| 类别 | 选型 |
| --- | --- |
| 框架 | Spring Boot 3.5.15（Java 17） |
| 持久层 | MyBatis 3.0.5（XML Mapper） |
| 数据库 | MySQL 8 |
| 缓存 | Redis（Spring Data Redis + Jedis 连接池） |
| 定时任务 | Quartz（`spring-boot-starter-quartz`） |
| 实时通信 | Spring WebSocket（端点 `/ws/chat`） |
| 鉴权 | JJWT 0.12.5 + 自定义 `JwtAuthFilter` |
| 密码加密 | `spring-security-crypto`（仅引入 BCrypt，不引入完整 Security 拦截链） |
| 对象存储 | 阿里云 OSS SDK 3.17.4 |
| 其他 | Lombok、FastJSON |

### 前端

| 类别 | 选型 |
| --- | --- |
| 框架 | Vue 3.5（`<script setup>`） |
| 构建 | Vite 8 |
| UI | Element Plus 2.14 + `@element-plus/icons-vue` |
| 路由 | Vue Router 5（History 模式） |
| 请求 | Axios（统一封装于 `src/api/request.js`） |

---

## 目录结构

```
homemaking/
├── back/                               # 后端 Spring Boot 工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/homemaking/
│       │   ├── HomemakingApplication.java
│       │   ├── config/                 # JWT 过滤器、WebSocket、Redis、密码编码器
│       │   ├── controller/             # 按角色/模块分包（admin、user、emp、order、chat…）
│       │   ├── dto/                    # 请求/响应传输对象
│       │   ├── entity/                 # 数据库实体
│       │   ├── mapper/                 # MyBatis Mapper 接口
│       │   ├── result/                 # 统一响应体 Result
│       │   ├── service/ services/      # 业务接口与实现
│       │   ├── task/                   # Quartz 定时任务（收入结算）
│       │   └── util/                   # 工具类（订单号、分页、密码）
│       └── resources/
│           ├── application.properties  # 本地配置，未入库（见下方「配置」）
│           └── mapper/                 # MyBatis XML
└── front/                              # 前端 Vue 3 工程
    ├── vite.config.js                  # 开发代理指向 localhost:8080
    └── src/
        ├── api/                        # Axios 实例与接口封装
        ├── admin/                      # 管理员后台
        ├── login/ registrant/          # 登录、注册
        ├── user/ order/ profile/       # 用户端：首页、订单、个人中心
        ├── staff/                      # 家政人员端：工作台、订单、聊天、收入
        ├── merchant/                   # 聊天页（用户 ↔ 家政人员 / 客服）
        ├── router/                     # 路由与角色守卫
        ├── store/ styles/ utils/ assets/
        └── main.js App.vue
```

---

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.8+（或直接使用 `back/mvnw`） |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |

阿里云 OSS 账号仅在需要上传图片功能时必需。

---

## 快速开始

### 1. 准备数据库

创建数据库（默认库名为 `homemaking`），导入表结构与初始数据：

```sql
CREATE DATABASE homemaking DEFAULT CHARACTER SET utf8mb4;
```

> 本仓库不含 SQL 导出文件，表结构请从 `back/src/main/resources/mapper/*.xml` 中的表名与字段对照建立，或使用本地已有的数据库导出。

### 2. 创建后端配置（必做）

`back/src/main/resources/application.properties` 因含数据库密码、OSS 密钥、JWT 密钥，已被 `.gitignore` 排除，**克隆后仓库里没有这个文件**，需自行创建。模板如下（把占位值换成自己的）：

```properties
# ---- 数据源 ----
spring.datasource.url=jdbc:mysql://localhost:3306/homemaking?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ---- MyBatis ----
mybatis.configuration.map-underscore-to-camel-case=true
mybatis.mapper-locations=classpath:mapper/*.xml

# ---- 上传大小限制 ----
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB

# ---- 阿里云 OSS ----
aliyun.oss.endpoint=oss-cn-xxxxxxxx.aliyuncs.com
aliyun.oss.bucket-name=你的bucket
aliyun.oss.access-key-id=你的AccessKeyId
aliyun.oss.access-key-secret=你的AccessKeySecret

# ---- Redis ----
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
spring.data.redis.timeout=3000ms
spring.data.redis.jedis.pool.max-active=8
spring.data.redis.jedis.pool.max-idle=8
spring.data.redis.jedis.pool.min-idle=0

# ---- Quartz ----
spring.quartz.job-store-type=memory
spring.quartz.properties.org.quartz.scheduler.instanceName=homemakingScheduler
spring.quartz.properties.org.quartz.threadPool.threadCount=5

# ---- JWT ----
jwt.secret=请替换为足够长的随机字符串（至少32字符）
jwt.expiration=86400000

# ---- 家政人员收入结算 ----
staff.income.ratio=0.8
staff.settle.delay-days=7
staff.settle.cron=0 0 1 * * ?
```

> `jwt.secret` 必须自行生成，不要复用示例值。

### 3. 启动后端

```bash
cd back
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Windows 也可用 `mvn spring-boot:run`。默认端口 **8080**（`application.properties` 未覆盖 `server.port`）。

### 4. 启动前端

```bash
cd front
npm install
npm run dev
```

访问 `http://localhost:5173`。开发服务器已配置代理，将 `/admin`、`/user`、`/emp`、`/ws`、`/upload-avatar`、`/user-info` 转发到 `http://localhost:8080`。

生产构建：

```bash
npm run build      # 产物在 front/dist
```

---

## 数据模型

共 17 张表，按模块划分：

| 模块 | 表 |
| --- | --- |
| 账号 | `sys_user`、`sys_staff`、`sys_admin` |
| 服务套餐 | `homemaking_package`、`sys_staff_package` |
| 订单 | `homemaking_order`、`homemaking_order_package`、`homemaking_order_img` |
| 收入结算 | `sys_staff_amount`、`sys_staff_amount_record` |
| 聊天 | `chat_room`、`chat_messages`、`chat_room_member`、`chat_message_read` |
| 附件与其他 | `user_img`、`sys_user_address` |

关键关系：

- `homemaking_order` 与 `homemaking_package` 通过 `homemaking_order_package` 多对多关联，中间表保存 `unit_price` 价格快照。
- `sys_staff_package` 维护家政人员可承接的套餐范围。
- `sys_staff_amount` 是家政人员账户余额，`sys_staff_amount_record` 记录每一笔收入流水。

---

## 接口约定

- 所有业务接口统一挂在 **`/admin`** 前缀下（前端 Axios `baseURL` 也是 `/admin`），按资源再分子路径，例如：

| 路径 | 说明 |
| --- | --- |
| `POST /admin/login` | 登录 |
| `POST /admin/send-code` | 发送验证码（存 Redis） |
| `POST /admin/reset-password` | 重置密码 |
| `GET /admin/users` | 用户列表（可选分页） |
| `GET /admin/emp` | 家政人员列表 |
| `GET /admin/admins` | 管理员列表 |
| `GET /admin/package/list` | 套餐列表 |
| `PUT /admin/package/{id}/status` | 套餐上架/下架 |
| `POST /admin/order/create` | 创建订单 |
| `POST /admin/order/dispatch` | 派单 |
| `POST /admin/order/take` | 接单 |
| `GET /admin/chat/rooms` | 会话列表 |
| `WS /ws/chat` | 聊天 WebSocket |

- **分页约定**：不传 `pageNum` / `pageSize` 时返回完整数组；传了则返回 `{ total, pageNum, pageSize, records }`。
- **响应体**：统一为 `Result{ code, msg, data }`。

---

## 安全说明

- 数据库密码、阿里云 AccessKey / Secret、JWT secret 只存在于本地 `application.properties`，**不入库**；仓库历史中的此类文件已清理。
- 密码统一 BCrypt 加密；`PasswordUtil` 对历史明文数据做兼容校验，存量数据刷成 hash 后明文分支自然失效。
- 若曾将密钥提交到公开仓库，仅清理历史并不足够，**必须去阿里云控制台轮换 AccessKey、并修改数据库密码**。

---

## 说明

本项目为课程设计 / 毕业设计性质的全栈实践项目，代码结构与注释以可读性优先，未做生产级的多环境配置分离与容器化。部署到公网时请额外完成：HTTPS、数据库连接池调优、OSS 访问权限收敛、日志落盘与备份。

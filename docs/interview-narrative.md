# 面试叙事点 · 实现详解

> 本文对应 `resume-maker-m1-design.md` 第 10 节「面试叙事点」的七个模块。每个叙事点先给结论，再落到仓库中真实可指认的代码与表结构，最后附上该点可能被追问的问题与回答思路。
>
> 本文基于 0.0.2 版本代码整理：dev / test / prod 三套环境统一使用 MySQL 8.0，测试使用 Testcontainers 启动临时 MySQL 容器，不再使用 H2 文件库。

---

## 0. 总体架构（30 秒版本）

```
浏览器 React (Vite 5173 / Nginx 8088)
   │  同源 /api + Bearer JWT
   ▼
Spring Boot 8080 ── MyBatis-Plus ── MySQL 8.0（7 张业务表 + Flyway 历史表）
   │                                  │
   └── Redis 7（仅导出 2 秒防连点）    └── 用户行锁 / 订单行锁 / 条件 UPDATE
前端 PDF：@react-pdf/renderer 在浏览器生成 Blob → pdfjs-dist 逐页渲染预览
```

技术栈：Spring Boot 3.5 · Spring Security · MyBatis-Plus 3.5.9 · Flyway · MySQL 8.0 · Redis 7 · React 19 · TypeScript · Vite 6 · Tailwind 4 · TanStack Query · zustand · Axios · @react-pdf/renderer 4.3.1 · Testcontainers · GitHub Actions。

数据库 7 张表：`users`、`templates`、`resumes`、`plans`、`orders`、`payment_logs`、`export_logs`，DDL 与种子数据由 `backend/src/main/resources/db/migration/mysql/` 下两条 Flyway 迁移管理，三套环境执行同一套脚本。

---

## 1. 认证：BCrypt 盐值 · JWT 无状态 · 密码错误统一提示防枚举

### 1.1 BCrypt 盐值

**代码位置**：`backend/.../config/SecurityConfig.java`、`backend/.../service/AuthService.java`

```java
// SecurityConfig.java
@Bean
PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder();   // 默认强度 10，每次生成随机盐，盐随哈希一同存储
}
```

注册流程（`AuthService.register`）：

1. 校验密码 UTF-8 字节数 ≤ 72——这是 BCrypt 的算法上限，超长会被静默截断，所以必须提前拦截；
2. 查询用户名唯一，重复返回 `409 用户名已被使用`；
3. `passwordEncoder.encode()` 生成「盐 + 哈希」写入 `users.password_hash`；
4. 实体上标 `@JsonIgnore`，`passwordHash` 永远不会出现在任何 API 响应里。

登录流程（`AuthService.login`）用 `passwordEncoder.matches()` 校验，而不是把哈希取出来自己比。

**为什么 BCrypt 而不是 MD5/SHA**：内建随机盐（同密码两次哈希不同，无法彩虹表）；工作因子可调，抗暴力破解。此处还可以主动讲：`$2a$10$...` 前缀里的 `10` 就是 cost，越大越慢。

### 1.2 JWT 无状态

**代码位置**：`backend/.../security/TokenService.java`、`JwtAuthenticationFilter.java`、`config/SecurityConfig.java`

```java
// TokenService.create —— 只放两个最小 claim
Jwts.builder()
    .subject(userId.toString())
    .claim("username", username)
    .issuedAt(new Date())
    .expiration(Date.from(Instant.now().plusSeconds(lifetimeHours * 3600)))  // 2 小时
    .signWith(signingKey)   // HMAC-SHA256
    .compact();
```

关键设计点：

- **密钥强校验**：`TokenService` 构造时要求 `app.jwt-secret` 至少 32 字符，否则启动直接抛 `IllegalStateException`——防止有人用弱密钥上线后被伪造 token。
- **无状态会话**：`SessionCreationPolicy.STATELESS`，服务端不存任何 session，每次请求由 `JwtAuthenticationFilter` 验签后把 `userId` 放进 `SecurityContext`（`principal` 就是 Long 类型的用户 ID，`UserController.current` 里直接 `(Long) authentication.getPrincipal()`）。
- **白名单端点**：`PUBLIC_ENDPOINTS` 放行注册/登录/模板浏览/套餐/配置/支付回调/健康检查，其余全部 `authenticated()`。
- **统一 401 响应**：`UnauthorizedEntryPoint` 返回统一 JSON 结构；前端 Axios 拦截器收到 401 就登出并跳转 `/login?redirect=...`，登录后回跳原页面。

### 1.3 密码错误统一提示防枚举

```java
// AuthService
private static final String UNKNOWN_USER_HASH =
    "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
// login 中：
String passwordHash = user == null ? UNKNOWN_USER_HASH : user.passwordHash;
boolean passwordMatches = passwordEncoder.matches(credentials.password(), passwordHash);
require(user != null && passwordMatches, 401, "用户名或密码错误");
```

- 用户不存在时**也执行一次 BCrypt 匹配**（对固定哈希），让「用户不存在」和「密码错误」两种分支耗时几乎一致，消除时间侧信道，无法通过响应时间探测用户名是否存在。
- 文案永远只有一句「用户名或密码错误」，不区分是哪个错了。

**诚实的 trade-off（面试加分点）**：注册接口的 `409 用户名已被使用` 仍然会暴露用户名存在性。这是产品上为了让用户立刻知道「为什么注册失败」的取舍；如果要彻底防枚举，注册应该改成「注册成功，请到邮箱确认」，本项目因为无邮箱验证功能（当前边界）没有这么做。

### 1.4 该模块的追问预案

- **JWT 被窃取了怎么办？** 目前 2 小时短时效 + 不存敏感 claim；无刷新 token。可以进一步说：生产上会加 Redis 黑名单/登出失效 + refresh token 轮换。
- **token 里为什么不放会员状态？** 见第 6 节——放进去会导致「到期了还在享受权益」的窗口期，实时查库才能立刻生效。
- **怎么防止 token 重放？** 支付回调不走 JWT（白名单 + HMAC 验签），见第 5 节。

---

## 2. 数据：简历 JSON 存储 vs 拆表的取舍 · 金额用「分」

### 2.1 简历 JSON 存储 vs 拆表

**表结构**（`db/migration/mysql/V1__schema.sql`）：

```sql
CREATE TABLE resumes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,          -- 与模板表解耦，只存引用
  title VARCHAR(100) NOT NULL,
  content JSON NOT NULL,                -- 整份简历内容作为 MySQL JSON 文档
  version INT NOT NULL DEFAULT 0,       -- 乐观锁版本号
  ...
);
```

简历正文（基本信息、教育/工作/项目经历、技能、自定义模块）整体存为一个 JSON 文档，`plans.features` 也是 JSON。前端 `types/index.ts` 里的 `Content` 类型与后端 `ResumeContentValidator` 的校验字段一一对应。

**为什么不拆成 `educations`、`experiences` 多张表？**

1. **访问模式**：简历的读写永远是「按用户/按简历整篇拉取」，没有任何「按公司名/技能关键词检索所有用户简历」的场景，拆表带来的 JOIN 与索引没有收益；
2. **结构演进**：编辑器模块（教育/工作/项目/技能/自定义）是产品迭代最频繁的地方，文档型存储改字段不需要 DDL 迁移；
3. **行数**：一份简历的条目有限（服务端限制每模块 ≤ 100 条、整体 ≤ 200KB），不存在单文档过大的问题。

**服务端必须校验 JSON**（不能信任前端）：

```java
// ResumeContentValidator
content.isObject() && content.toString().length() <= 200000
content.path("basic").isObject()                 // 基本信息必填
phone.matches("[+0-9()\\- ]{6,25}")              // 电话格式
每个模块 isArray() && size() <= 100
start.compareTo(end) <= 0                        // 结束时间不早于开始时间
```

**什么时候会拆表？** 如果未来做「简历内容全文搜索」「按技能匹配岗位」「统计某专业求职趋势」，JSON 的查询能力就不够了，MySQL 8 可以用 JSON 虚拟列索引或直接拆表。讲出「选型是跟着查询模式走的」就抓住了本质。

### 2.2 金额用「分」

- `plans.price`、`orders.amount` 都是 `INT`：月度会员 `1990`（19.90 元）、年度 `9900`（99.00 元），见 `V2__seed.sql`；
- **服务端定价**：`OrderService.create` 里金额取 `plan.price`，前端传多少都不算数；
- **回调金额校验**：`PaymentService.callback` 里 `order.amount.equals(callback.amount())`，防止渠道侧金额被篡改或错单。

**为什么不用 DECIMAL/FLOAT**：浮点计算有二进制精度误差（`0.1 + 0.2`），金融场景按最小单位整数存储是行业惯例，展示时前端除以 100。

---

## 3. 编辑器：数据/模板分离 · A4 预览与导出一致性

### 3.1 数据/模板分离

- `resumes` 表只存 `template_id` 外键 + `content` JSON；模板只是 `templates` 表里的一行样式元数据（`color`、`style`、`is_vip`、`sort`）。
- 前端渲染完全由「内容 + 模板」两个 props 驱动：`ResumePreview({ content, template })` 和 `PdfDocument({ content, template })` 签名一致——换模板就是换一个 prop，内容不动。
- 后端在创建/保存/复制简历时都调用 `TemplateService.checkAccess`，VIP 模板对非会员返回 403，防止前端绕过。
- 模板使用量 `used_count = used_count + 1` 原子累加，`recordUse` 用于热度统计。

### 3.2 A4 预览与导出一致性

一致性靠**单一数据源**保证：

```ts
// frontend/src/features/resumes/model/resumeSections.ts
export function sections(content) { ... }   // 唯一的「模块顺序 + 结构」定义
```

- HTML 实时预览（`ResumePreview.tsx`）和 PDF 文档（`PdfDocument.tsx`）都调用同一个 `sections(content)`，所以两者的模块顺序（个人简介→教育→工作→项目→技能→自定义）、字段展示、空模块过滤逻辑不可能漂移；
- 编辑器右侧预览标注「A4 标准尺寸 · PDF 导出时根据内容自动分页」；
- PDF 侧 `<Page size="A4">`，padding 与预览纸张比例对应；
- **回归测试兜底**：`frontend/scripts/verify-pdf.mjs` 用生产模板组件生成四种模板 PDF，校验中文渲染、A4 尺寸、水印，并构造 15 个工作条目的长简历验证自动分页后条目无丢失；`npm run test:pdf` 挂在 CI 上。

编辑器本身还有三个值得讲的工程细节（都在 `editor/hooks/useResumeDraft.ts`）：

- **800ms 防抖自动保存 + 串行提交**：pendingSave 复用同一个 Promise，避免连点产生并发写；
- **乐观锁冲突处理**：保存带 `version`，服务端 `WHERE version = ?` 条件更新，`updated == 0` 返回 409「简历已在其他窗口更新」，本地草稿仍保留在 localStorage（`rc-draft:userId:resumeId`），用户不会丢字；
- **离线恢复**：断网时改动留在本地草稿，`online` 事件触发重试保存；`beforeunload` 有未保存更改时弹浏览器确认。

---

## 4. 导出：react-pdf 分页与中文字体 · 导出次数限流（Redis + 每日重置）

### 4.1 react-pdf 分页与中文字体

**依赖**：`@react-pdf/renderer@4.3.1`（并 pin `@react-pdf/layout@4.4.1` override），配套 `pdfjs-dist@5.4.296` 做客户端逐页预览。

**中文字体**（`frontend/src/features/pdf/fonts.ts`）：

```ts
Font.register({
  family: "NotoSansSC",
  fonts: [
    { src: "/fonts/NotoSansSC-Regular.ttf", fontWeight: 400 },
    { src: "/fonts/NotoSansSC-Bold.ttf", fontWeight: 700 },
  ],
});
```

- 字体文件内置在前端静态资源 `public/fonts/`（OFL 开源许可），不依赖用户操作系统字体，PDF 在任何设备打开中文都一致；
- 用 `scripts/prepare-fonts.py` 校正字体内部名称，避免常规/粗体在 PDF 字体缓存中混淆（这是 react-pdf 中文的经典坑）。

**中文换行**（`PdfDocument.tsx`）：

```ts
const pdfText = (value = "") =>
  value.replace(/([\u2e80-\u9fff])(?=[\u2e80-\u9fff])/g, "$1\u200a");
```

在相邻 CJK 字符之间插入 hair-space（U+200A）作为显式断行机会，配合 `registerHyphenationCallback` 只在该处断行——中文段落可以自然折行，又不会像英文连字符那样在原文里插入可见的 `-`。

**分页控制**：

- 每个 section 标题 `minPresenceAhead={45}`、条目首行 `minPresenceAhead={35}`——标题/条目头部至少留 45/35pt 空间才在本页渲染，否则整体推到下一页，杜绝「标题孤悬页尾」；
- 免费水印用 `<Text fixed>` 渲染在每页页脚，会员导出时整个组件不挂载；
- 生成在浏览器完成（Blob），60 秒超时保护，`Promise.race` 兜底。

### 4.2 导出次数限流（Redis + 每日重置）

**核心是「两阶段预留 → 确认」+ 数据库行锁串行**（`backend/.../service/ExportService.java`）：

```java
@Transactional
public ExportLog reserve(userId, resumeId) {
  User user = userMapper.lock(userId);          // ① 用户行锁，串行化同用户并发
  ...
  releaseExpiredReservations(userId);           // ② 回收 10 分钟失联的 PENDING
  if (!user.isMember()) checkDailyQuota(userId); // ③ 免费用户当日额度
  checkRepeatedClick(userId);                    // ④ Redis 2 秒防连点
  exportLogMapper.insert(PENDING);               // ⑤ 预留
}
```

逐层拆解：

1. **`userMapper.lock`**：`SELECT * FROM users WHERE id = ? FOR UPDATE`。同一用户的并发导出在数据库层面排队，额度检查和预留是串行的，不会两个请求同时看到「还有 1 次额度」；
2. **每日重置**：`checkDailyQuota` 统计 `created_at >= LocalDate.now().atStartOfDay()` 且状态为 `PENDING/COMPLETED` 的记录数，免费用户 `< 1`，即每天 1 次，按**服务器时区（Asia/Shanghai）自然日**重置。用数据库日志计数而不是计数器变量，天然抗重启、可审计；
3. **失联回收**：超过 10 分钟的 `PENDING` 在下次 `reserve` 时被置为 `FAILED`，占用额度的「僵尸预留」自动释放；
4. **Redis 只做短时防连点**：`setIfAbsent("export:burst:userId", "1", 2s)`，2 秒内重复点击返回 429「操作太快」。Redis 挂掉时捕获 `RedisConnectionFailureException` 直接放行——**额度安全不依赖 Redis**，它只是体验层。这是有意偏离初版设计文档（「Redis 主计数 + DB 日志」）的：Redis 主计数在 Redis 故障时可能超额放行，DB 日志 + 行锁则不会；
5. **失败释放**：前端生成失败调 `/exports/{id}/complete { success:false }`，后端把 `PENDING → FAILED`，本次不占额度（仅对 10 分钟内的预留生效）；
6. **会员跳过额度**：`user.isMember()` 实时判断，不限次数、无水印（前端 `watermark = !isMember`）。

---

## 5. 支付：回调验签 + 幂等（状态条件 UPDATE）· 订单状态机 + 定时过期

### 5.1 回调验签 + 幂等

**验签**（`PaymentSignatureService.java`）：

```java
String canonical = orderNo + "\n" + amount + "\n" + transactionId
                 + "\n" + status + "\n" + timestamp;
HmacSHA256(secret, canonical)  // 十六进制
```

校验四层：

1. **格式**：字段非空、`transactionId` 限定 `[A-Za-z0-9_-]{1,100}`、状态只能是 `SUCCESS/FAILED`；
2. **时效**：`|now - timestamp| <= 300s`，防重放旧回调；
3. **签名**：`MessageDigest.isEqual` 常数时间比较，防时序攻击；
4. **金额**：`order.amount.equals(callback.amount())`。

**幂等**（`PaymentService.callback`）：

```java
signatureService.verify(callback, signature);
User user = userMapper.lock(found.userId);   // 统一锁顺序：先用户、后订单
Order order = orderMapper.lock(found.id);
require(order.amount.equals(callback.amount()), 400, "支付金额不匹配");

if (order.status.equals("PAID")) {           // 已支付 → 幂等分支
  require(callback.status().equals("SUCCESS")
      && Objects.equals(order.transactionId, callback.transactionId()), 409, "交易信息不匹配");
  paymentLogService.record(order, callback, "DUPLICATE");
  return "success";                           // 重复回调直接应答成功，不重复加权益
}
require(order.status.equals("CREATED") && order.expiredAt.isAfter(now), 409, "订单已关闭或已过期");
if (callback.status().equals("FAILED")) {    // 失败回调只记日志，订单保持 CREATED 等后续结果
  paymentLogService.record(order, callback, "FAILED");
  return "success";
}
markPaid(order, callback.transactionId());   // 条件 UPDATE 才是真正的幂等闸门
extendMembership(user, plan.durationDays);
```

**真正的并发安全在 `markPaid`**：

```java
UPDATE orders SET status='PAID', paid_at=?, transaction_id=?
WHERE id=? AND status='CREATED'
// updated == 1 才成功，否则 409「订单状态已改变」
```

两个并发回调同时到达时，只有第一个能把 `CREATED → PAID`，第二个走到 `PAID` 幂等分支返回 success——**加锁保证串行，条件 UPDATE 保证状态只迁移一次**，双保险。加权益（`extendMembership`）与改订单状态在**同一个事务**里，要么都成功要么都回滚，不会出现「订单付了钱、会员没加」。

每个回调都往 `payment_logs` 记一条（`SUCCESS/FAILED/DUPLICATE/REFUNDED`），`transaction_id` 有唯一约束，审计链完整。

### 5.2 订单状态机 + 定时过期

状态机：

```
CREATED ──回调 SUCCESS（条件 UPDATE）──▶ PAID ──refund()──▶ REFUNDED
   │
   └──过期（定时任务 / 懒过期）──▶ EXPIRED
   （回调 FAILED：只记日志，仍为 CREATED，等待渠道最终结果）
```

- **创建即计时**：`OrderService.create` 生成 `RC + 日期 + 16 位随机` 订单号，`expired_at = now + 15 分钟`；
- **下单幂等**：`(user_id, request_key)` 唯一约束 + 先查后插，前端重复点击/网络重试不会产生两笔订单；
- **定时过期**（`@EnableScheduling` + `@Scheduled(fixedDelay = 60000)`）：

```java
UPDATE orders SET status='EXPIRED'
WHERE status='CREATED' AND expired_at < now()
```

每分钟扫一次，条件 UPDATE 批量关闭过期订单；`idx_orders_expiry(status, expired_at)` 索引支撑这个扫描；

- **懒过期兜底**：查询订单详情时若发现 `CREATED` 且已过期，先 `expire()` 再返回——不依赖定时任务时机。

退款（dev 演示）同样走 `PAID → REFUNDED` 条件 UPDATE，会员扣回该套餐完整天数、最低回退到当前时间，并记 `REFUNDED` 日志。

---

## 6. 会员：member_expire_at 续期叠加 · 会员状态实时查库不入 JWT

### 6.1 续期叠加

```java
// PaymentService.extendMembership
LocalDateTime base = user.memberExpireAt != null && user.memberExpireAt.isAfter(now)
    ? user.memberExpireAt   // 还未到期 → 从现有到期时间往后加
    : now;                  // 已过期 → 从现在重新起算
UPDATE users SET member_expire_at = base.plusDays(days) WHERE id = ?
```

- 会员期内续费：到期时间**叠加**而不是覆盖（剩 10 天 + 30 天 = 40 天）；
- 已过期再买：从购买时刻重新计算；
- 退款：扣回该套餐完整天数，且 `member_expire_at` 最低回退到当前时间，不会扣成过去的时间戳；
- 全程在用户行锁保护下计算（`userMapper.lock`），并发支付/退款不会算出脏值。

### 6.2 会员状态实时查库不入 JWT

- JWT 的 claim 只有 `userId` + `username`（`TokenService.create`），**没有** `memberExpireAt` 或 `isMember`；
- 每次需要会员状态时实时查库：`User.isMember()` 就是 `memberExpireAt != null && isAfter(now)`；
- 用途覆盖：导出额度判断（`ExportService.reserve`）、VIP 模板权限（`TemplateService.checkAccess`）、会员中心接口（`UserService.membership`）、水印开关（前端 `isMember` 来自实时查询的用户信息）。

**为什么这么做**：如果会员状态写进 JWT，那「到期那一刻」用户手里的 token 在 2 小时有效期内仍然显示会员——要么加 Redis 黑名单，要么等 token 过期。实时查库则到期立刻生效，不存在特权窗口。代价只是每个需要判断的请求多一次主键查询，而这个查询和业务查询本来就发生在同一事务/同一连接里。

---

## 7. 部署：Docker Compose 编排 · 密钥不进仓库

### 7.1 编排

两份文件，两种形态：

- **`compose.yml`**（基础编排）：MySQL 8.0 + Redis 7 + 后端 + 前端/Nginx，全容器运行。MySQL 带健康检查，后端 `depends_on: condition: service_healthy`；数据库用命名数据卷 `mysql-data` / `redis-data` 持久化；MySQL/Redis **不暴露主机端口**，只在 Docker 内网互通；前端只绑 `127.0.0.1:8088`。
- **`compose.dev.yml`**（开发覆盖层）：只把 MySQL、Redis 的端口映射到 `127.0.0.1:3306/6379`，让**本机跑的 Java 进程和 Vite** 连接容器里的数据库：

```powershell
docker compose -f compose.yml -f compose.dev.yml up -d --wait mysql redis
```

环境切换不靠文件靠变量：`SPRING_PROFILES_ACTIVE: ${APP_PROFILE:-dev}`。dev 与 prod 的唯一差别在 Spring Profile 层（DemoSeed 演示账号、`/api/pay/mock` 模拟支付、`/api/orders/{id}/refund` 退款演示都是 `@Profile("dev")`；prod 关掉 swagger/springdoc）。数据库本身 dev 和 prod 完全一致。

后端 Dockerfile 用多阶段构建（maven 3.9 + temurin 21 构建 → temurin 21 JRE Alpine 运行），非 root 用户运行。

### 7.2 密钥不进仓库

- `.gitignore` 忽略 `.env`；仓库里只有 `.env.example`（占位符）；
- `init-env.ps1` 只在 `.env` 不存在时生成，用系统 CSPRNG 生成 6 个 32 字节 Base64 随机密钥（数据库密码、Redis 密码、JWT 密钥、支付签名密钥），不覆盖已有配置；
- **compose 强制存在**：`${DB_PASSWORD:?Run init-env.ps1 first}`——没有 `.env` 时 compose 直接报错拒绝启动，而不是用空密码跑起来；
- **后端启动强校验**：`TokenService` 要求 JWT 密钥 ≥ 32 字符、`PaymentSignatureService` 要求支付密钥非空，缺失即启动失败。dev 配置里的弱默认值只是本地兜底，且明文标注 `development-only`；
- 本地后端通过 `spring.config.import: optional:file:../.env` 从项目根目录读取同一份 `.env`，与 compose 共享配置，不会出现两份密钥漂移。

### 7.3 CI 与验证

- GitHub Actions：JDK 21（temurin）+ Node 22，后端 `mvn verify`（含 Spotless 格式检查、Testcontainers MySQL 集成测试），前端 `npm run check`（oxlint + oxfmt + tsc + 生产构建）+ `npm run test:pdf`（PDF 分页回归）；
- 11 项集成测试用 **Testcontainers** 自动拉起 MySQL 8.0 临时容器（`MySqlTestConfiguration` + `@ServiceConnection` 自动注入连接参数），执行与生产相同的 Flyway 迁移，测试结束自动销毁——测试库和生产库同方言、同 DDL，杜绝「测试用 H2 全绿、线上 MySQL 炸」的经典事故；
- 测试覆盖点（对应叙事点，可以直接背）：BCrypt/重复注册、未登录拦截、跨账号资源访问、VIP 模板校验、乐观锁 409 冲突、并发下单幂等、支付回调并发重放、验签/金额/订单过期、退款与会员续费叠加、并发导出额度、失败回收。

---

## 8. 面试常见深挖问题速查

| 问题                           | 一句回答                                                                                                                        |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| 为什么简历存 JSON 不拆表？     | 访问模式是整篇读写，没有跨简历内容检索；结构演进快，文档型免迁移；拆表收益为零。                                                |
| 金额为什么用 int？             | 分做单位消除浮点误差；服务端定价 + 回调金额校验防篡改。                                                                         |
| 并发导出怎么不超额？           | 用户行锁 `FOR UPDATE` 串行 + 数据库日志计数自然日额度，Redis 只做 2 秒防连点，挂了自动降级。                                    |
| 并发支付回调怎么不重复加权益？ | 锁顺序统一（用户→订单）+ `WHERE status='CREATED'` 条件 UPDATE，`affected == 1` 才是闸门；重复回调走 PAID 幂等分支返回 success。 |
| 订单怎么过期？                 | 15 分钟 `expired_at`；定时任务每分钟条件 UPDATE 批量关闭 + 查询时懒过期兜底。                                                   |
| 会员到期立刻生效怎么保证？     | 状态不入 JWT，每次实时查库 `isMember()`，零特权窗口。                                                                           |
| JWT 被盗/过期怎么办？          | 2 小时短时效；前端 401 拦截统一登出回跳；生产扩展 refresh token + Redis 黑名单。                                                |
| 密钥怎么管理？                 | `.env` 不入库；脚本生成随机密钥；compose `${VAR:?}` 强制存在；后端启动时校验强度，缺失即拒绝启动。                              |
| 测试怎么保证和生产一致？       | Testcontainers 真 MySQL 8.0 + 同一套 Flyway 脚本，无 H2 方言风险。                                                              |
| PDF 中文为什么能正常显示？     | 静态内置 Noto Sans SC 两字重 + 字体名称校正 + CJK 显式断行机会（U+200A）+ `minPresenceAhead` 防标题孤悬。                       |

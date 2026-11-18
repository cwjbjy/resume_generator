# 简历工坊 · Resume Craft

在线简历编辑与 PDF 导出：注册登录 → 选择模板 → 在线编辑（左表单 / 右 A4 实时预览）→ 导出 PDF → 开通会员解锁 VIP 模板与不限次导出。

## 一、本地开发启动

### 前置条件

| 依赖            | 版本                                            |
| --------------- | ----------------------------------------------- |
| Docker Desktop  | Linux 容器                                      |
| JDK             | 21                                              |
| Node.js         | 20.19+ / 22                                     |
| Git（Git Bash） | 2.x（Windows 上用 Git Bash 执行 `init-env.sh`） |

### 1. 启动依赖容器

```bash
cd /d/cwj/resume_generator   # Windows 在 Git Bash 中执行；服务器用实际路径
sh init-env.sh
docker compose up -d --wait mysql redis
```

- `init-env.sh` 仅在根目录 `.env` 不存在时生成随机密钥；`.env` 已存在时跳过。Windows 需在 Git Bash（自带 openssl）中执行，服务器直接执行。
- MySQL、Redis 映射到 `127.0.0.1:3306`、`127.0.0.1:6379`，供本机 Java/Vite 进程连接。端口冲突时在 `.env` 中改 `DB_PORT` / `REDIS_PORT`，再重跑该命令。
- MySQL 首次启动由 Flyway 自动执行迁移；数据保存在命名卷 `mysql-data`，重建容器不丢失。

### 2. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

或在 IDEA 中运行 `ResumeCraftApplication`。后端从项目根目录或 `backend/` 启动都会读取根目录 `.env`；默认 `dev` profile，首次启动自动创建演示账号。

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

`npm install` 仅首次需要。

### 访问地址

| 用途     | 地址                                                                            |
| -------- | ------------------------------------------------------------------------------- |
| 前端     | <http://127.0.0.1:5173>                                                         |
| 健康检查 | <http://127.0.0.1:8080/actuator/health>                                         |
| 接口文档 | <http://127.0.0.1:8080/swagger-ui/index.html>、<http://127.0.0.1:8080/doc.html> |

演示账号（仅 `dev` 环境自动创建）：`user01 / 123456`（普通用户）、`vip01 / 123456`（会员）。

### 停止

```powershell
docker compose stop mysql redis
```

`stop` 只停进程，容器与数据卷保留，下次启动沿用原有数据。

## 二、打包部署

### 1. 本地测试部署（--build 现场构建）

前端 Nginx、后端、MySQL、Redis 全部运行在容器中，Java 构建与运行均使用容器内 JDK 21。

```bash
cd /d/cwj/resume_generator   # Windows 在 Git Bash 中执行；服务器用实际路径
sh init-env.sh
docker compose up --build -d
```

访问 <http://127.0.0.1:8088>。停止：

```powershell
docker compose stop
```

### 2. 构建并推送镜像（阿里云 ACR）

服务器不做现场构建，镜像推送 ACR，服务器只拉取运行，避开小内存机器上的 Maven/npm 构建峰值。

**推荐方式：推送 git tag 由 CI 自动构建推送**（见 `.github/workflows/ci.yml`）。每次发版把版本号 +0.0.1 作为 tag 推到 GitHub，CI 自动构建并推送 `backend:<tag>`、`frontend:<tag>` 到 ACR；MySQL/Redis 已在服务器，不参与。前置：GitHub Secrets 中配置 `ACR_USERNAME`、`ACR_PASSWORD`（ACR 访问凭证的固定密码）。

```powershell
git tag 1.0.1
git push origin 1.0.1
```

### 3. 云服务器部署（拉取镜像运行）

服务器从 GitHub clone 仓库，镜像由 CI 构建推送后自动部署（见 `.github/workflows/ci.yml` 的 deploy job）。服务器只需一次性准备：

```bash
# 服务器：首次准备（clone 仓库 + 生成 .env + 授权 CI 的部署密钥）
cd /opt && git clone <仓库地址> resume-craft && cd resume-craft
sh init-env.sh
ssh-keygen -t ed25519 -f ~/.ssh/deploy_ci -N ''        # 生成专用部署密钥对
cat ~/.ssh/deploy_ci.pub >> ~/.ssh/authorized_keys      # 公钥授权
cat ~/.ssh/deploy_ci                                    # 私钥存入 GitHub Secrets 的 DEPLOY_SSH_KEY
```

当前为演示阶段，`init-env.sh` 生成 `APP_PROFILE=dev`；正式运营时改为 `prod`，重启 backend 即可关闭模拟支付、退款演示与默认账号。

此后每次发版只需推送 git tag：

```powershell
git tag 1.0.1
git push origin 1.0.1
```

CI 自动完成：构建推镜像到 ACR → SSH 登录服务器执行 `git fetch/reset + docker compose up -d --wait`（自动拉新镜像并重建容器，数据卷不受影响），`compose.prod.yml` 无需任何修改。GitHub Secrets 需配置 `ACR_USERNAME` / `ACR_PASSWORD`（ACR 推送凭据）与 `DEPLOY_HOST` / `DEPLOY_USER` / `DEPLOY_SSH_KEY`（服务器 SSH 信息）。

## 三、已实现功能

| 模块     | 功能                                                                                                                                          |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| 账号     | 注册、BCrypt 密码哈希、2 小时 JWT、登录回跳、过期处理、昵称与邮箱修改                                                                         |
| 模板     | 4 套模板（2 免费 / 2 会员）；筛选、搜索、详情、VIP 权限校验                                                                                   |
| 编辑     | 基本信息、教育、工作、项目、技能标签、自定义模块；增删改、拖动/按钮排序、日期校验、50%–100% 缩放、实时预览、切换模板                          |
| 保存     | 800ms 防抖、串行提交、version 乐观锁、本地草稿、离线保留、网络恢复重试、手动保存                                                              |
| 我的简历 | 列表、重命名、复制、二次确认删除；所有资源由后端校验所有者                                                                                    |
| 导出     | 4 套模板真实 PDF、A4 自动分页、内置静态中文字体、免费水印、PDF.js 逐页预览与下载、导出记录                                                    |
| 权益     | 免费每天 1 次（按服务器自然日重置）；会员不限次数；会员状态实时查库                                                                           |
| 支付     | 月度 19.9 元 / 30 天、年度 99 元 / 365 天；服务端定价、下单幂等、15 分钟过期、模拟支付、HMAC 验签、金额校验、并发回调幂等、续期叠加、退款演示 |
| 工程     | Spring Boot 3.5 · MyBatis-Plus · Flyway · React 19 · TypeScript · Vite 6 · Tailwind 4 · ahooks · zustand · Docker Compose · CI                |

编辑器面向桌面（最小宽度 1024px），模板、登录、会员、个人中心提供窄屏布局；HTML 预览与 PDF 导出共用同一模块顺序和内容结构。

登录和注册返回 `token`、`id`、`username`。前端随后携带 Bearer Token 调用 `GET /api/users/me` 获取当前用户的完整资料，再保存会话并进入目标页面。资料接口根据当前认证身份确定用户。

### 边界说明

- **支付为沙箱演示，不产生真实扣款。** `prod` 下禁用模拟支付、退款演示与默认账号；真实微信/支付宝需商户资质、渠道 SDK、对账与上线配置。
- **导出防重由 MySQL 控制**：同一用户同时最多一个 `PENDING` 任务，重复申请返回 409，完成或失败后可再次申请；免费额度采用「预留 → 生成 → 确认」两阶段，预留 10 分钟未确认自动回收。Redis 仅作后续功能预留，不参与业务判断。
- **PDF 在浏览器生成**：适合作品集范围；下载确认仅表示浏览器已生成文件。若需防止绕过前端导出，应改为服务端生成并鉴权下载。
- 退款仅演示状态变更与会员天数扣回，无真实资金流转。
- 未实现：AI 能力、公开分享、密码找回、自动续费、头像上传。

## 四、目录结构

后端采用按职责分层的包结构，源码根目录为 `backend/src/main/java/cn/resumecraft`。

```text
cn.resumecraft
├── controller    HTTP 接口入口
├── service       业务逻辑
├── mapper        数据库访问
├── entity        数据库实体
├── dto           请求与响应数据
├── common        ApiResponse 统一响应
├── exception     业务异常与全局异常处理
├── config        Spring 配置
├── security      JWT 认证与鉴权
├── bootstrap     开发环境数据初始化
└── ResumeCraftApplication.java
```

前端源码位于 `frontend/src`，按业务组织页面、组件、hooks 和接口调用。

- `features/<业务>/api` 存放业务接口，`lib/http.ts` 封装共享 HTTP 客户端。
- `features/resumes` 集中简历接口、数据模型、HTML 预览组件和 PDF 生成与预览。
- `app` 存放应用入口及运行配置接口，`components/ui`、`components/layout` 存放共享 UI 和布局。

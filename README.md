# CFAMS

基于微信小程序的校园固定资产管理系统。

## 仓库结构

- `CFAMS/`：Spring Boot 后端，当前使用 JDK 17。
- `CFAMS-web/`：Vue 3 管理端。
- 小程序将在后续开发时添加。

已完成若依基础框架裁剪与33张数据库表初始化脚本；校园业务接口和页面待开发。基于若依开发，保留各子目录的许可证及原作者说明。

## 本地运行

1. 使用 MySQL 8 创建空库 cfams，顺序执行 CFAMS/sql/01、02、03、04 开头的SQL。原始 ry_20260417.sql 仅为来源参考，不重复执行。
2. 配置后端环境变量：CFAMS_DB_PASSWORD、CFAMS_DRUID_PASSWORD、CFAMS_TOKEN_SECRET（自行生成随机密钥）；按环境设置 CFAMS_DB_URL、CFAMS_DB_USERNAME、CFAMS_REDIS_HOST、CFAMS_REDIS_PASSWORD、CFAMS_REDIS_DATABASE、CFAMS_UPLOAD_PATH。不要提交真实凭据。
3. 后端目录使用 JDK 17 执行 `mvn clean package`，再执行 `java -jar ruoyi-admin/target/ruoyi-admin.jar`，端口8081。
4. 前端目录执行 `npm install`、`npm run dev`，访问 http://127.0.0.1:5173；生产构建 `npm run build:prod`。
5. 若依初始化演示账号为admin，演示密码admin123，部署到公开环境前修改。

## 分支约定

- `dev`：唯一的日常开发分支，统一跟踪和提交后端、PC前端及后续小程序代码。
- `master`：保留可运行的稳定版本，功能验证通过后从 `dev` 合并。

前后端共用仓库根目录的一个 `.git`，所有代码都在同一个 `dev` 分支中维护，不再使用 `dev-server`、`dev-web`、`dev-app`。

本机统一工作目录为 `E:\毕业设计\CFAMS-repository`。后续修改该目录下的项目，不从旧的独立项目目录向此仓库推送。

日常在仓库根目录执行：

```bash
git switch dev
git pull --ff-only
# 修改代码并完成相关编译、测试后：
git status
git add .
git diff --cached --stat
git commit -m "feat: 实现资产分类管理"
git push
```

本地 `dev` 跟踪 `origin/dev`。提交前检查暂存文件，真实连接凭据、构建输出和本地环境配置不提交。前后端可以在同一次提交中完成配套修改。

阶段功能验证通过后创建 `dev → master` 的 Pull Request。合并后继续在 `dev` 开发；需要同步稳定分支上的新提交时，先保存当前修改，再执行 `git fetch origin`、`git merge origin/master`、`git push`。不对共享分支强制推送。

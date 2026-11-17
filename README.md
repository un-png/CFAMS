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

- master：可运行的集成版本，所有端代码都在这里。
- dev-server：后端开发，主要修改CFAMS目录。
- dev-web：PC前端开发，主要修改CFAMS-web目录。
- dev-app：小程序开发。

分支都是完整仓库快照，不是分别只存某一端代码。开始工作先拉取自己的开发分支并合并origin/master；完成一个可验证功能后提交、推送开发分支，创建PR合并到master。合并后其他开发分支再次同步master。不对共享分支强制推送。

现有dev分支暂未自动更新；首次开始开发时将本次master提交合并到对应dev分支。

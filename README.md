# CFAMS

基于微信小程序的校园固定资产管理系统。

## 仓库结构

- `CFAMS/`：Spring Boot 后端，使用 JDK 21。
- `CFAMS-web/`：Vue 3 管理端。
- 小程序将在后续开发时添加。

已完成若依基础框架裁剪与33张数据库表初始化脚本；校园业务接口和页面待开发。基于若依开发，保留各子目录的许可证及原作者说明。

后端父工程的 Maven 坐标为 `com.cfams:cfams`，Java 基础包统一为 `com.cfams`：

```text
CFAMS/
├── cfams-admin/       统一启动入口及系统管理接口
├── cfams-common/      公共模型与工具
├── cfams-framework/   认证、权限及基础配置
├── cfams-system/      系统服务及跨端共享业务能力
├── cfams-pc/          PC管理端业务
└── cfams-app/         微信小程序业务
```

`cfams-admin` 的启动类为 `com.cfams.CfamsApplication`，同时依赖并加载两个业务模块；`cfams-pc`、`cfams-app` 是同一后端中的业务JAR，不是两个独立启动服务。

业务模块采用“先按业务分包，再在业务包内分层”的目录结构，以资产业务骨架为例：

```text
cfams-pc/src/main/java/com/cfams/
└── asset/
    ├── controller/
    ├── domain/
    │   ├── dto/
    │   └── vo/
    ├── mapper/
    └── service/
        └── impl/
```

小程序采用同样结构，功能包直接放在 `com.cfams` 下，不再增加 `pc`、`app` 中间包。Mapper XML分别放在 `cfams-pc/src/main/resources/mapper/pc/asset`、`cfams-app/src/main/resources/mapper/app/asset`，保留端标识以避免资源路径冲突。

当前已按双端功能分析建立以下占位包，每个业务包包含 `controller`、`domain/dto`、`domain/vo`、`mapper`、`service/impl`：

| 模块 | 功能包 |
| --- | --- |
| cfams-pc | identity、profile、message、asset、qrcode、borrow、repair、inventory、scrap、statistics、attachment |
| cfams-app | identity、profile、message、asset、qrcode、borrow、repair、inventory、attachment |

所有包仅放置 `package-info.java`，不包含业务类或接口。类别、场所维护归入PC资产包，用户、角色、菜单等继续复用现有系统模块，不复制其实现。后续两端在相同包下新增类时，使用 `Pc`、`App` 等明确的端侧类名前缀，避免合并运行时出现相同全限定类名和Spring Bean名称；共享类只定义一次。

本次只建立包结构，没有新增业务接口或重复的表Mapper。跨端共享的实体、表操作和事务服务统一放在 `cfams-system` 的对应业务包，两个入口模块复用；端侧Mapper仅在确有专用查询需求时增加。端侧DTO、VO按业务命名，避免不同模块下相同简单类名造成MyBatis别名冲突。

## 本地运行

1. 使用 MySQL 8 创建空库 cfams，顺序执行 CFAMS/sql/01、02、03、04 开头的SQL。原始 ry_20260417.sql 仅为来源参考，不重复执行。
2. 核对 `cfams-admin/src/main/resources/application.yml` 与 `application-druid.yml` 中的MySQL、Redis、上传目录及Token配置；本机使用具体配置，发布前检查真实凭据。
3. 将 `JAVA_HOME` 指向JDK 21，在后端目录执行 `mvn clean package`，再执行 `java -jar cfams-admin/target/cfams-admin.jar`，端口8081。
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

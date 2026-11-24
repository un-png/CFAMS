# CFAMS

基于微信小程序的校园固定资产管理系统。

## 仓库结构

- `CFAMS/`：Spring Boot 后端，使用 JDK 21。
- `CFAMS-web/`：Vue 3 管理端。
- 小程序将在后续开发时添加。

已完成若依基础框架裁剪、33张数据库表初始化、PC页面与真实业务接口对接。校园接口包含74个HTTP操作（含业务选项接口）；小程序业务入口未开发。基于若依开发，保留各子目录的许可证及原作者说明。

后端父工程的 Maven 坐标为 `com.cfams:cfams`，Java 基础包统一为 `com.cfams`：

```text
CFAMS/
├── cfams-admin/       统一启动入口及系统管理接口
├── cfams-common/      公共模型与工具
├── cfams-framework/   认证、权限及基础配置
├── cfams-system/      若依系统服务与Mapper
├── cfams-pc/          PC管理端业务
└── cfams-app/         微信小程序业务
```

`cfams-admin` 的启动类为 `com.cfams.CfamsApplication`，同时依赖并加载两个业务模块；`cfams-pc`、`cfams-app` 是同一后端中的业务JAR，不是两个独立启动服务。

业务模块采用“先按业务分包，再在业务包内分层”的目录结构，以资产业务为例：

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

后续小程序采用同样结构，功能包直接放在 `com.cfams` 下，不再增加 `pc`、`app` 中间包。Mapper XML分别放在 `cfams-pc/src/main/resources/mapper/pc/asset`、`cfams-app/src/main/resources/mapper/app/asset`，保留端标识以避免资源路径冲突。

当前保留以下实际业务包；没有实现的占位包和空目录已清理：

| 模块 | 功能包 |
| --- | --- |
| cfams-pc | identity、message、asset、qrcode、borrow、repair、inventory、scrap、statistics、attachment、campus |
| cfams-app | 暂无实际业务包，仅保留Maven模块 |

类别、场所维护归入PC资产包，用户、角色、菜单等继续复用现有系统模块，不复制其实现。两端在相同包下新增类时，使用明确的端侧类名，避免全限定类名和Spring Bean名称冲突；共享类只定义一次。

PC接口包括人员/绑定、类别场所、台账/导入导出、二维码、借还审核交接、维修派工核验、盘点、两级报废、消息、附件和统计。17个共享表实体位于 `cfams-common/src/main/java/com/cfams/common/entity`；业务Controller、Service接口、`service/impl`实现、`domain/dto`、`domain/vo`和Mapper均位于 `cfams-pc/src/main/java/com/cfams/对应业务`，XML位于 `cfams-pc/src/main/resources/mapper/pc/对应业务`。`cfams-system`不再承载校园PC业务。端侧DTO明确限制可写字段，小程序入口仍未实现。

业务代码采用普通JavaBean、明确类型、传统switch及显式类型转换，不使用record、var、switch表达式或instanceof模式变量。实体、DTO、VO属性访问方法统一由Lombok `@Getter`、`@Setter`生成；显式业务方法与构造器有中文用途说明。JDK 21运行配置不变。DTO/VO按“业务＋用途＋DTO/VO”命名，例如`AssetQueryDTO`、`AssetSaveDTO`、`BorrowAuditDTO`、`RepairAssignDTO`、`AssetImportResultVO`。

简单固定的单表SQL使用Mapper注解，动态筛选、多表关联、批量操作及复杂统计保留XML；同一方法只定义一份SQL。业务查询不使用`FOR UPDATE`。资产及单据的已有状态条件更新与业务事务保留，履历和通知仍随事务提交；普通查询不再提供跨单据的串行保护，例如不同借用单并发审核的时段冲突检查仍存在竞态。PC接口实时检查账号和权限，数据范围沿用角色配置。统计响应区分当前分类/存量与事件时间趋势。

## 本地运行

1. 使用 MySQL 8 创建空库 cfams，按01至07顺序执行CFAMS/sql中初始化脚本；已有数据库不要重复执行01至03。07只新增维修/盘点任务资格，不自动授予普通角色。原始ry_20260417.sql仅为来源参考。
2. 核对 `cfams-admin/src/main/resources/application.yml` 与 `application-druid.yml` 中的MySQL、Redis、上传目录及Token配置；本机使用具体配置，发布前检查真实凭据。
3. 将 `JAVA_HOME` 指向JDK 21，在后端目录执行 `mvn clean package`，再执行 `java -jar cfams-admin/target/cfams-admin.jar`，端口8081。
4. 前端目录执行 `npm install`、`npm run dev`，访问 http://127.0.0.1:5173；生产构建 `npm run build:prod`。
5. 若依初始化演示账号为admin，演示密码admin123，部署到公开环境前修改。
6. OSS配置放在application.yml根节点`alioss`，字段为`endpoint`、`access-key-id`、`access-key-secret`、`bucket-name`，Endpoint须与桶地域一致。也兼容`campus.oss.*`与`CFAMS_OSS_*`环境变量。附件私有，授权后签名地址有效期300秒；不要提交真实密钥。

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

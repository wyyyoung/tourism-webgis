# 长安迹：西安市旅游资源 WebGIS

[在线只读演示](https://wyyyoung.github.io/tourism-webgis/) · [下载完整运行版](https://github.com/wyyyoung/tourism-webgis/releases/latest)

长安迹是一个面向 WebGIS 课程设计的西安旅游资源管理与空间分析系统。项目以 CesiumJS 构建三维地图，使用 Vue 3 实现交互界面，Spring Boot 提供 REST API，PostgreSQL/PostGIS 负责景点数据存储、空间查询和地理计算。

GitHub Pages 提供无需安装的只读演示，可浏览地图、筛选景点、查看详情和执行相机飞行。Release 中的完整运行版支持 PostGIS 空间分析、景点管理和真实驾车路线规划。

## 系统功能

- Cesium 三维地图、三种在线底图切换、西安定位和相机飞行
- 20 个西安景点标注、图文详情及名称、类型、行政区、推荐等级筛选
- 周边半径查询、多边形范围查询和真实缓冲区分析
- 基于 WGS84 椭球面的距离、面积及周长量算
- 2–8 个景点的 OSRM 真实驾车路线、里程/时间显示和顺序优化
- 道路服务不可用时自动回退为景点间空间直线
- 景点新增、编辑、删除、地图坐标拾取和分类统计
- Flyway 自动初始化 PostGIS 扩展、数据表、空间索引及景点数据

## 技术架构

```text
Vue 3 + CesiumJS
        │ REST API
Spring Boot 3 + Java 21
        │ JDBC / 空间 SQL
PostgreSQL 17 + PostGIS 3.5
        │
OSRM 公共驾车服务
```

项目采用前后端统一发布方式：生产构建后的前端由 Spring Boot 托管，因此完整版本只需访问一个地址。

## 下载成品并运行

### 环境要求

- Java 21
- Docker Desktop
- 可访问在线地图和 OSRM 服务的网络

从 [Releases](https://github.com/wyyyoung/tourism-webgis/releases/latest) 下载以下文件，并放在同一个目录：

```text
tourism-webgis-v1.0.0.jar
docker-compose.yml
运行说明-v1.0.0.md
```

启动 Docker Desktop，在该目录打开 PowerShell：

```powershell
docker compose up -d postgis
java -jar .\tourism-webgis-v1.0.0.jar
```

看到应用启动完成后访问：

```text
http://localhost:8080
```

按 `Ctrl+C` 停止 Java 应用，停止数据库执行：

```powershell
docker compose down
```

首次启动会自动创建数据库结构并导入 20 条景点数据。Docker 数据卷会保留数据，之后再次启动不会丢失修改。

## 从源码构建

源码构建还需要 Node.js 22 和 npm。克隆仓库后，在项目根目录依次执行：

```powershell
cd tourism-webgis-frontend
npm install
npm run build
cd ..

Remove-Item -Recurse -Force tourism-webgis-backend\src\main\resources\static -ErrorAction SilentlyContinue
New-Item -ItemType Directory tourism-webgis-backend\src\main\resources\static | Out-Null
Copy-Item -Recurse tourism-webgis-frontend\dist\* tourism-webgis-backend\src\main\resources\static

cd tourism-webgis-backend
.\mvnw.cmd clean package
cd ..
```

构建结果位于：

```text
tourism-webgis-backend\target\tourism-webgis.jar
```

启动完整系统：

```powershell
docker compose up -d postgis
java -jar .\tourism-webgis-backend\target\tourism-webgis.jar
```

## 开发模式

分别打开三个 PowerShell 终端。

终端一，启动 PostGIS：

```powershell
docker compose up -d postgis
```

终端二，启动 Spring Boot：

```powershell
cd tourism-webgis-backend
.\mvnw.cmd spring-boot:run
```

终端三，启动 Vue/Vite：

```powershell
cd tourism-webgis-frontend
npm install
npm run dev
```

开发地址：

- 前端：`http://localhost:5173`
- 后端 API：`http://localhost:8080/api`
- PostGIS：`localhost:5433`

## 默认配置

本地数据库配置：

```text
database: tourism_webgis
username: webgis
password: webgis123
```

这些凭据只用于项目自带的本地 Docker 数据库。可通过 `DB_URL`、`DB_USERNAME` 和 `DB_PASSWORD` 环境变量覆盖。

路线规划默认调用公共 OSRM：

```text
OSRM_BASE_URL=https://router.project-osrm.org
OSRM_CONNECT_TIMEOUT=8
OSRM_READ_TIMEOUT=8
```

## 项目结构

```text
tourism-webgis-frontend/   Vue 3 + Vite + CesiumJS 前端
tourism-webgis-backend/    Spring Boot + Flyway 后端
docker-compose.yml         PostgreSQL/PostGIS 本地环境
.github/workflows/         GitHub Pages 演示版构建配置
```

`node_modules`、`dist`、`target`、运行日志、数据库文件及课程参考资料不会提交到源码仓库；编译后的 JAR 通过 GitHub Releases 分发。

## GitHub Pages

在线地址：

```text
https://wyyyoung.github.io/tourism-webgis/
```

Pages 使用内置的 20 条景点数据，只提供地图浏览、筛选、详情和飞行等只读功能。完整空间分析与管理功能需要运行本地完整版本。

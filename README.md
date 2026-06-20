# 长安迹：西安市旅游资源 WebGIS

[在线只读演示：https://wyyyoung.github.io/tourism-webgis/](https://wyyyoung.github.io/tourism-webgis/)

> GitHub Pages 当前为只读演示版，可浏览地图、景点、筛选和详情。完整的 PostGIS 空间分析、景点管理及在线驾车规划需要运行本地完整版；后续部署公网后端后可升级为完整在线版。

课程作业项目，采用 Vue 3、CesiumJS、Spring Boot、PostgreSQL 和 PostGIS，实现旅游景点浏览、空间查询、统计与地图内数据管理。

## 主要功能

- 三维地图、底图切换、景点标注、详情与相机飞行
- 名称、类型、行政区、推荐等级组合筛选
- PostGIS 周边半径查询与多边形范围查询
- PostGIS 真实缓冲区分析与景点分类统计
- WGS84 椭球距离、面积和周长量算
- 2–8个景点的真实驾车道路规划、驾车距离优化与手动排序
- 景点新增、编辑、删除与地图坐标拾取
- 景点类型、行政区和推荐等级统计
- 前后端统一打包，通过一个网址访问

## 环境要求

源码首次构建需要：

- Java 21
- Node.js 22 及 npm
- Docker Desktop

已经包含 `runtime/tourism-webgis.jar` 的成品包只需要 Java 21 和 Docker Desktop。

## 一键运行

首次运行先执行：

```powershell
.\build.bat
```

之后双击或执行：

```powershell
.\start.bat
```

脚本会启动 Docker PostGIS、Spring Boot，并打开：

```text
http://localhost:8080
```

停止系统：

```powershell
.\stop.bat
```

## 开发模式

先启动 Docker Desktop，然后执行：

```powershell
.\dev.bat
```

- Vue 开发服务器：`http://localhost:5173`
- Spring Boot API：`http://localhost:8080/api`
- Docker PostGIS：`localhost:5433`

数据库默认配置：

```text
database: tourism_webgis
username: webgis
password: webgis123
```

这些凭据仅用于本地课程演示。

旅游线路默认调用公共 OSRM 驾车服务，需要互联网。道路服务超时或无路线时，系统会自动回退到 PostGIS 空间直线并在界面中提示。

可通过环境变量覆盖道路服务：

```text
OSRM_BASE_URL=https://router.project-osrm.org
OSRM_CONNECT_TIMEOUT=8
OSRM_READ_TIMEOUT=8
```

## 目录

```text
tourism-webgis-frontend/   Vue 3 + CesiumJS
tourism-webgis-backend/    Spring Boot + Flyway
docker-compose.yml         PostGIS
build.bat                  统一构建
start.bat                  一键启动
stop.bat                   一键停止
```

数据库首次启动时由 Flyway 自动创建 PostGIS 扩展、景点表、空间索引，并导入20条西安景点数据。

## GitHub Pages

推送到 `main` 分支后，GitHub Actions 会自动构建只读演示版并发布到：

```text
https://wyyyoung.github.io/tourism-webgis/
```

Pages 构建使用：

```text
VITE_DEPLOY_MODE=demo
VITE_BASE_PATH=/tourism-webgis/
```

本地完整版不使用这些环境变量，因此仍连接 Spring Boot 和 PostGIS。

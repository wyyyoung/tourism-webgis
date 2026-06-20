# 长安迹 WebGIS v1.0.0 运行说明

## 需要的环境

- Windows 10/11
- Java 21
- Docker Desktop
- 可访问在线地图服务的网络

## 文件准备

将下列两个文件放在同一个目录：

```text
tourism-webgis-v1.0.0.jar
docker-compose.yml
```

## 启动

先启动 Docker Desktop，然后在文件所在目录打开 PowerShell：

```powershell
docker compose up -d postgis
java -jar .\tourism-webgis-v1.0.0.jar
```

浏览器访问：

```text
http://localhost:8080
```

第一次启动会自动下载 PostGIS 镜像、创建数据库并导入 20 条西安景点数据，因此耗时会稍长。

## 停止

在运行 Java 的终端按 `Ctrl+C`，再执行：

```powershell
docker compose down
```

数据库内容保存在 Docker 数据卷中。需要连同数据库数据全部删除时才执行：

```powershell
docker compose down -v
```

注意：带 `-v` 的命令会永久删除本项目的数据库数据。

## 常见问题

- `java` 不是命令：安装 Java 21，并重新打开 PowerShell。
- 无法连接 Docker：启动 Docker Desktop，等待状态变为 Running。
- 端口被占用：确认本机的 `8080` 和 `5433` 端口未被其他程序使用。
- 地图或驾车路线无法加载：检查互联网连接；OSRM 不可用时路线会回退为空间直线。

在线只读演示：

https://wyyyoung.github.io/tourism-webgis/

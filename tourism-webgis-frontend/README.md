# 长安迹：西安市旅游资源 WebGIS

基于 Vue 3、Vite 和 CesiumJS 的课程作业前端首版。

## 环境要求

- Node.js 22 或更高版本
- npm 10 或更高版本
- 可访问在线地图瓦片的网络环境

## 启动

```powershell
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

## 构建

```powershell
npm run build
npm run preview
```

`npm install` 和 `npm run build` 会自动将 Cesium 的 Workers、Widgets、Assets 等静态资源复制到 `public/cesium`。

## 当前功能

- OpenStreetMap、CARTO 浅色地图和 Esri 公开影像底图切换
- 西安市旅游景点标注和点击弹窗
- 景点列表与相机飞行定位
- 名称、类型、行政区和推荐等级组合筛选
- 景点图文详情面板
- 鼠标位置经纬度显示
- 景点或地图点缓冲区分析
- 距离、面积和周长量算
- 多景点真实驾车道路规划、里程/时间展示与故障直线回退

景点数据通过 `/api/attractions` 从 Spring Boot 和 PostGIS 获取。开发服务器会把 `/api` 代理到 `http://localhost:8080`。

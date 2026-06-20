CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE tourist_attraction (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    district VARCHAR(30) NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    location geometry(Point, 4326) NOT NULL,
    address VARCHAR(200) NOT NULL,
    ticket VARCHAR(100) NOT NULL,
    opening_hours VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    image VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tourist_attraction_location
    ON tourist_attraction USING GIST (location);
CREATE INDEX idx_tourist_attraction_category ON tourist_attraction (category);
CREATE INDEX idx_tourist_attraction_district ON tourist_attraction (district);

INSERT INTO tourist_attraction
    (name, category, district, rating, location, address, ticket, opening_hours, description, image)
VALUES
('西安城墙', '历史文化', '碑林区', 5, ST_SetSRID(ST_MakePoint(108.9425, 34.2594), 4326), '南大街2号', '成人票约54元', '08:00–22:00（以景区公告为准）', '中国现存规模最大、保存最完整的古代城垣之一，可步行或骑行俯瞰古城中轴线。', '/images/history.svg'),
('西安钟楼', '历史文化', '莲湖区', 5, ST_SetSRID(ST_MakePoint(108.9402, 34.2610), 4326), '东西南北四条大街交汇处', '成人票约30元', '08:30–21:30', '明代古建筑，是西安城市中心的重要地标，夜间灯光景观尤其醒目。', '/images/history.svg'),
('西安鼓楼', '历史文化', '莲湖区', 5, ST_SetSRID(ST_MakePoint(108.9370, 34.2607), 4326), '北院门74号', '成人票约30元', '08:30–21:30', '与钟楼遥相呼应，楼内陈列鼓文化展品，毗邻回民街历史文化街区。', '/images/history.svg'),
('大雁塔', '宗教古迹', '雁塔区', 5, ST_SetSRID(ST_MakePoint(108.9642, 34.2180), 4326), '慈恩路1号大慈恩寺内', '寺院与登塔分别购票', '08:30–17:30', '唐代佛教建筑代表，玄奘法师曾在此主持译经，周边形成大唐文化旅游核心区。', '/images/temple.svg'),
('大唐不夜城', '城市休闲', '雁塔区', 5, ST_SetSRID(ST_MakePoint(108.9647, 34.2105), 4326), '慈恩路与雁南一路一带', '免费开放', '全天开放，夜间演出为主', '以盛唐文化为主题的步行街，集景观、演艺、餐饮和互动体验于一体。', '/images/city.svg'),
('陕西历史博物馆', '博物馆', '雁塔区', 5, ST_SetSRID(ST_MakePoint(108.9534, 34.2254), 4326), '小寨东路91号', '基本陈列免费预约', '08:30–18:00，周一闭馆', '馆藏跨越周秦汉唐等重要时期，被誉为古都明珠与华夏宝库。', '/images/museum.svg'),
('大明宫国家遗址公园', '历史文化', '新城区', 4, ST_SetSRID(ST_MakePoint(108.9646, 34.2937), 4326), '自强东路585号', '外围免费，核心区收费', '08:30–18:00', '唐长安城宫城遗址，丹凤门、含元殿等遗址展示出盛唐宫城格局。', '/images/history.svg'),
('小雁塔', '宗教古迹', '碑林区', 4, ST_SetSRID(ST_MakePoint(108.9343, 34.2467), 4326), '友谊西路72号', '免费预约', '09:00–17:30，周二闭馆', '唐代密檐式砖塔，与西安博物院共同构成幽静的历史文化园区。', '/images/temple.svg'),
('西安博物院', '博物馆', '碑林区', 4, ST_SetSRID(ST_MakePoint(108.9332, 34.2453), 4326), '友谊西路72号', '免费预约', '09:00–17:30，周二闭馆', '以古都西安历史为主题，馆、塔、园相结合，适合系统了解城市发展脉络。', '/images/museum.svg'),
('大唐芙蓉园', '城市休闲', '雁塔区', 4, ST_SetSRID(ST_MakePoint(108.9770, 34.2118), 4326), '芙蓉西路99号', '票价随演出场次调整', '09:00–22:00', '大型唐文化主题园区，水景、仿唐建筑与夜间演出共同营造盛唐游园体验。', '/images/city.svg'),
('曲江池遗址公园', '自然风光', '雁塔区', 4, ST_SetSRID(ST_MakePoint(108.9828, 34.1984), 4326), '曲江池西路', '免费开放', '全天开放', '以曲江历史水系为基础建设的开放式公园，适合环湖散步和城市休闲。', '/images/nature.svg'),
('永兴坊', '城市休闲', '新城区', 4, ST_SetSRID(ST_MakePoint(108.9718, 34.2632), 4326), '东新街1号', '免费开放', '街区全天开放', '依托城墙历史空间打造的陕西非遗美食文化街区，可集中体验地方小吃。', '/images/city.svg'),
('广仁寺', '宗教古迹', '莲湖区', 4, ST_SetSRID(ST_MakePoint(108.9177, 34.2746), 4326), '西北一路152号', '免费开放', '08:00–18:00', '陕西境内重要藏传佛教寺院，建筑色彩鲜明，位于西安城墙西北角附近。', '/images/temple.svg'),
('兴庆宫公园', '自然风光', '碑林区', 3, ST_SetSRID(ST_MakePoint(108.9850, 34.2527), 4326), '咸宁西路55号', '免费开放', '06:00–22:00', '在唐兴庆宫遗址上建设的城市公园，以湖面、花木与历史景观闻名。', '/images/nature.svg'),
('半坡博物馆', '博物馆', '灞桥区', 4, ST_SetSRID(ST_MakePoint(109.0488, 34.2772), 4326), '半坡路155号', '成人票约55元', '08:00–18:00', '中国首座史前遗址博物馆，原址展示新石器时代仰韶文化聚落遗迹。', '/images/museum.svg'),
('华清宫', '历史文化', '临潼区', 5, ST_SetSRID(ST_MakePoint(109.2147, 34.3639), 4326), '华清路38号', '成人票约120元', '07:30–18:00', '依骊山而建的皇家宫苑遗址，以温泉文化、唐代历史和大型实景演出著称。', '/images/history.svg'),
('秦始皇帝陵博物院', '博物馆', '临潼区', 5, ST_SetSRID(ST_MakePoint(109.2732, 34.3841), 4326), '秦陵北路', '成人票约120元', '08:30–18:00', '以兵马俑坑和秦始皇陵为核心的世界文化遗产，是西安最具代表性的旅游目的地之一。', '/images/museum.svg'),
('翠华山', '自然风光', '长安区', 4, ST_SetSRID(ST_MakePoint(109.0055, 33.9998), 4326), '太乙宫镇翠华山', '成人票约65元', '09:00–17:00', '秦岭北麓山岳景区，以山崩地貌、天池和森林景观为主要特色。', '/images/nature.svg'),
('终南山南五台', '自然风光', '长安区', 4, ST_SetSRID(ST_MakePoint(108.9955, 34.0217), 4326), '五台街道星火村', '成人票约45元', '09:00–16:00', '终南山世界地质公园的重要组成部分，兼具秦岭自然风光与佛教文化。', '/images/nature.svg'),
('汉城湖遗址公园', '自然风光', '未央区', 3, ST_SetSRID(ST_MakePoint(108.9220, 34.3082), 4326), '北二环与朱宏路交汇处', '免费开放', '06:00–22:00', '依托汉长安城遗址和水系建设的城市公园，可了解汉代都城文化。', '/images/nature.svg');


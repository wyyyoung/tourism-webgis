const asset = (path) => `${import.meta.env.BASE_URL}${path.replace(/^\/+/, "")}`;

const categoryImages = {
  历史文化: asset("images/history.svg"),
  博物馆: asset("images/museum.svg"),
  宗教古迹: asset("images/temple.svg"),
  自然风光: asset("images/nature.svg"),
  城市休闲: asset("images/city.svg")
};

const source = [
  {
    id: 1,
    name: "西安城墙",
    category: "历史文化",
    district: "碑林区",
    rating: 5,
    longitude: 108.9425,
    latitude: 34.2594,
    address: "南大街2号",
    ticket: "成人票约54元",
    openingHours: "08:00–22:00（以景区公告为准）",
    description: "中国现存规模最大、保存最完整的古代城垣之一，可步行或骑行俯瞰古城中轴线。"
  },
  {
    id: 2,
    name: "西安钟楼",
    category: "历史文化",
    district: "莲湖区",
    rating: 5,
    longitude: 108.9402,
    latitude: 34.261,
    address: "东西南北四条大街交汇处",
    ticket: "成人票约30元",
    openingHours: "08:30–21:30",
    description: "明代古建筑，是西安城市中心的重要地标，夜间灯光景观尤其醒目。"
  },
  {
    id: 3,
    name: "西安鼓楼",
    category: "历史文化",
    district: "莲湖区",
    rating: 5,
    longitude: 108.937,
    latitude: 34.2607,
    address: "北院门74号",
    ticket: "成人票约30元",
    openingHours: "08:30–21:30",
    description: "与钟楼遥相呼应，楼内陈列鼓文化展品，毗邻回民街历史文化街区。"
  },
  {
    id: 4,
    name: "大雁塔",
    category: "宗教古迹",
    district: "雁塔区",
    rating: 5,
    longitude: 108.9642,
    latitude: 34.218,
    address: "慈恩路1号大慈恩寺内",
    ticket: "寺院与登塔分别购票",
    openingHours: "08:30–17:30",
    description: "唐代佛教建筑代表，玄奘法师曾在此主持译经，周边形成大唐文化旅游核心区。"
  },
  {
    id: 5,
    name: "大唐不夜城",
    category: "城市休闲",
    district: "雁塔区",
    rating: 5,
    longitude: 108.9647,
    latitude: 34.2105,
    address: "慈恩路与雁南一路一带",
    ticket: "免费开放",
    openingHours: "全天开放，夜间演出为主",
    description: "以盛唐文化为主题的步行街，集景观、演艺、餐饮和互动体验于一体。"
  },
  {
    id: 6,
    name: "陕西历史博物馆",
    category: "博物馆",
    district: "雁塔区",
    rating: 5,
    longitude: 108.9534,
    latitude: 34.2254,
    address: "小寨东路91号",
    ticket: "基本陈列免费预约",
    openingHours: "08:30–18:00，周一闭馆",
    description: "馆藏跨越周秦汉唐等重要时期，被誉为古都明珠与华夏宝库。"
  },
  {
    id: 7,
    name: "大明宫国家遗址公园",
    category: "历史文化",
    district: "新城区",
    rating: 4,
    longitude: 108.9646,
    latitude: 34.2937,
    address: "自强东路585号",
    ticket: "外围免费，核心区收费",
    openingHours: "08:30–18:00",
    description: "唐长安城宫城遗址，丹凤门、含元殿等遗址展示出盛唐宫城格局。"
  },
  {
    id: 8,
    name: "小雁塔",
    category: "宗教古迹",
    district: "碑林区",
    rating: 4,
    longitude: 108.9343,
    latitude: 34.2467,
    address: "友谊西路72号",
    ticket: "免费预约",
    openingHours: "09:00–17:30，周二闭馆",
    description: "唐代密檐式砖塔，与西安博物院共同构成幽静的历史文化园区。"
  },
  {
    id: 9,
    name: "西安博物院",
    category: "博物馆",
    district: "碑林区",
    rating: 4,
    longitude: 108.9332,
    latitude: 34.2453,
    address: "友谊西路72号",
    ticket: "免费预约",
    openingHours: "09:00–17:30，周二闭馆",
    description: "以古都西安历史为主题，馆、塔、园相结合，适合系统了解城市发展脉络。"
  },
  {
    id: 10,
    name: "大唐芙蓉园",
    category: "城市休闲",
    district: "雁塔区",
    rating: 4,
    longitude: 108.977,
    latitude: 34.2118,
    address: "芙蓉西路99号",
    ticket: "票价随演出场次调整",
    openingHours: "09:00–22:00",
    description: "大型唐文化主题园区，水景、仿唐建筑与夜间演出共同营造盛唐游园体验。"
  },
  {
    id: 11,
    name: "曲江池遗址公园",
    category: "自然风光",
    district: "雁塔区",
    rating: 4,
    longitude: 108.9828,
    latitude: 34.1984,
    address: "曲江池西路",
    ticket: "免费开放",
    openingHours: "全天开放",
    description: "以曲江历史水系为基础建设的开放式公园，适合环湖散步和城市休闲。"
  },
  {
    id: 12,
    name: "永兴坊",
    category: "城市休闲",
    district: "新城区",
    rating: 4,
    longitude: 108.9718,
    latitude: 34.2632,
    address: "东新街1号",
    ticket: "免费开放",
    openingHours: "街区全天开放",
    description: "依托城墙历史空间打造的陕西非遗美食文化街区，可集中体验地方小吃。"
  },
  {
    id: 13,
    name: "广仁寺",
    category: "宗教古迹",
    district: "莲湖区",
    rating: 4,
    longitude: 108.9177,
    latitude: 34.2746,
    address: "西北一路152号",
    ticket: "免费开放",
    openingHours: "08:00–18:00",
    description: "陕西境内重要藏传佛教寺院，建筑色彩鲜明，位于西安城墙西北角附近。"
  },
  {
    id: 14,
    name: "兴庆宫公园",
    category: "自然风光",
    district: "碑林区",
    rating: 3,
    longitude: 108.985,
    latitude: 34.2527,
    address: "咸宁西路55号",
    ticket: "免费开放",
    openingHours: "06:00–22:00",
    description: "在唐兴庆宫遗址上建设的城市公园，以湖面、花木与历史景观闻名。"
  },
  {
    id: 15,
    name: "半坡博物馆",
    category: "博物馆",
    district: "灞桥区",
    rating: 4,
    longitude: 109.0488,
    latitude: 34.2772,
    address: "半坡路155号",
    ticket: "成人票约55元",
    openingHours: "08:00–18:00",
    description: "中国首座史前遗址博物馆，原址展示新石器时代仰韶文化聚落遗迹。"
  },
  {
    id: 16,
    name: "华清宫",
    category: "历史文化",
    district: "临潼区",
    rating: 5,
    longitude: 109.2147,
    latitude: 34.3639,
    address: "华清路38号",
    ticket: "成人票约120元",
    openingHours: "07:30–18:00",
    description: "依骊山而建的皇家宫苑遗址，以温泉文化、唐代历史和大型实景演出著称。"
  },
  {
    id: 17,
    name: "秦始皇帝陵博物院",
    category: "博物馆",
    district: "临潼区",
    rating: 5,
    longitude: 109.2732,
    latitude: 34.3841,
    address: "秦陵北路",
    ticket: "成人票约120元",
    openingHours: "08:30–18:00",
    description: "以兵马俑坑和秦始皇陵为核心的世界文化遗产，是西安最具代表性的旅游目的地之一。"
  },
  {
    id: 18,
    name: "翠华山",
    category: "自然风光",
    district: "长安区",
    rating: 4,
    longitude: 109.0055,
    latitude: 33.9998,
    address: "太乙宫镇翠华山",
    ticket: "成人票约65元",
    openingHours: "09:00–17:00",
    description: "秦岭北麓山岳景区，以山崩地貌、天池和森林景观为主要特色。"
  },
  {
    id: 19,
    name: "终南山南五台",
    category: "自然风光",
    district: "长安区",
    rating: 4,
    longitude: 108.9955,
    latitude: 34.0217,
    address: "五台街道星火村",
    ticket: "成人票约45元",
    openingHours: "09:00–16:00",
    description: "终南山世界地质公园的重要组成部分，兼具秦岭自然风光与佛教文化。"
  },
  {
    id: 20,
    name: "汉城湖遗址公园",
    category: "自然风光",
    district: "未央区",
    rating: 3,
    longitude: 108.922,
    latitude: 34.3082,
    address: "北二环与朱宏路交汇处",
    ticket: "免费开放",
    openingHours: "06:00–22:00",
    description: "依托汉长安城遗址和水系建设的城市公园，可了解汉代都城文化。"
  }
];

export const attractions = source.map((item) => ({
  ...item,
  image: categoryImages[item.category]
}));

export const categoryColors = {
  历史文化: "#ffb45b",
  博物馆: "#54d7ff",
  宗教古迹: "#c8a7ff",
  自然风光: "#64e6a5",
  城市休闲: "#ff7ca8"
};

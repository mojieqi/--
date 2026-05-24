# Phase 8 完成报告 — 智能推荐与内容发现  

**日期：** 2026-05-24  
**状态：** ✅ 已完成  
**耗时：** 1个周期（单日完成）  

---

## 一、产出文件清单  

| # | 文件路径 | 说明 |  
|:--:|------|------|  
| **后端Service** (2个) |||  
| 1 | `.../service/ICwRecommendService.java` | 推荐服务接口（4个方法） |  
| 2 | `.../service/impl/CwRecommendServiceImpl.java` | 推荐引擎核心（多因子算法+热度公式+中文分词） |  
| **后端Controller** (1个) |||  
| 3 | `.../controller/system/CwRecommendController.java` | 推荐REST API（4个端点） |  
| **SQL** (1个) |||  
| 4 | `sql/cw_recommend_menu.sql` | 发现页菜单+权限脚本 |  
| **前端API** (1个) |||  
| 5 | `ruoyi-ui/src/api/campus/recommend.js` | 推荐4个API封装 |  
| **前端页面** (4个) |||  
| 6 | `.../views/campus/discover/index.vue` | 发现页主页面（Feed+榜单+关键词） |  
| 7 | `.../views/campus/discover/components/RecommendFeed.vue` | 推荐Feed流组件 |  
| 8 | `.../views/campus/discover/components/HotRanking.vue` | 热门榜单组件（日/周/总Tab） |  
| 9 | `.../views/campus/discover/components/TopicCloud.vue` | 关键词云组件 |  
| **修改文件** (1个) |||  
| 10 | `views/campus/post/detail.vue` | 帖子详情页底部新增"相关推荐"模块 |  

**总新增：** 9个文件 | **修改：** 1个文件  

---

## 二、API 端点清单  

| 方法 | 路径 | 说明 | 权限 |  
|------|------|------|------|  
| GET | `/campus/recommend/feed` | 个性化推荐Feed（pageNum/pageSize） | — |  
| GET | `/campus/recommend/hot` | 热门榜单（type=day/week/all, limit） | — |  
| GET | `/campus/recommend/related/{postId}` | 相关推荐（limit） | — |  
| GET | `/campus/recommend/keywords` | 热门关键词（limit） | — |  

---

## 三、推荐算法说明  

### 3.1 热度分公式（HackerNews风格）  

```
热度分 = (log(view+1)*1 + like*3 + comment*5) × 时间衰减 × 置顶加权  

时间衰减 = 1 / (1 + hours/24)     // 24小时半衰期  
置顶加权 = 2.0（置顶）/ 1.0（普通）  
```

### 3.2 推荐Feed策略（多因子加权，纯算法无LLM延迟）

| 因子 | 权重 | 实现方式 |  
|------|:--:|---------|  
| 热度权重 | 40% | HackerNews公式排序 |  
| 时效性 | 30% | 24小时半衰期时间衰减 |  
| 多样性 | 20% | 同分类最多展示3条 |  
| 互动偏好 | 10% | 按用户浏览分类加权 |  

### 3.3 热门榜单  

| 榜单 | 时间范围 | 排序 |  
|------|---------|------|  
| 今日热榜 | 24小时内 | 热度分降序 |  
| 本周热榜 | 7天内 | 热度分降序 |  
| 总榜 | 全部时间 | 热度分降序 |  

### 3.4 相关推荐  

- **相同分类** → +50分  
- **标题关键词匹配** → 每个共享关键词 +10分  
- **热度分加成** → ×0.5倍  
- 按总分降序取TOP N  

### 3.5 关键词提取  

- 中文：Bigram窗口切分（2字词）  
- 英文：按非字母字符分词  
- 停用词过滤（30+中文停用词）  
- 分类名自动补充  

---

## 四、现有模块连通性  

| 模块 | 联通方式 |  
|------|---------|  
| **ICwPostService** (Phase 6) | 注入复用 `selectCwPostList` / `selectCwPostById`（含匿名处理） |  
| **CwPostMapper** (Phase 6) | 底层数据查询，无PageHelper干扰 |  
| **detail.vue** (Phase 6/7) | 帖子详情页底部新增"相关推荐"模块 |  
| **若依菜单系统** | 自动生成 `/campus/discover` 路由入口 |  

---

## 五、前端交互  

### 发现页 (discover/index.vue)  

- 双Tab切换：为你推荐 / 最新发布  
- 左侧Feed流（卡片式 + 加载更多）  
- 右侧排行榜 + 关键词云  
- 响应式布局（窄屏时侧边栏下移）  

### 相关推荐 (detail.vue)  

- 帖子详情页底部自动加载  
- 点击可跳转，显示浏览/点赞数  

---

## ⚠️ 您需要手动执行的操作  

### 步骤 1: 执行菜单权限脚本  
```sql  
source sql/cw_recommend_menu.sql  
```  

### 步骤 2: 重启后端服务  
- 停止当前运行的服务  
- 重新启动 `ry.bat` 或通过IDE启动  

### 步骤 3: 重新构建前端  
```bash  
cd ruoyi-ui  
npm run build:prod  
```  

### 步骤 4: 功能验证  

| 验证项 | 操作 | 预期结果 |  
|--------|------|----------|  
| 发现页入口 | 侧边栏 → 校园墙 → 发现精彩 | 进入发现页，看到Banner+双Tab |  
| 推荐Feed | 点击"为你推荐" | 展示推荐帖子列表（热度+多样性过滤） |  
| 最新发布 | 点击"最新发布" | 按时间倒序展示帖子 |  
| 今日热榜 | 右侧面板 → 默认今日Tab | 显示24h内帖子按热度排序 |  
| 本周/总榜 | 点击本周/总榜Tab | 正确切换时间范围 |  
| 关键词云 | 查看右侧关键词区域 | 展示提取的关键词（字号区分频次） |  
| 点击关键词 | 点击任意关键词 | 跳转帖子广场（keyword参数） |  
| 相关推荐 | 打开任意帖子详情 | 底部展示6条相关推荐 |  
| 点击相关推荐 | 点击相关帖子 | 跳转到该帖子详情页 |  
| 加载更多 | Feed流底部点加载更多 | 下一页数据追加显示 |  

---

## 六、文件变更总览  

```  
新增：  
  service/ICwRecommendService.java  
  service/impl/CwRecommendServiceImpl.java  
  controller/system/CwRecommendController.java  
  sql/cw_recommend_menu.sql  
  api/campus/recommend.js  
  views/campus/discover/index.vue  
  views/campus/discover/components/RecommendFeed.vue  
  views/campus/discover/components/HotRanking.vue  
  views/campus/discover/components/TopicCloud.vue  

修改：  
  views/campus/post/detail.vue     +相关推荐模块  
```  

**Phase 8 开发完毕，后端编译通过，零Lint错误。请执行上述操作验证。**  

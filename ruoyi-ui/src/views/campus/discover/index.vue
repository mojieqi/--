<template>
  <div class="discover-page">
    <!-- 顶部Banner -->
    <div class="discover-header">
      <h1><i class="el-icon-discover"></i> 发现精彩</h1>
      <p>探索校园热门话题，发现有趣内容</p>
    </div>

    <div class="discover-body">
      <!-- 左侧：推荐Feed -->
      <div class="discover-main">
        <el-tabs v-model="activeTab" @tab-click="onTabChange">
          <el-tab-pane label="为你推荐" name="feed">
            <recommend-feed ref="feed" />
          </el-tab-pane>
          <el-tab-pane label="最新发布" name="latest">
            <recommend-feed ref="latest" mode="latest" />
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 右侧：榜单 + 关键词 -->
      <div class="discover-side">
        <hot-ranking />
        <topic-cloud @click="onKeywordClick" />
      </div>
    </div>
  </div>
</template>

<script>
import RecommendFeed from './components/RecommendFeed'
import HotRanking from './components/HotRanking'
import TopicCloud from './components/TopicCloud'

export default {
  name: 'CampusDiscover',
  components: { RecommendFeed, HotRanking, TopicCloud },
  data() {
    return {
      activeTab: 'feed'
    }
  },
  created() {
    document.title = '发现精彩 - 校园墙'
  },
  methods: {
    onTabChange() {
      // tab切换时子组件自行处理
    },
    onKeywordClick(keyword) {
      this.$router.push({ path: '/campus/post', query: { keyword }})
    }
  }
}
</script>

<style scoped>
.discover-page {
  padding: 0;
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
}

.discover-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px 24px;
  color: #fff;
  text-align: center;
}
.discover-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}
.discover-header h1 i { margin-right: 6px; }
.discover-header p {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

.discover-body {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 16px;
  display: flex;
  gap: 20px;
}

.discover-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 4px 20px 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.discover-side {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (max-width: 900px) {
  .discover-body { flex-direction: column; }
  .discover-side { width: 100%; }
}
</style>

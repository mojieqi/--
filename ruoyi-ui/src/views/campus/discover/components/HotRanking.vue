<template>
  <div class="hot-ranking">
    <div class="ranking-header">
      <h3><i class="el-icon-trophy"></i> 热门榜单</h3>
    </div>

    <el-tabs v-model="active" @tab-click="onTabChange" size="small">
      <el-tab-pane label="今日" name="day" />
      <el-tab-pane label="本周" name="week" />
      <el-tab-pane label="总榜" name="all" />
    </el-tabs>

    <div v-if="loading" class="ranking-loading">
      <i class="el-icon-loading"></i>
    </div>

    <div v-else-if="list.length === 0" class="ranking-empty">
      暂无数据
    </div>

    <ol v-else class="ranking-list">
      <li
        v-for="(item, index) in list"
        :key="item.postId"
        class="ranking-item"
        @click="goDetail(item.postId)"
      >
        <span class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
        <span class="rank-title">{{ item.title }}</span>
        <span class="rank-heat">{{ formatHeat(item) }}</span>
      </li>
    </ol>
  </div>
</template>

<script>
import { getHot } from '@/api/campus/recommend'

export default {
  name: 'HotRanking',
  data() {
    return {
      active: 'day',
      loading: false,
      list: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getHot({ type: this.active, limit: 10 })
        this.list = res.data || []
      } catch (e) {
        console.error('加载榜单失败', e)
      } finally {
        this.loading = false
      }
    },
    onTabChange() {
      this.loadData()
    },
    formatHeat(post) {
      const like = post.likeCount || 0
      const comment = post.commentCount || 0
      const view = post.viewCount || 0
      const heat = like * 3 + comment * 5 + view
      if (heat >= 10000) return (heat / 10000).toFixed(1) + 'w'
      if (heat >= 1000) return (heat / 1000).toFixed(1) + 'k'
      return heat
    },
    goDetail(postId) {
      this.$router.push('/campus/post/detail/' + postId)
    }
  }
}
</script>

<style scoped>
.hot-ranking {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.ranking-header h3 {
  margin: 0 0 8px;
  font-size: 15px; font-weight: 600; color: #303133;
}
.ranking-header h3 i { color: #e6a23c; margin-right: 4px; }

.ranking-loading, .ranking-empty {
  text-align: center; padding: 30px 0; color: #909399; font-size: 13px;
}

.ranking-list {
  list-style: none; padding: 0; margin: 0;
}

.ranking-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background .15s;
}
.ranking-item:hover { background: #f9fafc; }
.ranking-item:last-child { border-bottom: none; }

.rank-num {
  width: 22px; height: 22px; line-height: 22px;
  text-align: center; font-size: 12px; font-weight: 700;
  border-radius: 4px; color: #909399; flex-shrink: 0;
}
.rank-num.rank-1 { background: #e6a23c; color: #fff; }
.rank-num.rank-2 { background: #b0bec5; color: #fff; }
.rank-num.rank-3 { background: #cd9a5b; color: #fff; }

.rank-title {
  flex: 1; min-width: 0;
  font-size: 13px; color: #303133;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.rank-heat {
  flex-shrink: 0;
  font-size: 12px; color: #e6a23c; font-weight: 600;
}
</style>

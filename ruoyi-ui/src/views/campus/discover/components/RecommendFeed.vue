<template>
  <div class="recommend-feed">
    <div v-if="loading" class="feed-loading">
      <i class="el-icon-loading"></i> 正在为你加载推荐内容...
    </div>

    <div v-else-if="postList.length === 0" class="feed-empty">
      <i class="el-icon-document-remove"></i>
      <p>暂无推荐内容</p>
    </div>

    <div v-else class="feed-list">
      <div
        v-for="post in postList"
        :key="post.postId"
        class="feed-card"
        @click="goDetail(post.postId)"
      >
        <div class="feed-card__body">
          <div v-if="post.coverImage" class="feed-card__cover">
            <img :src="post.coverImage" :alt="post.title" />
          </div>
          <div class="feed-card__info">
            <h3 class="feed-card__title">
              <span v-if="post.isTop === '1'" class="top-tag">置顶</span>
              {{ post.title }}
            </h3>
            <p class="feed-card__excerpt">{{ getExcerpt(post.content) }}</p>
            <div class="feed-card__meta">
              <span v-if="post.categoryName" class="category-tag">{{ post.categoryName }}</span>
              <span class="meta-user">{{ post.nickName || (post.isAnonymous === '1' ? '匿名用户' : post.createBy) }}</span>
              <span class="meta-time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="feed-card__footer">
          <span><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
          <span><i class="el-icon-star-off"></i> {{ post.likeCount || 0 }}</span>
          <span><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="feed-more" @click="loadMore">
      <el-button type="primary" plain :loading="loadingMore">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </el-button>
    </div>
  </div>
</template>

<script>
import { getFeed, getHot } from '@/api/campus/recommend'
import { listPost } from '@/api/campus/post'

export default {
  name: 'RecommendFeed',
  props: {
    mode: { type: String, default: 'feed' } // feed | latest
  },
  data() {
    return {
      loading: true,
      loadingMore: false,
      postList: [],
      pageNum: 1,
      pageSize: 10,
      hasMore: false
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      this.pageNum = 1
      try {
        let res
        if (this.mode === 'latest') {
          res = await listPost({ pageNum: this.pageNum, pageSize: this.pageSize })
          this.postList = res.rows || []
          this.hasMore = this.postList.length >= this.pageSize
        } else {
          res = await getFeed({ pageNum: this.pageNum, pageSize: this.pageSize })
          this.postList = res.data?.rows || []
          this.hasMore = this.postList.length >= this.pageSize
        }
      } catch (e) {
        console.error('加载推荐失败', e)
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (this.loadingMore) return
      this.loadingMore = true
      this.pageNum++
      try {
        let res
        if (this.mode === 'latest') {
          res = await listPost({ pageNum: this.pageNum, pageSize: this.pageSize })
          const rows = res.rows || []
          this.postList.push(...rows)
          this.hasMore = rows.length >= this.pageSize
        } else {
          res = await getFeed({ pageNum: this.pageNum, pageSize: this.pageSize })
          const rows = res.data?.rows || []
          this.postList.push(...rows)
          this.hasMore = rows.length >= this.pageSize
        }
      } catch (e) {
        console.error('加载更多失败', e)
      } finally {
        this.loadingMore = false
      }
    },
    getExcerpt(content) {
      if (!content) return ''
      return content.length > 120 ? content.substring(0, 120) + '...' : content
    },
    goDetail(postId) {
      this.$router.push('/campus/post/detail/' + postId)
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return y + '-' + m + '-' + day
    }
  }
}
</script>

<style scoped>
.feed-loading, .feed-empty {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}
.feed-empty i { font-size: 48px; display: block; margin-bottom: 12px; }

.feed-list { display: flex; flex-direction: column; gap: 12px; }

.feed-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px;
  cursor: pointer;
  transition: box-shadow .2s, border-color .2s;
}
.feed-card:hover {
  border-color: #667eea;
  box-shadow: 0 2px 12px rgba(102, 126, 234, .12);
}

.feed-card__body { display: flex; gap: 14px; }
.feed-card__cover {
  width: 140px; height: 100px; flex-shrink: 0;
  border-radius: 6px; overflow: hidden; background: #f5f7fa;
}
.feed-card__cover img {
  width: 100%; height: 100%; object-fit: cover;
}
.feed-card__info { flex: 1; min-width: 0; }

.feed-card__title {
  margin: 0 0 6px;
  font-size: 15px; font-weight: 600;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.top-tag {
  display: inline-block;
  background: #e6a23c; color: #fff;
  font-size: 11px; padding: 0 4px; border-radius: 2px;
  margin-right: 4px; vertical-align: 2px;
}

.feed-card__excerpt {
  margin: 0 0 8px;
  font-size: 13px; color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feed-card__meta {
  font-size: 12px; color: #c0c4cc;
  display: flex; align-items: center; gap: 10px;
}
.category-tag {
  background: #ecf5ff; color: #409eff;
  padding: 1px 6px; border-radius: 3px; font-size: 11px;
}

.feed-card__footer {
  margin-top: 10px; padding-top: 10px;
  border-top: 1px solid #f2f2f2;
  display: flex; gap: 16px;
  font-size: 12px; color: #c0c4cc;
}
.feed-card__footer i { margin-right: 2px; }

.feed-more { text-align: center; padding: 20px 0; }
</style>

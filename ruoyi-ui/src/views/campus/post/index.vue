<template>
  <div class="app-container campus-post-list">
    <!-- 分类导航 -->
    <div class="category-nav">
      <div
        class="category-item"
        :class="{ active: queryParams.categoryId === undefined || queryParams.categoryId === null }"
        @click="selectCategory(null)"
      >
        <i class="el-icon-menu"></i>
        <span>全部</span>
      </div>
      <div
        v-for="cat in categories"
        :key="cat.categoryId"
        class="category-item"
        :class="{ active: queryParams.categoryId === cat.categoryId }"
        @click="selectCategory(cat.categoryId)"
      >
        <span>{{ cat.categoryName }}</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-edit" size="mini" @click="handleAdd">发布帖子</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-input
          v-model="queryParams.title"
          placeholder="搜索帖子标题"
          clearable
          size="small"
          style="width: 240px"
          @keyup.enter.native="handleQuery"
        >
          <i slot="prefix" class="el-input__icon el-icon-search"></i>
        </el-input>
      </el-col>
    </el-row>

    <!-- 帖子列表 -->
    <div v-loading="loading" class="post-grid">
      <div v-if="postList.length === 0 && !loading" class="empty-state">
        <i class="el-icon-document" style="font-size: 48px; color: #c0c4cc;"></i>
        <p>暂无帖子，快去发布第一条吧</p>
      </div>
      <el-card
        v-for="post in postList"
        :key="post.postId"
        class="post-card"
        shadow="hover"
        @click.native="goDetail(post.postId)"
      >
        <div v-if="post.coverImage" class="card-image">
          <img :src="post.coverImage" :alt="post.title" />
        </div>
        <div class="card-content">
          <div class="card-header-line">
            <el-tag v-if="post.categoryName" size="mini" type="success">{{ post.categoryName }}</el-tag>
            <el-tag v-if="post.isTop === '1'" size="mini" type="danger">置顶</el-tag>
          </div>
          <h3 class="card-title">{{ post.title }}</h3>
          <p class="card-desc">{{ (post.content || '').substring(0, 100) }}{{ (post.content || '').length > 100 ? '...' : '' }}</p>
          <div class="card-footer">
            <span class="footer-item"><i class="el-icon-view"></i> {{ post.viewCount || 0 }}</span>
            <span class="footer-item"><i class="el-icon-star-off"></i> {{ post.likeCount || 0 }}</span>
            <span class="footer-item"><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }}</span>
            <span class="footer-time">{{ formatTime(post.createTime) }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listPost, categoryList } from '@/api/campus/post'

export default {
  name: 'CampusPostIndex',
  data() {
    return {
      loading: false,
      postList: [],
      categories: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        title: undefined,
        categoryId: undefined
      }
    }
  },
  created() {
    this.loadCategories()
    this.getList()
  },
  methods: {
    loadCategories() {
      categoryList().then(response => {
        this.categories = response.data || []
      })
    },
    getList() {
      this.loading = true
      listPost(this.queryParams).then(response => {
        this.postList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    selectCategory(categoryId) {
      this.queryParams.categoryId = categoryId
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleAdd() {
      this.$router.push('/campus/post/edit')
    },
    goDetail(postId) {
      this.$router.push('/campus/post/detail/' + postId)
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 172800000) return '昨天'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      const month = d.getMonth() + 1
      const day = d.getDate()
      return month + '-' + day
    }
  }
}
</script>

<style scoped>
.campus-post-list {
  max-width: 1200px;
  margin: 0 auto;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.category-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  color: #606266;
  background: #f5f7fa;
  transition: all 0.2s;
}

.category-item:hover {
  color: #409eff;
  background: #ecf5ff;
}

.category-item.active {
  color: #fff;
  background: #409eff;
}

.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-height: 200px;
}

@media (max-width: 900px) {
  .post-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .post-grid { grid-template-columns: 1fr; }
}

.post-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 8px;
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.post-card >>> .el-card__body {
  padding: 0;
}

.card-image {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #f5f7fa;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-content {
  padding: 16px;
}

.card-header-line {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #c0c4cc;
}

.footer-item i {
  margin-right: 2px;
}

.footer-time {
  margin-left: auto;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #c0c4cc;
}
</style>

<template>
  <div class="app-container campus-post-detail" v-loading="loading">
    <div v-if="post" class="detail-wrapper">
      <!-- 操作按钮 -->
      <el-row class="mb8">
        <el-col :span="1.5">
          <el-button icon="el-icon-back" size="mini" @click="goBack">返回</el-button>
        </el-col>
        <el-col :span="1.5" v-if="isOwner">
          <el-button type="primary" icon="el-icon-edit" size="mini" @click="handleEdit">编辑</el-button>
        </el-col>
        <el-col :span="1.5" v-if="isOwner">
          <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleDelete">删除</el-button>
        </el-col>
      </el-row>

      <!-- 帖子主体 -->
      <el-card class="detail-card" shadow="never">
        <!-- 头部信息 -->
        <div class="post-header">
          <div class="post-meta">
            <el-tag v-if="post.categoryName" size="small" type="success">{{ post.categoryName }}</el-tag>
            <el-tag v-if="post.isTop === '1'" size="small" type="danger">置顶</el-tag>
            <span class="post-author" v-if="post.isAnonymous === '1'">
              <i class="el-icon-user-solid"></i> 匿名用户
            </span>
            <span class="post-author" v-else>
              <i class="el-icon-user-solid"></i> {{ post.nickName || post.createBy }}
            </span>
            <span class="post-time">{{ post.createTime }}</span>
          </div>
          <h2 class="post-title">{{ post.title }}</h2>
        </div>

        <!-- 内容 -->
        <div class="post-content" v-html="formattedContent"></div>

        <!-- 图片展示 -->
        <div v-if="post.images && post.images.length > 0" class="post-images">
          <el-image
            v-for="(img, idx) in post.images"
            :key="idx"
            :src="img.imageUrl"
            :preview-src-list="post.images.map(i => i.imageUrl)"
            fit="cover"
            class="post-image-item"
          />
        </div>

        <!-- 互动栏 -->
        <div class="post-actions">
          <el-button icon="el-icon-star-off" size="small" :type="post.isLiked ? 'warning' : ''" @click="handleLike">
            {{ post.likeCount || 0 }} 赞
          </el-button>
          <span class="action-sep">|</span>
          <span class="action-item"><i class="el-icon-view"></i> {{ post.viewCount || 0 }} 浏览</span>
          <span class="action-sep">|</span>
          <span class="action-item"><i class="el-icon-chat-dot-round"></i> {{ post.commentCount || 0 }} 评论</span>
          <span class="action-sep">|</span>
          <el-button type="text" size="small" icon="el-icon-warning-outline" @click="handleReport">举报</el-button>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card" shadow="never">
        <comment-section :post-id="postId" ref="commentSection" />
      </el-card>

      <!-- 举报弹窗 -->
      <report-dialog
        :visible.sync="reportVisible"
        target-type="0"
        :target-id="postId"
        @success="handleReportSuccess"
      />
    </div>

    <!-- 帖子不存在 -->
    <div v-else-if="!loading" class="not-found">
      <i class="el-icon-document-delete" style="font-size: 64px; color: #c0c4cc;"></i>
      <p>帖子不存在或已被删除</p>
    </div>
  </div>
</template>

<script>
import { getPost, delPost } from '@/api/campus/post'
import { toggleLike } from '@/api/campus/like'
import CommentSection from '@/views/campus/comment/CommentSection'
import ReportDialog from '@/views/campus/report/ReportDialog'

export default {
  name: 'CampusPostDetail',
  components: { CommentSection, ReportDialog },
  data() {
    return {
      loading: true,
      post: null,
      isOwner: false,
      reportVisible: false
    }
  },
  computed: {
    postId() {
      return this.$route.params.postId
    },
    formattedContent() {
      if (!this.post || !this.post.content) return ''
      return this.post.content.replace(/\n/g, '<br>')
    }
  },
  created() {
    this.loadPost()
  },
  methods: {
    loadPost() {
      this.loading = true
      getPost(this.postId).then(response => {
        this.post = response.data
        // 判断是否帖子作者
        const currentUser = this.$store.state.user?.name
        this.isOwner = currentUser && this.post.createBy === currentUser
        this.loading = false
        document.title = (this.post ? this.post.title : '帖子详情') + ' - 校园墙'
      }).catch(() => {
        this.loading = false
      })
    },
    goBack() {
      this.$router.push('/campus/post')
    },
    handleEdit() {
      this.$router.push('/campus/post/edit/' + this.postId)
    },
    handleDelete() {
      this.$confirm('确定删除该帖子吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delPost(this.postId).then(() => {
          this.$message.success('删除成功')
          this.$router.push('/campus/post')
        })
      }).catch(() => {})
    },
    async handleLike() {
      if (!this.$store.state.user.token) {
        this.$message.warning('请先登录')
        return
      }
      try {
        const res = await toggleLike({ targetType: '0', targetId: this.postId })
        if (res.code === 200) {
          const { liked } = res.data
          this.post.isLiked = liked
          this.post.likeCount = (this.post.likeCount || 0) + (liked ? 1 : -1)
        }
      } catch (e) {
        console.error('点赞失败', e)
      }
    },
    handleReport() {
      if (!this.$store.state.user.token) {
        this.$message.warning('请先登录')
        return
      }
      this.reportVisible = true
    },
    handleReportSuccess() {
      this.$message.success('举报提交成功')
    }
  }
}
</script>

<style scoped>
.campus-post-detail {
  max-width: 800px;
  margin: 0 auto;
}

.detail-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.post-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #909399;
}

.post-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

.post-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
  white-space: pre-wrap;
  word-break: break-all;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 24px;
}

.post-image-item {
  width: 100%;
  height: 160px;
  border-radius: 4px;
  cursor: pointer;
}

.post-image-item >>> .el-image__inner {
  border-radius: 4px;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  font-size: 13px;
  color: #909399;
}

.action-sep {
  color: #dcdfe6;
}

.comment-card {
  border-radius: 8px;
}

.comment-card >>> .comment-placeholder {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
}

.not-found {
  text-align: center;
  padding: 80px 0;
  color: #c0c4cc;
}

.not-found p {
  margin-top: 16px;
  font-size: 16px;
}
</style>

<template>
  <div class="comment-section">
    <div class="comment-header">
      <span class="comment-title">
        <i class="el-icon-chat-dot-round"></i> 评论区 ({{ totalCount }})
      </span>
    </div>

    <!-- 发表评论输入框 -->
    <div class="comment-input-box">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="1000"
        show-word-limit
        :disabled="!isLogin"
      />
      <div class="comment-input-footer">
        <span v-if="!isLogin" class="login-tip">请先登录后评论</span>
        <el-button
          type="primary"
          size="small"
          :disabled="!commentContent.trim() || submitting"
          :loading="submitting"
          @click="handleSubmit"
        >
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div v-if="comments.length > 0" class="comment-list">
      <div
        v-for="comment in comments"
        :key="comment.commentId"
        class="comment-item"
      >
        <!-- 一级评论 -->
        <div class="comment-body">
          <div class="comment-avatar">
            <img :src="comment.avatar || defaultAvatar" alt="avatar" />
          </div>
          <div class="comment-main">
            <div class="comment-user">
              <span class="user-name">{{ comment.nickName || comment.createBy || '匿名用户' }}</span>
              <span v-if="comment.replyToName" class="reply-to">
                回复 <span class="reply-name">@{{ comment.replyToName }}</span>
              </span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
              <span class="action-time">{{ parseTime(comment.createTime) }}</span>
              <div class="action-btns">
                <span class="action-btn" @click="handleLike(comment, '1')">
                  <i :class="comment.isLiked ? 'el-icon-thumb active' : 'el-icon-thumb'" />
                  <span v-if="comment.likeCount > 0">{{ comment.likeCount }}</span>
                </span>
                <span class="action-btn reply-btn" @click="handleReply(comment)">
                  <i class="el-icon-chat-line-square" /> 回复
                </span>
                <span class="action-btn report-btn" @click="handleReport(comment, '1')">
                  <i class="el-icon-warning-outline" /> 举报
                </span>
                <span
                  v-if="canDelete(comment)"
                  class="action-btn delete-btn"
                  @click="handleDelete(comment)"
                >
                  <i class="el-icon-delete" /> 删除
                </span>
              </div>
            </div>

            <!-- 子回复列表 -->
            <div v-if="comment.children && comment.children.length > 0" class="child-comments">
              <div
                v-for="child in comment.children"
                :key="child.commentId"
                class="child-comment-item"
              >
                <div class="child-comment-body">
                  <div class="comment-avatar small">
                    <img :src="child.avatar || defaultAvatar" alt="avatar" />
                  </div>
                  <div class="comment-main">
                    <div class="comment-user">
                      <span class="user-name">{{ child.nickName || child.createBy || '匿名用户' }}</span>
                      <span v-if="child.replyToName" class="reply-to">
                        回复 <span class="reply-name">@{{ child.replyToName }}</span>
                      </span>
                    </div>
                    <div class="comment-content">{{ child.content }}</div>
                    <div class="comment-actions">
                      <span class="action-time">{{ parseTime(child.createTime) }}</span>
                      <div class="action-btns">
                        <span class="action-btn" @click="handleLike(child, '1')">
                          <i :class="child.isLiked ? 'el-icon-thumb active' : 'el-icon-thumb'" />
                          <span v-if="child.likeCount > 0">{{ child.likeCount }}</span>
                        </span>
                        <span class="action-btn reply-btn" @click="handleReplySub(child, comment.commentId)">
                          <i class="el-icon-chat-line-square" /> 回复
                        </span>
                        <span class="action-btn report-btn" @click="handleReport(child, '1')">
                          举报
                        </span>
                        <span
                          v-if="canDelete(child)"
                          class="action-btn delete-btn"
                          @click="handleDelete(child)"
                        >
                          <i class="el-icon-delete" />
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 子回复输入框 -->
            <div v-if="replyTarget && replyTarget.parentId === comment.commentId" class="reply-input-box">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="`回复 @${replyTarget.replyToName || replyTarget.createBy}:`"
                maxlength="500"
                show-word-limit
              />
              <div class="reply-input-footer">
                <el-button size="mini" @click="cancelReply">取消</el-button>
                <el-button type="primary" size="mini" :loading="submitting" @click="handleSubmitReply">
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="comments.length > 0" class="load-more">
      <span>— 已加载全部评论 —</span>
    </div>

    <!-- 空状态 -->
    <div v-if="comments.length === 0 && !loading" class="empty-comments">
      <i class="el-icon-chat-dot-round" />
      <p>暂无评论，来说两句吧</p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-comments">
      <i class="el-icon-loading" />
      <p>加载评论中...</p>
    </div>

    <!-- 举报弹窗 -->
    <report-dialog
      :visible.sync="reportVisible"
      :target-type="reportTargetType"
      :target-id="reportTargetId"
      @success="handleReportSuccess"
    />
  </div>
</template>

<script>
import { listComment, addComment, delComment } from '@/api/campus/comment'
import { toggleLike, batchLikeStatus } from '@/api/campus/like'
import ReportDialog from '@/views/campus/report/ReportDialog'
import defaultUserAvatar from '@/assets/images/profile.jpg'
import { parseTime } from '@/utils/ruoyi'

export default {
  name: 'CommentSection',
  components: { ReportDialog },
  props: {
    postId: { type: [Number, String], required: true }
  },
  data() {
    return {
      comments: [],
      totalCount: 0,
      commentContent: '',
      replyTarget: null,
      replyContent: '',
      submitting: false,
      loading: false,
      defaultAvatar: defaultUserAvatar,
      // 举报
      reportVisible: false,
      reportTargetType: '1',
      reportTargetId: null
    }
  },
  computed: {
    isLogin() {
      return !!this.$store.state.user.token
    }
  },
  watch: {
    postId: {
      immediate: true,
      handler() {
        if (this.postId) this.fetchComments()
      }
    }
  },
  methods: {
    parseTime,
    async fetchComments() {
      if (!this.postId) return
      this.loading = true
      try {
        const res = await listComment(this.postId)
        if (res.code === 200) {
          this.comments = res.data || []
          this.countTotal()
          // 批量获取点赞状态
          await this.loadLikeStatus()
        }
      } catch (e) {
        console.error('加载评论失败', e)
      } finally {
        this.loading = false
      }
    },
    countTotal() {
      let count = this.comments.length
      this.comments.forEach(c => {
        if (c.children) count += c.children.length
      })
      this.totalCount = count
    },
    async loadLikeStatus() {
      const allIds = []
      this.comments.forEach(c => {
        allIds.push(c.commentId)
        if (c.children) {
          c.children.forEach(child => allIds.push(child.commentId))
        }
      })
      if (allIds.length === 0) return
      try {
        const res = await batchLikeStatus({ targetType: '1', targetIds: allIds })
        const statusMap = res.code === 200 ? (res.data || {}) : {}

        this.comments.forEach(c => {
          c.isLiked = !!statusMap[c.commentId]
          if (c.children) {
            c.children.forEach(child => {
              child.isLiked = !!statusMap[child.commentId]
            })
          }
        })
      } catch (e) {
        console.error('加载点赞状态失败', e)
      }
    },
    async handleSubmit() {
      if (!this.commentContent.trim()) return
      this.submitting = true
      try {
        const res = await addComment({
          postId: this.postId,
          content: this.commentContent.trim()
        })
        if (res.code === 200) {
          this.commentContent = ''
          this.$message.success('评论发表成功')
          this.fetchComments()
        } else {
          this.$message.warning(res.msg || '评论发表失败')
        }
      } catch (e) {
        this.$message.error('评论发表失败')
      } finally {
        this.submitting = false
      }
    },
    handleReply(comment) {
      this.replyTarget = {
        parentId: comment.commentId,
        replyToId: comment.commentId,
        replyToUid: comment.userId,
        replyToName: comment.nickName || comment.createBy
      }
      this.replyContent = ''
    },
    handleReplySub(child, parentId) {
      this.replyTarget = {
        parentId: parentId,
        replyToId: child.commentId,
        replyToUid: child.userId,
        replyToName: child.nickName || child.createBy
      }
      this.replyContent = ''
    },
    cancelReply() {
      this.replyTarget = null
      this.replyContent = ''
    },
    async handleSubmitReply() {
      if (!this.replyContent.trim() || !this.replyTarget) return
      this.submitting = true
      try {
        const res = await addComment({
          postId: this.postId,
          content: this.replyContent.trim(),
          parentId: this.replyTarget.parentId,
          replyToId: this.replyTarget.replyToId,
          replyToUid: this.replyTarget.replyToUid,
          replyToName: this.replyTarget.replyToName
        })
        if (res.code === 200) {
          this.replyContent = ''
          this.replyTarget = null
          this.$message.success('回复成功')
          this.fetchComments()
        } else {
          this.$message.warning(res.msg || '回复失败')
        }
      } catch (e) {
        this.$message.error('回复失败')
      } finally {
        this.submitting = false
      }
    },
    async handleLike(comment, targetType) {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        return
      }
      try {
        const res = await toggleLike({ targetType, targetId: comment.commentId })
        if (res.code === 200) {
          const { liked } = res.data
          comment.isLiked = liked
          comment.likeCount = (comment.likeCount || 0) + (liked ? 1 : -1)
        }
      } catch (e) {
        console.error('点赞操作失败', e)
      }
    },
    async handleDelete(comment) {
      try {
        await this.$confirm('确定删除该评论吗？', '提示', { type: 'warning' })
        const res = await delComment(comment.commentId)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.fetchComments()
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error('删除失败')
      }
    },
    canDelete(comment) {
      const currentUser = this.$store.state.user.name
      return currentUser && comment.createBy === currentUser
    },
    handleReport(comment, targetType) {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        return
      }
      this.reportTargetType = targetType
      this.reportTargetId = comment.commentId
      this.reportVisible = true
    },
    handleReportSuccess() {
      this.$message.success('举报提交成功')
    }
  }
}
</script>

<style scoped>
.comment-section {
  padding: 16px 0;
}
.comment-header {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.comment-input-box {
  margin-bottom: 24px;
}
.comment-input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.login-tip {
  color: #909399;
  font-size: 12px;
}
.comment-list {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}
.comment-item {
  margin-bottom: 16px;
}
.comment-body {
  display: flex;
}
.comment-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 12px;
}
.comment-avatar.small {
  width: 32px;
  height: 32px;
}
.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.comment-main {
  flex: 1;
  min-width: 0;
}
.comment-user {
  margin-bottom: 4px;
}
.user-name {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
}
.reply-to {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}
.reply-name {
  color: #409eff;
}
.comment-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  word-break: break-all;
  margin-bottom: 6px;
}
.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}
.action-btns {
  display: flex;
  gap: 16px;
}
.action-btn {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s;
}
.action-btn:hover { color: #409eff; }
.action-btn .active { color: #409eff; }
.action-btn.delete-btn:hover { color: #f56c6c; }
.action-btn.report-btn:hover { color: #e6a23c; }
.child-comments {
  margin-top: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.child-comment-item {
  margin-bottom: 8px;
}
.child-comment-item:last-child { margin-bottom: 0; }
.child-comment-body {
  display: flex;
}
.reply-input-box {
  margin-top: 10px;
  padding-left: 52px;
}
.reply-input-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.load-more, .empty-comments, .loading-comments {
  text-align: center;
  padding: 24px 0;
  color: #c0c4cc;
  font-size: 13px;
}
.empty-comments i, .loading-comments i {
  font-size: 32px;
  display: block;
  margin-bottom: 8px;
}
.loading-comments i {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

<template>
  <div class="app-container campus-my-posts">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部帖子" name="all" />
      <el-tab-pane label="审核通过" name="1" />
      <el-tab-pane label="待审核" name="0" />
      <el-tab-pane label="审核驳回" name="2" />
    </el-tabs>

    <el-table v-loading="loading" :data="postList" style="width: 100%">
      <el-table-column label="分类" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.categoryName ? 'success' : 'info'">
            {{ scope.row.categoryName || '未分类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-link type="primary" @click="goDetail(scope.row.postId)">{{ scope.row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.auditStatus === '1'" size="mini" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === '0'" size="mini" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === '2'" size="mini" type="danger">已驳回</el-tag>
          <el-tag v-else size="mini" type="warning">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="互动" width="160" align="center">
        <template slot-scope="scope">
          <span style="margin-right: 8px;"><i class="el-icon-view"></i> {{ scope.row.viewCount || 0 }}</span>
          <span style="margin-right: 8px;"><i class="el-icon-star-off"></i> {{ scope.row.likeCount || 0 }}</span>
          <span><i class="el-icon-chat-dot-round"></i> {{ scope.row.commentCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="160" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="goDetail(scope.row.postId)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row.postId)">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color: #f56c6c" @click="handleDelete(scope.row.postId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
import { myPosts, delPost } from '@/api/campus/post'

export default {
  name: 'CampusMyPosts',
  data() {
    return {
      loading: false,
      activeTab: 'all',
      postList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        auditStatus: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      myPosts(this.queryParams).then(response => {
        this.postList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleTabClick(tab) {
      if (tab.name === 'all') {
        this.queryParams.auditStatus = undefined
      } else {
        this.queryParams.auditStatus = tab.name
      }
      this.queryParams.pageNum = 1
      this.getList()
    },
    goDetail(postId) {
      this.$router.push('/campus/post/detail/' + postId)
    },
    handleEdit(postId) {
      this.$router.push('/campus/post/edit/' + postId)
    },
    handleDelete(postId) {
      this.$confirm('确定删除该帖子吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delPost(postId).then(() => {
          this.$message.success('删除成功')
          this.getList()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.campus-my-posts {
  max-width: 1200px;
  margin: 0 auto;
}
</style>

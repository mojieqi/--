<template>
  <div class="app-container campus-post-edit">
    <el-card class="edit-card" shadow="never">
      <div slot="header">
        <span>{{ isEdit ? '编辑帖子' : '发布帖子' }}</span>
      </div>

      <el-form ref="postForm" :model="form" :rules="rules" label-width="80px">
        <!-- 分类选择 -->
        <el-form-item label="发布板块" prop="categoryId">
          <el-radio-group v-model="form.categoryId" size="small">
            <el-radio-button
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.categoryId"
            >{{ cat.categoryName }}</el-radio-button>
          </el-radio-group>
          <span class="form-tip">未选择则由AI自动分类</span>
        </el-form-item>

        <!-- 标题 -->
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入帖子标题(2-200字)" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 内容 -->
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="分享你的校园生活...&#10;&#10;提示：友善发言，文明交流"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- 图片上传 -->
        <el-form-item label="配图">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="fileList"
            :limit="9"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :before-upload="beforeUpload"
            :disabled="uploading"
          >
            <i class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">
              最多9张图片，支持jpg/png/gif，单张不超过10MB
            </div>
          </el-upload>
        </el-form-item>

        <!-- 匿名发布 -->
        <el-form-item label="匿名发布">
          <el-switch
            v-model="form.isAnonymous"
            active-value="1"
            inactive-value="0"
            active-text="匿名"
            inactive-text="实名"
          />
          <span class="form-tip" style="margin-left:8px;">启用后你的身份将不会公开显示</span>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitForm">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>

      <!-- AI审核提示 -->
      <el-alert
        v-if="submitResult"
        :title="submitResult.title"
        :type="submitResult.type"
        :description="submitResult.description"
        show-icon
        :closable="true"
        style="margin-top: 16px;"
      />
    </el-card>
  </div>
</template>

<script>
import { addPost, updatePost, getPost, categoryList } from '@/api/campus/post'
import { getToken } from '@/utils/auth'

export default {
  name: 'CampusPostEdit',
  data() {
    return {
      isEdit: false,
      submitting: false,
      uploading: false,
      categories: [],
      fileList: [],
      submitResult: null,
      form: {
        postId: undefined,
        categoryId: undefined,
        title: '',
        content: '',
        isAnonymous: '0',
        images: []
      },
      rules: {
        title: [
          { required: true, message: '请输入帖子标题', trigger: 'blur' },
          { min: 2, message: '标题至少2个字', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入帖子内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    uploadUrl() {
      return process.env.VUE_APP_BASE_API + '/common/upload'
    },
    uploadHeaders() {
      return { Authorization: 'Bearer ' + getToken() }
    },
    postId() {
      return this.$route.params.postId
    }
  },
  created() {
    this.loadCategories()
    if (this.postId) {
      this.isEdit = true
      this.loadPost()
    }
  },
  methods: {
    loadCategories() {
      categoryList().then(response => {
        this.categories = response.data || []
      })
    },
    loadPost() {
      getPost(this.postId).then(response => {
        const post = response.data
        this.form.postId = post.postId
        this.form.categoryId = post.categoryId
        this.form.title = post.title
        this.form.content = post.content
        this.form.isAnonymous = post.isAnonymous || '0'
        if (post.images) {
          this.fileList = post.images.map((img, idx) => ({
            uid: idx,
            name: 'image-' + idx,
            url: img.imageUrl
          }))
          this.form.images = post.images
        }
      })
    },
    handleUploadSuccess(response, file, fileList) {
      this.uploading = false
      // 上传成功后更新images列表
      this.form.images = fileList
        .filter(f => f.response && f.response.data)
        .map((f, idx) => ({
          imageUrl: f.response.data.fileName || f.response.data,
          thumbnailUrl: f.response.data.fileName || f.response.data,
          sort: idx
        }))
    },
    handleUploadRemove(file, fileList) {
      this.form.images = fileList
        .filter(f => f.status === 'success' && f.response && f.response.data)
        .map((f, idx) => ({
          imageUrl: f.response.data.fileName || f.response.data,
          thumbnailUrl: f.response.data.fileName || f.response.data,
          sort: idx
        }))
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }
      if (!isLt10M) {
        this.$message.error('图片大小不能超过10MB')
        return false
      }
      this.uploading = true
      return true
    },
    submitForm() {
      this.$refs.postForm.validate(valid => {
        if (!valid) return

        this.submitting = true
        this.submitResult = null

        const api = this.isEdit ? updatePost(this.form) : addPost(this.form)

        api.then(response => {
          this.submitting = false
          const post = response.data

          if (!this.isEdit) {
            // 新发布，显示审核结果
            if (post && post.auditStatus === '1') {
              this.submitResult = {
                title: '发布成功！AI审核已通过',
                type: 'success',
                description: '你的帖子已发布到校园墙，其他同学可以看到啦'
              }
            } else if (post && post.auditStatus === '2') {
              this.submitResult = {
                title: '发布失败：内容审核未通过',
                type: 'warning',
                description: '审核原因：' + (post.auditReason || '内容可能包含违规信息，请修改后重新发布')
              }
            } else {
              this.submitResult = {
                title: '发布成功，等待审核',
                type: 'info',
                description: '你的帖子已提交，审核通过后将在校园墙展示'
              }
            }
          } else {
            this.$message.success('修改成功')
            this.goBack()
          }
        }).catch(error => {
          this.submitting = false
          this.$message.error('操作失败: ' + (error || '未知错误'))
        })
      })
    },
    goBack() {
      if (this.isEdit) {
        this.$router.push('/campus/post/detail/' + this.postId)
      } else {
        this.$router.push('/campus/post')
      }
    }
  }
}
</script>

<style scoped>
.campus-post-edit {
  max-width: 800px;
  margin: 0 auto;
}

.edit-card {
  border-radius: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.el-upload__tip {
  display: block;
  margin-top: 8px;
}
</style>

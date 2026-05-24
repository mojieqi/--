<template>
  <div class="knowledge-container">
    <!-- 顶部工具栏 -->
    <el-row :gutter="20" class="toolbar">
      <el-col :span="6">
        <el-input v-model="queryParams.kbName" placeholder="搜索知识库名称" clearable @keyup.enter="getList" prefix-icon="el-icon-search"/>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" icon="el-icon-plus" @click="showAddDialog" v-hasPermi="['ai:knowledge:add']">新建知识库</el-button>
      </el-col>
    </el-row>

    <!-- 知识库卡片列表 -->
    <div v-loading="loading" class="kb-card-grid">
      <div class="kb-card" v-for="item in kbList" :key="item.kbId" @click="openKnowledgeBase(item)">
        <div class="card-header">
          <span class="card-title">{{ item.kbName }}</span>
          <el-switch v-model="item._active" active-value="0" inactive-value="1" @change="(val) => handleStatusChange(item, val)" @click.native.stop/>
        </div>
        <div class="card-body">
          <p class="card-desc">{{ item.kbDesc || '暂无描述' }}</p>
        </div>
        <div class="card-footer">
          <span><i class="el-icon-document"/> {{ item.docCount || 0 }} 文档</span>
          <span><i class="el-icon-tickets"/> {{ item.chunkCount || 0 }} 分块</span>
          <span class="card-time">{{ item.createTime }}</span>
        </div>
        <div class="card-actions">
          <el-button type="text" icon="el-icon-edit" @click.stop="showEditDialog(item)" v-hasPermi="['ai:knowledge:edit']">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" style="color:#f56c6c" @click.stop="handleDelete(item)" v-hasPermi="['ai:knowledge:remove']">删除</el-button>
        </div>
      </div>
      <div class="kb-card kb-card-empty" v-if="kbList.length === 0 && !loading">
        <div class="empty-hint">暂无知识库，点击上方按钮新建</div>
      </div>
    </div>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- ==================== 知识库编辑对话框 ==================== -->
    <el-dialog :title="kbDialogTitle" :visible.sync="kbDialogVisible" width="550px" :close-on-click-modal="false">
      <el-form ref="kbForm" :model="kbForm" :rules="kbRules" label-width="100px">
        <el-form-item label="知识库名称" prop="kbName">
          <el-input v-model="kbForm.kbName" placeholder="请输入知识库名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="知识库描述" prop="kbDesc">
          <el-input v-model="kbForm.kbDesc" type="textarea" :rows="3" placeholder="请输入知识库描述" maxlength="500"/>
        </el-form-item>
        <el-form-item label="向量化模型">
          <el-input v-model="kbForm.embeddingModel" placeholder="暂未启用，留空即可" disabled/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="kbDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitKbForm" :loading="kbSubmitting">确 定</el-button>
      </div>
    </el-dialog>

    <!-- ==================== 文档管理抽屉 ==================== -->
    <el-drawer :title="'知识库: ' + currentKb.kbName" :visible.sync="docDrawerVisible" size="70%" direction="rtl" :before-close="closeDocDrawer">
      <!-- 文档操作工具栏 -->
      <el-row :gutter="10" class="doc-toolbar">
        <el-col :span="8">
          <el-input v-model="docQueryParams.docName" placeholder="搜索文档名称" clearable @keyup.enter="getDocList" prefix-icon="el-icon-search" size="small"/>
        </el-col>
        <el-col :span="6">
          <el-upload :action="uploadUrl" :data="uploadData" :headers="uploadHeaders" :before-upload="beforeUpload"
            :on-success="onUploadSuccess" :on-error="onUploadError" :show-file-list="false" accept=".txt,.md,.pdf,.docx,.xlsx">
            <el-button type="primary" icon="el-icon-upload2" size="small" v-hasPermi="['ai:knowledge:doc:upload']">上传文档</el-button>
          </el-upload>
        </el-col>
        <el-col :span="4" :offset="6">
          <el-input v-model="searchKeyword" placeholder="检索内容" size="small" @keyup.enter="doSearch">
            <el-button slot="append" icon="el-icon-search" @click="doSearch" size="small"/>
          </el-input>
        </el-col>
        <el-col :span="24" style="margin-top:6px;color:#909399;font-size:12px;">
          支持 txt、md、pdf、docx、xlsx 格式，单个文件不超过50MB
        </el-col>
      </el-row>

      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <el-alert :title="'检索到 ' + searchResults.length + ' 个相关分块'" type="success" :closable="false" show-icon/>
        <div v-for="(chunk, idx) in searchResults" :key="idx" class="search-chunk-item">
          <span class="chunk-index">#{{ chunk.chunkIndex + 1 }}</span>
          <p class="chunk-text">{{ chunk.chunkContent }}</p>
        </div>
        <el-button type="text" icon="el-icon-close" @click="clearSearch" style="margin-top:8px;">清除搜索结果</el-button>
      </div>

      <!-- 文档表格 -->
      <el-table v-loading="docLoading" :data="docList" style="margin-top:10px;" v-if="searchResults.length === 0">
        <el-table-column label="文档名称" prop="docName" min-width="200" show-overflow-tooltip/>
        <el-table-column label="类型" prop="docType" width="80">
          <template slot-scope="scope">
            <el-tag size="mini" :type="docTypeTag(scope.row.docType)">{{ scope.row.docType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" prop="fileSize" width="100">
          <template slot-scope="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="分块" prop="chunkCount" width="70" align="center"/>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.status === '0'" style="color:#e6a23c;"><i class="el-icon-loading"/> 处理中</span>
            <span v-else-if="scope.row.status === '1'" style="color:#67c23a;"><i class="el-icon-circle-check"/> 完成</span>
            <span v-else style="color:#f56c6c;"><i class="el-icon-circle-close"/> 失败</span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" prop="createTime" width="160"/>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" icon="el-icon-view" @click="viewDocChunks(scope.row)" v-hasPermi="['ai:knowledge:doc:query']">分块</el-button>
            <el-button type="text" icon="el-icon-refresh" @click="handleReparse(scope.row)" v-hasPermi="['ai:knowledge:doc:reparse']">重解析</el-button>
            <el-button type="text" icon="el-icon-delete" style="color:#f56c6c" @click="handleDeleteDoc(scope.row)" v-hasPermi="['ai:knowledge:doc:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="docTotal > 0 && searchResults.length === 0" :total="docTotal" :page.sync="docQueryParams.pageNum" :limit.sync="docQueryParams.pageSize" @pagination="getDocList" layout="total, prev, pager, next"/>
    </el-drawer>

    <!-- ==================== 分块查看对话框 ==================== -->
    <el-dialog title="文档分块详情" :visible.sync="chunkDialogVisible" width="800px">
      <div class="chunk-list" v-if="chunkList.length > 0">
        <div class="chunk-item" v-for="(chunk, idx) in chunkList" :key="idx">
          <div class="chunk-header">分块 #{{ chunk.chunkIndex + 1 }} <span class="chunk-tokens">{{ chunk.tokenCount }} tokens</span></div>
          <p class="chunk-content">{{ chunk.chunkContent }}</p>
        </div>
      </div>
      <div v-else style="text-align:center;color:#999;padding:40px;">暂无分块数据</div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledgeBase, getKnowledgeBase, addKnowledgeBase, updateKnowledgeBase, delKnowledgeBase }
  from '@/api/ai/knowledge'
import { listDocument, getDocument, delDocument, reparseDocument, searchKnowledge }
  from '@/api/ai/knowledge'
import { getToken } from '@/utils/auth'

export default {
  name: 'AiKnowledge',
  data() {
    return {
      loading: false,
      kbList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, kbName: '' },

      // 知识库对话框
      kbDialogVisible: false,
      kbDialogTitle: '',
      kbSubmitting: false,
      isKbEdit: false,
      kbForm: {},
      kbRules: {
        kbName: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }]
      },

      // 文档抽屉
      docDrawerVisible: false,
      currentKb: {},
      docLoading: false,
      docList: [],
      docTotal: 0,
      docQueryParams: { pageNum: 1, pageSize: 10, kbId: null, docName: '' },
      uploadUrl: process.env.VUE_APP_BASE_API + '/ai/knowledge/doc/upload',
      uploadHeaders: { Authorization: 'Bearer ' + getToken() },

      // 检索
      searchKeyword: '',
      searchResults: [],

      // 分块查看
      chunkDialogVisible: false,
      chunkList: []
    }
  },
  computed: {
    uploadData() {
      return { kbId: this.currentKb.kbId }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    // ==================== 知识库列表 ====================
    getList() {
      this.loading = true
      listKnowledgeBase(this.queryParams).then(response => {
        this.kbList = (response.rows || []).map(item => {
          item._active = item.status
          return item
        })
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },

    showAddDialog() {
      this.isKbEdit = false
      this.kbDialogTitle = '新建知识库'
      this.kbForm = { status: '0' }
      this.kbDialogVisible = true
      this.$nextTick(() => { if (this.$refs.kbForm) this.$refs.kbForm.clearValidate() })
    },

    showEditDialog(row) {
      this.isKbEdit = true
      this.kbDialogTitle = '修改知识库'
      this.kbForm = { ...row }
      this.kbDialogVisible = true
      this.$nextTick(() => { if (this.$refs.kbForm) this.$refs.kbForm.clearValidate() })
    },

    submitKbForm() {
      this.$refs.kbForm.validate(valid => {
        if (!valid) return
        this.kbSubmitting = true
        const action = this.isKbEdit ? updateKnowledgeBase(this.kbForm) : addKnowledgeBase(this.kbForm)
        action.then(() => {
          this.$modal.msgSuccess(this.isKbEdit ? '修改成功' : '新增成功')
          this.kbDialogVisible = false
          this.getList()
        }).finally(() => { this.kbSubmitting = false })
      })
    },

    handleDelete(row) {
      this.$modal.confirm('确定删除知识库「' + row.kbName + '」？删除后知识库内的所有文档和分块数据将一并清除。').then(() => {
        return delKnowledgeBase(row.kbId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },

    handleStatusChange(row, val) {
      updateKnowledgeBase({ kbId: row.kbId, status: val }).then(() => {
        this.$modal.msgSuccess(val === '0' ? '已启用' : '已停用')
      }).catch(() => {
        row._active = val === '0' ? '1' : '0'
      })
    },

    // ==================== 文档管理 ====================
    openKnowledgeBase(row) {
      this.currentKb = row
      this.docQueryParams.kbId = row.kbId
      this.docQueryParams.docName = ''
      this.searchKeyword = ''
      this.searchResults = []
      this.docDrawerVisible = true
      this.getDocList()
    },

    closeDocDrawer() {
      this.docDrawerVisible = false
      this.getList()
    },

    getDocList() {
      this.docLoading = true
      listDocument(this.docQueryParams).then(response => {
        this.docList = response.rows || []
        this.docTotal = response.total || 0
        this.docLoading = false
      }).catch(() => { this.docLoading = false })
    },

    beforeUpload(file) {
      const allowed = ['txt', 'md', 'pdf', 'docx', 'xlsx']
      const ext = file.name.split('.').pop().toLowerCase()
      if (!allowed.includes(ext)) {
        this.$modal.msgError('仅支持 ' + allowed.join('/') + ' 格式')
        return false
      }
      if (file.size > 50 * 1024 * 1024) {
        this.$modal.msgError('文件不能超过50MB')
        return false
      }
      this.docLoading = true
      return true
    },

    onUploadSuccess(response) {
      this.docLoading = false
      if (response.code === 200) {
        this.$modal.msgSuccess('上传成功，已自动解析')
        this.getDocList()
      } else {
        this.$modal.msgError(response.msg || '上传失败')
      }
    },

    onUploadError() {
      this.docLoading = false
      this.$modal.msgError('上传失败')
    },

    handleDeleteDoc(row) {
      this.$modal.confirm('确定删除文档「' + row.docName + '」？').then(() => {
        return delDocument(row.docId)
      }).then(() => {
        this.getDocList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },

    handleReparse(row) {
      this.$modal.confirm('确定重新解析文档「' + row.docName + '」？将重新提取文本并分块。').then(() => {
        return reparseDocument(row.docId)
      }).then(response => {
        this.$modal.msgSuccess('重新解析完成，共 ' + response.data.chunkCount + ' 个分块')
        this.getDocList()
      }).catch(() => {})
    },

    viewDocChunks(row) {
      getDocument(row.docId).then(response => {
        this.chunkList = (response.data && response.data.chunks) || []
        this.chunkDialogVisible = true
      })
    },

    // ==================== 检索 ====================
    doSearch() {
      if (!this.searchKeyword.trim()) return this.$modal.msgWarning('请输入检索关键词')
      searchKnowledge({ kbId: this.currentKb.kbId, keyword: this.searchKeyword }).then(response => {
        this.searchResults = response.data || []
        if (this.searchResults.length === 0) {
          this.$modal.msgInfo('未找到相关内容')
        }
      })
    },

    clearSearch() {
      this.searchResults = []
      this.searchKeyword = ''
    },

    // ==================== 工具方法 ====================
    docTypeTag(type) {
      const map = { txt: '', md: '', pdf: 'danger', docx: 'primary', xlsx: 'success' }
      return map[type] || ''
    },

    formatFileSize(bytes) {
      if (!bytes) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB']
      let i = 0
      let size = bytes
      while (size >= 1024 && i < units.length - 1) { size /= 1024; i++ }
      return size.toFixed(i === 0 ? 0 : 1) + ' ' + units[i]
    }
  }
}
</script>

<style scoped>
.knowledge-container { padding: 10px 0; }

/* 工具栏 */
.toolbar { margin-bottom: 15px; }

/* 知识库卡片网格 */
.kb-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.kb-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 18px 20px;
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.2s;
}
.kb-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.08); transform: translateY(-2px); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.card-title { font-size: 16px; font-weight: 600; color: #303133; }

.card-body { margin-bottom: 12px; }
.card-desc { color: #909399; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin: 0; }

.card-footer { display: flex; gap: 16px; font-size: 12px; color: #c0c4cc; }
.card-time { margin-left: auto; }

.card-actions { margin-top: 12px; padding-top: 10px; border-top: 1px solid #f2f3f5; display: flex; gap: 8px; }

.kb-card-empty { display: flex; align-items: center; justify-content: center; cursor: default; }
.empty-hint { color: #c0c4cc; font-size: 14px; }

/* 文档工具栏 */
.doc-toolbar { margin: 10px 0 5px; }

/* 搜索结果 */
.search-results { margin-top: 12px; }
.search-chunk-item { background: #f0f9eb; border-left: 3px solid #67c23a; margin-top: 8px; padding: 10px 14px; border-radius: 4px; }
.chunk-index { font-size: 12px; color: #67c23a; font-weight: bold; }
.chunk-text { margin: 6px 0 0; font-size: 13px; color: #606266; line-height: 1.6; white-space: pre-wrap; word-break: break-all; }

/* 分块列表 */
.chunk-list { max-height: 500px; overflow-y: auto; }
.chunk-item { background: #fafafa; border: 1px solid #ebeef5; margin-bottom: 10px; border-radius: 6px; padding: 12px 16px; }
.chunk-header { font-size: 13px; font-weight: 600; color: #409eff; margin-bottom: 6px; }
.chunk-tokens { font-size: 11px; color: #c0c4cc; font-weight: normal; margin-left: 10px; }
.chunk-content { font-size: 13px; color: #606266; line-height: 1.7; white-space: pre-wrap; word-break: break-all; margin: 0; }
</style>

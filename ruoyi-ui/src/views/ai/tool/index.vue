<template>
  <div class="tool-container">
    <!-- 顶部工具栏 -->
    <el-row :gutter="20" class="toolbar">
      <el-col :span="6">
        <el-input v-model="queryParams.toolName" placeholder="搜索工具名称" clearable @keyup.enter="getList" prefix-icon="el-icon-search"/>
      </el-col>
      <el-col :span="6">
        <el-select v-model="queryParams.status" placeholder="启用状态" clearable @change="getList" style="width:100%">
          <el-option label="已启用" value="0"/>
          <el-option label="已停用" value="1"/>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" icon="el-icon-plus" @click="showAddDialog" v-hasPermi="['ai:tool:add']">新增工具</el-button>
      </el-col>
    </el-row>

    <!-- 提示栏 -->
    <el-alert type="info" :closable="false" show-icon style="margin-bottom:15px;">
      <template slot="title">
        重新应用后，ToolRegistry 会加载所有<span style="font-weight:600;">已启用</span>的工具。停用内置工具后，Agent 将无法调用该工具。
      </template>
    </el-alert>

    <!-- 工具卡片网格 -->
    <div v-loading="loading" class="tool-card-grid">
      <div class="tool-card" v-for="item in toolList" :key="item.toolId">
        <!-- 卡片顶部标签 -->
        <div class="card-header">
          <span class="card-title">{{ item.toolName }}</span>
          <div class="card-badges">
            <el-tag v-if="item.isBuiltin === '1'" size="mini" type="warning" effect="plain">内置</el-tag>
            <el-tag size="mini" :type="item.status === '0' ? 'success' : 'info'">{{ item.status === '0' ? '已启用' : '已停用' }}</el-tag>
          </div>
        </div>

        <!-- 工具代码 -->
        <div class="card-code">
          <code>{{ item.toolCode }}</code>
        </div>

        <!-- 工具描述 -->
        <div class="card-body">
          <p class="card-desc">{{ item.toolDesc || '暂无描述' }}</p>
        </div>

        <!-- 处理器类 -->
        <div class="card-handler">
          <el-tooltip :content="item.handlerClass" placement="top">
            <span class="handler-text"><i class="el-icon-cpu"/> {{ shortHandler(item.handlerClass) }}</span>
          </el-tooltip>
        </div>

        <!-- 页脚信息 -->
        <div class="card-footer">
          <span class="card-time">{{ item.createTime }}</span>
        </div>

        <!-- 操作区 -->
        <div class="card-actions">
          <el-button type="text" icon="el-icon-switch-button" @click="handleStatusToggle(item)"
            v-hasPermi="['ai:tool:changeStatus']">
            {{ item.status === '0' ? '停用' : '启用' }}
          </el-button>
          <el-button type="text" icon="el-icon-edit" @click="showEditDialog(item)" v-hasPermi="['ai:tool:edit']">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" style="color:#f56c6c" @click="handleDelete(item)"
            v-hasPermi="['ai:tool:remove']" :disabled="item.isBuiltin === '1'">删除</el-button>
        </div>
      </div>

      <div class="tool-card tool-card-empty" v-if="toolList.length === 0 && !loading">
        <div class="empty-hint">暂无工具，点击上方「新增工具」按钮添加自定义工具</div>
      </div>
    </div>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- ==================== 新增/编辑对话框 ==================== -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="650px" :close-on-click-modal="false">
      <el-form ref="toolForm" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="工具名称" prop="toolName">
          <el-input v-model="form.toolName" placeholder="请输入工具名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="工具代码" prop="toolCode">
          <el-input v-model="form.toolCode" placeholder="英文标识，如 my_custom_tool" maxlength="50"
            :disabled="isEdit && form.isBuiltin === '1'"/>
          <span class="form-tip">唯一标识，内置工具不可修改</span>
        </el-form-item>
        <el-form-item label="工具描述" prop="toolDesc">
          <el-input v-model="form.toolDesc" type="textarea" :rows="2" placeholder="简述工具功能" maxlength="500"/>
        </el-form-item>
        <el-form-item label="处理器类" prop="handlerClass">
          <el-input v-model="form.handlerClass" placeholder="com.ruoyi.system.agent.tool.YourTool" maxlength="200"
            :disabled="isEdit && form.isBuiltin === '1'"/>
          <span class="form-tip">继承 AbstractTool 的完整类路径，内置工具不可修改</span>
        </el-form-item>
        <el-form-item label="Schema定义" prop="functionSchema">
          <el-input v-model="form.functionSchema" type="textarea" :rows="6" placeholder='{"name":"...","description":"...","parameters":{...}}'/>
          <span class="form-tip">Function Calling 的 JSON Schema，须包含 name / description / parameters</span>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" controls-position="right"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" maxlength="500"/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
      </div>
    </el-dialog>

    <!-- ==================== Schema 预览弹窗 ==================== -->
    <el-dialog title="Schema 预览" :visible.sync="schemaDialogVisible" width="600px">
      <div class="schema-preview">
        <pre>{{ schemaPreview }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTool, getTool, addTool, updateTool, delTool, changeToolStatus } from '@/api/ai/tool'

export default {
  name: 'AiTool',
  data() {
    const validateSchema = (rule, value, callback) => {
      if (!value) return callback(new Error('请输入 Schema 定义'))
      try {
        const obj = JSON.parse(value)
        if (!obj.name) return callback(new Error('Schema 缺少 name 字段'))
        if (!obj.description) return callback(new Error('Schema 缺少 description 字段'))
        if (!obj.parameters || typeof obj.parameters !== 'object') return callback(new Error('Schema 缺少 parameters 对象'))
        callback()
      } catch (e) {
        callback(new Error('JSON 格式不正确: ' + e.message))
      }
    }
    return {
      loading: false,
      toolList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, toolName: '', status: undefined },

      // 对话框
      dialogVisible: false,
      dialogTitle: '',
      submitting: false,
      isEdit: false,
      form: { status: '0', sort: 0 },
      rules: {
        toolName: [{ required: true, message: '请输入工具名称', trigger: 'blur' }],
        toolCode: [
          { required: true, message: '请输入工具代码', trigger: 'blur' },
          { pattern: /^[a-zA-Z][a-zA-Z0-9_]+$/, message: '仅支持英文/数字/下划线，且首字符为字母', trigger: 'blur' }
        ],
        handlerClass: [{ required: true, message: '请输入处理器类', trigger: 'blur' }],
        functionSchema: [{ required: true, validator: validateSchema, trigger: 'blur' }]
      },

      // Schema 预览
      schemaDialogVisible: false,
      schemaPreview: ''
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 加载列表 */
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (params.status === '' || params.status === undefined) delete params.status
      listTool(params).then(response => {
        this.toolList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },

    /** 显示新增对话框 */
    showAddDialog() {
      this.isEdit = false
      this.dialogTitle = '新增工具'
      this.form = { status: '0', sort: 0 }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.toolForm) this.$refs.toolForm.clearValidate() })
    },

    /** 显示编辑对话框 */
    showEditDialog(row) {
      this.isEdit = true
      this.dialogTitle = '修改工具'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.toolForm) this.$refs.toolForm.clearValidate() })
    },

    /** 提交表单 */
    submitForm() {
      this.$refs.toolForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        const data = { ...this.form }
        const action = this.isEdit ? updateTool(data) : addTool(data)
        action.then(() => {
          this.$modal.msgSuccess(this.isEdit ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.getList()
        }).finally(() => { this.submitting = false })
      })
    },

    /** 删除工具 */
    handleDelete(row) {
      if (row.isBuiltin === '1') {
        return this.$modal.msgWarning('内置工具不可删除，可通过停用功能禁用它')
      }
      this.$modal.confirm('确定删除工具「' + row.toolName + '」？').then(() => {
        return delTool(row.toolId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },

    /** 切换启用/停用状态 */
    handleStatusToggle(row) {
      const newStatus = row.status === '0' ? '1' : '0'
      const action = newStatus === '0' ? '启用' : '停用'
      this.$modal.confirm('确定' + action + '工具「' + row.toolName + '」？').then(() => {
        return changeToolStatus({ toolIds: [row.toolId], status: newStatus })
      }).then(() => {
        row.status = newStatus
        this.$modal.msgSuccess('已' + action)
      }).catch(() => {})
    },

    /** 截短处理器类名 */
    shortHandler(className) {
      if (!className) return ''
      const parts = className.split('.')
      return parts.length > 2 ? '...' + parts.slice(-2).join('.') : className
    }
  }
}
</script>

<style scoped>
.tool-container { padding: 10px 0; }

.toolbar { margin-bottom: 15px; }

.form-tip { font-size: 12px; color: #909399; }

/* 卡片网格 */
.tool-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.tool-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 18px 20px;
  transition: box-shadow 0.25s, transform 0.2s;
}
.tool-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.08); transform: translateY(-2px); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.card-title { font-size: 16px; font-weight: 600; color: #303133; }
.card-badges { display: flex; gap: 6px; }

.card-code {
  margin-bottom: 10px;
}
.card-code code {
  display: inline-block;
  background: #f5f7fa;
  color: #409eff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 3px;
  font-family: "JetBrains Mono", "Consolas", monospace;
}

.card-body { margin-bottom: 10px; }
.card-desc { color: #606266; font-size: 13px; line-height: 1.5; margin: 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.card-handler { margin-bottom: 10px; }
.handler-text {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  font-family: "JetBrains Mono", "Consolas", monospace;
}

.card-footer { display: flex; font-size: 12px; color: #c0c4cc; margin-bottom: 2px; }
.card-time { margin-left: auto; }

.card-actions {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f2f3f5;
  display: flex;
  gap: 4px;
}
.card-actions .el-button--text { padding: 4px 8px; font-size: 13px; }

.tool-card-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
}
.empty-hint { color: #c0c4cc; font-size: 14px; }

/* Schema 预览 */
.schema-preview pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 400px;
  overflow-y: auto;
}
</style>

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

    <!-- 注册状态摘要栏 (Phase 4.5 前后端联动) -->
    <div v-if="registryData" class="registry-bar" :class="registryStatusClass">
      <div class="registry-summary">
        <i :class="registryStatusIcon"/>
        <span class="registry-text">
          <strong>ToolRegistry:</strong> {{ registryData.registeredCount }}/{{ registryData.totalEnabled }} 工具可注册
          <template v-if="registryData.failedCount > 0">
            · <span class="registry-warn">{{ registryData.failedCount }} 个处理器类缺失</span>
          </template>
        </span>
      </div>
      <el-button type="text" icon="el-icon-refresh" @click="fetchRegistryStatus" :loading="registryLoading" style="font-size:13px;">刷新</el-button>
    </div>

    <!-- 工具卡片网格 -->
    <div v-loading="loading" class="tool-card-grid">
      <div class="tool-card" v-for="item in toolList" :key="item.toolId">
        <!-- 卡片顶部标签 -->
        <div class="card-header">
          <span class="card-title">{{ item.toolName }}</span>
          <div class="card-badges">
            <el-tag v-if="item.isBuiltin === '1'" size="mini" type="warning" effect="plain">内置</el-tag>
            <el-tag size="mini" :type="getRegistryBadge(item).statusType"
              :effect="getRegistryBadge(item).effect || 'plain'">
              {{ getRegistryBadge(item).text }}
            </el-tag>
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

        <!-- Schema 参数预览 (Phase 4.5) -->
        <div class="card-schema-toggle" v-if="item.functionSchema" @click="toggleSchemaParams(item)">
          <i :class="item._schemaExpanded ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"/>
          <span>参数详情 ({{ parseSchemaParamCount(item.functionSchema) }} 个参数)</span>
        </div>
        <div class="card-schema-params" v-if="item._schemaExpanded && item.functionSchema">
          <table class="param-mini-table">
            <thead><tr><th>参数名</th><th>类型</th><th>必填</th><th>描述</th></tr></thead>
            <tbody>
              <tr v-for="p in parseSchemaParams(item.functionSchema)" :key="p.name">
                <td><code>{{ p.name }}</code></td>
                <td><span class="param-type">{{ p.type }}</span></td>
                <td><el-tag size="mini" :type="p.required ? 'danger' : 'info'">{{ p.required ? '是' : '否' }}</el-tag></td>
                <td class="param-desc">{{ p.description || '-' }}</td>
              </tr>
            </tbody>
          </table>
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
          <el-button type="text" icon="el-icon-document-copy" @click="copyToolCode(item)">复制</el-button>
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
          <div class="schema-input-wrapper">
            <el-input v-model="form.functionSchema" type="textarea" :rows="6" placeholder='{"name":"...","description":"...","parameters":{...}}'/>
            <div class="schema-actions">
              <el-button type="text" size="mini" icon="el-icon-magic-stick" @click="formatSchema">格式化 JSON</el-button>
              <el-button type="text" size="mini" icon="el-icon-view" @click="previewSchema">预览参数表格</el-button>
            </div>
          </div>
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

    <!-- ==================== Schema 预览弹窗 (Phase 4.5 增强) ==================== -->
    <el-dialog title="Schema 预览" :visible.sync="schemaDialogVisible" width="650px">
      <!-- 参数表格 -->
      <div v-if="schemaParamRows.length > 0" class="schema-param-table">
        <h4 style="margin-bottom:12px;color:#303133;">参数列表 ({{ schemaParamRows.length }})</h4>
        <el-table :data="schemaParamRows" border size="small" max-height="220">
          <el-table-column prop="name" label="参数名" width="140">
            <template slot-scope="scope"><code>{{ scope.row.name }}</code></template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template slot-scope="scope"><el-tag size="mini">{{ scope.row.type }}</el-tag></template>
          </el-table-column>
          <el-table-column label="必填" width="70" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row.required ? 'danger' : 'info'">{{ scope.row.required ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip/>
        </el-table>
        <el-divider/>
      </div>
      <!-- JSON 原文 -->
      <div class="schema-preview">
        <pre>{{ schemaPreview }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTool, getTool, addTool, updateTool, delTool, changeToolStatus, registryStatus } from '@/api/ai/tool'

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
      schemaPreview: '',

      // Phase 4.5: 注册状态 & Schema参数预览
      registryData: null,
      registryLoading: false,
      schemaParamDialogVisible: false,
      schemaParamRows: []
    }
  },
  computed: {
    /** 注册状态栏样式类 */
    registryStatusClass() {
      if (!this.registryData) return ''
      return this.registryData.failedCount > 0 ? 'registry-bar-warn' : 'registry-bar-ok'
    },
    /** 注册状态栏图标 */
    registryStatusIcon() {
      if (!this.registryData) return ''
      return this.registryData.failedCount > 0 ? 'el-icon-warning-outline' : 'el-icon-circle-check'
    }
  },
  created() {
    this.getList()
    this.fetchRegistryStatus()
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
    },

    // ==================== Phase 4.5 新增方法 ====================

    /** 获取 ToolRegistry 注册状态 */
    fetchRegistryStatus() {
      this.registryLoading = true
      registryStatus().then(response => {
        this.registryData = response.data || null
        this.registryLoading = false
      }).catch(() => { this.registryLoading = false })
    },

    /** 根据注册状态返回卡片徽章 */
    getRegistryBadge(item) {
      // 在列表模式下没有注册信息时，显示基础状态
      if (!this.registryData || !this.registryData.tools) {
        if (item.status === '1') {
          return { text: '已停用', statusType: 'info', effect: 'plain' }
        }
        return { text: '已启用', statusType: 'success', effect: 'plain' }
      }
      const found = this.registryData.tools.find(t => t.toolCode === item.toolCode)
      if (!found) {
        return item.status === '1'
          ? { text: '已停用', statusType: 'info', effect: 'plain' }
          : { text: '已启用', statusType: 'success', effect: 'plain' }
      }
      switch (found.registryStatus) {
        case 'REGISTERED':
          return { text: '已注册', statusType: 'success', effect: 'plain' }
        case 'CLASS_MISSING':
          return { text: '类缺失', statusType: 'danger', effect: 'dark' }
        case 'DISABLED':
          return { text: '已停用', statusType: 'info', effect: 'plain' }
        default:
          return { text: '已启用', statusType: 'success', effect: 'plain' }
      }
    },

    /** 展开/折叠卡片上的 Schema 参数 */
    toggleSchemaParams(item) {
      this.$set(item, '_schemaExpanded', !item._schemaExpanded)
    },

    /** 解析 Schema JSON → 参数列表 */
    parseSchemaParams(schemaJson) {
      try {
        const schema = JSON.parse(schemaJson)
        // 兼容两种格式: 完整格式和纯parameters格式
        const params = schema.parameters || schema
        if (!params || !params.properties) return []
        const required = new Set(params.required || [])
        return Object.entries(params.properties).map(([name, def]) => ({
          name,
          type: def.type || 'string',
          required: required.has(name),
          description: def.description || ''
        }))
      } catch (e) {
        return []
      }
    },

    /** 获取 Schema 参数数量 */
    parseSchemaParamCount(schemaJson) {
      return this.parseSchemaParams(schemaJson).length
    },

    /** 复制工具代码到剪贴板 */
    copyToolCode(item) {
      const text = [
        `// 工具代码: ${item.toolCode}`,
        `// 工具名称: ${item.toolName}`,
        `// 处理器类: ${item.handlerClass}`,
        `// Schema: ${item.functionSchema || '无'}`
      ].join('\n')
      navigator.clipboard.writeText(text).then(() => {
        this.$modal.msgSuccess('已复制工具信息到剪贴板')
      }).catch(() => {
        // 降级方案: 使用 textarea
        const ta = document.createElement('textarea')
        ta.value = text
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$modal.msgSuccess('已复制工具信息到剪贴板')
      })
    },

    /** 格式化 Schema JSON */
    formatSchema() {
      const raw = this.form.functionSchema
      if (!raw) return this.$modal.msgWarning('请先输入 Schema 内容')
      try {
        const obj = JSON.parse(raw)
        this.form.functionSchema = JSON.stringify(obj, null, 2)
        this.$modal.msgSuccess('已格式化 JSON')
        // 重新校验
        this.$nextTick(() => {
          if (this.$refs.toolForm) this.$refs.toolForm.validateField('functionSchema')
        })
      } catch (e) {
        this.$modal.msgError('JSON 格式不正确: ' + e.message)
      }
    },

    /** 预览 Schema 参数表格 */
    previewSchema() {
      const raw = this.form.functionSchema
      if (!raw) return this.$modal.msgWarning('请先输入 Schema 内容')
      try {
        const obj = JSON.parse(raw)
        this.schemaPreview = JSON.stringify(obj, null, 2)
        this.schemaParamRows = this.parseSchemaParams(raw)
        this.schemaDialogVisible = true
      } catch (e) {
        this.$modal.msgError('JSON 格式不正确: ' + e.message)
      }
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

/* ==================== Phase 4.5 新增样式 ==================== */

/* 注册状态栏 */
.registry-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  transition: background .3s;
}
.registry-bar-ok { background: #f0f9eb; border: 1px solid #e1f3d8; }
.registry-bar-warn { background: #fef0f0; border: 1px solid #fde2e2; }

.registry-summary { display: flex; align-items: center; gap: 8px; }
.registry-summary i { font-size: 18px; }
.registry-bar-ok .registry-summary i { color: #67c23a; }
.registry-bar-warn .registry-summary i { color: #f56c6c; }

.registry-warn { color: #f56c6c; font-weight: 500; }

/* Schema 参数切换 */
.card-schema-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  user-select: none;
  padding: 4px 0;
  transition: color .2s;
}
.card-schema-toggle:hover { color: #337ecc; }

/* 微型参数表 */
.card-schema-params { margin: 6px 0 4px; }
.param-mini-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  background: #fafbfc;
  border-radius: 4px;
  overflow: hidden;
}
.param-mini-table th {
  background: #f0f2f5;
  color: #909399;
  font-weight: 500;
  padding: 4px 8px;
  text-align: left;
}
.param-mini-table td {
  padding: 3px 8px;
  border-top: 1px solid #ebeef5;
  color: #606266;
  vertical-align: middle;
}
.param-mini-table code {
  background: #ecf5ff;
  color: #409eff;
  padding: 1px 5px;
  border-radius: 2px;
  font-size: 11px;
  font-family: "JetBrains Mono","Consolas",monospace;
}
.param-type {
  font-family: "JetBrains Mono","Consolas",monospace;
  font-size: 11px;
  color: #909399;
  background: #f5f7fa;
  padding: 1px 5px;
  border-radius: 2px;
}
.param-desc {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  color: #909399;
}

/* Schema 输入增强 */
.schema-input-wrapper .schema-actions {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}
.schema-input-wrapper .el-button--text { font-size: 12px; }

/* 参数预览表格 */
.schema-param-table { margin-bottom: 4px; }
.schema-param-table h4 { font-weight: 600; }
.schema-param-table code {
  background: #ecf5ff;
  color: #409eff;
  padding: 1px 5px;
  border-radius: 2px;
  font-size: 12px;
}
</style>

<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" class="search-bar">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="提示词类型" prop="templateType">
        <el-select v-model="queryParams.templateType" placeholder="提示词类型" clearable size="small">
          <el-option v-for="dict in typeOptions" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="模板状态" clearable size="small">
          <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['ai:prompt:add']">新增模板</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 卡片列表 -->
    <div class="prompt-card-container">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in promptList" :key="item.templateId" style="margin-bottom: 20px;">
          <div class="prompt-card" :class="{ 'system-card': item.templateType === '0', 'user-card': item.templateType === '1', 'disabled-card': item.status === '1' }">
            <div class="card-header">
              <div class="card-title">
                <i v-if="item.templateType === '0'" class="el-icon-setting" title="系统提示词"></i>
                <i v-else class="el-icon-user" title="用户提示词"></i>
                <span class="title-text">{{ item.templateName }}</span>
              </div>
              <div class="card-tags">
                <el-tag v-if="item.templateType === '0'" size="small" type="primary">系统提示词</el-tag>
                <el-tag v-else size="small" type="success">用户提示词</el-tag>
                <el-tag v-if="item.isBuiltin === '1'" size="small" type="warning">内置</el-tag>
              </div>
            </div>
            <div class="card-body">
              <div class="info-item" v-if="item.sceneName">
                <label>场景：</label>
                <span>{{ item.sceneName }}</span>
              </div>
              <div class="info-item" v-if="item.sceneCode">
                <label>场景代码：</label>
                <span class="code-text">{{ item.sceneCode }}</span>
              </div>
              <div class="preview-box">
                <label>内容预览：</label>
                <p class="preview-text">{{ (item.promptContent || '').substring(0, 120) }}{{ (item.promptContent || '').length > 120 ? '...' : '' }}</p>
              </div>
              <div class="info-item" v-if="item.variables">
                <label>变量：</label>
                <span class="var-tag" v-for="v in parseVariables(item.variables)" :key="v">{{ v }}</span>
              </div>
              <div class="info-item">
                <label>状态：</label>
                <el-switch v-model="item.status" active-value="0" inactive-value="1"
                  @change="handleStatusChange(item)"
                  v-hasPermi="['ai:prompt:edit']"
                  :disabled="item.isBuiltin === '1'"></el-switch>
              </div>
            </div>
            <div class="card-footer">
              <el-button type="text" size="small" icon="el-icon-magic-stick" @click="handleGenerate(item)" v-hasPermi="['ai:prompt:generate']"
                v-if="item.templateType !== undefined">AI生成</el-button>
              <el-button type="text" size="small" icon="el-icon-edit" @click="handleUpdate(item)"
                v-hasPermi="['ai:prompt:edit']"
                :disabled="item.isBuiltin === '1'">编辑</el-button>
              <el-button type="text" size="small" icon="el-icon-delete" @click="handleDelete(item)"
                v-hasPermi="['ai:prompt:remove']"
                :disabled="item.isBuiltin === '1'" style="color: #F56C6C;">删除</el-button>
            </div>
          </div>
        </el-col>
        <!-- 添加卡片 -->
        <el-col :span="6" style="margin-bottom: 20px;" v-hasPermi="['ai:prompt:add']">
          <div class="prompt-card add-card" @click="handleAdd">
            <div class="add-content">
              <i class="el-icon-plus add-icon"></i>
              <p>添加新模板</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="form.templateName" placeholder="请输入模板名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="提示词类型" prop="templateType">
          <el-radio-group v-model="form.templateType" @change="onTypeChange">
            <el-radio-button v-for="dict in typeOptions" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="场景代码" prop="sceneCode">
          <el-select v-model="form.sceneCode" placeholder="请选择场景" clearable allow-create filterable
            @change="onSceneChange">
            <el-option v-for="s in sceneOptions" :key="s.value" :label="s.label + ' (' + s.value + ')'" :value="s.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="场景名称" prop="sceneName">
          <el-input v-model="form.sceneName" placeholder="请输入场景名称"/>
        </el-form-item>
        <el-form-item label="提示词内容" prop="promptContent">
          <el-input v-model="form.promptContent" type="textarea" :rows="12" :placeholder="'请输入提示词内容，支持 {{变量名}} 占位符'"
            @input="extractVariables"/>
        </el-form-item>
        <el-form-item label="变量列表">
          <el-tag v-for="(v, idx) in currentVariables" :key="idx" closable @close="removeVariable(idx)" type="info" style="margin-right:5px;">
            {{ formatBrace(v) }}
          </el-tag>
          <span v-if="currentVariables.length === 0" style="color:#999;">提示词中检测到的变量会自动显示在这里</span>
        </el-form-item>
        <el-form-item label="关联系统提示词" v-if="form.templateType === '1'">
          <el-select v-model="form.systemPromptId" placeholder="请选择关联的系统提示词" clearable filterable>
            <el-option v-for="s in systemPromptList" :key="s.templateId" :label="s.templateName" :value="s.templateId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in statusOptions" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示顺序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
      </div>
    </el-dialog>

    <!-- AI生成/润色对话框 -->
    <el-dialog :title="aiDialogTitle" :visible.sync="aiDialogVisible" width="800px" append-to-body :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="操作模式">
          <el-radio-group v-model="aiMode">
            <el-radio-button label="generate">全新生成</el-radio-button>
            <el-radio-button label="polish">润色优化</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="aiMode === 'generate' ? '需求描述' : '待润色提示词'">
          <el-input v-model="aiInputContent" type="textarea" :rows="8"
            :placeholder="aiMode === 'generate' ? '描述你想要的提示词功能和要求，AI将为你生成专业的提示词...' : '粘贴需要润色的提示词内容，AI将帮你优化使其更专业...'"/>
        </el-form-item>
        <el-form-item v-if="aiResult">
          <div class="ai-result-box">
            <label>生成结果：</label>
            <div class="result-toolbar">
              <el-button size="mini" icon="el-icon-document-copy" @click="copyAiResult">复制</el-button>
              <el-button size="mini" icon="el-icon-check" type="primary" @click="applyAiResult">采纳到编辑器</el-button>
            </div>
            <pre class="ai-result-content">{{ aiResult }}</pre>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="aiDialogVisible = false">关 闭</el-button>
        <el-button type="primary" @click="executeAiGenerate" :loading="aiGenerating">
          <i class="el-icon-magic-stick"></i> {{ aiMode === 'generate' ? '开始生成' : '开始润色' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPrompt, getPrompt, addPrompt, updatePrompt, delPrompt, listSystemPrompts, generatePrompt } from "@/api/ai/promptTemplate"

export default {
  name: "AiPrompt",
  data() {
    return {
      showSearch: true,
      dialogVisible: false,
      dialogTitle: "",
      aiDialogVisible: false,
      aiDialogTitle: "AI生成提示词",
      aiGenerating: false,
      submitting: false,
      total: 0,
      promptList: [],
      systemPromptList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        templateName: undefined,
        templateType: undefined,
        status: undefined
      },
      form: {},
      currentVariables: [],
      aiMode: 'generate',
      aiInputContent: '',
      aiResult: '',
      aiType: '0',
      typeOptions: [
        { label: '系统提示词', value: '0' },
        { label: '用户提示词', value: '1' }
      ],
      statusOptions: [
        { label: '启用', value: '0' },
        { label: '停用', value: '1' }
      ],
      sceneOptions: [
        { label: 'AI内容审核', value: 'content_audit' },
        { label: 'AI智能分类', value: 'content_classify' },
        { label: 'AI智能推荐', value: 'content_recommend' },
        { label: 'AI评论管理', value: 'comment_moderate' },
        { label: '校园智能助手', value: 'campus_chat' },
        { label: '举报分析', value: 'user_report' },
        { label: '自定义场景', value: 'custom' }
      ],
      rules: {
        templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
        templateType: [{ required: true, message: '提示词类型不能为空', trigger: 'change' }],
        promptContent: [{ required: true, message: '提示词内容不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList();
    this.loadSystemPrompts();
  },
  methods: {
    getList() {
      listPrompt(this.queryParams).then(response => {
        this.promptList = response.rows;
        this.total = response.total;
      });
    },
    loadSystemPrompts() {
      listSystemPrompts().then(response => {
        this.systemPromptList = response.data || [];
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.reset();
      this.dialogTitle = "添加提示词模板";
      this.dialogVisible = true;
    },
    handleUpdate(row) {
      this.reset();
      getPrompt(row.templateId).then(response => {
        this.form = response.data;
        this.extractVariables();
        this.dialogTitle = "修改提示词模板";
        this.dialogVisible = true;
      });
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除模板【' + row.templateName + '】？').then(() => {
        return delPrompt(row.templateId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$modal.confirm('确认"' + text + '"模板【' + row.templateName + '】？').then(() => {
        return updatePrompt({ templateId: row.templateId, status: row.status });
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    onTypeChange(val) {
      if (val === '1') {
        this.loadSystemPrompts();
      }
    },
    onSceneChange(val) {
      const found = this.sceneOptions.find(s => s.value === val);
      if (found) {
        this.form.sceneName = found.label.replace(/\s\(.+\)$/, '');
      }
    },
    extractVariables() {
      const content = this.form.promptContent || '';
      const matches = content.match(/\{\{(\w+)\}\}/g);
      if (matches) {
        this.currentVariables = matches.map(m => m.replace(/[{}]/g, ''));
      } else {
        this.currentVariables = [];
      }
    },
    removeVariable(idx) {
      const varName = this.currentVariables[idx];
      this.form.promptContent = this.form.promptContent.replace(new RegExp('\\{\\{' + varName + '\\}\\}', 'g'), '');
      this.currentVariables.splice(idx, 1);
    },
    formatBrace(v) {
      return '{{' + v + '}}';
    },
    parseVariables(str) {
      try {
        return JSON.parse(str);
      } catch (e) {
        return [];
      }
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitting = true;
          // 更新变量列表
          if (this.currentVariables.length > 0) {
            this.form.variables = JSON.stringify(this.currentVariables);
          }
          const action = this.form.templateId ? updatePrompt(this.form) : addPrompt(this.form);
          action.then(() => {
            this.$modal.msgSuccess(this.form.templateId ? "修改成功" : "新增成功");
            this.dialogVisible = false;
            this.getList();
          }).finally(() => {
            this.submitting = false;
          });
        }
      });
    },
    handleGenerate(item) {
      this.aiResult = '';
      this.aiInputContent = '';
      this.aiMode = 'generate';
      this.aiType = item.templateType || '0';
      this.aiDialogTitle = 'AI生成提示词 - ' + (item.templateType === '0' ? '系统提示词' : '用户提示词');
      this.aiDialogVisible = true;
    },
    executeAiGenerate() {
      if (!this.aiInputContent.trim()) {
        this.$modal.msgWarning(this.aiMode === 'generate' ? '请先输入需求描述' : '请先粘贴需要润色的提示词');
        return;
      }
      this.aiGenerating = true;
      const data = {
        promptContent: this.aiInputContent,
        templateType: this.aiType,
        remark: this.aiMode
      };
      generatePrompt(data).then(response => {
        this.aiResult = response.msg || response.data || '';
        this.$modal.msgSuccess(this.aiMode === 'generate' ? '生成成功' : '润色成功');
      }).catch(() => {
        this.$modal.msgError('AI生成失败，请检查LLM配置是否正确');
      }).finally(() => {
        this.aiGenerating = false;
      });
    },
    copyAiResult() {
      navigator.clipboard.writeText(this.aiResult).then(() => {
        this.$modal.msgSuccess('已复制到剪贴板');
      }).catch(() => {
        const textArea = document.createElement('textarea');
        textArea.value = this.aiResult;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        this.$modal.msgSuccess('已复制到剪贴板');
      });
    },
    applyAiResult() {
      this.form.promptContent = this.aiResult;
      this.extractVariables();
      this.aiDialogVisible = false;
      this.$modal.msgSuccess('已采纳到编辑器');
    },
    reset() {
      this.form = {
        templateId: undefined,
        templateName: undefined,
        templateType: '0',
        sceneCode: undefined,
        sceneName: undefined,
        promptContent: undefined,
        variables: undefined,
        systemPromptId: undefined,
        status: '0',
        isBuiltin: '0',
        sort: 0,
        remark: undefined
      };
      this.currentVariables = [];
      this.resetForm("form");
    }
  }
}
</script>

<style scoped>
.prompt-card-container {
  min-height: 200px;
}
.prompt-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 18px 20px;
  height: 320px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  cursor: default;
  border-left: 4px solid transparent;
}
.prompt-card.system-card {
  border-left-color: #409EFF;
}
.prompt-card.user-card {
  border-left-color: #67C23A;
}
.prompt-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}
.prompt-card.disabled-card {
  opacity: 0.7;
  background: #f5f7fa;
}
.add-card {
  border: 2px dashed #dcdfe6;
  border-left: 2px dashed #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fafafa;
}
.add-card:hover {
  border-color: #409EFF;
  background: #f0f7ff;
}
.add-content {
  text-align: center;
  color: #909399;
}
.add-content:hover {
  color: #409EFF;
}
.add-icon {
  font-size: 40px;
  margin-bottom: 8px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}
.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
  flex: 1;
  overflow: hidden;
}
.card-title i {
  font-size: 16px;
  color: #909399;
  flex-shrink: 0;
}
.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-tags {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 8px;
}
.card-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.info-item {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}
.info-item label {
  color: #909399;
  flex-shrink: 0;
}
.code-text {
  font-family: monospace;
  background: #f0f2f5;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 12px;
}
.preview-box {
  flex: 1;
  overflow: hidden;
  margin-top: 4px;
}
.preview-box label {
  font-size: 13px;
  color: #909399;
}
.preview-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  margin: 4px 0 0 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 80px;
  overflow: hidden;
}
.var-tag {
  display: inline-block;
  background: #ecf5ff;
  color: #409EFF;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 12px;
  margin-right: 4px;
  font-family: monospace;
}
.card-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;
}
.ai-result-box {
  width: 100%;
}
.ai-result-box label {
  font-weight: 600;
  color: #303133;
  display: block;
  margin-bottom: 8px;
}
.result-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.ai-result-content {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 400px;
  overflow-y: auto;
  color: #303133;
  font-family: 'Microsoft YaHei', sans-serif;
}
.search-bar {
  background: #fff;
  padding: 16px 16px 4px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
</style>

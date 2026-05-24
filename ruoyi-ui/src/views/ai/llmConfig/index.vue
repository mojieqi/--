<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置名称" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置名称"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务商" prop="provider">
        <el-select v-model="queryParams.provider" placeholder="请选择服务商" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['ai:llm:add']"
        >新增配置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['ai:llm:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 配置卡片列表 -->
    <div class="config-card-container" v-loading="loading">
      <el-row :gutter="20">
        <el-col
          v-for="(item, index) in configList"
          :key="index"
          :xs="24"
          :sm="12"
          :md="12"
          :lg="8"
          :xl="6"
        >
          <div class="config-card" :class="{ 'is-disabled': item.status === '1', 'is-default': item.isDefault === '1' }">
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="provider-info">
                <span class="provider-icon">{{ getProviderIcon(item.provider) }}</span>
                <span class="provider-name">{{ item.providerName }}</span>
              </div>
              <div class="card-tags">
                <el-tag v-if="item.isDefault === '1'" type="success" size="mini" effect="dark">默认</el-tag>
                <el-tag :type="item.status === '0' ? 'success' : 'info'" size="mini">
                  {{ item.status === '0' ? '启用' : '停用' }}
                </el-tag>
              </div>
            </div>

            <!-- 卡片内容 -->
            <div class="card-body">
              <div class="config-name">{{ item.configName }}</div>
              <div class="config-item">
                <span class="label">BaseURL：</span>
                <span class="value" :title="item.baseUrl">{{ truncateText(item.baseUrl, 30) }}</span>
              </div>
              <div class="config-item">
                <span class="label">API Key：</span>
                <span class="value">{{ maskApiKey(item.apiKey) }}</span>
              </div>
              <div class="config-item">
                <span class="label">默认模型：</span>
                <span class="value model-value">{{ item.defaultModel || '未设置' }}</span>
              </div>
            </div>

            <!-- 卡片底部操作 -->
            <div class="card-footer">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-connection"
                @click="handleTest(item)"
                v-hasPermi="['ai:llm:list']"
                :disabled="!item.apiKey"
              >测试接入</el-button>
              <el-button
                v-if="item.isDefault !== '1'"
                type="success"
                size="mini"
                plain
                @click="handleSetDefault(item)"
                v-hasPermi="['ai:llm:edit']"
              >设为默认</el-button>
              <el-button
                type="text"
                size="mini"
                icon="el-icon-edit"
                @click="handleUpdate(item)"
                v-hasPermi="['ai:llm:edit']"
              >编辑</el-button>
              <el-button
                type="text"
                size="mini"
                icon="el-icon-delete"
                @click="handleDelete(item)"
                v-hasPermi="['ai:llm:remove']"
              >删除</el-button>
            </div>
          </div>
        </el-col>

        <!-- 添加卡片 -->
        <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="6">
          <div class="add-card" @click="handleAdd">
            <i class="el-icon-plus"></i>
            <span>添加新配置</span>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="configList.length === 0 && !loading" description="暂无配置，点击上方添加新配置">
        <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleAdd">添加配置</el-button>
      </el-empty>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称，如：通义千问生产环境" />
        </el-form-item>
        <el-form-item label="服务商" prop="provider">
          <el-select v-model="form.provider" placeholder="请选择服务商" @change="handleProviderChange">
            <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="BaseURL" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="请输入API BaseURL" />
          <div class="form-tip">填写后点击"测试接入"可自动获取可用模型</div>
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" placeholder="请输入API密钥" show-password />
        </el-form-item>
        <el-form-item label="默认模型" prop="defaultModel">
          <el-input v-model="form.defaultModel" placeholder="请输入默认模型名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="isDefaultSwitch" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 模型选择对话框 -->
    <el-dialog title="测试接入 - 选择模型" :visible.sync="modelDialogVisible" width="500px" append-to-body>
      <div class="model-loading" v-if="modelLoading">
        <i class="el-icon-loading"></i>
        <span>正在获取可用模型...</span>
      </div>
      <div class="model-list" v-else>
        <el-radio-group v-model="selectedModel">
          <el-radio
            v-for="model in modelList"
            :key="model"
            :label="model"
            class="model-radio"
          >
            {{ model }}
          </el-radio>
        </el-radio-group>
        <el-empty v-if="modelList.length === 0" description="未获取到可用模型，请检查配置">
          <div class="model-tip">
            <p>可能的原因：</p>
            <ul>
              <li>API Key 不正确或已过期</li>
              <li>BaseURL 配置不正确</li>
              <li>网络连接失败</li>
            </ul>
          </div>
        </el-empty>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="modelDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmModel" :disabled="!selectedModel">确认选择</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, getConfig, addConfig, updateConfig, delConfig, setDefault, testConnection } from "@/api/ai/llmConfig"

export default {
  name: "AiLlmConfig",
  data() {
    return {
      loading: true,
      showSearch: true,
      configList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 100,
        configName: undefined,
        provider: undefined,
        status: undefined
      },
      dialogTitle: "",
      dialogVisible: false,
      dialogType: "add",
      form: {},
      isDefaultSwitch: "0",
      rules: {
        configName: [
          { required: true, message: "配置名称不能为空", trigger: "blur" }
        ],
        provider: [
          { required: true, message: "请选择服务商", trigger: "change" }
        ],
        apiKey: [
          { required: true, message: "API Key不能为空", trigger: "blur" }
        ]
      },
      modelDialogVisible: false,
      modelLoading: false,
      modelList: [],
      selectedModel: "",
      currentConfigId: null,
      providerOptions: [
        { label: "通义千问", value: "qwen" },
        { label: "Deepseek", value: "deepseek" },
        { label: "OpenAI", value: "openai" },
        { label: "Anthropic", value: "anthropic" },
        { label: "Google", value: "google" },
        { label: "豆包", value: "doubao" },
        { label: "智谱AI", value: "zhipu" },
        { label: "OpenRouter", value: "openrouter" }
      ],
      providerBaseUrls: {
        qwen: "https://dashscope.aliyuncs.com/compatible-mode/v1",
        deepseek: "https://api.deepseek.com",
        openai: "https://api.openai.com/v1",
        anthropic: "https://api.anthropic.com",
        google: "https://generativelanguage.googleapis.com/v1beta",
        doubao: "https://ark.cn-beijing.volces.com/api/v3",
        zhipu: "https://open.bigmodel.cn/api/paas/v4",
        openrouter: "https://openrouter.ai/api/v1"
      },
      providerIcons: {
        qwen: "通",
        deepseek: "Deep",
        openai: "AI",
        anthropic: "An",
        google: "G",
        doubao: "豆",
        zhipu: "智",
        openrouter: "OR"
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listConfig(this.queryParams).then(response => {
        this.configList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.dialogTitle = "添加LLM配置"
      this.dialogType = "add"
      this.dialogVisible = true
    },
    handleUpdate(row) {
      this.loading = true
      getConfig(row.configId).then(response => {
        this.form = response.data
        this.isDefaultSwitch = this.form.isDefault || "0"
        this.dialogTitle = "编辑LLM配置"
        this.dialogType = "edit"
        this.dialogVisible = true
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSetDefault(row) {
      this.$confirm('是否将 "' + row.configName + '" 设为默认配置?', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        setDefault(row.configId).then(() => {
          this.$modal.msgSuccess("设置成功")
          this.getList()
        })
      }).catch(() => {})
    },
    handleTest(row) {
      this.currentConfigId = row.configId
      this.modelList = []
      this.selectedModel = ""
      this.modelDialogVisible = true
      this.modelLoading = true

      // 如果没有保存过API Key，先更新配置
      if (!row.apiKey && this.form.apiKey) {
        this.$message.warning("请先保存配置后再测试接入")
        return
      }

      testConnection(row.configId).then(response => {
        this.modelLoading = false
        if (response.data && response.data.length > 0) {
          this.modelList = response.data
        } else {
          this.modelList = []
        }
      }).catch(error => {
        this.modelLoading = false
        this.modelList = []
        this.$message.error(error.msg || "获取模型列表失败")
      })
    },
    confirmModel() {
      if (!this.selectedModel) {
        this.$message.warning("请选择模型")
        return
      }

      // 更新配置的默认模型
      const config = {
        configId: this.currentConfigId,
        defaultModel: this.selectedModel
      }

      // 获取当前配置并更新
      getConfig(this.currentConfigId).then(response => {
        const currentConfig = response.data
        currentConfig.defaultModel = this.selectedModel
        updateConfig(currentConfig).then(() => {
          this.$modal.msgSuccess("默认模型设置成功")
          this.modelDialogVisible = false
          this.getList()
        })
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.isDefault = this.isDefaultSwitch
          if (this.dialogType === "add") {
            addConfig(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.dialogVisible = false
              this.getList()
            })
          } else {
            updateConfig(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.dialogVisible = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const configIds = row.configId
      this.$confirm('是否确认删除配置名称为"' + row.configName + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return delConfig(configIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('/ai/llm/config/export', {
        ...this.queryParams
      }, `llm_config_${new Date().getTime()}.xlsx`)
    },
    handleProviderChange(value) {
      const provider = this.providerOptions.find(p => p.value === value)
      if (provider) {
        this.form.providerName = provider.label
        // 自动填充 BaseURL
        if (this.providerBaseUrls[value] && !this.form.baseUrl) {
          this.form.baseUrl = this.providerBaseUrls[value]
        }
      }
    },
    reset() {
      this.form = {
        configId: undefined,
        configName: undefined,
        provider: undefined,
        providerName: undefined,
        baseUrl: undefined,
        apiKey: undefined,
        defaultModel: undefined,
        status: "0",
        isDefault: "0",
        sort: 0,
        remark: undefined
      }
      this.isDefaultSwitch = "0"
      this.resetForm("form")
    },
    getProviderIcon(provider) {
      return this.providerIcons[provider] || provider?.substring(0, 2).toUpperCase() || "?"
    },
    maskApiKey(apiKey) {
      if (!apiKey) return "未设置"
      if (apiKey.length <= 8) return "••••••••"
      return apiKey.substring(0, 4) + "••••••••" + apiKey.substring(apiKey.length - 4)
    },
    truncateText(text, maxLength) {
      if (!text) return ""
      return text.length > maxLength ? text.substring(0, maxLength) + "..." : text
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mb8 {
  margin-bottom: 16px;
}

/* 配置卡片容器 */
.config-card-container {
  min-height: 300px;
}

/* 配置卡片 */
.config-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.config-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #409EFF;
}

.config-card.is-default::before {
  background: #67C23A;
}

.config-card.is-disabled {
  opacity: 0.6;
  background: #fafafa;
}

.config-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.provider-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

.provider-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-tags {
  display: flex;
  gap: 8px;
}

/* 卡片内容 */
.card-body {
  margin-bottom: 16px;
}

.config-name {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.config-item {
  display: flex;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.config-item .label {
  color: #909399;
  flex-shrink: 0;
}

.config-item .value {
  color: #606266;
  word-break: break-all;
}

.config-item .model-value {
  color: #409EFF;
  font-weight: 500;
}

/* 卡片底部操作 */
.card-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-footer .el-button {
  padding: 6px 12px;
}

/* 添加卡片 */
.add-card {
  background: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 200px;
}

.add-card:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.add-card i {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 12px;
}

.add-card span {
  font-size: 14px;
  color: #606266;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 模型列表 */
.model-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #606266;
}

.model-loading i {
  font-size: 24px;
  margin-right: 12px;
}

.model-list {
  max-height: 400px;
  overflow-y: auto;
}

.model-radio {
  display: block;
  padding: 12px 16px;
  margin: 8px 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  width: 100%;
}

.model-radio:hover {
  border-color: #409EFF;
  background: #f0f7ff;
}

.model-tip {
  text-align: left;
  font-size: 13px;
  color: #909399;
}

.model-tip p {
  margin-bottom: 8px;
}

.model-tip ul {
  margin: 0;
  padding-left: 20px;
}

.model-tip li {
  margin-bottom: 4px;
}
</style>

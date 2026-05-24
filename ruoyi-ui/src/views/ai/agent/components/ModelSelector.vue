<template>
  <div class="model-selector">
    <div class="selector-item">
      <label>LLM 模型</label>
      <el-select v-model="selectedConfig" placeholder="选择LLM配置" size="small" @change="handleChange">
        <el-option
          v-for="item in configs"
          :key="item.configId"
          :label="item.configName + ' (' + (item.defaultModel || 'default') + ')'"
          :value="item.configId"
        />
      </el-select>
    </div>
    <div class="selector-item">
      <label>提示词模板</label>
      <el-select v-model="selectedPrompt" placeholder="选择提示词模板" size="small" clearable @change="handleChange">
        <el-option
          v-for="item in prompts"
          :key="item.templateId"
          :label="item.templateName"
          :value="item.templateId"
        />
      </el-select>
    </div>
    <div class="selector-item">
      <label>知识库 (RAG)</label>
      <el-select v-model="selectedKb" placeholder="选择知识库" size="small" clearable @change="handleChange">
        <el-option
          v-for="item in knowledgeBases"
          :key="item.kbId"
          :label="item.kbName"
          :value="item.kbId"
        />
      </el-select>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ModelSelector',
  props: {
    configs: { type: Array, default: () => [] },
    prompts: { type: Array, default: () => [] },
    knowledgeBases: { type: Array, default: () => [] },
    llmConfigId: { type: Number, default: null },
    promptTemplateId: { type: Number, default: null },
    kbId: { type: Number, default: null }
  },
  data() {
    return {
      selectedConfig: this.llmConfigId,
      selectedPrompt: this.promptTemplateId,
      selectedKb: this.kbId
    }
  },
  watch: {
    llmConfigId(v) { this.selectedConfig = v },
    promptTemplateId(v) { this.selectedPrompt = v },
    kbId(v) { this.selectedKb = v }
  },
  methods: {
    handleChange() {
      this.$emit('change', {
        llmConfigId: this.selectedConfig,
        promptTemplateId: this.selectedPrompt,
        kbId: this.selectedKb
      })
    }
  }
}
</script>

<style scoped>
.model-selector {
  padding: 4px 0;
}
.selector-item {
  margin-bottom: 14px;
}
.selector-item label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}
.selector-item .el-select {
  width: 100%;
}
</style>

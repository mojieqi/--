<template>
  <div class="agent-settings">
    <div class="settings-title">Agent 设置</div>

    <ModelSelector
      :configs="configs"
      :prompts="prompts"
      :knowledgeBases="knowledgeBases"
      :llmConfigId="conversation.llmConfigId"
      :promptTemplateId="conversation.promptTemplateId"
      :kbId="conversation.kbId"
      @change="$emit('update-settings', $event)"
    />

    <el-divider></el-divider>

    <div class="setting-row">
      <label>记忆模式</label>
      <el-radio-group v-model="memoryMode" size="small" @change="handleMemoryChange">
        <el-radio label="0">无记忆</el-radio>
        <el-radio label="1">短期记忆</el-radio>
      </el-radio-group>
    </div>

    <div class="setting-row">
      <label>最大Token数</label>
      <el-input-number v-model="maxTokens" :min="512" :max="32768" :step="512" size="small" @change="handleMaxTokensChange"></el-input-number>
    </div>

    <el-divider></el-divider>

    <div class="setting-actions">
      <el-button size="small" type="warning" icon="el-icon-delete" @click="$emit('clear-context')" style="width:100%">
        清空对话上下文
      </el-button>
    </div>
  </div>
</template>

<script>
import ModelSelector from './ModelSelector.vue'

export default {
  name: 'AgentSettings',
  components: { ModelSelector },
  props: {
    conversation: { type: Object, default: () => ({}) },
    configs: { type: Array, default: () => [] },
    prompts: { type: Array, default: () => [] },
    knowledgeBases: { type: Array, default: () => [] }
  },
  data() {
    return {
      memoryMode: this.conversation.memoryMode || '1',
      maxTokens: this.conversation.maxTokens || 4096
    }
  },
  watch: {
    'conversation.memoryMode'(v) { this.memoryMode = v || '1' },
    'conversation.maxTokens'(v) { this.maxTokens = v || 4096 }
  },
  methods: {
    handleMemoryChange(v) {
      this.$emit('update-settings', { memoryMode: v })
    },
    handleMaxTokensChange(v) {
      this.$emit('update-settings', { maxTokens: v })
    }
  }
}
</script>

<style scoped>
.agent-settings {
  width: 260px;
  height: 100%;
  background: #fafafa;
  border-left: 1px solid #e8e8e8;
  padding: 16px;
  overflow-y: auto;
}
.settings-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.setting-row {
  margin-bottom: 14px;
}
.setting-row label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}
.setting-actions {
  margin-top: 8px;
}
</style>

<template>
  <div class="conversation-sidebar">
    <div class="sidebar-header">
      <el-button type="primary" size="small" icon="el-icon-plus" @click="$emit('new-conversation')" style="width:100%">
        新对话
      </el-button>
    </div>
    <div class="sidebar-list" v-loading="loading">
      <div
        v-for="item in conversations"
        :key="item.conversationId"
        :class="['conv-item', { active: item.conversationId === activeId }]"
        @click="$emit('select', item)"
      >
        <div class="conv-title">{{ item.title || '新对话' }}</div>
        <div class="conv-actions">
          <span class="conv-count">{{ item.messageCount || 0 }}条</span>
          <el-popconfirm
            title="确定删除该会话吗？"
            @confirm="$emit('delete', item.conversationId)"
          >
            <i slot="reference" class="el-icon-delete delete-btn" @click.stop></i>
          </el-popconfirm>
        </div>
      </div>
      <el-empty v-if="!loading && conversations.length === 0" description="暂无对话" :image-size="60"></el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConversationSidebar',
  props: {
    conversations: { type: Array, default: () => [] },
    activeId: { type: Number, default: null },
    loading: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.conversation-sidebar {
  width: 240px;
  height: 100%;
  background: #fafafa;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 12px;
}
.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;
}
.conv-item {
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.conv-item:hover { background: #e8f4fd; }
.conv-item.active { background: #d0e8ff; }
.conv-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}
.conv-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #999;
}
.delete-btn {
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  color: #f56c6c;
}
.conv-item:hover .delete-btn { opacity: 1; }
</style>

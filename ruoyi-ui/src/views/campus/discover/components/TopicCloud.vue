<template>
  <div class="topic-cloud">
    <div class="cloud-header">
      <h3><i class="el-icon-collection-tag"></i> 热门关键词</h3>
    </div>

    <div v-if="loading" class="cloud-loading">
      <i class="el-icon-loading"></i>
    </div>

    <div v-else-if="words.length === 0" class="cloud-empty">
      暂无数据
    </div>

    <div v-else class="cloud-wrap">
      <span
        v-for="(w, i) in words"
        :key="i"
        class="cloud-tag"
        :class="getClass(w)"
        @click="$emit('click', w.text)"
      >
        {{ w.text }}
      </span>
    </div>
  </div>
</template>

<script>
import { getKeywords } from '@/api/campus/recommend'

export default {
  name: 'TopicCloud',
  props: {
    limit: { type: Number, default: 20 }
  },
  data() {
    return {
      loading: false,
      words: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getKeywords({ limit: this.limit })
        this.words = res.data || []
      } catch (e) {
        console.error('加载关键词失败', e)
      } finally {
        this.loading = false
      }
    },
    getClass(word) {
      const v = word.value || 1
      if (v >= 10) return 'size-xl'
      if (v >= 5) return 'size-lg'
      if (v >= 3) return 'size-md'
      return 'size-sm'
    }
  }
}
</script>

<style scoped>
.topic-cloud {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.cloud-header h3 {
  margin: 0 0 12px;
  font-size: 15px; font-weight: 600; color: #303133;
}
.cloud-header h3 i { color: #409eff; margin-right: 4px; }

.cloud-loading, .cloud-empty {
  text-align: center; padding: 20px 0; color: #909399; font-size: 13px;
}

.cloud-wrap {
  display: flex; flex-wrap: wrap; gap: 8px;
}

.cloud-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: #ecf5ff; color: #409eff;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.cloud-tag:hover {
  background: #409eff; color: #fff;
}

.size-xl { font-size: 15px; font-weight: 700; }
.size-lg { font-size: 14px; font-weight: 600; }
.size-md { font-size: 13px; }
.size-sm { font-size: 12px; }
</style>

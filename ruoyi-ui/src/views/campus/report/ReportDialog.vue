<template>
  <el-dialog
    title="举报内容"
    :visible.sync="dialogVisible"
    width="480px"
    :close-on-click-modal="false"
    append-to-body
  >
    <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="举报原因" prop="reportReason">
        <el-radio-group v-model="form.reportReason">
          <el-radio
            v-for="reason in reasonOptions"
            :key="reason.value"
            :label="reason.value"
            class="report-reason-item"
          >
            {{ reason.label }}
            <span class="reason-desc">{{ reason.desc }}</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="详细描述" prop="reportDesc">
        <el-input
          v-model="form.reportDesc"
          type="textarea"
          :rows="3"
          placeholder="请补充详细描述(选填)"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交举报</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { addReport } from '@/api/campus/report'

export default {
  name: 'ReportDialog',
  props: {
    visible: { type: Boolean, default: false },
    targetType: { type: String, required: true },  // 0帖子 1评论
    targetId: { type: [Number, String], required: true }
  },
  data() {
    return {
      submitting: false,
      form: {
        reportReason: '',
        reportDesc: ''
      },
      reasonOptions: [
        { value: 'spam', label: '垃圾广告', desc: '商业推广、刷屏信息' },
        { value: 'harassment', label: '人身攻击', desc: '辱骂、侮辱、诽谤' },
        { value: 'porn', label: '色情低俗', desc: '色情、擦边内容' },
        { value: 'fake', label: '虚假信息', desc: '谣言、虚假求助' },
        { value: 'privacy', label: '侵犯隐私', desc: '泄露他人信息' },
        { value: 'other', label: '其他', desc: '其他违规行为' }
      ],
      rules: {
        reportReason: [
          { required: true, message: '请选择举报原因', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    dialogVisible: {
      get() { return this.visible },
      set(val) { this.$emit('update:visible', val) }
    }
  },
  watch: {
    visible(val) {
      if (val) this.resetForm()
    }
  },
  methods: {
    resetForm() {
      this.form = { reportReason: '', reportDesc: '' }
      this.submitting = false
      if (this.$refs.form) this.$refs.form.resetFields()
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
      } catch {
        return
      }
      this.submitting = true
      try {
        const res = await addReport({
          targetType: this.targetType,
          targetId: this.targetId,
          reportReason: this.form.reportReason,
          reportDesc: this.form.reportDesc
        })
        if (res.code === 200) {
          this.$message.success('举报提交成功，我们将尽快处理')
          this.$emit('success')
          this.dialogVisible = false
        } else {
          this.$message.warning(res.msg || '举报失败')
        }
      } catch (e) {
        this.$message.error('举报提交失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.report-reason-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.reason-desc {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}
</style>

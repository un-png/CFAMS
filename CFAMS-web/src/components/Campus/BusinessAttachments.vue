<template>
  <div v-loading="loading">
    <el-alert v-if="error" :title="error" type="warning" :closable="false">
      <el-button link type="primary" @click="getList">重新加载附件</el-button>
    </el-alert>
    <el-table :data="attachmentList" size="small">
      <el-table-column label="文件名称" prop="fileName" show-overflow-tooltip />
      <el-table-column label="大小（字节）" prop="fileSize" width="120" align="center" />
      <el-table-column label="操作" width="90" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { listAttachments, getAttachmentUrl } from '@/api/campus/attachment'

const props = defineProps({ bizType: { type: String, required: true }, bizId: [Number, String] })
const attachmentList = ref([])
const loading = ref(false)
const error = ref('')
let requestId = 0

async function getList() {
  const current = ++requestId
  attachmentList.value = []
  error.value = ''
  if (!props.bizId) { loading.value = false; return }
  loading.value = true
  try {
    const response = await listAttachments(props.bizType, props.bizId)
    if (current === requestId) attachmentList.value = response.data || []
  } catch (err) {
    if (current === requestId) error.value = err.message || '附件加载失败'
  } finally {
    if (current === requestId) loading.value = false
  }
}

async function handleView(row) {
  const preview = window.open('about:blank', '_blank')
  if (preview) preview.opener = null
  try {
    const response = await getAttachmentUrl(row.attachmentId)
    if (preview) preview.location = response.data
    else ElMessage.warning('请允许浏览器打开附件窗口')
  } catch {
    preview?.close()
  }
}

watch(() => [props.bizType, props.bizId], getList, { immediate: true })
onBeforeUnmount(() => { requestId++ })
defineExpose({ getList })
</script>

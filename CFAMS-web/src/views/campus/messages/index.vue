<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item
        label="消息标题"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          clearable
          placeholder="请输入标题"
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="消息类型"
        prop="messageType"
      >
        <el-select
          v-model="queryParams.messageType"
          clearable
          placeholder="全部类型"
          style="width: 160px"
        >
          <el-option
            v-for="item in messageTypes"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="阅读状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          clearable
          placeholder="全部状态"
          style="width: 140px"
        >
          <el-option
            label="未读"
            value="0"
          />
          <el-option
            label="已读"
            value="1"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="Search"
          @click="handleQuery"
        >搜索</el-button>
        <el-button
          icon="Refresh"
          @click="resetQuery"
        >重置</el-button>
      </el-form-item>
    </el-form>
    <el-row
      :gutter="10"
      class="mb8"
    >
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Check"
          :disabled="!selection.length"
          :loading="readLoading"
          v-hasPermi="['campus:messages:read']"
          @click="handleRead(selection)"
        >标记已读</el-button>
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      class="mb8"
    />
    <el-table
      v-loading="loading"
      :data="messageList"
      @selection-change="selection = $event"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="消息标题"
        prop="title"
        min-width="240"
        show-overflow-tooltip
      />
      <el-table-column
        label="消息类型"
        width="120"
        align="center"
      >
        <template #default="scope">{{ typeLabel(scope.row.messageType) }}</template>
      </el-table-column>
      <el-table-column
        label="业务ID"
        prop="bizId"
        width="100"
        align="center"
      />
      <el-table-column
        label="创建时间"
        width="180"
        align="center"
      >
        <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column
        label="阅读状态"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-tag :type="scope.row.readFlag === '0' ? 'warning' : 'info'">{{ scope.row.readFlag === '0' ? '未读' : '已读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="200"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            v-hasPermi="['campus:messages:query']"
            @click="handleView(scope.row)"
          >详情</el-button>
          <el-button
            v-if="scope.row.readFlag === '0'"
            link
            type="primary"
            icon="Check"
            :disabled="readLoading"
            v-hasPermi="['campus:messages:read']"
            @click="handleRead([scope.row])"
          >已读</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
    <el-dialog
      title="消息详情"
      v-model="open"
      width="650px"
      append-to-body
    >
      <el-descriptions
        v-if="detail"
        :column="1"
        border
      >
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeLabel(detail.messageType) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <span style="white-space: pre-wrap">{{ detail.content }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="阅读状态">{{ detail.readFlag === '0' ? '未读' : '已读' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="open = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusMessages">
import {
  ref,
  reactive,
  getCurrentInstance,
  onBeforeUnmount
} from 'vue'
import {
  listMessages,
  getMessage,
  readMessages
} from '@/api/campus/messages'

const {
  proxy
} = getCurrentInstance()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  messageType: undefined,
  status: undefined
})
const dateRange = ref([])
const messageList = ref([]),
  total = ref(0),
  selection = ref([])
const showSearch = ref(true),
  loading = ref(false),
  readLoading = ref(false),
  error = ref('')
const open = ref(false),
  detail = ref(null)
const messageTypes = [{
    value: 'BORROW_AUDIT',
    label: '借用审核'
  }, {
    value: 'PICKUP',
    label: '领取'
  },
  {
    value: 'RETURN',
    label: '归还'
  }, {
    value: 'DUE_SOON',
    label: '临期'
  },
  {
    value: 'OVERDUE',
    label: '逾期'
  }, {
    value: 'REPAIR',
    label: '维修'
  },
  {
    value: 'INVENTORY',
    label: '盘点'
  }, {
    value: 'SCRAP',
    label: '报废'
  }
]
let requestId = 0

function typeLabel(type) {
  return messageTypes.find(item => item.value === type)?.label || type
}
async function getList() {
  const current = ++requestId
  loading.value = true
  error.value = ''
  const params = {
    ...queryParams
  }
  if (dateRange.value?.length === 2) {
    const end = new Date(`${dateRange.value[1]}T00:00:00`)
    end.setDate(end.getDate() + 1)
    params.beginTime = `${dateRange.value[0]} 00:00:00`
    params.endTime =
      `${end.getFullYear()}-${String(end.getMonth()+1).padStart(2,'0')}-${String(end.getDate()).padStart(2,'0')} 00:00:00`
  }
  try {
    const response = await listMessages(params)
    if (current !== requestId) return
    messageList.value = response.rows || []
    total.value = response.total || 0
    selection.value = []
  } catch (err) {
    if (current === requestId) {
      messageList.value = [];
      total.value = 0;
      error.value = err.message || '消息加载失败'
    }
  } finally {
    if (current === requestId) loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList()
}

function resetQuery() {
  dateRange.value = [];
  proxy.resetForm('queryRef');
  handleQuery()
}
async function handleView(row) {
  try {
    const response = await getMessage(row.messageId);
    detail.value = response.data;
    open.value = true
  } catch {
    /* 统一错误提示 */
  }
}
async function handleRead(rows) {
  if (readLoading.value) return
  if (rows.length > 100) return proxy.$modal.msgWarning('一次最多标记100条消息')
  readLoading.value = true
  try {
    await readMessages(rows.map(row => row.messageId));
    proxy.$modal.msgSuccess('已标记为已读');
    getList()
  } catch {
    /* 不更新失败记录 */
  } finally {
    readLoading.value = false
  }
}
onBeforeUnmount(() => {
  requestId++
})
getList()
</script>

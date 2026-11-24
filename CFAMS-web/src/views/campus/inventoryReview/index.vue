<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
      @submit.prevent="handleQuery"
    >
      <el-form-item
        label="任务ID"
        prop="taskId"
      >
        <el-input-number
          v-model="queryParams.taskId"
          :min="1"
          :precision="0"
          controls-position="right"
          style="width: 160px"
        />
      </el-form-item>
      <el-form-item
        label="资产编号 / 名称"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="管理部门"
        prop="deptId"
      >
        <el-select
          v-model="queryParams.deptId"
          clearable
          filterable
          style="width: 180px"
        >
          <el-option
            v-for="item in departments"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="任务状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="(label, value) in taskStatuses"
            :key="value"
            :label="label"
            :value="String(value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="复核状态"
        prop="reviewStatus"
      >
        <el-select
          v-model="queryParams.reviewStatus"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="(label, value) in reviewStatuses"
            :key="value"
            :label="label"
            :value="String(value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="差异类型"
        prop="difference"
      >
        <el-select
          v-model="queryParams.difference"
          clearable
          style="width: 150px"
        >
          <el-option
            label="位置变更"
            value="moved"
          />
          <el-option
            label="损坏"
            value="damaged"
          />
          <el-option
            label="疑似丢失"
            value="missing"
          />
          <el-option
            label="未盘"
            value="unseen"
          />
        </el-select>
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
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
    <el-alert
      v-if="optionError"
      :title="optionError"
      type="warning"
      :closable="false"
    >
      <el-button
        link
        @click="loadOptions"
      >重试选项</el-button>
    </el-alert>
    <el-alert
      v-if="listError"
      :title="listError"
      type="error"
      :closable="false"
    />
    <el-table
      v-loading="loading"
      :data="itemList"
    >
      <el-table-column
        label="任务ID"
        prop="taskId"
        width="90"
      />
      <el-table-column
        label="资产编号"
        prop="assetCode"
        min-width="150"
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="150"
      />
      <el-table-column
        label="基准位置"
        min-width="130"
      >
        <template #default="{ row }">{{ baseLocation(row) }}</template>
      </el-table-column>
      <el-table-column
        label="现场位置"
        min-width="130"
      >
        <template #default="{ row }">{{ locationLabel(row.observedLocationId) }}</template>
      </el-table-column>
      <el-table-column
        label="现场完好"
        width="100"
      >
        <template #default="{ row }">{{ conditions[row.observedCondition] || '—' }}</template>
      </el-table-column>
      <el-table-column
        label="疑似丢失"
        width="90"
      >
        <template #default="{ row }">{{ row.missingFlag === '1' ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column
        label="盘点人员"
        prop="assignedUserName"
        min-width="110"
      />
      <el-table-column
        label="盘点时间"
        prop="checkedTime"
        width="170"
      />
      <el-table-column
        label="复核状态"
        width="110"
      >
        <template #default="{ row }">{{ reviewStatuses[row.reviewStatus] }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="150"
        fixed="right"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPermi="['campus:inventoryReview:query']"
            link
            type="primary"
            :disabled="busy"
            @click="handleDetail(row)"
          >查看</el-button>
          <el-button
            v-if="row.reviewStatus !== '1'"
            v-hasPermi="['campus:inventoryReview:review']"
            link
            type="primary"
            :disabled="busy"
            @click="handleReview(row)"
          >复核</el-button>
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
      v-model="open"
      :title="reviewMode ? '盘点差异复核' : '盘点明细'"
      width="960px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :before-close="closeDialog"
    >
      <template v-if="record">
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="任务ID">{{ record.taskId }}</el-descriptions-item>
          <el-descriptions-item label="资产">{{ record.assetCode || `#${record.assetId}` }} {{ record.assetName }}
          </el-descriptions-item>
          <el-descriptions-item label="执行人">{{ record.assignedUserName || `#${record.assignedUserId}` }}
          </el-descriptions-item>
          <el-descriptions-item label="盘点时间">{{ record.checkedTime || '未盘点' }}</el-descriptions-item>
          <el-descriptions-item label="现场位置">{{ locationLabel(record.observedLocationId) }}</el-descriptions-item>
          <el-descriptions-item label="现场完好">{{ conditions[record.observedCondition] || '—' }}</el-descriptions-item>
          <el-descriptions-item label="疑似丢失">{{ record.missingFlag === '1' ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="复核状态">{{ reviewStatuses[record.reviewStatus] }}</el-descriptions-item>
          <el-descriptions-item
            label="现场说明"
            :span="2"
          >{{ record.checkNote || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复核位置">{{ locationLabel(record.resolvedLocationId) }}</el-descriptions-item>
          <el-descriptions-item label="复核完好">{{ conditions[record.resolvedCondition] || '—' }}</el-descriptions-item>
          <el-descriptions-item label="关联维修单ID">{{ record.repairId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="关联处置单ID">{{ record.scrapId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复核时间">{{ record.reviewTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复核人ID">{{ record.reviewUserId || '—' }}</el-descriptions-item>
          <el-descriptions-item
            label="复核结论"
            :span="2"
          >{{ record.reviewNote || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h3>发布基准</h3>
        <record-details
          :record="parseSnapshot(record.baseSnapshot)"
          page-key="assets"
          :options="{ locations, departments }"
        />
        <template v-if="record.reviewSnapshot">
          <h3>上次复核时台账</h3>
          <record-details
            :record="parseSnapshot(record.reviewSnapshot)"
            page-key="assets"
            :options="{ locations, departments }"
          />
        </template>
        <h3>现场附件</h3>
        <business-attachments
          :key="record.itemId"
          biz-type="INVENTORY_ITEM"
          :biz-id="record.itemId"
        />
        <template v-if="reviewMode">
          <el-alert
            title="仅待复核任务可办理；退回重盘后任务恢复执行中。现场记录由现场人员提交。"
            type="info"
            :closable="false"
            class="mb8"
          />
          <el-alert
            v-if="!record.checkedTime"
            title="该明细未盘点，只能记录未盘结论或退回重盘，不能修改台账或关联维修。"
            type="warning"
            :closable="false"
            class="mb8"
          />
          <el-form
            ref="reviewRef"
            :model="form"
            :rules="rules"
            label-width="130px"
            :disabled="busy"
          >
            <el-form-item
              label="办理结论"
              prop="approved"
            >
              <el-radio-group v-model="form.approved">
                <el-radio :value="true">确认复核</el-radio>
                <el-radio :value="false">退回重盘</el-radio>
              </el-radio-group>
            </el-form-item>
            <template v-if="form.approved && record.checkedTime">
              <el-form-item label="仅记录结论">
                <el-switch v-model="form.recordOnly" />
                <span class="form-tip">适用于台账已变化或资产已注销</span>
              </el-form-item>
              <template v-if="!form.recordOnly">
                <el-form-item
                  label="确认位置"
                  prop="resolvedLocationId"
                >
                  <el-select
                    v-model="form.resolvedLocationId"
                    clearable
                    filterable
                    :disabled="record.missingFlag === '1'"
                    placeholder="留空表示不调整"
                  >
                    <el-option
                      v-for="item in locations"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item
                  label="确认完好情况"
                  prop="resolvedCondition"
                >
                  <el-select
                    v-model="form.resolvedCondition"
                    clearable
                    placeholder="留空表示不调整"
                  >
                    <el-option
                      v-for="(label, value) in conditions"
                      :key="value"
                      :label="label"
                      :value="String(value)"
                      :disabled="record.missingFlag === '1' && String(value) !== '2'"
                    />
                  </el-select>
                </el-form-item>
                <template v-if="record.missingFlag !== '1'">
                  <el-form-item label="维修处理">
                    <el-radio-group v-model="form.repairMode">
                      <el-radio value="none">不关联</el-radio>
                      <el-radio value="existing">关联已有维修</el-radio>
                      <el-radio value="create">新建报修</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item
                    v-if="form.repairMode === 'existing'"
                    label="维修单ID"
                    prop="repairId"
                  >
                    <el-input-number
                      v-model="form.repairId"
                      :min="1"
                      :precision="0"
                      controls-position="right"
                    />
                    <span class="form-tip">须为本资产尚未完成的维修单</span>
                  </el-form-item>
                  <template v-if="form.repairMode === 'create'">
                    <el-form-item
                      label="故障类型"
                      prop="faultType"
                    >
                      <el-select v-model="form.faultType">
                        <el-option
                          v-for="item in faultTypes"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>
                    <el-form-item
                      label="故障描述"
                      prop="faultDescription"
                    >
                      <el-input
                        v-model="form.faultDescription"
                        type="textarea"
                        maxlength="2000"
                        show-word-limit
                      />
                    </el-form-item>
                    <el-form-item
                      label="现场地点"
                      prop="faultLocation"
                    >
                      <el-input
                        v-model="form.faultLocation"
                        maxlength="200"
                      />
                    </el-form-item>
                  </template>
                </template>
              </template>
            </template>
            <el-form-item
              label="复核结论"
              prop="reviewNote"
            >
              <el-input
                v-model="form.reviewNote"
                type="textarea"
                :rows="3"
                :maxlength="record.checkedTime ? 1000 : 980"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </template>
      </template>
      <template #footer>
        <el-button
          v-if="reviewMode"
          v-hasPermi="['campus:inventoryReview:review']"
          type="primary"
          :loading="busy"
          @click="submitReview"
        >{{ form.approved ? '确认复核' : '退回重盘' }}</el-button>
        <el-button
          :disabled="busy"
          @click="open = false"
        >关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusInventoryReview">
import {
  ref,
  reactive,
  computed,
  nextTick
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import RecordDetails from '@/components/Campus/RecordDetails.vue'
import BusinessAttachments from '@/components/Campus/BusinessAttachments.vue'
import {
  getOptions
} from '@/api/campus/options'
import {
  listInventoryItems,
  getInventoryItem,
  reviewInventoryItem
} from '@/api/campus/inventory'

const taskStatuses = ['未开始', '执行中', '待复核', '已完成']
const reviewStatuses = ['待复核', '已复核', '退回重盘'],
  conditions = ['正常', '损坏', '待核验']
const faultTypes = [{
  value: 'ELECTRICAL',
  label: '电气故障'
}, {
  value: 'MECHANICAL',
  label: '机械故障'
}, {
  value: 'SOFTWARE',
  label: '软件故障'
}, {
  value: 'DAMAGE',
  label: '外观损坏'
}, {
  value: 'OTHER',
  label: '其他'
}]
const queryRef = ref(),
  reviewRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  taskId: undefined,
  keyword: '',
  deptId: undefined,
  status: '2',
  reviewStatus: '0',
  difference: undefined
})
const showSearch = ref(true),
  loading = ref(false),
  busy = ref(false),
  total = ref(0),
  itemList = ref([])
const departments = ref([]),
  locations = ref([]),
  optionError = ref(''),
  listError = ref('')
const open = ref(false),
  record = ref(null),
  reviewMode = ref(false),
  form = ref({})
let listTicket = 0
const adjustsLedger = computed(() => form.value.approved && record.value?.checkedTime && !form.value.recordOnly)
const rules = computed(() => {
  const required = message => [{
    required: true,
    whitespace: true,
    message,
    trigger: 'blur'
  }]
  const result = {
    reviewNote: required('请填写复核结论')
  }
  if (adjustsLedger.value && form.value.repairMode === 'existing') result.repairId = [{
    required: true,
    type: 'number',
    min: 1,
    message: '请输入有效维修单ID',
    trigger: 'change'
  }]
  if (adjustsLedger.value && form.value.repairMode === 'create') {
    result.faultType = required('请选择故障类型')
    result.faultDescription = required('请输入故障描述')
    result.faultLocation = required('请输入现场地点')
  }
  return result
})

function closeDialog(done) {
  if (!busy.value) done()
}

function locationLabel(id) {
  return id == null ? '—' : locations.value.find(item => String(item.value) === String(id))?.label || `#${id}`
}

function parseSnapshot(value) {
  try {
    return Object.fromEntries(Object.entries(JSON.parse(value)).map(([key, item]) => [key.replace(/_([a-z])/g, (_,
      letter) => letter.toUpperCase()), item]))
  } catch {
    return null
  }
}

function baseLocation(row) {
  const base = parseSnapshot(row.baseSnapshot);
  return base?.locationName || locationLabel(base?.locationId)
}
async function loadOptions() {
  optionError.value = ''
  const results = await Promise.allSettled([
    ['departments', departments],
    ['locations', locations]
  ].map(async ([type, target]) => {
    target.value = (await getOptions(type)).data || []
  }))
  if (results.some(result => result.status === 'rejected')) optionError.value = '部门或场所选项加载失败，请重试'
}
async function getList() {
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listInventoryItems({
      ...queryParams
    })
    if (ticket !== listTicket) return
    itemList.value = response.rows || []
    total.value = Number(response.total || 0)
  } catch (error) {
    if (ticket === listTicket) {
      itemList.value = [];
      total.value = 0;
      listError.value = error.message || '明细加载失败'
    }
  } finally {
    if (ticket === listTicket) loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields();
  handleQuery()
}
async function loadRecord(row, reviewing) {
  if (busy.value) return
  busy.value = true
  try {
    // detail 接口仅返回实体字段，保留列表返回的资产及执行人展示字段。
    record.value = {
      ...row,
      ...(await getInventoryItem(row.itemId)).data
    }
    if (reviewing && record.value.reviewStatus === '1') return ElMessage.warning('该明细已经复核')
    reviewMode.value = reviewing
    form.value = {
      approved: true,
      reviewNote: '',
      recordOnly: false,
      resolvedLocationId: undefined,
      resolvedCondition: record.value.missingFlag === '1' ? '2' : undefined,
      repairMode: 'none',
      repairId: undefined,
      faultType: undefined,
      faultDescription: '',
      faultLocation: ''
    }
    open.value = true
    await nextTick()
    reviewRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '详情加载失败')
  } finally {
    busy.value = false
  }
}

function handleDetail(row) {
  return loadRecord(row, false)
}

function handleReview(row) {
  return loadRecord(row, true)
}
async function submitReview() {
  if (busy.value || !await reviewRef.value.validate().catch(() => false)) return
  const data = {
    approved: form.value.approved,
    reviewNote: form.value.reviewNote.trim()
  }
  if (form.value.approved && record.value.checkedTime) {
    data.recordOnly = form.value.recordOnly
    if (!form.value.recordOnly) {
      data.resolvedLocationId = form.value.resolvedLocationId || null
      data.resolvedCondition = form.value.resolvedCondition || null
      if (record.value.missingFlag === '1') {
        data.resolvedLocationId = null
        if (data.resolvedCondition !== '2') return ElMessage.warning('疑似丢失须确认待核验状态')
      } else {
        if (form.value.repairMode !== 'none' && data.resolvedCondition !== '1') return ElMessage.warning(
          '关联或新建维修时请确认完好情况为损坏')
        if ((data.resolvedCondition === '1' || record.value.observedCondition === '1') && form.value.repairMode ===
          'none') return ElMessage.warning('损坏资产必须关联或新建维修')
        if (form.value.repairMode === 'existing') data.repairId = form.value.repairId
        if (form.value.repairMode === 'create') Object.assign(data, {
          createRepair: true,
          faultType: form.value.faultType,
          faultDescription: form.value.faultDescription.trim(),
          faultLocation: form.value.faultLocation.trim()
        })
      }
    }
  }
  busy.value = true
  try {
    await ElMessageBox.confirm(data.approved ? '确认提交复核结论？' : '确认退回重盘？任务将恢复执行中。', '复核确认')
    await reviewInventoryItem(record.value.itemId, data)
    ElMessage.success('办理成功')
    open.value = false
    await getList()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) ElMessage.error(error.message || '复核失败')
  } finally {
    busy.value = false
  }
}
onBeforeRouteLeave(() => !busy.value)
getList()
loadOptions()
</script>

<style scoped>
.form-tip {
  margin-left: 12px;
  color: var(--el-text-color-secondary);
}
</style>

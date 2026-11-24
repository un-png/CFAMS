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
        label="任务编号 / 名称"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入任务编号或名称"
          clearable
          style="width: 220px"
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
          style="width: 200px"
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
          style="width: 160px"
        >
          <el-option
            v-for="(label, value) in statuses"
            :key="value"
            :label="label"
            :value="String(value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="截止日期"
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
          v-hasPermi="['campus:inventory:add']"
          type="primary"
          plain
          icon="Plus"
          :disabled="busy"
          @click="handleAdd"
        >新增任务</el-button>
      </el-col>
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
      :data="taskList"
    >
      <el-table-column
        label="任务编号"
        prop="taskNo"
        min-width="190"
        show-overflow-tooltip
      />
      <el-table-column
        label="任务名称"
        prop="taskName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="管理部门"
        prop="deptName"
        min-width="120"
      />
      <el-table-column
        label="开始时间"
        prop="startTime"
        width="170"
      />
      <el-table-column
        label="截止时间"
        prop="endTime"
        width="170"
      />
      <el-table-column
        label="清单总数"
        prop="totalCount"
        width="90"
        align="center"
      />
      <el-table-column
        label="已盘数量"
        prop="checkedCount"
        width="90"
        align="center"
      />
      <el-table-column
        label="已复核"
        prop="reviewedCount"
        width="90"
        align="center"
      />
      <el-table-column
        label="任务状态"
        width="120"
        align="center"
      >
        <template #default="{ row }">{{ taskStatus(row) }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="260"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPermi="['campus:inventory:query']"
            link
            type="primary"
            :disabled="busy"
            @click="handleDetail(row)"
          >详情</el-button>
          <el-button
            v-if="isDraft(row)"
            v-hasPermi="['campus:inventory:add']"
            link
            type="primary"
            icon="Edit"
            :disabled="busy"
            @click="handleUpdate(row)"
          >修改</el-button>
          <el-button
            v-if="isDraft(row)"
            v-hasPermi="['campus:inventory:publish']"
            link
            type="primary"
            :disabled="busy"
            @click="handlePublish(row)"
          >发布</el-button>
          <el-button
            v-if="row.status === '0' && row.publishTime"
            v-hasPermi="['campus:inventory:publish']"
            link
            type="primary"
            :disabled="busy"
            @click="handleStart(row)"
          >开始</el-button>
          <el-button
            v-if="row.status === '1'"
            v-hasPermi="['campus:inventory:publish']"
            link
            type="primary"
            :disabled="busy"
            @click="handleSubmit(row)"
          >提交复核</el-button>
          <el-button
            v-if="row.status === '2'"
            v-hasPermi="['campus:inventoryReview:review']"
            link
            type="primary"
            :disabled="busy"
            @click="handleComplete(row)"
          >结项</el-button>
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
      :title="editingId ? '修改盘点任务' : '新增盘点任务'"
      width="700px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="closeDialog"
    >
      <el-form
        ref="taskRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        :disabled="busy"
      >
        <el-form-item
          label="任务名称"
          prop="taskName"
        >
          <el-input
            v-model="form.taskName"
            maxlength="150"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          label="管理部门"
          prop="deptId"
        >
          <el-select
            v-model="form.deptId"
            filterable
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
          label="范围部门"
          prop="scopeDeptId"
        >
          <el-select
            v-model="form.scopeDeptId"
            clearable
            filterable
            placeholder="不限部门（包含下级）"
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
          label="范围场所"
          prop="scopeLocationId"
        >
          <el-select
            v-model="form.scopeLocationId"
            clearable
            filterable
            placeholder="不限场所（包含下级）"
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
          label="范围类别"
          prop="scopeCategoryId"
        >
          <el-select
            v-model="form.scopeCategoryId"
            clearable
            filterable
            placeholder="不限类别（包含下级）"
          >
            <el-option
              v-for="item in categories"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="盘点人员"
          prop="userIds"
        >
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            remote
            reserve-keyword
            :remote-method="searchPeople"
            :loading="peopleLoading"
            :multiple-limit="500"
            placeholder="搜索具有盘点执行权限的人员"
          >
            <el-option
              v-for="item in people"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="开始时间"
          prop="startTime"
        >
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="taskRef?.validateField('endTime')"
          />
        </el-form-item>
        <el-form-item
          label="截止时间"
          prop="endTime"
        >
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item
          label="任务说明"
          prop="remark"
        >
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="busy"
          @click="submitForm"
        >确 定</el-button>
        <el-button
          :disabled="busy"
          @click="open = false"
        >取 消</el-button>
      </template>
    </el-dialog>
  
    <el-dialog
      v-model="publishOpen"
      title="范围预览与人员分配"
      width="900px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="closeDialog"
    >
      <p>当前范围共 {{ assignments.length }} 件资产，发布时冻结基准。请为每件资产指定任务内的盘点人员。</p>
      <el-row
        :gutter="10"
        class="mb8"
      >
        <el-col :span="10">
          <el-select
            v-model="batchUserId"
            :disabled="busy"
            placeholder="统一分配人员"
          >
            <el-option
              v-for="item in assignedPeople"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button
            :disabled="busy || !batchUserId"
            @click="assignAll"
          >应用到全部</el-button>
        </el-col>
        <el-col :span="6">
          <el-button
            :disabled="busy"
            icon="Refresh"
            @click="refreshCandidates"
          >刷新范围</el-button>
        </el-col>
      </el-row>
      <el-table
        :data="assignments"
        max-height="440"
      >
        <el-table-column
          prop="assetCode"
          label="资产编号"
          min-width="150"
        />
        <el-table-column
          prop="assetName"
          label="资产名称"
          min-width="160"
        />
        <el-table-column label="场所">
          <template #default="{ row }">{{ optionLabel(locations, row.locationId) }}</template>
        </el-table-column>
        <el-table-column
          label="盘点人员"
          min-width="200"
        >
          <template #default="{ row }">
            <el-select
              v-model="row.userId"
              :disabled="busy"
              filterable
            >
              <el-option
                v-for="item in assignedPeople"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button
          v-hasPermi="['campus:inventory:publish']"
          type="primary"
          :loading="busy"
          :disabled="!assignments.length || assignments.length > 10000"
          @click="submitPublish"
        >确认发布</el-button>
        <el-button
          :disabled="busy"
          @click="publishOpen = false"
        >取 消</el-button>
      </template>
    </el-dialog>
  
    <el-dialog
      v-model="detailOpen"
      title="盘点任务详情"
      width="1000px"
      append-to-body
    >
      <el-descriptions
        v-if="detail"
        :column="2"
        border
      >
        <el-descriptions-item label="任务编号">{{ detail.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detail.taskName }}</el-descriptions-item>
        <el-descriptions-item label="管理部门">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ taskStatus(detail) }}</el-descriptions-item>
        <el-descriptions-item label="范围部门">{{ optionLabel(departments, detail.scopeDeptId, '不限') }}</el-descriptions-item>
        <el-descriptions-item label="范围场所">{{ optionLabel(locations, detail.scopeLocationId, '不限') }}
        </el-descriptions-item>
        <el-descriptions-item label="范围类别">{{ optionLabel(categories, detail.scopeCategoryId, '不限') }}
        </el-descriptions-item>
        <el-descriptions-item label="盘点人员">{{ (detail.userIds || []).map(id => optionLabel(people, id)).join('、') }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detail.publishTime || '未发布' }}</el-descriptions-item>
        <el-descriptions-item label="结项时间">{{ detail.completeTime || '—' }}</el-descriptions-item>
        <el-descriptions-item
          label="执行进度"
          :span="2"
        >总数 {{ detail.totalCount }}，已盘 {{ detail.checkedCount }}，已复核 {{ detail.reviewedCount }}</el-descriptions-item>
        <el-descriptions-item
          label="任务说明"
          :span="2"
        >{{ detail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <h3>盘点清单及人员分配</h3>
      <el-alert
        v-if="itemError"
        :title="itemError"
        type="error"
        :closable="false"
      />
      <el-table
        v-loading="itemLoading"
        :data="itemList"
      >
        <el-table-column
          prop="assetCode"
          label="资产编号"
        />
        <el-table-column
          prop="assetName"
          label="资产名称"
        />
        <el-table-column
          prop="assignedUserName"
          label="执行人"
        />
        <el-table-column
          prop="checkedTime"
          label="盘点时间"
          width="170"
        />
        <el-table-column
          prop="checkNote"
          label="现场说明"
          show-overflow-tooltip
        />
        <el-table-column label="复核状态">
          <template #default="{ row }">{{ ['待复核', '已复核', '退回重盘'][row.reviewStatus] }}</template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="itemTotal > 0"
        :total="itemTotal"
        v-model:page="itemQuery.pageNum"
        v-model:limit="itemQuery.pageSize"
        @pagination="getItems"
      />
    </el-dialog>
  </div>
</template>

<script setup name="CampusInventory">
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
import {
  getOptions
} from '@/api/campus/options'
import {
  listInventory,
  getInventory,
  addInventory,
  updateInventory,
  getInventoryCandidates,
  publishInventory,
  startInventory,
  submitInventory,
  completeInventory,
  listInventoryItems
} from '@/api/campus/inventory'

const statuses = ['未开始', '执行中', '待复核', '已完成']
const queryRef = ref(),
  taskRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  deptId: undefined,
  status: undefined
})
const dateRange = ref([]),
  showSearch = ref(true),
  loading = ref(false),
  busy = ref(false)
const taskList = ref([]),
  total = ref(0),
  listError = ref(''),
  optionError = ref('')
const departments = ref([]),
  locations = ref([]),
  categories = ref([]),
  people = ref([]),
  peopleLoading = ref(false)
const open = ref(false),
  editingId = ref(),
  form = ref({})
const publishOpen = ref(false),
  publishTask = ref(null),
  assignments = ref([]),
  batchUserId = ref()
const detailOpen = ref(false),
  detail = ref(null),
  itemList = ref([]),
  itemTotal = ref(0),
  itemLoading = ref(false),
  itemError = ref('')
const itemQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  taskId: undefined
})
let listTicket = 0,
  peopleTicket = 0,
  itemTicket = 0
const assignedPeople = computed(() => (publishTask.value?.userIds || []).map(value => ({
  value,
  label: optionLabel(people.value, value)
})))
const required = message => ({
  required: true,
  message,
  trigger: 'change'
})
const rules = {
  taskName: [{
    required: true,
    whitespace: true,
    message: '请输入任务名称',
    trigger: 'blur'
  }],
  deptId: [required('请选择管理部门')],
  userIds: [{
    type: 'array',
    required: true,
    min: 1,
    message: '请选择盘点人员',
    trigger: 'change'
  }],
  startTime: [required('请选择开始时间')],
  endTime: [required('请选择截止时间'), {
    validator: (_, value, done) => done(value && form.value.startTime && value <= form.value.startTime ?
      new Error('截止时间必须晚于开始时间') : undefined),
    trigger: 'change'
  }]
}

function optionLabel(options, id, fallback = '—') {
  return id == null ? fallback : options.find(item => String(item.value) === String(id))?.label || `#${id}`
}

function isDraft(row) {
  return row.status === '0' && !row.publishTime
}

function taskStatus(row) {
  return isDraft(row) ? '未发布草稿' : statuses[row.status] || row.status
}

function closeDialog(done) {
  if (!busy.value) done()
}

function reportError(error) {
  ElMessage.error(error?.message || '操作失败，请重试')
}
async function getList() {
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listInventory({
      ...queryParams,
      beginTime: dateRange.value?.[0] ? `${dateRange.value[0]} 00:00:00` : undefined,
      endTime: dateRange.value?.[1] ? `${dateRange.value[1]} 23:59:59` : undefined
    })
    if (ticket !== listTicket) return
    taskList.value = response.rows || []
    total.value = Number(response.total || 0)
  } catch (error) {
    if (ticket === listTicket) {
      taskList.value = [];
      total.value = 0;
      listError.value = error.message || '任务列表加载失败'
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
  dateRange.value = [];
  handleQuery()
}
async function loadOptions() {
  optionError.value = ''
  const results = await Promise.allSettled([
    ['departments', departments],
    ['locations', locations],
    ['categories', categories]
  ].map(async ([type, target]) => {
    target.value = (await getOptions(type)).data || []
  }))
  if (results.some(result => result.status === 'rejected')) optionError.value = '部分范围选项加载失败，请重试'
  await searchPeople('')
}
async function searchPeople(keyword) {
  const ticket = ++peopleTicket
  peopleLoading.value = true
  try {
    const response = await getOptions('inventoryUsers', keyword)
    if (ticket !== peopleTicket) return
    const retained = people.value.filter(item => form.value.userIds?.includes(item.value) || publishTask.value
      ?.userIds?.includes(item.value))
    people.value = [...new Map([...retained, ...(response.data || [])].map(item => [String(item.value), item]))
      .values()
    ]
  } catch {
    if (ticket === peopleTicket) optionError.value = '盘点人员加载失败，请重试选项'
  } finally {
    if (ticket === peopleTicket) peopleLoading.value = false
  }
}

function handleAdd() {
  editingId.value = undefined
  form.value = {
    taskName: '',
    deptId: undefined,
    scopeDeptId: undefined,
    scopeLocationId: undefined,
    scopeCategoryId: undefined,
    userIds: [],
    startTime: '',
    endTime: '',
    remark: ''
  }
  open.value = true
  nextTick(() => taskRef.value?.clearValidate())
}
async function handleUpdate(row) {
  busy.value = true
  try {
    const task = (await getInventory(row.taskId)).data
    if (!isDraft(task)) return ElMessage.warning('任务已发布，请刷新列表')
    editingId.value = row.taskId
    form.value = {
      taskName: task.taskName,
      deptId: task.deptId,
      scopeDeptId: task.scopeDeptId,
      scopeLocationId: task.scopeLocationId,
      scopeCategoryId: task.scopeCategoryId,
      userIds: task.userIds || [],
      startTime: task.startTime?.replace('T', ' '),
      endTime: task.endTime?.replace('T', ' '),
      remark: task.remark || ''
    }
    for (const id of form.value.userIds)
      if (!people.value.some(item => String(item.value) === String(id))) people.value.push({
        value: id,
        label: `已分配人员 #${id}`
      })
    open.value = true
    await nextTick()
    taskRef.value?.clearValidate()
  } catch (error) {
    reportError(error)
  } finally {
    busy.value = false
  }
}
async function submitForm() {
  if (busy.value || !await taskRef.value.validate().catch(() => false)) return
  busy.value = true
  try {
    const data = {
      ...form.value,
      taskName: form.value.taskName.trim(),
      scopeDeptId: form.value.scopeDeptId || null,
      scopeLocationId: form.value.scopeLocationId || null,
      scopeCategoryId: form.value.scopeCategoryId || null
    }
    if (editingId.value) await updateInventory(editingId.value, data)
    else await addInventory(data)
    ElMessage.success('保存成功')
    open.value = false
    await getList()
  } catch (error) {
    reportError(error)
  } finally {
    busy.value = false
  }
}
async function handlePublish(row) {
  busy.value = true
  try {
    publishTask.value = (await getInventory(row.taskId)).data
    const response = await getInventoryCandidates(row.taskId)
    assignments.value = (response.data || []).map(item => ({
      ...item,
      userId: publishTask.value.userIds?.length === 1 ? publishTask.value.userIds[0] : undefined
    }))
    batchUserId.value = undefined
    publishOpen.value = true
  } catch (error) {
    reportError(error)
  } finally {
    busy.value = false
  }
}
async function refreshCandidates() {
  await handlePublish(publishTask.value)
}

function assignAll() {
  assignments.value.forEach(item => {
    item.userId = batchUserId.value
  })
}
async function submitPublish() {
  if (busy.value) return
  if (!assignments.value.length || assignments.value.length > 10000) return ElMessage.warning('发布范围需为1至10000件资产')
  if (assignments.value.some(item => !item.userId)) return ElMessage.warning('请为全部资产分配盘点人员')
  busy.value = true
  try {
    await ElMessageBox.confirm('确认发布该任务？发布后不能修改范围及人员。', '发布确认')
    await publishInventory(publishTask.value.taskId, {
      assignments: assignments.value.map(({
        assetId,
        userId
      }) => ({
        assetId,
        userId
      }))
    })
    publishOpen.value = false
    ElMessage.success('发布成功')
    await getList()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) reportError(error)
  } finally {
    busy.value = false
  }
}
async function confirmTransition(row, title, action) {
  if (busy.value) return
  busy.value = true
  try {
    await ElMessageBox.confirm(`确认${title}任务“${row.taskName}”？`, '确认操作')
    await action(row.taskId)
    ElMessage.success(`${title}成功`)
    await getList()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) reportError(error)
  } finally {
    busy.value = false
  }
}

function handleStart(row) {
  return confirmTransition(row, '开始', startInventory)
}

function handleSubmit(row) {
  return confirmTransition(row, '提交复核', submitInventory)
}

function handleComplete(row) {
  return confirmTransition(row, '结项', completeInventory)
}
async function handleDetail(row) {
  busy.value = true
  try {
    detail.value = (await getInventory(row.taskId)).data
    itemQuery.taskId = row.taskId
    itemQuery.pageNum = 1
    itemList.value = []
    detailOpen.value = true
    await getItems()
  } catch (error) {
    reportError(error)
  } finally {
    busy.value = false
  }
}
async function getItems() {
  const ticket = ++itemTicket
  itemLoading.value = true
  itemError.value = ''
  try {
    const response = await listInventoryItems({
      ...itemQuery
    })
    if (ticket !== itemTicket) return
    itemList.value = response.rows || []
    itemTotal.value = Number(response.total || 0)
  } catch (error) {
    if (ticket === itemTicket) {
      itemList.value = [];
      itemTotal.value = 0;
      itemError.value = error.message || '盘点清单加载失败'
    }
  } finally {
    if (ticket === itemTicket) itemLoading.value = false
  }
}
onBeforeRouteLeave(() => !busy.value)
getList()
loadOptions()
</script>

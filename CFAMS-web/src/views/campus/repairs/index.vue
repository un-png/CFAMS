<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      v-show="showSearch"
      :model="queryParams"
      :inline="true"
      @submit.prevent
    >
      <el-form-item
        label="关键词"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入业务单号、资产名称或编号"
          clearable
          style="width:250px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="部门"
        prop="deptId"
      >
        <el-select
          v-model="queryParams.deptId"
          placeholder="请选择部门"
          clearable
          filterable
          remote
          :remote-method="loadDepartments"
          style="width:190px"
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
        label="故障类型"
        prop="faultType"
      >
        <el-select
          v-model="queryParams.faultType"
          placeholder="故障类型"
          clearable
          style="width:150px"
        >
          <el-option
            v-for="item in faultTypes"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          clearable
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
    <el-tabs
      v-model="activeTab"
      @tab-change="handleQuery"
    >
      <el-tab-pane
        label="全部"
        name="all"
      />
      <el-tab-pane
        label="待受理"
        name="pending"
      />
      <el-tab-pane
        label="已派工"
        name="assigned"
      />
      <el-tab-pane
        label="维修中"
        name="repairing"
      />
      <el-tab-pane
        label="已驳回"
        name="rejected"
      />
    </el-tabs>
    <el-alert
      v-if="listError"
      :title="listError"
      type="error"
      :closable="false"
      show-icon
    />
    <el-alert
      v-if="optionError"
      :title="optionError"
      type="warning"
      :closable="false"
    >
      <el-button
        link
        @click="loadDepartments()"
      >重试部门选项</el-button>
    </el-alert>
    <el-row
      :gutter="10"
      class="mb8"
    >
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
    <el-table
      v-loading="loading"
      :data="rows"
    >
      <el-table-column
        label="报修单号"
        prop="repairNo"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="故障类型"
        prop="faultType"
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="scope">{{ displayValue('repairs', 'faultType', scope.row.faultType) }}</template>
      </el-table-column>
      <el-table-column
        label="报修人"
        prop="reporterName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="报修时间"
        prop="reportTime"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        label="维修人员"
        prop="repairerName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="状态"
        prop="status"
        width="110"
        align="center"
      >
        <template #default="scope">
          <el-tag>{{ displayValue('repairs', 'status', scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="250"
        align="center"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleDetail(scope.row)"
            v-hasPermi="['campus:repairs:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="warning"
            icon="User"
            @click="handleAssign(scope.row)"
            v-hasPermi="['campus:repairs:assign']"
          >受理派工</el-button>
          <el-button
            v-if="['1','2'].includes(String(scope.row.status))"
            link
            type="warning"
            icon="Refresh"
            @click="handleReassign(scope.row)"
            v-hasPermi="['campus:repairs:assign']"
          >改派</el-button>
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
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="!submitting"
      :close-on-press-escape="!submitting"
      :show-close="!submitting"
    >
      <el-alert
        v-if="repairerError"
        :title="repairerError"
        type="warning"
        :closable="false"
      >
        <el-button
          link
          @click="loadRepairers()"
        >重试维修人员</el-button>
      </el-alert>
      <RecordDetails
        :record="selectedRepair"
        page-key="repairs"
        :options="{ departments }"
      />
      <BusinessAttachments
        v-if="selectedRepair"
        biz-type="REPAIR"
        :biz-id="selectedRepair.repairId"
      />
      <el-form
        ref="processRef"
        :model="processForm"
        :rules="rules"
        :disabled="submitting"
        label-width="110px"
      >
        <el-form-item
          v-if="!reassigning"
          label="处理结果"
        >
          <el-radio-group v-model="processForm.approve">
            <el-radio :value="true">确认派工</el-radio>
            <el-radio :value="false">驳回报修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="processForm.approve"
          label="维修人员"
          prop="repairerUserId"
        >
          <el-select
            :loading="repairersLoading"
            v-model="processForm.repairerUserId"
            filterable
            remote
            :remote-method="loadRepairers"
            style="width:100%"
          >
            <el-option
              v-for="item in repairers"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="reassigning && String(item.value) === String(selectedRepair?.repairerUserId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="处理意见"
          prop="opinion"
        >
          <el-input
            v-model="processForm.opinion"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          :disabled="submitting"
          @click="dialogVisible = false"
        >取 消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitProcess"
        >确 定</el-button>
      </template>
    </el-dialog>
    <el-drawer
      v-model="detailVisible"
      title="报修受理与派工详情"
      size="780px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <el-alert
          v-if="detailError"
          :title="detailError"
          type="error"
          :closable="false"
          show-icon
        />
        <RecordDetails
          v-if="detail"
          :record="detail"
          page-key="repairs"
          :options="{ departments }"
        />
        <h3>维修过程</h3>
        <el-table
          :data="records"
          border
          size="small"
        >
          <el-table-column
            prop="actionType"
            label="动作"
            min-width="110"
          />
          <el-table-column
            prop="operatorName"
            label="操作人"
            min-width="110"
          />
          <el-table-column
            prop="content"
            label="处理说明"
            min-width="220"
            show-overflow-tooltip
          />
          <el-table-column
            prop="actionTime"
            label="时间"
            min-width="170"
          />
        </el-table>
        <BusinessAttachments
          v-if="detail"
          biz-type="REPAIR"
          :biz-id="detail.repairId"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="CampusRepairs">
import {
  ref,
  onBeforeUnmount,
  nextTick,
  computed
} from "vue"
import {
  ElMessage
} from "element-plus"
import auth from "@/plugins/auth"
import RecordDetails from "@/components/Campus/RecordDetails.vue"
import BusinessAttachments from "@/components/Campus/BusinessAttachments.vue"
import {
  displayValue
} from "@/utils/campusDisplay"
import {
  getOptions
} from "@/api/campus/options"
import {
  listRepairs,
  getRepair,
  getRepairRecords,
  assignRepair,
  reassignRepair
} from "@/api/campus/repair"

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const showSearch = ref(true)
const queryRef = ref()
const departments = ref([])
const optionError = ref("")
const listError = ref("")
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailError = ref("")
const records = ref([])
const dateRange = ref([])
const activeTab = ref("pending")
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined,
  faultType: undefined
})
const faultTypes = [{
    value: "ELECTRICAL",
    label: "电气故障"
  },
  {
    value: "MECHANICAL",
    label: "机械故障"
  },
  {
    value: "SOFTWARE",
    label: "软件故障"
  },
  {
    value: "DAMAGE",
    label: "外观损坏"
  },
  {
    value: "OTHER",
    label: "其他"
  }
]
let listRequest = 0
let detailRequest = 0
let departmentRequest = 0

function buildParams() {
  const params = {
    ...queryParams.value
  }
  if (dateRange.value?.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  const tabs = {
    pending: {
      status: "0"
    },
    assigned: {
      status: "1"
    },
    repairing: {
      status: "2"
    },
    rejected: {
      status: "5"
    }
  }
  Object.assign(params, tabs[activeTab.value] || {})
  return params
}

// 查询 → listRepairs → PcRepairController.list。
async function getList() {
  if (!auth.hasPermi("campus:repairs:list")) return
  const ticket = ++listRequest
  loading.value = true
  listError.value = ""
  try {
    const response = await listRepairs(buildParams())
    if (ticket !== listRequest) return
    rows.value = response.rows || []
    total.value = Number(response.total ?? 0)
    if (!rows.value.length && queryParams.value.pageNum > 1) {
      queryParams.value.pageNum = 1
      await getList()
    }
  } catch (error) {
    if (ticket === listRequest) {
      rows.value = []
      total.value = 0
      listError.value = error.message || "列表加载失败，请刷新重试"
    }
  } finally {
    if (ticket === listRequest) loading.value = false
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  dateRange.value = []
  activeTab.value = "pending"
  handleQuery()
}

async function loadDepartments(keyword = "") {
  if (!auth.hasPermi("campus:repairs:list")) return
  const ticket = ++departmentRequest
  optionError.value = ""
  try {
    const response = await getOptions("departments", keyword)
    if (ticket === departmentRequest) departments.value = response.data || []
  } catch (error) {
    if (ticket === departmentRequest) optionError.value = error.message || "部门选项加载失败"
  }
}

// 详情 → getRepair/getRepairRecords → PcRepairController.detail/process。
async function handleDetail(row) {
  if (!auth.hasPermi("campus:repairs:query")) return
  const ticket = ++detailRequest
  detail.value = null
  records.value = []
  detailError.value = ""
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getRepair(row.repairId)
    if (ticket !== detailRequest) return
    detail.value = response.data
    const events = await getRepairRecords(row.repairId)
    if (ticket === detailRequest) records.value = events.data || []
  } catch (error) {
    if (ticket === detailRequest) detailError.value = error.message || "详情加载失败"
  } finally {
    if (ticket === detailRequest) detailLoading.value = false
  }
}

const dialogVisible = ref(false)
const processRef = ref()
const submitting = ref(false)
const selectedRepair = ref(null)
const reassigning = ref(false)
const repairers = ref([])
const repairersLoading = ref(false)
const repairerError = ref("")
let repairerRequest = 0
const processForm = ref({
  approve: true,
  repairerUserId: undefined,
  opinion: ""
})
const dialogTitle = computed(() => reassigning.value ? "维修任务改派" : "受理与派工")
const rules = computed(() => ({
  repairerUserId: [{
    required: processForm.value.approve,
    message: "请选择维修人员",
    trigger: "change"
  }],
  opinion: [{
    required: true,
    whitespace: true,
    message: "请输入处理意见",
    trigger: "blur"
  }, {
    max: 500,
    message: "处理意见不能超过500字",
    trigger: "blur"
  }]
}))

async function loadRepairers(keyword = "") {
  const ticket = ++repairerRequest
  repairersLoading.value = true
  repairerError.value = ""
  try {
    const response = await getOptions("repairers", keyword)
    if (ticket === repairerRequest) repairers.value = response.data || []
  } catch (error) {
    if (ticket === repairerRequest) repairerError.value = error.message || "维修人员加载失败，请重新搜索"
  } finally {
    if (ticket === repairerRequest) repairersLoading.value = false
  }
}

function handleAssign(row) {
  if (!auth.hasPermi("campus:repairs:assign") || row.status !== "0" || submitting.value) return
  selectedRepair.value = row
  reassigning.value = false
  processForm.value = {
    approve: true,
    repairerUserId: undefined,
    opinion: ""
  }
  dialogVisible.value = true
  nextTick(() => processRef.value?.clearValidate())
  loadRepairers()
}

function handleReassign(row) {
  if (!auth.hasPermi("campus:repairs:assign") || !["1", "2"].includes(row.status) || submitting.value) return
  selectedRepair.value = row
  reassigning.value = true
  processForm.value = {
    approve: true,
    repairerUserId: undefined,
    opinion: ""
  }
  dialogVisible.value = true
  nextTick(() => processRef.value?.clearValidate())
  loadRepairers()
}

// 受理/改派表单 → repair.js → PcRepairController.assign/reassign。
async function submitProcess() {
  if (submitting.value || !await processRef.value.validate().catch(() => false)) return
  if (reassigning.value && String(processForm.value.repairerUserId) === String(selectedRepair.value.repairerUserId)) {
    ElMessage.warning("请选择其他维修人员")
    return
  }
  submitting.value = true
  try {
    const body = {
      approve: reassigning.value || processForm.value.approve,
      repairerUserId: processForm.value.approve ? processForm.value.repairerUserId : null,
      opinion: processForm.value.opinion.trim()
    }
    if (reassigning.value) {
      await reassignRepair(selectedRepair.value.repairId, body)
    } else {
      await assignRepair(selectedRepair.value.repairId, body)
    }
    dialogVisible.value = false
    ElMessage.success(reassigning.value ? "改派成功" : "受理完成")
    await getList()
  } catch (error) {
    ElMessage.error(error.message || "维修任务处理失败")
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => {
  listRequest++
  detailRequest++
  departmentRequest++
  repairerRequest++
})

getList()
loadDepartments()
</script>

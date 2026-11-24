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
        label="申请人"
        prop="applicant"
      >
        <el-input
          v-model="queryParams.applicant"
          placeholder="请输入申请人"
          clearable
          style="width:180px"
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
        label="待审核"
        name="pending"
      />
      <el-tab-pane
        label="已通过"
        name="approved"
      />
      <el-tab-pane
        label="已驳回"
        name="rejected"
      />
      <el-tab-pane
        label="已取消"
        name="cancelled"
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
        label="借用单号"
        prop="borrowNo"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产编号"
        prop="assetCode"
        min-width="130"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="申请人"
        prop="applicantName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产部门"
        min-width="120"
      >
        <template #default="scope">{{ displayValue("borrow", "assetDeptId", scope.row.assetDeptId, { departments }) }}
        </template>
      </el-table-column>
      <el-table-column
        label="预计领取"
        prop="plannedStartTime"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        label="预计归还"
        prop="plannedEndTime"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        label="状态"
        prop="status"
        width="110"
        align="center"
      >
        <template #default="scope">
          <el-tag>{{ displayValue('borrow', 'status', scope.row.status) }}</el-tag>
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
            v-hasPermi="['campus:borrow:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0' && String(scope.row.applicantUserId) !== String(user.id)"
            link
            type="warning"
            icon="Check"
            @click="handleAudit(scope.row)"
            v-hasPermi="['campus:borrow:audit']"
          >审核</el-button>
          <el-button
            v-if="['0','2'].includes(scope.row.status) && !scope.row.actualPickupTime"
            link
            type="danger"
            icon="Close"
            @click="handleCancel(scope.row)"
            v-hasPermi="['campus:borrow:audit']"
          >取消</el-button>
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
      <RecordDetails
        :record="selectedBorrow"
        page-key="borrow"
        :options="{ departments }"
      />
      <el-form
        ref="processRef"
        :model="processForm"
        :rules="rules"
        :disabled="submitting"
        label-width="110px"
      >
        <el-form-item
          v-if="!cancelling"
          label="审核结果"
        >
          <el-radio-group v-model="processForm.approve">
            <el-radio :value="true">通过审核</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="cancelling"
          label="取消原因"
          prop="reason"
        >
          <el-input
            v-model="processForm.reason"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          v-else
          label="审核意见"
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
      title="借用申请审核详情"
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
          page-key="borrow"
          :options="{ departments }"
        />
        <el-form
          v-if="detail?.voucherToken"
          label-width="100px"
        >
          <el-form-item label="电子凭证">
            <el-input
              :model-value="detail.voucherToken"
              readonly
            />
          </el-form-item>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="CampusBorrow">
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
import useUserStore from "@/store/modules/user"
import RecordDetails from "@/components/Campus/RecordDetails.vue"
import {
  displayValue
} from "@/utils/campusDisplay"
import {
  getOptions
} from "@/api/campus/options"
import {
  listBorrow,
  getBorrow,
  auditBorrow,
  cancelBorrow
} from "@/api/campus/borrow"

const user = useUserStore()
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

const dateRange = ref([])
const activeTab = ref("pending")
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined,
  applicant: undefined
})

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
    approved: {
      status: "2"
    },
    rejected: {
      status: "1"
    },
    cancelled: {
      status: "6"
    }
  }
  Object.assign(params, tabs[activeTab.value] || {})
  return params
}

// 查询 → listBorrow → PcBorrowController.list。
async function getList() {
  if (!auth.hasPermi("campus:borrow:list")) return
  const ticket = ++listRequest
  loading.value = true
  listError.value = ""
  try {
    const response = await listBorrow(buildParams())
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
  if (!auth.hasPermi("campus:borrow:list")) return
  const ticket = ++departmentRequest
  optionError.value = ""
  try {
    const response = await getOptions("departments", keyword)
    if (ticket === departmentRequest) departments.value = response.data || []
  } catch (error) {
    if (ticket === departmentRequest) optionError.value = error.message || "部门选项加载失败"
  }
}

// 详情 → getBorrow → PcBorrowController.detail。
async function handleDetail(row) {
  if (!auth.hasPermi("campus:borrow:query")) return
  const ticket = ++detailRequest
  detail.value = null
  detailError.value = ""
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getBorrow(row.borrowId)
    if (ticket !== detailRequest) return
    detail.value = response.data
  } catch (error) {
    if (ticket === detailRequest) detailError.value = error.message || "详情加载失败"
  } finally {
    if (ticket === detailRequest) detailLoading.value = false
  }
}

const dialogVisible = ref(false)
const processRef = ref()
const submitting = ref(false)
const selectedBorrow = ref(null)
const cancelling = ref(false)
const processForm = ref({
  approve: true,
  opinion: ""
})
const dialogTitle = computed(() => cancelling.value ? "取消预约" : "借用审核")
const rules = computed(() => cancelling.value ? {
  reason: [{
    required: true,
    whitespace: true,
    message: "请输入取消原因",
    trigger: "blur"
  }, {
    max: 500,
    message: "取消原因不能超过500字",
    trigger: "blur"
  }]
} : {
  opinion: [{
    required: true,
    whitespace: true,
    message: "请输入审核意见",
    trigger: "blur"
  }, {
    max: 500,
    message: "审核意见不能超过500字",
    trigger: "blur"
  }]
})

function handleAudit(row) {
  if (!auth.hasPermi("campus:borrow:audit") || row.status !== "0" || String(row.applicantUserId) === String(user.id) ||
    submitting.value) return
  selectedBorrow.value = row
  cancelling.value = false
  processForm.value = {
    approve: true,
    opinion: ""
  }
  dialogVisible.value = true
  nextTick(() => processRef.value?.clearValidate())
}

function handleCancel(row) {
  if (!auth.hasPermi("campus:borrow:audit") || !["0", "2"].includes(row.status) || row.actualPickupTime || submitting
    .value) return
  selectedBorrow.value = row
  cancelling.value = true
  processForm.value = {
    reason: ""
  }
  dialogVisible.value = true
  nextTick(() => processRef.value?.clearValidate())
}

// 审核/取消弹窗 → borrow.js → PcBorrowController.audit/cancel。
async function submitProcess() {
  if (submitting.value || !await processRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const id = selectedBorrow.value.borrowId
    if (cancelling.value) {
      await cancelBorrow(id, {
        reason: processForm.value.reason.trim()
      })
    } else {
      await auditBorrow(id, {
        approve: processForm.value.approve,
        opinion: processForm.value.opinion.trim()
      })
    }
    dialogVisible.value = false
    ElMessage.success(cancelling.value ? "预约已取消" : "审核成功")
    await getList()
  } catch (error) {
    ElMessage.error(error.message || "操作失败，请刷新后重试")
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => {
  listRequest++
  detailRequest++
  departmentRequest++
})

getList()
loadDepartments()
</script>

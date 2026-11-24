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
        label="待领取确认"
        name="pickup"
      />
      <el-tab-pane
        label="待归还确认"
        name="return"
      />
      <el-tab-pane
        label="已确认"
        name="confirmed"
      />
      <el-tab-pane
        label="已拒绝"
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
        label="交接编号"
        prop="handoverId"
        min-width="110"
        show-overflow-tooltip
      />
      <el-table-column
        label="借用单号"
        prop="borrowNo"
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
        label="申请人"
        prop="applicantName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="交接类型"
        prop="handoverType"
        min-width="100"
        show-overflow-tooltip
      >
        <template #default="scope">{{ displayValue('handover', 'handoverType', scope.row.handoverType) }}</template>
      </el-table-column>
      <el-table-column
        label="申请时间"
        prop="requestTime"
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
          <el-tag>{{ displayValue('handover', 'status', scope.row.status) }}</el-tag>
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
            v-hasPermi="['campus:handover:query']"
          >详情</el-button>
          <el-button
            v-if="scope.row.status === '0' && String(scope.row.applicantUserId) !== String(user.id)"
            link
            type="warning"
            icon="Connection"
            @click="handleConfirm(scope.row)"
            v-hasPermi="['campus:handover:confirm']"
          >交接确认</el-button>
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
      title="交接核验"
      width="620px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="!submitting"
      :close-on-press-escape="!submitting"
      :show-close="!submitting"
    >
      <RecordDetails
        :record="selectedHandover"
        page-key="handover"
        :options="{ departments }"
      />
      <el-form
        ref="processRef"
        :model="processForm"
        :rules="rules"
        :disabled="submitting"
        label-width="110px"
      >
        <el-form-item label="确认结果">
          <el-radio-group v-model="processForm.approve">
            <el-radio :value="true">确认交接</el-radio>
            <el-radio :value="false">拒绝交接</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="processForm.approve"
          label="电子凭证"
          prop="voucherToken"
        >
          <el-input
            v-model="processForm.voucherToken"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item
          v-if="processForm.approve"
          label="完好情况"
          prop="conditionStatus"
        >
          <el-select
            v-model="processForm.conditionStatus"
            style="width:100%"
          >
            <el-option
              label="正常"
              value="0"
            />
            <el-option
              label="损坏"
              value="1"
            />
            <el-option
              label="待核验"
              value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="交接说明"
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
      title="借还交接详情"
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
          page-key="handover"
          :options="{ departments }"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="CampusHandover">
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
  listHandovers,
  getHandover,
  confirmHandover
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

const activeTab = ref("pickup")
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

  const tabs = {
    pickup: {
      status: "0",
      handoverType: "1"
    },
    return: {
      status: "0",
      handoverType: "2"
    },
    confirmed: {
      status: "1"
    },
    rejected: {
      status: "2"
    }
  }
  Object.assign(params, tabs[activeTab.value] || {})
  return params
}

// 查询 → listHandovers → PcHandoverController.list。
async function getList() {
  if (!auth.hasPermi("campus:handover:list")) return
  const ticket = ++listRequest
  loading.value = true
  listError.value = ""
  try {
    const response = await listHandovers(buildParams())
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

  activeTab.value = "pickup"
  handleQuery()
}

async function loadDepartments(keyword = "") {
  if (!auth.hasPermi("campus:handover:list")) return
  const ticket = ++departmentRequest
  optionError.value = ""
  try {
    const response = await getOptions("departments", keyword)
    if (ticket === departmentRequest) departments.value = response.data || []
  } catch (error) {
    if (ticket === departmentRequest) optionError.value = error.message || "部门选项加载失败"
  }
}

// 详情 → getHandover → PcHandoverController.detail。
async function handleDetail(row) {
  if (!auth.hasPermi("campus:handover:query")) return
  const ticket = ++detailRequest
  detail.value = null
  detailError.value = ""
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getHandover(row.handoverId)
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
const selectedHandover = ref(null)
const processForm = ref({
  approve: true,
  voucherToken: "",
  conditionStatus: "0",
  opinion: ""
})
const rules = computed(() => ({
  voucherToken: [{
    required: processForm.value.approve,
    whitespace: true,
    message: "请输入电子凭证",
    trigger: "blur"
  }, {
    max: 64,
    message: "凭证不能超过64字",
    trigger: "blur"
  }],
  conditionStatus: [{
    required: processForm.value.approve,
    message: "请选择完好情况",
    trigger: "change"
  }],
  opinion: [{
    required: true,
    whitespace: true,
    message: "请输入交接说明",
    trigger: "blur"
  }, {
    max: 500,
    message: "交接说明不能超过500字",
    trigger: "blur"
  }]
}))

function handleConfirm(row) {
  if (!auth.hasPermi("campus:handover:confirm") || row.status !== "0" || String(row.applicantUserId) === String(user
      .id) || submitting.value) return
  selectedHandover.value = row
  processForm.value = {
    approve: true,
    voucherToken: "",
    conditionStatus: "0",
    opinion: ""
  }
  dialogVisible.value = true
  nextTick(() => processRef.value?.clearValidate())
}

// 交接表单 → confirmHandover → PcHandoverController.confirm。
async function submitProcess() {
  if (submitting.value || !await processRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const {
      approve,
      opinion,
      conditionStatus,
      voucherToken
    } = processForm.value
    await confirmHandover(selectedHandover.value.handoverId, {
      approve,
      opinion: opinion.trim(),
      conditionStatus: approve ? conditionStatus : null,
      voucherToken: approve ? voucherToken.trim() : null
    })
    dialogVisible.value = false
    ElMessage.success(approve ? "交接已确认" : "交接已拒绝")
    await getList()
  } catch (error) {
    ElMessage.error(error.message || "交接处理失败")
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

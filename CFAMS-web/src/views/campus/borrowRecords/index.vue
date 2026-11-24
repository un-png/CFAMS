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
        label="借用中"
        name="borrowing"
      />
      <el-tab-pane
        label="临期"
        name="due"
      />
      <el-tab-pane
        label="逾期"
        name="overdue"
      />
      <el-tab-pane
        label="已归还"
        name="returned"
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
      />
      <el-table-column
        label="预计归还"
        prop="plannedEndTime"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        label="实际领取"
        prop="actualPickupTime"
        min-width="170"
        show-overflow-tooltip
      />
      <el-table-column
        label="实际归还"
        prop="actualReturnTime"
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
          <el-tag>{{ displayValue('borrowRecords', 'status', scope.row.status) }}</el-tag>
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
            v-hasPermi="['campus:borrowRecords:query']"
          >详情</el-button>
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
    <el-drawer
      v-model="detailVisible"
      title="借用记录与逾期详情"
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
          page-key="borrowRecords"
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

<script setup name="CampusBorrowRecords">
import {
  ref,
  onBeforeUnmount
} from "vue"
import auth from "@/plugins/auth"
import RecordDetails from "@/components/Campus/RecordDetails.vue"
import {
  displayValue
} from "@/utils/campusDisplay"
import {
  getOptions
} from "@/api/campus/options"
import {
  listBorrowRecords,
  getBorrow
} from "@/api/campus/borrow"

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
const activeTab = ref("all")
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
    borrowing: {
      status: "3"
    },
    due: {
      timing: "dueSoon"
    },
    overdue: {
      timing: "overdue"
    },
    returned: {
      status: "5"
    }
  }
  Object.assign(params, tabs[activeTab.value] || {})
  return params
}

// 查询 → listBorrowRecords → PcBorrowController.records。
async function getList() {
  if (!auth.hasPermi("campus:borrowRecords:list")) return
  const ticket = ++listRequest
  loading.value = true
  listError.value = ""
  try {
    const response = await listBorrowRecords(buildParams())
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
  activeTab.value = "all"
  handleQuery()
}

async function loadDepartments(keyword = "") {
  if (!auth.hasPermi("campus:borrowRecords:list")) return
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
  if (!auth.hasPermi("campus:borrowRecords:query")) return
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

onBeforeUnmount(() => {
  listRequest++
  detailRequest++
  departmentRequest++
})

getList()
loadDepartments()
</script>

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
        <template #default="scope">{{ displayValue('repairRecords', 'faultType', scope.row.faultType) }}</template>
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
      />
      <el-table-column
        label="维修人员"
        prop="repairerName"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="维修费用"
        prop="repairCost"
        min-width="110"
        show-overflow-tooltip
      />
      <el-table-column
        label="核验时间"
        prop="verifyTime"
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
          <el-tag>{{ displayValue('repairRecords', 'status', scope.row.status) }}</el-tag>
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
            v-hasPermi="['campus:repairRecords:query']"
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
      title="维修记录详情"
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
          page-key="repairRecords"
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

<script setup name="CampusRepairRecords">
import {
  ref,
  onBeforeUnmount
} from "vue"
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
  listRepairRecords,
  getRepair,
  getRepairRecords
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

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined
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

  return params
}

// 查询 → listRepairRecords → PcRepairController.records。
async function getList() {
  if (!auth.hasPermi("campus:repairRecords:list")) return
  const ticket = ++listRequest
  loading.value = true
  listError.value = ""
  try {
    const response = await listRepairRecords(buildParams())
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

  handleQuery()
}

async function loadDepartments(keyword = "") {
  if (!auth.hasPermi("campus:repairRecords:list")) return
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
  if (!auth.hasPermi("campus:repairRecords:query")) return
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

onBeforeUnmount(() => {
  listRequest++
  detailRequest++
  departmentRequest++
})

getList()
loadDepartments()
</script>

<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item
        label="报表类型"
        prop="reportType"
      >
        <el-select
          v-model="queryParams.reportType"
          style="width: 170px"
        >
          <el-option
            label="全部业务"
            value="ALL"
          />
          <el-option
            label="资产数量与价值"
            value="ASSET"
          />
          <el-option
            label="借用记录"
            value="BORROW"
          />
          <el-option
            label="报修与费用"
            value="REPAIR"
          />
          <el-option
            label="盘点差异"
            value="INVENTORY"
          />
          <el-option
            label="报废处置"
            value="SCRAP"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="分组维度"
        prop="dimension"
      >
        <el-select
          v-model="queryParams.dimension"
          style="width: 140px"
        >
          <el-option
            label="月份"
            value="MONTH"
          />
          <el-option
            label="类别"
            value="CATEGORY"
          />
          <el-option
            label="部门"
            value="DEPARTMENT"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="资产类别"
        prop="categoryId"
      >
        <el-select
          v-model="queryParams.categoryId"
          clearable
          filterable
          placeholder="全部类别"
          style="width: 180px"
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
        label="所属部门"
        prop="deptId"
      >
        <el-select
          v-model="queryParams.deptId"
          clearable
          filterable
          placeholder="全部部门"
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
      <el-form-item label="使用年限">
        <el-input-number
          v-model="queryParams.minUseYears"
          :min="0"
          :precision="0"
          controls-position="right"
          placeholder="最小年限"
          style="width: 125px"
        />
        <span class="range-separator">至</span>
        <el-input-number
          v-model="queryParams.maxUseYears"
          :min="0"
          :precision="0"
          controls-position="right"
          placeholder="最大年限"
          style="width: 125px"
        />
      </el-form-item>
      <el-form-item label="统计时间">
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
          type="warning"
          plain
          icon="Download"
          :loading="exportLoading"
          v-hasPermi="['campus:reports:export']"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
    <el-alert
      v-if="limitations"
      :title="limitations"
      type="info"
      :closable="false"
      class="mb8"
    />
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      class="mb8"
    />
    <el-table
      v-loading="loading"
      :data="reportList"
    >
      <el-table-column
        label="分组"
        prop="dimension"
        min-width="160"
      />
      <el-table-column
        label="入册数量"
        prop="assetCount"
        align="center"
        min-width="100"
      />
      <el-table-column
        label="登记价值（元）"
        prop="registeredValue"
        align="right"
        min-width="145"
      />
      <el-table-column
        label="借用次数"
        prop="borrowCount"
        align="center"
        min-width="100"
      />
      <el-table-column
        label="报修次数"
        prop="repairCount"
        align="center"
        min-width="100"
      />
      <el-table-column
        label="维修费用（元）"
        prop="repairCost"
        align="right"
        min-width="145"
      />
      <el-table-column
        label="盘点数量"
        prop="inventoryCount"
        align="center"
        min-width="100"
      />
      <el-table-column
        label="盘点差异"
        prop="inventoryDifferenceCount"
        align="center"
        min-width="100"
      />
      <el-table-column
        label="注销数量"
        prop="scrapCount"
        align="center"
        min-width="100"
      />
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="pageNum"
      v-model:limit="pageSize"
    />
  </div>
</template>

<script setup name="CampusReports">
import {
  computed,
  ref,
  reactive,
  getCurrentInstance,
  onBeforeUnmount
} from 'vue'
import {
  listReports,
  exportReports
} from '@/api/campus/statistics'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const queryParams = reactive({
  reportType: 'ALL',
  dimension: 'MONTH',
  categoryId: undefined,
  deptId: undefined,
  minUseYears: undefined,
  maxUseYears: undefined
})
const dateRange = ref([]),
  categories = ref([]),
  departments = ref([])
const allRows = ref([]),
  pageNum = ref(1),
  pageSize = ref(10)
const loading = ref(false),
  exportLoading = ref(false),
  showSearch = ref(true),
  error = ref(''),
  limitations = ref('')
const total = computed(() => allRows.value.length)
// 报表接口返回全部分组，前端仅对展示结果分页。
const reportList = computed(() => allRows.value.slice((pageNum.value - 1) * pageSize.value, pageNum.value * pageSize
  .value))
let requestId = 0

function buildQuery() {
  const result = {
    ...queryParams
  }
  if (dateRange.value?.length === 2) {
    const next = new Date(`${dateRange.value[1]}T00:00:00`)
    next.setDate(next.getDate() + 1)
    result.beginTime = `${dateRange.value[0]} 00:00:00`
    result.endTime =
      `${next.getFullYear()}-${String(next.getMonth() + 1).padStart(2, '0')}-${String(next.getDate()).padStart(2, '0')} 00:00:00`
  }
  return result
}

function validQuery() {
  if (queryParams.minUseYears != null && queryParams.maxUseYears != null && queryParams.minUseYears > queryParams
    .maxUseYears) {
    proxy.$modal.msgWarning('最小使用年限不能大于最大年限');
    return false
  }
  return true
}
async function getList() {
  if (!validQuery()) return
  const current = ++requestId
  loading.value = true
  error.value = ''
  try {
    const response = await listReports(buildQuery())
    if (current !== requestId) return
    allRows.value = response.data || []
    limitations.value = response.limitations || ''
    pageNum.value = 1
  } catch (err) {
    if (current === requestId) {
      allRows.value = [];
      error.value = err.message || '报表加载失败'
    }
  } finally {
    if (current === requestId) loading.value = false
  }
}

function handleQuery() {
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.minUseYears = undefined
  queryParams.maxUseYears = undefined
  dateRange.value = []
  handleQuery()
}
async function handleExport() {
  if (!validQuery() || exportLoading.value) return
  exportLoading.value = true
  try {
    await exportReports(buildQuery())
  } catch (err) {
    proxy.$modal.msgError(err.message)
  } finally {
    exportLoading.value = false
  }
}
async function loadOptions() {
  const results = await Promise.allSettled([
    getOptions('categories').then(res => {
      categories.value = res.data
    }),
    getOptions('departments').then(res => {
      departments.value = res.data
    })
  ])
  if (results.some(item => item.status === 'rejected')) proxy.$modal.msgWarning('部分筛选选项加载失败')
}
onBeforeUnmount(() => {
  requestId++
})
getList()
loadOptions()
</script>

<style scoped>
.range-separator {
  margin: 0 8px;
}
</style>

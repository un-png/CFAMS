<template>
  <div class="app-container campus-dashboard">
    <el-form
      :inline="true"
      :model="filters"
      @submit.prevent="applyFilters"
    >
      <el-form-item label="所属部门">
        <el-select
          v-model="filters.dept"
          placeholder="全部授权部门"
          clearable
          :loading="departmentsLoading"
          style="width:200px"
        >
          <el-option
            v-for="dept in departments"
            :key="dept.value"
            :label="dept.label"
            :value="dept.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="统计时间">
        <el-date-picker
          v-model="filters.period"
          value-format="YYYY-MM-DD"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          range-separator="至"
        />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="Search"
          @click="applyFilters"
        >查询</el-button>
        <el-button
          icon="Refresh"
          @click="resetFilters"
        >重置</el-button>
      </el-form-item>
    </el-form>
    <p
      v-if="filterSummary"
      class="filter-summary"
    >{{ filterSummary }}</p>
    <el-alert
      v-if="departmentsError"
      title="部门列表加载失败"
      type="warning"
      :closable="false"
    >
      <el-button
        link
        @click="loadDepartments"
      >重试</el-button>
    </el-alert>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
    >
      <el-button
        link
        @click="applyFilters"
      >重试</el-button>
    </el-alert>
    <el-row
      :gutter="20"
      v-loading="loading"
      class="mb20"
    >
      <el-col
        v-for="item in metrics"
        :key="item.label"
        :xs="24"
        :sm="12"
        :lg="6"
      >
        <el-card
          shadow="hover"
          class="metric-card"
        >
          <div class="metric">
            <el-icon
              :style="{ color: item.color }"
              :size="32"
            >
              <component :is="item.icon" />
            </el-icon>
            <div>
              <span>{{ item.label }}</span>
              <strong>{{ metricValue(item.key) }}<small>{{ item.unit }}</small>
              </strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card
      v-if="availableTodos.length"
      shadow="never"
      class="mb20"
    >
      <template #header>
        <span>管理待办</span>
      </template>
      <div class="work-links">
        <router-link
          v-for="item in availableTodos"
          :key="item.path"
          :to="item.path"
        >
          <span>{{ item.label }}</span>
          <span>{{ pendingValue(item.key) }} <el-icon>
              <ArrowRight />
            </el-icon>
          </span>
        </router-link>
      </div>
    </el-card>
    <el-row :gutter="20">
      <el-col
        v-for="chart in charts"
        :key="chart.key"
        :xs="24"
        :lg="12"
      >
        <el-card
          shadow="never"
          class="mb20"
        >
          <template #header>
            <span>{{ chart.title }}</span>
          </template>
          <div
            :ref="el => setContainer(chart.key, el)"
            class="chart"
          />
        </el-card>
      </el-col>
    </el-row>
    <section class="ratios">
      <div>期间借用率 <strong>{{ data?.rates?.borrowRate == null ? '暂无数据' : data.rates.borrowRate+'%' }}</strong>
      </div>
      <div>期间报修率 <strong>{{ data?.rates?.repairRate == null ? '暂无数据' : data.rates.repairRate+'%' }}</strong>
      </div>
      <div>核验维修费用 <strong>{{ data ? (data.trend||[]).reduce((n,row)=>n+Number(row.repairCost||0),0).toFixed(2) : '—' }}
          <small>元</small>
        </strong>
      </div>
    </section>
    <el-collapse v-if="data?.limitations">
      <el-collapse-item
        title="统计口径"
        name="scope"
      >{{ data.limitations }}</el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup name="CampusDashboard">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref
} from 'vue'
import usePermissionStore from '@/store/modules/permission'
import {
  Box,
  Coin,
  Switch,
  Tickets,
  ArrowRight
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  ElMessage
} from 'element-plus'
import {
  getOptions
} from '@/api/campus/options'
import {
  getDashboard
} from '@/api/campus/statistics'

const filters = ref({})
const appliedFilters = ref(null)
const departments = ref([])
const departmentsLoading = ref(false)
const departmentsError = ref(false)
const data = ref(null),
  loading = ref(false),
  error = ref('')
let requestTicket = 0,
  disposed = false
const filterSummary = computed(() => {
  if (!appliedFilters.value) return ''
  const dept = departments.value.find(item => item.value === appliedFilters.value.dept)?.label || '全部授权部门'
  const period = appliedFilters.value.period?.join(' 至 ') || '近一年（默认统计区间）'
  return `${dept} · ${period}`
})
async function loadDepartments() {
  departmentsLoading.value = true
  departmentsError.value = false
  try {
    const response = await getOptions('departments')
    departments.value = response.data || []
  } catch {
    departmentsError.value = true
  } finally {
    departmentsLoading.value = false
  }
}
async function applyFilters() {
  const period = filters.value.period
  if (period?.length && (period.length !== 2 || period[0] > period[1])) {
    ElMessage.warning('请选择正确的统计时间范围')
    return
  }
  appliedFilters.value = {
    ...filters.value,
    period: period ? [...period] : null
  }
  const ticket = ++requestTicket
  loading.value = true;
  error.value = ''
  try {
    const params = {
      deptId: filters.value.dept
    }
    if (period?.length === 2) {
      const end = new Date(`${period[1]}T00:00:00`)
      end.setDate(end.getDate() + 1)
      params.beginTime = `${period[0]} 00:00:00`
      params.endTime =
        `${end.getFullYear()}-${String(end.getMonth()+1).padStart(2,'0')}-${String(end.getDate()).padStart(2,'0')} 00:00:00`
    }
    const response = await getDashboard(params)
    if (ticket !== requestTicket || disposed) return
    data.value = response.data;
    renderCharts()
  } catch (err) {
    if (ticket === requestTicket) {
      error.value = err?.message || '统计加载失败';
      data.value = null;
      renderCharts()
    }
  } finally {
    if (ticket === requestTicket) loading.value = false
  }
}

function resetFilters() {
  filters.value = {}
  appliedFilters.value = null
  applyFilters()
}
const metrics = [{
    key: 'assetCount',
    label: '有效在册资产',
    unit: '件',
    icon: Box,
    color: '#409eff'
  },
  {
    key: 'registeredValue',
    label: '登记价值',
    unit: '元',
    icon: Coin,
    color: '#319c7b'
  },
  {
    key: 'borrowedCount',
    label: '当前借出',
    unit: '件',
    icon: Switch,
    color: '#c48c35'
  },
  {
    key: 'pending',
    label: '待处理业务',
    unit: '项',
    icon: Tickets,
    color: '#8b6db1'
  }
]
const todos = [{
    key: 'borrowAudit',
    label: '借用待审核',
    path: '/campus/borrow/borrow'
  },
  {
    key: 'handover',
    label: '交接待确认',
    path: '/campus/borrow/handover'
  },
  {
    key: 'repairVerification',
    label: '维修待核验',
    path: '/campus/repair/repairVerify'
  },
  {
    key: 'inventoryReview',
    label: '盘点待复核',
    path: '/campus/inventory/inventoryReview'
  },
  {
    key: 'scrapAudit',
    label: '报废待审核',
    path: '/campus/scrap/scrapAudit'
  }
]
const permissionStore = usePermissionStore()
const availableTodos = computed(() => todos.filter(item => {
  const parts = item.path.split('/')
  const parent = permissionStore.addRoutes.find(route => route.path === parts.slice(0, -1).join('/'))
  return parent && !parent.hidden && parent.children?.some(child => child.path === parts.at(-1) && !child.hidden)
}))
const charts = [{
  key: 'category',
  title: '资产类别分布'
}, {
  key: 'dept',
  title: '部门资产分布'
}, {
  key: 'trend',
  title: '借用与报修趋势'
}, {
  key: 'cost',
  title: '维修费用趋势'
}]
const containers = new Map()
const instances = []

function metricValue(key) {
  if (!data.value) return '—';
  return key === 'pending' ? Object.values(data.value.pending || {}).reduce((n, v) => n + Number(v || 0), 0) : Number(
    data.value.stock?.[key] || 0).toLocaleString('zh-CN', {
    maximumFractionDigits: 2
  })
}

function pendingValue(key) {
  if (!data.value) return '—';
  return key === 'handover' ? Number(data.value.pending?.pickupHandover || 0) + Number(data.value.pending
    ?.returnHandover || 0) : data.value.pending?.[key] ?? 0
}

function renderCharts() {
  const current = data.value
  const distributions = [current?.categories || [], current?.departments || []]
  instances.forEach((chart, index) => {
    const values = index < 2 ? distributions[index] : current?.trend || []
    chart.setOption({
      animation: false,
      tooltip: {
        trigger: 'axis'
      },
      legend: index === 2 ? {
        data: ['借用', '报修']
      } : undefined,
      graphic: values.length ? [] : [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: error.value ? '统计加载失败' : '暂无统计数据',
          fill: '#909399',
          font: '14px sans-serif'
        }
      }],
      grid: {
        left: 55,
        right: 20,
        bottom: 55,
        top: 30
      },
      xAxis: {
        type: 'category',
        data: values.map(v => index < 2 ? v.name : v.dimension),
        axisLabel: {
          overflow: 'truncate',
          width: 90
        }
      },
      yAxis: {
        type: 'value',
        minInterval: index === 3 ? undefined : 1
      },
      series: index < 2 ? [{
        type: 'bar',
        barMaxWidth: 34,
        data: values.map(v => v.count),
        itemStyle: {
          color: index === 0 ? '#409eff' : '#319c7b'
        }
      }] : index === 2 ? [{
        name: '借用',
        type: 'line',
        data: values.map(v => v.borrowCount)
      }, {
        name: '报修',
        type: 'line',
        data: values.map(v => v.repairCount)
      }] : [{
        name: '维修费用',
        type: 'bar',
        barMaxWidth: 34,
        data: values.map(v => v.repairCost),
        itemStyle: {
          color: '#c48c35'
        }
      }]
    }, true)
  })
}
let observer

function setContainer(key, el) {
  if (el) containers.set(key, el)
}
onMounted(async () => {
  loadDepartments()
  await nextTick()
  containers.forEach(el => {
    const instance = echarts.init(el)
    instance.setOption({
      animation: false,
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无统计数据',
          fill: '#909399',
          font: '14px sans-serif'
        }
      }],
      grid: {
        left: 45,
        right: 20,
        bottom: 32,
        top: 20
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLine: {
          lineStyle: {
            color: '#dcdfe6'
          }
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          show: false
        },
        splitLine: {
          lineStyle: {
            color: '#ebeef5',
            type: 'dashed'
          }
        }
      },
      series: []
    })
    instances.push(instance)
  })
  observer = new ResizeObserver(() => instances.forEach(chart => chart.resize()))
  containers.forEach(el => observer.observe(el))
  applyFilters()
})
onBeforeUnmount(() => {
  disposed = true;
  requestTicket++;
  observer?.disconnect();
  instances.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.filter-summary {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0 0 18px;
}

.metric-card {
  margin-bottom: 12px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 20px;
  min-height: 80px;
}

.metric span {
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 12px;
  font-size: 26px;
}

small {
  font-size: 12px;
  font-weight: 400;
  margin-left: 8px;
  color: var(--el-text-color-secondary);
}

.work-links {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px;
}

.work-links a {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-regular);
  gap: 8px;
}

.work-links a:hover {
  color: var(--el-color-primary);
}

.work-links a span:last-child {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart {
  height: 245px;
  width: 100%;
}

.ratios {
  display: flex;
  gap: 36px;
  flex-wrap: wrap;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: 20px;
  padding: 24px 0;
  font-size: 14px;
}

.ratios strong {
  margin-left: 16px;
}

@media(max-width:900px) {
  .work-links {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

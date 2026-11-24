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
        label="单号 / 资产"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="申请人"
        prop="applicant"
      >
        <el-input
          v-model="queryParams.applicant"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="所属部门"
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
      <el-form-item label="申请时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
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
    <el-tabs
      v-model="activeTab"
      @tab-change="handleQuery"
    >
      <el-tab-pane
        label="待初审"
        name="0"
      />
      <el-tab-pane
        label="待复审"
        name="1"
      />
      <el-tab-pane
        label="已处理"
        name="handled"
      />
    </el-tabs>
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
        @click="loadDepartments"
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
      :data="scrapList"
    >
      <el-table-column
        label="处置单号"
        prop="scrapNo"
        min-width="190"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产编号"
        prop="assetCode"
        min-width="140"
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="150"
      />
      <el-table-column
        label="处置类型"
        width="100"
      >
        <template #default="{ row }">{{ row.disposeType === '1' ? '报废' : '丢失注销' }}</template>
      </el-table-column>
      <el-table-column
        label="所属部门"
        prop="deptName"
        min-width="120"
      />
      <el-table-column
        label="申请人"
        prop="applicantName"
        min-width="110"
      />
      <el-table-column
        label="申请时间"
        prop="applyTime"
        width="170"
      />
      <el-table-column
        label="审核状态"
        width="110"
      >
        <template #default="{ row }">{{ statuses[row.status] }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        width="150"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPermi="['campus:scrapAudit:query']"
            link
            type="primary"
            :disabled="busy"
            @click="handleDetail(row)"
          >查看</el-button>
          <el-button
            v-if="row.status === '0' && !isApplicant(row)"
            v-hasPermi="['campus:scrapAudit:firstAudit']"
            link
            type="primary"
            :disabled="busy"
            @click="handleFirstAudit(row)"
          >初审</el-button>
          <el-button
            v-if="row.status === '1' && !isApplicant(row)"
            v-hasPermi="['campus:scrapAudit:finalAudit']"
            link
            type="primary"
            :disabled="busy"
            @click="handleFinalAudit(row)"
          >复审</el-button>
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
      :title="auditLevel === '1' ? '报废初审' : auditLevel === '2' ? '报废复审' : '报废申请详情'"
      width="900px"
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
          <el-descriptions-item label="处置单号">{{ record.scrapNo }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ statuses[record.status] }}</el-descriptions-item>
          <el-descriptions-item label="资产编号">{{ record.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ record.assetName }}</el-descriptions-item>
          <el-descriptions-item label="处置类型">{{ record.disposeType === '1' ? '报废' : '丢失注销' }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ record.deptName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ record.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ record.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="注销时间">{{ record.completedTime || '—' }}</el-descriptions-item>
          <el-descriptions-item
            label="处置原因"
            :span="2"
          >{{ record.reason }}</el-descriptions-item>
        </el-descriptions>
        <h3>证明材料</h3>
        <business-attachments
          :key="record.scrapId"
          biz-type="SCRAP"
          :biz-id="record.scrapId"
        />
        <h3>两级审核记录</h3>
        <el-table :data="record.audits || []">
          <el-table-column
            label="级别"
            width="90"
          >
            <template #default="{ row }">{{ row.auditLevel === '1' ? '初审' : '复审' }}</template>
          </el-table-column>
          <el-table-column
            label="结果"
            width="90"
          >
            <template #default="{ row }">{{ row.auditResult === '1' ? '通过' : '驳回' }}</template>
          </el-table-column>
          <el-table-column label="审核人">
            <template #default="{ row }">{{ row.auditorName || `#${row.auditorUserId}` }}</template>
          </el-table-column>
          <el-table-column
            label="审核意见"
            prop="auditOpinion"
            min-width="200"
          />
          <el-table-column
            label="审核时间"
            prop="auditTime"
            width="170"
          />
        </el-table>
        <template v-if="auditLevel">
          <el-alert
            :title="auditLevel === '2' ? '复审通过后资产将注销。复审人必须与初审人不同；未完成借还或维修业务将阻止通过。' : '请核对处置原因及证明材料。初审通过后进入复审。'"
            type="warning"
            :closable="false"
            class="mb8"
          />
          <el-form
            ref="auditRef"
            :model="form"
            :rules="rules"
            label-width="100px"
            :disabled="busy"
          >
            <el-form-item label="审核结论">
              <el-radio-group v-model="form.approved">
                <el-radio :value="true">通过</el-radio>
                <el-radio :value="false">驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              label="审核意见"
              prop="opinion"
            >
              <el-input
                v-model="form.opinion"
                type="textarea"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </template>
      </template>
      <template #footer>
        <el-button
          v-if="auditLevel === '1'"
          v-hasPermi="['campus:scrapAudit:firstAudit']"
          type="primary"
          :loading="busy"
          @click="submitFirstAudit"
        >提交初审</el-button>
        <el-button
          v-if="auditLevel === '2'"
          v-hasPermi="['campus:scrapAudit:finalAudit']"
          type="primary"
          :loading="busy"
          @click="submitFinalAudit"
        >提交复审</el-button>
        <el-button
          :disabled="busy"
          @click="open = false"
        >关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusScrapAudit">
import {
  ref,
  reactive,
  nextTick
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import useUserStore from '@/store/modules/user'
import BusinessAttachments from '@/components/Campus/BusinessAttachments.vue'
import {
  getOptions
} from '@/api/campus/options'
import {
  listScrap,
  getScrap,
  firstAuditScrap,
  finalAuditScrap
} from '@/api/campus/scrap'

const user = useUserStore(),
  statuses = ['待初审', '待复审', '已驳回', '已注销']
const queryRef = ref(),
  auditRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  applicant: '',
  deptId: undefined
})
const showSearch = ref(true),
  dateRange = ref([]),
  activeTab = ref('0'),
  loading = ref(false),
  busy = ref(false)
const scrapList = ref([]),
  total = ref(0),
  listError = ref(''),
  departments = ref([]),
  optionError = ref('')
const open = ref(false),
  record = ref(null),
  auditLevel = ref(''),
  form = ref({
    approved: true,
    opinion: ''
  })
const rules = {
  opinion: [{
    required: true,
    whitespace: true,
    message: '请填写审核意见',
    trigger: 'blur'
  }]
}
let listTicket = 0

function closeDialog(done) {
  if (!busy.value) done()
}

function isApplicant(row) {
  return String(row.applicantUserId) === String(user.id)
}
async function loadDepartments() {
  optionError.value = ''
  try {
    departments.value = (await getOptions('departments')).data || []
  } catch {
    optionError.value = '部门选项加载失败，请重试'
  }
}
async function getList() {
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listScrap({
      ...queryParams,
      status: activeTab.value === 'handled' ? undefined : activeTab.value,
      handled: activeTab.value === 'handled' ? true : undefined,
      beginTime: dateRange.value?.[0] ? `${dateRange.value[0]} 00:00:00` : undefined,
      endTime: dateRange.value?.[1] ? `${dateRange.value[1]} 23:59:59` : undefined
    })
    if (ticket !== listTicket) return
    scrapList.value = response.rows || []
    total.value = Number(response.total || 0)
  } catch (error) {
    if (ticket === listTicket) {
      scrapList.value = [];
      total.value = 0;
      listError.value = error.message || '审核列表加载失败'
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

function auditProblem(data, level) {
  if (data.status !== (level === '1' ? '0' : '1')) return '单据状态已变化，请刷新列表'
  if (isApplicant(data)) return '申请人不能审核自己的单据'
  if (level === '2') {
    const first = data.audits?.find(item => item.auditLevel === '1' && item.auditResult === '1')
    if (!first) return '尚未通过初审'
    if (String(first.auditorUserId) === String(user.id)) return '初审和复审必须由不同人员办理'
  }
  return ''
}
async function loadRecord(row, level) {
  if (busy.value) return
  busy.value = true
  try {
    const data = (await getScrap(row.scrapId)).data
    if (level) {
      const problem = auditProblem(data, level)
      if (problem) return ElMessage.warning(problem)
    }
    record.value = data
    auditLevel.value = level
    form.value = {
      approved: true,
      opinion: ''
    }
    open.value = true
    await nextTick()
    auditRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(error.message || '申请详情加载失败')
  } finally {
    busy.value = false
  }
}

function handleDetail(row) {
  return loadRecord(row, '')
}

function handleFirstAudit(row) {
  return loadRecord(row, '1')
}

function handleFinalAudit(row) {
  return loadRecord(row, '2')
}
async function submitAudit(level, action) {
  if (busy.value || auditLevel.value !== level || !await auditRef.value.validate().catch(() => false)) return
  const problem = auditProblem(record.value, level)
  if (problem) return ElMessage.warning(problem)
  busy.value = true
  try {
    const message = !form.value.approved ? '确认驳回该处置申请？' : level === '1' ? '确认初审通过并转入复审？' : '确认复审通过并注销资产？'
    await ElMessageBox.confirm(message, '审核确认', {
      type: 'warning'
    })
    await action(record.value.scrapId, {
      approved: form.value.approved,
      opinion: form.value.opinion.trim()
    })
    ElMessage.success('审核成功')
    open.value = false
    await getList()
  } catch (error) {
    if (!['cancel', 'close'].includes(error)) ElMessage.error(error.message || '审核失败')
  } finally {
    busy.value = false
  }
}

function submitFirstAudit() {
  return submitAudit('1', firstAuditScrap)
}

function submitFinalAudit() {
  return submitAudit('2', finalAuditScrap)
}
onBeforeRouteLeave(() => !busy.value)
getList()
loadDepartments()
</script>

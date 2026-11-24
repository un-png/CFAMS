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
      <el-form-item
        label="处置类型"
        prop="disposeType"
      >
        <el-select
          v-model="queryParams.disposeType"
          clearable
          style="width: 150px"
        >
          <el-option
            label="报废"
            value="1"
          />
          <el-option
            label="丢失注销"
            value="2"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="审核状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          clearable
          style="width: 150px"
        >
          <el-option
            v-for="(label, value) in statuses"
            :key="value"
            :label="label"
            :value="String(value)"
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
    <el-row
      :gutter="10"
      class="mb8"
    >
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['campus:scrap:add']"
          type="primary"
          plain
          icon="Plus"
          :disabled="busy"
          @click="handleAdd"
        >新增申请</el-button>
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
        width="180"
        align="center"
      >
        <template #default="{ row }">
          <el-button
            v-hasPermi="['campus:scrap:query']"
            link
            type="primary"
            :disabled="busy"
            @click="handleDetail(row)"
          >详情</el-button>
          <el-button
            v-if="canUpload(row)"
            v-hasPermi="['campus:scrap:add']"
            link
            type="primary"
            :disabled="busy"
            @click="handleAttachment(row)"
          >补充证明</el-button>
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
      :title="attachmentOnly ? '补充证明材料' : '新增报废申请'"
      width="700px"
      append-to-body
      :close-on-click-modal="false"
      :before-close="closeDialog"
    >
      <el-alert
        v-if="savedId && !attachmentOnly"
        :title="`申请 #${savedId} 已创建。继续上传剩余附件不会重复建单；也可关闭后从列表补充证明。`"
        type="warning"
        :closable="false"
        class="mb8"
      />
      <el-alert
        v-if="uploadError"
        :title="uploadError"
        type="error"
        :closable="false"
        class="mb8"
      />
      <el-form
        v-if="!attachmentOnly"
        ref="scrapRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="busy || !!savedId"
      >
        <el-form-item
          label="处置资产"
          prop="assetId"
        >
          <el-select
            v-model="form.assetId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchAssets"
            :loading="assetLoading"
            placeholder="输入资产编号或名称"
          >
            <el-option
              v-for="item in assets"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="处置类型"
          prop="disposeType"
        >
          <el-radio-group v-model="form.disposeType">
            <el-radio value="1">报废</el-radio>
            <el-radio value="2">丢失注销</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="处置原因"
          prop="reason"
        >
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <el-upload
        action=""
        :auto-upload="false"
        :file-list="files"
        accept=".jpg,.jpeg,.png,.pdf"
        :limit="9"
        :disabled="busy"
        :on-change="chooseFile"
        :on-remove="removeFile"
        :on-exceed="exceedFiles"
      >
        <el-button
          icon="Upload"
          :disabled="busy"
        >选择证明材料</el-button>
        <template #tip>
          <div class="el-upload__tip">支持 JPG、PNG、PDF；每批最多9个，单个不超过10MB。审核通过前须有真实证明附件。</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button
          v-hasPermi="['campus:scrap:add']"
          type="primary"
          :loading="busy"
          @click="submitForm"
        >{{ savedId ? '上传附件' : '保存申请' }}</el-button>
        <el-button
          :disabled="busy"
          @click="open = false"
        >关 闭</el-button>
      </template>
    </el-dialog>
  
    <el-dialog
      v-model="detailOpen"
      title="报废申请详情"
      width="900px"
      append-to-body
      destroy-on-close
    >
      <template v-if="detail">
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="处置单号">{{ detail.scrapNo }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">{{ statuses[detail.status] }}</el-descriptions-item>
          <el-descriptions-item label="资产编号">{{ detail.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ detail.assetName }}</el-descriptions-item>
          <el-descriptions-item label="处置类型">{{ detail.disposeType === '1' ? '报废' : '丢失注销' }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ detail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="注销时间">{{ detail.completedTime || '—' }}</el-descriptions-item>
          <el-descriptions-item
            label="处置原因"
            :span="2"
          >{{ detail.reason }}</el-descriptions-item>
        </el-descriptions>
        <h3>证明材料</h3>
        <business-attachments
          :key="detail.scrapId"
          biz-type="SCRAP"
          :biz-id="detail.scrapId"
        />
        <h3>两级审核记录</h3>
        <el-table :data="detail.audits || []">
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
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusScrap">
import {
  ref,
  reactive,
  nextTick
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import {
  ElMessage
} from 'element-plus'
import useUserStore from '@/store/modules/user'
import BusinessAttachments from '@/components/Campus/BusinessAttachments.vue'
import {
  getOptions
} from '@/api/campus/options'
import {
  uploadAttachment
} from '@/api/campus/attachment'
import {
  listScrap,
  getScrap,
  addScrap
} from '@/api/campus/scrap'

const user = useUserStore()
const statuses = ['待初审', '待复审', '已驳回', '已注销']
const queryRef = ref(),
  scrapRef = ref()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  deptId: undefined,
  disposeType: undefined,
  status: undefined
})
const showSearch = ref(true),
  dateRange = ref([]),
  loading = ref(false),
  busy = ref(false)
const scrapList = ref([]),
  total = ref(0),
  listError = ref(''),
  optionError = ref('')
const departments = ref([]),
  assets = ref([]),
  assetLoading = ref(false)
const open = ref(false),
  attachmentOnly = ref(false),
  savedId = ref(),
  form = ref({}),
  files = ref([]),
  uploadError = ref('')
const detailOpen = ref(false),
  detail = ref(null)
let listTicket = 0,
  assetTicket = 0
// 成功文件按 uid 移出待传列表；部分失败保留单据 ID，重试只续传剩余文件。
const uploadedIds = new Set()
const rules = {
  assetId: [{
    required: true,
    message: '请选择处置资产',
    trigger: 'change'
  }],
  disposeType: [{
    required: true,
    message: '请选择处置类型',
    trigger: 'change'
  }],
  reason: [{
    required: true,
    whitespace: true,
    message: '请输入处置原因',
    trigger: 'blur'
  }]
}

function canUpload(row) {
  return row.status === '0' && String(row.applicantUserId) === String(user.id)
}

function closeDialog(done) {
  if (!busy.value) done()
}
async function getList() {
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listScrap({
      ...queryParams,
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
      listError.value = error.message || '申请列表加载失败'
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
  try {
    departments.value = (await getOptions('departments')).data || []
  } catch {
    optionError.value = '部门选项加载失败，请重试'
  }
}
async function searchAssets(keyword) {
  const ticket = ++assetTicket
  assetLoading.value = true
  try {
    const response = await getOptions('assets', keyword)
    if (ticket !== assetTicket) return
    const selected = assets.value.filter(item => String(item.value) === String(form.value.assetId))
    assets.value = [...new Map([...selected, ...(response.data || [])].map(item => [String(item.value), item]))
      .values()
    ]
  } catch {
    if (ticket === assetTicket) ElMessage.error('资产选项加载失败，请重新搜索')
  } finally {
    if (ticket === assetTicket) assetLoading.value = false
  }
}

function resetUpload() {
  files.value = [];
  uploadedIds.clear();
  uploadError.value = ''
}

function handleAdd() {
  attachmentOnly.value = false
  savedId.value = undefined
  form.value = {
    assetId: undefined,
    disposeType: '1',
    reason: ''
  }
  resetUpload()
  open.value = true
  nextTick(() => scrapRef.value?.clearValidate())
  searchAssets('')
}

function handleAttachment(row) {
  attachmentOnly.value = true
  savedId.value = row.scrapId
  resetUpload()
  open.value = true
}

function chooseFile(file, list) {
  const raw = file.raw
  const valid = raw && raw.size > 0 && raw.size <= 10 * 1024 * 1024 && /\.(jpg|jpeg|png|pdf)$/i.test(raw.name)
  if (!valid) ElMessage.warning('请选择非空 JPG、PNG 或 PDF 文件，单个不超过10MB')
  files.value = list.filter(item => !uploadedIds.has(item.uid) && (valid || item.uid !== file.uid))
}

function removeFile(_, list) {
  files.value = list.filter(item => !uploadedIds.has(item.uid))
}

function exceedFiles() {
  ElMessage.warning('每批最多选择9个附件')
}
async function submitForm() {
  if (busy.value) return
  if (!savedId.value && !await scrapRef.value.validate().catch(() => false)) return
  if (savedId.value && !files.value.length) return ElMessage.warning('请选择需要上传的证明材料')
  busy.value = true
  uploadError.value = ''
  try {
    if (!savedId.value) {
      const response = await addScrap({
        assetId: form.value.assetId,
        disposeType: form.value.disposeType,
        reason: form.value.reason.trim()
      })
      savedId.value = response.data
    }
    for (const file of [...files.value]) {
      if (uploadedIds.has(file.uid)) continue
      await uploadAttachment('SCRAP', savedId.value, file.raw)
      uploadedIds.add(file.uid)
      files.value = files.value.filter(item => item.uid !== file.uid)
    }
    ElMessage.success(attachmentOnly.value ? '证明材料上传成功' : '申请保存成功')
    open.value = false
    await getList()
  } catch (error) {
    uploadError.value = savedId.value ? `单据 #${savedId.value} 已存在，剩余附件未上传：${error.message || '请重试'}` : error
      .message || '申请保存失败'
    if (savedId.value) await getList()
  } finally {
    busy.value = false
  }
}
async function handleDetail(row) {
  busy.value = true
  try {
    detail.value = (await getScrap(row.scrapId)).data;
    detailOpen.value = true
  } catch (error) {
    ElMessage.error(error.message || '详情加载失败')
  } finally {
    busy.value = false
  }
}
onBeforeRouteLeave(() => !busy.value)
getList()
loadOptions()
</script>

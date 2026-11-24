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
        label="学工号 / 姓名"
        prop="keyword"
      >
        <el-input
          v-model.trim="queryParams.keyword"
          placeholder="请输入学工号或姓名"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="所属部门"
        prop="deptId"
      >
        <el-select
          v-model="queryParams.deptId"
          placeholder="请选择部门"
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
        label="人员类型"
        prop="personType"
      >
        <el-select
          v-model="queryParams.personType"
          placeholder="请选择类型"
          clearable
          style="width: 160px"
        >
          <el-option
            label="学生"
            value="1"
          />
          <el-option
            label="教职工"
            value="2"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 160px"
        >
          <el-option
            label="有效"
            value="0"
          />
          <el-option
            label="停用"
            value="1"
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
    <el-row
      :gutter="10"
      class="mb8"
    >
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          :disabled="busy"
          @click="handleAdd"
          v-hasPermi="['campus:people:add']"
        >新增人员</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single || busy"
          @click="handleUpdate()"
          v-hasPermi="['campus:people:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          :disabled="busy"
          @click="handleImport"
          v-hasPermi="['campus:people:import']"
        >导入名册</el-button>
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
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
        type="primary"
        @click="loadOptions"
      >重试选项</el-button>
    </el-alert>
    <el-table
      v-loading="loading"
      :data="peopleList"
      row-key="personId"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="学号 / 工号"
        prop="personNo"
        min-width="140"
        align="center"
      />
      <el-table-column
        label="姓名"
        prop="realName"
        min-width="100"
        align="center"
      />
      <el-table-column
        label="人员类型"
        align="center"
        width="100"
      >
        <template #default="{ row }">{{ row.personType === '1' ? '学生' : row.personType === '2' ? '教职工' : '—' }}</template>
      </el-table-column>
      <el-table-column
        label="所属部门"
        prop="deptName"
        min-width="140"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column
        label="关联账号"
        prop="userName"
        min-width="120"
        align="center"
      />
      <el-table-column
        label="绑定状态"
        align="center"
        width="110"
      >
        <template #default="{ row }">{{ bindingLabels[row.bindingState] || '—' }}</template>
      </el-table-column>
      <el-table-column
        label="状态"
        align="center"
        width="90"
      >
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '有效' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        width="170"
        class-name="small-padding fixed-width"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            icon="Edit"
            :disabled="busy"
            @click="handleUpdate(row)"
            v-hasPermi="['campus:people:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            :disabled="busy"
            @click="handleStatusChange(row)"
            v-hasPermi="['campus:people:edit']"
          >{{ row.status === '0' ? '停用' : '启用' }}</el-button>
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
      :title="title"
      v-model="open"
      width="560px"
      append-to-body
      :close-on-click-modal="!busy"
      :close-on-press-escape="!busy"
      :show-close="!busy"
      @closed="reset"
    >
      <el-form
        ref="personRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        :disabled="busy"
      >
        <el-form-item
          label="学号 / 工号"
          prop="personNo"
        >
          <el-input
            v-model.trim="form.personNo"
            maxlength="32"
            placeholder="请输入学号或工号"
          />
        </el-form-item>
        <el-form-item
          label="姓名"
          prop="realName"
        >
          <el-input
            v-model.trim="form.realName"
            maxlength="50"
            placeholder="请输入姓名"
          />
        </el-form-item>
        <el-form-item
          label="人员类型"
          prop="personType"
        >
          <el-select
            v-model="form.personType"
            placeholder="请选择人员类型"
            style="width: 100%"
          >
            <el-option
              label="学生"
              value="1"
            />
            <el-option
              label="教职工"
              value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="所属部门"
          prop="deptId"
        >
          <el-select
            v-model="form.deptId"
            filterable
            placeholder="请选择部门"
            style="width: 100%"
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
          label="关联账号"
          prop="userId"
        >
          <el-select
            v-model="form.userId"
            clearable
            filterable
            remote
            :remote-method="loadUsers"
            :loading="usersLoading"
            :disabled="accountLocked"
            placeholder="输入账号或姓名搜索"
            style="width: 100%"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <span
            v-if="accountLocked"
            class="form-tip"
          >已关联账号不可在名册编辑中替换或解除</span>
        </el-form-item>
        <el-form-item
          label="状态"
          prop="status"
        >
          <el-select
            v-model="form.status"
            style="width: 100%"
          >
            <el-option
              label="有效"
              value="0"
            />
            <el-option
              label="停用"
              value="1"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="备注"
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
      title="导入人员名册"
      v-model="importOpen"
      width="620px"
      append-to-body
      :close-on-click-modal="!busy"
      :close-on-press-escape="!busy"
      :show-close="!busy"
    >
      <el-button
        type="primary"
        link
        icon="Download"
        :loading="templateLoading"
        @click="handleTemplate"
      >下载导入模板</el-button>
      <el-upload
        class="import-upload"
        action=""
        drag
        :auto-upload="false"
        :file-list="importFiles"
        accept=".xlsx,.xls"
        :limit="1"
        :disabled="busy"
        :on-change="chooseImport"
        :on-remove="removeImport"
        :on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">支持 xls、xlsx，最大 10MB，1 至 5000 行。存在错误时整批不导入。</div>
        </template>
      </el-upload>
      <el-alert
        v-if="importResult"
        :title="`成功 ${importResult.successCount} 条；失败 ${importResult.failures.length} 条`"
        :type="importResult.failures.length ? 'warning' : 'success'"
        :closable="false"
      />
      <el-table
        v-if="importResult?.failures.length"
        :data="importResult.failures"
        max-height="300"
      >
        <el-table-column
          prop="row"
          label="行号"
          width="80"
        />
        <el-table-column
          prop="reason"
          label="失败原因"
        />
      </el-table>
      <template #footer>
        <el-button
          type="primary"
          :loading="busy"
          :disabled="!importFiles.length"
          @click="submitImport"
        >确认导入</el-button>
        <el-button
          :disabled="busy"
          @click="importOpen = false"
        >关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusPeople">
import {
  computed,
  getCurrentInstance,
  onBeforeUnmount,
  reactive,
  ref
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import {
  UploadFilled
} from '@element-plus/icons-vue'
import auth from '@/plugins/auth'
import {
  listPeople,
  getPerson,
  addPerson,
  updatePerson,
  disablePerson,
  importPeople,
  downloadPeopleTemplate
} from '@/api/campus/people'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const peopleList = ref([])
const loading = ref(false)
const busy = ref(false)
const showSearch = ref(true)
const total = ref(0)
const selectedIds = ref([])
const single = computed(() => selectedIds.value.length !== 1)
const listError = ref('')
const optionError = ref('')
const departments = ref([])
const users = ref([])
const usersLoading = ref(false)
const selectedUser = ref(null)
const userOptions = computed(() => selectedUser.value && !users.value.some(item => String(item.value) === String(
  selectedUser.value.value)) ? [selectedUser.value, ...users.value] : users.value)
const bindingLabels = {
  UNBOUND: '未绑定',
  BOUND: '已绑定',
  DISABLED: '已停用'
}
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined,
  personType: undefined,
  status: undefined
})
const open = ref(false)
const title = ref('')
const form = ref({})
const editingId = ref(null)
const accountLocked = ref(false)
const rules = {
  personNo: [{
    required: true,
    whitespace: true,
    message: '学号 / 工号不能为空',
    trigger: 'blur'
  }, {
    max: 32,
    message: '最多32个字符',
    trigger: 'blur'
  }],
  realName: [{
    required: true,
    whitespace: true,
    message: '姓名不能为空',
    trigger: 'blur'
  }, {
    max: 50,
    message: '最多50个字符',
    trigger: 'blur'
  }],
  personType: [{
    required: true,
    message: '请选择人员类型',
    trigger: 'change'
  }],
  deptId: [{
    required: true,
    message: '请选择所属部门',
    trigger: 'change'
  }],
  status: [{
    required: true,
    message: '请选择状态',
    trigger: 'change'
  }],
  remark: [{
    max: 500,
    message: '备注不能超过500个字符',
    trigger: 'blur'
  }]
}
const importOpen = ref(false)
const importFiles = ref([])
const importResult = ref(null)
const templateLoading = ref(false)
let listTicket = 0
let userTicket = 0

// 查询：页面 → listPeople → GET /campus/people；选项独立通过 getOptions 加载。
async function getList() {
  if (!auth.hasPermi('campus:people:list')) return
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listPeople({
      ...queryParams
    })
    if (ticket !== listTicket) return
    peopleList.value = response.rows || []
    total.value = Number(response.total || 0)
    selectedIds.value = []
  } catch (error) {
    if (ticket === listTicket) {
      peopleList.value = []
      total.value = 0
      listError.value = error.message || '人员列表加载失败，请刷新重试'
    }
  } finally {
    if (ticket === listTicket) loading.value = false
  }
}
async function loadOptions() {
  if (!auth.hasPermi('campus:people:list')) return
  optionError.value = ''
  const results = await Promise.allSettled([getOptions('departments'), getOptions('users')])
  if (results[0].status === 'fulfilled') departments.value = results[0].value.data || []
  if (results[1].status === 'fulfilled') users.value = results[1].value.data || []
  if (results.some(result => result.status === 'rejected')) optionError.value = '部分部门或账号选项加载失败'
}
async function loadUsers(keyword) {
  const ticket = ++userTicket
  usersLoading.value = true
  try {
    const response = await getOptions('users', keyword)
    if (ticket === userTicket) users.value = response.data || []
  } catch {
    if (ticket === userTicket) optionError.value = '账号搜索失败，请重试'
  } finally {
    if (ticket === userTicket) usersLoading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef');
  handleQuery()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(row => row.personId)
}

function reset() {
  editingId.value = null
  accountLocked.value = false
  selectedUser.value = null
  form.value = {
    personNo: '',
    realName: '',
    personType: undefined,
    deptId: undefined,
    userId: null,
    status: '0',
    remark: ''
  }
  proxy.resetForm('personRef')
}

function handleAdd() {
  reset()
  title.value = '新增人员'
  open.value = true
}

function personPayload(record) {
  return {
    personNo: record.personNo,
    realName: record.realName,
    personType: record.personType,
    deptId: record.deptId,
    userId: record.userId || null,
    status: record.status,
    remark: record.remark || ''
  }
}
async function handleUpdate(row) {
  if (busy.value) return
  const id = row?.personId ?? selectedIds.value[0]
  if (!id) return
  busy.value = true
  try {
    const record = (await getPerson(id)).data
    reset()
    editingId.value = id
    form.value = personPayload(record)
    accountLocked.value = !!record.userId
    if (record.userId) selectedUser.value = {
      value: record.userId,
      label: record.userName || `账号 #${record.userId}`
    }
    title.value = '修改人员'
    open.value = true
  } catch {
    /* 统一请求拦截器展示错误 */
  } finally {
    busy.value = false
  }
}
// 保存：页面 → addPerson / updatePerson → POST /campus/people 或 PUT /campus/people/{id}。
async function submitForm() {
  if (busy.value || !await proxy.$refs.personRef.validate().catch(() => false)) return
  busy.value = true
  try {
    const payload = personPayload(form.value)
    if (editingId.value) await updatePerson(editingId.value, payload)
    else await addPerson(payload)
    proxy.$modal.msgSuccess(editingId.value ? '修改成功' : '新增成功')
    open.value = false
    await getList()
    await loadOptions()
  } catch {
    /* 保留表单供重试 */
  } finally {
    busy.value = false
  }
}
// 停用走专用接口；启用读取详情后提交完整 PersonDTO。
async function handleStatusChange(row) {
  if (busy.value) return
  busy.value = true
  try {
    const record = (await getPerson(row.personId)).data
    const status = record.status === '0' ? '1' : '0'
    await proxy.$modal.confirm(`确认${status === '1' ? '停用' : '启用'}人员“${record.realName}”？`)
    if (status === '1') await disablePerson(row.personId)
    else await updatePerson(row.personId, {
      ...personPayload(record),
      status
    })
    proxy.$modal.msgSuccess('操作成功')
    await getList()
  } catch {
    /* 取消确认或由统一拦截器展示请求错误 */
  } finally {
    busy.value = false
  }
}

function handleImport() {
  importFiles.value = []
  importResult.value = null
  importOpen.value = true
}

function chooseImport(file) {
  importResult.value = null
  if (!file.raw || file.size <= 0 || file.size > 10 * 1024 * 1024 || !/\.(xlsx|xls)$/i.test(file.name)) {
    importFiles.value = []
    proxy.$modal.msgWarning('请选择非空且不超过10MB的 xls 或 xlsx 文件')
    return
  }
  importFiles.value = [file]
}

function removeImport() {
  importFiles.value = [];
  importResult.value = null
}

function handleExceed() {
  proxy.$modal.msgWarning('请移除原文件后重新选择')
}
async function handleTemplate() {
  if (templateLoading.value) return
  templateLoading.value = true
  try {
    await downloadPeopleTemplate()
  } catch (error) {
    proxy.$modal.msgError(error.message || '模板下载失败')
  } finally {
    templateLoading.value = false
  }
}
// 导入：页面 → importPeople → importFile → POST /campus/people/import（multipart）。
async function submitImport() {
  if (busy.value || !importFiles.value[0]?.raw) return
  busy.value = true
  importResult.value = null
  try {
    const response = await importPeople(importFiles.value[0].raw)
    importResult.value = response.data
    if (!importResult.value.failures.length) {
      importFiles.value = []
      proxy.$modal.msgSuccess('导入成功')
      await getList()
      await loadOptions()
    }
  } catch {
    /* 保留文件供重试 */
  } finally {
    busy.value = false
  }
}
onBeforeRouteLeave(() => !busy.value)
onBeforeUnmount(() => {
  listTicket++;
  userTicket++
})
getList()
loadOptions()
</script>

<style scoped>
.form-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.import-upload {
  margin: 16px 0;
}
</style>

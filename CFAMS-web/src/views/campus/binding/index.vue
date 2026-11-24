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
        label="绑定状态"
        prop="bindingState"
      >
        <el-select
          v-model="queryParams.bindingState"
          placeholder="请选择绑定状态"
          clearable
          style="width: 180px"
        >
          <el-option
            label="未绑定"
            value="UNBOUND"
          />
          <el-option
            label="已绑定"
            value="BOUND"
          />
          <el-option
            label="已停用"
            value="DISABLED"
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
        @click="loadDepartments"
      >重试选项</el-button>
    </el-alert>
    <el-table
      v-loading="loading"
      :data="bindingList"
    >
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
        label="所属部门"
        prop="deptName"
        min-width="140"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column
        label="系统账号"
        prop="userName"
        min-width="120"
        align="center"
      />
      <el-table-column
        label="微信绑定状态"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.bindingState === 'BOUND' ? 'success' : 'info'">{{ bindingLabels[row.bindingState] || '—' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="绑定时间"
        width="180"
        align="center"
      >
        <template #default="{ row }">{{ proxy.parseTime(row.bindTime) || '—' }}</template>
      </el-table-column>
      <el-table-column
        label="最近登录时间"
        width="180"
        align="center"
      >
        <template #default="{ row }">{{ proxy.parseTime(row.lastLoginTime) || '—' }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="210"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="{ row }">
          <el-button
            v-if="row.status === '0' && row.bindingState !== 'BOUND'"
            link
            type="primary"
            :disabled="busy"
            @click="handleIssue(row)"
            v-hasPermi="['campus:binding:issue']"
          >发放凭据</el-button>
          <el-button
            v-if="row.bindingId"
            link
            type="primary"
            :disabled="busy"
            @click="handleRebind(row)"
            v-hasPermi="['campus:binding:rebind']"
          >处理换绑</el-button>
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
      title="换绑核验"
      v-model="open"
      width="580px"
      append-to-body
      :close-on-click-modal="!busy"
      :close-on-press-escape="!busy"
      :show-close="!busy"
      @closed="reset"
    >
      <el-descriptions
        :column="2"
        border
        class="person-summary"
      >
        <el-descriptions-item label="姓名">{{ currentPerson?.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号 / 工号">{{ currentPerson?.personNo }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ currentPerson?.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="系统账号">{{ currentPerson?.userName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-alert
        title="完成线下身份核验后解除旧绑定，随后可重新发放一次性凭据。"
        type="info"
        :closable="false"
        class="person-summary"
      />
      <el-form
        ref="bindingRef"
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
            placeholder="请输入核验的学号或工号"
          />
        </el-form-item>
        <el-form-item
          label="核验说明"
          prop="reason"
        >
          <el-input
            v-model.trim="form.reason"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入身份核验过程及换绑原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="busy"
          @click="submitRebind"
          v-hasPermi="['campus:binding:rebind']"
        >确认换绑</el-button>
        <el-button
          :disabled="busy"
          @click="open = false"
        >取 消</el-button>
      </template>
    </el-dialog>
    <el-dialog
      title="一次性绑定凭据"
      v-model="credentialOpen"
      width="540px"
      append-to-body
      destroy-on-close
      @closed="clearCredential"
    >
      <el-alert
        title="请妥善交付本人，关闭后不再展示本次凭据。"
        type="warning"
        :closable="false"
      />
      <p>人员：{{ credentialPerson }}</p>
      <el-input
        :model-value="credential?.token"
        readonly
        aria-label="一次性绑定凭据"
      />
      <p>有效期至：{{ proxy.parseTime(credential?.expireTime) || '—' }}</p>
      <template #footer>
        <el-button @click="credentialOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusBinding">
import {
  getCurrentInstance,
  onBeforeUnmount,
  onDeactivated,
  reactive,
  ref
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import auth from '@/plugins/auth'
import {
  listBindings,
  issueBindingCredential,
  rebindPerson
} from '@/api/campus/binding'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const bindingList = ref([])
const loading = ref(false)
const busy = ref(false)
const showSearch = ref(true)
const total = ref(0)
const listError = ref('')
const optionError = ref('')
const departments = ref([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined,
  bindingState: undefined
})
const bindingLabels = {
  UNBOUND: '未绑定',
  BOUND: '已绑定',
  DISABLED: '已停用'
}
const open = ref(false)
const currentPerson = ref(null)
const form = ref({
  personNo: '',
  reason: ''
})
const rules = {
  personNo: [{
      required: true,
      whitespace: true,
      message: '请填写学号 / 工号',
      trigger: 'blur'
    },
    {
      max: 32,
      message: '学号 / 工号最多32个字符',
      trigger: 'blur'
    },
    {
      validator: (_, value, callback) => callback(value === currentPerson.value?.personNo ? undefined : new Error(
        '学号 / 工号与当前人员不一致')),
      trigger: 'blur'
    }
  ],
  reason: [{
      required: true,
      whitespace: true,
      message: '请填写核验说明',
      trigger: 'blur'
    },
    {
      max: 500,
      message: '核验说明最多500个字符',
      trigger: 'blur'
    }
  ]
}
const credentialOpen = ref(false)
const credential = ref(null)
const credentialPerson = ref('')
let listTicket = 0

// 查询：页面 → listBindings → GET /campus/binding，无独立绑定详情接口。
async function getList() {
  if (!auth.hasPermi('campus:binding:list')) return
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listBindings({
      ...queryParams
    })
    if (ticket !== listTicket) return
    bindingList.value = response.rows || []
    total.value = Number(response.total || 0)
  } catch (error) {
    if (ticket === listTicket) {
      bindingList.value = []
      total.value = 0
      listError.value = error.message || '绑定列表加载失败，请刷新重试'
    }
  } finally {
    if (ticket === listTicket) loading.value = false
  }
}
async function loadDepartments() {
  if (!auth.hasPermi('campus:binding:list')) return
  optionError.value = ''
  try {
    departments.value = (await getOptions('departments')).data || []
  } catch {
    optionError.value = '部门选项加载失败'
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

function reset() {
  form.value = {
    personNo: '',
    reason: ''
  }
  currentPerson.value = null
  proxy.resetForm('bindingRef')
}

function handleRebind(row) {
  if (busy.value || !row.bindingId) return
  reset()
  currentPerson.value = {
    ...row
  }
  form.value.personNo = row.personNo
  open.value = true
}
// 发放：页面 → issueBindingCredential → POST /campus/binding/{personId}/issue。
async function handleIssue(row) {
  if (busy.value || row.status !== '0' || row.bindingState === 'BOUND') return
  busy.value = true
  try {
    await proxy.$modal.confirm(`确认为“${row.realName}”发放凭据？补发会使旧凭据失效。`)
    clearCredential()
    credential.value = (await issueBindingCredential(row.personId)).data
    credentialPerson.value = `${row.realName}（${row.personNo}）`
    credentialOpen.value = true
    await getList()
  } catch {
    /* 取消确认或由统一拦截器展示请求错误 */
  } finally {
    busy.value = false
  }
}
// 换绑：页面 → rebindPerson → POST /campus/binding/{personId}/rebind。
async function submitRebind() {
  if (busy.value || !currentPerson.value?.bindingId || !await proxy.$refs.bindingRef.validate().catch(() => false))
    return
  busy.value = true
  try {
    await proxy.$modal.confirm(`确认解除“${currentPerson.value.realName}”的旧绑定？`)
    await rebindPerson(currentPerson.value.personId, {
      bindingId: currentPerson.value.bindingId,
      personNo: form.value.personNo,
      reason: form.value.reason
    })
    proxy.$modal.msgSuccess('旧绑定已解除，可重新发放凭据')
    open.value = false
    await getList()
  } catch {
    /* 保留核验表单供重试 */
  } finally {
    busy.value = false
  }
}

function clearCredential() {
  credential.value = null
  credentialPerson.value = ''
}

function leavePage() {
  credentialOpen.value = false
  clearCredential()
}
onBeforeRouteLeave(() => {
  if (busy.value) return false
  leavePage()
})
onDeactivated(leavePage)
onBeforeUnmount(() => {
  listTicket++;
  leavePage()
})
getList()
loadDepartments()
</script>

<style scoped>
.person-summary {
  margin-bottom: 20px;
}
</style>

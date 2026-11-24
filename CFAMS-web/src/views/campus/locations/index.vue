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
        label="场所编码 / 名称"
        prop="keyword"
      >
        <el-input
          v-model.trim="queryParams.keyword"
          placeholder="请输入场所编码或名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="管理部门"
        prop="deptId"
      >
        <el-select
          v-model="queryParams.deptId"
          clearable
          filterable
          placeholder="请选择部门"
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
        label="场所类型"
        prop="locationType"
      >
        <el-select
          v-model="queryParams.locationType"
          clearable
          placeholder="请选择类型"
          style="width: 160px"
        >
          <el-option
            label="校区"
            value="1"
          />
          <el-option
            label="楼栋"
            value="2"
          />
          <el-option
            label="楼层"
            value="3"
          />
          <el-option
            label="房间"
            value="4"
          />
          <el-option
            label="其他"
            value="5"
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
          v-hasPermi="['campus:locations:add']"
        >新增场所</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single || busy"
          @click="handleUpdate()"
          v-hasPermi="['campus:locations:edit']"
        >修改</el-button>
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
      :data="locationList"
      row-key="locationId"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="场所编码"
        prop="locationCode"
        min-width="140"
        align="center"
      />
      <el-table-column
        label="场所名称"
        prop="locationName"
        min-width="160"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column
        label="场所类型"
        width="100"
        align="center"
      >
        <template #default="{ row }">{{ typeLabels[row.locationType] || '—' }}</template>
      </el-table-column>
      <el-table-column
        label="上级场所"
        min-width="160"
        align="center"
        show-overflow-tooltip
      >
        <template #default="{ row }">{{ parentName(row.parentId) }}</template>
      </el-table-column>
      <el-table-column
        label="管理部门"
        min-width="140"
        align="center"
        show-overflow-tooltip
      >
        <template #default="{ row }">{{ departmentName(row.deptId) }}</template>
      </el-table-column>
      <el-table-column
        label="状态"
        width="90"
        align="center"
      >
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="170"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            icon="Edit"
            :disabled="busy"
            @click="handleUpdate(row)"
            v-hasPermi="['campus:locations:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            :disabled="busy"
            @click="handleStatusChange(row)"
            v-hasPermi="['campus:locations:edit']"
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
        ref="locationRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="busy"
      >
        <el-form-item
          label="场所编码"
          prop="locationCode"
        >
          <el-input
            v-model.trim="form.locationCode"
            maxlength="32"
            placeholder="请输入场所编码"
          />
        </el-form-item>
        <el-form-item
          label="场所名称"
          prop="locationName"
        >
          <el-input
            v-model.trim="form.locationName"
            maxlength="100"
            placeholder="请输入场所名称"
          />
        </el-form-item>
        <el-form-item
          label="场所类型"
          prop="locationType"
        >
          <el-select
            v-model="form.locationType"
            placeholder="请选择类型"
            style="width: 100%"
          >
            <el-option
              label="校区"
              value="1"
            />
            <el-option
              label="楼栋"
              value="2"
            />
            <el-option
              label="楼层"
              value="3"
            />
            <el-option
              label="房间"
              value="4"
            />
            <el-option
              label="其他"
              value="5"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="上级场所"
          prop="parentId"
        >
          <el-select
            v-model="form.parentId"
            clearable
            filterable
            placeholder="不选择表示顶级场所"
            style="width: 100%"
          >
            <el-option
              v-for="item in parentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="item.unavailable"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="管理部门"
          prop="deptId"
        >
          <el-select
            v-model="form.deptId"
            clearable
            filterable
            placeholder="不选择表示公共场所"
            style="width: 100%"
          >
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="item.unavailable"
            />
          </el-select>
          <span class="form-tip">公共场所仅全范围管理员可维护</span>
        </el-form-item>
        <el-form-item
          label="显示顺序"
          prop="orderNum"
        >
          <el-input-number
            v-model="form.orderNum"
            :min="0"
            :max="2147483647"
            :precision="0"
            controls-position="right"
          />
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
              label="正常"
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
  </div>
</template>

<script setup name="CampusLocations">
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
import auth from '@/plugins/auth'
import {
  listLocations,
  getLocation,
  addLocation,
  updateLocation
} from '@/api/campus/locations'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const locationList = ref([])
const locations = ref([])
const departments = ref([])
const loading = ref(false)
const busy = ref(false)
const showSearch = ref(true)
const total = ref(0)
const selectedIds = ref([])
const single = computed(() => selectedIds.value.length !== 1)
const listError = ref('')
const optionError = ref('')
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  deptId: undefined,
  locationType: undefined
})
const typeLabels = {
  '1': '校区',
  '2': '楼栋',
  '3': '楼层',
  '4': '房间',
  '5': '其他'
}
const open = ref(false)
const title = ref('')
const form = ref({})
const parentOptions = computed(() => {
  const options = locations.value.filter(item => String(item.value) !== String(form.value.locationId))
  if (form.value.parentId && !options.some(item => String(item.value) === String(form.value.parentId))) {
    return [{
      value: form.value.parentId,
      label: `${parentName(form.value.parentId)}（当前父级）`,
      unavailable: true
    }, ...options]
  }
  return options
})
const departmentOptions = computed(() => {
  if (form.value.deptId && !departments.value.some(item => String(item.value) === String(form.value.deptId))) {
    return [{
      value: form.value.deptId,
      label: departmentName(form.value.deptId),
      unavailable: true
    }, ...departments.value]
  }
  return departments.value
})
const rules = {
  locationCode: [{
    required: true,
    whitespace: true,
    message: '场所编码不能为空',
    trigger: 'blur'
  }, {
    max: 32,
    message: '场所编码最多32个字符',
    trigger: 'blur'
  }],
  locationName: [{
    required: true,
    whitespace: true,
    message: '场所名称不能为空',
    trigger: 'blur'
  }, {
    max: 100,
    message: '场所名称最多100个字符',
    trigger: 'blur'
  }],
  locationType: [{
    required: true,
    message: '请选择场所类型',
    trigger: 'change'
  }],
  orderNum: [{
    required: true,
    type: 'integer',
    min: 0,
    max: 2147483647,
    message: '显示顺序必须为有效的非负整数',
    trigger: 'change'
  }],
  status: [{
    required: true,
    message: '请选择状态',
    trigger: 'change'
  }],
  remark: [{
    max: 500,
    message: '备注最多500个字符',
    trigger: 'blur'
  }]
}
let listTicket = 0

// 查询：页面 → listLocations → GET /campus/locations/list。
async function getList() {
  if (!auth.hasPermi('campus:locations:list')) return
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listLocations({
      ...queryParams
    })
    if (ticket !== listTicket) return
    locationList.value = response.rows || []
    total.value = Number(response.total || 0)
    selectedIds.value = []
  } catch (error) {
    if (ticket === listTicket) {
      locationList.value = []
      total.value = 0
      listError.value = error.message || '场所列表加载失败，请刷新重试'
    }
  } finally {
    if (ticket === listTicket) loading.value = false
  }
}
async function loadOptions() {
  if (!auth.hasPermi('campus:locations:list')) return
  optionError.value = ''
  const results = await Promise.allSettled([getOptions('locations'), getOptions('departments')])
  if (results[0].status === 'fulfilled') locations.value = results[0].value.data || []
  if (results[1].status === 'fulfilled') departments.value = results[1].value.data || []
  if (results.some(result => result.status === 'rejected')) optionError.value = '部分场所或部门选项加载失败'
}

function parentName(id) {
  if (id == null) return '顶级场所'
  return locations.value.find(item => String(item.value) === String(id))?.label ||
    locationList.value.find(item => String(item.locationId) === String(id))?.locationName ||
    `场所 #${id}`
}

function departmentName(id) {
  if (id == null) return '公共场所'
  return departments.value.find(item => String(item.value) === String(id))?.label || `部门 #${id}`
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
  selectedIds.value = rows.map(row => row.locationId)
}

function reset() {
  form.value = {
    locationId: undefined,
    locationCode: '',
    locationName: '',
    locationType: undefined,
    parentId: null,
    deptId: null,
    orderNum: 0,
    status: '0',
    remark: ''
  }
  proxy.resetForm('locationRef')
}

function handleAdd() {
  reset()
  title.value = '新增场所'
  open.value = true
}

function locationPayload(record) {
  return {
    locationId: record.locationId,
    locationCode: record.locationCode,
    locationName: record.locationName,
    locationType: record.locationType,
    parentId: record.parentId || null,
    deptId: record.deptId || null,
    orderNum: record.orderNum,
    status: record.status,
    remark: record.remark || ''
  }
}
async function handleUpdate(row) {
  if (busy.value) return
  const id = row?.locationId ?? selectedIds.value[0]
  if (!id) return
  busy.value = true
  try {
    const record = (await getLocation(id)).data
    reset()
    form.value = locationPayload(record)
    title.value = '修改场所'
    open.value = true
  } catch {
    /* 统一请求拦截器展示错误 */
  } finally {
    busy.value = false
  }
}
// 保存：页面 → addLocation / updateLocation → POST / PUT /campus/locations。
async function submitForm() {
  if (busy.value || !await proxy.$refs.locationRef.validate().catch(() => false)) return
  busy.value = true
  try {
    const payload = locationPayload(form.value)
    if (payload.locationId) await updateLocation(payload)
    else await addLocation(payload)
    proxy.$modal.msgSuccess(payload.locationId ? '修改成功' : '新增成功')
    open.value = false
    await getList()
    await loadOptions()
  } catch {
    /* 保留表单供重试，后端校验部门范围、父子环及资产引用 */
  } finally {
    busy.value = false
  }
}
// 启停：getLocation 读取完整数据 → updateLocation 修改 status。
async function handleStatusChange(row) {
  if (busy.value) return
  busy.value = true
  try {
    const record = (await getLocation(row.locationId)).data
    const status = record.status === '0' ? '1' : '0'
    await proxy.$modal.confirm(`确认${status === '1' ? '停用' : '启用'}场所“${record.locationName}”？`)
    await updateLocation({
      ...locationPayload(record),
      status
    })
    proxy.$modal.msgSuccess('操作成功')
    await getList()
    await loadOptions()
  } catch {
    /* 取消确认或由统一拦截器展示请求错误 */
  } finally {
    busy.value = false
  }
}
onBeforeRouteLeave(() => !busy.value)
onBeforeUnmount(() => {
  listTicket++
})
getList()
loadOptions()
</script>

<style scoped>
.form-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>

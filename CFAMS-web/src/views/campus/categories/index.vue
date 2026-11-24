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
        label="类别编码 / 名称"
        prop="keyword"
      >
        <el-input
          v-model.trim="queryParams.keyword"
          placeholder="请输入类别编码或名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
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
            label="正常"
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
          v-hasPermi="['campus:categories:add']"
        >新增类别</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single || busy"
          @click="handleUpdate()"
          v-hasPermi="['campus:categories:edit']"
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
        @click="loadCategories"
      >重试选项</el-button>
    </el-alert>
    <el-table
      v-loading="loading"
      :data="categoryList"
      row-key="categoryId"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="类别编码"
        prop="categoryCode"
        min-width="140"
        align="center"
      />
      <el-table-column
        label="类别名称"
        prop="categoryName"
        min-width="160"
        align="center"
        show-overflow-tooltip
      />
      <el-table-column
        label="上级类别"
        min-width="160"
        align="center"
        show-overflow-tooltip
      >
        <template #default="{ row }">{{ parentName(row.parentId) }}</template>
      </el-table-column>
      <el-table-column
        label="显示顺序"
        prop="orderNum"
        width="100"
        align="center"
      />
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
        label="备注"
        prop="remark"
        min-width="160"
        align="center"
        show-overflow-tooltip
      />
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
            v-hasPermi="['campus:categories:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            :disabled="busy"
            @click="handleStatusChange(row)"
            v-hasPermi="['campus:categories:edit']"
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
      width="540px"
      append-to-body
      :close-on-click-modal="!busy"
      :close-on-press-escape="!busy"
      :show-close="!busy"
      @closed="reset"
    >
      <el-form
        ref="categoryRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="busy"
      >
        <el-form-item
          label="类别编码"
          prop="categoryCode"
        >
          <el-input
            v-model.trim="form.categoryCode"
            maxlength="32"
            placeholder="请输入类别编码"
          />
        </el-form-item>
        <el-form-item
          label="类别名称"
          prop="categoryName"
        >
          <el-input
            v-model.trim="form.categoryName"
            maxlength="100"
            placeholder="请输入类别名称"
          />
        </el-form-item>
        <el-form-item
          label="上级类别"
          prop="parentId"
        >
          <el-select
            v-model="form.parentId"
            filterable
            clearable
            placeholder="不选择表示顶级类别"
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

<script setup name="CampusCategories">
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
  listCategories,
  getCategory,
  addCategory,
  updateCategory
} from '@/api/campus/categories'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const categoryList = ref([])
const categories = ref([])
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
  status: undefined
})
const open = ref(false)
const title = ref('')
const form = ref({})
const parentOptions = computed(() => {
  const options = categories.value.filter(item => String(item.value) !== String(form.value.categoryId))
  // 选项接口只返回启用类别；保留原父级回显，避免编辑时无意清空。
  if (form.value.parentId && !options.some(item => String(item.value) === String(form.value.parentId))) {
    return [{
      value: form.value.parentId,
      label: `${parentName(form.value.parentId)}（当前父级）`,
      unavailable: true
    }, ...options]
  }
  return options
})
const rules = {
  categoryCode: [{
    required: true,
    whitespace: true,
    message: '类别编码不能为空',
    trigger: 'blur'
  }, {
    max: 32,
    message: '类别编码最多32个字符',
    trigger: 'blur'
  }],
  categoryName: [{
    required: true,
    whitespace: true,
    message: '类别名称不能为空',
    trigger: 'blur'
  }, {
    max: 100,
    message: '类别名称最多100个字符',
    trigger: 'blur'
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

// 查询：页面 → listCategories → GET /campus/categories/list。
async function getList() {
  if (!auth.hasPermi('campus:categories:list')) return
  const ticket = ++listTicket
  loading.value = true
  listError.value = ''
  try {
    const response = await listCategories({
      ...queryParams
    })
    if (ticket !== listTicket) return
    categoryList.value = response.rows || []
    total.value = Number(response.total || 0)
    selectedIds.value = []
  } catch (error) {
    if (ticket === listTicket) {
      categoryList.value = []
      total.value = 0
      listError.value = error.message || '类别列表加载失败，请刷新重试'
    }
  } finally {
    if (ticket === listTicket) loading.value = false
  }
}
async function loadCategories() {
  if (!auth.hasPermi('campus:categories:list')) return
  optionError.value = ''
  try {
    categories.value = (await getOptions('categories')).data || []
  } catch {
    optionError.value = '上级类别选项加载失败'
  }
}

function parentName(id) {
  if (id == null) return '顶级类别'
  return categories.value.find(item => String(item.value) === String(id))?.label ||
    categoryList.value.find(item => String(item.categoryId) === String(id))?.categoryName ||
    `类别 #${id}`
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
  selectedIds.value = rows.map(row => row.categoryId)
}

function reset() {
  form.value = {
    categoryId: undefined,
    categoryCode: '',
    categoryName: '',
    parentId: null,
    orderNum: 0,
    status: '0',
    remark: ''
  }
  proxy.resetForm('categoryRef')
}

function handleAdd() {
  reset()
  title.value = '新增类别'
  open.value = true
}

function categoryPayload(record) {
  return {
    categoryId: record.categoryId,
    categoryCode: record.categoryCode,
    categoryName: record.categoryName,
    parentId: record.parentId || null,
    orderNum: record.orderNum,
    status: record.status,
    remark: record.remark || ''
  }
}
async function handleUpdate(row) {
  if (busy.value) return
  const id = row?.categoryId ?? selectedIds.value[0]
  if (!id) return
  busy.value = true
  try {
    const record = (await getCategory(id)).data
    reset()
    form.value = categoryPayload(record)
    title.value = '修改类别'
    open.value = true
  } catch {
    /* 统一请求拦截器展示错误 */
  } finally {
    busy.value = false
  }
}
// 保存：页面 → addCategory / updateCategory → POST / PUT /campus/categories。
async function submitForm() {
  if (busy.value || !await proxy.$refs.categoryRef.validate().catch(() => false)) return
  busy.value = true
  try {
    const payload = categoryPayload(form.value)
    if (payload.categoryId) await updateCategory(payload)
    else await addCategory(payload)
    proxy.$modal.msgSuccess(payload.categoryId ? '修改成功' : '新增成功')
    open.value = false
    await getList()
    await loadCategories()
  } catch {
    /* 保留表单供重试，后端校验父子环及引用关系 */
  } finally {
    busy.value = false
  }
}
// 启停：getCategory 读取完整数据 → updateCategory 修改 status。
async function handleStatusChange(row) {
  if (busy.value) return
  busy.value = true
  try {
    const record = (await getCategory(row.categoryId)).data
    const status = record.status === '0' ? '1' : '0'
    await proxy.$modal.confirm(`确认${status === '1' ? '停用' : '启用'}类别“${record.categoryName}”？`)
    await updateCategory({
      ...categoryPayload(record),
      status
    })
    proxy.$modal.msgSuccess('操作成功')
    await getList()
    await loadCategories()
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
loadCategories()
</script>

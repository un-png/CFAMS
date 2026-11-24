<template>
  <div class="app-container">
    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
    >
      <el-form-item
        label="资产编号/名称"
        prop="keyword"
      >
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入编号或名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="资产类别"
        prop="categoryId"
      >
        <el-select
          v-model="queryParams.categoryId"
          placeholder="全部类别"
          clearable
          filterable
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
          placeholder="全部部门"
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
        label="存放场所"
        prop="locationId"
      >
        <el-select
          v-model="queryParams.locationId"
          placeholder="全部场所"
          clearable
          filterable
          style="width: 180px"
        >
          <el-option
            v-for="item in locations"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="资产状态"
        prop="status"
      >
        <el-select
          v-model="queryParams.status"
          placeholder="全部状态"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in assetStatuses"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="完好情况"
        prop="conditionStatus"
      >
        <el-select
          v-model="queryParams.conditionStatus"
          placeholder="全部"
          clearable
          style="width: 140px"
        >
          <el-option
            v-for="item in conditions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
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
          v-hasPermi="['campus:assets:add']"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="selection.length !== 1 || selection[0]?.assetStatus !== '0'"
          v-hasPermi="['campus:assets:edit']"
          @click="handleUpdate(selection[0])"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          v-hasPermi="['campus:assets:import']"
          @click="handleImport"
        >导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          :loading="exportLoading"
          v-hasPermi="['campus:assets:export']"
          @click="handleExport"
        >导出</el-button>
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
      class="mb8"
    />
    <el-alert
      v-if="optionError"
      :title="optionError"
      type="warning"
      :closable="false"
      class="mb8"
    >
      <el-button
        link
        @click="loadOptions"
      >重新加载选项</el-button>
    </el-alert>
    <el-table
      v-loading="loading"
      :data="assetList"
      @selection-change="selection = $event"
    >
      <el-table-column
        type="selection"
        width="55"
        align="center"
      />
      <el-table-column
        label="资产编号"
        prop="assetCode"
        min-width="175"
        show-overflow-tooltip
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column
        label="类别"
        prop="categoryName"
        min-width="140"
        show-overflow-tooltip
      />
      <el-table-column
        label="规格型号"
        prop="specification"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column
        label="所属部门"
        prop="deptName"
        min-width="120"
      />
      <el-table-column
        label="存放场所"
        prop="locationName"
        min-width="150"
        show-overflow-tooltip
      />
      <el-table-column
        label="责任人"
        prop="keeperName"
        width="110"
        align="center"
      />
      <el-table-column
        label="资产状态"
        width="100"
        align="center"
      >
        <template #default="scope">
          <el-tag :type="scope.row.assetStatus === '0' ? 'success' : 'info'">
            {{ assetStatuses.find(item => item.value === scope.row.assetStatus)?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="完好情况"
        width="100"
        align="center"
      >
        <template #default="scope">{{ conditions.find(item => item.value === scope.row.conditionStatus)?.label }}
        </template>
      </el-table-column>
      <el-table-column
        label="登记价值（元）"
        prop="purchasePrice"
        width="135"
        align="right"
      />
      <el-table-column
        label="入册时间"
        width="170"
        align="center"
      >
        <template #default="scope">{{ parseTime(scope.row.registeredTime) }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="235"
        fixed="right"
        align="center"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            v-hasPermi="['campus:assets:query']"
            @click="handleView(scope.row)"
          >详情</el-button>
          <el-button
            v-if="scope.row.assetStatus === '0'"
            link
            type="primary"
            icon="Edit"
            v-hasPermi="['campus:assets:edit']"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            v-if="scope.row.assetStatus !== '3'"
            link
            type="primary"
            icon="Picture"
            v-hasPermi="['campus:assets:edit']"
            @click="handlePhotos(scope.row)"
          >照片</el-button>
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
      width="800px"
      append-to-body
      :close-on-click-modal="!submitLoading"
      :before-close="closeForm"
    >
      <el-form
        ref="assetRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        :disabled="submitLoading || !!savedId"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item
              label="资产编号"
              prop="assetCode"
            >
              <el-input
                v-model="form.assetCode"
                :disabled="!!form.assetId"
                maxlength="64"
                placeholder="请输入唯一资产编号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="资产名称"
              prop="assetName"
            >
              <el-input
                v-model="form.assetName"
                maxlength="150"
                placeholder="请输入资产名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="资产类别"
              prop="categoryId"
            >
              <el-select
                v-model="form.categoryId"
                filterable
                placeholder="请选择类别"
              >
                <el-option
                  v-for="item in categories"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="规格型号"
              prop="specification"
            >
              <el-input
                v-model="form.specification"
                placeholder="请输入规格型号"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="采购日期"
              prop="purchaseDate"
            >
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="登记价值"
              prop="purchasePrice"
            >
              <el-input-number
                v-model="form.purchasePrice"
                :min="0"
                :precision="2"
                :max="999999999999.99"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="供应商"
              prop="supplierName"
            >
              <el-input
                v-model="form.supplierName"
                placeholder="请输入供应商名称"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="使用年限"
              prop="expectedUseYears"
            >
              <el-input-number
                v-model="form.expectedUseYears"
                :min="0.01"
                :max="999.99"
                :precision="2"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="所属部门"
              prop="deptId"
            >
              <el-select
                v-model="form.deptId"
                filterable
                placeholder="请选择部门"
              >
                <el-option
                  v-for="item in departments"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="存放场所"
              prop="locationId"
            >
              <el-select
                v-model="form.locationId"
                filterable
                placeholder="请选择场所"
              >
                <el-option
                  v-for="item in locations"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="责任人"
              prop="keeperUserId"
            >
              <el-select
                v-model="form.keeperUserId"
                filterable
                remote
                :remote-method="searchUsers"
                :loading="usersLoading"
                placeholder="搜索姓名或账号"
              >
                <el-option
                  v-for="item in users"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="开放借用"
              prop="borrowEnabled"
            >
              <el-radio-group v-model="form.borrowEnabled">
                <el-radio value="0">否</el-radio>
                <el-radio value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              label="可见范围"
              prop="visibility"
            >
              <el-radio-group v-model="form.visibility">
                <el-radio value="1">授权部门及关联人员</el-radio>
                <el-radio value="0">校内认证用户</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item
              label="备注"
              prop="remark"
            >
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-form label-width="110px">
        <el-form-item
          label="资产照片"
          v-hasPermi="['campus:assets:edit']"
        >
          <el-upload
            v-model:file-list="photos"
            :auto-upload="false"
            accept=".jpg,.jpeg,.png,.webp"
            :limit="5"
            :on-change="checkPhoto"
            :disabled="submitLoading"
          >
            <el-button icon="Upload">选择照片</el-button>
            <template #tip>
              <div class="el-upload__tip">最多5张，每张不超过10MB，保存资产后上传。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="savedId"
        title="资产已保存，部分照片上传失败。再次确定只重试未上传照片。"
        type="warning"
        :closable="false"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="submitForm"
          >确 定</el-button>
          <el-button
            :disabled="submitLoading"
            @click="open = false"
          >取 消</el-button>
        </div>
      </template>
    </el-dialog>
  
    <el-dialog
      title="资产详情"
      v-model="detailOpen"
      width="900px"
      append-to-body
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <el-descriptions
          v-if="detail"
          :column="2"
          border
        >
          <el-descriptions-item label="资产编号">{{ detail.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ detail.assetName }}</el-descriptions-item>
          <el-descriptions-item label="资产类别">{{ detail.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ detail.specification || '—' }}</el-descriptions-item>
          <el-descriptions-item label="采购日期">{{ detail.purchaseDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="登记价值（元）">{{ detail.purchasePrice }}</el-descriptions-item>
          <el-descriptions-item label="供应商">{{ detail.supplierName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="预计使用年限">{{ detail.expectedUseYears || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ detail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="存放场所">{{ detail.locationName }}</el-descriptions-item>
          <el-descriptions-item label="责任人">{{ detail.keeperName }}</el-descriptions-item>
          <el-descriptions-item label="资产状态">{{ assetStatuses.find(item => item.value === detail.assetStatus)?.label }}
          </el-descriptions-item>
          <el-descriptions-item label="完好情况">{{ conditions.find(item => item.value === detail.conditionStatus)?.label }}
          </el-descriptions-item>
          <el-descriptions-item label="开放借用">{{ detail.borrowEnabled === '1' ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="可见范围">{{ detail.visibility === '0' ? '校内认证用户' : '授权部门及关联人员' }}
          </el-descriptions-item>
          <el-descriptions-item label="入册时间">{{ parseTime(detail.registeredTime) }}</el-descriptions-item>
          <el-descriptions-item
            label="备注"
            :span="2"
          >{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">资产履历</el-divider>
        <el-table :data="historyList">
          <el-table-column
            label="事件"
            prop="eventType"
            width="120"
          />
          <el-table-column
            label="时间"
            prop="eventTime"
            width="190"
          />
          <el-table-column
            label="变更原因"
            prop="changeReason"
          />
        </el-table>
        <pagination
          v-show="historyTotal > 0"
          :total="historyTotal"
          v-model:page="historyQuery.pageNum"
          v-model:limit="historyQuery.pageSize"
          @pagination="getHistory"
        />
        <el-divider content-position="left">资产照片</el-divider>
        <business-attachments
          v-if="detail"
          biz-type="ASSET"
          :biz-id="detail.assetId"
        />
      </div>
    </el-dialog>
    <el-dialog
      title="补充资产照片"
      v-model="photosOpen"
      width="550px"
      append-to-body
      :close-on-click-modal="!submitLoading"
      :before-close="closeForm"
    >
      <el-upload
        v-model:file-list="photos"
        :auto-upload="false"
        accept=".jpg,.jpeg,.png,.webp"
        :limit="5"
        :on-change="checkPhoto"
        :disabled="submitLoading"
      >
        <el-button icon="Upload">选择照片</el-button>
      </el-upload>
      <template #footer>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitPhotos"
        >上 传</el-button>
        <el-button
          :disabled="submitLoading"
          @click="photosOpen = false"
        >取 消</el-button>
      </template>
    </el-dialog>
    <el-dialog
      title="资产导入"
      v-model="importOpen"
      width="650px"
      append-to-body
      :close-on-click-modal="!importLoading"
      :before-close="done => { if (!importLoading) done() }"
    >
      <el-button
        link
        type="primary"
        icon="Download"
        @click="handleTemplate"
      >下载导入模板</el-button>
      <el-upload
        v-model:file-list="importFiles"
        drag
        :auto-upload="false"
        accept=".xlsx,.xls"
        :limit="1"
        :disabled="importLoading"
        :on-change="checkImport"
      >
        <el-icon class="el-icon--upload">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">仅允许Excel文件，最大10MB。</div>
        </template>
      </el-upload>
      <el-alert
        v-if="importResult"
        :title="`成功 ${importResult.imported} 条，失败 ${importResult.errors?.length || 0} 条`"
        :type="importResult.errors?.length ? 'warning' : 'success'"
        :closable="false"
      />
      <el-table
        v-if="importResult?.errors?.length"
        :data="importResult.errors"
      >
        <el-table-column
          prop="row"
          label="行号"
          width="70"
        />
        <el-table-column
          prop="field"
          label="字段"
          width="120"
        />
        <el-table-column
          prop="reason"
          label="原因"
        />
      </el-table>
      <template #footer>
        <el-button
          type="primary"
          :loading="importLoading"
          @click="submitImport"
        >确 定</el-button>
        <el-button
          :disabled="importLoading"
          @click="importOpen = false"
        >取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusAssets">
import {
  ref,
  reactive,
  getCurrentInstance,
  onBeforeUnmount
} from 'vue'
import {
  onBeforeRouteLeave
} from 'vue-router'
import {
  listAssets,
  getAsset,
  addAsset,
  updateAsset,
  listAssetHistory,
  importAssets,
  exportAssets,
  downloadAssetTemplate
} from '@/api/campus/assets'
import {
  getOptions
} from '@/api/campus/options'
import {
  uploadAttachment
} from '@/api/campus/attachment'
import BusinessAttachments from '@/components/Campus/BusinessAttachments.vue'

const {
  proxy
} = getCurrentInstance()
const showSearch = ref(true)
const loading = ref(false)
const assetList = ref([])
const total = ref(0)
const selection = ref([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  categoryId: undefined,
  deptId: undefined,
  locationId: undefined,
  status: undefined,
  conditionStatus: undefined
})
const assetStatuses = [{
  value: '0',
  label: '在库'
}, {
  value: '1',
  label: '借出'
}, {
  value: '2',
  label: '维修中'
}, {
  value: '3',
  label: '已报废'
}]
const conditions = [{
  value: '0',
  label: '正常'
}, {
  value: '1',
  label: '损坏'
}, {
  value: '2',
  label: '待核验'
}]
const departments = ref([]),
  categories = ref([]),
  locations = ref([]),
  users = ref([])
const usersLoading = ref(false),
  optionError = ref(''),
  listError = ref('')
const open = ref(false),
  title = ref(''),
  form = ref({}),
  photos = ref([]),
  savedId = ref(null)
const submitLoading = ref(false),
  exportLoading = ref(false)
const detailOpen = ref(false),
  detailLoading = ref(false),
  detail = ref(null),
  historyList = ref([]),
  historyTotal = ref(0)
const historyQuery = reactive({
  pageNum: 1,
  pageSize: 10
})
const photosOpen = ref(false),
  photoAssetId = ref(null)
const importOpen = ref(false),
  importLoading = ref(false),
  importFiles = ref([]),
  importResult = ref(null)
let listRequest = 0,
  userRequest = 0,
  detailRequest = 0
const rules = {
  assetCode: [{
    required: true,
    whitespace: true,
    message: '资产编号不能为空',
    trigger: 'blur'
  }],
  assetName: [{
    required: true,
    whitespace: true,
    message: '资产名称不能为空',
    trigger: 'blur'
  }],
  categoryId: [{
    required: true,
    message: '请选择类别',
    trigger: 'change'
  }],
  deptId: [{
    required: true,
    message: '请选择部门',
    trigger: 'change'
  }],
  locationId: [{
    required: true,
    message: '请选择场所',
    trigger: 'change'
  }],
  keeperUserId: [{
    required: true,
    message: '请选择责任人',
    trigger: 'change'
  }],
  purchasePrice: [{
    required: true,
    message: '请填写登记价值',
    trigger: 'change'
  }],
  purchaseDate: [{
    validator: (_, value, callback) => {
      const today = new Date()
      today.setHours(23, 59, 59, 999)
      callback(value && new Date(value) > today ? new Error('采购日期不能晚于今天') : undefined)
    },
    trigger: 'change'
  }]
}

async function getList() {
  const current = ++listRequest
  loading.value = true
  listError.value = ''
  try {
    const response = await listAssets({
      ...queryParams
    })
    if (current !== listRequest) return
    assetList.value = response.rows || []
    total.value = response.total || 0
    selection.value = []
  } catch (err) {
    if (current === listRequest) {
      assetList.value = [];
      total.value = 0;
      listError.value = err.message || '资产加载失败'
    }
  } finally {
    if (current === listRequest) loading.value = false
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
async function loadOptions() {
  optionError.value = ''
  const results = await Promise.allSettled([
    getOptions('departments').then(res => {
      departments.value = res.data
    }),
    getOptions('categories').then(res => {
      categories.value = res.data
    }),
    getOptions('locations').then(res => {
      locations.value = res.data
    }),
    searchUsers('')
  ])
  if (results.some(result => result.status === 'rejected')) optionError.value = '部分选项加载失败，请重新加载'
}
async function searchUsers(keyword) {
  const current = ++userRequest
  usersLoading.value = true
  try {
    const response = await getOptions('users', keyword)
    if (current === userRequest) {
      users.value = response.data || []
      if (form.value.keeperUserId && !users.value.some(item => item.value === form.value.keeperUserId)) users.value
        .push({
          value: form.value.keeperUserId,
          label: form.value.keeperName || `责任人 #${form.value.keeperUserId}`
        })
    }
  } finally {
    if (current === userRequest) usersLoading.value = false
  }
}

function resetForm() {
  form.value = {
    assetId: undefined,
    assetCode: '',
    assetName: '',
    categoryId: undefined,
    specification: '',
    purchaseDate: undefined,
    purchasePrice: undefined,
    supplierName: '',
    expectedUseYears: undefined,
    deptId: undefined,
    locationId: undefined,
    keeperUserId: undefined,
    borrowEnabled: '0',
    visibility: '1',
    remark: ''
  }
  photos.value = []
  savedId.value = null
  proxy.$refs.assetRef?.clearValidate()
}

function handleAdd() {
  resetForm();
  title.value = '新增资产';
  open.value = true
}
async function handleUpdate(row) {
  try {
    const response = await getAsset(row.assetId)
    resetForm()
    Object.keys(form.value).forEach(key => {
      form.value[key] = response.data[key] ?? form.value[key]
    })
    form.value.keeperName = response.data.keeperName
    await searchUsers('')
    title.value = '修改资产'
    open.value = true
  } catch {
    /* 请求拦截器显示接口错误 */
  }
}

function checkPhoto(file) {
  if (!file.raw || file.size <= 0 || !/\.(jpe?g|png|webp)$/i.test(file.name) || file.size > 10 * 1024 * 1024) {
    photos.value = photos.value.filter(item => item.uid !== file.uid)
    proxy.$modal.msgWarning('请选择不超过10MB的JPG、PNG或WebP图片')
  }
}
async function uploadPhotos(id) {
  for (const file of photos.value) {
    if (file.uploaded) continue
    await uploadAttachment('ASSET', id, file.raw)
    file.uploaded = true
  }
}
async function submitForm() {
  if (submitLoading.value) return
  if (!savedId.value && !await proxy.$refs.assetRef.validate().catch(() => false)) return
  submitLoading.value = true
  try {
    if (!savedId.value) {
      const {
        keeperName,
        ...data
      } = form.value
      const response = await (data.assetId ? updateAsset(data) : addAsset(data))
      savedId.value = data.assetId || response.data.assetId
    }
    await uploadPhotos(savedId.value)
    proxy.$modal.msgSuccess('保存成功')
    open.value = false
    getList()
  } catch (err) {
    proxy.$modal.msgError(err.message || '保存失败');
    if (savedId.value) getList()
  } finally {
    submitLoading.value = false
  }
}

function closeForm(done) {
  if (!submitLoading.value) done()
}
async function handleView(row) {
  const current = ++detailRequest
  detail.value = null
  historyList.value = []
  historyTotal.value = 0
  historyQuery.pageNum = 1
  detailOpen.value = true
  detailLoading.value = true
  try {
    const response = await getAsset(row.assetId)
    if (current !== detailRequest) return
    detail.value = response.data
    await getHistory()
  } catch {
    /* 统一错误提示 */
  } finally {
    if (current === detailRequest) detailLoading.value = false
  }
}
async function getHistory() {
  if (!detail.value) return
  const id = detail.value.assetId
  const response = await listAssetHistory(id, {
    ...historyQuery
  })
  if (detail.value?.assetId === id) {
    historyList.value = response.rows || [];
    historyTotal.value = response.total || 0
  }
}

function handlePhotos(row) {
  photoAssetId.value = row.assetId;
  photos.value = [];
  photosOpen.value = true
}
async function submitPhotos() {
  if (submitLoading.value) return
  if (!photos.value.length) return proxy.$modal.msgWarning('请选择照片')
  submitLoading.value = true
  try {
    await uploadPhotos(photoAssetId.value);
    proxy.$modal.msgSuccess('上传成功');
    photosOpen.value = false
  } catch {
    /* 失败时保留未上传文件以便重试 */
  } finally {
    submitLoading.value = false
  }
}

function handleImport() {
  importFiles.value = [];
  importResult.value = null;
  importOpen.value = true
}

function checkImport(file) {
  importResult.value = null
  if (!file.raw || file.size <= 0 || !/\.(xlsx|xls)$/i.test(file.name) || file.size > 10 * 1024 * 1024) {
    importFiles.value = [];
    proxy.$modal.msgWarning('请选择非空且不超过10MB的Excel文件')
  }
}
async function submitImport() {
  if (importLoading.value) return
  if (!importFiles.value.length) return proxy.$modal.msgWarning('请选择Excel文件')
  importLoading.value = true
  try {
    const response = await importAssets(importFiles.value[0].raw)
    importResult.value = response.data
    if (!response.data.errors?.length) importFiles.value = []
    getList()
  } catch {
    /* 保留导入文件以便检查 */
  } finally {
    importLoading.value = false
  }
}
async function handleTemplate() {
  try {
    await downloadAssetTemplate()
  } catch (err) {
    proxy.$modal.msgError(err.message)
  }
}
async function handleExport() {
  exportLoading.value = true
  try {
    await exportAssets({
      ...queryParams
    })
  } catch (err) {
    proxy.$modal.msgError(err.message)
  } finally {
    exportLoading.value = false
  }
}
onBeforeRouteLeave(() => !submitLoading.value && !importLoading.value)
onBeforeUnmount(() => {
  listRequest++;
  userRequest++;
  detailRequest++
})
getList()
loadOptions()
</script>

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
          clearable
          placeholder="请输入编号或名称"
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item
        label="类别"
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
        label="部门"
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
      <el-form-item
        label="场所"
        prop="locationId"
      >
        <el-select
          v-model="queryParams.locationId"
          clearable
          filterable
          placeholder="全部场所"
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
          icon="Grid"
          :disabled="!selection.length"
          :loading="preparing"
          v-hasPermi="['campus:labels:preview']"
          @click="handlePreview(selection)"
        >标签排版</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Printer"
          :disabled="!selection.length"
          :loading="preparing"
          v-hasPermi="['campus:labels:print']"
          @click="handlePreview(selection, true)"
        >批量打印</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Printer"
          :disabled="!selection.length"
          :loading="preparing"
          v-hasPermi="['campus:labels:print']"
          @click="handlePreview(selection, true)"
        >补打标签</el-button>
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      />
    </el-row>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      class="mb8"
    />
    <el-table
      v-loading="loading"
      :data="labelList"
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
        min-width="180"
      />
      <el-table-column
        label="资产名称"
        prop="assetName"
        min-width="180"
      />
      <el-table-column
        label="类别"
        prop="categoryName"
        min-width="140"
      />
      <el-table-column
        label="存放场所"
        prop="locationName"
        min-width="150"
      />
      <el-table-column
        label="标签状态"
        width="110"
        align="center"
      >
        <template #default="scope">
          <el-tag
            v-if="scope.row.qrToken"
            type="success"
          >已生成</el-tag>
          <span v-else>未生成</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        align="center"
        width="140"
        class-name="small-padding fixed-width"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            :disabled="preparing"
            v-hasPermi="['campus:labels:preview']"
            @click="handlePreview([scope.row])"
          >预览标签</el-button>
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
      title="二维码标签排版"
      v-model="open"
      width="800px"
      append-to-body
      :close-on-click-modal="!printing"
      :before-close="done => { if (!printing) done() }"
      @closed="clearImages"
    >
      <el-form :inline="true">
        <el-form-item label="标签规格">
          <el-select
            v-model="labelSize"
            style="width: 160px"
          >
            <el-option
              label="60 × 40 mm"
              value="60x40"
            />
            <el-option
              label="80 × 50 mm"
              value="80x50"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="每行数量">
          <el-input-number
            v-model="labelColumns"
            :min="1"
            :max="labelSize === '80x50' ? 2 : 3"
          />
        </el-form-item>
      </el-form>
      <div class="label-preview">
        <div
          v-for="item in labels"
          :key="item.assetId"
          class="asset-label"
        >
          <img
            :src="item.image"
            alt="资产二维码"
          />
          <div>
            <strong>{{ item.assetName }}</strong>
            <p>{{ item.assetCode }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            icon="Printer"
            :loading="printing"
            v-hasPermi="['campus:labels:print']"
            @click="handlePrint"
          >打 印</el-button>
          <el-button
            :disabled="printing"
            @click="open = false"
          >关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CampusLabels">
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
  listLabels,
  previewLabels,
  printLabels,
  getLabelImage
} from '@/api/campus/labels'
import {
  getOptions
} from '@/api/campus/options'

const {
  proxy
} = getCurrentInstance()
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  categoryId: undefined,
  deptId: undefined,
  locationId: undefined
})
const labelList = ref([]),
  total = ref(0),
  selection = ref([])
const loading = ref(false),
  showSearch = ref(true),
  error = ref('')
const categories = ref([]),
  departments = ref([]),
  locations = ref([])
const open = ref(false),
  preparing = ref(false),
  printing = ref(false),
  labels = ref([])
const labelSize = ref('60x40'),
  labelColumns = ref(2)
let requestId = 0,
  disposed = false
const imageUrls = new Set()
const printFrames = new Set()
async function getList() {
  const current = ++requestId
  loading.value = true
  error.value = ''
  try {
    const response = await listLabels({
      ...queryParams
    })
    if (current !== requestId) return
    labelList.value = response.rows || []
    total.value = response.total || 0
    selection.value = []
  } catch (err) {
    if (current === requestId) {
      labelList.value = [];
      total.value = 0;
      error.value = err.message || '标签加载失败'
    }
  } finally {
    if (current === requestId) loading.value = false
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

function clearImages() {
  imageUrls.forEach(url => URL.revokeObjectURL(url));
  imageUrls.clear();
  labels.value = []
}
async function handlePreview(rows, forPrint = false) {
  if (preparing.value) return
  if (!rows.length || rows.length > 200) return proxy.$modal.msgWarning('请选择1至200件资产')
  preparing.value = true
  clearImages()
  try {
    const response = await (forPrint ? printLabels(rows.map(item => item.assetId)) : previewLabels(rows.map(item =>
      item.assetId)))
    for (const item of response.data) {
      const image = await getLabelImage(item.assetId)
      if (disposed) return
      const url = URL.createObjectURL(image)
      imageUrls.add(url)
      labels.value.push({
        ...item,
        image: url
      })
    }
    open.value = true
  } catch (err) {
    clearImages();
    proxy.$modal.msgError(err.message || '标签生成失败')
  } finally {
    preparing.value = false
  }
}
async function handlePrint() {
  if (printing.value || !labels.value.length) return
  printing.value = true
  let frame
  try {
    await printLabels(labels.value.map(item => item.assetId))
    frame = document.createElement('iframe')
    frame.style.cssText = 'position:fixed;width:0;height:0;border:0'
    document.body.appendChild(frame)
    printFrames.add(frame)
    const doc = frame.contentDocument
    doc.open()
    doc.write('<!doctype html><html><head><title>资产标签</title></head><body></body></html>')
    doc.close()
    const [width, height] = labelSize.value.split('x').map(Number)
    const columns = Math.min(labelColumns.value, Math.floor(190 / width))
    const style = doc.createElement('style')
    style.textContent =
      `@page{size:A4;margin:10mm}body{margin:0;font-family:sans-serif}.sheet{display:grid;grid-template-columns:repeat(${columns},${width}mm)}.label{box-sizing:border-box;width:${width}mm;height:${height}mm;display:flex;align-items:center;gap:2mm;padding:3mm;border:1px dashed #999;break-inside:avoid;font-size:10px;overflow-wrap:anywhere}.label img{width:25mm;height:25mm}`
    doc.head.appendChild(style)
    const sheet = doc.createElement('div')
    sheet.className = 'sheet'
    doc.body.appendChild(sheet)
    for (const item of labels.value) {
      const box = doc.createElement('div'),
        image = doc.createElement('img'),
        caption = doc.createElement('div')
      box.className = 'label'
      image.src = item.image
      caption.textContent = `${item.assetName} ${item.assetCode}`
      box.append(image, caption)
      sheet.append(box)
    }
    await Promise.all([...doc.images].map(image => image.decode()))
    frame.contentWindow.focus()
    frame.contentWindow.print()
    const printedFrame = frame
    setTimeout(() => {
      printedFrame.remove();
      printFrames.delete(printedFrame)
    }, 60000)
  } catch (err) {
    frame?.remove();
    if (frame) printFrames.delete(frame);
    proxy.$modal.msgError(err.message || '打印失败')
  } finally {
    printing.value = false
  }
}
async function loadOptions() {
  const results = await Promise.allSettled([
    getOptions('categories').then(res => {
      categories.value = res.data
    }),
    getOptions('departments').then(res => {
      departments.value = res.data
    }),
    getOptions('locations').then(res => {
      locations.value = res.data
    })
  ])
  if (results.some(item => item.status === 'rejected')) proxy.$modal.msgWarning('部分筛选选项加载失败')
}
onBeforeRouteLeave(() => !preparing.value && !printing.value)
onBeforeUnmount(() => {
  disposed = true;
  requestId++;
  clearImages();
  printFrames.forEach(frame => frame.remove())
})
getList()
loadOptions()
</script>

<style scoped>
.label-preview {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.asset-label {
  border: 1px dashed var(--el-border-color);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow-wrap: anywhere;
}

.asset-label img {
  width: 100px;
  height: 100px;
}
</style>

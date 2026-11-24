import request from '@/utils/request'

// 任务页 → 本模块 → CampusInventoryController → CampusInventoryServiceImpl → CampusInventoryMapper。
// 列表返回 { rows, total }，详情/创建/范围预览返回 { data }；创建的 data 为 taskId。
export function listInventory(params) {
  return request({ url: '/campus/inventory/list', method: 'get', params })
}

export function getInventory(id) {
  return request({ url: `/campus/inventory/${id}`, method: 'get' })
}

// InventoryTaskDTO: taskName, deptId, scopeDeptId?, scopeLocationId?, scopeCategoryId?,
// startTime/endTime (yyyy-MM-dd HH:mm:ss), userIds[], remark?。修改仅允许未发布草稿。
export function addInventory(data) {
  return request({ url: '/campus/inventory', method: 'post', data })
}

export function updateInventory(id, data) {
  return request({ url: `/campus/inventory/${id}`, method: 'put', data })
}

export function getInventoryCandidates(id) {
  return request({ url: `/campus/inventory/${id}/candidates`, method: 'get' })
}

// data: { assignments: [{ assetId, userId }] }，必须完整覆盖预览范围。
export function publishInventory(id, data) {
  return request({ url: `/campus/inventory/${id}/publish`, method: 'post', data })
}

export function startInventory(id) {
  return request({ url: `/campus/inventory/${id}/start`, method: 'post' })
}

export function submitInventory(id) {
  return request({ url: `/campus/inventory/${id}/submit`, method: 'post' })
}

export function completeInventory(id) {
  return request({ url: `/campus/inventory/${id}/complete`, method: 'post' })
}

// 差异页/任务明细 → 本模块 → CampusInventoryReviewController → 同一 Service/Mapper。
// params: pageNum, pageSize, taskId?, keyword?, deptId?, status?(任务状态),
// reviewStatus?(0待复核/1已复核/2退回重盘), difference?(moved/damaged/missing/unseen)。
export function listInventoryItems(params) {
  return request({ url: '/campus/inventoryReview/list', method: 'get', params })
}

export function getInventoryItem(id) {
  return request({ url: `/campus/inventoryReview/${id}`, method: 'get' })
}

// data: approved, reviewNote, recordOnly?, resolvedLocationId?, resolvedCondition?,
// repairId?, createRepair?, faultType?, faultDescription?, faultLocation?。
// 不接收现场采集写入；退回只提交 approved:false 和 reviewNote。
export function reviewInventoryItem(id, data) {
  return request({ url: `/campus/inventoryReview/${id}/review`, method: 'post', data })
}

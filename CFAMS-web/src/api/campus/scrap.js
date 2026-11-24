import request from '@/utils/request'

// 申请页/审核页 → 本模块 → CampusScrapController → CampusScrapServiceImpl → CampusScrapMapper。
// params: pageNum, pageSize, keyword?, deptId?, applicant?, disposeType?, status?,
// handled?, beginTime?, endTime?；列表返回 { rows, total }，详情返回 { data }（含 audits/attachments）。
export function listScrap(params) {
  return request({ url: '/campus/scrap/list', method: 'get', params })
}

export function getScrap(id) {
  return request({ url: `/campus/scrap/${id}`, method: 'get' })
}

// data: { assetId, disposeType: '1'报废/'2'丢失注销, reason }；返回 data 为 scrapId。
// 取得 ID 后通过公共 attachment.js 上传 SCRAP 证明，不能将文件混入创建 DTO。
export function addScrap(data) {
  return request({ url: '/campus/scrap', method: 'post', data })
}

// data: { approved: boolean, opinion: string }；初审与复审是独立权限和状态操作。
export function firstAuditScrap(id, data) {
  return request({ url: `/campus/scrap/${id}/firstAudit`, method: 'post', data })
}

export function finalAuditScrap(id, data) {
  return request({ url: `/campus/scrap/${id}/finalAudit`, method: 'post', data })
}

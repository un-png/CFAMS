// 业务详情中的状态和日期显示，不负责页面、表单或接口分发。
export const statusLabels = {
  assets: ['在库', '借出', '维修中', '已报废'], borrow: ['待审核', '已驳回', '待领取', '借用中', '待归还确认', '已完成', '已取消'],
  handover: ['待确认', '已确认', '已拒绝'], repairs: ['待受理', '已派工', '维修中', '待核验', '已完成', '已驳回'],
  inventory: ['未开始', '执行中', '待复核', '已完成'], inventoryReview: ['待复核', '已复核', '退回重盘'],
  scrap: ['待初审', '待复审', '已驳回', '已注销'], messages: ['未读', '已读'],
  condition: ['正常', '损坏', '待核验'], enabled: ['正常', '停用']
}
const families = { labels: 'assets', borrowRecords: 'borrow', repairVerify: 'repairs', repairRecords: 'repairs', scrapAudit: 'scrap' }
export const family = key => families[key] || key

const choices = {
  personType: [['1', '学生'], ['2', '教职工']],
  locationType: [['1', '校区'], ['2', '楼栋'], ['3', '楼层'], ['4', '房间'], ['5', '其他']],
  disposeType: [['1', '报废'], ['2', '丢失注销']],
  bindingState: [['UNBOUND', '未绑定'], ['BOUND', '已绑定'], ['DISABLED', '已停用']],
  faultType: [['ELECTRICAL', '电气故障'], ['MECHANICAL', '机械故障'], ['SOFTWARE', '软件故障'], ['DAMAGE', '外观损坏'], ['OTHER', '其他']],
  messageType: [['BORROW_AUDIT', '借用审核'], ['PICKUP', '领取'], ['RETURN', '归还'], ['DUE_SOON', '临期'], ['OVERDUE', '逾期'], ['REPAIR', '维修'], ['INVENTORY', '盘点'], ['SCRAP', '报废']],
  reportType: [['ASSET', '资产数量与价值'], ['BORROW', '借用记录'], ['REPAIR', '报修与费用'], ['INVENTORY', '盘点差异'], ['SCRAP', '报废处置']],
  repairResult: [['1', '已修复'], ['2', '无法修复']], handoverType: [['1', '领取'], ['2', '归还']],
  borrowEnabled: [['0', '否'], ['1', '是']], visibility: [['1', '授权部门及关联人员'], ['0', '校内认证用户']]
}

export function displayValue(key, prop, value, options = {}) {
  if (value == null || value === '') return '—'
  if (prop === 'status') return (['people', 'categories', 'locations'].includes(key) ? statusLabels.enabled : statusLabels[family(key)])?.[value] || value
  if (prop === 'assetStatus') return statusLabels.assets[value] || value
  if (['conditionStatus', 'observedCondition', 'resolvedCondition'].includes(prop)) return statusLabels.condition[value] || value
  if (prop === 'reviewStatus') return statusLabels.inventoryReview[value] || value
  if (prop === 'readFlag') return statusLabels.messages[value] || value
  if (choices[prop]) return choices[prop].find(([code]) => code === String(value))?.[1] || value
  const type = { deptId: 'departments', assetDeptId: 'departments', locationId: 'locations', observedLocationId: 'locations', categoryId: 'categories', parentId: key === 'categories' ? 'categories' : 'locations' }[prop]
  if (type) return options[type]?.find(item => String(item.value) === String(value))?.label || `#${value}`
  if (prop === 'qrToken') return '已生成'
  if (prop === 'borrowEnabled') return value==='1'?'是':'否'
  if (prop === 'missingFlag') return value==='1'?'是':'否'
  if (/Time$|Date$/.test(prop)) return String(value).replace('T', ' ').replace(/(\.\d+)?\+\d{2}:\d{2}$/, '')
  return value
}

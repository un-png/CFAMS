import request from '@/utils/request'

// 部门、类别、场所、人员和资产的授权选项。
export function getOptions(type, keyword) {
  return request({
    url: `/campus/options/${type}`,
    method: 'get',
    params: { keyword }
  })
}

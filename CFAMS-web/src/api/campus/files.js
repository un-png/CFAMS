import request from '@/utils/request'
import { saveAs } from 'file-saver'

export function importFile(url, file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url, method: 'post', data, timeout: 120000,
    headers: { 'Content-Type': undefined, repeatSubmit: false }
  })
}

export async function getFile(url, method = 'get', params = {}) {
  const response = await request({
    url, method, responseType: 'blob', timeout: 120000,
    ...(method === 'get'
      ? { params }
      : { data: new URLSearchParams(Object.entries(params).filter(([, value]) => value != null && value !== '')) }),
    headers: method === 'get' ? {} : { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
  if (response.type.includes('json')) {
    const error = JSON.parse(await response.text())
    throw new Error(error.msg || '文件生成失败')
  }
  return response
}

export async function downloadFile(url, method, params, filename) {
  saveAs(await getFile(url, method, params), filename)
}

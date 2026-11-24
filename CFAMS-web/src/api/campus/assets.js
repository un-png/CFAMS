import request from '@/utils/request'
import { downloadFile, importFile } from './files'

export function listAssets(query) {
  return request({ url: '/campus/assets/list', method: 'get', params: query })
}

export function getAsset(id) {
  return request({ url: `/campus/assets/${id}`, method: 'get' })
}

export function addAsset(data) {
  return request({ url: '/campus/assets', method: 'post', data })
}

export function updateAsset(data) {
  return request({ url: '/campus/assets', method: 'put', data })
}

export function listAssetHistory(id, query) {
  return request({ url: `/campus/assets/${id}/history`, method: 'get', params: query })
}

export function importAssets(file) {
  return importFile('/campus/assets/import', file)
}

export function downloadAssetTemplate() {
  return downloadFile('/campus/assets/importTemplate', 'get', {}, '资产导入模板.xlsx')
}

export function exportAssets(query) {
  return downloadFile('/campus/assets/export', 'post', query, '资产台账.xlsx')
}

import request from '@/utils/request'
import { getFile } from './files'

export function listLabels(query) {
  return request({ url: '/campus/labels/list', method: 'get', params: query })
}

export function previewLabels(assetIds) {
  return request({ url: '/campus/labels/preview', method: 'post', data: { assetIds } })
}

export function printLabels(assetIds) {
  return request({ url: '/campus/labels/print', method: 'post', data: { assetIds } })
}

export function getLabelImage(id) {
  return getFile(`/campus/labels/${id}/image`)
}

import request from '@/utils/request'
import { downloadFile } from './files'

export function getDashboard(query) {
  return request({ url: '/campus/statistics/dashboard', method: 'get', params: query })
}

export function listReports(query) {
  return request({ url: '/campus/statistics/reports', method: 'get', params: query })
}

export function exportReports(query) {
  return downloadFile('/campus/statistics/reports/export', 'post', query, '校园资产统计.xlsx')
}

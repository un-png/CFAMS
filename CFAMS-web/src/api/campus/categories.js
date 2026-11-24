import request from '@/utils/request'

export function listCategories(params) {
  return request({ url: '/campus/categories/list', method: 'get', params })
}

export function getCategory(categoryId) {
  return request({ url: `/campus/categories/${categoryId}`, method: 'get' })
}

export function addCategory(data) {
  return request({ url: '/campus/categories', method: 'post', data })
}

export function updateCategory(data) {
  return request({ url: '/campus/categories', method: 'put', data })
}

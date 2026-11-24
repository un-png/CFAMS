import request from '@/utils/request'

export function listMessages(query) {
  return request({ url: '/campus/messages/list', method: 'get', params: query })
}

export function getMessage(id) {
  return request({ url: `/campus/messages/${id}`, method: 'get' })
}

export function readMessages(ids) {
  return request({ url: '/campus/messages/read', method: 'put', data: ids })
}

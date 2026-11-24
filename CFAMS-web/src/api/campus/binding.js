import request from '@/utils/request'

export function listBindings(params) {
  return request({ url: '/campus/binding', method: 'get', params })
}

export function issueBindingCredential(personId) {
  return request({ url: `/campus/binding/${personId}/issue`, method: 'post' })
}

export function rebindPerson(personId, data) {
  return request({ url: `/campus/binding/${personId}/rebind`, method: 'post', data })
}

import request from '@/utils/request'
import { downloadFile, importFile } from '@/api/campus/files'

export function listPeople(params) {
  return request({ url: '/campus/people', method: 'get', params })
}

export function getPerson(personId) {
  return request({ url: `/campus/people/${personId}`, method: 'get' })
}

export function addPerson(data) {
  return request({ url: '/campus/people', method: 'post', data })
}

export function updatePerson(personId, data) {
  return request({ url: `/campus/people/${personId}`, method: 'put', data })
}

export function disablePerson(personId) {
  return request({ url: `/campus/people/${personId}/disable`, method: 'put' })
}

export function importPeople(file) {
  return importFile('/campus/people/import', file)
}

export function downloadPeopleTemplate() {
  return downloadFile('/campus/people/importTemplate', 'post', {}, '校园人员名册模板.xlsx')
}

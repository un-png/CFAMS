import request from '@/utils/request'

export function listLocations(params) {
  return request({ url: '/campus/locations/list', method: 'get', params })
}

export function getLocation(locationId) {
  return request({ url: `/campus/locations/${locationId}`, method: 'get' })
}

export function addLocation(data) {
  return request({ url: '/campus/locations', method: 'post', data })
}

export function updateLocation(data) {
  return request({ url: '/campus/locations', method: 'put', data })
}

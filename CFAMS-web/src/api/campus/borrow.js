import request from "@/utils/request"

export function listBorrow(params) {
  return request({
    url: "/campus/borrow/list",
    method: "get",
    params
  })
}
export function listBorrowRecords(params) {
  return request({
    url: "/campus/borrow/records",
    method: "get",
    params
  })
}
export function getBorrow(id) {
  return request({
    url: `/campus/borrow/${id}`,
    method: "get"
  })
}
export function auditBorrow(id, data) {
  return request({
    url: `/campus/borrow/${id}/audit`,
    method: "put",
    data
  })
}
export function cancelBorrow(id, data) {
  return request({
    url: `/campus/borrow/${id}/cancel`,
    method: "put",
    data
  })
}
export function listHandovers(params) {
  return request({
    url: "/campus/handover/list",
    method: "get",
    params
  })
}
export function getHandover(id) {
  return request({
    url: `/campus/handover/${id}`,
    method: "get"
  })
}
export function confirmHandover(id, data) {
  return request({
    url: `/campus/handover/${id}/confirm`,
    method: "put",
    data
  })
}

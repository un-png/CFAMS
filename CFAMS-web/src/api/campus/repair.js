import request from "@/utils/request"

export function listRepairs(params) {
  return request({
    url: "/campus/repairs/list",
    method: "get",
    params
  })
}
export function listRepairVerification(params) {
  return request({
    url: "/campus/repairs/verification",
    method: "get",
    params
  })
}
export function listRepairRecords(params) {
  return request({
    url: "/campus/repairs/records",
    method: "get",
    params
  })
}
export function getRepair(id) {
  return request({
    url: `/campus/repairs/${id}`,
    method: "get"
  })
}
export function getRepairRecords(id) {
  return request({
    url: `/campus/repairs/${id}/records`,
    method: "get"
  })
}
export function assignRepair(id, data) {
  return request({
    url: `/campus/repairs/${id}/assign`,
    method: "put",
    data
  })
}
export function reassignRepair(id, data) {
  return request({
    url: `/campus/repairs/${id}/reassign`,
    method: "put",
    data
  })
}
export function verifyRepair(id, data) {
  return request({
    url: `/campus/repairs/${id}/verify`,
    method: "put",
    data
  })
}

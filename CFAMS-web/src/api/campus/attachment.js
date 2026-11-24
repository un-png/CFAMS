import request from '@/utils/request'

export function listAttachments(bizType, bizId) {
  return request({ url: '/campus/attachments/list', method: 'get', params: { bizType, bizId } })
}

export function getAttachmentUrl(id) {
  return request({ url: `/campus/attachments/${id}/url`, method: 'get' })
}

export function uploadAttachment(bizType, bizId, file) {
  const data = new FormData()
  data.append('bizType', bizType)
  data.append('bizId', bizId)
  data.append('file', file)
  return request({
    url: '/campus/attachments', method: 'post', data, timeout: 60000,
    headers: { 'Content-Type': undefined, repeatSubmit: false }
  })
}

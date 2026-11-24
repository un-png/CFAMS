// 从服务端授权菜单中选择可见页面，不绕过菜单隐藏、停用和角色授权。
export function firstVisibleMenu(routes, parent = '') {
  for (const route of routes) {
    if (route.hidden || route.meta?.link || /^https?:\/\//i.test(route.path)) continue
    const path = route.path.startsWith('/')
      ? route.path
      : `${parent}/${route.path}`.replace(/\/+/g, '/')
    if (route.children?.length) {
      const child = firstVisibleMenu(route.children, path)
      if (child) return child
    } else if (route.meta?.title && route.component) {
      return path
    }
  }
  return ''
}

export function defaultMenuPath(routes) {
  return firstVisibleMenu(routes) || '/user/profile'
}

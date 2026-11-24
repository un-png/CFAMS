-- PC派工和盘点人员选择使用的任务资格；不创建小程序页面或接口。
-- 与主角色分离，仍需具体任务指派；普通角色不自动授权。
START TRANSACTION;
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,menu_type,visible,status,perms,create_by,create_time,remark)
SELECT '维修执行资格',m.menu_id,99,'#','F','1','0','campus:repair:execute','admin',NOW(),'人员可被指派维修任务；不包含派工和核验权限'
FROM sys_menu m WHERE m.perms='campus:repairs:list'
AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='campus:repair:execute');
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,menu_type,visible,status,perms,create_by,create_time,remark)
SELECT '盘点执行资格',m.menu_id,99,'#','F','1','0','campus:inventory:execute','admin',NOW(),'人员可被分配盘点明细；不包含发布和复核权限'
FROM sys_menu m WHERE m.perms='campus:inventory:list'
AND NOT EXISTS(SELECT 1 FROM sys_menu WHERE perms='campus:inventory:execute');
INSERT INTO sys_role_menu(role_id,menu_id)
SELECT r.role_id,m.menu_id FROM sys_role r CROSS JOIN sys_menu m
WHERE r.role_key='admin' AND r.del_flag='0'
AND m.perms IN ('campus:repair:execute','campus:inventory:execute')
AND NOT EXISTS(SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);
COMMIT;

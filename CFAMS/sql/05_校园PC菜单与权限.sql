-- 校园PC页面菜单及按钮权限；依赖已有若依系统表。

-- 不覆盖菜单启停、显示状态或普通角色权限；业务接口尚未实现。

SET NAMES utf8mb4;

START TRANSACTION;

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3000,'统计分析',0,5,'campus/overview',NULL,'',1,1,'M','0','0','','chart','admin',NOW(),'校园PC端统计分析；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3000);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3000 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3000);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3001,'统计看板',3000,1,'dashboard','campus/Dashboard','CampusDashboard',1,1,'C','0','0','campus:dashboard:list','chart','admin',NOW(),'校园PC端统计看板；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3001);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3001 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3001);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3002,'统计报表',3000,2,'reports','campus/BusinessPage','CampusReports',1,1,'C','0','0','campus:reports:list','chart','admin',NOW(),'校园PC端统计报表；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3002);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3002 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3002);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3003,'导出报表',3002,1,'#',NULL,'',1,1,'F','0','0','campus:reports:export','#','admin',NOW(),'校园PC端导出报表；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3003);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3003 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3003);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3004,'资产管理',0,6,'campus/assets',NULL,'',1,1,'M','0','0','','table','admin',NOW(),'校园PC端资产管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3004);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3004 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3004);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3005,'资产台账',3004,1,'assets','campus/BusinessPage','CampusAssets',1,1,'C','0','0','campus:assets:list','table','admin',NOW(),'校园PC端资产台账；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3005);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3005 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3005);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3006,'登记资产',3005,1,'#',NULL,'',1,1,'F','0','0','campus:assets:add','#','admin',NOW(),'校园PC端登记资产；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3006);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3006 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3006);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3007,'批量导入',3005,2,'#',NULL,'',1,1,'F','0','0','campus:assets:import','#','admin',NOW(),'校园PC端批量导入；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3007);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3007 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3007);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3008,'导出明细',3005,3,'#',NULL,'',1,1,'F','0','0','campus:assets:export','#','admin',NOW(),'校园PC端导出明细；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3008);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3008 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3008);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3009,'详情',3005,4,'#',NULL,'',1,1,'F','0','0','campus:assets:query','#','admin',NOW(),'校园PC端详情；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3009);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3009 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3009);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3010,'修改',3005,5,'#',NULL,'',1,1,'F','0','0','campus:assets:edit','#','admin',NOW(),'校园PC端修改；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3010);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3010 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3010);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3011,'二维码标签',3004,2,'labels','campus/BusinessPage','CampusLabels',1,1,'C','0','0','campus:labels:list','table','admin',NOW(),'校园PC端二维码标签；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3011);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3011 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3011);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3012,'标签排版',3011,1,'#',NULL,'',1,1,'F','0','0','campus:labels:preview','#','admin',NOW(),'校园PC端标签排版；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3012);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3012 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3012);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3013,'批量打印',3011,2,'#',NULL,'',1,1,'F','0','0','campus:labels:print','#','admin',NOW(),'校园PC端批量打印；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3013);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3013 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3013);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3014,'借还管理',0,7,'campus/borrow',NULL,'',1,1,'M','0','0','','date','admin',NOW(),'校园PC端借还管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3014);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3014 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3014);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3015,'借用申请审核',3014,1,'borrow','campus/BusinessPage','CampusBorrow',1,1,'C','0','0','campus:borrow:list','date','admin',NOW(),'校园PC端借用申请审核；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3015);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3015 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3015);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3016,'查看',3015,1,'#',NULL,'',1,1,'F','0','0','campus:borrow:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3016);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3016 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3016);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3017,'审核',3015,2,'#',NULL,'',1,1,'F','0','0','campus:borrow:audit','#','admin',NOW(),'校园PC端审核；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3017);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3017 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3017);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3018,'借还交接',3014,2,'handover','campus/BusinessPage','CampusHandover',1,1,'C','0','0','campus:handover:list','date','admin',NOW(),'校园PC端借还交接；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3018);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3018 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3018);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3019,'查看',3018,1,'#',NULL,'',1,1,'F','0','0','campus:handover:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3019);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3019 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3019);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3020,'交接确认',3018,2,'#',NULL,'',1,1,'F','0','0','campus:handover:confirm','#','admin',NOW(),'校园PC端交接确认；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3020);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3020 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3020);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3021,'借用记录与逾期',3014,3,'borrowRecords','campus/BusinessPage','CampusBorrowRecords',1,1,'C','0','0','campus:borrowRecords:list','date','admin',NOW(),'校园PC端借用记录与逾期；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3021);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3021 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3021);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3022,'查看',3021,1,'#',NULL,'',1,1,'F','0','0','campus:borrowRecords:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3022);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3022 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3022);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3023,'维修管理',0,8,'campus/repair',NULL,'',1,1,'M','0','0','','tool','admin',NOW(),'校园PC端维修管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3023);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3023 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3023);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3024,'报修受理与派工',3023,1,'repairs','campus/BusinessPage','CampusRepairs',1,1,'C','0','0','campus:repairs:list','tool','admin',NOW(),'校园PC端报修受理与派工；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3024);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3024 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3024);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3025,'查看',3024,1,'#',NULL,'',1,1,'F','0','0','campus:repairs:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3025);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3025 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3025);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3026,'受理派工',3024,2,'#',NULL,'',1,1,'F','0','0','campus:repairs:assign','#','admin',NOW(),'校园PC端受理派工；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3026);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3026 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3026);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3027,'维修结果核验',3023,2,'repairVerify','campus/BusinessPage','CampusRepairVerify',1,1,'C','0','0','campus:repairVerify:list','tool','admin',NOW(),'校园PC端维修结果核验；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3027);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3027 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3027);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3028,'查看',3027,1,'#',NULL,'',1,1,'F','0','0','campus:repairVerify:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3028);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3028 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3028);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3029,'核验',3027,2,'#',NULL,'',1,1,'F','0','0','campus:repairVerify:verify','#','admin',NOW(),'校园PC端核验；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3029);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3029 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3029);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3030,'维修记录',3023,3,'repairRecords','campus/BusinessPage','CampusRepairRecords',1,1,'C','0','0','campus:repairRecords:list','tool','admin',NOW(),'校园PC端维修记录；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3030);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3030 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3030);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3031,'详情',3030,1,'#',NULL,'',1,1,'F','0','0','campus:repairRecords:query','#','admin',NOW(),'校园PC端详情；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3031);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3031 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3031);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3032,'盘点管理',0,9,'campus/inventory',NULL,'',1,1,'M','0','0','','list','admin',NOW(),'校园PC端盘点管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3032);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3032 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3032);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3033,'盘点任务',3032,1,'inventory','campus/BusinessPage','CampusInventory',1,1,'C','0','0','campus:inventory:list','list','admin',NOW(),'校园PC端盘点任务；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3033);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3033 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3033);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3034,'新建任务',3033,1,'#',NULL,'',1,1,'F','0','0','campus:inventory:add','#','admin',NOW(),'校园PC端新建任务；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3034);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3034 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3034);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3035,'详情',3033,2,'#',NULL,'',1,1,'F','0','0','campus:inventory:query','#','admin',NOW(),'校园PC端详情；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3035);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3035 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3035);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3036,'发布',3033,3,'#',NULL,'',1,1,'F','0','0','campus:inventory:publish','#','admin',NOW(),'校园PC端发布；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3036);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3036 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3036);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3037,'盘点差异复核',3032,2,'inventoryReview','campus/BusinessPage','CampusInventoryReview',1,1,'C','0','0','campus:inventoryReview:list','list','admin',NOW(),'校园PC端盘点差异复核；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3037);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3037 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3037);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3038,'查看',3037,1,'#',NULL,'',1,1,'F','0','0','campus:inventoryReview:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3038);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3038 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3038);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3039,'复核',3037,2,'#',NULL,'',1,1,'F','0','0','campus:inventoryReview:review','#','admin',NOW(),'校园PC端复核；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3039);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3039 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3039);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3040,'报废管理',0,10,'campus/scrap',NULL,'',1,1,'M','0','0','','documentation','admin',NOW(),'校园PC端报废管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3040);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3040 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3040);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3041,'报废申请',3040,1,'scrap','campus/BusinessPage','CampusScrap',1,1,'C','0','0','campus:scrap:list','documentation','admin',NOW(),'校园PC端报废申请；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3041);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3041 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3041);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3042,'新增申请',3041,1,'#',NULL,'',1,1,'F','0','0','campus:scrap:add','#','admin',NOW(),'校园PC端新增申请；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3042);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3042 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3042);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3043,'详情',3041,2,'#',NULL,'',1,1,'F','0','0','campus:scrap:query','#','admin',NOW(),'校园PC端详情；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3043);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3043 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3043);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3044,'报废分级审核',3040,2,'scrapAudit','campus/BusinessPage','CampusScrapAudit',1,1,'C','0','0','campus:scrapAudit:list','documentation','admin',NOW(),'校园PC端报废分级审核；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3044);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3044 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3044);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3045,'查看',3044,1,'#',NULL,'',1,1,'F','0','0','campus:scrapAudit:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3045);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3045 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3045);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3046,'初审',3044,2,'#',NULL,'',1,1,'F','0','0','campus:scrapAudit:firstAudit','#','admin',NOW(),'校园PC端初审；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3046);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3046 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3046);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3047,'复审',3044,3,'#',NULL,'',1,1,'F','0','0','campus:scrapAudit:finalAudit','#','admin',NOW(),'校园PC端复审；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3047);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3047 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3047);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3048,'校园基础配置',0,11,'campus/basic',NULL,'',1,1,'M','0','0','','tree','admin',NOW(),'校园PC端校园基础配置；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3048);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3048 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3048);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3049,'校园人员名册',3048,1,'people','campus/BusinessPage','CampusPeople',1,1,'C','0','0','campus:people:list','tree','admin',NOW(),'校园PC端校园人员名册；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3049);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3049 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3049);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3050,'新增人员',3049,1,'#',NULL,'',1,1,'F','0','0','campus:people:add','#','admin',NOW(),'校园PC端新增人员；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3050);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3050 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3050);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3051,'导入名册',3049,2,'#',NULL,'',1,1,'F','0','0','campus:people:import','#','admin',NOW(),'校园PC端导入名册；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3051);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3051 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3051);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3052,'修改',3049,3,'#',NULL,'',1,1,'F','0','0','campus:people:edit','#','admin',NOW(),'校园PC端修改；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3052);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3052 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3052);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3053,'身份绑定管理',3048,2,'binding','campus/BusinessPage','CampusBinding',1,1,'C','0','0','campus:binding:list','tree','admin',NOW(),'校园PC端身份绑定管理；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3053);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3053 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3053);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3054,'发放凭据',3053,1,'#',NULL,'',1,1,'F','0','0','campus:binding:issue','#','admin',NOW(),'校园PC端发放凭据；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3054);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3054 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3054);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3055,'处理换绑',3053,2,'#',NULL,'',1,1,'F','0','0','campus:binding:rebind','#','admin',NOW(),'校园PC端处理换绑；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3055);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3055 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3055);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3056,'资产类别',3048,3,'categories','campus/BusinessPage','CampusCategories',1,1,'C','0','0','campus:categories:list','tree','admin',NOW(),'校园PC端资产类别；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3056);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3056 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3056);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3057,'新增类别',3056,1,'#',NULL,'',1,1,'F','0','0','campus:categories:add','#','admin',NOW(),'校园PC端新增类别；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3057);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3057 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3057);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3058,'修改',3056,2,'#',NULL,'',1,1,'F','0','0','campus:categories:edit','#','admin',NOW(),'校园PC端修改；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3058);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3058 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3058);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3059,'资产场所',3048,4,'locations','campus/BusinessPage','CampusLocations',1,1,'C','0','0','campus:locations:list','tree','admin',NOW(),'校园PC端资产场所；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3059);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3059 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3059);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3060,'新增场所',3059,1,'#',NULL,'',1,1,'F','0','0','campus:locations:add','#','admin',NOW(),'校园PC端新增场所；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3060);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3060 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3060);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3061,'修改',3059,2,'#',NULL,'',1,1,'F','0','0','campus:locations:edit','#','admin',NOW(),'校园PC端修改；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3061);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3061 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3061);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3062,'业务消息',0,12,'campus/inbox',NULL,'',1,1,'M','0','0','','message','admin',NOW(),'校园PC端业务消息；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3062);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3062 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3062);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3063,'业务消息',3062,1,'messages','campus/BusinessPage','CampusMessages',1,1,'C','0','0','campus:messages:list','message','admin',NOW(),'校园PC端业务消息；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3063);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3063 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3063);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3064,'标记已读',3063,1,'#',NULL,'',1,1,'F','0','0','campus:messages:read','#','admin',NOW(),'校园PC端标记已读；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3064);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3064 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3064);

INSERT INTO sys_menu (menu_id,menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) SELECT 3065,'查看',3063,2,'#',NULL,'',1,1,'F','0','0','campus:messages:query','#','admin',NOW(),'校园PC端查看；页面结构阶段，业务处理未接入。' WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=3065);

INSERT INTO sys_role_menu (role_id,menu_id) SELECT r.role_id,3065 FROM sys_role r WHERE r.role_key='admin' AND r.del_flag='0' AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=3065);

COMMIT;

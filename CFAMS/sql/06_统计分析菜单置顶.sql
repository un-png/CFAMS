-- 保留管理员已有的显示、停用和角色授权，仅将统计分析目录排到首位。
START TRANSACTION;
SET @cfams_first_order = (
    SELECT COALESCE(MIN(order_num), 0) - 1
    FROM sys_menu
    WHERE parent_id = 0 AND path <> 'campus/overview'
);
UPDATE sys_menu
SET order_num = @cfams_first_order
WHERE parent_id = 0 AND path = 'campus/overview' AND menu_type = 'M';
COMMIT;

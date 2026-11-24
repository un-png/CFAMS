package com.cfams.campus.mapper;

import org.apache.ibatis.annotations.Select;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.cfams.campus.domain.vo.CampusOptionVO;
import com.cfams.campus.domain.dto.CampusQueryDTO;

/** 页面下拉选项查询，只返回业务主键与显示名称。 */
public interface CampusOptionMapper {
    /** 查询数据范围内有效部门的选项。 */
    List<CampusOptionVO> departments(@Param("q") CampusQueryDTO q);
    /** 查询授权范围内有效用户，可额外按维修或盘点任务资格筛选。 */
    List<CampusOptionVO> users(@Param("q") CampusQueryDTO q, @Param("taskPermission") String permission);
    /** 查询启用的资产类别选项。 */
    @Select(
            "SELECT category_id AS value,CONCAT(category_name,' (',category_code,')') AS label FROM ca_category WHERE " +
            "status='0' ORDER BY order_num,category_id")
    List<CampusOptionVO> categories();
    /** 查询授权部门场所及未归属部门的公共场所。 */
    List<CampusOptionVO> locations(@Param("q") CampusQueryDTO q);
    /** 按关键字查询授权范围内尚未注销的资产选项。 */
    List<CampusOptionVO> assets(@Param("q") CampusQueryDTO q);
}

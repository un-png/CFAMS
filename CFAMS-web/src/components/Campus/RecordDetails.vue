<template>
  <el-descriptions v-if="record" :column="2" border>
    <el-descriptions-item v-for="[key,value] in entries" :key="key" :label="labels[key] || key" :span="typeof value === 'string' && value.length > 80 ? 2 : 1">
      <span class="detail-value">{{ displayValue(pageKey,key,value,options) }}</span>
    </el-descriptions-item>
  </el-descriptions>
  <el-empty v-else description="暂无详情" />
</template>
<script setup>
import { computed } from 'vue'
import { displayValue } from '@/utils/campusDisplay'
const props=defineProps({ record:Object, pageKey:String, options:{type:Object,default:()=>({})} })
const labels={
  assetId:'资产ID',assetCode:'资产编号',assetName:'资产名称',categoryId:'类别',categoryName:'类别',deptId:'所属部门',deptName:'所属部门',assetDeptId:'资产部门',
  locationId:'场所',locationName:'场所',keeperUserId:'责任人ID',keeperName:'责任人',purchaseDate:'采购日期',purchasePrice:'登记金额',specification:'规格型号',
  supplierName:'供应商',expectedUseYears:'预计使用年限',assetStatus:'资产状态',conditionStatus:'完好情况',borrowEnabled:'开放借用',visibility:'可见范围',
  registeredTime:'入册时间',scrappedTime:'注销时间',remark:'备注',createBy:'创建人',createTime:'创建时间',updateTime:'更新时间',
  borrowId:'借用ID',borrowNo:'借用单号',applicantName:'申请人',applicantUserId:'申请人ID',purpose:'用途',plannedStartTime:'预计领取',plannedEndTime:'预计归还',
  actualPickupTime:'实际领取',actualReturnTime:'实际归还',auditOpinion:'审核意见',auditTime:'审核时间',auditUserId:'审核人ID',status:'状态',applyTime:'申请时间',
  handoverId:'交接ID',handoverType:'交接类型',requestTime:'申请交接时间',requestNote:'申请说明',confirmTime:'确认时间',confirmNote:'交接意见',
  repairId:'维修ID',repairNo:'报修单号',reporterName:'报修人',repairerName:'维修人',faultType:'故障类型',faultDescription:'故障说明',faultTime:'故障时间',
  faultLocation:'故障地点',reportTime:'报修时间',repairPlan:'维修方案',repairCost:'维修费用',repairResult:'维修结果',resultDescription:'结果说明',verifyOpinion:'核验意见',
  feedbackTime:'反馈时间',verifyTime:'核验时间',acceptOpinion:'受理意见',assignTime:'派工时间',startTime:'开始时间',endTime:'结束时间',
  taskId:'任务ID',taskNo:'任务编号',taskName:'任务名称',publishTime:'发布时间',totalCount:'待盘数量',checkedCount:'已盘数量',reviewedCount:'已复核数量',
  itemId:'明细ID',assignedUserId:'执行人ID',assignedUserName:'执行人',checkedTime:'盘点时间',observedLocationId:'现场场所',observedCondition:'现场完好',
  missingFlag:'疑似丢失',checkNote:'现场说明',reviewStatus:'复核状态',reviewNote:'复核结论',reviewTime:'复核时间',resolvedCondition:'复核完好',resolvedLocationId:'复核位置',
  scrapId:'处置ID',scrapNo:'处置单号',disposeType:'处置类型',reason:'处置原因',completedTime:'完成时间',completeTime:'结项时间',
  personId:'人员ID',personNo:'学号 / 工号',realName:'姓名',personType:'人员类型',userName:'关联账号',bindingState:'绑定状态',bindTime:'绑定时间',
  title:'标题',content:'内容',messageId:'消息ID',messageType:'消息类型',bizType:'业务类型',bizId:'业务ID',readFlag:'阅读状态',readTime:'阅读时间',
  categoryCode:'类别编码',locationCode:'场所编码',locationType:'场所类型',orderNum:'排序',parentId:'上级节点',
  cancelReason:'取消原因',cancelTime:'取消时间',scopeDeptId:'范围部门ID',scopeCategoryId:'范围类别ID',scopeLocationId:'范围场所ID'
}
const entries=computed(()=>Object.entries(props.record||{}).filter(([key,value])=>value!=null && typeof value!=='object' && !['qrToken','voucherToken','baseSnapshot','reviewSnapshot','beforeSnapshot','afterSnapshot','bindingId','appid'].includes(key)))
</script>
<style scoped>.detail-value{white-space:pre-wrap;overflow-wrap:anywhere;}</style>

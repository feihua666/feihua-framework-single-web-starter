package com.feihua.starter.service.impl;

import com.feihua.exception.BaseException;
import com.feihua.exception.DataNotFoundException;

import com.feihua.framework.activity.ActUtils;
import com.feihua.framework.activity.api.ApiActivitiBusinessService;
import com.feihua.framework.activity.api.ApiActivitiCommonService;
import com.feihua.framework.activity.dto.AdditionalParam;
import com.feihua.framework.activity.dto.ApplyParamDto;
import com.feihua.framework.activity.dto.CompleteTaskParamDto;
import com.feihua.framework.base.modules.office.api.ApiBaseOfficePoService;
import com.feihua.framework.base.modules.role.api.ApiBaseRolePoService;
import com.feihua.framework.base.modules.role.dto.BaseRoleDto;
import com.feihua.framework.base.modules.role.po.BaseRolePo;
import com.feihua.framework.constants.DictEnum;
import com.feihua.starter.service.modules.leave.api.ApiOaLeavePoService;
import com.feihua.starter.service.modules.leave.dto.LeaveApplyParamDto;
import com.feihua.starter.service.modules.leave.dto.OaLeaveDto;
import com.feihua.starter.service.modules.leave.dto.SearchLeavesConditionDto;
import com.feihua.starter.service.modules.leave.po.OaLeavePo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import feihua.jdbc.api.pojo.BasePo;
import feihua.jdbc.api.pojo.PageAndOrderbyParamDto;
import feihua.jdbc.api.pojo.PageResultDto;
import feihua.jdbc.api.service.impl.ApiBaseServiceImpl;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.feihua.starter.service.mapper.OaLeavePoMapper;


/**
 * This class was generated by MyBatis Generator.
 * @author yangwei 2018-04-11 14:57:50
 */
@Service
public class ApiOaLeavePoServiceImpl extends ApiBaseServiceImpl<OaLeavePo, OaLeaveDto, String> implements ApiOaLeavePoService,ApiActivitiBusinessService {

    public ApiOaLeavePoServiceImpl() {
        super(OaLeaveDto.class);
    }

    @Autowired
    private OaLeavePoMapper oaLeavePoMapper;
    @Autowired
    private ApiActivitiCommonService apiActivitiCommonService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ApiBaseRolePoService apiBaseRolePoService;
    @Autowired
    private ApiBaseOfficePoService apiBaseOfficePoService;

    @Transactional( propagation = Propagation.SUPPORTS, readOnly = true )
    @Override
    public PageResultDto<OaLeaveDto> searchLeavesDsf(SearchLeavesConditionDto searchLeavesConditionDto, PageAndOrderbyParamDto pageAndOrderbyParamDto) {
        super.pageAndOrderbyStart(pageAndOrderbyParamDto);
        Page p = PageHelper.getLocalPage();
        List<OaLeavePo> list = oaLeavePoMapper.searchLeavesDsf(searchLeavesConditionDto);
        return new PageResultDto(this.wrapDtos(list), this.wrapPage(p));
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProcInsIdByBusinessId(String procInsId, String BusinessId, AdditionalParam param) {
        OaLeavePo entity = new OaLeavePo();
        entity.setId(BusinessId);
        entity.setProcessInstanceId(procInsId);
        entity.setApplyTime(new Date());
        oaLeavePoMapper.updateByPrimaryKeySelective(entity);
    }

    /**
     *
     * @param procInsId
     * @param BusinessId
     * @param param 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onStartProcessComplete(String procInsId, String BusinessId, AdditionalParam param) {

        //设置部门负责人审批
        Map<String, Object> vars = ActUtils.createVariableMap();
        BaseRolePo rolePoCondition = new BaseRolePo();
        rolePoCondition.setType(DictEnum.RoleType.departmentmanager.name());
        rolePoCondition.setDelFlag(BasePo.YesNo.N.name());

        List<BaseRoleDto> roleDtos = apiBaseRolePoService.selectList(rolePoCondition);
        if (roleDtos != null && !roleDtos.isEmpty()) {
            List<String> roleIds = new ArrayList<>();
            for (BaseRoleDto roleDto : roleDtos) {
                roleIds.add(roleDto.getId());
            }
            ActUtils.putVariableRoleIds(vars,roleIds);
        }else {
            throw new DataNotFoundException("can not find department aduitor");
        }

        //完成请假任务
        apiActivitiCommonService.completeFirstTask(param.getCurrentUserId(),procInsId,null,null,vars);

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void apply(ApplyParamDto param) {
        LeaveApplyParamDto leaveApplyParamDto = ((LeaveApplyParamDto) param);
        OaLeavePo dbEntity = oaLeavePoMapper.selectByPrimaryKey(leaveApplyParamDto.getLeaveId());

        if (dbEntity == null) {
            throw new DataNotFoundException("leave not found for id:" + leaveApplyParamDto.getLeaveId());
        }
        // 已经审请
        if(StringUtils.isNotEmpty(dbEntity.getProcessInstanceId())){
            throw new BaseException("have applied");
        }
        Map<String, Object> vars = ActUtils.createVariableMap();
        ActUtils.putVariableUserId(vars, leaveApplyParamDto.getCurrentUserId());
        ProcessInstance procIns = apiActivitiCommonService.startProcess("process_leave",leaveApplyParamDto.getLeaveId(),dbEntity.getReason(),vars,leaveApplyParamDto.getCurrentUserId());
        // 请在这里调用
        this.updateProcInsIdByBusinessId(procIns.getId(),leaveApplyParamDto.getLeaveId(),null);
        AdditionalParam additionalParam = new AdditionalParam();
        additionalParam.setCurrentRoleId(param.getCurrentRoleId());
        additionalParam.setCurrentUserId(param.getCurrentUserId());
        this.onStartProcessComplete(procIns.getId(),leaveApplyParamDto.getLeaveId(),additionalParam);

    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeTask(CompleteTaskParamDto completeTaskParamDto) {

        String flag = completeTaskParamDto.getFlag();
        // leaveId
        String businessId = completeTaskParamDto.getBusinessId();

        // 完成流程任务
        Map<String, Object> vars = ActUtils.createVariableMap();

        vars.put("flag", "1".equals(flag) ? flag:"0");
        OaLeavePo dbEntity = oaLeavePoMapper.selectByPrimaryKey(businessId);
        if (dbEntity == null) {
            throw new DataNotFoundException("leave not found for id:" + businessId);
        }
        if(!completeTaskParamDto.getProcInsId().equals(dbEntity.getProcessInstanceId())){
            throw new BaseException("expected procInsId=" + dbEntity.getProcessInstanceId() + " found procInsId=" + completeTaskParamDto.getProcInsId());
        }
        //部门审批
        if("departmentaudit".equals(completeTaskParamDto.getTaskDefKey())){

            //如果同意设置人事审批人
            if("1".equals(flag)){
                BaseRolePo rolePoCondition = new BaseRolePo();
                rolePoCondition.setType(DictEnum.RoleType.hr.name());
                rolePoCondition.setDelFlag(BasePo.YesNo.N.name());

                List<BaseRoleDto> roleDtos = apiBaseRolePoService.selectList(rolePoCondition);
                if (roleDtos != null && !roleDtos.isEmpty()) {
                    List<String> roleIds = new ArrayList<>();
                    for (BaseRoleDto roleDto : roleDtos) {
                        roleIds.add(roleDto.getId());
                    }
                    ActUtils.putVariableRoleIds(vars,roleIds);
                }
            }else {
                //不同意
                HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("apply_leave").processInstanceBusinessKey(businessId).processInstanceId(completeTaskParamDto.getProcInsId()).singleResult();
                ActUtils.putVariableUserId(vars,task.getAssignee());
            }

        }
        //人事审批
        else if("hraduit".equals(completeTaskParamDto.getTaskDefKey())){
            if("1".equals(flag)){
                //同意就直接结束了
            }else {
                //不同意
                HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                        .taskDefinitionKey("apply_leave")
                        .processInstanceBusinessKey(businessId)
                        .processInstanceId(completeTaskParamDto.getProcInsId())
                        .singleResult();
                ActUtils.putVariableUserId(vars,task.getAssignee());
            }
        }
        //重新修改申请
        else if("updateaplly".equals(completeTaskParamDto.getTaskDefKey())){
            if("1".equals(flag)){
                //设置部门审批人
                BaseRolePo rolePoCondition = new BaseRolePo();
                rolePoCondition.setType(DictEnum.RoleType.departmentmanager.name());
                rolePoCondition.setDelFlag(BasePo.YesNo.N.name());

                List<BaseRoleDto> roleDtos = apiBaseRolePoService.selectList(rolePoCondition);
                if (roleDtos != null && !roleDtos.isEmpty()) {
                    List<String> roleIds = new ArrayList<>();
                    for (BaseRoleDto roleDto : roleDtos) {
                        roleIds.add(roleDto.getId());
                    }
                    ActUtils.putVariableRoleIds(vars,roleIds);
                }

            }else {
                //直接结束
            }
        }
        //设置审批人
        apiActivitiCommonService.complete(completeTaskParamDto.getTaskId(),completeTaskParamDto.getProcInsId(),completeTaskParamDto.getComment(),dbEntity.getReason(),vars);
    }

    @Override
    public OaLeaveDto wrapDto(OaLeavePo po) {
        OaLeaveDto oaLeaveDto = new OaLeaveDto();
        oaLeaveDto.setStartTime(po.getStartTime());
        oaLeaveDto.setApplyTime(po.getApplyTime());
        oaLeaveDto.setDataAreaId(po.getDataAreaId());
        oaLeaveDto.setEndTime(po.getEndTime());
        oaLeaveDto.setDataUserId(po.getDataUserId());
        oaLeaveDto.setId(po.getId());
        oaLeaveDto.setDataType(po.getDataType());
        oaLeaveDto.setLeaveType(po.getLeaveType());
        oaLeaveDto.setReason(po.getReason());
        oaLeaveDto.setUpdateAt(po.getUpdateAt());
        oaLeaveDto.setRealityEndTime(po.getRealityEndTime());
        oaLeaveDto.setRealityStartTime(po.getRealityStartTime());
        oaLeaveDto.setDataOfficeId(po.getDataOfficeId());
        oaLeaveDto.setProcessInstanceId(po.getProcessInstanceId());
        return oaLeaveDto;
    }
}
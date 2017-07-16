package com.cnpc.framework.base.service.impl;

import com.cnpc.framework.base.entity.FunctionFilter;
import com.cnpc.framework.base.entity.RoleFunction;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.RoleFunctionService;
import com.cnpc.framework.utils.StrUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by billJiang on 2017/1/3.
 * e-mail:jrn1012@petrochina.com.cn qq:475572229
 * 角色授权服务
 */
@Service("roleFunctionService")
public class RoleFunctionServiceImpl extends BaseServiceImpl implements RoleFunctionService {
    @Override
    public Result deleteRoleFunction(String id) {
        //delete functionfilter first
        RoleFunction roleFunction = this.get(RoleFunction.class, id);
        String hql = "delete from FunctionFilter where roleId='" + roleFunction.getRoleId() + "' and functionId='"+roleFunction.getFunctionId()+"'";
        this.executeHql(hql);
        //delete rolefunction entity
        this.delete(roleFunction);
        return new Result();
    }

    @Override
    public Result saveRoleFunction(RoleFunction rfobj) {
        if (!StrUtil.isEmpty(rfobj.getId())) {
            deleteRoleFunction(rfobj.getId());
        }
       this.save(rfobj).toString();
       return saveBatchFunctionFilter(rfobj.getRoleId(),rfobj.getFunctionId(),rfobj.getFflist());
    }

    @Override
    public RoleFunction getRoleFunction(String roleId, String functionId) {
        RoleFunction roleFunction = this.get("from RoleFunction where roleId='" + roleId + "' and functionId='" + functionId + "'");
        if (roleFunction == null)
            return new RoleFunction();
        return roleFunction;
    }

    @Override
    public Result saveBatchRoleFunction(String roleId, List<RoleFunction> roleFunctionList) {
        String hql="delete from RoleFunction where roleId='"+roleId+"'";
        this.executeHql(hql);
        this.batchSave(roleFunctionList);
        return new Result();
    }

    @Override
    public Result saveBatchFunctionFilter(String roleId, String functionId, List<FunctionFilter> functionFilterList) {
        String hql="delete from FunctionFilter where roleId='"+roleId+"' and functionId='"+functionId+"'";
        this.executeHql(hql);
        this.batchSave(functionFilterList);
        return new Result();
    }
}

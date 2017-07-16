package com.cnpc.framework.base.service.impl;

import com.cnpc.framework.utils.PropertiesUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Role;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.RoleService;

import java.util.*;

@Service("roleService")
public class RoleServiceImpl extends BaseServiceImpl implements RoleService {

    @Override
    public Result delete(String id) {

        String hql = "from UserRole where roleId='" + id + "'";
        if (this.find(hql).isEmpty()) {
            Role role = this.get(Role.class, id);
            this.delete(role);
            return new Result(true);
        }
        return new Result(false, "该角色已经绑定用户，请先解绑用户");
    }

    @Override
    public Set<String> getRoleCodeSet(String username) {
        String sql= PropertiesUtil.getValue("shiro.sql.roles");
        Map<String,Object> params=new HashMap<String,Object>();
        params.put("param",username);
        List list=super.findMapBySql(sql,params);
        Set<String> retSet=new HashSet<String>();
        retSet.addAll(list);
        return retSet;
    }
}

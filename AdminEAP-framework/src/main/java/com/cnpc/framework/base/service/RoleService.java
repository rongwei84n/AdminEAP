package com.cnpc.framework.base.service;

import com.cnpc.framework.base.pojo.Result;

import java.util.Set;

public interface RoleService extends BaseService {

    Result delete(String id);

    /**
     * 根据登录名，获取角色集合
     * @param username 登录名
     * @return 角色编码集合
     */
    Set<String> getRoleCodeSet(String username);
}

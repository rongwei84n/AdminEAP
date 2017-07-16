package com.cnpc.framework.base.service.impl;

import java.util.*;

import com.cnpc.framework.utils.TreeUtil;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Function;
import com.cnpc.framework.base.pojo.TreeNode;
import com.cnpc.framework.base.service.FunctionService;
import com.cnpc.framework.utils.StrUtil;

@Service("functionService")
public class FunctionServiceImpl extends BaseServiceImpl implements FunctionService {

    @Override
    public List<TreeNode> getTreeData() {

        // 获取数据
        String hql = "from Function order by levelCode asc";
        List<Function> funcs = this.find(hql);
        Map<String, TreeNode> nodelist = new LinkedHashMap<String, TreeNode>();
        for (Function func : funcs) {
            TreeNode node = new TreeNode();
            node.setText(func.getName());
            node.setId(func.getId());
            node.setParentId(func.getParentId());
            node.setLevelCode(func.getLevelCode());
            node.setIcon(func.getIcon());
            nodelist.put(node.getId(), node);
        }
        // 构造树形结构
        return TreeUtil.getNodeList(nodelist);
    }

    @Override
    public List<Function> getAll() {

        String hql = "from Function where (deleted=0 or deleted is null) order by levelCode";
        return this.find(hql);
    }

    @Override
    public Set<String> getFunctionCodeSet(Set<String> roleCodes) {
        return null;
    }
}

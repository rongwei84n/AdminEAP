package com.cnpc.framework.base.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cnpc.framework.utils.TreeUtil;
import org.springframework.stereotype.Service;

import com.cnpc.framework.base.entity.Dict;
import com.cnpc.framework.base.pojo.TreeNode;
import com.cnpc.framework.base.service.DictService;
import com.cnpc.framework.utils.StrUtil;

@Service("dictService")
public class DictServiceImpl extends BaseServiceImpl implements DictService {

    @Override
    public List<TreeNode> getTreeData() {

        // 获取数据
        String hql = "from Dict order by levelCode asc";
        List<Dict> dicts = this.find(hql);
        Map<String, TreeNode> nodelist = new LinkedHashMap<String, TreeNode>();
        for (Dict dict : dicts) {
            TreeNode node = new TreeNode();
            node.setText(dict.getName());
            node.setId(dict.getId());
            node.setParentId(dict.getParentId());
            node.setLevelCode(dict.getLevelCode());
            nodelist.put(node.getId(), node);
        }
        // 构造树形结构
        List<TreeNode> tnlist = TreeUtil.getNodeList(nodelist);
        return tnlist;
    }

    public List<Dict> getDictsByCode(String code){
        String hql="from Dict where code='"+code+"'";
        Dict dict=this.get(hql);
        return this.find("from Dict where parentId='"+dict.getId()+"' order by levelCode");
    }
}

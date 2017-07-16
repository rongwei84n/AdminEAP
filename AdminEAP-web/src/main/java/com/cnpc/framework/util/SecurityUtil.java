package com.cnpc.framework.util;

import com.cnpc.framework.base.entity.User;
import org.apache.shiro.SecurityUtils;

/**
 * Created by billJiang on 2017/2/6.
 * e-mail:jrn1012@petrochina.com.cn qq:475572229
 */
public class SecurityUtil {

    public static String getUserId(){
       return getUser().getId();
    }

    public static User getUser(){
        return (User)SecurityUtils.getSubject().getPrincipal();
    }
}

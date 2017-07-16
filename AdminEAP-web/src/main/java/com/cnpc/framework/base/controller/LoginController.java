package com.cnpc.framework.base.controller;

import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.pojo.ResultCode;
import com.cnpc.framework.base.service.RoleService;
import com.cnpc.framework.base.service.UserService;
import com.cnpc.framework.oauth.common.CustomOAuthService;
import com.cnpc.framework.oauth.entity.OAuthUser;
import com.cnpc.framework.oauth.service.OAuthServices;
import com.cnpc.framework.oauth.service.OAuthUserService;
import com.cnpc.framework.utils.EncryptUtil;
import com.cnpc.framework.utils.PropertiesUtil;
import com.cnpc.framework.utils.StrUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private RoleService roleService;

    @Resource
    private UserService userService;

    @Autowired
    OAuthServices oAuthServices;

    @Resource
    OAuthUserService oAuthUserService;

    private final static String MAIN_PAGE = PropertiesUtil.getValue("page.main");
    private final static String LOGIN_PAGE = PropertiesUtil.getValue("page.login");
    private final static String REGISTER_PAGE = PropertiesUtil.getValue("page.register");

    @RequestMapping(value = "/login")
    private String doLogin(HttpServletRequest request, Model model) {
        logger.debug("LoginController begin..");
        model.addAttribute("oAuthServices", oAuthServices.getAllOAuthServices());
        //已经登录过，直接进入主页
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            boolean isAuthorized = Boolean.valueOf(subject.getSession().getAttribute("isAuthorized").toString());
            if (isAuthorized) {
                System.out.println("LoginController begin 2..");
                return MAIN_PAGE;
            }
        }
        String userName = request.getParameter("userName");
        logger.debug("userName [{}] ", userName);
        
        //默认首页，第一次进来
        if (StrUtil.isEmpty(userName)) {
            logger.info("userName is empty, return login page");
            return LOGIN_PAGE;
        }
        
        String password = request.getParameter("password");
        //密码加密+加盐
        password = EncryptUtil.getPassword(password, userName);
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        token.setRememberMe(true);
        subject = SecurityUtils.getSubject();
        String msg;
        try {
            subject.login(token);
            //通过认证
            if (subject.isAuthenticated()) {
                Set<String> roles = roleService.getRoleCodeSet(userName);
                if (!roles.isEmpty()) {
                    subject.getSession().setAttribute("isAuthorized", true);
                    return MAIN_PAGE;
                } else {//没有授权
                    msg = "您没有得到相应的授权！";
                    model.addAttribute("message", new ResultCode("1", msg));
                    subject.getSession().setAttribute("isAuthorized", false);
                    logger.error(msg);
                    return LOGIN_PAGE;
                }
            } else {
                return LOGIN_PAGE;
            }
            //0 未授权 1 账号问题 2 密码错误  3 账号密码错误
        } catch (IncorrectCredentialsException e) {
            msg = "登录密码错误. Password for account " + token.getPrincipal() + " was incorrect";
            model.addAttribute("message", new ResultCode("2", msg));
            logger.error(msg, e);
        } catch (ExcessiveAttemptsException e) {
            msg = "登录失败次数过多";
            model.addAttribute("message", new ResultCode("3", msg));
            logger.error(msg, e);
        } catch (LockedAccountException e) {
            msg = "帐号已被锁定. The account for username " + token.getPrincipal() + " was locked.";
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg, e);
        } catch (DisabledAccountException e) {
            msg = "帐号已被禁用. The account for username " + token.getPrincipal() + " was disabled.";
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg, e);
        } catch (ExpiredCredentialsException e) {
            msg = "帐号已过期. the account for username " + token.getPrincipal() + "  was expired.";
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg, e);
        } catch (UnknownAccountException e) {
            msg = "帐号不存在. There is no user with username of " + token.getPrincipal();
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg, e);
        } catch (UnauthorizedException e) {
            msg = "您没有得到相应的授权！" + e.getMessage();
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg, e);
        }
        logger.info("LoginController doLogin done..");
        return LOGIN_PAGE;
    }


    @RequestMapping(value = "/logout")
    private String doLogout(Model model) {
        model.addAttribute("oAuthServices", oAuthServices.getAllOAuthServices());
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return LOGIN_PAGE;
    }

    @RequestMapping(value="/register",method = RequestMethod.GET)
    public String register(Model model){
        model.addAttribute("oAuthInfo", new OAuthUser());
        return REGISTER_PAGE;
    }


    //----------------oauth 认证------------------
    @RequestMapping(value = "/oauth/{type}/callback", method = RequestMethod.GET)
    public String callback(@RequestParam(value = "code", required = true) String code, @PathVariable(value = "type") String type,
                           HttpServletRequest request, Model model) {
        model.addAttribute("oAuthServices", oAuthServices.getAllOAuthServices());
        try {
            CustomOAuthService oAuthService = oAuthServices.getOAuthService(type);
            Token accessToken = oAuthService.getAccessToken(null, new Verifier(code));
            //第三方授权返回的用户信息
            OAuthUser oAuthInfo = oAuthService.getOAuthUser(accessToken);
            //查询本地数据库中是否通过该方式登陆过
            OAuthUser oAuthUser = oAuthUserService.findByOAuthTypeAndOAuthId(oAuthInfo.getoAuthType(), oAuthInfo.getoAuthId());
            //未建立关联，转入用户注册界面
            if (oAuthUser == null) {
                model.addAttribute("oAuthInfo", oAuthInfo);
                return REGISTER_PAGE;
            }

            //如果已经过关联，直接登录
            User user = userService.get(User.class, oAuthUser.getUserId());
            return loginByAuth(user);
        }catch (Exception e){
            String msg = "连接"+type+"服务器异常. 错误信息为："+e.getMessage();
            model.addAttribute("message", new ResultCode("1", msg));
            logger.error(msg);
            return LOGIN_PAGE;
        }

    }

    @RequestMapping(value = "/oauth/register", method = RequestMethod.POST)
    public String register_oauth(User user, @RequestParam(value = "oAuthType", required = false, defaultValue = "") String oAuthType,
                           @RequestParam(value = "oAuthId", required = true, defaultValue = "") String oAuthId,
                           HttpServletRequest request,Model model) {
        model.addAttribute("oAuthServices", oAuthServices.getAllOAuthServices());
        OAuthUser oAuthInfo = new OAuthUser();
        oAuthInfo.setoAuthId(oAuthId);
        oAuthInfo.setoAuthType(oAuthType);
        //保存用户
        user.setPassword(EncryptUtil.getPassword(user.getPassword(),user.getLoginName()));
        String userId=userService.save(user).toString();
        //建立第三方账号关联
        OAuthUser oAuthUser=oAuthUserService.findByOAuthTypeAndOAuthId(oAuthType,oAuthId);
        if(oAuthUser==null&&!oAuthType.equals("-1")){
            oAuthInfo.setUserId(userId);
            oAuthUserService.save(oAuthInfo);
        }
        //关联成功后登陆
        return loginByAuth(user);
    }




    public String loginByAuth(User user){
        UsernamePasswordToken token = new UsernamePasswordToken(user.getLoginName(), user.getPassword());
        token.setRememberMe(true);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
        //通过认证
        if (subject.isAuthenticated()) {
            return MAIN_PAGE;
        } else {
            return LOGIN_PAGE;
        }
    }


    /**
     * 校验当前登录名/邮箱的唯一性
     * @param loginName 登录名
     * @param userId 用户ID（用户已经存在，改回原来的名字还是唯一的）
     * @return
     */
    @RequestMapping(value = "/oauth/checkUnique", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Boolean> checkExist(String loginName, String userId) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        User user = userService.getUserByLoginName(loginName);
        //用户不存在，校验有效
        if (user == null) {
            map.put("valid", true);
        } else {
            if(!StrUtil.isEmpty(userId)&&userId.equals(user.getLoginName())){
                map.put("valid",true);
            }else {
                map.put("valid", false);
            }
        }
        return map;
    }


}

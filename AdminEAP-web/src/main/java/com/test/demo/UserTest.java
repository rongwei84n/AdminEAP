package com.test.demo;

import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.service.UserService;
import com.cnpc.framework.testng.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Resource;

public class UserTest extends BaseTest {

	@Resource
	private UserService userService;

	@Test(dataProvider = "dataProvider", groups = { "function-test" })
	public void checkUserExist(String code, String loginname) {
		System.out.println(code + "-----" + loginname);
		User user = userService.getUserByLoginName(loginname);
		Assert.assertNotNull(user);
	}
}


package com.how2java.tmall.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.UserService;

/**
 * 将dao层的逻辑（即使访问数据库）写入
 * @author Administrator
 *
 */
public class JPARealm extends AuthorizingRealm{
	@Autowired
	UserService userService;
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		
		SimpleAuthorizationInfo s = new SimpleAuthorizationInfo();
        return s;
	}

	@Override
	/**
	 * @param token 理解为提交shiro验证的token，里面有userName
	 */
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String userName=token.getPrincipal().toString();
		User user=userService.getByName(userName);
		//数据库的密文
		String passwordInDB=user.getPassword();
		String salt=user.getSalt();
		SimpleAuthenticationInfo authenticationInfo=new SimpleAuthenticationInfo(userName,
																				passwordInDB,
																				ByteSource.Util.bytes(salt),
																				getName());
		return authenticationInfo;
	}
	
}

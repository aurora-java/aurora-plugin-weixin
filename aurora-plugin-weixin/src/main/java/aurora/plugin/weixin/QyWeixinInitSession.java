package aurora.plugin.weixin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;


public class QyWeixinInitSession extends AbstractEntry {
	IObjectRegistry mRegistry;
	
	UncertainEngine mUncertainEngine;
	
	String groupName;

	String roleId;
	
	String lang;
	
	String deviceType;
	
	String companyId;
	
	

	public QyWeixinInitSession(IObjectRegistry registry,UncertainEngine uncertainEngine) {
		mRegistry = registry;
		mUncertainEngine = uncertainEngine;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {

		
		HttpServletRequest req = (HttpServletRequest) runner.getContext().get(
				"_instance." + HttpServletRequest.class.getName());
		HttpSession session = req.getSession(false);
		
		HashMap taskMap = (HashMap) mUncertainEngine.getGlobalContext().get("tokenMap");
		
		QiyeTokenTask task = (QiyeTokenTask) taskMap.get(groupName);
		if(task ==null){
			
			throw new RuntimeException("请检查 groupName 是否和配置文件task groupname对应");
		}
		
//		if(session !=null){
//		 session.invalidate();
//		}
		
		Boolean sessionExist = req.getSession(false) == null?false : true;
		int retry = 5;
		
		if (!sessionExist) {
			String code = req.getParameter("code");
			String groupName = req.getParameter("state");

			if (code != null && groupName != null) {
				
				//检查参数配置
				if(roleId == null || groupName ==null || lang ==null){
					throw new RuntimeException("roleId , groupname ,lang 不能为空");
				}
				
				// 来自微信请求，获取user_id
				String userId = null;
//				QiyeTokenTask task  = (QiyeTokenTask) tokenMap.get(groupName);
				

				while (retry > 0) {

					try {
						userId = QiyeWeixinNetworkUtil.getUserId(task.token, code);

						// 请求成功返回
						break;

					} catch (Exception e) {
						retry--;

						if (retry > 0) {
							throw e;

						} else {

							continue;
						}

					}

				}
				
				//已经获得user_id创建session
			  session	= req.getSession();
			  session.setAttribute("user_id", userId);
			  session.setAttribute("role_id", roleId);
			  session.setAttribute("lang",lang);
			  session.setAttribute("device_type", deviceType);

			} else {

				throw new Exception("没有session请登陆");
			}

		}


	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}

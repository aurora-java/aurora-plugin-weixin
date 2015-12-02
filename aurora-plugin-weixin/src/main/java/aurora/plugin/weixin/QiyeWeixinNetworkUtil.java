package aurora.plugin.weixin;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.org.apache.regexp.internal.recompile;
import com.sun.tools.doclets.formats.html.resources.standard;

import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import aurora.plugin.weixin.util.Assert;

public class QiyeWeixinNetworkUtil {
	
	public static final String qiyePostMessageUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
	
	public static final String qiyeCreateUserUrl  = "https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token=";
	
	public static final String qiyeUpdateUserUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/update?access_token=";
	
	public static final String qiyeDeleteUserUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token=%s&userid=%s";
	
	public static final String qiyeBatchDeleteUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/batchdelete?access_token=";
	
	public static final String qiyeGetUserUrl   = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=%s&userid=%s";
		
	public static final String qiyeInviteUserUrl = "https://qyapi.weixin.qq.com/cgi-bin/invite/send?access_token=";
	
	static {

		System.setProperty("sun.net.client.defaultConnectTimeout", "30000");// 连接超时30秒
		System.setProperty("sun.net.client.defaultReadTimeout", "30000"); // 读取超时30秒
	}

	public static String getUserId(String token, String code)
			throws IOException, JSONException, RuntimeException {
		
		
		

		String url = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token="
				+ token + "&code=" + code;

		String respStr = UrlHelper.doget(url);


		JSONObject respJson = new JSONObject(respStr);


		return respJson.getString("UserId");

	}
	
	public static String getJsTicket(String token  ) throws IOException, JSONException{
		
		String url = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token=" + token;
		
		String respStr = UrlHelper.doget(url);


		JSONObject respJson = new JSONObject(respStr);
			    
	    
	    return  respJson.getString("ticket");
		
	}

/**
 * 	
 * @param corpId
 * @param secrect
 * @return
 * @throws IOException 
 * @throws JSONException 
 */

	public static String getAccessToken(String corpId, String secrect) throws IOException, JSONException {


		String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
				+ corpId + "&corpsecret=" + secrect;
		String accessToken = null;

		String respStr	= UrlHelper.doget(url);

		JSONObject respJson = new JSONObject(respStr);
		accessToken = respJson.getString("access_token");
			
		return accessToken;
	}
/**
 * 
 * @param menuConfig
 * @param corpId
 * @param secrect
 * @param agentId
 * @throws IOException
 * @throws JSONException
 */
	

	public static String createMenu(String menuConfig, String corpId,
			String secrect, String agentId) throws IOException, JSONException {

		String accessToken = getAccessToken(corpId, secrect);

		String action = "https://qyapi.weixin.qq.com/cgi-bin/menu/create?access_token="
				+ accessToken + "&agentid=" + agentId;

		String respString = UrlHelper.doPost(action,menuConfig);

		return respString;
		
	}

	public static String deleteMenu(String corpId, String secrect,
			String agentId) throws IOException, JSONException {
		String accessToken = getAccessToken(corpId, secrect);

		String action = "https://qyapi.weixin.qq.com/cgi-bin/menu/delete?access_token="
				+ accessToken + "&agentid=" + agentId;

		String respString = UrlHelper.doget(action);

		return respString;

	}
	
	public static String getTokenByTaskName(String taskName,IObjectRegistry objectRegistry) {
		
		UncertainEngine uncertainEngine = (UncertainEngine) objectRegistry.getInstanceOfType(UncertainEngine.class);
		
		
		HashMap<String,QiyeTokenTask> tokenTaskMap  = (HashMap) uncertainEngine.getGlobalContext().get("tokenMap");
		
		QiyeTokenTask task  = tokenTaskMap.get(taskName);
		
		return task.token;
	}

/***
 * 
 * @return
 * @throws IOException 
 */
	public static String sendNewsMessage(String taskName,String param,IObjectRegistry objectRegistry) throws RuntimeException, IOException
	{
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(param);
		Assert.notNull(objectRegistry);

//		UncertainEngine uncertainEngine = (UncertainEngine) objectRegistry.getInstanceOfType(UncertainEngine.class);
//		
//		
//		HashMap<String,QiyeTokenTask> tokenTaskMap  = (HashMap) uncertainEngine.getGlobalContext().get("tokenMap");
//		
//		QiyeTokenTask task  = tokenTaskMap.get(taskName);
		
		
		
		respStr = UrlHelper.doPost(qiyePostMessageUrl + getTokenByTaskName(taskName, objectRegistry), param);
						
		return respStr;
	}
	
	public static String  createUser(String taskName,String param,IObjectRegistry objectRegistry) throws IOException {
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(param);
		Assert.notNull(objectRegistry);;

			
		
		respStr = UrlHelper.doPost(qiyeCreateUserUrl + getTokenByTaskName(taskName, objectRegistry), param);
						
		return respStr;
		
	
	}
	
	public static String updateUser(String taskName,String param,IObjectRegistry objectRegistry) throws IOException{
		
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(param);
		Assert.notNull(objectRegistry);

		respStr = UrlHelper.doPost(qiyeUpdateUserUrl + getTokenByTaskName(taskName, objectRegistry), param);
						
		return respStr;	
		
	}
	
	
	public static String deleteUser(String taskName,String userId,IObjectRegistry objectRegistry) throws IOException
	{
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(userId);
		Assert.notNull(objectRegistry);
		
		String getUrl = String.format(qiyeDeleteUserUrl, getTokenByTaskName(taskName, objectRegistry),userId);
		
		respStr = UrlHelper.doget(getUrl);
		
		return respStr;
	}
	
	public static String batchDelete(String  taskName,String param,IObjectRegistry objectRegistry) throws IOException 
	{
		String respSt = null;
		
		Assert.notNull(taskName);
		Assert.notNull(param);
		Assert.notNull(objectRegistry);
		
		respSt = UrlHelper.doPost(qiyeBatchDeleteUrl+ getTokenByTaskName(taskName, objectRegistry), param);
		
		return respSt;
		
	}
	
	public static String getUser(String  taskName,String userId,IObjectRegistry objectRegistry) throws IOException {
		
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(userId);
		Assert.notNull(objectRegistry);
		
		String getUrl = String.format(qiyeGetUserUrl, getTokenByTaskName(taskName, objectRegistry),userId);
		
		respStr = UrlHelper.doget(getUrl);
		
		return respStr;
					
	}
	
	public static String inviteUser(String  taskName,String param,IObjectRegistry objectRegistry) throws IOException{
		
		String respStr= null;
		
		Assert.notNull(taskName);
		Assert.notNull(param);
		Assert.notNull(objectRegistry);
		
		respStr = UrlHelper.doPost(qiyeInviteUserUrl, getTokenByTaskName(taskName, objectRegistry));

		
		return respStr;
		
		
	}
	
	
	
}

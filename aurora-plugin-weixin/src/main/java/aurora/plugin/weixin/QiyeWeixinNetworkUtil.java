package aurora.plugin.weixin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import aurora.plugin.weixin.util.Assert;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;

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
	
	public static String getAccessToken(String corpId, String secrect) throws IOException, JSONException {


		String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
				+ corpId + "&corpsecret=" + secrect;
		String accessToken = null;

		String respStr	= UrlHelper.doget(url);

		JSONObject respJson = new JSONObject(respStr);
		accessToken = respJson.getString("access_token");

		return accessToken;
	}

	/**2.获取accessToken
	 * 
	 * @param taskName
	 * @param objectRegistry
	 * @return
	 */
	 
	public static String getTokenByTaskName(String taskName,IObjectRegistry objectRegistry) {

		UncertainEngine uncertainEngine = (UncertainEngine) objectRegistry.getInstanceOfType(UncertainEngine.class);

		HashMap<String,QiyeTokenTask> tokenTaskMap  = (HashMap) uncertainEngine.getGlobalContext().get("tokenMap");

		QiyeTokenTask task  = tokenTaskMap.get(taskName);

		return task.getToken();
	}

   /**3.获取jsapiTickt
    * 
    * @param token
    * @return
    * @throws IOException
    * @throws JSONException
    */
	public static String getJsTicket(String token  ) throws IOException, JSONException{

		String url = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token=" + token;

		String respStr = UrlHelper.doget(url);


		JSONObject respJson = new JSONObject(respStr);


		return  respJson.getString("ticket");

	}

	/**
	 * 4.获取微信的JSSDK配置信息
	 * @param request
	 * @return
	 * @throws JSONException 
	 * @throws IOException 
	 */
	public static Map<String, Object> getWxConfig(String taskName,IObjectRegistry objectRegistry,HttpServletRequest request) throws IOException, JSONException {
		Map<String, Object> ret = new HashMap<String, Object>();
		//1.准备好参与签名的字段

		String nonceStr = UUID.randomUUID().toString(); // 必填，生成签名的随机串
		
		//1.2  JsapiTicket
		UncertainEngine uncertainEngine = (UncertainEngine) objectRegistry.getInstanceOfType(UncertainEngine.class);
		HashMap<String,QiyeTokenTask> tokenTaskMap  = (HashMap) uncertainEngine.getGlobalContext().get("tokenMap");
		QiyeTokenTask task  = tokenTaskMap.get(taskName);
		
		String accessToken=task.getToken();
		String jsapi_ticket =getJsTicket(accessToken );// 必填，生成签名的H5应用调用企业微信JS接口的临时票据
		
		//1.3获取时间戳
		String timestamp = Long.toString(System.currentTimeMillis() / 1000); // 必填，生成签名的时间戳
		
		String url=request.getRequestURL().toString();


		//2.字典序           ，注意这里参数名必须全部小写，且必须有序
		String sign = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + nonceStr+ "&timestamp=" + timestamp + "&url=" + url;

		//3.sha1签名
		String signature = "";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(sign.getBytes("UTF-8"));
			signature = byteToHex(crypt.digest());
			//System.out.println("signature:"+signature);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ret.put("appId", task.getCorpId());
		ret.put("timestamp", timestamp);
		ret.put("nonceStr", nonceStr);
		ret.put("signature", signature);
		return ret;
	}

	/**
	 * 方法名：byteToHex</br>
	 * 详述：字符串加密辅助方法 </br>
	 * 开发人员：souvc  </br>
	 * 创建时间：2016-1-5  </br>
	 * @param hash
	 * @return 说明返回值含义
	 * @throws 说明发生此异常的条件
	 */
	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static String getUserId(String token, String code)
			throws IOException, JSONException, RuntimeException {
		
		
		

		String url = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?access_token="
				+ token + "&code=" + code;

		String respStr = UrlHelper.doget(url);


		JSONObject respJson = new JSONObject(respStr);


		return respJson.getString("UserId");

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
		
		respStr = UrlHelper.doPost(qiyeInviteUserUrl+ getTokenByTaskName(taskName, objectRegistry), param);

		
		return respStr;
		
		
	}
	
	
	
}

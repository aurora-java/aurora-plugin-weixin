package aurora.plugin.weixin;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class QiyeWeixinNetworkUtil {

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

		if (respJson.has("errcode")) {
			throw new RuntimeException("出错返回" + respJson.getString("errcode")
					+ respJson.getString("errmsg"));
		} else if (!respJson.has("UserId")) {
			throw new RuntimeException("请求者非企业员工，无法获取user_id");
		}

		return respJson.getString("UserId");

	}
	
	public static String getJsTicket(String token  ) throws IOException, JSONException{
		
		String url = "https://qyapi.weixin.qq.com/cgi-bin/get_jsapi_ticket?access_token=" + token;
		
		String respStr = UrlHelper.doget(url);


		JSONObject respJson = new JSONObject(respStr);
		
	    String errmsg=	respJson.getString("errmsg");
	    
	    //success return 
	    if(!errmsg.equals("ok")){
	    	
	    	throw new RuntimeException("getJsTicket error return" + respJson.toString());

	    	
	    }
	    
	    return  respJson.getString("ticket");
		
	}

/**
 * 	
 * @param corpId
 * @param secrect
 * @return
 */

	public static String getAccessToken(String corpId, String secrect) {


		String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
				+ corpId + "&corpsecret=" + secrect;
		String accessToken = null;
		try {

		String respStr	= UrlHelper.doget(url);

			JSONObject respJson = new JSONObject(respStr);
			accessToken = respJson.getString("access_token");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken;
	}

	

	public static void createMenu(String menuConfig, String corpId,
			String secrect, String agentId) throws IOException {

		String accessToken = getAccessToken(corpId, secrect);

		String action = "https://qyapi.weixin.qq.com/cgi-bin/menu/create?access_token="
				+ accessToken + "&agentid=" + agentId;

		String respString = UrlHelper.doPost(action,menuConfig);

		System.out.println("resp is " + respString);

	}

	public static String deleteMenu(String corpId, String secrect,
			String agentId) throws IOException {
		String accessToken = getAccessToken(corpId, secrect);

		String action = "https://qyapi.weixin.qq.com/cgi-bin/menu/delete?access_token="
				+ accessToken + "&agentid=" + agentId;

		String respString = UrlHelper.doget(action);
		System.out.println(" resp is " + respString);

		return respString;

	}

}

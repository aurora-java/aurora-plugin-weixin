package aurora.plugin.weixin;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;



import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import aurora.service.http.HttpServiceFactory;
import aurora.service.http.HttpServiceInstance;

import uncertain.proc.IProcedureManager;

public abstract class WeixinService  extends HttpServiceInstance{
	
	public static final String TEXT_TYPE = "text";
	
	public static final String IMG_TYPE = "image";
	
	public static final String EVENT_TYPE ="event";
	
	public  static final String BASIC_PATH = "modules/weixin/";
	
	public static final String TEXT_PROC_NAME = "textEcho.svc";

	public static final String IMG_PROC_NAME  =  "imgEcho.svc";
	
	
	public static final String OTHER_PROC_NAME = "otherEcho.svc";
	
	public static final String EVENT_PROC_NAME = "eventEcho.svc";
	
	HashMap<String, WXBizMsgCrypt> mWxBizMap;
	
	WXBizMsgCrypt mWeibizMsgCrypt;
	
	String mAppName;
	
	String msgSignature ;
	String timestamp ;
	String nonce;
	String echoStr;
	
	//是否为echo请求
	boolean isEchoStr =false;
	//ehco请求解析后的数据
	String encodeEchoStr;
	
	
	public WeixinService(String name, IProcedureManager proc_manager,HashMap<String, WXBizMsgCrypt> wxBizMap) {
		super(name, proc_manager);
		mWxBizMap = wxBizMap;
		
	}
	

	
	public void setAppName(String appName) throws ServletException{
		
		mAppName = appName;
    	mWeibizMsgCrypt  = mWxBizMap.get(appName);

    	if(mWeibizMsgCrypt == null) {
    		
    		throw new ServletException("无效的app名，请检查配置");
    		
    		
    	}
	}

    
/**
 * 将解密后的echostr返回给微信服务器	
 */
    public void doEchoStr() {
    	doResp(encodeEchoStr);
	}
    public void doResp(String resMsg){
		PrintWriter out = null;
		try {
			
			out = getResponse().getWriter();
			
			
			
			out.println(resMsg);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
    
        
    
	public void populate() throws Exception
	{
		msgSignature = (String) getServiceContext().getParameter().get("msg_signature");
		timestamp = (String) getServiceContext().getParameter().get("timestamp");
		nonce = (String) getServiceContext().getParameter().get("nonce");

		
		if(getRequest().getMethod() == "GET" && getServiceContext().getParameter().containsKey("echostr")){
			
			isEchoStr = true;
			echoStr = (String) getServiceContext().getParameter().get("echostr");
			
			
			encodeEchoStr = mWeibizMsgCrypt.VerifyURL(msgSignature, timestamp, nonce, echoStr);
			
		//微信发来的非echo请求都为post
		}else if(getRequest().getMethod() == "POST"){

			 /** 读取接收到的xml消息 */  
			String xmldataString = WeixinPluginUtl.receiveDataToString(getRequest());
			
			
			String deXmlStr	=  mWeibizMsgCrypt.DecryptMsg(msgSignature, timestamp, nonce, xmldataString);
			
			WeixinPluginUtl.packXmlDataToParameter(deXmlStr, getServiceContext().getParameter());
			
		}
		
		
	}
	
	
	public abstract boolean handlerRequest(HttpServiceFactory serviceFactory) throws Exception;
		
   
}

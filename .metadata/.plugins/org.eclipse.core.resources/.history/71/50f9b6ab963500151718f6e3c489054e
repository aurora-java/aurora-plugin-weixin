package aurora.plugin.weixin;

import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import aurora.application.features.HttpRequestTransfer;
import uncertain.core.UncertainEngine;
import uncertain.proc.IProcedureManager;

public class WeixinServiceFactory {
	protected  UncertainEngine mUncertainEngine;
	
	protected  IProcedureManager  mProcedureManager;
	
	//企业微信
	public static final String QIYE_WEIXIN = "qiye";
	
	//公众微信
	public static final String GZ_WEIXIN = "gz";
	
	public WeixinServiceFactory(UncertainEngine uncertainEngine)
	{
		mUncertainEngine = uncertainEngine;
		mProcedureManager  = uncertainEngine.getProcedureManager();
		
		
	}
	
	public WeixinService createWeixinService(HttpServletRequest req, HttpServletResponse resp, HashMap<String, WXBizMsgCrypt> wxBizMap,String method[]) throws Exception
	{
		
		if(method[0].equals(QIYE_WEIXIN)){
			
			return createQiyeWeixinService(req, resp, wxBizMap,method[1]);
			
		}else if(method[0].equals(GZ_WEIXIN)) {
			
			return null;

		}else {
			 
			
			throw new ServletException("无效的微信类型，请检查url配置是否正确");
			
		}
		
	}
	
	
	public QiyeWeixinService createQiyeWeixinService(HttpServletRequest req, HttpServletResponse resp,HashMap<String, WXBizMsgCrypt> wxBizMap,String appName) throws Exception
	{
		QiyeWeixinService weixinService =   new QiyeWeixinService(req.getRequestURI(),mProcedureManager,wxBizMap);
		weixinService.setRequest(req);
		weixinService.setResponse(resp);
		weixinService.setAppName(appName);

		HttpRequestTransfer.copyRequest(weixinService);
		weixinService.populate();

		return weixinService;
		
		
	}
	
}

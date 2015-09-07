package aurora.plugin.weixin;

import java.io.IOException;
import java.util.HashMap;

import org.xml.sax.SAXException;

import aurora.service.http.HttpServiceFactory;

import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import sun.security.rsa.RSASignature.SHA1withRSA;
import uncertain.composite.CompositeMap;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;

public class QiyeWeixinService extends WeixinService {

	public QiyeWeixinService(String name, IProcedureManager proc_manager,
			HashMap<String, WXBizMsgCrypt> wxBizMap) {
		super(name, proc_manager, wxBizMap);
	}

	private Procedure getProc(String procPath, HttpServiceFactory serviceFactory)
			throws IOException, SAXException {
		CompositeMap map = serviceFactory.loadServiceConfig(procPath);
		Procedure proc = mProcManager.createProcedure(map);

		return proc;
	}

	public void doResp(String msgType, String returnStr)
			throws AesException, IOException {

		String sRespData = null;
		if (msgType.equals(TEXT_TYPE)) {

			 sRespData = WeixinPluginUtl.formatTextMsg(
					getServiceContext().getParameter(), returnStr);



		} else if (msgType.equals(IMG_TYPE)) {
			
			 sRespData = WeixinPluginUtl.formatTextMsg(
					getServiceContext().getParameter(), returnStr);

					

		} else {
			
			 sRespData = WeixinPluginUtl.formatTextMsg(
						getServiceContext().getParameter(), returnStr);
			

		}
		
		String sEncryptMsg = mWeibizMsgCrypt.EncryptMsg(sRespData,
				timestamp,
				nonce);
		
		doResp(sEncryptMsg);



	}

	public boolean handlerRequest(HttpServiceFactory serviceFactory)
			throws Exception {
		String msgType = getServiceContext().getParameter()
				.getString("MsgType");
		String procPath = BASIC_PATH + "qiye/" + mAppName + "/";

		
		Boolean isSuccess = true;

		if (msgType.equals(TEXT_TYPE)) {

			isSuccess = invoke(getProc(procPath + TEXT_PROC_NAME,
					serviceFactory));

		} else if (msgType.equals(IMG_TYPE)) {

			isSuccess = invoke(getProc(procPath + IMG_PROC_NAME, serviceFactory));

		} else {

			isSuccess = invoke(getProc(procPath + OTHER_PROC_NAME,
					serviceFactory));

		}

		if (isSuccess) {
			String returnStr = getServiceContext().getParameter().getString("json");

			if (returnStr == null) {
				
				return false;
			}

			doResp(msgType, returnStr);
		}

		return isSuccess;
	}

}

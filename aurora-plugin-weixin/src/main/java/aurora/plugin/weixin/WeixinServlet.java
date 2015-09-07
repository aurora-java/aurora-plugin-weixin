package aurora.plugin.weixin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.json.JSONException;

import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

import aurora.service.IService;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.ServiceThreadLocal;
import aurora.service.http.HttpServiceFactory;
import aurora.service.http.WebContextInit;
import aurora.transaction.ITransactionService;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.ProcedureManager;
import uncertain.proc.Sleep;

public class WeixinServlet extends HttpServlet {

	protected UncertainEngine mUncertainEngine;
	protected ProcedureManager mProcManager;
	protected ServletConfig mConfig;
	protected ServletContext mContext;
	private IObjectRegistry mRegistry;
	HttpServiceFactory mServiceFactory;

	protected WeixinServiceFactory mWeixinServiceFactory;

	protected HashMap<String, WXBizMsgCrypt> wxBizMap;

	protected HashMap<String, QiyeTokenTask> tokenMap;

	protected QiyeWeixinConfig weixinConfig;

	private static final long serialVersionUID = 1753926278834479860L;

	protected WeixinService createServiceInstance(HttpServletRequest request,
			HttpServletResponse response, String method[]) throws Exception {

		final WeixinService svc = mWeixinServiceFactory.createWeixinService(
				request, response, wxBizMap, method);
		return svc;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		doService(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doService(req, resp);

	}

	public String[] populateMethod(String requestUri) throws ServletException {

		int posi = requestUri.indexOf("weixin") + 7;
		String methodStr = requestUri.substring(posi);

		String method[] = methodStr.split("/");
		if (method.length != 2) {

			throw new ServletException(" error url url后面只能携带两个参数");
		}

		return method;

	}

	protected void doService(HttpServletRequest request,
			HttpServletResponse resp) throws ServletException, IOException {

		boolean is_success = true;

		UserTransaction trans = null;
		IObjectRegistry or = mUncertainEngine.getObjectRegistry();
		ITransactionService ts = (ITransactionService) or
				.getInstanceOfType(ITransactionService.class);

		ServiceContext ctx = null;
		WeixinService svc = null;
		String method[];

		if (ts == null)
			throw new ServletException("ITransactionService instance not found");

		trans = ts.getUserTransaction();

		try {
			trans.begin();
			method = populateMethod(request.getRequestURI());

			svc = createServiceInstance(request, resp, method);
			ctx = svc.getServiceContext();

			if (svc.isEchoStr) {

				svc.doEchoStr();

			} else {

				is_success = svc.handlerRequest(mServiceFactory);
				if (ctx.hasError()) {

					is_success = false;
				}

			}

		} catch (Throwable e) {
			is_success = false;
			e.printStackTrace();
			mUncertainEngine.logException("Error when executing service "
					+ request.getRequestURI(), e);
		} finally {

			if (is_success) {
				try {
					trans.commit();
				} catch (Throwable e) {
					mUncertainEngine.logException("Error when commit service "
							+ request.getRequestURI(), e);
				}
			} else {
				try {
					trans.rollback();
				} catch (Throwable e) {
					mUncertainEngine.logException(
							"Error when rollback service "
									+ request.getRequestURI(), e);
				}
			}

			if (svc != null) {
				// release resource
				svc.release();
				cleanUp(svc);
			}

			ServiceThreadLocal.remove();

			ts.stop();
		}

	}

	protected void cleanUp(IService svc) {
		((ServiceInstance) svc).clear();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);

		mConfig = config;
		mContext = config.getServletContext();
		
		mUncertainEngine = WebContextInit.getUncertainEngine(mContext);
		if (mUncertainEngine == null) {
			throw new ServletException("Uncertain engine not initialized");
		}

		mProcManager = (ProcedureManager) mUncertainEngine
				.getProcedureManager();
		mRegistry = mUncertainEngine.getObjectRegistry();

		mServiceFactory = (HttpServiceFactory) mUncertainEngine
				.getObjectRegistry()
				.getInstanceOfType(HttpServiceFactory.class);

		mWeixinServiceFactory = new WeixinServiceFactory(mUncertainEngine);

		weixinConfig = (QiyeWeixinConfig) mRegistry
				.getInstanceOfType(QiyeWeixinConfig.class);

		wxBizMap = new HashMap<String, WXBizMsgCrypt>();

		tokenMap = weixinConfig.qiyeTokenTaskMap;
		mUncertainEngine.getGlobalContext().put("tokenMap", tokenMap);
		

		HashMap<String, QiyeWeixinInstance> weixinInstanceMap = weixinConfig.weixinInstanceMap;

		if (weixinInstanceMap == null) {

			throw new ServletException("weinxinconfig 配置不能为空");

		}

		for (String key : weixinInstanceMap.keySet()) {

			QiyeWeixinInstance instance = weixinInstanceMap.get(key);

			try {
				WXBizMsgCrypt crpt = new WXBizMsgCrypt(instance.token,
						instance.encodingAESKey, instance.corpId);
				wxBizMap.put(key, crpt);

			} catch (AesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new ServletException(
						"初始化微信失败，请检查，配置的token, encodingAESKey,corpId");
			}

		}

		// ///初始化task,每段时间访问获取token
		for (String keyString : tokenMap.keySet()) {

			final QiyeTokenTask task = tokenMap.get(keyString);

			Timer current = new Timer();
			current.schedule(new TimerTask() {
				QiyeTokenTask mTask = task;

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String token = QiyeWeixinNetworkUtil.getAccessToken(
							mTask.getCorpId(), mTask.getSecrect());

					task.setToken(token);
				}
			}, 0, 1000 * 7000);
			
			current.schedule(new TimerTask() {
				
				@Override
				public void run() {
					int retry =5;
					while(retry>0){
						if(task.getToken() == null){
							
								throw new RuntimeException("token 获取失败");
						}else {
							String jsapiTicket;
							try {
								jsapiTicket = QiyeWeixinNetworkUtil.getJsTicket(task.getToken());
								task.setJsapiTicket(jsapiTicket);

							} catch (IOException e) {
									throw new RuntimeException();
							} catch (JSONException e) {
									throw new RuntimeException();
							}
							
						}
						
						
					}
					
					
				}
			}, 3000,1000 * 7200);

		}

	}

}

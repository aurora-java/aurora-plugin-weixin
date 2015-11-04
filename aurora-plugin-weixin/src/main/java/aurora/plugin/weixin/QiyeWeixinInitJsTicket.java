package aurora.plugin.weixin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

public class QiyeWeixinInitJsTicket extends AbstractEntry{

	IObjectRegistry mRegistry;
	
	UncertainEngine mUncertainEngine;
	
	String groupName;
	


	String url;

	
	
	public QiyeWeixinInitJsTicket(IObjectRegistry registry,UncertainEngine uncertainEngine) {
		mRegistry = registry;
		mUncertainEngine = uncertainEngine;
	}
	
	 
	@Override
	public void run(ProcedureRunner runner) throws Exception {

		HashMap taskMap = (HashMap) mUncertainEngine.getGlobalContext().get("tokenMap");
		CompositeMap context = runner.getContext();			
		QiyeTokenTask task = (QiyeTokenTask) taskMap.get(groupName);
		HttpServletRequest req = (HttpServletRequest) runner.getContext().get(
				"_instance." + HttpServletRequest.class.getName());
		
		
		if(task ==null){
			
			throw new RuntimeException("请检查 groupName 是否和配置文件task groupname对应");
		}
		
		
		if(url == null){
			url = req.getRequestURL().toString() + "?"+req.getQueryString();

		}
		
		CompositeMap jsTicketMap = WeixinPluginUtl.getSignature(task.getJsapiTicket(), url);
		
		context.getChild("parameter").putAll(jsTicketMap);
		
		
	}
	
	
	public String getGroupName() {
		return groupName;
	}


	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
	
	

}

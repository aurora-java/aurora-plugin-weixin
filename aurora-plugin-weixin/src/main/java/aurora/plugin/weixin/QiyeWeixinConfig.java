package aurora.plugin.weixin;

import java.util.HashMap;

import uncertain.ocm.IObjectRegistry;
import aurora.plugin.weixin.QiyeWeixinInstance;

public class QiyeWeixinConfig {
	
	HashMap<String,QiyeWeixinInstance> weixinInstanceMap ;
	
	HashMap<String,QiyeTokenTask> qiyeTokenTaskMap ;

	IObjectRegistry mObjectRegistry;
	
	public QiyeWeixinConfig(IObjectRegistry registry) {
		
		weixinInstanceMap = new HashMap<String, QiyeWeixinInstance>();
		
		qiyeTokenTaskMap = new HashMap<String, QiyeTokenTask>();
		
		mObjectRegistry = registry;

	}
	
	public void onInitialize() {
		mObjectRegistry.registerInstance(QiyeWeixinConfig.class, this);
	
	}
	
	
	
	public void addInstances(QiyeWeixinInstance[] weixinInstances) {
		
		for(QiyeWeixinInstance instance : weixinInstances){
			
			weixinInstanceMap.put(instance.getAppName(), instance);
			
		}

	}

	
	public void addTokenTasks(QiyeTokenTask[] tokenTasks ) {
		for(QiyeTokenTask task : tokenTasks){
			
			qiyeTokenTaskMap.put(task.getGroupName(), task);
			
		}
		
		
	}

	public HashMap<String, QiyeWeixinInstance> getWeixinInstanceMap() {
		return weixinInstanceMap;
	}


	public HashMap<String, QiyeTokenTask> getQiyeTokenTaskMap() {
		return qiyeTokenTaskMap;
	}


	
	
	
}

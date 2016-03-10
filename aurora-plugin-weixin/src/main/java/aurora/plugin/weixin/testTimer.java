package aurora.plugin.weixin;

import java.util.Timer;
import java.util.TimerTask;

import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;



public class testTimer {

	  public static void main(String args[]) { 
		 Timer a = 	new Timer();
		 a.schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println("hello");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.run();
				System.out.println("end");
			}
			
//			public void run() {
//				// TODO Auto-generated method stub
//				try {
//					System.out.println("hello every one");
//					RuntimeException a = new RuntimeException();
//					throw a;
//				} catch (RuntimeException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					while (true) {
//						
//					}
//				}
//			}
		}, 0, 1);
	  
	  } 
	
}

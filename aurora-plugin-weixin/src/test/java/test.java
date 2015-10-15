import javax.servlet.ServletException;

import aurora.plugin.weixin.WeixinServlet;
import junit.framework.TestCase;


public class test extends TestCase {

	
    public void testAdd() {
    	
      WeixinServlet servlet =	new WeixinServlet();
    	
      try {
		servlet.populateMethod("http://srmtest.haidilao.com/hdltrain/weixin/app1");
	} catch (ServletException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
}


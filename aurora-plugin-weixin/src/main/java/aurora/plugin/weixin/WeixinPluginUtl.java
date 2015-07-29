package aurora.plugin.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import aurora.security.crypto.codec.Hex;
import uncertain.composite.CompositeMap;

public class WeixinPluginUtl {

	static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	static DocumentBuilder db;

	static {

		try {
			db = dbf.newDocumentBuilder();

		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		}
	}

	public static String getRandomString(int length) { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }  
	
	/**
	 * 读取servlet中请求数据 转换成string返回
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String receiveDataToString(HttpServletRequest request)
			throws IOException {

		/** 读取接收到的xml消息 */
		StringBuffer sb = new StringBuffer();
		InputStream is = request.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String s = "";
		while ((s = br.readLine()) != null) {
			sb.append(s);
		}
		String xml = sb.toString(); // 次即为接收到微信端发送过来的xml数据
		return xml;

	}

	public static void packXmlDataToParameter(String xmlData,
			CompositeMap parameter) throws IOException,
			ParserConfigurationException, SAXException {

		StringReader sr = new StringReader(xmlData);
		InputSource is = new InputSource(sr);
		Document document = db.parse(is);

		Element root = document.getDocumentElement();
		NodeList nodelist = root.getChildNodes();

		for (int i = 0; i < nodelist.getLength(); i++) {

			Node node = nodelist.item(i);
			if (node.getNodeType() == 1) {

				parameter.put(node.getNodeName(), node.getTextContent());

			}

		}


	}

	// public static String formatReponseMsg(CompositeMap map ,String
	// encryptMsg) {
	//
	// StringBuffer sb = new StringBuffer();
	// Date date = new Date();
	//
	// sb.append("<xml><Encrypt><![CDATA[");
	// sb.append(encryptMsg);
	// sb.append("]]></Encrypt><MsgSignature><![CDATA[");
	// sb.append(map.getString("msg_signature"));
	// sb.append("]]></MsgSignature>");
	// sb.append("<TimeStamp>");
	// sb.append(date.getTime());
	// sb.append("</TimeStamp>");
	// sb.append("<Nonce><![CDATA[");
	// sb.append(map.get("nonce"));
	// sb.append("]]></Nonce></xml>");
	//
	// System.out.println("msg is " + sb.toString());
	//
	// return sb.toString();
	// }

	public static String formatTextMsg(CompositeMap map, String content) {
		StringBuffer sb = new StringBuffer();
		Date date = new Date();

		sb.append("<xml><ToUserName><![CDATA[");
		sb.append(map.getString("FromUserName"));
		sb.append("]]></ToUserName><FromUserName><![CDATA[");
		sb.append(map.getString("ToUserName"));
		sb.append("]]></FromUserName><CreateTime>");
		sb.append(date.getTime());
		sb.append("</CreateTime><MsgType><![CDATA[text]]></MsgType>");
		sb.append("<Content><![CDATA[");
		sb.append(content);
		sb.append("]]></Content></xml>");

		return sb.toString();

	}
	
	
	protected static String encryptSHA1(String data){
		try{
			return String.valueOf(Hex.encode(MessageDigest.getInstance("SHA-1").digest(data.getBytes())));
		}catch (NoSuchAlgorithmException e){
			throw new RuntimeException(e);
		}
	}
	
/**
 * 通过jsticket 获取签名	
 * @param jsTicket
 * @return
 */
	public CompositeMap  getSignature(String jsTicket,String url){
		
		CompositeMap ticketEncryptMap = new CompositeMap();
		String currentTimeStamp = String.format("%l", System.currentTimeMillis());
		String noncestr = getRandomString(16);//随机字符串
		
		StringBuffer dataBuffer = new StringBuffer();
		
		dataBuffer.append("jsapi_ticket=");
		dataBuffer.append(jsTicket);
		dataBuffer.append("&noncestr=");
		dataBuffer.append(noncestr);
		dataBuffer.append("&timestamp=");
		dataBuffer.append(currentTimeStamp);
		dataBuffer.append("&url=");
		dataBuffer.append(url);
		
		String signature	= encryptSHA1(dataBuffer.toString());
		
		ticketEncryptMap.putString("timestamp", currentTimeStamp);
		ticketEncryptMap.putString("noncestr", noncestr);
		ticketEncryptMap.putString("signature", signature);
		
		return ticketEncryptMap;
		
	}
	
}

package aurora.plugin.weixin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class UrlHelper {

	public static String doget(String url) throws IOException {

		return doService(url, "GET", null);
	}

	public static String doPost(String url, String param) throws IOException {

		return doService(url, "POST", param);

	}

	public static String doService(String url, String method, String param)
			throws IOException {

		URL service;

		service = new URL(url);

		HttpURLConnection http;
		http = (HttpURLConnection) service.openConnection();

		http.setRequestMethod(method); // 必须是get方式请求
		http.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		http.setDoOutput(true);
		http.setDoInput(true);
		http.connect();

		if (param != null) {

			OutputStream os = http.getOutputStream();
			os.write(param.getBytes("UTF-8"));// 传入参数
			os.flush();
			os.close();
		}

		InputStream is = http.getInputStream();
		int size = is.available();
		byte[] resp = new byte[size];
		is.read(resp);

		String respStr = new String(resp, "UTF-8");

		return respStr;

	}
	
	

	/**
	 * 2.发送https请求之获取临时素材 
	 *  
	 * @param requestUrl 请求地址 
	 * @param requestMethod 请求方式（GET、POST） 
	 * @param outputStr 提交的数据 
	 * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值) 
	 * @throws NoSuchProviderException 
	 * @throws Exception 
	 */  
	public static File getFile(String requestUrl,String savePath) throws Exception {  
        //String path=System.getProperty("user.dir")+"/img//1.png";
	
		
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化  
			TrustManager[] tm = { new MyX509TrustManager() };  
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
			sslContext.init(null, tm, new java.security.SecureRandom());  
			// 从上述SSLContext对象中得到SSLSocketFactory对象  
			SSLSocketFactory ssf = sslContext.getSocketFactory();  

			URL url = new URL(requestUrl);  
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
			httpUrlConn.setSSLSocketFactory(ssf);  

			httpUrlConn.setDoOutput(true);  
			httpUrlConn.setDoInput(true);  
			httpUrlConn.setUseCaches(false);  
			// 设置请求方式（GET/POST）  
			httpUrlConn.setRequestMethod("GET");  

			httpUrlConn.connect();  

			//获取文件扩展名
			String ext=getExt(httpUrlConn.getContentType());
			savePath=savePath+ext;
			//下载文件到f文件
			File f = new File(savePath);

			
			// 获取微信返回的输入流
			InputStream in = httpUrlConn.getInputStream(); 
			
			//输出流，将微信返回的输入流内容写到文件中
			FileOutputStream out = new FileOutputStream(f);
			 
			int length=100*1024;
			byte[] byteBuffer = new byte[length]; //存储文件内容
			
			int byteread =0;
	        int bytesum=0;
			
			while (( byteread=in.read(byteBuffer)) != -1) {  
				bytesum += byteread; //字节数 文件大小 
				out.write(byteBuffer,0,byteread);  
				
			}  
			System.out.println("bytesum: "+bytesum);
			
	        in.close();  
			// 释放资源  
			out.close();  
			in = null;  
			out=null;
			
			httpUrlConn.disconnect();  

			
			return f;
	}  


	/**
	 * @desc ：3.微信上传临时素材的请求方法
	 *  
	 * @param requestUrl  微信上传临时素材的接口url
	 * @param file    要上传的文件
	 * @return String  上传成功后，微信服务器返回的消息
	 */
	public static String upLoadTempFile(String requestUrl, File file) {  
		StringBuffer buffer = new StringBuffer();  

		try{
			//1.建立连接
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();  //打开链接

			//1.1输入输出设置
			httpUrlConn.setDoInput(true);
			httpUrlConn.setDoOutput(true);
			httpUrlConn.setUseCaches(false); // post方式不能使用缓存
			//1.2设置请求头信息
			httpUrlConn.setRequestProperty("Connection", "Keep-Alive");
			httpUrlConn.setRequestProperty("Charset", "UTF-8");
			//1.3设置边界
			String BOUNDARY = "----------" + System.currentTimeMillis();
			httpUrlConn.setRequestProperty("Content-Type","multipart/form-data; boundary="+ BOUNDARY);

			// 请求正文信息
			// 第一部分：
			//2.将文件头输出到微信服务器
			StringBuilder sb = new StringBuilder();
			sb.append("--"); // 必须多两道线
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data;name=\"media\";filelength=\"" + file.length()
			+ "\";filename=\""+ file.getName() + "\"\r\n");
			sb.append("Content-Type:application/octet-stream\r\n\r\n");
			byte[] head = sb.toString().getBytes("utf-8");
			// 获得输出流
			OutputStream outputStream = new DataOutputStream(httpUrlConn.getOutputStream());
			// 将表头写入输出流中：输出表头
			outputStream.write(head);

			//3.将文件正文部分输出到微信服务器
			// 把文件以流文件的方式 写入到微信服务器中
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				outputStream.write(bufferOut, 0, bytes);
			}
			in.close();
			//4.将结尾部分输出到微信服务器
			byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
			outputStream.write(foot);
			outputStream.flush();
			outputStream.close();


			//5.将微信服务器返回的输入流转换成字符串  
			InputStream inputStream = httpUrlConn.getInputStream();  
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  

			String str = null;  
			while ((str = bufferedReader.readLine()) != null) {  
				buffer.append(str);  
			}  

			bufferedReader.close();  
			inputStreamReader.close();  
			// 释放资源  
			inputStream.close();  
			inputStream = null;  
			httpUrlConn.disconnect();  


		} catch (IOException e) {
			System.out.println("发送POST请求出现异常！" + e);
			e.printStackTrace();
		} 
		return buffer.toString();
	}
	
	private static String getExt(String contentType){
		if("image/jpeg".equals(contentType)){
			return ".jpg";
		}else if("image/png".equals(contentType)){
			return ".png";
		}else if("image/gif".equals(contentType)){
			return ".gif";
		}
		
		return null;
	}


}

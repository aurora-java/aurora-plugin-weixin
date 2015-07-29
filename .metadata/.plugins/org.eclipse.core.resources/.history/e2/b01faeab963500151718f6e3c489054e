package aurora.plugin.weixin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

}

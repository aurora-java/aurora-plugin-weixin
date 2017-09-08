package aurora.plugin.weixin;


import java.io.File;
import java.io.UnsupportedEncodingException;


import org.json.JSONException;
import org.json.JSONObject;

import uncertain.ocm.IObjectRegistry;





/**@desc  : 临时素材业务类
 * 
 * @author: shirayner
 * @date  : 2017-8-18 下午2:07:25
 */
public class TempMaterialService {
	
	//上传临时素材url
	public static String uploadTempMaterial_url="https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

	//获取临时素材url
	public static String getTempMaterial_url="https://qyapi.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";

	/**
	 * @desc ：上传临时素材
	 *  
	 * @param accessToken   接口访问凭证 
	 * @param type   媒体文件类型，分别有图片（image）、语音（voice）、视频（video），普通文件(file) 
	 * @param fileUrl  本地文件的url。例如 "D/1.img"。
	 * @return JSONObject   上传成功后，微信服务器返回的参数，有type、media_id	、created_at
	 * @throws JSONException 
	 */
	public static JSONObject uploadTempMaterial(String taskName,IObjectRegistry objectRegistry,String type,String fileUrl) throws JSONException{
		
		//1.创建本地文件
		File file=new File(fileUrl);

		//2.拼接请求url
		String accessToken=QiyeWeixinNetworkUtil.getTokenByTaskName(taskName, objectRegistry);

		uploadTempMaterial_url=uploadTempMaterial_url.replace("ACCESS_TOKEN", accessToken)
				.replace("TYPE", type);

		//3.调用接口，发送请求，上传文件到微信服务器
		String result=UrlHelper.upLoadTempFile(uploadTempMaterial_url, file);

		//4.json字符串转对象：解析返回值，json反序列化
		result = result.replaceAll("[\\\\]", "");
		System.out.println("result:" + result);
		JSONObject resultJSON = new JSONObject(result);

		//5.返回参数判断
		if (resultJSON != null) {
			if (resultJSON.get("media_id") != null) {
				System.out.println("上传" + type + "临时素材成功:"+resultJSON.get("media_id"));
				return resultJSON;
			} else {
				System.out.println("上传" + type + "临时素材成功失败");
			}
		}
		return null;
	}

	/**
	 * 2.获取临时素材
	 * @param taskName
	 * @param savePath
	 * @param objectRegistry
	 * @param mediaId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getTempMaterial(String taskName,String savePath,IObjectRegistry objectRegistry,String mediaId) throws UnsupportedEncodingException{

		//1.拼接请求url
		//1.1获取accessToken
		String accessToken=QiyeWeixinNetworkUtil.getTokenByTaskName(taskName, objectRegistry);
		System.out.println("accessToken"+accessToken);
		getTempMaterial_url=getTempMaterial_url.replace("ACCESS_TOKEN", accessToken)
				.replace("MEDIA_ID", mediaId);

		
		//2.调用接口，发送请求，获取临时素材
		File file=UrlHelper.getFile(getTempMaterial_url,savePath);
		
		System.out.println("file:"+file.getName());

	return file.getName();
	}
	


}

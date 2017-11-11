package connectors;

import com.box.sdk.BoxAPIConnection;

public class BoxConnector extends OAuthConnector{
	//joe app
	public static String clientId = "zfn7obf9hv6bu326uxe1410je1h46vks";
	public static String secretId = "CaZCy0ZSrNnKDRhd1mlyIqvJMkMtKLjA";
	public static String redirect_uri = "https://www.test.com/test";
	
	public static String auth_ur = "https://account.box.com/api/oauth2/authorize?response_type=code&client_id="+clientId+"&state=A12345FB&redirect_uri="+redirect_uri+"&box_login=mklinchin@metavistech.com";
	public static String token_url = "https://api.box.com/oauth2/token";
	
	public BoxConnector connect(String username){
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+clientId+"&client_secret="+secretId;
		String authParams = "?response_type=code&client_id="+clientId+"&state=A12345FB&redirect_uri="+redirect_uri+"&box_login="+username;
		
		getBrowser(auth_ur+authParams, token_url, tokenParams, redirect_uri);
		return this;
	}
	
	public String getTitle(){
		return "Connect to Box";
	}
	
	public BoxAPIConnection getBoxApi(){
		return new BoxAPIConnection(clientId, secretId, (String)getResponse().get("access_token"), (String)getResponse().get("refresh_token"));
	}
}
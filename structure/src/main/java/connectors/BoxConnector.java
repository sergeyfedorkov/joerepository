package connectors;

import com.box.sdk.BoxAPIConnection;

public class BoxConnector extends OAuthConnector{
	public static String clientId = "zfn7obf9hv6bu326uxe1410je1h46vks";
	public static String secretId = "CaZCy0ZSrNnKDRhd1mlyIqvJMkMtKLjA";
	
	public BoxConnector connect(String username){
		String authParams = "response_type=code&client_id="+clientId+"&redirect_uri="+getRedirectUri()+"&box_login="+username;
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+clientId+"&client_secret="+secretId;
		
		openBrowser(authParams, tokenParams);
		return this;
	}
	
	@Override
	public String getRedirectUri(){
		return "https://www.test.com/test";
	}
	
	@Override
	public String getAuthorizationUrl(String parameters){
		return "https://account.box.com/api/oauth2/authorize?"+parameters;
	}
	
	@Override
	public String getTokenUrl(){
		return "https://api.box.com/oauth2/token";
	}

	@Override
	public String getTitle(){
		return "Connect to Box";
	}
	
	public BoxAPIConnection getBoxApi(){
		return new BoxAPIConnection(clientId, secretId, getAccessToken(), getRefreshToken());
	}
}
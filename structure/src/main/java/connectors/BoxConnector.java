package connectors;

import com.box.sdk.BoxAPIConnection;

public class BoxConnector extends OAuthConnector{
	public OAuthConnector connect(String username){
		String authParams = "response_type=code&client_id="+getClientId()+"&redirect_uri="+getRedirectUri()+"&box_login="+username;
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+getClientId()+"&client_secret="+getSecretId();
		
		return openBrowser(authParams, tokenParams);
	}
	
	@Override
	public String getClientId(){
		return "zfn7obf9hv6bu326uxe1410je1h46vks";
	}
	
	@Override
	public String getSecretId(){
		return "CaZCy0ZSrNnKDRhd1mlyIqvJMkMtKLjA";
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
		return new BoxAPIConnection(getClientId(), getSecretId(), getAccessToken(), getRefreshToken());
	}
}
package connectors;

public class ExchangeConnector extends OAuthConnector{
	public static String resource = "https://outlook.office365.com";
	
	public OAuthConnector connect(String username){
		String authParams = "resource="+resource+"&client_id="+getClientId()+"&response_type=code&redirect_uri="+getRedirectUri()+"&login_hint="+username;
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+getClientId()+"&resource="+resource+"&redirect_uri="+getRedirectUri();
		
		return openBrowser(authParams, tokenParams);
	}
	
	@Override
	public String getClientId(){
		return "a0c73c16-a7e3-4564-9a95-2bdf47383716";
	}
	
	@Override
	public String getSecretId(){
		return null;
	}
	
	@Override
	public String getRedirectUri(){
		return "urn:ietf:wg:oauth:2.0:oob";
	}
	
	@Override
	public String getAuthorizationUrl(String parameters){
		return "https://login.windows.net/common/oauth2/authorize?"+parameters;
	}
	
	@Override
	public String getTokenUrl(){
		return "https://login.windows.net/common/oauth2/token";
	}
	
	@Override
	public String getTitle(){
		return "Connect to Exchange";
	}
}
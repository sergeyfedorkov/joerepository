package connectors;

public class ExchangeConnector extends OAuthConnector{
	public static String clientId = "a0c73c16-a7e3-4564-9a95-2bdf47383716";
	public static String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
	public static String resource = "https://outlook.office365.com";
	
	public static String auth_ur = "https://login.windows.net/common/oauth2/authorize";
	public static String tokenUrl = "https://login.windows.net/common/oauth2/token";
	
	public ExchangeConnector connect(String username){
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+clientId+"&resource="+resource+"&redirect_uri="+redirect_uri;
		String authParams = "?resource="+resource+"&client_id="+clientId+"&response_type=code&redirect_uri="+redirect_uri+"&login_hint="+username;
		
		getBrowser(auth_ur+authParams, tokenUrl, tokenParams, redirect_uri);
		return this;
	}
	
	public String getAccessToken(){
		return (String)getResponse().get("access_token");
	}
	
	public String getTitle(){
		return "Connect to Exchange";
	}
}
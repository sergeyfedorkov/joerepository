package connectors.oauth;


public class ExchangeConnector extends OAuthConnector{
	public OAuthConnector connect(String username){
		String authParams = "resource="+getResource()+"&client_id="+getClientId()+"&response_type=code&redirect_uri="+getRedirectUri()+"&login_hint="+username;
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+getClientId()+"&resource="+getResource()+"&redirect_uri="+getRedirectUri();
		
		OAuthConnector connector = openBrowser(authParams, tokenParams);
		
		//String refreshParameters = "grant_type=refresh_token&redirect_uri="+getRedirectUri()+"&client_id="+getClientId()+"&refresh_token="+getRefreshToken()+"&resource="+getResource();
		//connector.refresh(refreshParameters);
		return connector;
	}
	
	public String getResource(){
		//return "https://outlook.office365.com";
		return "https://graph.windows.net";
	}
	
	@Override
	public String getClientId(){
		//return "a0c73c16-a7e3-4564-9a95-2bdf47383716";//Microsoft Exchange Online Remote PowerShell
		//return "50afce61-c917-435b-8c6d-60aa5a8b8aa7";//powershell
		return "1b730954-1685-4b74-9bfd-dac224a7b895";
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
		//return "https://login.windows.net/common/oauth2/authorize?"+parameters;
		return "https://login.microsoftonline.com/common/oauth2/authorize?"+parameters;
	}
	
	@Override
	public String getTokenUrl(){
		//return "https://login.windows.net/common/oauth2/token";
		return "https://login.microsoftonline.com/common/oauth2/token";
	}
	
	@Override
	public String getTitle(){
		return "Connect to Exchange";
	}
}
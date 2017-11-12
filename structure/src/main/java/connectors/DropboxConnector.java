package connectors;

public class DropboxConnector extends OAuthConnector{
	public DropboxConnector connect(String username){
		String authParams = "client_id="+getClientId()+"&response_type=code&redirect_uri="+getRedirectUri();
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+getClientId()+"&client_secret="+getSecretId()+"&redirect_uri="+getRedirectUri();
				
		openBrowser(authParams, tokenParams);
		return this;
	}
	
	@Override
	public String getClientId(){
		return "u1eex8sdsp2g7hu";
	}
	
	@Override
	public String getSecretId(){
		return "r12cobb4gcszqvz";
	}
	
	@Override
	public String getRedirectUri(){
		return "https://www.test.com/com";
	}
	
	@Override
	public String getAuthorizationUrl(String parameters){
		return "https://www.dropbox.com/oauth2/authorize?"+parameters;
	}
	
	@Override
	public String getTokenUrl(){
		return "https://api.dropboxapi.com/oauth2/token";
	}

	@Override
	public String getTitle() {
		return "Connect to Dropbox";
	}
}
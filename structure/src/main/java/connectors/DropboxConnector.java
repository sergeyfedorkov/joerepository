package connectors;

public class DropboxConnector extends OAuthConnector{
	private final String client_id = "u1eex8sdsp2g7hu";
	private final String secret_id = "r12cobb4gcszqvz";
	
	public DropboxConnector connect(String username){
		String authParams = "client_id="+client_id+"&response_type=code&redirect_uri="+getRedirectUri();
		String tokenParams = "grant_type=authorization_code&code={code}&client_id="+client_id+"&client_secret="+secret_id+"&redirect_uri="+getRedirectUri();
				
		openBrowser(authParams, tokenParams);
		return this;
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
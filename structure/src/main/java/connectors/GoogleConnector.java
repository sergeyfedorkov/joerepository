package connectors;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

public class GoogleConnector {
	private String client_id = "101140258396.apps.googleusercontent.com";
	private String client_secret = "dF0TBt2M_owgVyluMd4J8mVE";
	
	private static final String OAUTH2_REDIRECT_URL = "urn:ietf:wg:oauth:2.0:oob";
	public static final String[] DRIVE_SCOPES = new String[] {DriveScopes.DRIVE, DriveScopes.DRIVE_FILE};
	
	private String code;
	private GoogleCredential credential;
	
	public GoogleConnector connect(String username){
		getGoogleConnector(username);
		return this;
	}
	
	private void getGoogleConnector(String username){
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(), client_id, client_secret, Arrays.asList(DRIVE_SCOPES)).build();
		
		final Shell dialog = new Shell(Display.getDefault());
		
		dialog.setText("Connect to Google");
		dialog.setLayout(new GridLayout(1, false));
		
		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(flow.newAuthorizationUrl().setRedirectUri(OAUTH2_REDIRECT_URL).build() + getLoginHintOption(username));
		browser.addTitleListener(new TitleListener() {
			@Override
			public void changed(TitleEvent event) {
				if (event.title.contains("code=")) {
					code = getValueOf(event.title, "code");
					dialog.close();
				}
			}
		});

		dialog.setSize(800, 800);
		dialog.pack();
		dialog.open();
		
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		try{
			GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(OAUTH2_REDIRECT_URL).execute();
			credential = new GoogleCredential.Builder().setTransport(new NetHttpTransport()).setJsonFactory(new JacksonFactory()).setClientSecrets(client_id, client_secret).build().setFromTokenResponse(response);
		}catch(Exception e){}
	}

	public GoogleCredential getCredential() {
		return credential;
	}
	
	/*
	 * Private section
	 */
	private static String getLoginHintOption(String username) {
		return String.format("&login_hint=%s", username.replaceAll("@", "%40"));
	}
	
	private String getValueOf(String line, String key) {
		return new String(line.substring(line.indexOf(key + "=") + key.length() + 1));
	}
}
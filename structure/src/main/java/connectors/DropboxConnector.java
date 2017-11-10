package connectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;

public class DropboxConnector {
	private final String client_id = "u1eex8sdsp2g7hu";
	private final String secret_id = "r12cobb4gcszqvz";
	
	private String token;
	
	public DropboxConnector connect(String username){
		getDropboxConnector(username);
		return this;
	}
	
	private void getDropboxConnector(String username){
        DbxWebAuth webAuth = new DbxWebAuth(DbxRequestConfig.newBuilder("").build(), new DbxAppInfo(client_id, secret_id));
        String url = webAuth.authorize(DbxWebAuth.newRequestBuilder().withNoRedirect().build());
        getBrowser(webAuth, url);
	}
	
	private void getBrowser(DbxWebAuth webAuth, String url){
		final Shell dialog = new Shell(Display.getDefault());
		
		dialog.setText("Connect to Dropbox");
		dialog.setLayout(new GridLayout(1, false));
		
		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(url);
		browser.addLocationListener(new LocationListener(){
			public void changed(LocationEvent arg0) {
				String content = browser.getText();
				String dataToken="data-token=\"";
				
				if (content.indexOf(dataToken) != -1){
					content = content.substring(content.indexOf(dataToken)+dataToken.length());
					content = content.substring(0, content.indexOf("\""));
					try {
						token = webAuth.finishFromCode(content).getAccessToken();
					} catch (DbxException e) {
						e.printStackTrace();
					}
					dialog.close();
				}
			}

			public void changing(LocationEvent arg0) {}
		});

		dialog.setSize(800, 550);
		dialog.pack();
		dialog.open();
		
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	public String getToken() {
		return token;
	}
}
package connectors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.box.sdk.BoxAPIConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoxConnector {
	private BoxAPIConnection api;
	
	public BoxConnector connect(String username){
		getBoxConnector(username);
		return this;
	}
	
	//joe app
	public static String clientId = "zfn7obf9hv6bu326uxe1410je1h46vks";
	public static String secretId = "CaZCy0ZSrNnKDRhd1mlyIqvJMkMtKLjA";
	public static String redirect_uri = "https://www.test.com/test";
	
	public static String auth_ur = "https://account.box.com/api/oauth2/authorize?response_type=code&client_id="+clientId+"&state=A12345FB&redirect_uri="+redirect_uri+"&box_login=mklinchin@metavistech.com";
	public static String token_url = "https://api.box.com/oauth2/token";
	
	private void getBoxConnector(String username){
		final Shell dialog = new Shell(Display.getDefault());
		
		dialog.setText("Connect to Box");
		dialog.setLayout(new GridLayout(1, false));
		
		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(auth_ur+"&box_login="+username);
		browser.addLocationListener(new LocationListener(){
			public void changed(LocationEvent arg0) {
				String patternCode = "&code=";
				if (arg0.location.startsWith(redirect_uri) && arg0.location.indexOf(patternCode) != -1){
					dialog.close();
					getTokens(arg0.location.substring(arg0.location.indexOf(patternCode)+patternCode.length()));
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
	
	private void getTokens(String code){
		try{
			String params = "grant_type=authorization_code&code={code}&client_id="+clientId+"&client_secret="+URLEncoder.encode(secretId, "UTF-8");
			String parameters = params.replaceAll("\\{code\\}", code);
			URLConnection connection = new URL(token_url).openConnection();
			connection.setDoOutput(true);
			((HttpURLConnection)connection).setRequestMethod("POST");
			connection.addRequestProperty("Content-Length", parameters.getBytes().length+"");
					
			OutputStream output = connection.getOutputStream();
			output.write(parameters.getBytes());
			
			InputStream stream = ((HttpURLConnection)connection).getInputStream();
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8")); 
			StringBuilder responseStrBuilder = new StringBuilder();
	
			String inputStr;
			while ((inputStr = streamReader.readLine()) != null) responseStrBuilder.append(inputStr);
			
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			Map<String, Object> responseMap = (Map<String, Object>) mapper.readValue(responseStrBuilder.toString(), HashMap.class);
			api = new BoxAPIConnection(clientId, secretId, (String)responseMap.get("access_token"), (String)responseMap.get("refresh_token"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public BoxAPIConnection getApi() {
		return api;
	}
}

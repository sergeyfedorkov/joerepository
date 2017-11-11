package connectors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class OAuthConnector {
	private static final String PATTERN_CODE = "code=";
	private Map<String, Object> response;
	
	public abstract String getTitle();

	public void getBrowser(String oauthUrl, final String tokenUrl, final String parameters, final String redirectUrl){
		final Shell dialog = new Shell(Display.getDefault());
		
		dialog.setText(getTitle());
		dialog.setLayout(new GridLayout(1, false));
		
		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(oauthUrl);
		browser.addLocationListener(new LocationListener(){
			public void changed(LocationEvent arg0) {
				if (arg0.location.startsWith(redirectUrl) && arg0.location.indexOf(PATTERN_CODE) != -1){
					dialog.close();
					getTokens(tokenUrl, parameters.replaceAll("\\{code\\}", getCode(arg0.location)));
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
	
	private String getCode(String location){
		String code = location.substring(location.indexOf(PATTERN_CODE)+PATTERN_CODE.length());
		if (code.indexOf("&") != -1) code = code.substring(0, code.indexOf("&"));
		return code;
	}
	
	@SuppressWarnings("unchecked")
	private void getTokens(String tokenUrl, String parameters){
		try{
			URLConnection connection = new URL(tokenUrl).openConnection();
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
			response = (Map<String, Object>)mapper.readValue(responseStrBuilder.toString(), HashMap.class);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Map<String, Object> getResponse() {
		return response;
	}
}
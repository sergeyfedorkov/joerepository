package connectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import structure.ClaimsDialog;

public class ClaimsConnector {
	public static final String HTTP_HEADER_COOKIE = "Cookie";
	public static final String SPAPI_ITEM_UNIQUE_ID = "ETag";
	
	public String claims(final String url){
		ClaimsDialog claims = new ClaimsDialog(new Shell(Display.getDefault(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.SYSTEM_MODAL));
		claims.open(url);
		return claims.getCookie();
	}
}
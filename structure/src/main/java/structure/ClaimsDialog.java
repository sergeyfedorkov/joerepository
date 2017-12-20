package structure;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import structure.utils.WinInet;

import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class ClaimsDialog extends Dialog {
	private String url;

	private String fedAuth;
	private String rtFa;
	private String cookieObSSOCookie;
	private String cookieASPXAUTH;
	private String ctSession;
	private String ctSessionSsl;
	private String nsc_tmaa;
	private String nsc_tmas;
	private String forefront;
	private String gmac;
	private String siteMinder;
	private String requestId;
	private String dltt;
	private String dol;
	private String dltt_prod;
	private String dol_prod;
	private String shellSession;
	private String shellPersist;
	
	public ClaimsDialog(Shell parent) {
		super(parent);
	}

	public void open(String url) {
		this.url = url;
		Shell dialog = getParent();
		
		dialog.setText("Connect to SharePoint");
		dialog.setLayout(new GridLayout(1, false));
		dialog.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				checkCookie();
			}
		});

		Browser browser = new Browser(dialog, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.setUrl(url+"/_layouts/viewlsts.aspx");

		dialog.setSize(800, 550);
		dialog.pack();
		dialog.open();
		
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	private void checkCookie() {
		fedAuth = getCookie("FedAuth");
		rtFa = getCookie("rtFa");
		cookieObSSOCookie = getCookie("ObSSOCookie");
		cookieASPXAUTH = getCookie(".ASPXAUTH");
		ctSession = getCookie("CTSESSION");
		ctSessionSsl = getCookie("CTSESSIONSSL");
		nsc_tmaa = getCookie("NSC_TMAA");
		nsc_tmas = getCookie("NSC_TMAS");
		forefront = getCookie("iCenter-ALLVIPS");
		requestId = getCookie("RequestID");
		siteMinder = getCookie("SMSESSION");
		dltt = getCookie("TS94c35f");
		dol = getCookie("dol_persist");
		dltt_prod = getCookie("TSea3ea7");
		dol_prod = getCookie("dol_persist_prd");
		shellSession = getCookie("NLSessionStrunk1");
		shellPersist = getCookie("NLSessionStrunk1PersistForOffice");
	}
	
	private String getCookie(String cookie) {
		try {
			IntByReference size = new IntByReference(1024*10);
			byte data[] = new byte[1024*10];
			
			boolean rc = WinInet.INSTANCE.InternetGetCookieExW(new WString(url), new WString(cookie), data, size, 0x2000, 0);
			if (rc) {
				ByteArrayOutputStream bais = new ByteArrayOutputStream();
				bais.write(data, 0, size.getValue()*2);
				return bais.toString("UTF-16LE").trim(); // Windows WCHAR
			}
		} catch(UnsupportedEncodingException ex) {	}
		
		return null;
	}
	
	public String getCookie() {
		if (fedAuth!=null) {
			if (rtFa!=null) {
				return String.format("%s; %s",fedAuth, rtFa);
			} else if (siteMinder!=null) {
				return String.format("%s; %s",fedAuth, siteMinder);
			} else if (dltt!=null && dol!=null) {
				boolean r1 = assignIdentityManagerCookie(dol);
				boolean r2 = true;
				if (dltt!=null) r2 = assignIdentityManagerCookie(dltt);
				
				if (r1 && r2) {
					return String.format("%s", fedAuth);
				} else if (dltt!=null) {
					return String.format("%s; %s; %s", dol, fedAuth, dltt);
				} else {
					return String.format("%s; %s", dol, fedAuth);
				}
			} else if (dol_prod!=null) { 
				boolean r1 = assignIdentityManagerCookie(dol_prod);
				boolean r2 = true;
				if (dltt_prod!=null) r2 = assignIdentityManagerCookie(dltt_prod);
				
				if (r1 && r2) {
					return String.format("%s", fedAuth);
				} else if (dltt_prod!=null) {
					return String.format("%s; %s; %s", dol_prod, fedAuth, dltt_prod);
				} else {
					return String.format("%s; %s", dol_prod, fedAuth);
				}
			} else if (shellSession != null && shellPersist != null) {
				return String.format("%s; %s; %s;", fedAuth, shellSession, shellPersist);
			} else {
				return String.format("%s; ",fedAuth);
			}
		} else if (cookieObSSOCookie!=null && cookieASPXAUTH!=null) {
			assignIdentityManagerCookie(cookieObSSOCookie);
			return String.format("%s", cookieASPXAUTH);
		} else if (ctSession!=null && ctSessionSsl!=null) {
			return String.format("%s; %s", ctSession, ctSessionSsl);
		} else if (nsc_tmaa!=null && nsc_tmas!=null) {
			return String.format("%s; %s", nsc_tmaa, nsc_tmas);
		} else if (forefront!=null) {
			return String.format("%s; ", forefront);
		} else if (gmac!=null) {
			return String.format("%s; ", gmac);
		} else if (requestId!=null) {
			return String.format("%s; spAuthType=Windows", requestId);
		} else if (siteMinder!=null) {
			return String.format("%s; ", siteMinder);
		} else {
			return null;
		}
	}
	
	private boolean assignIdentityManagerCookie(String fullCookie) {
		boolean rc = false;
		
		int i = fullCookie.indexOf("=");
		if (i>0) {
			String name = fullCookie.substring(0, i);
			String value = fullCookie.substring(i+1);
			
			for (HttpCookie cookie:((CookieManager)java.net.CookieHandler.getDefault()).getCookieStore().getCookies()) {
				String n = cookie.getName();
				if (n!=null && n.equals(name)) {
					cookie.setValue(value);
					rc = true;
					break;
				}
			}
		}
		
		return rc;
	}
}
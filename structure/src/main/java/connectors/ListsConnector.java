package connectors;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import com.microsoft.schemas.sharepoint.soap.Authentication;
import com.microsoft.schemas.sharepoint.soap.AuthenticationSoap;
import com.microsoft.schemas.sharepoint.soap.Lists;
import com.microsoft.schemas.sharepoint.soap.ListsSoap;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.internal.ws.transport.Headers;

public class ListsConnector {
	public static ListsSoap getService(String url, String claims){
		ListsSoap soap = new Lists().getListsSoap();
		
		((BindingProvider)soap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url+"/_vti_bin/Lists.asmx");
		
		Headers reqHeaders = new Headers();
		((BindingProvider)soap).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
		//reqHeaders.set(ClaimsConnector.HTTP_HEADER_COOKIE, claims);
		reqHeaders.set("Authorization", "Basic " + Base64.encode(("joe@metavistech.com:43046721Jo").getBytes()));
		
		return soap;
	}
	
	public static AuthenticationSoap getAuthenticationService(String url, String claims){
		AuthenticationSoap soap = new Authentication().getAuthenticationSoap();
		
		((BindingProvider)soap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url+"/_vti_bin/Authentication.asmx");
		
		Headers reqHeaders = new Headers();
		((BindingProvider)soap).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
		//reqHeaders.set(ClaimsConnector.HTTP_HEADER_COOKIE, claims);
		//reqHeaders.set("Authorization", "Basic " + Base64.encode(("joe@metavistech.com:43046721Jo").getBytes()));
		return soap;
	}
}
package connectors;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import com.microsoft.schemas.sharepoint.soap.Lists;
import com.microsoft.schemas.sharepoint.soap.ListsSoap;
import com.sun.xml.internal.ws.transport.Headers;

public class ListsConnector {
	public static ListsSoap getService(String url, String claims){
		ListsSoap soap = new Lists().getListsSoap();
		
		((BindingProvider)soap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url+"/_vti_bin/Lists.asmx");
		
		Headers reqHeaders = new Headers();
		((BindingProvider)soap).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
		reqHeaders.set(ClaimsConnector.HTTP_HEADER_COOKIE, claims);
		
		return soap;
	}
}
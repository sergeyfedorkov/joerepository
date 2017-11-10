
package com.microsoft.schemas.sharepoint.soap;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "Lists", targetNamespace = "http://schemas.microsoft.com/sharepoint/soap/", wsdlLocation = "file:/D:/METAVIS/projects/structure/wsdl/Lists.wsdl")
public class Lists
    extends Service
{

    private final static URL LISTS_WSDL_LOCATION;
    private final static WebServiceException LISTS_EXCEPTION;
    private final static QName LISTS_QNAME = new QName("http://schemas.microsoft.com/sharepoint/soap/", "Lists");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("file:/D:/METAVIS/projects/structure/wsdl/Lists.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        LISTS_WSDL_LOCATION = url;
        LISTS_EXCEPTION = e;
    }

    public Lists() {
        super(__getWsdlLocation(), LISTS_QNAME);
    }

    public Lists(WebServiceFeature... features) {
        super(__getWsdlLocation(), LISTS_QNAME, features);
    }

    public Lists(URL wsdlLocation) {
        super(wsdlLocation, LISTS_QNAME);
    }

    public Lists(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, LISTS_QNAME, features);
    }

    public Lists(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Lists(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns ListsSoap
     */
    @WebEndpoint(name = "ListsSoap")
    public ListsSoap getListsSoap() {
        return super.getPort(new QName("http://schemas.microsoft.com/sharepoint/soap/", "ListsSoap"), ListsSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ListsSoap
     */
    @WebEndpoint(name = "ListsSoap")
    public ListsSoap getListsSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://schemas.microsoft.com/sharepoint/soap/", "ListsSoap"), ListsSoap.class, features);
    }

    /**
     * 
     * @return
     *     returns ListsSoap
     */
    @WebEndpoint(name = "ListsSoap12")
    public ListsSoap getListsSoap12() {
        return super.getPort(new QName("http://schemas.microsoft.com/sharepoint/soap/", "ListsSoap12"), ListsSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ListsSoap
     */
    @WebEndpoint(name = "ListsSoap12")
    public ListsSoap getListsSoap12(WebServiceFeature... features) {
        return super.getPort(new QName("http://schemas.microsoft.com/sharepoint/soap/", "ListsSoap12"), ListsSoap.class, features);
    }

    private static URL __getWsdlLocation() {
        if (LISTS_EXCEPTION!= null) {
            throw LISTS_EXCEPTION;
        }
        return LISTS_WSDL_LOCATION;
    }

}

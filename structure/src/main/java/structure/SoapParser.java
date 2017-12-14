package structure;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SoapParser {
	public static String LIST_DOES_NOT_EXISTS = "0x82000006";
	
	public static DocumentBuilder createNewXMLDocument() {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {}
		return builder;
	}
	
	public static String getErrorText(Object xml) {
		List<Object> xmls = new ArrayList<Object>();
		xmls.add(xml);
		return getErrorText(xmls);
	}
	
	public static String getErrorText(List<Object> xml) {
		String content = getContent(xml);
		if (content == null) return null;
		String result = null;
		
		try {
		    Document document = createNewXMLDocument().parse(new ByteArrayInputStream(content.getBytes()));
			
		    NodeList errorTextNode = document.getElementsByTagName("ErrorText");
		    if (errorTextNode != null && errorTextNode.getLength()>0) {
		    	result = errorTextNode.item(0).getTextContent();
		    }
			
		    NodeList errorStringNode = document.getElementsByTagName("errorstring");
		    if (errorStringNode != null && errorStringNode.getLength()>0) {
		    	if (result == null) result = "";
		    	result += errorStringNode.item(0).getTextContent();
		    }
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return result != null?result.replaceAll("\n", "").replaceAll("\\s{2,}", " "):null;
	}
	
	public static String getErrorCode(Object xml) {
		List<Object> xmls = new ArrayList<Object>();
		xmls.add(xml);
		return getErrorCode(xmls);
	}
	
	public static String getErrorCode(List<Object> xml) {
		String content = getContent(xml);
		if (content == null) return null;
		String result = null;
		
		try {
		    Document document = createNewXMLDocument().parse(new ByteArrayInputStream(content.getBytes()));
			
		    NodeList errorTextNode = document.getElementsByTagName("errorcode");
		    if (errorTextNode != null && errorTextNode.getLength()>0) result = errorTextNode.item(0).getTextContent();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return result != null?result:null;
	}
	
	public static String getContent(List<Object> xml) {
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			StringWriter sw = new StringWriter();
			trans.transform(new DOMSource((Element)xml.get(0)), new StreamResult(sw));
			return sw.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	    
	    return null;
	}
}

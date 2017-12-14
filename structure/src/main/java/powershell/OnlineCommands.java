package powershell;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class OnlineCommands {
	public abstract class DocumentStreamProcessor {
		public void process(Document document){};
		public void exception(Document document){};
	}
	
	public static final String NS_F = "http://schemas.microsoft.com/wbem/wsman/1/wsmanfault";
	public String NS_A = "http://www.w3.org/2005/08/addressing";
	public static final int PARSE_POS = 21;
	public static final int ARGUMENTS_SIZE = 2056;
	
	public static final String FAULT = "http://www.w3.org/2005/08/addressing/fault";
	public static final String FAULT1 = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
	public static final String SOAP_FAULT = "http://www.w3.org/2005/08/addressing/soap/fault";
	public static final String WSMAN_FAULT = "http://schemas.dmtf.org/wbem/wsman/1/wsman/fault";
	public static final String NS_A_EXCHANGE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	
	public static final String OFFICE365_ENDPOINT = "https://outlook.office365.com/powershell-liveid?PSVersion=4.0";
	
	public static final String COMPANY_INFORMATION_RESPONSE = "http://provisioning.microsoftonline.com/IProvisioningWebService/GetCompanyInformationResponse";
	public static final String CREATE_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse";
	public static final String COMMAND_RESPONSE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandResponse";
	public static final String RECEIVE_RESPONSE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/ReceiveResponse";
	public static final String NS_RSP = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell";
	
	private String WAIT_ERROR = "Please wait for ";
	private String REQUEST_TOO_FREQUENT = "Your request is too frequent";
	
	public static final String NEW_FOLDER = "";
	public final static String PATH = "D:/GIT/structure/";
	public String result;
	private String accessToken;
	
	public OnlineCommands(String accessToken){
		this.accessToken=accessToken;
	}
	
	public void update(String userPrincipalName) throws Exception{
		 Map<String, Object> arguments = new TreeMap<String, Object>();
	     arguments.put("-Identity", userPrincipalName);

		
		runCommand(NEW_FOLDER, arguments, new DocumentStreamProcessor() {
			public void process(Document document) {
				parseResult(document);
			}
		});
	}
	
	public void parseResult(Document document) {
		NodeList list = document.getElementsByTagName("S");
		if (list != null){
			for (int i=0;i<list.getLength();i++){
				Node node = list.item(i);
				if (node.getAttributes().getNamedItem("N") != null && node.getAttributes().getNamedItem("N").getNodeValue().equals("InformationalRecord_Message")){
					result = node.getTextContent();
					return;
				}
			}
		}
		
		NodeList ts = document.getElementsByTagName("T");
		for (int i=0;i<ts.getLength();i++){
			Node t = ts.item(i);
			if (t.getTextContent().equals("System.Management.Automation.ErrorRecord")){
				result = document.getElementsByTagName("ToString").item(0).getTextContent();
				break;
			}
		}
		
		if (result == null){
			ts = document.getElementsByTagName("T");
			for (int i=0;i<ts.getLength();i++){
				Node t = ts.item(i);
				if (t.getTextContent().equals("System.Management.Automation.WarningRecord")){
					result = document.getElementsByTagName("ToString").item(0).getTextContent();
					break;
				}
			}
		}
	}
	
	public void changeNSA() {
		NS_A = NS_A_EXCHANGE;
	}
	
	public void runCommand(String commandName, Map<String, Object> arguments, DocumentStreamProcessor processor) throws Exception {
		changeNSA();
		
		String commandId = command(commandName, getShellId(null, 5), arguments, null, null);
		List<String> streams = recieveCommand(getShellId(null, 5), commandId, null, null);
		
		for (int i=0;i<streams.size();i++) {
			String stream = streams.get(i);
			InputSource is = new InputSource(new StringReader(stream));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			
			if (processor != null) {
				if (streams.size() >1){
					if (i != streams.size()-1) processor.process(document);
				} else {
					processor.exception(document);
				}
			}
		}
	}
	
	public String getShellId(String endpointUrl, int tries){
		try{
			String sessionId = getRandomGuid();
			String shellId = create(sessionId, endpointUrl);
			recieve(shellId, sessionId, endpointUrl);
			recieve(shellId, sessionId, endpointUrl);
			return shellId;
		}catch(Exception e){
			String message = e.getMessage();
			if (message.contains(REQUEST_TOO_FREQUENT)) {
				throw new RuntimeException(e);
			} else {
				if (tries>5) return null;
				try {
					int wait = 30;
					int index = message.indexOf(WAIT_ERROR);
					if (index > -1) {
						int index1 = message.indexOf(" ", index+16);
						String seconds = message.substring(index+16, index1);
						wait = new Integer(seconds.trim());
					}
					Thread.sleep(wait*1000);
				} catch (Exception e1) {}
				return getShellId(endpointUrl, tries++);
			}
		}
	}
	
	private String getRandomGuid(){
		return "uuid:"+UUID.randomUUID().toString().toUpperCase();
	}
	
	public String create(String sessionId, String endpointUrl) throws Exception{
		Document document = getDocument("/soap/create.xml");
		document.getDocumentElement().getElementsByTagName("p:SessionId").item(0).setTextContent(sessionId);
		document.getDocumentElement().getElementsByTagName("p:OperationID").item(0).setTextContent(getRandomGuid());
		document.getDocumentElement().getElementsByTagName("a:MessageID").item(0).setTextContent(getRandomGuid());
		if (endpointUrl != null) {
			document.getDocumentElement().getElementsByTagName("a:To").item(0).setTextContent(endpointUrl);
			document.getDocumentElement().getElementsByTagName("w:ResourceURI").item(0).setTextContent("http://schemas.microsoft.com/powershell/Microsoft.PowerShell");
		} else {
			endpointUrl = OFFICE365_ENDPOINT;
		}
		
		SOAPMessage msg = MessageFactory.newInstance().createMessage();
        SOAPPart soapPart = msg.getSOAPPart();  
        soapPart.setContent(new StreamSource(new ByteArrayInputStream(getStringFromDocument(document).getBytes())));
        msg.saveChanges();

        auth(msg);
	    msg.getMimeHeaders().setHeader("Content-Type", "application/soap+xml;charset=UTF-8");
	    
	    SOAPMessage resp = OverloadHandler.getInstance().soapCall(SOAPConnectionFactory.newInstance().createConnection(), msg, endpointUrl, 0);
		return processGetShellId(resp);
	}
	
	private String processGetShellId(SOAPMessage response) throws SOAPException {
		String action = getAction(response);
		
		if (isError(action)) {
			throw new SOAPException(getFaultText(response));
		} else if (action.equalsIgnoreCase(CREATE_RESPONSE)) {
			return response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_RSP, "ShellId").item(0).getTextContent();
		}
		
		return null;
	}
	
	private Document getDocument(String documentName) throws Exception{
		InputStream input = new FileInputStream(new File(PATH+documentName));
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
		input.close();
		return document;
	}
	
	public void recieve(String shellId, String sessionId, String endpointUrl) throws Exception {
		Document document = getDocument("/soap/recieve.xml");
		document.getDocumentElement().getElementsByTagName("w:Selector").item(0).setTextContent(shellId);
		document.getDocumentElement().getElementsByTagName("p:SessionId").item(0).setTextContent(sessionId);
		document.getDocumentElement().getElementsByTagName("p:OperationID").item(0).setTextContent(getRandomGuid());
		document.getDocumentElement().getElementsByTagName("a:MessageID").item(0).setTextContent(getRandomGuid());
		if (endpointUrl != null) {
			document.getDocumentElement().getElementsByTagName("a:To").item(0).setTextContent(endpointUrl);
			document.getDocumentElement().getElementsByTagName("w:ResourceURI").item(0).setTextContent("http://schemas.microsoft.com/powershell/Microsoft.PowerShell");
		} else {
			endpointUrl = OFFICE365_ENDPOINT;
		}
		
		SOAPMessage msg = MessageFactory.newInstance().createMessage();  
        SOAPPart soapPart = msg.getSOAPPart();  
        soapPart.setContent(new StreamSource(new ByteArrayInputStream(getStringFromDocument(document).getBytes())));
        msg.saveChanges();

        auth(msg);
	    msg.getMimeHeaders().setHeader("Content-Type", "application/soap+xml;charset=UTF-8");

	    processCommonResponse(OverloadHandler.getInstance().soapCall(SOAPConnectionFactory.newInstance().createConnection(), msg, endpointUrl, 0));
	}
	
	public static String getStringFromDocument(Document doc) throws TransformerException {
		StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
	}
	
	protected String getFaultText(SOAPMessage response) throws SOAPException {
		NodeList textElements = response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_A, "Text");
		
		if (textElements.getLength() > 0) {
			String error = textElements.item(0).getTextContent();
			if (error != null && !error.trim().isEmpty()) return error;
			
			textElements = response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_F, "ProviderFault");
			
			if (textElements.getLength() > 0) {
				error = textElements.item(0).getTextContent();
				if (error != null && !error.trim().isEmpty()) return error;
			}
		}
		
		return null;
	}
	
	protected String getAction(SOAPMessage response) throws SOAPException {
		NodeList actionList = response.getSOAPPart().getEnvelope().getHeader().getElementsByTagNameNS(NS_A, "Action");
		
		if(actionList.getLength() > 0) return actionList.item(0).getTextContent();
		return null;
	}
	
	protected boolean isError(String action){
		return action != null && (action.equalsIgnoreCase(FAULT) || action.equalsIgnoreCase(SOAP_FAULT) || action.equalsIgnoreCase(WSMAN_FAULT) || action.equalsIgnoreCase(FAULT1));
	}
	
	private int getStringLength(String str){
		if (str == null) return 0;
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		return str.length();
	}
	
	@SuppressWarnings("unchecked")
	private int getValueLength(Object value){
		if (value instanceof List){
			int len = 0;
			for (String v:(List<String>)value) len+=(getStringLength(v)+7);
			if (((List<String>)value).size() == 0) len+=-5;
			return len;
		}
		
		int count = value!=null?getStringLength(value.toString()):0;
		return count;
	}
	
	@SuppressWarnings("unchecked")
	private void changeArguments(Document document, String commandName, Map<String, Object> arguments) throws Exception{
		Document documentArguments = getDocument("/soap/arguments.xml");
		Document documentArgumentName = getDocument("/soap/argumentName.xml");
		
		int refId = 0;
		NodeList nodes = documentArguments.getElementsByTagName("Obj");
		for (int i=0;i<nodes.getLength();i++){
			Node node = nodes.item(i);
			if (node.getAttributes().getNamedItem("N") != null && node.getAttributes().getNamedItem("N").getTextContent().equals("Args")){
				refId = Integer.parseInt(node.getAttributes().getNamedItem("RefId").getTextContent());
				break;
			}
		}
		
		documentArguments.getElementsByTagName("S").item(0).setTextContent(commandName);
		
		int argumentsLength = arguments.isEmpty()?-5:0;//-5 is the difference between closed tag && opened with closed tags
		Iterator<String> keys = arguments.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Object value = arguments.get(key);
			if (value == null && !key.endsWith(":")) key +=":";
			if (!key.startsWith("-")) key="-"+key;
			
			if (!(value instanceof Boolean)){
				argumentsLength+=getDocumentSize("/soap/argumentName.xml")+key.length()+getValueLength(value);
			
				documentArgumentName.getElementsByTagName("S").item(0).setTextContent(key);
				documentArgumentName.getElementsByTagName("Obj").item(0).getAttributes().getNamedItem("RefId").setTextContent((++refId)+"");
			} else {
				argumentsLength+=key.length()+getValueLength(value);
			}

			Document documentArgument = null;
			if (value != null){
				if (value instanceof List){
					argumentsLength+=getDocumentSize("/soap/argumentArray.xml");
					documentArgument = getDocument("/soap/argumentArray.xml");
					Element node = (Element)documentArgument.getElementsByTagName("LST").item(0);
					
					for (String v:(List<String>)value) {
						Element s = documentArgument.createElement("S");
						s.setTextContent(v);
						node.appendChild(s);
					}
				} else if (value instanceof String){
					argumentsLength+=getDocumentSize("/soap/argumentString.xml");
					documentArgument = getDocument("/soap/argumentString.xml");
					documentArgument.getElementsByTagName("S").item(0).setTextContent(value.toString());
				} else if (value instanceof Boolean) {
					argumentsLength+=getDocumentSize("/soap/argumentBoolean.xml");
					documentArgument = getDocument("/soap/argumentBoolean.xml");
					documentArgument.getElementsByTagName("S").item(0).setTextContent(key);
					documentArgument.getElementsByTagName("B").item(0).setTextContent(value.toString());
				} else {
					argumentsLength+=getDocumentSize("/soap/argumentInteger.xml");
					documentArgument = getDocument("/soap/argumentInteger.xml");
					documentArgument.getElementsByTagName("I32").item(0).setTextContent(value.toString());
				}
				
				NodeList list = documentArgument.getElementsByTagName("Obj");
				for (int i=0;i<list.getLength();i++) list.item(i).getAttributes().getNamedItem("RefId").setTextContent((++refId)+"");
			}
			
			if (!(value instanceof Boolean)) documentArguments.getElementsByTagName("LST").item(1).appendChild(documentArguments.importNode(documentArgumentName.getDocumentElement(), true));
			if (documentArgument != null) documentArguments.getElementsByTagName("LST").item(1).appendChild(documentArguments.importNode(documentArgument.getDocumentElement(), true));
		}
		
		nodes = documentArguments.getElementsByTagName("Obj");
		for (int i=0;i<nodes.getLength();i++){
			Node node = nodes.item(i);
			if (node.getAttributes().getNamedItem("N") != null && (node.getAttributes().getNamedItem("N").getTextContent().equals("ApartmentState") || 
					node.getAttributes().getNamedItem("N").getTextContent().equals("RemoteStreamOptions") || 
					node.getAttributes().getNamedItem("N").getTextContent().equals("HostInfo"))){
				node.getAttributes().getNamedItem("RefId").setTextContent((++refId)+"");
			}
		}
		
		document.getDocumentElement().getElementsByTagName("rsp:Arguments").item(0).setTextContent(getBase64String((getArgumentsHeader(document, commandName, argumentsLength)+getStringFromDocument(documentArguments)).getBytes("windows-1251")));
	}
	
	private static String getCodes() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        buffer.append("abcdefghijklmnopqrstuvwxyz");
        buffer.append("0123456789");
        buffer.append("+/");
        return buffer.toString();
    }
	
	private static int getUInt(byte b) {
        return b & 0xFF;
    }
	
	public static String getBase64String(byte[] input)  {
        StringBuffer buffer = new StringBuffer();
        String codes = getCodes();
        int padding = (3 - (input.length % 3)) % 3;
        byte[] paddedInput = new byte[input.length+padding];

        System.arraycopy(input, 0, paddedInput, 0, input.length);
        
        for (int i = 0; i < paddedInput.length; i += 3) {
            int j = (getUInt(paddedInput[i]) << 16) + (getUInt(paddedInput[i + 1]) << 8) + getUInt(paddedInput[i + 2]);

            buffer.append((char)codes.charAt((j >> 18) & 0x3f));
            buffer.append((char)codes.charAt((j >> 12) & 0x3f));
            buffer.append((char)codes.charAt((j >> 6) & 0x3f));
            buffer.append((char)codes.charAt(j & 0x3f));
        }
        
        for (int i=0; i< padding;i++) buffer.setCharAt(buffer.length() - (i+1), '=');
        return buffer.toString();
    }
	
	private String getArgumentsHeader(Document document, String commandName, int argumentsLenght) throws IOException{
		int size = ARGUMENTS_SIZE+commandName.length()+argumentsLenght;
		
		byte sizeArray[] = new byte[2];
		sizeArray[1] = (byte)size;
		size>>=8;
		sizeArray[0] = (byte)size;
		
		GUID guid = new GUID(UUID.randomUUID().toString());
		document.getDocumentElement().getElementsByTagName("rsp:CommandLine").item(0).getAttributes().getNamedItem("CommandId").setNodeValue(guid.getTrimGuid());

		byte first[] = new byte[]{0, 0, 0, 0, 0, 0, 0, 0x0B, 0, 0, 0, 0, 0, 0, 0, 0, 03, 0, 0};//19
		byte third[] = new byte[]{2, 0, 0, 0};//4
		byte fourth[] = new byte[]{6, 0x10, 02, 0};//4
		byte fifth[] = new byte[]{(byte)0xCE , (byte)0xAD , (byte)0x88 , (byte)0x13 , (byte)0x42 , (byte)0x1B , (byte)0x8A , (byte)0x4E , (byte)0xA6 , (byte)0x15 , (byte)0x78 , (byte)0xCC , (byte)0x4A , (byte)0x06 , (byte)0x26 , (byte)0x0E};//16
		byte sixth[] = getEncodedCommandId(guid.getBytes());//16
		byte seventh[] = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};//3
		
		byte header[] = new byte[64];
		System.arraycopy(first, 0, header, 0, 19);
		System.arraycopy(sizeArray, 0, header, 19, 2);
		System.arraycopy(third, 0, header, 21, 4);
		System.arraycopy(fourth, 0, header, 25, 4);
		System.arraycopy(fifth, 0, header, 29, 16);
		System.arraycopy(sixth, 0, header, 45, 16);
		System.arraycopy(seventh, 0, header, 61, 3);
		return new String(header, "windows-1251");
	}
	
	private byte[] getEncodedCommandId(byte initial[]){
		byte result[] = new byte[16];
		result[0] = initial[3];
		result[1] = initial[2];
		result[2] = initial[1];
		result[3] = initial[0];
		result[4] = initial[5];
		result[5] = initial[4];
		result[6] = initial[7];
		result[7] = initial[6];
		result[8] = initial[8];
		result[9] = initial[9];
		result[10] = initial[10];
		result[11] = initial[11];
		result[12] = initial[12];
		result[13] = initial[13];
		result[14] = initial[14];
		result[15] = initial[15];
		
		return result;
	}
	
	private int getDocumentSize(String documentName) throws Exception{
		InputStream input = new FileInputStream(new File(PATH+documentName));
		int res = input.available();
		input.close();
		return res;
	}
	
	private void auth(SOAPMessage msg){
       	msg.getMimeHeaders().addHeader("Authorization", "Basic "+Base64.decodeBase64(("oauth:"+accessToken).getBytes()));
	}
	
	public String command(String commandName, String shellId, Map<String, Object> arguments, String sessionId, String endpointUrl) throws Exception {
		sessionId = (sessionId == null) ? getRandomGuid() : sessionId;
		
		Document document = getDocument("/soap/command.xml");
		document.getDocumentElement().getElementsByTagName("w:Selector").item(0).setTextContent(shellId);
		document.getDocumentElement().getElementsByTagName("rsp:Command").item(0).setTextContent(commandName);
		document.getDocumentElement().getElementsByTagName("p:SessionId").item(0).setTextContent(sessionId);
		document.getDocumentElement().getElementsByTagName("p:OperationID").item(0).setTextContent(getRandomGuid());
		document.getDocumentElement().getElementsByTagName("a:MessageID").item(0).setTextContent(getRandomGuid());
		if (endpointUrl != null) {
			document.getDocumentElement().getElementsByTagName("a:To").item(0).setTextContent(endpointUrl);
			document.getDocumentElement().getElementsByTagName("w:ResourceURI").item(0).setTextContent("http://schemas.microsoft.com/powershell/Microsoft.PowerShell");
		} else {
			endpointUrl = OFFICE365_ENDPOINT;
		}
		
		changeArguments(document, commandName, arguments);
		
		SOAPMessage msg = MessageFactory.newInstance().createMessage();  
        SOAPPart soapPart = msg.getSOAPPart();  
        soapPart.setContent(new StreamSource(new ByteArrayInputStream(getStringFromDocument(document).getBytes())));
        msg.saveChanges();

        auth(msg);
	    msg.getMimeHeaders().setHeader("Content-Type", "application/soap+xml;charset=UTF-8");

		return processGetCommandId(OverloadHandler.getInstance().soapCall(SOAPConnectionFactory.newInstance().createConnection(), msg, endpointUrl, 0));
	}
	
	private String processGetCommandId(SOAPMessage response) throws SOAPException {
		String action = getAction(response);
		
		if (isError(action)) {
			throw new SOAPException(getFaultText(response));
		} else if (action.equalsIgnoreCase(COMMAND_RESPONSE)) {
			return response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_RSP, "CommandId").item(0).getTextContent();
		}
		
		return null;
	}
	
	private void processCommonResponse(SOAPMessage response) throws SOAPException {
		String action = getAction(response);
		
		if (isError(action)) throw new SOAPException(getFaultText(response));
	}
	
	public List<String> recieveCommand(String shellId, String commandId, String sessionId, String endpointUrl) throws Exception{
		sessionId = (sessionId == null) ? getRandomGuid() : sessionId;

		Document document = getDocument("/soap/recieveCommand.xml");
		document.getDocumentElement().getElementsByTagName("w:Selector").item(0).setTextContent(shellId);
		document.getDocumentElement().getElementsByTagName("rsp:DesiredStream").item(0).getAttributes().getNamedItem("CommandId").setNodeValue(commandId);
		document.getDocumentElement().getElementsByTagName("p:SessionId").item(0).setTextContent(sessionId);
		document.getDocumentElement().getElementsByTagName("p:OperationID").item(0).setTextContent(getRandomGuid());
		document.getDocumentElement().getElementsByTagName("a:MessageID").item(0).setTextContent(getRandomGuid());
		endpointUrl = OFFICE365_ENDPOINT;
		
		SOAPMessage msg = MessageFactory.newInstance().createMessage();  
        SOAPPart soapPart = msg.getSOAPPart();  
        soapPart.setContent(new StreamSource(new ByteArrayInputStream(getStringFromDocument(document).getBytes())));
        msg.saveChanges();

        auth(msg);
	    msg.getMimeHeaders().setHeader("Content-Type", "application/soap+xml;charset=UTF-8");

	    SOAPMessage response = OverloadHandler.getInstance().soapCall(SOAPConnectionFactory.newInstance().createConnection(), msg, endpointUrl, 0);
	    List<String> streams = processGetStreams(response);
	    if (!processIsReceiveEnd(response, false)) streams.addAll(recieveCommand(shellId, commandId, sessionId, endpointUrl));
	    return streams;
	}
	
	private List<String> processGetStreams(SOAPMessage response) throws SOAPException {
		List<String> streams = new ArrayList<String>();
		String action = getAction(response);
		
		if (isError(action)) {
			throw new SOAPException(getFaultText(response));
		} else if (action.equalsIgnoreCase(RECEIVE_RESPONSE)) {
			NodeList nodes = response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_RSP, "Stream");
			for (int i=0;i<nodes.getLength();i++){
				Node node = nodes.item(i);
				byte bytes[] = Base64.decodeBase64(node.getTextContent().getBytes());
				String decodedStream = null;
				
				try{
					int bom = getBOMPosition(node.getTextContent());
					if (bom != -1){
						decodedStream= new String(bytes, bom+3, bytes.length-bom-3, "UTF-8");
					} else {
						decodedStream = new String(bytes, PARSE_POS, bytes.length-PARSE_POS, "UTF-8");
					}
					
					if (decodedStream != null) streams.add(decodedStream.replaceAll("\u00A7", ""));
				}catch(Exception e){}
			}
		}
		
		return streams;
	}
	
	private int getBOMPosition(String response){
		byte bytes[] = Base64.decodeBase64(response.getBytes());
		for (int i=0;i<bytes.length;i++){
			byte b = bytes[i];
			String ef = Integer.toHexString(b).replaceAll("ffffff", "");
			if (ef.equalsIgnoreCase("EF")){
				String bb = Integer.toHexString(bytes[i+1]).replaceAll("ffffff", "");
				if (bb.equalsIgnoreCase("BB")){
					String bf = Integer.toHexString(bytes[i+2]).replaceAll("ffffff", "");
					if (bf.equalsIgnoreCase("BF")) return i;
				}
			}
		}
		
		return -1;
	}
	
	private boolean processIsReceiveEnd(SOAPMessage response, boolean isRemote) throws SOAPException {
		String action = getAction(response);
		
		if (isError(action)) {
			throw new SOAPException(getFaultText(response));
		} else if (action.equalsIgnoreCase(RECEIVE_RESPONSE)) {
			Node commandState = response.getSOAPPart().getEnvelope().getBody().getElementsByTagNameNS(NS_RSP, "CommandState").item(0);
			if (commandState != null){
				boolean state = false;
				Node stateNode = commandState.getAttributes().getNamedItem("State");
				if (stateNode != null) state = stateNode.getTextContent().equals("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done");
				String exitCode = commandState.getFirstChild().getTextContent();
				return state && exitCode.equals("0");
			}
		}
		
		return isRemote;
	}
}
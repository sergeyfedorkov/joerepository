package structure.objects;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import structure.SoapParser;
import structure.Statistics;
import structure.configuration.Configuration;
import structure.utils.Utils;

import com.microsoft.schemas.sharepoint.soap.GetListItems;
import com.microsoft.schemas.sharepoint.soap.UpdateListItems;

import connectors.ClaimsConnector;
import connectors.ListsConnector;

public class SharepointObject extends GenericObject {
	private static final long serialVersionUID = -6360731811101388454L;
	
	private static final String FOLDER_CONTENT_TYPE = "Folder";
	private static final String CONTENT_TYPE = "ows_ContentType";
	private static final String FILE_LEAF_REF = "ows_FileLeafRef";
	private static final String QUERY = "<Query/>";
	private static final int LIST_TEMPLATE = 101;
	private static final String LIST_DESCRIPTION = "generated files";
	private static final String BREAK_ROLE_URL = "%s/_api/web/GetFileByServerRelativeUrl('%s')/ListItemAllFields/breakroleinheritance(AllowUnsafeUpdates=true)";
	
	private String claims;
	private String site;
	private Map<String, String> metadata;
	
	public SharepointObject(String pathname, String target, long size, String siteUrl, Statistics statistics, Configuration configuration, String claims) {
		super(pathname, target, size, statistics, configuration);
		this.site=siteUrl;
		this.claims=claims;
	}
	
	public boolean createContent() throws IOException{
		return uploadFile(site+Utils.SEPARATOR+getTarget()+Utils.SEPARATOR+getPath(), claims) == null;
	}
	
	public boolean createContainer(){
		return SoapParser.getErrorText(ListsConnector.getService(site, claims).updateListItems(getTarget(), getUpdates()).getContent()) == null;
	}
	
	public boolean createTarget(){
		return SoapParser.getErrorText(ListsConnector.getService(site, claims).addList(getTarget(), LIST_DESCRIPTION, LIST_TEMPLATE).getContent()) == null;
	}
	
	public boolean removeTarget(){
		ListsConnector.getService(site, claims).deleteList(getTarget());
		return true;
	}
	
	public boolean checkTarget(){
		return SoapParser.getErrorText(ListsConnector.getService(site, claims).getList(getTarget()).getContent()) == null;
	}
	
	@Override
	public boolean retrieveChildren() throws Exception {
		parseChildren(ListsConnector.getService(site, claims).getListItems(getTarget(), null, getQuery(), getViewFields(), "2000", getQueryOptions(), null).getContent());
		return true;
	}
	
	public String getClaims() {
		return claims;
	}

	public String getSiteUrl() {
		return site;
	}
	
	public boolean isDirectory(){
		return metadata == null || metadata.get(CONTENT_TYPE).equalsIgnoreCase(FOLDER_CONTENT_TYPE);
	}
	
	/*
	 * Private Section
	 */
	private void parseChildren(List<Object> objects){
		for (Object object:objects){
			if (object instanceof Element) {
				Element e = (Element) object;
				NodeList nodes = e.getElementsByTagName("z:row");
				for (int i = 0; i < nodes.getLength(); i++) {
					NamedNodeMap attributes = nodes.item(i).getAttributes();
					
					Map<String, String> metadata = new HashMap<String, String>();
					for (int j = 0; j < attributes.getLength(); j++) {
						Node a = attributes.item(j);
						metadata.put(a.getNodeName(), a.getNodeValue());
					}
					
					String ows_FileLeafRef = metadata.get(FILE_LEAF_REF).split(";#")[1];
					SharepointObject sharepointObject = new SharepointObject(getPath()+Utils.SEPARATOR+ows_FileLeafRef, getTarget(), getSize(), getSiteUrl(), getStatistics(), getConfiguration(), claims);
					sharepointObject.setMetadata(metadata);
					getChildren().add(sharepointObject);
				}
			}
		}
	}
	
	private GetListItems.Query getQuery() throws Exception{
		GetListItems.Query query = new GetListItems.Query();
		
		Document queryDocument = SoapParser.createNewXMLDocument().parse(new InputSource(new StringReader(QUERY)));
		
		query.getContent().add(queryDocument.getDocumentElement());
		return query;
	}
	
	private GetListItems.QueryOptions getQueryOptions(){
		Document document = SoapParser.createNewXMLDocument().newDocument();
		
		Map<String, Object> queryOptionsMap = new HashMap<String, Object>();
		if (!getPath().equalsIgnoreCase(getTarget())) queryOptionsMap.put("Folder", site+Utils.SEPARATOR+getTarget()+Utils.SEPARATOR+Utils.apply(getPath()));
		
		GetListItems.QueryOptions queryOptions = new GetListItems.QueryOptions();
		
		if (queryOptionsMap!=null && !queryOptionsMap.isEmpty()) {
			Element omeOptions = document.createElement("QueryOptions");

			for(String key : queryOptionsMap.keySet()) {
				Element ome = document.createElement(key);
				Object v = queryOptionsMap.get(key);
				
				if (v!=null) {
					ome.setTextContent((String)v);
					omeOptions.appendChild(ome);
				}
			}
			
			queryOptions.getContent().add(omeOptions);
		}
		
		return queryOptions;
	}
	
	private GetListItems.ViewFields getViewFields(){
		Document document = SoapParser.createNewXMLDocument().newDocument();
		
		GetListItems.ViewFields viewFields = new GetListItems.ViewFields();
		
		Element omeFields = document.createElement("ViewFields");

		viewFields.getContent().add(omeFields);
		return viewFields;
	}
	
	private String uploadFile(String url, String claims){
		OutputStream out = null;

		try {
			URLConnection conn = new URL(url.replaceAll(" ", "%20")).openConnection();
			((HttpURLConnection)conn).setRequestMethod("PUT");
		    conn.setDoOutput(true);
			conn.setRequestProperty(ClaimsConnector.HTTP_HEADER_COOKIE, claims);

		    out = conn.getOutputStream();
		    out.write(getBytes());

			if (conn.getHeaderField(ClaimsConnector.SPAPI_ITEM_UNIQUE_ID) == null) return "Failure to upload document";
		} catch(Exception e){
			return e.getMessage();
		} finally {
			try{
				if (out!=null) out.close();
			}catch(Exception e1){	}
		}
		
		//breakRoleInheritance(claims);
		return null;
	}
	
	private String breakRoleInheritance(String claims){
		OutputStream out = null;

		try {
			String url = String.format(BREAK_ROLE_URL, site, "/personal/joe_metavistech_com/"+getTarget()+"/"+getPath());
			URLConnection conn = new URL(url.replaceAll(" ", "%20")).openConnection();
			((HttpURLConnection)conn).setRequestMethod("POST");
			conn.setRequestProperty(ClaimsConnector.HTTP_HEADER_COOKIE, claims);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("X-RequestDigest", getDigestValue(claims));
			conn.setDoOutput(true);
			
			out = conn.getOutputStream();
		    out.write(new byte[]{});

			((HttpURLConnection)conn).getInputStream();
		} catch(Exception e){
			return e.getMessage();
		} finally {
			try{
				if (out!=null) out.close();
			}catch(Exception e1){	}
		}
		
		return null;
	}
	
	private String getDigestValue(String claims){
		OutputStream out = null;

		try {
			String url = "https://metavistech-my.sharepoint.com/_api/contextinfo";
			URLConnection conn = new URL(url.replaceAll(" ", "%20")).openConnection();
			((HttpURLConnection)conn).setRequestMethod("POST");
			conn.setRequestProperty(ClaimsConnector.HTTP_HEADER_COOKIE, claims);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("accept", "application/json;odata=verbose");
			conn.setRequestProperty("content-type", "application/json;odata=verbose");
			conn.setDoOutput(true);
			
			out = conn.getOutputStream();
		    out.write(new byte[]{});

			InputStream stream = ((HttpURLConnection)conn).getInputStream();
			String result = Utils.getResultFromStream(stream, 0);
			JSONObject json = new JSONObject(result);
			JSONObject d = json.getJSONObject("d");
			JSONObject webInformation = d.getJSONObject("GetContextWebInformation");
			return webInformation.getString("FormDigestValue");
		} catch(Exception e){
			throw new RuntimeException(e);
		} finally {
			try{
				if (out!=null) out.close();
			}catch(Exception e1){	}
		}
	}
	
	private UpdateListItems.Updates getUpdates(){
		List<Map<String, Object>> values = getValues(getPath());
		Document document = SoapParser.createNewXMLDocument().newDocument();
				
		Element omBatch = document.createElement("Batch");
		omBatch.setAttribute("OnError", "Continue");
		omBatch.setAttribute("ListVersion", "1");

		for(int i=0;i<values.size();i++) {
			Map<String, Object> value = values.get(i);
			Element omMethod = document.createElement("Method");
			omMethod.setAttribute("ID", "" + i);
			omMethod.setAttribute("Cmd", "New");
			
			for(String key:value.keySet()) {
				Element omField = document.createElement("Field");
				omField.setAttribute("Name", key);
				omField.setTextContent(value.get(key).toString());
				omMethod.appendChild(omField);
			}

			omBatch.appendChild(omMethod);
		}
		
		UpdateListItems.Updates updates = new UpdateListItems.Updates();
		updates.getContent().add(omBatch);
		return updates;
	}

	private List<Map<String, Object>> getValues(String path){
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		values.add(map);

		map.put("BaseName", Utils.apply(path));
		map.put("Title", getName());
		map.put("ContentType", "Folder");
		map.put("FSObjType", "1");
		map.put("ID", "New");
		return values;
	}

	private void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
}
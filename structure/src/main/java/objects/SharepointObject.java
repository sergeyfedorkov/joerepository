package objects;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import structure.Configuration;
import structure.SoapParser;
import structure.Statistics;
import utils.Utils;

import com.microsoft.schemas.sharepoint.soap.UpdateListItems;

import connectors.ClaimsConnector;
import connectors.ListsConnector;

public class SharepointObject extends GenericObject {
	private static final long serialVersionUID = -6360731811101388454L;
	
	private static final int LIST_TEMPLATE = 101;
	private static final String LIST_DESCRIPTION = "generated files";
	
	private String claims;
	private String site;
	
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
	
	public String getClaims() {
		return claims;
	}

	public String getSiteUrl() {
		return site;
	}
	
	/*
	 * Private Section
	 */
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
		
		return null;
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
}
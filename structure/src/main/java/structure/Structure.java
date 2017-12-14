package structure;

import java.net.ProxySelector;

import objects.GenericObject;
import utils.DebugProxySelector;
import utils.Utils;
import configuration.Configuration;
import connectors.ClaimsConnector;
import connectors.GoogleConnector;
import connectors.api.DropboxConnectorApi;
import connectors.oauth.BoxConnector;
import connectors.oauth.ExchangeConnector;
import connectors.oauth.OAuthConnector;

public class Structure {
	private static Configuration configuration = Configuration.getInstance();
	
	public static void main(String[] args) {
		ProxySelector.setDefault(new DebugProxySelector());
		if (configuration.validate()){
			if (configuration.isSharepoint()){
				sharepoint();
			} else if (configuration.isDropbox()){
				dropbox();
			} else if (configuration.isBox()){
				box();
			} else if (configuration.isGoogle()) {
				google();
			} else if (configuration.isExchange()) {
				exchange();
			} else {
				filesystem();
			}
		}
	}
	
	public static void exchange(){
		Utils.print("----------Exchange");
		OAuthConnector connector = new ExchangeConnector().connect(configuration.getExchangeuser());
		if (connector.getAccessToken() == null) return;
	}
	
	public static void filesystem(){
		Utils.print("----------FileSystem");
		start(new FileBuilder().path(configuration.getTarget()).target(configuration.getTarget()).size(configuration.getSize()).configuration(configuration).build());
	}
	
	public static void dropbox(){
		Utils.print("----------Dropbox");
		//OAuthConnector connector = new DropboxConnector().connect(configuration.getDropboxuser());
		DropboxConnectorApi connector = new DropboxConnectorApi().connect(configuration.getDropboxuser());
		//if (connector.getAccessToken() == null) return;
		if (connector.getToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getToken()).user(configuration.getDropboxuser()).configuration(configuration).build());
	}
	
	public static void google(){
		Utils.print("----------Google");
		GoogleConnector connector = new GoogleConnector().connect(configuration.getGoogleuser());
		if (connector.getCredential() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).credentials(connector.getCredential()).configuration(configuration).build());
	}
	
	public static void sharepoint(){
		Utils.print("----------Sharepoint");
		String claims = new ClaimsConnector().claims(configuration.getSite());
		if (claims == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).site(configuration.getSite()).claims(claims).configuration(configuration).build());
	}
	
	public static void box(){
		Utils.print("----------Box");
		OAuthConnector connector = new BoxConnector().connect(configuration.getBoxuser());
		if (connector.getAccessToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).api(((BoxConnector)connector).getBoxApi()).configuration(configuration).build());
	}
	
	public static void start(GenericObject target){
		if (configuration.isRead()){
			Utils.print("----------Read\n\n");
			if (!target.exist()) return;
			
			Utils.breakline();
			read(target);
		} else {
			Utils.print("----------Create\n\n");
			if (configuration.isDeleteTarget() && target.exist()) target.remove();
			if (!target.exist()) target.target();
			
			Utils.breakline();
			create(target, 0);
		}
		
		Statistics.getInstance().print();
	}
	
	private static void read(GenericObject parent){
		for (GenericObject child:parent.children()) {
			child.setStatistics(Statistics.getInstance());
			read(child);
		}
	}
	
	private static void create(GenericObject parent, int level){
		if (Statistics.getInstance().end(configuration)) return;
		
		if (level != configuration.getDeep()) {
			for (int index=0;index<configuration.getFolders();index++){
				if (Statistics.getInstance().end(configuration)) return;
				
				GenericObject folder = new FileBuilder().parent(parent).path(configuration.getContainerName(index, level)).statistics(Statistics.getInstance()).build();
				if (folder.container()) create(folder, level+1);
			}
		}
		
		content(parent, level);
	}
	
	private static void content(GenericObject parent, int level){
		if (Statistics.getInstance().end(configuration)) return;
		
		for (int index=0;index<configuration.getDocuments();index++){
			if (Statistics.getInstance().end(configuration)) return;
			new FileBuilder().parent(parent).path(configuration.getContentName(index, level)).statistics(Statistics.getInstance()).build().content();
		}
	}
}
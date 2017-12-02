package structure;

import java.net.ProxySelector;

import objects.GenericObject;
import utils.DebugProxySelector;
import connectors.ClaimsConnector;
import connectors.GoogleConnector;
import connectors.oauth.BoxConnector;
import connectors.oauth.DropboxConnector;
import connectors.oauth.ExchangeConnector;
import connectors.oauth.OAuthConnector;

public class Structure {
	private static Statistics statistics = Statistics.getInstance();
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
		OAuthConnector connector = new ExchangeConnector().connect(configuration.getExchangeuser());
		if (connector.getAccessToken() == null) return;
		//start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getToken()).user(configuration.getDropboxuser()).build());
	}
	
	public static void filesystem(){
		start(new FileBuilder().path(configuration.getTarget()).target(configuration.getTarget()).size(configuration.getSize()).build());
	}
	
	public static void dropbox(){
		OAuthConnector connector = new DropboxConnector().connect(configuration.getDropboxuser());
		if (connector.getAccessToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getAccessToken()).user(configuration.getDropboxuser()).build());
	}
	
	public static void google(){
		GoogleConnector connector = new GoogleConnector().connect(configuration.getGoogleuser());
		if (connector.getCredential() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).credentials(connector.getCredential()).build());
	}
	
	public static void sharepoint(){
		String claims = new ClaimsConnector().claims(configuration.getSite());
		if (claims == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).site(configuration.getSite()).claims(claims).build());
	}
	
	public static void box(){
		OAuthConnector connector = new BoxConnector().connect(configuration.getBoxuser());
		if (connector.getAccessToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).api(((BoxConnector)connector).getBoxApi()).build());
	}
	
	public static void start(GenericObject target){
		if (configuration.isDeleteTarget() && target.exist()) target.remove();
		if (!target.exist()) target.target();
		
		structure(target, 0);
		statistics.print();
	}
	
	private static void structure(GenericObject parent, int level){
		if (statistics.end(configuration)) return;
		
		if (level != configuration.getDeep()) {
			for (int index=0;index<configuration.getFolders();index++){
				if (statistics.end(configuration)) return;
				
				GenericObject folder = new FileBuilder().parent(parent).path(configuration.getContainerName(index, level)).statistics(statistics).configuration(configuration).build();
				if (folder.container()) structure(folder, level+1);
			}
		}
		
		content(parent, level);
	}
	
	private static void content(GenericObject parent, int level){
		if (statistics.end(configuration)) return;
		
		for (int index=0;index<configuration.getDocuments();index++){
			if (statistics.end(configuration)) return;
			new FileBuilder().parent(parent).path(configuration.getContentName(index, level)).statistics(statistics).configuration(configuration).build().content();
		}
	}
}
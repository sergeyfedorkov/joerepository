package structure;

import java.net.ProxySelector;

import objects.GenericObject;
import utils.DebugProxySelector;
import connectors.BoxConnector;
import connectors.ClaimsConnector;
import connectors.DropboxConnector;
import connectors.ExchangeConnector;
import connectors.GoogleConnector;

public class Structure {
	private static Statistics statistics;
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
		ExchangeConnector connector = new ExchangeConnector().connect(configuration.getExchangeuser());
		System.out.println(connector.getAccessToken());
		if (connector.getAccessToken() == null) return;
		//start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getToken()).user(configuration.getDropboxuser()).build());
	}
	
	public static void filesystem(){
		start(new FileBuilder().path(configuration.getTarget()).target(configuration.getTarget()).size(configuration.getSize()).build());
	}
	
	public static void dropbox(){
		DropboxConnector connector = new DropboxConnector().connect(configuration.getBoxuser());
		if (connector.getToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getToken()).user(configuration.getDropboxuser()).build());
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
		BoxConnector connector = new BoxConnector().connect(configuration.getBoxuser());
		if (connector.getBoxApi() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).api(connector.getBoxApi()).build());
	}
	
	public static void start(GenericObject target){
		if (configuration.isDeleteTarget() && target.exist()) target.remove();
		if (!target.exist()) target.target();
		
		statistics = Statistics.getInstance();
		structure(target, 0);
		statistics.print();
	}
	
	private static void structure(GenericObject parent, int level){
		if (level != configuration.getDeep()) {
			for (int index=0;index<configuration.getFolders();index++){
				GenericObject folder = new FileBuilder().parent(parent).path(configuration.getContainerName(index, level)).statistics(statistics).build();
				if (folder.container()) structure(folder, level+1);
			}
		}
		
		content(parent, level);
	}
	
	private static void content(GenericObject parent, int level){
		for (int index=0;index<configuration.getDocuments();index++){
			new FileBuilder().parent(parent).path(configuration.getContentName(index, level)).statistics(statistics).build().content();
		}
	}
}
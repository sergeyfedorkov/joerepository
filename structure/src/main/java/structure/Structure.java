package structure;

import java.net.ProxySelector;

import structure.configuration.Configuration;
import structure.objects.GenericObject;
import structure.ui.ViewLogger;
import utils.DebugProxySelector;
import utils.Utils;
import connectors.ClaimsConnector;
import connectors.GoogleConnector;
import connectors.api.DropboxConnectorApi;
import connectors.oauth.BoxConnector;
import connectors.oauth.ExchangeConnector;
import connectors.oauth.OAuthConnector;

public class Structure {
	private Configuration configuration = Configuration.open();
	private static Structure instance;
	
	public static void main(String[] args) {
		Structure.getInstance().run();
	}
	
	public static synchronized Structure getInstance(){
		if (instance == null) instance = new Structure();
		return instance;
	}
	
	private void run(){
		if (configuration == null) return;
		if (configuration.isProxy()) ProxySelector.setDefault(new DebugProxySelector());
		
		configuration.clear();
		if (configuration.isSharepoint()){
			sharepoint();
		} else if (configuration.isDropbox()){
			dropbox();
		} else if (configuration.isBox()){
			box();
		} else if (configuration.isGoogle()){
			google();
		} else if (configuration.isExchange()){
			exchange();
		} else if (configuration.isFileSystem()){
			filesystem();
		}
	}
	
	public void exchange(){
		OAuthConnector connector = new ExchangeConnector().connect(configuration.getExchangeuser());
		if (connector.getAccessToken() == null) return;
	}
	
	public void filesystem(){
		start(new FileBuilder().path(configuration.getTarget()).target(configuration.getTarget()).size(configuration.getSize()).configuration(configuration).build());
	}
	
	public void dropbox(){
		//OAuthConnector connector = new DropboxConnector().connect(configuration.getDropboxuser());
		DropboxConnectorApi connector = new DropboxConnectorApi().connect(configuration.getDropboxuser());
		//if (connector.getAccessToken() == null) return;
		if (connector.getToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).token(connector.getToken()).user(configuration.getDropboxuser()).configuration(configuration).build());
	}
	
	public void google(){
		GoogleConnector connector = new GoogleConnector().connect(configuration.getGoogleuser());
		if (connector.getCredential() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).credentials(connector.getCredential()).configuration(configuration).build());
	}
	
	public void sharepoint(){
		String claims = new ClaimsConnector().claims(configuration.getSite());
		if (claims == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).site(configuration.getSite()).claims(claims).configuration(configuration).build());
	}
	
	public void box(){
		OAuthConnector connector = new BoxConnector().connect(configuration.getBoxuser());
		if (connector.getAccessToken() == null) return;
		start(new FileBuilder().target(configuration.getTarget()).size(configuration.getSize()).api(((BoxConnector)connector).getBoxApi()).configuration(configuration).build());
	}
	
	public void call(GenericObject target){
		Utils.print(configuration.getTitle()+"\n");
		Utils.breakline();
		
		if (configuration.isRead()){
			if (!target.exist()) return;
			
			Utils.breakline();
			read(target);
		} else {
			if (configuration.isDeleteTarget() && target.exist()) target.remove();
			if (!target.exist()) target.target();
			
			Utils.breakline();
			create(target, 0);
		}
		
		Statistics.getInstance().print();
	}
	
	public void start(GenericObject target){
		if (configuration.isView()) {
			ViewLogger.create(this, target);
		} else {
			call(target);
		}
	}
	
	private void read(GenericObject parent){
		for (GenericObject child:parent.children()) {
			child.setStatistics(Statistics.getInstance());
			read(child);
		}
	}
	
	private void create(GenericObject parent, int level){
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
	
	private void content(GenericObject parent, int level){
		if (Statistics.getInstance().end(configuration)) return;
		
		for (int index=0;index<configuration.getDocuments();index++){
			if (Statistics.getInstance().end(configuration)) return;
			new FileBuilder().parent(parent).path(configuration.getContentName(index, level)).statistics(Statistics.getInstance()).build().content();
		}
	}

	public Configuration getConfiguration() {
		return configuration;
	}
}
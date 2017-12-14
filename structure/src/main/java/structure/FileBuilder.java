package structure;

import objects.BoxObject;
import objects.DropboxObject;
import objects.FileSystemObject;
import objects.GenericObject;
import objects.GoogleObject;
import objects.SharepointObject;
import utils.Utils;

import com.box.sdk.BoxAPIConnection;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import configuration.Configuration;

public class FileBuilder {
	//common
	private String path = "";
	private String target;
	private long size;
	private Statistics statistics;
	private Configuration configuration;
	private Object parent;
	
	//sharepoint
	private String site;
	private String claims;
	
	//dropbox
	private String token;
	private String user;
	
	//box
	private BoxAPIConnection api;
	
	//google
	private GoogleCredential credentials;
	
	public GenericObject build(){
		if (claims != null){
			return new SharepointObject(path, target, size, site, statistics, configuration, claims);
		} else if (token != null) {
			return new DropboxObject(path, target, size, statistics, configuration, user, token);
		} else if (api != null) {
			return new BoxObject(path, target, size, statistics, configuration, api, parent);
		} else if (credentials != null){
			return new GoogleObject(path, target, size, statistics, configuration, credentials, parent);
		} else {
			return new FileSystemObject(path, target, size, statistics, configuration);
		}
	}
	
	public FileBuilder parent(GenericObject parent){
		if (parent instanceof SharepointObject){
			SharepointObject object = (SharepointObject)parent;
			site(object.getSiteUrl());
			claims(object.getClaims());
		} else if (parent instanceof DropboxObject) {
			DropboxObject object = (DropboxObject)parent;
			token(object.getToken());
			user(object.getUser());
		} else if (parent instanceof BoxObject){
			BoxObject object = (BoxObject)parent;
			api(object.getApi());
			parent(object.getFolder());
		} else if (parent instanceof GoogleObject){
			GoogleObject object = (GoogleObject)parent;
			credentials(object.getCredential());
			parent(object.getFolder());
		}
		
		configuration(parent.getConfiguration());
		target(parent.getTarget());
		size(parent.getSize());
		path(parent.getPath());
		return this;
	}

	public FileBuilder path(String path) {
		this.path += path+Utils.SEPARATOR;
		return this;
	}

	public FileBuilder target(String target) {
		this.target = target;
		return this;
	}

	public FileBuilder site(String site) {
		this.site = site;
		return this;
	}

	public FileBuilder claims(String claims) {
		this.claims = claims;
		return this;
	}
	
	public FileBuilder token(String token) {
		this.token = token;
		return this;
	}
	
	public FileBuilder user(String user) {
		this.user = user;
		return this;
	}
	
	public FileBuilder api(BoxAPIConnection api) {
		this.api = api;
		return this;
	}
	
	public FileBuilder credentials(GoogleCredential credentials){
		this.credentials=credentials;
		return this;
	}
	
	public FileBuilder parent(Object parent){
		this.parent=parent;
		return this;
	}

	public FileBuilder size(long size) {
		this.size = size;
		return this;
	}
	
	public FileBuilder statistics(Statistics statistics){
		this.statistics=statistics;
		return this;
	}
	
	public FileBuilder configuration(Configuration configuration){
		this.configuration=configuration;
		return this;
	}
}
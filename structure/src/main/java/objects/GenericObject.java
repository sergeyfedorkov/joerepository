package objects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import structure.Statistics;
import structure.configuration.Configuration;
import utils.Utils;

public abstract class GenericObject extends File {
	private static final long serialVersionUID = -6978073793316037363L;
	
	private Statistics statistics;
	private Configuration configuration;
	private long size;
	private String target;
	private List<GenericObject> children = new ArrayList<GenericObject>();

	public GenericObject(String pathname, String target, long size, Statistics statistics, Configuration configuration) {
		super(pathname);
		this.target=target;
		this.size=size;
		this.statistics=statistics;
		this.configuration=configuration;
	}
	
	public List<GenericObject> getChildren() {
		return children;
	}

	public long getSize() {
		return size;
	}

	public String getTarget() {
		return target;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void setStatistics(Statistics statistics){
		this.statistics=statistics;
	}
	
	public byte[] getBytes(){
		byte content[] = new byte[(int)getSize()];
		new Random().nextBytes(content);
		return content;
	}
	
	public abstract boolean createContainer() throws Exception;
	public abstract boolean createContent() throws Exception;
	public abstract boolean createTarget() throws Exception;
	public abstract boolean checkTarget() throws Exception;
	public abstract boolean removeTarget() throws Exception;
	public abstract boolean retrieveChildren() throws Exception;
	
	public boolean remove(){
		if (configuration.isPrint()) Utils.print(getPath(), getTarget(), " - remove - ");
		
		return (Boolean)new OverloadHandler(statistics, configuration.isPrint()){
			public Object method() throws Exception{
				return removeTarget();
			}
		}.run(true);
	}
	
	public boolean exist(){
		if (configuration.isPrint()) Utils.print(getPath(), getTarget(), " - check - ");
		
		return (Boolean)new OverloadHandler(statistics, configuration.isPrint()){
			public Object method() throws Exception{
				return checkTarget();
			}
		}.run(true);
	}
	
	public boolean target(){
		if (configuration.isPrint()) Utils.print(getPath(), getTarget(), " - create - ");
		
		return (Boolean)new OverloadHandler(statistics, configuration.isPrint()){
			public Object method() throws Exception{
				return createTarget();
			}
		}.run(true);
	}
	
	public boolean content(){
		if (configuration.isPrint()) Utils.print(getPath(), getTarget(), " - ");
		
		return (Boolean)new OverloadHandler(statistics, configuration.isPrint()){
			public Object method() throws Exception{
				return createContent();
			}
		}.run(false);
	}
	
	public boolean container(){
		if (configuration.isPrint()) Utils.print(getPath(), getTarget(), " - ");
		
		return (Boolean)new OverloadHandler(statistics, configuration.isPrint()){
			public Object method() throws Exception{
				return createContainer();
			}
		}.run(true);
	}
	
	public List<GenericObject> children(){
		boolean print = !getName().isEmpty() && !getName().equalsIgnoreCase(getTarget()) && !Utils.apply(getPath()).equalsIgnoreCase(getTarget()) && configuration.isPrint();
		if (print) Utils.print(getPath(), getTarget(), " - ");
		if (print && isDirectory()) Utils.print("children - ");
		
		new OverloadHandler(statistics, print){
			public Object method() throws Exception{
				return isDirectory()?retrieveChildren():null;
			}
		}.run(isDirectory());
		
		return getChildren();
	}
}
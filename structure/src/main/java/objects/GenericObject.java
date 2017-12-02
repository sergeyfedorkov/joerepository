package objects;

import java.io.File;
import java.util.Date;
import java.util.Random;

import javax.xml.ws.soap.SOAPFaultException;

import structure.Configuration;
import structure.SoapParser;
import structure.Statistics;
import utils.Utils;

public abstract class GenericObject extends File {
	private static final long serialVersionUID = -6978073793316037363L;
	
	private Statistics statistics;
	private Configuration configuration;
	private long size;
	private String target;

	public GenericObject(String pathname, String target, long size, Statistics statistics, Configuration configuration) {
		super(pathname);
		this.target=target;
		this.size=size;
		this.statistics=statistics;
		this.configuration=configuration;
	}

	public long getSize() {
		return size;
	}

	public String getTarget() {
		return target;
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
	
	public boolean remove(){
		String error = null;
		Date date = new Date();
		
		try{
			return removeTarget();
		} catch(SOAPFaultException e){
			error = SoapParser.getErrorText(e.getFault());
			return false;
		} catch (Exception e){
			error = e.getMessage();
			return false;
		} finally {
			String time = getTimeContainer(date);
			if (configuration == null || configuration.isPrint()) Utils.print(getPath(), getTarget(), " - remove - "+time+(error != null?(" - "+error):""));
		}
	}
	
	public boolean exist(){
		String error = null;
		Date date = new Date();
		
		try{
			return checkTarget();
		} catch(SOAPFaultException e){
			error = SoapParser.getErrorText(e.getFault());
			return false;
		} catch (Exception e){
			error = e.getMessage();
			return false;
		} finally {
			String time = getTimeContainer(date);
			if (configuration == null || configuration.isPrint()) Utils.print(getPath(), getTarget(), " - check - "+time+(error != null?(" - "+error):""));
		}
	}
	
	public boolean target(){
		String error = null;
		Date date = new Date();
		
		try{
			return createTarget();
		} catch(SOAPFaultException e){
			error = SoapParser.getErrorText(e.getFault());
			return false;
		} catch (Exception e){
			error = e.getMessage();
			return false;
		} finally {
			String time = getTimeContainer(date);
			if (configuration == null || configuration.isPrint()) Utils.print(getPath(), getTarget(), " - create - "+time+(error != null?(" - "+error):"")+"\n");
		}
	}
	
	public boolean content(){
		String error = null;
		Date date = new Date();
		
		try{
			return createContent();
		} catch(SOAPFaultException e){
			error = SoapParser.getErrorText(e.getFault());
			return false;
		} catch (Exception e){
			error = e.getMessage();
			return false;
		} finally {
			String time = getTimeContent(date);
			if (configuration == null || configuration.isPrint()) Utils.print(getPath(), getTarget(), " - "+time+(error != null?(" - "+error):""));
		}
	}
	
	public boolean container(){
		String error = null;
		Date date = new Date();
		
		try{
			return createContainer();
		} catch(SOAPFaultException e){
			error = SoapParser.getErrorText(e.getFault());
			return false;
		} catch (Exception e){
			error = e.getMessage();
			return false;
		} finally {
			String time = getTimeContainer(date);
			if (configuration == null || configuration.isPrint()) Utils.print(getPath(), getTarget(), " - "+time+(error != null?(" - "+error):""));
		}
	}
	
	private String getTimeContainer(Date date){
		return Statistics.getTimeElapsed(statistics == null?new Date().getTime()-date.getTime():statistics.addContainer(date));
	}
	
	private String getTimeContent(Date date){
		return Statistics.getTimeElapsed(statistics == null?new Date().getTime()-date.getTime():statistics.addContent(date));
	}
}
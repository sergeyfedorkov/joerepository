package objects;

import java.io.File;
import java.util.Date;
import java.util.Random;

import javax.xml.ws.soap.SOAPFaultException;

import structure.SoapParser;
import structure.Statistics;
import utils.Utils;

public abstract class GenericObject extends File {
	private static final long serialVersionUID = -6978073793316037363L;
	
	private Statistics statistics;
	private int size;
	private String target;

	public GenericObject(String pathname, String target, int size, Statistics statistics) {
		super(pathname);
		setTarget(target);
		setSize(size);
		setStatistics(statistics);
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public byte[] getBytes(){
		byte content[] = new byte[getSize()];
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
			Utils.print(getPath(), getTarget(), " - remove - "+getTimeContainer(date)+(error != null?(" - "+error):""));
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
			Utils.print(getPath(), getTarget(), " - check - "+getTimeContainer(date)+(error != null?(" - "+error):""));
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
			Utils.print(getPath(), getTarget(), " - create - "+getTimeContainer(date)+(error != null?(" - "+error):"")+"\n");
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
			Utils.print(getPath(), getTarget(), " - "+getTimeContent(date)+(error != null?(" - "+error):""));
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
			Utils.print(getPath(), getTarget(), " - "+getTimeContainer(date)+(error != null?(" - "+error):""));
		}
	}
	
	private String getTimeContainer(Date date){
		return Statistics.getTimeElapsed(getStatistics() == null?new Date().getTime()-date.getTime():getStatistics().addContainer(date));
	}
	
	private String getTimeContent(Date date){
		return Statistics.getTimeElapsed(getStatistics() == null?new Date().getTime()-date.getTime():getStatistics().addContent(date));
	}
}
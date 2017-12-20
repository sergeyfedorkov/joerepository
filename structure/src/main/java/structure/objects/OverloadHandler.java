package structure.objects;

import java.util.Date;

import javax.xml.ws.soap.SOAPFaultException;

import structure.SoapParser;
import structure.Statistics;
import utils.Utils;

public abstract class OverloadHandler{
	private static final String TOO_MANY_REQUESTS = "429";
	private static final int DELAYES[] = new int[]{2, 2, 10, 10, 15, 15};
	private Statistics statistics;
	private boolean print;
	
	public OverloadHandler(Statistics statistics, boolean print){
		this.statistics=statistics;
		this.print=print;
	}
	
	public Object run(boolean isContainer){
		Date date = new Date();
	
		try{
			return start(0);
		} finally {
			String time = isContainer?getTimeContainer(date):getTimeContent(date);
			if (print) {
				Utils.print(time);
				Utils.breakline();
			}
		}
	}
	
	private Object start(int index){
		try{
			return method();
		} catch(SOAPFaultException e){
			String error = SoapParser.getErrorText(e.getFault()).trim();
			if (print) Utils.print((error != null?error+" - ":""));
			return false;
		} catch (Exception e){
			if (e.getMessage().indexOf(TOO_MANY_REQUESTS) != -1 && index<DELAYES.length){
				if (print) Utils.print("(429) - ");
				
				sleep(DELAYES[index]);
				
				if (print) Utils.print(DELAYES[index]+" second(s) - ");
				return start(index+1);
			} else {
				if (print) Utils.print(e.getMessage()+" - ");
			}
			
			return false;
		}
	}
	
	public abstract Object method() throws Exception;
	
	/*
	 * Private Section
	 */
	
	private void sleep(int delay){
		try {
			Thread.sleep(delay*1000);
		} catch (InterruptedException e1) {}
	}
	
	private String getTimeContainer(Date date){
		return Statistics.getTimeElapsed(statistics == null?new Date().getTime()-date.getTime():statistics.addContainer(date));
	}
	
	private String getTimeContent(Date date){
		return Statistics.getTimeElapsed(statistics == null?new Date().getTime()-date.getTime():statistics.addContent(date));
	}
}
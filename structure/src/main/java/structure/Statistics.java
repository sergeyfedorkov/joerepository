package structure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import structure.configuration.Configuration;
import structure.utils.Utils;

public class Statistics {
	private static Statistics instance = null;
	
	private List<Long> folders = new ArrayList<Long>();
	private List<Long> documents = new ArrayList<Long>();
	private Date startDate;
		
	private Statistics(){
		startDate = new Date();
	}
	
	public static synchronized Statistics getInstance(){
		if (instance == null) instance = new Statistics();
		return instance;
	}
	
	public void print(){
		Utils.print("\nStatistics:\n");
		
		if (folders.size() != 0){
			long foldersAvarage = (long)folders.stream().mapToLong(p -> p.longValue()).average().getAsDouble();
			Utils.print("Number of Folders: "+folders.size()+". Average time: "+getTimeElapsed(foldersAvarage));
			Utils.breakline();
		}
		
		if (documents.size() != 0) {
			long documentsAvarage = (long)documents.stream().mapToLong(p -> p.longValue()).average().getAsDouble();;
			Utils.print("Number of Documents: "+documents.size()+". Average time: "+getTimeElapsed(documentsAvarage));
			Utils.breakline();
		}
		
		Utils.print("Total time consumed: "+getTimeElapsed(new Date().getTime()-startDate.getTime()));
		Utils.breakline();
	}
	
	public long currentProcess(){
		long result = 0;
		if (folders.size() != 0) result+=folders.size();
		if (documents.size() != 0) result+=documents.size();
		return result; 
	}
	
	public static String getTimeElapsed(long elapsed){
        long milliseconds = elapsed % 1000;
        elapsed = elapsed / 1000;
        long seconds = elapsed % 60;
        elapsed = elapsed / 60;
        long minutes = elapsed % 60;
        elapsed = elapsed / 60;
        long hours = elapsed % 24;
        elapsed = elapsed / 24;
        return (hours != 0?(hours + " hour(s) "):"") + (minutes != 0?(minutes + " minute(s) "):"") + (seconds != 0?(seconds + " second(s) "):"") + milliseconds+" millisecond(s)";
	}

	public long addContent(Date startDate){
		long result = new Date().getTime()-startDate.getTime();
		documents.add(result);
		return result;
	}
	
	public long addContainer(Date startDate){
		long result = new Date().getTime()-startDate.getTime();
		folders.add(result);
		return result;
	}
	
	public boolean end(Configuration configuration){
		if (configuration.getTotalDocuments() == 0) return false;
		return documents.size() >= configuration.getTotalDocuments();
	}
}
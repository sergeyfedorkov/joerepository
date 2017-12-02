package structure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		System.out.println("\nStatistics:");
		
		long foldersAvarage = (long)folders.stream().mapToLong(p -> p.longValue()).average().getAsDouble();
		System.out.println("Number of Folders created: "+folders.size()+". Average time: "+getTimeElapsed(foldersAvarage));
		
		long documentsAvarage = (long)documents.stream().mapToLong(p -> p.longValue()).average().getAsDouble();
		System.out.println("Number of Documents created: "+documents.size()+". Average time: "+getTimeElapsed(documentsAvarage));
		
		System.out.println("Total time consumed: "+getTimeElapsed(new Date().getTime()-startDate.getTime()));
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
		return documents.size() >= configuration.getTotalDocuments();
	}
}
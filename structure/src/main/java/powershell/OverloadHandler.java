package powershell;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPMessage;

public class OverloadHandler {
	private static final String[] HTTP_ERROR_PREFIX = {"Server returned HTTP response code: ", "The server sent HTTP status code "};
	private static final int[] delays = new int[]{1, 2, 4, 8, 8};
	private final long waitTimeForHttpErrors = 1000*5;
	private final int maxRetries = 3;
	
	private static OverloadHandler instance = null;
	
	public static synchronized OverloadHandler getInstance(){
		if (instance == null) instance = new OverloadHandler();
		return instance;
	}
	
	public OverloadHandler(){}
	
	public SOAPMessage soapCall(SOAPConnection connection, SOAPMessage msg, String serviceEndpoint, int index) throws Exception{
		try{
			return connection.call(msg, serviceEndpoint);
		}catch(Exception io){
			if (foundHTTPError(io, new int[]{429})){
				if (sleep(index)){
					return soapCall(connection, msg, serviceEndpoint, ++index);
				} else {
					throw io;
				}
			} else if(foundHTTPError(io)){
				if (index >= maxRetries){
					throw io;
				}else{
					try {
						Thread.sleep(waitTimeForHttpErrors);
					} catch (InterruptedException e) {}
					return soapCall(connection, msg, serviceEndpoint, ++index);
				}				
			} else {
				throw io;
			}
		}
	}
	
	private boolean foundHTTPError(Throwable exception, int[] errors){
		for (int code:errors){
			for (String prefix:HTTP_ERROR_PREFIX){
				if (exception.getMessage().indexOf(prefix+code) != -1) return true;
			}
		}
		
		return false;
	}
	
	private boolean foundHTTPError(Throwable exception){
		return foundHTTPError(exception, new int[]{400, 404, 409, 500, 503});
	}
	
	private boolean sleep(int index){
		try {
			if (index>=delays.length) {
				System.out.println("The final attempt to properly handle the 429 response was unsuccessful, therefore this object was skipped and the operation continued.");
				return false;
			}
			
			System.out.println("A 429 response was received. This operation paused for "+delays[index]+" minute(s) before making another attempt");
			Thread.sleep(delays[index]*1000*60);
		} catch (InterruptedException e) {
			System.out.println("An exception occurred during the processing of the 429 response, therefore this object was skipped and the operation continued");
			return false;
		}
		
		return true;
	}
}
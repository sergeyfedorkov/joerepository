package migration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MigrationManager {
	private static final int THREADS_NUMBER = 5;
	private static ThreadPoolExecutor instance;
	
	public static synchronized ThreadPoolExecutor getInstance(){
		if (instance == null) {
			instance = new ThreadPoolExecutor(THREADS_NUMBER, THREADS_NUMBER, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			instance.allowCoreThreadTimeOut(true);
		}
		
		return instance;
	}
}
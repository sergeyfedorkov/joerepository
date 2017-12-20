package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.Files;

public class Utils {
	public static final String SEPARATOR = "/";
	public static final String LOG_FOLDER = "process";
	public static final String WRITE_FILE = LOG_FOLDER+SEPARATOR+"structure_write.log";
	public static final String MEDIUM_FILE = LOG_FOLDER+SEPARATOR+"structure_medium.log";
	public static final String READ_FILE = LOG_FOLDER+SEPARATOR+"structure_read.log";
	
	public static void breakline(){
		print("\n");
	}
	
	public static void print(String print){
		try {
			System.out.write(print.getBytes());
			Files.copy(new File(WRITE_FILE), new File(MEDIUM_FILE));
		} catch (IOException e) {}
	}
	
	public static void print(String path, String target, String result){
		path = apply(path);
		target = apply(target);
		
		boolean replace = !path.equalsIgnoreCase(target);
		if (replace) path = path.replaceAll(target, "");
		if (path.isEmpty()) path = target;
		path = apply(path);
		
		int count = replace?path.lastIndexOf(SEPARATOR)+1:0;
		out(path, result, count);
	}
	
	private static void out(String path, String result, int count){
		StringBuffer buffer = new StringBuffer();
		for (int i=0;i<count;i++) buffer.append("-");
		
		buffer.append(path.substring(count, path.length()));
		if (result != null) buffer.append(result);
		print(buffer.toString());
	}
	
	public static String apply(String path){
		if (path.startsWith("\\") || path.startsWith(SEPARATOR)) path = path.substring(1, path.length());
		return path.replaceAll("\\\\", SEPARATOR);
	}
	
	public static void deleteFolder(File folder) {
		for (File child:folder.listFiles()) {
			if (child.isDirectory()) deleteFolder(child);
			child.delete();
		}
	}
	
	public static String getResultFromStream(InputStream stream, long skip){
		InputStreamReader isr = new InputStreamReader(stream);
		final int bufferSize = 1024;
		char[] buffer = new char[bufferSize];
		StringBuffer strBuffer = new StringBuffer();

		try {
			if (skip != 0) isr.skip(skip);
			
		    while (true) {
		        int read = isr.read(buffer, 0, bufferSize);
		        if (read == -1) break;
		        strBuffer.append(buffer, 0, read);
		     }
		} catch (IOException e) {}
		
		return strBuffer.toString();
	}
}
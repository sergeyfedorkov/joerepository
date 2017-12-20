package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.Files;

public class Utils {
	public static final String SEPARATOR = "/";
	
	public static void breakline(){
		print("\n");
	}
	
	public static void print(String print){
						try {
							System.out.write(print.getBytes());
							new File("structure_read.log").delete();
							Files.copy(new File("structure.log"), new File("structure_read.log"));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

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
	
	public static String getResultFromStream(InputStream stream) throws IOException{
		try{
			String line;
			StringBuffer lines = new StringBuffer();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			while((line = reader.readLine()) != null ) lines.append(line).append("\n");
			return lines.toString();
		} finally {
			try{
				if (stream != null) stream.close();
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
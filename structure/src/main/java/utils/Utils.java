package utils;

import java.io.File;

public class Utils {
	public static final String SEPARATOR = "/";
	
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
		for (int i=0;i<count;i++) System.out.print("-");
		
		System.out.print(path.substring(count, path.length()));
		System.out.println(result);
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
}
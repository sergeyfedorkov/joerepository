package objects;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import structure.Configuration;
import structure.Statistics;
import utils.Utils;

public class FileSystemObject extends GenericObject {
	private static final long serialVersionUID = 265552533095450746L;

	public FileSystemObject(String pathname, String target, int size, Statistics statistics, Configuration configuration) {
		super(pathname, target, size, statistics, configuration);
	}
	
	public boolean checkTarget(){
		return exists();
	}
	
	public boolean createContent() throws IOException{
		boolean result = createNewFile();
		populate();
		return result;
	}

	public boolean createContainer(){
		return mkdir();
	}
	
	public boolean createTarget(){
		return mkdirs();
	}
	
	public boolean removeTarget(){
		if (isDirectory()) Utils.deleteFolder(this);
		return super.delete();
	}
	
	/*
	 * Private Section 
	 */
	private void populate(){
		OutputStream stream = null;
		
		try{
			stream = new FileOutputStream(getPath());
			stream.write(getBytes());
		}catch(Exception e){
			try{
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}catch(Exception e1){}
		}
	}
}
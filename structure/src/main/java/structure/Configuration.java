package structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;

public class Configuration {
	private static final String FILE = "structure.ini";
	private static final String PLACEHOLDER_LEVEL = "%level%";
	private static final String PLACEHOLDER_INDEX = "%index%";
	
	//#configuration of the target server
	private String site;
	private String dropboxuser;
	private String boxuser;
	private String googleuser;
	private String exchangeuser;

	//#configuration of the target location
	private String target;

	//#configuration of the structure to be created
	private int deep;
	private int folders;
	private int documents;
	private int size;

	//#configuration of the container/content names
	private String contentFormat;
	private String containerFormat;

	//#configuration whether delete target location or not
	private boolean deleteTarget;
	
	//#configuration of whether to print process or not
	private boolean print;
	
	private static Configuration instance;
	
	public static synchronized Configuration getInstance(){
		if (instance == null) instance = new Configuration();
		return instance;
	}
	
	public Configuration(){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(FILE));
			String line = null;
			while((line = reader.readLine()) != null){
				if (line.startsWith("#") || line.isEmpty()) continue;
				
				Field field = this.getClass().getDeclaredField(line.split("=")[0]);
				field.setAccessible(true);
				
				if (field.getType().getTypeName().equals("boolean")){
					field.set(this, Boolean.TRUE);
				} else if (field.getType().getTypeName().equals("int")){
					field.set(this, Integer.parseInt(line.split("=")[1]));
				} else {
					field.set(this, line.split("=")[1]);
				}
			}
		}catch(Exception e){
			try{
				if (reader != null) reader.close();
			}catch(Exception ee){}
		}
	}

	public int getDeep() {
		return deep;
	}

	public int getFolders() {
		return folders;
	}

	public int getDocuments() {
		return documents;
	}

	public int getSize() {
		return size;
	}

	public String getSite() {
		return site;
	}

	public String getTarget() {
		return target;
	}
	
	public String getDropboxuser() {
		return dropboxuser;
	}
	
	public String getBoxuser() {
		return boxuser;
	}
	
	public String getGoogleuser() {
		return googleuser;
	}
	
	public String getExchangeuser() {
		return exchangeuser;
	}
	
	public boolean isDeleteTarget() {
		return deleteTarget;
	}
	
	public boolean isPrint() {
		return print;
	}
	
	public boolean isExchange(){
		return exchangeuser != null;
	}
	
	public boolean isSharepoint(){
		return site != null;
	}
	
	public boolean isDropbox(){
		return dropboxuser != null;
	}
	
	public boolean isBox(){
		return boxuser != null;
	}
	
	public boolean isGoogle(){
		return googleuser != null;
	}
	
	public String getContentName(int index, int level){
		return contentFormat.replaceAll(PLACEHOLDER_LEVEL, "level "+level).replaceAll(PLACEHOLDER_INDEX, index+"");
	}
	
	public String getContainerName(int index, int level){
		return containerFormat.replaceAll(PLACEHOLDER_LEVEL, "level "+level).replaceAll(PLACEHOLDER_INDEX, index+"");
	}
	
	public boolean validate(){
		boolean commonResult = target != null && deep != 0 && folders != 0 && documents != 0 && size != 0;
		boolean googleResult = dropboxuser != null;
		boolean dropboxResult = dropboxuser != null;
		boolean boxResult = boxuser != null;
		boolean exchangeResult = exchangeuser != null;
		boolean sharepointResult = site != null;
		boolean filesystemResult = !dropboxResult && !sharepointResult && !boxResult && !googleResult && !exchangeResult;
		
		boolean result = commonResult && (filesystemResult || dropboxResult || sharepointResult || boxResult || googleResult || exchangeResult);
		if (!result){
			StringBuffer buffer = new StringBuffer();
			buffer.append("Application is not configured properly. Set up configuration in the structure.ini file\n");
			buffer.append("Mandatory parameters are:\n");
			buffer.append("'site' - for sharepoint or\n");
			buffer.append("''dropboxuser' - for dropbox or\n");
			buffer.append("''boxuser' - for box or\n");
			buffer.append("''googleuser' - for google or\n");
			buffer.append("''exchangeuser' - for exchange or\n");
			buffer.append("nothing - for file system\n");
			
			buffer.append("'target' - target location (fs directory, sharepoint library or dropbox folder)\n");
			buffer.append("'deep', 'folders', 'documents', 'size', 'contentFormat', 'containerFormat' - the definition of the structure to create\n");
			buffer.append("'deleteSource' is optional'\n");
			buffer.append("First case is for sharepoint run, the second one is for filesystem run\n");
			buffer.append("Its not neccessary to remove parameters, just comment it out with '#'\n");
			buffer.append("\n-------example of ini file---------\n\n");
			
			buffer.append("#site=****\n");
			buffer.append("#dropboxuser=****\n");
			buffer.append("#boxuser=****\n");
			buffer.append("#googleuser=****\n");
			buffer.append("#target=library\n");
			buffer.append("deep=3\n");
			buffer.append("folders=3\n");
			buffer.append("documents=3\n");
			buffer.append("size=3\n");
			buffer.append("contentFormat=document %index% %level%.txt\n");
			buffer.append("containerFormat=folder %index% %level%\n");
			buffer.append("deleteSource=true\n");
			System.out.println(buffer.toString());
		}
		
		return result;
	}
}
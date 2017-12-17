package configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import utils.Utils;

public class Configuration{
	public static final String STORE = "configurations";
	public static final String EXTENSION = ".conf";
	public static final String NEW = "";
	
	private static final String PLACEHOLDER_LEVEL = "%level%";
	private static final String PLACEHOLDER_INDEX = "%index%";
	
	private static final String B = "b";
	private static final String KB = "kb";
	private static final String MB = "mb";
	private static final String GB = "gb";
	
	private static int MAX = 100;
	
	private String name = "";
	
	//#configuration of the target server
	@ConfigurationAnnotation(type="Configuration of the Target Server", name="Site Url")
	private String site;
	
	@ConfigurationAnnotation(type="Configuration of the Target Server", name="Dropbox User")
	private String dropboxuser;
	
	@ConfigurationAnnotation(type="Configuration of the Target Server", name="Box User")
	private String boxuser;
	
	@ConfigurationAnnotation(type="Configuration of the Target Server", name="Google User")
	private String googleuser;
	
	@ConfigurationAnnotation(type="Configuration of the Target Server", name="Exchange User")
	private String exchangeuser;

	//#configuration of the target location
	@ConfigurationAnnotation(type="Configuration of the Target Location", name="Target Location")
	private String target = "library";

	//#configuration of the structure to be created
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Deep")
	private int deep = 2;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Containers count on level")
	private int folders = 2;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Contents count on level")
	private int documents = 2;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Size of the content")
	private long size = 2;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Total Containers in the structure")
	private int totalFolders;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Total Contents in the structure")
	private int totalDocuments;
	
	@ConfigurationAnnotation(type="Configuration of the structure to be created", name="Total Size in the structure")
	private String totalSize;

	//#configuration of the container/content names
	@ConfigurationAnnotation(type="Configuration of the container/content names", name="Format of the content's name")
	private String contentFormat = "document %index% level %level%.txt";
	
	@ConfigurationAnnotation(type="Configuration of the container/content names", name="Format of the container's name")
	private String containerFormat = "folder %index% level %level%";

	//#other options
	@ConfigurationAnnotation(type = "Other Options", name="Delete Target")
	private boolean deleteTarget = true;
	
	@ConfigurationAnnotation(type = "Other Options", name="Print structure to the log")
	private boolean print = true;
	
	@ConfigurationAnnotation(type = "Other Options", name="Read structure")
	private boolean read;
	
	@ConfigurationAnnotation(type = "Other Options", name="Enable Proxy")
	private boolean proxy;
	
	public Configuration(String name){
		this.name=name;
	}
	
	public List<Field> getFields(){
		List<Field> fields = new ArrayList<Field>();
		for (final Field field:getClass().getDeclaredFields()){
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
			if (field.getName().equals("name")) continue;
			
			field.setAccessible(true);
			fields.add(field);
		}
		return fields;
	}
	
	public static Configuration load(File file){
		Configuration configuration = new Configuration(file.getName().substring(0, file.getName().lastIndexOf(".")));
		BufferedReader reader = null;
		
		try{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			
			while ((line = reader.readLine()) != null){
				String split[] = line.split("=");
				Field field = configuration.getClass().getDeclaredField(split[0]);
				field.setAccessible(true);
				
				if (field.getType().getTypeName().equals("boolean")){
					field.set(configuration, Boolean.TRUE);
				} else if (field.getType().getTypeName().equals("int")){
					field.set(configuration, Integer.parseInt(split[1]));
				} else if (field.getType().getTypeName().equals("long")){
					field.set(configuration, Long.parseLong(split[1]));
				} else {
					field.set(configuration, split.length == 1?null:split[1]);
				}
			}
		}catch (Exception e){}finally{
			try{
				if (reader != null) reader.close();
			}catch(Exception ee){}
		}
		
		return configuration;
	}
	
	public Configuration delete(){
		new File(Configuration.STORE+Utils.SEPARATOR+getName()+EXTENSION).delete();
		return this;
	}
	
	public Configuration save(){
		FileOutputStream stream = null;
		try{
			stream = new FileOutputStream(new File(Configuration.STORE+Utils.SEPARATOR+getName()+EXTENSION));
			stream.write(toString().getBytes());
		}catch(Exception e){
			try{
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}catch(Exception ee){}
		}
		
		return this;
	}
	
	public String rename(String oldName){
		if (!getName().equalsIgnoreCase(oldName)) new File(Configuration.STORE+Utils.SEPARATOR+oldName+EXTENSION).renameTo(new File(Configuration.STORE+Utils.SEPARATOR+getName()+EXTENSION));
		return oldName;
	}
	
	public static Configuration open(){
		ConfigurationDialog dialog = new ConfigurationDialog(new Shell(Display.getDefault(), SWT.DIALOG_TRIM | SWT.SYSTEM_MODAL));
		return dialog.open();
	}
	
	public int getDeep() {
		if (totalFolders != 0){
			for (int deep=1;deep<MAX;deep++){
				long result = 0;
				for (int j=1;j<=deep;j++) result += Math.pow(deep, j);
				
				if (result>totalFolders) {
					this.deep = deep;
					return deep;
				}
			}
		}
		
		return deep;
	}

	public int getFolders() {
		if (totalFolders != 0){
			for (int deep=1;deep<MAX;deep++){
				long result = 0;
				for (int j=1;j<=deep;j++) result += Math.pow(deep, j);
				
				if (result>totalFolders) {
					this.deep = deep;
					return deep;
				}
			}
		}
		return folders;
	}

	public int getDocuments() {
		if (totalDocuments != 0){
			return totalDocuments/totalFolders;
		}
		return documents;
	}

	public long getSize() {
		if (totalSize != null && !totalSize.isEmpty()){
			long index = 1;
			long value = 0;
			
			if (totalSize.toLowerCase().indexOf(KB) != -1){
				value = Long.parseLong(totalSize.toLowerCase().replaceAll(KB, "").trim());
				index = 1024;
			} else if (totalSize.toLowerCase().indexOf(MB) != -1){
				value = Long.parseLong(totalSize.toLowerCase().replaceAll(MB, "").trim());
				index = 1024*1024;
			} else if (totalSize.toLowerCase().indexOf(GB) != -1){
				value = Long.parseLong(totalSize.toLowerCase().replaceAll(GB, "").trim());
				index = 1024*1024*1024;
			} else if (totalSize.toLowerCase().indexOf(B) != -1){
				value = Long.parseLong(totalSize.toLowerCase().replaceAll(B, "").trim());
			} else {
				value = Long.parseLong(totalSize);
			}
			
			return (value*index)/totalDocuments;
		}
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
		return exchangeuser != null && !exchangeuser.isEmpty();
	}
	
	public boolean isSharepoint(){
		return site != null && !site.isEmpty();
	}
	
	public boolean isDropbox(){
		return dropboxuser != null && !dropboxuser.isEmpty();
	}
	
	public boolean isBox(){
		return boxuser != null && !boxuser.isEmpty();
	}
	
	public boolean isGoogle(){
		return googleuser != null && !googleuser.isEmpty();
	}
	
	public boolean isFileSystem(){
		return !isSharepoint() && !isDropbox() && !isBox() &&! isGoogle() && !isExchange();
	}
	
	public boolean isRead() {
		return read;
	}
	
	public boolean isProxy(){
		return proxy;
	}
	
	public String getContentName(int index, int level){
		return contentFormat.replaceAll(PLACEHOLDER_LEVEL, "level "+level).replaceAll(PLACEHOLDER_INDEX, index+"");
	}
	
	public String getContainerName(int index, int level){
		return containerFormat.replaceAll(PLACEHOLDER_LEVEL, "level "+level).replaceAll(PLACEHOLDER_INDEX, index+"");
	}
	
	public String validate(){
		boolean configurationNameResult = name != null && !name.isEmpty();
		boolean targetServerResult = (isSharepoint() ^ isDropbox() ^ isBox() ^ isGoogle() ^ isExchange()) || isFileSystem();
		boolean targetLocationResult = target != null && !target.isEmpty();
		boolean targetStructureResult = (deep != 0 && folders != 0 && documents != 0 && size != 0) ^ (totalFolders != 0 && totalDocuments != 0 && totalSize != null && !totalSize.isEmpty());
		boolean targetFormatResult = contentFormat != null && !contentFormat.isEmpty() && containerFormat != null && !containerFormat.isEmpty();
		
		if (!configurationNameResult) return "Enter Configuration name or select an existing one";
		if (!targetServerResult) return "Configuration of the Target Server is not valid";
		if (!targetLocationResult) return "Configuration of the Target Location is not valid";
		if (!targetStructureResult) return "Configuration of the Target Structure is not valid";
		if (!targetFormatResult) return "Configuration of the Target Format is not valid";
		return null;
	}

	public int getTotalFolders() {
		return totalFolders;
	}

	public int getTotalDocuments() {
		return totalDocuments;
	}

	public String getTotalSize() {
		return totalSize;
	}
	
	public boolean setFieldValue(Field field, Object value){
		try{
			if (field.getGenericType().toString().equals("int")){
				field.set(this, Integer.parseInt(value.toString()));
			} else if (field.getGenericType().toString().equals("long")){
				field.set(this, Long.parseLong(value.toString()));
			} else if (field.getGenericType().toString().equals("boolean")){
				field.set(this, Boolean.parseBoolean(value.toString()));
			} else {
				field.set(this, value);
			}
			
			return true;
		} catch (Exception e){}
		
		return false;
	}
	
	public Object getFieldValue(Field field){
		try{
			field.setAccessible(true);
			return field.get(this);
		} catch (Exception e){}
		
		return null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		for (Field field:getFields()){
			if (getFieldValue(field) == null) continue;
			Object value = getFieldValue(field);
		
			if (value instanceof Boolean){
				if ((Boolean)value){
					buffer.append(field.getName());
					buffer.append("\n");
				}
			} else if (!value.toString().isEmpty()){
				buffer.append(field.getName()+"="+value);
				buffer.append("\n");
			}
		}
		
		return buffer.toString();
	}
	
	public String getTitle(){
		String result = "";
		if (isSharepoint()) result+="Sharepoint,";
		if (isDropbox()) result+="Dropbox,";
		if (isBox()) result+="Box,";
		if (isGoogle()) result+="Google,";
		if (isFileSystem()) result+="FileSystem,";
		
		result+=(isRead()?"Read":"Create");
		return result;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Configuration other = (Configuration) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
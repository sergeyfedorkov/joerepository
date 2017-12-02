package objects;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import structure.Configuration;
import structure.Statistics;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleObject extends GenericObject {
	private static final long serialVersionUID = 4368340431019705735L;
	private static final String MIME_TYPE = "application/vnd.google-apps.folder";
	private GoogleCredential credential;
	private File parent;
	private File folder;

	public GoogleObject(String pathname, String target, long size, Statistics statistics, Configuration configuration, GoogleCredential credentials, Object parent) {
		super(pathname, target, size, statistics, configuration);
		this.credential = credentials;
		this.parent = (File)parent;
	}

	@Override
	public boolean createContainer() throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(getName());
		fileMetadata.setParents(Arrays.asList(new String[]{parent.getId()}));
		fileMetadata.setMimeType(MIME_TYPE);

		folder = getService().files().create(fileMetadata).execute();
		return folder != null;
	}

	@Override
	public boolean createContent() throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(getName());
		fileMetadata.setParents(Arrays.asList(new String[]{parent.getId()}));
		InputStreamContent mediaContent = new InputStreamContent("*/*", new ByteArrayInputStream(getBytes()));
		
		getService().files().create(fileMetadata, mediaContent).execute();
		return true;
	}

	@Override
	public boolean createTarget() throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(getTarget());
		fileMetadata.setMimeType(MIME_TYPE);

		folder = getService().files().create(fileMetadata).execute();
		return true;
	}

	@Override
	public boolean checkTarget() throws Exception {
		folder = findTarget();
		return folder != null;
	}

	@Override
	public boolean removeTarget() throws Exception {
		getService().files().delete(folder.getId()).execute();
		return true;
	}
	
	public File getFolder() {
		return folder;
	}
	
	public GoogleCredential getCredential() {
		return credential;
	}
	
	/*
	 * Private section
	 */
	private File findTarget() throws IOException{
		String pageToken = null;
		do {
		  FileList files = getService().files().list().setQ("mimeType = '"+MIME_TYPE+"' and name = '"+getTarget()+"'").setSpaces("drive").setFields("nextPageToken, files(id, name)").setPageToken(pageToken).execute();
		  for (File file : files.getFiles()) {
			  if (file.getParents() == null || file.getParents().isEmpty()) return file;
		  }
		  pageToken = files.getNextPageToken();
		} while (pageToken != null);
		
		return null;
	}
	
	private Drive getService(){
		return new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("application name").build();
	}
}
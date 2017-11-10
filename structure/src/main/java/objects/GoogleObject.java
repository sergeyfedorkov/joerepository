package objects;

import java.util.Arrays;

import structure.Statistics;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class GoogleObject extends GenericObject {
	private static final long serialVersionUID = 4368340431019705735L;
	private GoogleCredential credential;

	public GoogleObject(String pathname, String target, int size, Statistics statistics, GoogleCredential credentials) {
		super(pathname, target, size, statistics);
		this.credential=credentials;
	}

	@Override
	public boolean createContainer() throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(getName());
		fileMetadata.setParents(Arrays.asList(getPath().split("/")));
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		getService().files().create(fileMetadata).set("id", getPath()).execute();
		return false;
	}

	@Override
	public boolean createContent() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createTarget() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkTarget() throws Exception {
		return false;
	}

	@Override
	public boolean removeTarget() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Drive getService(){
		return new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("application name").build();
	}

	public GoogleCredential getCredential() {
		return credential;
	}
}
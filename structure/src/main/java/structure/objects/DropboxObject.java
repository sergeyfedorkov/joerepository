package structure.objects;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import structure.Statistics;
import structure.configuration.Configuration;
import utils.Utils;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxTeamClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.dropbox.core.v2.team.UserSelectorArg;

public class DropboxObject extends GenericObject {
	private static final long serialVersionUID = -547277363906329224L;
	private static final DbxRequestConfig config = DbxRequestConfig.newBuilder("").withAutoRetryEnabled(5).build();
	private static DbxClientV2 client;
	
	private String token;
	private String user;
	private Metadata metadata;
	
	public DropboxObject(String path, String target, long size, Statistics statistics, Configuration configuration, String user, String token) {
		super(path, target, size, statistics, configuration);
		this.user=user;
		this.token=token;
	}

	public boolean checkTarget() throws Exception{
		return getClient().files().getMetadata(Utils.SEPARATOR+getTarget()) != null;
	}
	
	public boolean removeTarget() throws Exception{
		getClient().files().deleteV2(Utils.SEPARATOR+getTarget());
		return true;
	}
	
	public boolean createTarget() throws Exception{
		getClient().files().createFolderV2(Utils.SEPARATOR+getTarget());
		return true;
	}
	
	public boolean createContainer() throws Exception{
		getClient().files().createFolderV2(Utils.SEPARATOR+getTarget()+Utils.SEPARATOR+Utils.apply(getPath()));
		return true;
	}
	
	public boolean createContent() throws Exception{
		getClient().files().upload(Utils.SEPARATOR+getTarget()+Utils.SEPARATOR+Utils.apply(getPath())).uploadAndFinish(new ByteArrayInputStream(getBytes()));
		return true;
	}
	
	public boolean isDirectory(){
		return metadata == null || metadata instanceof FolderMetadata;
	}
	
	public boolean retrieveChildren() throws Exception {
		ListFolderResult result = getClient().files().listFolder(Utils.SEPARATOR+getTarget()+Utils.SEPARATOR+Utils.apply(getPath()));
		while (true) {
			for (Metadata metadata:result.getEntries()){
				DropboxObject object = new DropboxObject(getPath()+Utils.SEPARATOR+metadata.getName(), getTarget(), getSize(), getStatistics(), getConfiguration(), user, token);
				object.setMetadata(metadata);
				
				getChildren().add(object);
			}
			
			if (!result.getHasMore()) break;
			
			result = getClient().files().listFolderContinue(result.getCursor());
		}
		
		return true;
	}
	
	public DbxClientV2 getClient() throws Exception{
		if (client != null) return client;
		
		List<UserSelectorArg> args = new ArrayList<UserSelectorArg>();
		args.add(UserSelectorArg.email(user));
		TeamMemberInfo info = getTeamClient().team().membersGetInfo(args).get(0).getMemberInfoValue();
		client = getTeamClient().asMember(info.getProfile().getTeamMemberId());
		return client;
	}
	

	public String getToken() {
		return token;
	}

	public String getUser() {
		return user;
	}
	
	/*
	 * Private Section
	 */
	private DbxTeamClientV2 getTeamClient(){
		return new DbxTeamClientV2(config, token);
	}

	private void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
}
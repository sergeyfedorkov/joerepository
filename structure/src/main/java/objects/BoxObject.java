package objects;

import java.io.ByteArrayInputStream;

import structure.Configuration;
import structure.Statistics;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class BoxObject extends GenericObject{
	private static final long serialVersionUID = -6407870180519307013L;
	private BoxAPIConnection api;
	private BoxFolder parent;
	private BoxFolder folder;

	public BoxObject(String pathname, String target, int size, Statistics statistics, Configuration configuration, BoxAPIConnection api, Object parent) {
		super(pathname, target, size, statistics, configuration);
		this.api = api;
		this.parent=(BoxFolder)parent;
	}
	
	@Override
	public boolean createContainer() throws Exception {
		folder = parent.createFolder(getName()).getResource();
		return true;
	}

	@Override
	public boolean createContent() throws Exception {
		parent.uploadFile(new ByteArrayInputStream(getBytes()), getName());
		return true;
	}

	@Override
	public boolean createTarget() throws Exception {
		folder = BoxFolder.getRootFolder(api).createFolder(getTarget()).getResource();
		return true;
	}

	@Override
	public boolean checkTarget() throws Exception {
		folder = getTargetFolder();
		return folder != null;
	}

	@Override
	public boolean removeTarget() throws Exception {
		folder.delete(true);
		return true;
	}

	public BoxAPIConnection getApi() {
		return api;
	}
	
	public BoxFolder getFolder() {
		return folder == null?BoxFolder.getRootFolder(api):folder;
	}
	
	/*
	 * private section
	 */
	private BoxFolder getTargetFolder(){
		for (BoxItem.Info item:BoxFolder.getRootFolder(api).getChildren()){
			if (item.getName().equalsIgnoreCase(getTarget())) return (BoxFolder)item.getResource();
		}
		
		return null;
	}
}
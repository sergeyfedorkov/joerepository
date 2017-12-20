package structure.objects;

import java.io.ByteArrayInputStream;

import structure.Statistics;
import structure.configuration.Configuration;
import utils.Utils;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxResource;

public class BoxObject extends GenericObject{
	private static final long serialVersionUID = -6407870180519307013L;
	private BoxAPIConnection api;
	private BoxFolder parent;
	private BoxResource resource;

	public BoxObject(String pathname, String target, long size, Statistics statistics, Configuration configuration, BoxAPIConnection api, Object parent) {
		super(pathname, target, size, statistics, configuration);
		this.api = api;
		this.parent=(BoxFolder)parent;
	}
	
	@Override
	public boolean createContainer() throws Exception {
		resource = parent.createFolder(getName()).getResource();
		return true;
	}

	@Override
	public boolean createContent() throws Exception {
		parent.uploadFile(new ByteArrayInputStream(getBytes()), getName());
		return true;
	}

	@Override
	public boolean createTarget() throws Exception {
		resource = BoxFolder.getRootFolder(api).createFolder(getTarget()).getResource();
		return true;
	}

	@Override
	public boolean checkTarget() throws Exception {
		resource = getTargetFolder();
		return resource != null;
	}

	@Override
	public boolean removeTarget() throws Exception {
		((BoxFolder)resource).delete(true);
		return true;
	}

	public BoxAPIConnection getApi() {
		return api;
	}
	
	public BoxResource getFolder() {
		return resource == null?BoxFolder.getRootFolder(api):resource;
	}
	
	public boolean isDirectory(){
		return resource instanceof BoxFolder;
	}
	
	public boolean retrieveChildren() throws Exception {
		for (BoxItem.Info itemInfo:(BoxFolder)resource) {
			BoxObject object = new BoxObject(getPath()+Utils.SEPARATOR+itemInfo.getName(), getTarget(), getSize(), getStatistics(), getConfiguration(), api, resource);
			object.setResource(itemInfo.getResource());
			getChildren().add(object);
		}
		
		return true;
	}
	
	/*
	 * private section
	 */
	private BoxFolder getTargetFolder(){
		for (BoxItem.Info item:BoxFolder.getRootFolder(api)){
			if (item.getName().equalsIgnoreCase(getTarget())) return (BoxFolder)item.getResource();
		}
		
		return null;
	}

	private void setResource(BoxResource resource) {
		this.resource = resource;
	}
}
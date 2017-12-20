package structure.ui;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

public class GenericDialog extends Dialog {
	public GenericDialog(Shell parent) {
		super(parent);
		setLogo();
	}
	
	protected void setLogo(){
		try{
			getParent().setImage(ImageDescriptor.createFromImageData(new ImageData(new FileInputStream(new File("icons/logo.ico")))).createImage());
		}catch(Exception e){}
	}
}
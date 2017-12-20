package structure;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import objects.GenericObject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.Utils;

import com.google.common.io.Files;

public class ViewLogger extends Dialog {
	private static final int WIDTH = 900;
	private static final int HEIGHT = 600;
	
	private Structure structure;
	private GenericObject target;
	private Text text; 

	public ViewLogger(Shell parent) {
		super(parent);
	}
	
	public static Shell create(Structure structure, GenericObject target){
		Shell shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.SYSTEM_MODAL);
		shell.setText(structure.getConfiguration().getTitle());
		ViewLogger dialog = new ViewLogger(shell);
		dialog.setStructure(structure);
		dialog.setTarget(target);
		return dialog.open();
	}

	public Shell open(){
		Shell dialog = getParent();
		dialog.setLayoutData(new GridData(GridData.FILL_BOTH));
		dialog.setLayout(new GridLayout(1, false));
		
		createContent(dialog);
		
		dialog.setSize(WIDTH, HEIGHT);
		centerOnScreen(dialog);
		
		int count = 0;
		dialog.open();
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			count = read(++count);
			if (!display.readAndDispatch()) display.sleep();
		}
		return dialog;
	}
	
	private void createContent(Shell parent){
		text = new Text(parent, SWT.MULTI | SWT.V_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				text.setTopIndex(text.getLineCount() - 1);	
			}
		});

		changeOut();
		
		Job job = new Job("First Job") {
            protected IStatus run(IProgressMonitor monitor) {
            	structure.call(target);
                return Status.OK_STATUS;
            }

        };
        job.setUser(true);
        job.schedule();
	}
	
	private int read(int count){
		if (count != 1) return count;
		
		try{
			File file = new File(Utils.READ_FILE);
			Files.copy(new File(Utils.MEDIUM_FILE), file);
			
			FileInputStream input = new FileInputStream(file);
			Long available = (Long)text.getData();
			String append = Utils.getResultFromStream(input, (available != null?available:0));
			if (!append.isEmpty()){
				text.append(append);
				text.setData(file.length());
			}
		}catch(Exception ee){}
		return 0;
	}
	
	private void changeOut(){
		try {
			System.setOut(new PrintStream(new FileOutputStream(new File(Utils.WRITE_FILE))));
		} catch (FileNotFoundException e) {}
	}
	
	private void centerOnScreen(Shell shell) {
		shell.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - shell.getSize().x/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - shell.getSize().y/2);
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}

	public void setTarget(GenericObject target) {
		this.target = target;
	}
}
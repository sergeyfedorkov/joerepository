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

public class Console extends Dialog {
	private static final int WIDTH = 900;
	private static final int HEIGHT = 600;
	
	private Structure structure;
	private GenericObject target;
	private Text text; 

	public Console(Shell parent) {
		super(parent);
		parent.setText("Console");
	}
	
	public static Shell create(Structure structure, GenericObject target){
		Console dialog = new Console(new Shell(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.SYSTEM_MODAL));
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
		if (count != 10) return count;
		if (text == null || text.isDisposed()) return 0;
		
		try{
			FileInputStream input = new FileInputStream(new File("structure_read.log"));
			text.setText(Utils.getResultFromStream(input));
		}catch(Exception ee){}
		return 0;
	}
	
	private void changeOut(){
		try {
			System.setOut(new PrintStream(new FileOutputStream(new File("structure.log"))));
		} catch (FileNotFoundException e) {}
	}
	
	private void centerOnScreen(Shell shell) {
		shell.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - shell.getSize().x/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - shell.getSize().y/2);
	}

	public Structure getStructure() {
		return structure;
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}

	public GenericObject getTarget() {
		return target;
	}

	public void setTarget(GenericObject target) {
		this.target = target;
	}
}
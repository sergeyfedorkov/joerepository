package structure.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import structure.configuration.Configuration;

public class ConfigurationDialogChange extends GenericDialog {
	private static final int HEIGHT = 65;
	private Button save;

	public ConfigurationDialogChange(Shell parent) {
		super(parent);
	}
	
	public String open(Configuration configuration) {
		String oldName = configuration.getName();
		Shell dialog = getParent();
		
		dialog.setText("Enter Configuration Name");
		dialog.setLayout(new GridLayout(3, false));
	
		createContent(dialog, configuration);
		
		dialog.setLocation(dialog.getParent().getLocation().x+20, dialog.getParent().getLocation().y+20);
		dialog.setSize(dialog.getParent().getSize().x-40, HEIGHT);
		dialog.open();
				
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		return configuration.rename(oldName);
	}
	
	private void createContent(Shell shell, Configuration configuration){
		new Label(shell, SWT.NONE).setText("Name: ");
		
		Text text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(configuration.getName());
		text.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				save.setEnabled(!text.getText().isEmpty() && !text.getText().equalsIgnoreCase(configuration.getName()));
			}
		});
		
		save = new Button(shell, SWT.PUSH);
		save.setText("Change");
		save.setEnabled(false);
		save.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configuration.setName(text.getText());
				shell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
}
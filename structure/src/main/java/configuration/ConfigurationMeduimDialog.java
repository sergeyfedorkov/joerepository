package configuration;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

public class ConfigurationMeduimDialog extends Dialog {
	protected static final int WIDTH = 530;
	
	protected Configuration configuration;
	
	protected Button save;
	protected ToolItem delete;
	protected ToolItem add;
	protected ToolItem change;
	protected Combo combo;
	protected Label status;
	
	protected List<Control> options;
	
	public ConfigurationMeduimDialog(Shell parent) {
		super(parent);
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		((GridLayout)parent.getLayout()).marginLeft=2;
		((GridLayout)parent.getLayout()).marginRight=2;
		((GridLayout)parent.getLayout()).marginTop=2;
		((GridLayout)parent.getLayout()).marginBottom=2;
		getConfigurations();
	}
	
	protected void centerOnScreen(Shell shell) {
		shell.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - shell.getSize().x/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - shell.getSize().y/2);
	}
	
	protected Composite createAdditionalComposite(Composite parent){
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout)comp.getLayout()).marginRight=-3;
		return comp;
	}
	
	protected Composite createComboComposite(Composite parent){
		Composite comboComposite = new Composite(parent, SWT.NONE);
		comboComposite.setLayout(new GridLayout(6, false));
		comboComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout)comboComposite.getLayout()).marginLeft=-5;
		((GridLayout)comboComposite.getLayout()).marginRight=-10;
		((GridLayout)comboComposite.getLayout()).marginTop=-5;
		((GridLayout)comboComposite.getLayout()).marginBottom=-5;
		return comboComposite;
	}
	
	protected Map<String, Composite> createGroups(Configuration configuration, Composite parent){
		Map<String, Composite> groups = new HashMap<String, Composite>();
		
		for (Field field:getViewFields(configuration)){
			ConfigurationAnnotation annotation = field.getAnnotation(ConfigurationAnnotation.class);
			if (annotation == null) continue;
			
			String type = annotation.type();
			if (groups.containsKey(type)) continue;

			groups.put(type, createGroupComposite(parent, type));
		}
		
		return groups;
	}
	
	private Composite createGroupComposite(Composite parent, String type){
		if (type.equalsIgnoreCase("Other Options")){
			Group group = new Group(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			group.setLayout(new GridLayout(3, false));
			group.setText(type);
			
//			Composite composite = new Composite(group, SWT.NONE);
//			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
//			composite.setLayout(new GridLayout(3, false));
//			return composite;
			return group;
		} else {
			Group group = new Group(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			group.setLayout(new GridLayout(2, false));
			group.setText(type);
			return group;
		}
	}
	
	protected List<Field> getViewFields(Configuration configuration){
		List<Field> fields = new ArrayList<Field>();
		for (Field field:configuration.getFields()){
			ConfigurationAnnotation annotation = field.getAnnotation(ConfigurationAnnotation.class);
			if (annotation == null) continue;
			fields.add(field);
		}
		
		return fields;
	}
	
	protected void createStatusControls(Shell parent){
		Composite statusComposite = new Composite(parent, SWT.NONE);
		statusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statusComposite.setLayout(new GridLayout(4, false));
		((GridData)statusComposite.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridLayout)statusComposite.getLayout()).marginTop=-5;
		((GridLayout)statusComposite.getLayout()).marginRight=-5;
		((GridLayout)statusComposite.getLayout()).marginBottom=-5;
		
		status = new Label(statusComposite, SWT.NONE);
		status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		
		Composite emptyComposite = new Composite(statusComposite, SWT.NONE);
		emptyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		emptyComposite.setLayout(new GridLayout(1, false));
		((GridLayout)emptyComposite.getLayout()).marginTop=-35;
		
		save = new Button(statusComposite, SWT.NONE);
		save.setText("Save");
		save.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				Configuration configuration = getConfiguration().save();
				if (action()) {
					setConfiguration(configuration);
					parent.close();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		Button cancel = new Button(statusComposite, SWT.NONE);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				parent.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	protected Configuration getConfiguration(){
		return (Configuration)combo.getData(combo.getText());
	}
	
	protected List<Configuration> getConfigurations(){
		long lastRan = 0;
		List<Configuration> configurations = new ArrayList<Configuration>();
		
		for (File file:new File(Configuration.STORE).listFiles()) {
			Configuration configuration = Configuration.load(file);
			if (configuration.lastRan()>lastRan) {
				lastRan = configuration.lastRan();
				configurations.add(0, configuration);
			} else {
				configurations.add(configuration);
			}
		}
		return configurations;
	}
	
	protected void rename(String oldName, Configuration newConfiguration){
		int index = combo.indexOf(oldName);
		
		combo.setData(oldName, null);
		combo.remove(index);
		combo.add(newConfiguration.getName(), index);
		combo.setData(newConfiguration.getName(), newConfiguration);
		combo.select(index);
	}
	
	protected void setupIcons(){
		try {
			change.setImage(ImageDescriptor.createFromImageData(new ImageData(new FileInputStream(new File("icons/change16.ico")))).createImage());
		} catch (FileNotFoundException e1) {}
		
		
		try {
			add.setImage(ImageDescriptor.createFromImageData(new ImageData(new FileInputStream(new File("icons/plus16.ico")))).createImage());
		} catch (FileNotFoundException e1) {}
		
		try {
			delete.setImage(ImageDescriptor.createFromImageData(new ImageData(new FileInputStream(new File("icons/minus16.ico")))).createImage());
		} catch (FileNotFoundException e1) {}
	}
	
	protected void validate(){
		String result = getConfiguration().validate();
		getParent().setText(result == null?getConfiguration().getTitle():"Not Valid");
		
		save.setEnabled(result == null);
		
		status.setText(result != null?result:"");
		status.pack();
	}
	
	protected boolean confirm(){
		MessageDialog dialog = new MessageDialog(getParent(), "Delete", null, "Do you really want to delete this configuration?", MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, IDialogConstants.YES_ID);
		return dialog.open() == 0;
	}
	
	protected boolean action(){
		MessageDialog dialog = new MessageDialog(getParent(), "Select Action", null, "Configuration is saved. Do you want to run it?", MessageDialog.QUESTION, new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL}, IDialogConstants.YES_ID);
		return dialog.open() == 0;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
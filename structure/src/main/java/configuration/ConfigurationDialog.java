package configuration;

import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Text;

public class ConfigurationDialog extends Dialog {
	private static final int WIDTH = 530;
	
	private Button save;
	private Button delete;
	private Combo combo;
	private Button changeName;
	private Label status;
	
	private Configuration configuration;
	private List<Configuration> configurations;
	private List<Control> options;

	public ConfigurationDialog(Shell parent) {
		super(parent);
	}
	
	public Configuration open() {
		Shell dialog = getParent();
		
		dialog.setText("Configuration");
		dialog.setLayout(new GridLayout(1, false));

		getConfigurations();
		createContent(dialog);
		validate();
		
		dialog.pack();
		dialog.setSize(WIDTH, dialog.getSize().y);
		centerOnScreen(dialog);
		dialog.open();
		
		if (configurations.size() == 0) changeName.notifyListeners(SWT.Selection, null);
		
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		return configuration;
	}
	
	private void centerOnScreen(Shell c) {
	    c.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width/2 - c.getSize().x/2, Toolkit.getDefaultToolkit().getScreenSize().height/2 - c.getSize().y/2);
	}
	
	private void load(){
		for (Configuration configuration:configurations){
			combo.add(configuration.getName());
			combo.setData(configuration.getName(), configuration);
		}
		
		if (configurations.size() == 0) createConfiguration();
		combo.select(0);
	}
	
	private Configuration createConfiguration(){
		Configuration configuration = new Configuration(Configuration.NEW);
		combo.add(configuration.getName());
		combo.setData(configuration.getName(), configuration);
		return configuration;
	}
	
	private void getConfigurations(){
		configurations = new ArrayList<Configuration>();
		for (File file:new File(Configuration.STORE).listFiles()) configurations.add(Configuration.load(file));
	}
	
	private void createContent(Shell shell){
		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite compoComposite = new Composite(parent, SWT.NONE);
		compoComposite.setLayout(new GridLayout(5, false));
		compoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout)compoComposite.getLayout()).marginLeft=-5;
		((GridLayout)compoComposite.getLayout()).marginRight=-5;
		((GridLayout)compoComposite.getLayout()).marginTop=-10;
		((GridLayout)compoComposite.getLayout()).marginBottom=-5;
		
		new Label(compoComposite, SWT.NONE).setText("Configuration:");
		Composite comp = new Composite(compoComposite, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridLayout)comp.getLayout()).marginRight=-3;
		combo = new Combo(comp, SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_BOTH));
		combo.setLayout(new GridLayout(1, false));
		combo.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				for (Control option:options){
					Object value = getConfiguration().getFieldValue((Field)option.getData());
					if (value == null) value = "";
					
					if (option instanceof Text){
						((Text)option).setText(value.toString());
					} else if (option instanceof Button){
						((Button)option).setSelection(Boolean.parseBoolean(value.toString()));
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		delete = new Button(compoComposite, SWT.PUSH);
		delete.setText("Delete");
		delete.setEnabled(configurations.size() != 0);
		delete.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				Configuration configuration = getConfiguration().delete();
				configurations.remove(configuration);
				combo.setData(configuration.getName(), null);
				combo.remove(combo.indexOf(configuration.getName()));
				
				if (configurations.size() == 0) {
					createConfiguration();
					combo.select(0);
					combo.notifyListeners(SWT.Selection, null);
					changeName.notifyListeners(SWT.Selection, null);
					delete.setEnabled(false);
				}
				
				getConfigurations();
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		changeName = new Button(compoComposite, SWT.PUSH);
		changeName.setText("Change Name");
		changeName.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				Configuration configuration = getConfiguration();
				reload(new ConfigurationDialogChange(new Shell(getParent(), SWT.DIALOG_TRIM | SWT.SYSTEM_MODAL)).open(configuration), configuration);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
				
		load();
		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		populate(parent);
		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createStatus(parent);
	
		combo.notifyListeners(SWT.Selection, null);
	}
	
	private void createStatus(Composite parent){
		Composite statusComposite = new Composite(parent, SWT.NONE);
		statusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statusComposite.setLayout(new GridLayout(3, false));
		((GridData)statusComposite.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridLayout)statusComposite.getLayout()).marginTop=-5;
		((GridLayout)statusComposite.getLayout()).marginBottom=-5;
		
		status = new Label(statusComposite, SWT.NONE);
		status.setLayoutData(new GridData());
		status.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		((GridData)status.getLayoutData()).horizontalAlignment = SWT.LEFT;
		
		Composite emptyComposite = new Composite(statusComposite, SWT.NONE);
		emptyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		emptyComposite.setLayout(new GridLayout(1, false));
		((GridLayout)emptyComposite.getLayout()).marginTop=-35;
		
		save = new Button(statusComposite, SWT.NONE);
		save.setText("Save and Run");
		save.setLayoutData(new GridData());
		((GridData)save.getLayoutData()).horizontalAlignment = SWT.RIGHT;
		save.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				configuration = getConfiguration().save();
				parent.getShell().close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	
	private void validate(){
		String result = getConfiguration().validate();
		getParent().setText(result == null?getConfiguration().getTitle():"Not Valid");
		save.setEnabled(result == null);
		status.setText(result != null?result:"");
		status.pack();
	}
	
	private void reload(String oldName, Configuration newConfiguration){
		combo.setData(oldName, null);
		int index = combo.indexOf(oldName);
		
		combo.remove(index);
		combo.add(newConfiguration.getName(), index);
		combo.setData(newConfiguration.getName(), newConfiguration);
		combo.select(index);
		validate();
	}
	
	private void populate(Composite parent){
		Map<String, Group> groups = createGroups(getConfiguration(), parent);
		options = new ArrayList<Control>();
		
		for (final Field field:getConfiguration().getFields()){
			Group group = groups.get(((ConfigurationAnnotation)field.getAnnotation(ConfigurationAnnotation.class)).type());
			new Label(group, SWT.NONE).setText(((ConfigurationAnnotation)field.getAnnotation(ConfigurationAnnotation.class)).name()+":");
			
			Control control = null;
			if (field.getGenericType().toString().equals("class java.lang.String") || field.getGenericType().toString().equals("int") || field.getGenericType().toString().equals("long")){
				control = new Text(group, SWT.BORDER);
				((Text)control).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				((Text)control).addModifyListener(new ModifyListener(){
					public void modifyText(ModifyEvent e) {
						getConfiguration().setFieldValue(field, ((Text)e.widget).getText());
						validate();
					}
				});
			} else if (field.getGenericType().toString().equals("boolean")){
				control = new Button(group, SWT.CHECK);
				((Button)control).addSelectionListener(new SelectionListener(){
					public void widgetSelected(SelectionEvent e) {
						getConfiguration().setFieldValue(field, ((Button)e.widget).getSelection());
						validate();
					}

					public void widgetDefaultSelected(SelectionEvent e) {}
				});
			}
			
			control.setData(field);
			options.add(control);
		}
	}
	
	private Map<String, Group> createGroups(Configuration configuration, Composite parent){
		Map<String, Group> groups = new HashMap<String, Group>();
		
		for (Field field:configuration.getFields()){
			String type = ((ConfigurationAnnotation)field.getAnnotation(ConfigurationAnnotation.class)).type();
			if (groups.containsKey(type)) continue;
			
			Group group = new Group(parent, SWT.NONE);
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			group.setLayout(new GridLayout(type.equalsIgnoreCase("Other Options")?4:2, false));
			group.setText(type);
			groups.put(type, group);
		}
		
		return groups;
	}
	
	private Configuration getConfiguration(){
		return (Configuration)combo.getData(combo.getText());
	}
}
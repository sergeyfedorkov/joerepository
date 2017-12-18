package configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConfigurationDialog extends ConfigurationMeduimDialog {
	public ConfigurationDialog(Shell parent) {
		super(parent);
	}
	
	public Configuration open() {
		Shell dialog = getParent();
		
		createContent(dialog);
		validate();
		
		dialog.pack();
		dialog.setSize(WIDTH, dialog.getSize().y);
		centerOnScreen(dialog);
		dialog.open();
		
		if (getConfigurations().size() == 0) changeName.notifyListeners(SWT.Selection, null);
		
		Display display = dialog.getDisplay();
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		return configuration;
	}
	
	private void load(){
		for (Configuration configuration:getConfigurations()){
			combo.add(configuration.getName());
			combo.setData(configuration.getName(), configuration);
		}
		
		createConfiguration(combo.getItemCount() == 0);
	}
	
	private void add(){
		Configuration configuration = new Configuration(Configuration.NEW);
		combo.add(configuration.getName(), 0);
		combo.setData(configuration.getName(), configuration);
		delete.setEnabled(false);
	}
	
	private void createConfiguration(boolean add){
		if (add) add();
		
		combo.select(0);
		combo.notifyListeners(SWT.Selection, null);
	}
	
	private void createContent(Shell parent){
		Composite comboComposite = createComboComposite(parent);
		
		new Label(comboComposite, SWT.NONE).setText("Configuration:");
		
		combo = new Combo(createAdditionalComposite(comboComposite), SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_BOTH));
		combo.setLayout(new GridLayout(1, false));
		combo.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				if (options == null) return;
				delete.setEnabled(getConfiguration().saved());
				
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
		
		add = new Button(comboComposite, SWT.PUSH);
		add.setText("Add");
		add.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				createConfiguration(true);
				changeName.notifyListeners(SWT.Selection, null);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		delete = new Button(comboComposite, SWT.PUSH);
		delete.setText("Delete");
		delete.setEnabled(getConfigurations().size() != 0);
		delete.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				if (confirm()) {
					Configuration configuration = getConfiguration().delete();
					combo.setData(configuration.getName(), null);
					combo.remove(combo.indexOf(configuration.getName()));
					
					createConfiguration(combo.getItemCount() == 0);
					validate();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		changeName = new Button(comboComposite, SWT.PUSH);
		changeName.setText("Change Name");
		changeName.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				Configuration configuration = getConfiguration();
				rename(new ConfigurationDialogChange(new Shell(getParent(), SWT.DIALOG_TRIM | SWT.SYSTEM_MODAL)).open(configuration), configuration);
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
				
		load();
		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		populate(parent);
		new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createStatusControls(parent);
		combo.notifyListeners(SWT.Selection, null);
	}
	
	private void populate(Composite parent){
		Map<String, Group> groups = createGroups(getConfiguration(), parent);
		options = new ArrayList<Control>();
		
		for (Field field:getConfiguration().getFields()){
			Group group = groups.get(((ConfigurationAnnotation)field.getAnnotation(ConfigurationAnnotation.class)).type());
			new Label(group, SWT.NONE).setText(((ConfigurationAnnotation)field.getAnnotation(ConfigurationAnnotation.class)).name()+":");
			
			Control control = null;
			if (field.getGenericType().toString().equals("boolean")){
				control = new Button(group, SWT.CHECK);
				((Button)control).addSelectionListener(new SelectionListener(){
					public void widgetSelected(SelectionEvent e) {
						getConfiguration().setFieldValue(field, ((Button)e.widget).getSelection());
						validate();
					}

					public void widgetDefaultSelected(SelectionEvent e) {}
				});
			} else {
				control = new Text(group, SWT.BORDER);
				((Text)control).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				((Text)control).addModifyListener(new ModifyListener(){
					public void modifyText(ModifyEvent e) {
						getConfiguration().setFieldValue(field, ((Text)e.widget).getText());
						validate();
					}
				});
			}
			
			control.setData(field);
			options.add(control);
		}
	}
}
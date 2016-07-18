package gui.dialog;

import gui.ImageIconProxy;
import model.Project;
import model.Route;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by joseph on 16/07/16.
 */
public class ProjectSettingsDialog extends JDialog
{
	private static final String[] settingsSections = {"Project", "User defined properties"};
	private JList<String> valueList = new JList<String>();

	private Route rootRoutes;
	private Frame parent;
	private boolean changeMade = false;
	private JList<String> settingsSectionList;
	private CardLayout cardLayout;
	private JPanel settingsPanel;
	private JButton exitButton = new JButton("Close project settings window");
	private JButton addPropBtn = new JButton("Add property", ImageIconProxy.getIcon("add"));
	private JButton editPropBtn = new JButton("Rename property", ImageIconProxy.getIcon("edit"));
	private JButton delPropBtn = new JButton("Delete property", ImageIconProxy.getIcon("del"));
	private JComboBox<String> propList;
	private JCheckBox checkBoxDisallowNewValues = new JCheckBox("Disallow new values");
	private JCheckBox checkBoxMemorizeNewValue = new JCheckBox("Memorize new values");
	private JLabel defaultPropValLbl = new JLabel("Nothing to display");
	private JButton changeDefaultPropValueBtn = new JButton("Change default value", ImageIconProxy.getIcon("edit"));
	private JButton addValueBtn = new JButton("Add value", ImageIconProxy.getIcon("add"));
	private JButton renameValueBtn = new JButton("Rename value", ImageIconProxy.getIcon("edit"));
	private JButton deleteValueBtn = new JButton("Delete", ImageIconProxy.getIcon("del"));

	public ProjectSettingsDialog(Frame owner, Route rootRoutes)
	{
		super(owner, "Project settings", true);
		this.rootRoutes = rootRoutes;

		parent = owner;
		buildUI();

		pack();
		setMinimumSize(getSize());
		//setLocationRelativeTo(parent);
	}

	private void buildUI()
	{
		//settings categories
		JPanel mainPanel = new JPanel(new BorderLayout());
		settingsSectionList = new JList<String>(settingsSections);
		settingsSectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		settingsSectionList.setMinimumSize(new Dimension(100, 10));

		mainPanel.add(new JScrollPane(settingsSectionList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.WEST);

		//settings themselves
		cardLayout = new CardLayout();
		settingsPanel = new JPanel(cardLayout);

		//Project settings
		JPanel panel = new JPanel();
		panel.add(new JLabel(settingsSections[0]));
		settingsPanel.add(panel, settingsSections[0]);

		//user defined properties
		settingsPanel.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, buildUserDefinedPropertiesPanel(), buildUserDefinedPropertyValuesPanel()), settingsSections[1]);


		mainPanel.add(settingsPanel, BorderLayout.CENTER);

		panel = new JPanel();
		panel.add(exitButton);
		mainPanel.add(panel, BorderLayout.SOUTH);


		settingsSectionList.setSelectedIndex(1);
		cardLayout.show(settingsPanel, settingsSections[1]);
		setContentPane(mainPanel);

		addListeners();
		if(Project.getActiveProject().getUserRoutesPropertiesNames().length != 0)
		{
			propList.setSelectedIndex(0);
		}
	}

	private Box buildUserDefinedPropertiesPanel()
	{
		Box mainPanel = Box.createVerticalBox();

		JLabel headLbl = new JLabel(settingsSections[1] + ":");
		headLbl.setAlignmentX(CENTER_ALIGNMENT);
		mainPanel.add(headLbl);

		Box vBox = Box.createVerticalBox();
		vBox.setAlignmentX(CENTER_ALIGNMENT);
		addPropBtn.setMaximumSize(new Dimension(208,34));
		addPropBtn.setAlignmentX(CENTER_ALIGNMENT);
		editPropBtn.setMaximumSize(new Dimension(208,34));
		editPropBtn.setAlignmentX(CENTER_ALIGNMENT);
		delPropBtn.setMaximumSize(new Dimension(208,34));
		delPropBtn.setAlignmentX(CENTER_ALIGNMENT);

		vBox.add(addPropBtn);
		vBox.add(editPropBtn);
		vBox.add(delPropBtn);
		propList = new JComboBox<String>(Project.getActiveProject().getUserRoutesPropertiesNames());
		propList.setMaximumSize(new Dimension(99999999,34));
		vBox.add(propList);

		mainPanel.add(vBox);

		vBox = Box.createVerticalBox();
		vBox.setAlignmentX(CENTER_ALIGNMENT);
		vBox.add(checkBoxDisallowNewValues);
		vBox.add(checkBoxMemorizeNewValue);
		vBox.add(new JLabel("Default value:"));
		vBox.add(defaultPropValLbl);

		mainPanel.add(vBox);

		vBox = Box.createVerticalBox();
		vBox.setAlignmentX(CENTER_ALIGNMENT);
		changeDefaultPropValueBtn.setAlignmentX(CENTER_ALIGNMENT);
		changeDefaultPropValueBtn.setMaximumSize(new Dimension(208,34));

		vBox.add(changeDefaultPropValueBtn);

		mainPanel.add(vBox);

		editPropBtn.setEnabled(false);
		delPropBtn.setEnabled(false);
		checkBoxDisallowNewValues.setEnabled(false);
		checkBoxMemorizeNewValue.setEnabled(false);
		changeDefaultPropValueBtn.setEnabled(false);

		return mainPanel;
	}

	private Box buildUserDefinedPropertyValuesPanel()
	{
		Box mainPanel = Box.createVerticalBox();
		JLabel headLabel = new JLabel("Available values:");
		headLabel.setAlignmentX(CENTER_ALIGNMENT);

		mainPanel.add(headLabel);
		mainPanel.add(new JScrollPane(valueList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		addValueBtn.setAlignmentX(CENTER_ALIGNMENT);
		addValueBtn.setMaximumSize(new Dimension(208,34));
		renameValueBtn.setAlignmentX(CENTER_ALIGNMENT);
		renameValueBtn.setMaximumSize(new Dimension(208,34));
		deleteValueBtn.setAlignmentX(CENTER_ALIGNMENT);
		deleteValueBtn.setMaximumSize(new Dimension(208,34));

		mainPanel.add(addValueBtn);
		mainPanel.add(renameValueBtn);
		mainPanel.add(deleteValueBtn);

		valueList.setEnabled(false);
		addValueBtn.setEnabled(false);
		renameValueBtn.setEnabled(false);
		deleteValueBtn.setEnabled(false);

		return mainPanel;
	}

	private void addListeners()
	{
		//settings selection
		settingsSectionList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent listSelectionEvent)
			{
				if(settingsSectionList.getSelectedIndex() != -1)
				{
					cardLayout.show(settingsPanel, settingsSectionList.getSelectedValue());
				}
			}
		});

		//exit dialog window
		exitButton.addActionListener(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				setVisible(false);
			}
		});

		//project settings

		//user defined properties
		addPropBtn.addActionListener(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String newProp = JOptionPane.showInputDialog(null, "Enter new property name:", "Add new property", JOptionPane.PLAIN_MESSAGE);
				if(newProp != null)
				{
					Project project = Project.getActiveProject();
					if(project.getUserRouteProperty(newProp) != null)
					{
						JOptionPane.showMessageDialog(null, "This property already exists.", "Cannot add property", JOptionPane.ERROR_MESSAGE);
						return;
					}

					String defaultValue = JOptionPane.showInputDialog(null, "Enter default value for property " + newProp + " :", "Add new property", JOptionPane.PLAIN_MESSAGE);
					if(defaultValue == null)
					{
						return;
					}

					project.addUserRouteProperty(newProp, defaultValue);
					rootRoutes.addUserProperty(newProp, defaultValue);
					propList.setModel(new DefaultComboBoxModel<String>(project.getUserRoutesPropertiesNames()));
					propList.setSelectedItem(newProp);
				}
			}
		});

		editPropBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String oldName = (String) propList.getSelectedItem();
				String newName = (String) JOptionPane.showInputDialog(null, "Enter new name for " + oldName + " property:"
						, "Rename property", JOptionPane.PLAIN_MESSAGE, null, null, oldName);
				if(newName != null);
				{
					Project project = Project.getActiveProject();
					if(project.getUserRouteProperty(newName) != null)
					{
						JOptionPane.showMessageDialog(null, "A property with this name already exists.", "Cannot rename property", JOptionPane.ERROR_MESSAGE);
						return;
					}

					String defaultValue = project.getUserRouteProperty(oldName).getDefaultValue();
					project.removeUserRouteProperty(oldName);
					project.addUserRouteProperty(newName, defaultValue);

					rootRoutes.renameUserProperty(oldName, newName);
					propList.setModel(new DefaultComboBoxModel<String>(project.getUserRoutesPropertiesNames()));
					propList.setSelectedItem(newName);
				}
			}
		});

		delPropBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String propName = (String) propList.getSelectedItem();
				if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
						"Are you sure you want to delete the following property?\n" + propName,
						"Delete property", JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE))
				{
					Project project = Project.getActiveProject();
					project.removeUserRouteProperty(propName);

					rootRoutes.removeUserProperty(propName);
					propList.setModel(new DefaultComboBoxModel<String>(project.getUserRoutesPropertiesNames()));

					if(project.getUserRoutesPropertiesNames().length == 0)
					{
						propList.setSelectedIndex(-1);
					}
				}
			}
		});
//Todo: def val = "" empty string
		propList.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(propList.getSelectedIndex() != -1)
				{
					editPropBtn.setEnabled(true);
					delPropBtn.setEnabled(true);
					checkBoxDisallowNewValues.setEnabled(true);


					Project.UserDefinedRouteProperty userDefinedRouteProperty
							= Project.getActiveProject().getUserRouteProperty((String) propList.getSelectedItem());
					defaultPropValLbl.setText(userDefinedRouteProperty.getDefaultValue());
					changeDefaultPropValueBtn.setEnabled(true);

					if(userDefinedRouteProperty.isNewValuesDisabled())
					{
						checkBoxDisallowNewValues.setSelected(true);
						checkBoxMemorizeNewValue.setSelected(false);
						checkBoxMemorizeNewValue.setEnabled(false);
					}
					else
					{
						checkBoxDisallowNewValues.setSelected(false);
						checkBoxMemorizeNewValue.setEnabled(true);
						if(userDefinedRouteProperty.isValuesMemorized())
						{
							checkBoxMemorizeNewValue.setSelected(true);
						}
						else
						{
							checkBoxMemorizeNewValue.setEnabled(false);
						}
					}

					valueList.setListData(userDefinedRouteProperty.getValues());
				}
				else
				{
					editPropBtn.setEnabled(false);
					delPropBtn.setEnabled(false);
					checkBoxDisallowNewValues.setEnabled(false);
					checkBoxMemorizeNewValue.setEnabled(false);
					changeDefaultPropValueBtn.setEnabled(false);
					valueList.setEnabled(false);
					addValueBtn.setEnabled(false);
					renameValueBtn.setEnabled(false);
					deleteValueBtn.setEnabled(false);

					checkBoxDisallowNewValues.setSelected(false);
					checkBoxMemorizeNewValue.setSelected(false);
					valueList.setListData(new String[0]);

					defaultPropValLbl.setText("Nothing to display");
				}
			}
		});

		checkBoxDisallowNewValues.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(checkBoxDisallowNewValues.isSelected())
				{
					checkBoxMemorizeNewValue.setEnabled(false);
					Project.getActiveProject().getUserRouteProperty((String) propList.getSelectedItem()).setNewValuesDisabled(true);
				}
				else
				{
					checkBoxMemorizeNewValue.setEnabled(true);
					Project.getActiveProject().getUserRouteProperty((String) propList.getSelectedItem()).setNewValuesDisabled(false);
				}
			}
		});

		checkBoxMemorizeNewValue.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(checkBoxMemorizeNewValue.isSelected())
				{
					Project.getActiveProject().getUserRouteProperty((String) propList.getSelectedItem()).setValuesMemorized(true);
				}
				else
				{
					Project.getActiveProject().getUserRouteProperty((String) propList.getSelectedItem()).setValuesMemorized(false);
				}
			}
		});

		changeDefaultPropValueBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String propName = (String) propList.getSelectedItem();
				Project.UserDefinedRouteProperty userDefinedRouteProperty =  Project.getActiveProject().getUserRouteProperty(propName);
				String oldDef = userDefinedRouteProperty.getDefaultValue();

				String newDef = (String) JOptionPane.showInputDialog(null, "Change default value of property: " + propName + "\nfrom: " + oldDef + "\nto:",
						"Change default value", JOptionPane.PLAIN_MESSAGE, null, userDefinedRouteProperty.getValues(), oldDef);

				if(newDef != null)
				{
					if(newDef.equals(oldDef))
					{
						JOptionPane.showMessageDialog(null, "Please choose a different value from the previous.");
						return;
					}

					userDefinedRouteProperty.setDefaultValue(newDef);
					defaultPropValLbl.setText(newDef);

					if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Replace the old default value with the new one in all routes?", "Update the route",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
					{
						rootRoutes.changeUserPropertyValue(propName, oldDef, newDef);
					}
				}
			}
		});
	}

	public boolean display()
	{
		setVisible(true);
		return changeMade;
	}
}
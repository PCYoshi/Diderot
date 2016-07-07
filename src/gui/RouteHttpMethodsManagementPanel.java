package gui;

import model.HttpMethod;
import model.Route;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Created by joseph on 06/07/16.
 */
public class RouteHttpMethodsManagementPanel extends JPanel implements ActionListener
{
	private JButton addMethodBtn = new JButton("Add http method", ImageIconProxy.getIcon("rsc/plus.png"));
	private JButton updMethodBtn = new JButton("Change http method", ImageIconProxy.getIcon("rsc/edit.png"));
	private JButton delMethodBtn = new JButton("Delete http method", ImageIconProxy.getIcon("rsc/del.png"));

	private JTabbedPane methodsTabbedPanel = new JTabbedPane();
	private Route route;

	public RouteHttpMethodsManagementPanel(Route route)
	{
		super(new BorderLayout());
		this.route = route;

		for(Map.Entry<String, HttpMethod> entry: route.getHttpMethods().entrySet())
		{
			methodsTabbedPanel.addTab(entry.getKey(), new JLabel(entry.getKey()));
		}

		buildUI();
	}

	private void buildUI()
	{
		JPanel topPanel = new JPanel(new BorderLayout());

		final JTextArea descriptionTextArea = new JTextArea(route.getDescription());
		JScrollPane scrollPanel = new JScrollPane(descriptionTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		topPanel.add(new JLabel("Global route description:"), BorderLayout.NORTH);
		topPanel.add(scrollPanel, BorderLayout.CENTER);


		JPanel bottomPanel = new JPanel(new BorderLayout());

		JPanel btnPanel = new JPanel();
		btnPanel.add(addMethodBtn);
		btnPanel.add(updMethodBtn);
		btnPanel.add(delMethodBtn);

		bottomPanel.add(btnPanel, BorderLayout.NORTH);
		bottomPanel.add(methodsTabbedPanel, BorderLayout.CENTER);

		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
		add(mainPanel);

		addMethodBtn.addActionListener(this);
		updMethodBtn.addActionListener(this);
		delMethodBtn.addActionListener(this);

		descriptionTextArea.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				route.setDescription(descriptionTextArea.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				insertUpdate(documentEvent);
			}

			@Override
			/**
			 * Not implemented
			 */
			public void changedUpdate(DocumentEvent documentEvent)
			{
				//This not the implementation you are looking for.
			}
		});

		if(methodsTabbedPanel.getTabCount() == 0)
		{
			delMethodBtn.setEnabled(false);
			updMethodBtn.setEnabled(false);
		}
	}

	private void setEnabledButton(boolean enabled)
	{
		updMethodBtn.setEnabled(enabled);
		delMethodBtn.setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if(actionEvent.getSource() == addMethodBtn)
		{
			actionAddMethod();
		}
		else if(actionEvent.getSource() == updMethodBtn)
		{
			actionUpdateMethod();
		}
		else if(actionEvent.getSource() == delMethodBtn)
		{
			actionRemoveMethod();
		}
	}

	private void actionAddMethod()
	{
		String methodToAdd = JOptionPane.showInputDialog(this, "Which HTTP method would you add?", "Add HTTP method", JOptionPane.QUESTION_MESSAGE);
		if(methodToAdd != null)
		{
			methodToAdd = methodToAdd.toUpperCase();
			if(route.getHttpMethods().containsKey(methodToAdd))
			{
				JOptionPane.showMessageDialog(this, "This method already exists.", "Cannot add HTTP method", JOptionPane.WARNING_MESSAGE);
				return;
			}

			route.getHttpMethods().put(methodToAdd, new HttpMethod());
			methodsTabbedPanel.add(methodToAdd, new JLabel(methodToAdd));
			setEnabledButton(true);
			methodsTabbedPanel.setSelectedIndex(methodsTabbedPanel.getTabCount()-1);
		}
	}

	private void actionUpdateMethod()
	{
		int currentTab = methodsTabbedPanel.getSelectedIndex();
		String methodToRename = methodsTabbedPanel.getTitleAt(currentTab);

		String methodRenamed = (String) JOptionPane.showInputDialog(this, "Enter new name for http method: " + methodToRename, "Change http method", JOptionPane.QUESTION_MESSAGE, null, null, methodToRename);
		if(methodRenamed != null)
		{
			methodRenamed = methodRenamed.toUpperCase();
			if(route.getHttpMethods().containsKey(methodRenamed))
			{
				JOptionPane.showMessageDialog(this, "This method already exists.", "Cannot change http method", JOptionPane.WARNING_MESSAGE);
				return;
			}

			HttpMethod httpMethod = route.getHttpMethods().remove(methodToRename);
			route.getHttpMethods().put(methodRenamed, httpMethod);

			methodsTabbedPanel.setTitleAt(methodsTabbedPanel.getSelectedIndex(), methodRenamed);
		}
	}

	private void actionRemoveMethod()
	{
		int currentTab = methodsTabbedPanel.getSelectedIndex();
		String methodToRemove = methodsTabbedPanel.getTitleAt(currentTab);

		if(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
				"Are you sure you want to remove the following HTTP method?\n" + methodToRemove,
				"Remove HTTP Method ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE))
		{
			route.getHttpMethods().remove(methodToRemove);
			methodsTabbedPanel.remove(currentTab);

			if(methodsTabbedPanel.getTabCount() == 0)
			{
				setEnabledButton(false);
			}
		}
	}
}
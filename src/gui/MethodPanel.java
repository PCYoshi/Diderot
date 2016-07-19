package gui;

import model.HttpMethod;
import model.Project;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 * Created by joseph on 10/07/16.
 */
public class MethodPanel extends JPanel implements Scrollable
{
	private HttpMethod httpMethod;
	private JTextArea description = new JTextArea();
	private AbstractAction addParamAction, delParamAction;
	private JButton addParamBtn = new JButton(),
			delParamBtn = new JButton();
	private JTable paramTable;

	public MethodPanel(HttpMethod httpMethod)
	{
		super();
		this.httpMethod = httpMethod;
		description.setText(httpMethod.getDescription());
		buildUI();
	}

	private void buildUI()
	{
		Border labelBorder = BorderFactory.createEmptyBorder(5, 2, 0, 2);
		Border componentBorder = BorderFactory.createMatteBorder(0, 4, 0, 4, getBackground());
		Border textAreaBorder = BorderFactory.createLineBorder(getBackground().darker(), 1);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel label = new JLabel("Method description:");
		label.setBorder(labelBorder);
		label.setAlignmentX(LEFT_ALIGNMENT);
		add(label);

		description.setAlignmentX(LEFT_ALIGNMENT);
		description.setBorder(BorderFactory.createCompoundBorder(componentBorder, textAreaBorder));
		description.setLineWrap(true);
		add(description);

		JPanel labelButtonPanel = new JPanel(new BorderLayout());
		labelButtonPanel.setBorder(labelBorder);

		label = new JLabel("Parameters:");
		labelButtonPanel.add(label, BorderLayout.WEST);

		JPanel paramBtnPanel = new JPanel();
		paramBtnPanel.add(addParamBtn);
		paramBtnPanel.add(delParamBtn);
		labelButtonPanel.add(paramBtnPanel, BorderLayout.CENTER);

		paramTable = new JTable(httpMethod);
		paramTable.setAutoCreateRowSorter(true);
		TableColumnModel colMod = paramTable.getColumnModel();
		colMod.getColumn(0).setPreferredWidth(100);
		colMod.getColumn(0).setMaxWidth(400);
		colMod.getColumn(1).setMaxWidth(100);
		colMod.getColumn(2).setPreferredWidth(400);

		Box box = Box.createVerticalBox();
		box.setBorder(componentBorder);
		box.setAlignmentX(LEFT_ALIGNMENT);
		box.add(labelButtonPanel);
		box.add(paramTable.getTableHeader());
		box.add(paramTable);
		add(box);

		label = new JLabel("User defined properties:");
		label.setBorder(labelBorder);
		label.setAlignmentX(LEFT_ALIGNMENT);
		add(label);

		final String[] userDefinedProperties = Project.getActiveProject().getUserRoutesPropertiesNames();
		if(userDefinedProperties.length == 0)
		{
			label = new JLabel("No user defined properties created. To create one, go into project settings.");
			JPanel panel = new JPanel(new BorderLayout());
			panel.setAlignmentX(LEFT_ALIGNMENT);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(label, BorderLayout.CENTER);
			add(panel);
		}
		else
		{
			final JPanel panel = new JPanel(new GridLayout(userDefinedProperties.length, 2, 15, 5));
			panel.setBorder(componentBorder);
			panel.setAlignmentX(LEFT_ALIGNMENT);

			for(final String property : userDefinedProperties)
			{
				JLabel propLabel = new JLabel(property + ":");
				propLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				propLabel.setBackground(Color.RED);
				panel.add(propLabel);

				Project.UserDefinedRouteProperty userProperty = Project.getActiveProject().getUserRouteProperty(property);
				if(userProperty.isNewValuesDisabled())
				{
					JComboBox comboBox = new JComboBox<String>(userProperty.getValues());
					panel.add(comboBox);

					comboBox.setSelectedItem(httpMethod.getUserPropertyValue(property));
					comboBox.addFocusListener(new UserDefinedPropertyEditedValueListener(property));
				}
				else
				{
					JTextField valueText = new JTextField(httpMethod.getUserPropertyValue(property));
					panel.add(valueText);

					valueText.addFocusListener(new UserDefinedPropertyEditedValueListener(property));
					valueText.addKeyListener(new UserDefinedPropertyAutocompletionListener(userProperty));

					if(userProperty.isValuesMemorized())
					{
						valueText.addFocusListener(new UserDefinedPropertyNewValueListener(userProperty));
					}
				}
			}

			add(panel);
		}

		addListener();
	}

	private void addListener()
	{
		description.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				httpMethod.setDescription(description.getText());
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

		paramTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent listSelectionEvent)
			{
				if(paramTable.getSelectedRowCount() == 0)
				{
					delParamAction.setEnabled(false);
				}
				else
				{
					delParamAction.setEnabled(true);
				}
			}
		});

		addParamAction = new AbstractAction("Add parameter", ImageIconProxy.getIcon("add"))
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				httpMethod.addParameter(httpMethod.getUniqueParameterName());
			}
		};
		addParamBtn.setAction(addParamAction);

		delParamAction = new AbstractAction("Delete selected parameter(s)", ImageIconProxy.getIcon("del"))
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(null,
						"Are you sure you want to delete the selected parameter(s)?\n",
						"Delete parameter(s)", JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE))
				{
					return;
				}

				int[] selectedRowsIndex = paramTable.getSelectedRows();
				int[] correctIndex = new int[selectedRowsIndex.length];

				for(int i = 0; i < selectedRowsIndex.length; i++)
				{
					correctIndex[i] = paramTable.getRowSorter().convertRowIndexToModel(selectedRowsIndex[i]);
				}

				Arrays.sort(correctIndex);

				for(int i = selectedRowsIndex.length; i > 0; i--)
				{
					httpMethod.removeParameter(correctIndex[i - 1]);
				}
			}
		};
		delParamAction.setEnabled(false);
		delParamBtn.setAction(delParamAction);
	}


	@Override
	public Dimension getPreferredScrollableViewportSize()
	{
		return null;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle rectangle, int i, int i1)
	{
		return 10;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle rectangle, int i, int i1)
	{
		return 10;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	private static class UserDefinedPropertyNewValueListener extends FocusAdapter
	{
		private Project.UserDefinedRouteProperty userProperty;

		public UserDefinedPropertyNewValueListener(Project.UserDefinedRouteProperty userProperty)
		{
			this.userProperty = userProperty;
		}

		@Override
		public void focusLost(FocusEvent e)
		{
			JTextField textField = (JTextField) e.getSource();
			if(!textField.getText().isEmpty())
			{
				userProperty.add(textField.getText());
			}
		}
	}

	private static class UserDefinedPropertyAutocompletionListener extends KeyAdapter
	{
		private final Project.UserDefinedRouteProperty userProperty;

		public UserDefinedPropertyAutocompletionListener(Project.UserDefinedRouteProperty userProperty)
		{
			this.userProperty = userProperty;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			final JTextField textField = (JTextField) e.getSource();

			if(e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU || e.getKeyCode() == KeyEvent.VK_SPACE || (e.getModifiers() & KeyEvent.CTRL_DOWN_MASK) != 0)
			{
				JPopupMenu popupMenu = new JPopupMenu();

				boolean empty = true;
				for(final String s : userProperty.getValues())
				{
					if(s.toLowerCase().startsWith(textField.getText().toLowerCase()))
					{
						empty = false;
						popupMenu.add(new AbstractAction(s)
						{
							@Override
							public void actionPerformed(ActionEvent actionEvent)
							{
								textField.setText(s);
							}
						});
					}
				}
				if(!empty)
				{
					popupMenu.show(textField, 10, textField.getSize().height - 2);
				}
			}
		}
	}

	private class UserDefinedPropertyEditedValueListener extends FocusAdapter
	{
		private String property;

		public UserDefinedPropertyEditedValueListener(String property)
		{
			this.property = property;
		}

		@Override
		public void focusLost(FocusEvent e)
		{
			Object src = e.getSource();
			if(src instanceof JTextField)
			{
				JTextField textField = (JTextField) src;
				httpMethod.setUserProperty(property, textField.getText());
			}
			else if(src instanceof JComboBox)
			{
				JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
				httpMethod.setUserProperty(property, (String) comboBox.getSelectedItem());
			}
		}
	}
}
package gui;

import model.Route;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.util.Vector;

/**
 * Created by joseph on 04/07/16.
 */
public class RoutesTreePanel extends JTree implements TreeExpansionListener
{
	private Vector<TreePath> expandedNodes = new Vector<TreePath>();

	public RoutesTreePanel(Route rootRoutes)
	{
		super();
		addTreeExpansionListener(this);
		setModel(rootRoutes);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	/*public void rebuildTree()
	{
		//DefaultMutableTreeNode root = new DefaultMutableTreeNode(projectName);
		removeTreeExpansionListener(this);
		//fillTree(root, rootRoutes);
		//setModel(new DefaultTreeModel(root));

		for(TreePath tp : expandedNodes)
		{
			expandPath(tp);
			System.out.println("expanding : " + tp);
		}
		addTreeExpansionListener(this);
	}

	private void fillTree(DefaultMutableTreeNode node, Route route)
	{
		for(Map.Entry<String, Route> entry : route.getSubRoutes().entrySet())
		{
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(entry.getKey());
			fillTree(newNode, entry.getValue());
			node.add(newNode);
		}
	}*/

	@Override
	public void treeExpanded(TreeExpansionEvent treeExpansionEvent)
	{
		expandedNodes.add(treeExpansionEvent.getPath());
		System.out.println("expand : " + treeExpansionEvent.getPath());
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent treeExpansionEvent)
	{
		expandedNodes.remove(treeExpansionEvent.getPath());//not perfect: if user delete subroutes, they will still remain in this vector.
		System.out.println("colapse : " + treeExpansionEvent.getPath());
	}

	public void updateModel()
	{
		TreeModel model = getModel();
		Route root = (Route) model.getRoot();
		setModel(new Route(""));
		setModel(root);

		removeTreeExpansionListener(this);
		for(TreePath tp : expandedNodes)
		{
			expandPath(tp);
			System.out.println("expanding : " + tp);
		}
		addTreeExpansionListener(this);
	}
}
package plugin.importer;

import model.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import plugin.OperationNameIcon;
import plugin.PluginsSettings;
import plugin.exporter.DefaultDiderotProjectExporter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import static plugin.importer.DiderotProjectImporter.decodeNewLine;

/**
 * The type Default diderot project importer.
 * @author joseph
 */
public class DefaultDiderotProjectImporter extends DefaultHandler implements DiderotProjectImporter
{
	private Route rootRoute;
	private Project project;
	private JFrame parent;

	static private HashMap<String, OperationNameIcon> availableOperations = new HashMap<>();

	static
	{
		availableOperations.put("New project", new OperationNameIcon("createProject", "new"));
		availableOperations.put("Open project", new OperationNameIcon("importProject", "open"));
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public HashMap<String, OperationNameIcon> getAvailableImportingOperations()
	{
		return availableOperations;
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public String getPluginName()
	{
		return "Diderot default project importer";
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public String getPluginAuthor()
	{
		return "Joseph Caillet";
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public String getPluginContactInformation()
	{
		return "https://github.com/JosephCaillet/Diderot";
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public String getPluginVersion()
	{
		return "1.0";
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public String getPluginDescription()
	{
		return "This default plugin is used to provide diderot project opening.";
	}

	/**
	 * {@inheritDoc}
	 * @param rootRoute {@inheritDoc}
	 * @param project   {@inheritDoc}
	 */
	@Override
	public void setDiderotData(Route rootRoute, Project project)
	{
		this.rootRoute = rootRoute;
		this.project = project;
	}

	/**
	 * {@inheritDoc}
	 * @param parent {@inheritDoc}
	 */
	@Override
	public void setParentFrame(JFrame parent)
	{
		this.parent = parent;
	}

	/**
	 * Create and initialize a new project.
	 */
	public void createProject()
	{
		project.clear();
		project.addResponseFormat("HTML");
		project.addResponseFormat("JSON");
		project.addResponseFormat("XML");
		project.addResponseFormat("CSS");
		project.addResponseFormat("CSV");
		project.addResponseFormat("Plain text");
		project.setDefaultResponseFormat("JSON");

		project.addUserRouteProperty("Status", "implemented");
		Project.UserDefinedRouteProperty udp = project.getUserRouteProperty("Status");
		udp.add("beta");
		udp.add("depreciated");
		udp.setNewValuesDisabled(true);

		project.setName("New Project");
		project.setDomain("newProject.com");
		project.setAuthors(System.getProperty("user.name"));
		project.setAuthors(System.getProperty("user.name"));

		rootRoute.clear();
		rootRoute.rename("newProject.com");

		PluginsSettings.clear();
	}

	/**
	 * Import a project.
	 */
	public void importProject()
	{
		//Todo: remove "." to start in home directory of user
		JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setFileFilter(new FileNameExtensionFilter("Diderot project file", "dip"));
		if(JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(parent))
		{
			return;
		}

		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Element diderotProject = documentBuilder.parse(fileChooser.getSelectedFile()).getDocumentElement();

			project.clear();
			rootRoute.clear();
			PluginsSettings.clear();

			loadProject(diderotProject);
			loadParameterTypes(diderotProject);
			loadPluginsProperties(diderotProject);
			loadResponsesOutputFormat(diderotProject);
			loadUserDefinedProperties(diderotProject);
			loadRoutes(diderotProject.getElementsByTagName("route").item(0));
			project.setOpenedStatus(true);
			PluginsSettings.setPropertyValue(DefaultDiderotProjectExporter.class.getName() + "projectFileName", fileChooser.getSelectedFile().getAbsolutePath());
		}
		catch(SAXException | IOException | ParserConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Load parameters types .
	 * @param projectElement the project element
	 */
	private void loadParameterTypes(Element projectElement)
	{
		Node types = projectElement.getElementsByTagName("parameterTypes").item(0);
		if(types == null)
		{
			return;
		}

		NodeList nodeList = types.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.TEXT_NODE)
			{
				continue;
			}

			project.addParameterType(node.getAttributes().getNamedItem("name").getTextContent());

			NodeList nodeList2 = node.getChildNodes();
			for(int j = 0; j < nodeList2.getLength(); j++)
			{
				Node node2 = nodeList2.item(j);
				if(node2.getNodeType() == Node.TEXT_NODE)
				{
					continue;
				}

				project.addSubParameterType(node.getAttributes().getNamedItem("name").getTextContent(), node2.getTextContent());
			}
		}
	}

	/**
	 * Load project object.
	 * @param projectElement the project element
	 */
	private void loadProject(Element projectElement)
	{
		project.setName(projectElement.getAttribute("name"));
		project.setCompany(projectElement.getAttribute("company"));
		project.setAuthors(projectElement.getAttribute("authors"));
		project.setContact(projectElement.getAttribute("contact"));
		project.setVersion(projectElement.getAttribute("version"));

		Node description = projectElement.getElementsByTagName("description").item(0).getFirstChild();
		if(description != null)
		{
			project.setDescription(decodeNewLine(description.getTextContent()));
		}
	}

	/**
	 * Load plugins properties.
	 * @param projectElement the project element
	 */
	private void loadPluginsProperties(Element projectElement)
	{
		Node pluginsProperties = projectElement.getElementsByTagName("pluginsProperties").item(0);
		if(pluginsProperties == null)
		{
			return;
		}

		NodeList nodeList = pluginsProperties.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.TEXT_NODE)
			{
				continue;
			}

			PluginsSettings.setPropertyValue(node.getAttributes().getNamedItem("property").getTextContent(), node.getTextContent());
		}
	}

	/**
	 * Load responses output format.
	 * @param projectElement the project element
	 */
	private void loadResponsesOutputFormat(Element projectElement)
	{
		//TODO fix typo in: responseOutputFormat
		Node responsesOutputFormat = projectElement.getElementsByTagName("responseOutputFormat").item(0);
		NodeList nodeList = responsesOutputFormat.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			if(nodeList.item(i).getNodeType() == Node.TEXT_NODE)
			{
				continue;
			}
			project.addResponseFormat(nodeList.item(i).getTextContent());
		}
	}

	/**
	 * Load user defined properties.
	 * @param projectElement the project element
	 */
	private void loadUserDefinedProperties(Element projectElement)
	{
		Node userDefinedProperties = projectElement.getElementsByTagName("userDefinedProperties").item(0);
		NodeList nodeList = userDefinedProperties.getChildNodes();

		for(int i = 0; i < nodeList.getLength(); i++)
		{
			if(nodeList.item(i).getNodeType() == Node.TEXT_NODE)
			{
				continue;
			}

			NamedNodeMap propAtr = nodeList.item(i).getAttributes();
			Vector<String> values = new Vector<>();
			String defaultVal = "";

			NodeList valuesList = nodeList.item(i).getChildNodes();
			for(int j = 0; j< valuesList.getLength(); j++)
			{
				Node value = valuesList.item(j);
				if(value.getNodeType() == Node.TEXT_NODE)
				{
					continue;
				}

				String valueText = value.getTextContent();
				if(value.getAttributes().getNamedItem("default") != null)
				{
					defaultVal = valueText;
				}
				values.add(valueText);
			}

			project.addUserRouteProperty(propAtr.getNamedItem("name").getTextContent(), defaultVal);
			Project.UserDefinedRouteProperty userDefinedRouteProperty = project.getUserRouteProperty(propAtr.getNamedItem("name").getTextContent());
			userDefinedRouteProperty.setValuesMemorized(Boolean.parseBoolean(propAtr.getNamedItem("memorize").getTextContent()));
			userDefinedRouteProperty.setNewValuesDisabled((Boolean.parseBoolean(propAtr.getNamedItem("disallow").getTextContent())));
		}
	}

	/**
	 * Initialise routes loading.
	 * @param routeNode the route node
	 */
	private void loadRoutes(Node routeNode)
	{
		String routeName = routeNode.getAttributes().getNamedItem("name").getTextContent();
		project.setDomain(routeName);
		rootRoute.rename(routeName);
		loadRoutes(routeNode, rootRoute);
	}

	/**
	 * Load routes.
	 * @param routeNode the route node
	 * @param route     the route
	 */
	private void loadRoutes(Node routeNode, Route route)
	{
		NodeList routeChildren = routeNode.getChildNodes();
		for(int i = 0; i < routeChildren.getLength(); i++)
		{
			if(routeChildren.item(i).getNodeType() == Node.TEXT_NODE)
			{
				continue;
			}

			String nodeName = routeChildren.item(i).getNodeName();
			if("description".equals(nodeName))
			{
				route.setDescription(decodeNewLine(routeChildren.item(i).getTextContent()));
				if(route.hasUrlParameter())
				{
					route.setUrlParamType(routeNode.getAttributes().getNamedItem("urlParamType").getTextContent());
					route.setUrlParamSubType(routeNode.getAttributes().getNamedItem("urlParamSubType").getTextContent());
					route.setUrlParamDescription(routeNode.getAttributes().getNamedItem("urlParamDescription").getTextContent());
				}
			}
			else if("methods".equals(nodeName))
			{
				NodeList methods = routeChildren.item(i).getChildNodes();
				for(int j = 0; j < methods.getLength(); j++)
				{
					if(methods.item(j).getNodeType() == Node.TEXT_NODE)
					{
						continue;
					}

					String name = methods.item(j).getAttributes().getNamedItem("name").getTextContent();
					HttpMethod newHttpMethod = new HttpMethod();
					route.addHttpMethod(name, newHttpMethod);

					NodeList methodsChildren = methods.item(j).getChildNodes();
					for(int k = 0; k < methodsChildren.getLength(); k++)
					{
						if(methodsChildren.item(k).getNodeType() == Node.TEXT_NODE)
						{
							continue;
						}

						nodeName = methodsChildren.item(k).getNodeName();
						if("description".equals(nodeName))
						{
							newHttpMethod.setDescription(decodeNewLine(methodsChildren.item(k).getTextContent()));
						}
						else if("parameters".equals(nodeName))
						{
							NodeList params = methodsChildren.item(k).getChildNodes();
							for(int l = 0; l < params.getLength(); l++)
							{
								if(params.item(l).getNodeType() == Node.TEXT_NODE)
								{
									continue;
								}

								NamedNodeMap attributes = params.item(l).getAttributes();
								Boolean isRequired = Boolean.valueOf(attributes.getNamedItem("required").getTextContent());
								Parameter newParam = new Parameter(isRequired, attributes.getNamedItem("description").getTextContent());
								newParam.setType(attributes.getNamedItem("type").getTextContent());
								newParam.setSubType(attributes.getNamedItem("subType").getTextContent());
								newParam.setLocation(attributes.getNamedItem("location").getTextContent());
								newHttpMethod.addParameter(attributes.getNamedItem("name").getTextContent(), newParam);
							}
						}
						else if("responses".equals(nodeName))
						{
							NodeList responses = methodsChildren.item(k).getChildNodes();
							for(int l = 0; l < responses.getLength(); l++)
							{
								if(responses.item(l).getNodeType() == Node.TEXT_NODE)
								{
									continue;
								}

								nodeName = responses.item(l).getNodeName();
								for(int m = 0; m < responses.getLength(); m++)
								{
									if(responses.item(m).getNodeType() == Node.TEXT_NODE)
									{
										continue;
									}

									Response response = new Response();
									NamedNodeMap attributes = responses.item(m).getAttributes();
									response.setOutputType(attributes.getNamedItem("outputFormat").getTextContent());


									NodeList responseChildren = responses.item(m).getChildNodes();
									for(int n = 0; n < responseChildren.getLength(); n++)
									{
										if(responseChildren.item(n).getNodeType() == Node.TEXT_NODE)
										{
											continue;
										}

										nodeName = responseChildren.item(n).getNodeName();
										if("description".equals(nodeName))
										{
											response.setDescription(decodeNewLine(responseChildren.item(n).getTextContent()));
										}
										else if("outputSchema".equals(nodeName))
										{
											response.setSchema(decodeNewLine(responseChildren.item(n).getTextContent()));
										}
									}
									newHttpMethod.addResponse(attributes.getNamedItem("name").getTextContent(), response);
								}
							}
						}
						else if("userDefinedProperties".equals(nodeName))
						{
							NodeList values = methodsChildren.item(k).getChildNodes();
							for(int l = 0; l < values.getLength(); l++)
							{
								if(values.item(l).getNodeType() == Node.TEXT_NODE)
								{
									continue;
								}

								String property = values.item(l).getAttributes().getNamedItem("property").getTextContent();
								String value = values.item(l).getTextContent();

								newHttpMethod.setUserProperty(property, value);
							}
						}
					}
				}
			}
			else if("routes".equals(nodeName))
			{
				NodeList childRoutes = routeChildren.item(i).getChildNodes();
				for(int j = 0; j < childRoutes.getLength(); j++)
				{
					if(childRoutes.item(j).getNodeType() == Node.TEXT_NODE)
					{
						continue;
					}

					String name = childRoutes.item(j).getAttributes().getNamedItem("name").getTextContent();
					Route newRoute = new Route(name);
					route.addRoute(name, newRoute);
					loadRoutes(childRoutes.item(j), newRoute);
				}
			}
		}
	}
}
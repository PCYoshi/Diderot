package plugin.exporter;

import gui.dialog.DocumentationExportDialog;
import org.w3c.dom.Document;
import plugin.OperationNameIcon;
import plugin.PluginsSettings;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.util.HashMap;

/**
 * Default Diderot Documentation Exporter, exports project to html using an xslt file.
 * @author joseph
 */
public class DefaultDiderotDocumentationExporter extends DefaultDiderotProjectExporter
{
	static private HashMap<String, OperationNameIcon> availableOperations = new HashMap<>();

	static
	{
		availableOperations.put("Generate documentation", new OperationNameIcon("generateHtmlDocumentation", "exportweb"));
	}

	/**
	 * Copy directory recursively.
	 * @param source      the source
	 * @param destination the destination
	 * @return true if operation successful, false otherwise
	 */
	//method strongly inspired by kayz1's solution here: http://stackoverflow.com/questions/6214703/copy-entire-directory-contents-to-another-directory
	static public boolean copyDirectory(File source, File destination)
	{
		if (source.isDirectory())
		{
			if (!destination.exists() && !destination.mkdirs())
			{
				return false;
			}

			String files[] = source.list();

			for (String file : files)
			{
				File srcFile = new File(source, file);
				File destFile = new File(destination, file);

				if(!copyDirectory(srcFile, destFile))
				{
					return false;
				}
			}
		}
		else
		{
			InputStream in = null;
			OutputStream out = null;

			try
			{
				in = new FileInputStream(source);
				out = new FileOutputStream(destination);

				byte[] buffer = new byte[1024];

				int length;
				while ((length = in.read(buffer)) > 0)
				{
					out.write(buffer, 0, length);
				}
			}
			catch (Exception e)
			{
				try
				{
					in.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
					return false;
				}

				try
				{
					out.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
					return false;
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	public HashMap<String, OperationNameIcon> getAvailableExportingOperations()
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
		return "Diredot default documentation exporter";
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
		return "This default plugin generates web documentation of your routes.";
	}

	/**
	 * Generate html documentation.
	 */
	public void generateHtmlDocumentation()
	{
		String fileName = PluginsSettings.getPropertyValue(getClass().getName() + "documentationFolder");
		if(fileName == null)
		{
			fileName = PluginsSettings.getPropertyValue(DefaultDiderotProjectExporter.class.getName() + "projectFileName", ".");
		}

		String logoLocation = PluginsSettings.getPropertyValue(getClass().getName() + "documentationLogo", ".");
		boolean openBrowser = Boolean.parseBoolean(
				PluginsSettings.getPropertyValue(getClass().getName() + "documentationOpenInBrowser", "true"));

		DocumentationExportDialog exportDialog = new DocumentationExportDialog(parent, fileName, logoLocation, openBrowser);
		if(!exportDialog.display())
		{
			return;
		}

		openBrowser = exportDialog.isOpenInBrowserAsked();
		logoLocation = exportDialog.getLogoLocation();
		fileName = exportDialog.getExportLocation();

		if(!new File(fileName).isDirectory())
		{
			JOptionPane.showMessageDialog(parent,  fileName + " is not a folder.", "Export error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		PluginsSettings.setPropertyValue(getClass().getName() + "documentationFolder", fileName);
		PluginsSettings.setPropertyValue(getClass().getName() + "documentationLogo", logoLocation);
		PluginsSettings.setPropertyValue(getClass().getName() + "documentationOpenInBrowser", String.valueOf(openBrowser));

		if(!fileName.endsWith("/"))
		{
			fileName = fileName + "/";
		}

		try
		{
			Document xmlSaveDocument = createDocument();

			StreamSource streamSource = new StreamSource("generateDocumentation.xsl");

			Transformer xmlTransformer = TransformerFactory.newInstance().newTransformer(streamSource);
			xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			copyDirectory(new File("documentationTemplate/"), new File(fileName));

			File f = new File(fileName + "index.html");
			xmlTransformer.transform(new DOMSource(xmlSaveDocument), new StreamResult(f));

			File logo = new File(logoLocation);
			if(logo.exists() && logoLocation.toLowerCase().endsWith("png"))//because default value is ".", the current directory, which exists.
			{
				copyDirectory(logo, new File(fileName + "logo.png"));
			}

			if(openBrowser)
			{
				Desktop.getDesktop().browse(f.toURI());
			}
		}
		catch(ParserConfigurationException | TransformerException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
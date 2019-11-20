package org.gusdb.fgputil.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.gusdb.fgputil.FormatUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * @author Jerric
 */
public class XmlTransformer {

  public static void convert(InputStream inStream, InputStream xslStream,
      OutputStream outStream, String failureMessage) throws TransformException {
    TransformerFactory Factory = TransformerFactory.newInstance();

    if (!Factory.getFeature(SAXSource.FEATURE)
        || !Factory.getFeature(SAXResult.FEATURE))
      throw new TransformException(
          "Unsupported XML feature. Conversion cancelled.");

    SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) Factory);
    // Create an XMLFilter for each stylesheet.
    XMLFilter xmlFilter;
    try {
      xmlFilter = saxTFactory.newXMLFilter(new StreamSource(xslStream));
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      SAXParser parser = parserFactory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      xmlFilter.setParent(reader);

      Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);
      serializer.setOutputStream(outStream);
      xmlFilter.setContentHandler(serializer.asContentHandler());

      xmlFilter.parse(new InputSource(inStream));
    }
    catch (TransformerConfigurationException | SAXException |
           IOException | ParserConfigurationException ex) {
      throw new TransformException(failureMessage, ex);
    }
  }

  /**
   * Transforms one XML file to another with the transform specification in the
   * passed XSLT file.  This program uses the "xmlDataDir" system property to
   * define where to look for both XML and XSL files.  If this property does not
   * exist, it will treat the input arguments as normal paths.
   * 
   * @param args input file names (inputXml, xsl, outputXml)
   */
  public static void main(String[] args) {
    // get the file parameters
    if (args.length != 3) {
      System.err.println("Usage: xmlConvert <input_xml> <xsl> <output_xml>");
      System.exit(-1);
    }

    String dataDir = System.getProperty("xmlDataDir");
    File xmlDataDir = (dataDir == null ? null : new File(dataDir));
    File inXmlFile = new File(xmlDataDir, args[0]);
    File inXslFile = new File(xmlDataDir, args[1]);
    File outXmlFile = new File(xmlDataDir, args[2]);

    if (!inXmlFile.exists() || !inXslFile.exists()) {
      System.err.println("The input XML or XSL does not exist!");
      System.exit(-1);
    }

    try (
      InputStream inXmlStream = new FileInputStream(inXmlFile);
      InputStream inXslStream = new FileInputStream(inXslFile);
      OutputStream outXmlStream = new FileOutputStream(outXmlFile)) {

      // convert the xml
      XmlTransformer.convert(inXmlStream, inXslStream, outXmlStream,
          "Failed to transform XML using " + inXslFile.getName());

      // save the result
      outXmlStream.flush();

      System.out.println("Xml file has been transformed successfully.");
    }
    catch (Exception ex) {
      System.err.println("Xml file could not be successfully transformed.");
      System.err.println(FormatUtil.getStackTrace(ex));
    }
  }
}

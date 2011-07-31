/*
 * OAIDCCrosswalk.java
 *
 * Version: $Revision: 4386 $
 *
 * Date: $Date: 2009-10-06 21:00:23 +0100 (Tue, 06 Oct 2009) $
 *
 * Copyright (c) 2002-2005, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.app.oai;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.dspace.app.util.MetadataExposure;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.content.crosswalk.IConverter;
import org.dspace.search.HarvestedItemInfo;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.PluginManager;
import org.dspace.core.LogManager;
import org.apache.log4j.Logger;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;

/**
 * OAI_DC Crosswalk implementation based on oaidc.properties file. All metadata
 * included in the oaidc.properties file will be mapped on a valid oai_dc
 * element, invalid oai_dc element will be not used. It is possible specify for
 * any metadata a converter {@link org.dspace.content.crosswalk.IConverter}
 * to manipulate the metadata value before that it will be dissemite in OAI_DC.
 * 
 * @author Robert Tansley
 * @author Andrea Bollini
 * @version $Revision: 4386 $
 */
public class OAIDCCrosswalk extends Crosswalk
{
	// Pattern containing all the characters we want to filter out / replace
    // converting a String to xml
    private static final Pattern invalidXmlPattern = Pattern
           .compile("([^\\t\\n\\r\\u0020-\\ud7ff\\ue000-\\ufffd\\u10000-\\u10ffff]+|[&<>])");

    // Patter to extract the converter name if any
    private static final Pattern converterPattern = Pattern.compile(".*\\((.*)\\)");

    private static final String[] oaidcElement = new String[] { "title",
            "creator", "subject", "description", "publisher", "contributor",
            "date", "type", "format", "identifier", "source", "language",
            "relation", "coverage", "rights" };

    /** Location of config file */
    private static final String configFilePath = ConfigurationManager
            .getProperty("dspace.dir")
            + File.separator
            + "config"
            + File.separator
            + "crosswalks"
            + File.separator + "oaidc.properties";

    /** log4j logger */
    private static Logger log = Logger.getLogger(OAIDCCrosswalk.class);

    private static final Map<String, Set<String>> config = new HashMap<String, Set<String>>();

    static
    {
        // Read in configuration
        Properties crosswalkProps = new Properties();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(configFilePath);
            crosswalkProps.load(fis);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(
                    "Wrong configuration for OAI_DC", e);
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException ioe)
                {
                    log.error(ioe);
                }
            }
        }

        Set<Object> keySet = crosswalkProps.keySet();
        if (keySet != null)
        {
            for (Object key : keySet)
            {
                String oaielement = crosswalkProps.getProperty((String) key);
                if (oaielement != null && !oaielement.trim().equals(""))
                {
                    Set<String> tmp = config.get(oaielement);
                    if (tmp == null)
                    {
                        tmp = new HashSet<String>();
                    }

                    tmp.add((String) key);
                    config.put(oaielement, tmp);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException(
                    "Configurazione errata per l'uscita OAI_DC");
        }
    }

    public OAIDCCrosswalk(Properties properties)
    {
        super("http://www.openarchives.org/OAI/2.0/oai_dc/ "
                + "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
    }

    public boolean isAvailableFor(Object nativeItem)
    {
        // We have DC for everything
        return true;
    }

    public String createMetadata(Object nativeItem)
            throws CannotDisseminateFormatException
    {
        Item item = ((HarvestedItemInfo) nativeItem).item;

        StringBuffer metadata = new StringBuffer();

        metadata
                .append(
                        "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" ")
                .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
                .append(
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append(
                        "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">");

        for (String element : oaidcElement)
        {
            Set<String> itemMetadata = config.get(element);

            if (itemMetadata != null && itemMetadata.size() > 0)
            {
                for (String mdString : itemMetadata)
                {
                    String converterName = null;
                    IConverter converter = null;
                    Matcher converterMatcher = converterPattern.matcher(mdString);
                    if (converterMatcher.matches())
                    {
                        converterName = converterMatcher.group(1);
                        converter = (IConverter) PluginManager.getNamedPlugin(
                                IConverter.class, converterName);
                        if (converter == null)
                        {
                            log.warn(LogManager.getHeader(null,
                                    "createMetadata",
                                    "no converter plugin found with name "
                                            + converterName + " for metadata "
                                            + mdString));
                        }
                    }

                    DCValue[] dcValues;
                    if (converterName != null)
                    {
                        dcValues = item.getMetadata(mdString.replaceAll("\\("
                                + converterName + "\\)", ""));
                    }
                    else
                    {
                        dcValues = item.getMetadata(mdString);
                    }

                    try
                    {
                    for (DCValue dcValue : dcValues)
                    {
                            if (!MetadataExposure.isHidden(((HarvestedItemInfo) nativeItem).context,
                                                          dcValue.schema, dcValue.element, dcValue.qualifier))
                            {
                        String value;
                        if (converter != null)
                        {
                            value = converter.makeConversion(dcValue.value);
                        }
                        else
                        {
                            value = dcValue.value;
                        }

                        // Also replace all invalid characters with ' '
                        if (value != null)
                        {
                            StringBuffer valueBuf = new StringBuffer(value
                                    .length());
                            Matcher xmlMatcher = invalidXmlPattern
                                    .matcher(value.trim());
                            while (xmlMatcher.find())
                            {
                                String group = xmlMatcher.group();

                                // group will either contain a character that we
                                // need to encode for xml
                                // (ie. <, > or &), or it will be an invalid
                                // character
                                // test the contents and replace appropriately

                                if (group.equals("&"))
                                    xmlMatcher.appendReplacement(valueBuf,
                                            "&amp;");
                                else if (group.equals("<"))
                                    xmlMatcher.appendReplacement(valueBuf,
                                            "&lt;");
                                else if (group.equals(">"))
                                    xmlMatcher.appendReplacement(valueBuf,
                                            "&gt;");
                                else
                                    xmlMatcher.appendReplacement(valueBuf, " ");
                            }

                            // add bit of the string after the final match
                            xmlMatcher.appendTail(valueBuf);

                            metadata.append("<dc:").append(element).append(">")
                                    .append(valueBuf.toString())
                                    .append("</dc:").append(element)
                                    .append(">");
                        }
                    }
                }
            }
                    catch (SQLException e)
                    {
                        throw new CannotDisseminateFormatException(e.toString());
        }
                }
            }
        }

        metadata.append("</oai_dc:dc>");

        return metadata.toString();
    }
}

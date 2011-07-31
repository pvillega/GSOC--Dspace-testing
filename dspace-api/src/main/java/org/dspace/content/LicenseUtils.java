package org.dspace.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.license.FormattableArgument;
import org.dspace.content.license.LicenseArgumentFormatter;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Utility class to manage generation and storing of the license text that the
 * submitter has to grant/granted for archiving the item
 * 
 * @author bollini
 * 
 */
public class LicenseUtils
{
    /**
     * Return the text of the license that the user has granted/must grant
     * before for submit the item. The license text is build using the template
     * defined for the collection if any or the wide site configuration. In the
     * license text the following substitution can be used. {0} the eperson
     * firstname<br>
     * {1} the eperson lastname<br>
     * {2} the eperson email<br>
     * {3} the current date<br>
     * {4} the collection object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {5} the item object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {6} the eperson object that will be formatted using the appropriate
     * LicenseArgumentFormatter plugin (if defined)<br>
     * {x} any addittion argument supplied wrapped in the
     * LicenseArgumentFormatter based on his type (map key)
     * 
     * @see LicenseArgumentFormatter
     * @param locale
     * @param collection
     * @param item
     * @param eperson
     * @param additionalInfo
     * @return the license text obtained substituting the provided argument in
     *         the license template
     */
    public static String getLicenseText(Locale locale, Collection collection,
            Item item, EPerson eperson, Map<String, Object> additionalInfo)
    {
        Formatter formatter = new Formatter(locale);

        // EPerson firstname, lastname, email and the current date
        // will be available as separate arguments to make more simple produce
        // "tradition" text license
        // collection, item and eperson object will be also available
        int numArgs = 7 + (additionalInfo != null ? additionalInfo.size() : 0);
        Object[] args = new Object[numArgs];
        args[0] = eperson.getFirstName();
        args[1] = eperson.getLastName();
        args[2] = eperson.getEmail();
        args[3] = new java.util.Date();
        args[4] = new FormattableArgument("collection", collection);
        args[5] = new FormattableArgument("item", item);
        args[6] = new FormattableArgument("eperson", eperson);

        if (additionalInfo != null)
        {
            int i = 1;
            for (String key : additionalInfo.keySet())
            {
                args[6 + i] = new FormattableArgument(key, additionalInfo
                        .get(key));
                i++;
            }
        }

        String licenseTemplate = collection.getLicense();

        return formatter.format(licenseTemplate, args).toString();
    }

    /**
     * Utility method if no additional arguments has need to be supplied to the
     * license template. (i.e. call the full getLicenseText supplying
     * <code>null</code> for the additionalInfo argument)
     * 
     * @param locale
     * @param collection
     * @param item
     * @param eperson
     * @return
     */
    public static String getLicenseText(Locale locale, Collection collection,
            Item item, EPerson eperson)
    {
        return getLicenseText(locale, collection, item, eperson, null);
    }

    /**
     * Store a copy of the license a user granted in the item.
     * 
     * @param context
     *            the dspace context
     * @param item
     *            the item object of the license
     * @param licenseText
     *            the license the user granted
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    public static void grantLicense(Context context, Item item,
            String licenseText) throws SQLException, IOException,
            AuthorizeException
    {
        // Put together text to store
        // String licenseText = "License granted by " + eperson.getFullName()
        // + " (" + eperson.getEmail() + ") on "
        // + DCDate.getCurrent().toString() + " (GMT):\n\n" + license;

        // Store text as a bitstream
        byte[] licenseBytes = licenseText.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(licenseBytes);
        Bitstream b = item.createSingleBitstream(bais, "LICENSE");

        // Now set the format and name of the bitstream
        b.setName("license.txt");
        b.setSource("Written by org.dspace.content.LicenseUtils");

        // Find the License format
        BitstreamFormat bf = BitstreamFormat.findByShortDescription(context,
                "License");
        b.setFormat(bf);

        b.update();
    }
}
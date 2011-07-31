/*
 * LicenseUtilsTest.java
 *
 * Copyright (c) 2002-2009, The DSpace Foundation.  All rights reserved.
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
 * - Neither the name of the DSpace Foundation nor the names of its
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

package org.dspace.content;

import org.apache.commons.io.IOUtils;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import org.dspace.authorize.AuthorizeException;
import org.dspace.eperson.EPerson;
import org.dspace.core.ConfigurationManager;
import org.dspace.AbstractUnitTest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.*;

/**
 * Unit Tests for class LicenseUtils
 * @author pvillega
 */
public class LicenseUtilsTest extends AbstractUnitTest
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(LicenseUtilsTest.class);

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init()
    {
        super.init();
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy()
    {
        super.destroy();
    }

    /**
     * Test of getLicenseText method, of class LicenseUtils.
     */
    @Test
    public void testGetLicenseText_5args() throws SQLException, AuthorizeException
    {
        //parameters for the test
        Locale locale = null;
        Collection collection = null;
        Item item = null;
        EPerson person = null;
        Map<String, Object> additionalInfo = null;

        // We don't test attribute 4 as this is the date, and the date often differs between when the test
        // is executed, and when the LicenceUtils code gets the current date/time which causes the test to fail
        String template = "Template license: %1$s %2$s %3$s %5$s %6$s";
        String templateLong = "Template license: %1$s %2$s %3$s %5$s %6$s %8$s %9$s %10$s %11$s";
        String templateResult = "Template license: first name last name test@email.com  ";
        String templateLongResult = "Template license: first name last name test@email.com   arg1 arg2 arg3 arg4";
        String defaultLicense = ConfigurationManager.getDefaultSubmissionLicense();

        context.turnOffAuthorisationSystem();
        //TODO: the tested method doesn't verify the input, will throw NPE if any parameter is null

        //testing for default license
        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = null;
        assertThat("testGetLicenseText_5args 0", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(defaultLicense));

        locale = Locale.GERMAN;
        collection = Collection.create(context);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = null;
        assertThat("testGetLicenseText_5args 1", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(defaultLicense));

        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = new HashMap<String, Object>();
        additionalInfo.put("arg1", "arg1");
        additionalInfo.put("arg2", "arg2");
        additionalInfo.put("arg3", "arg3");
        assertThat("testGetLicenseText_5args 2", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(defaultLicense));

        //test collection template
        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        collection.setLicense(template);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = null;
        assertThat("testGetLicenseText_5args 3", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(templateResult));

        locale = Locale.GERMAN;
        collection = Collection.create(context);
        collection.setLicense(template);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = null;
        assertThat("testGetLicenseText_5args 4", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(templateResult));

        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        collection.setLicense(templateLong);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        additionalInfo = new LinkedHashMap<String, Object>();
        additionalInfo.put("arg1", "arg1");
        additionalInfo.put("arg2", "arg2");
        additionalInfo.put("arg3", "arg3");        
        additionalInfo.put("arg4", "arg4");
        assertThat("testGetLicenseText_5args 5", LicenseUtils.getLicenseText(locale, collection, item, person, additionalInfo), equalTo(templateLongResult));
        
        context.restoreAuthSystemState();
    }

    /**
     * Test of getLicenseText method, of class LicenseUtils.
     */
    @Test
    public void testGetLicenseText_4args() throws SQLException, AuthorizeException
    {
        //parameters for the test
        Locale locale = null;
        Collection collection = null;
        Item item = null;
        EPerson person = null;

        String template = "Template license: %1$s %2$s %3$s %5$s %6$s";
        String templateResult = "Template license: first name last name test@email.com  ";
        String defaultLicense = ConfigurationManager.getDefaultSubmissionLicense();

        context.turnOffAuthorisationSystem();
        //TODO: the tested method doesn't verify the input, will throw NPE if any parameter is null

        //testing for default license
        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        assertThat("testGetLicenseText_5args 0", LicenseUtils.getLicenseText(locale, collection, item, person), equalTo(defaultLicense));

        locale = Locale.GERMAN;
        collection = Collection.create(context);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        assertThat("testGetLicenseText_5args 1", LicenseUtils.getLicenseText(locale, collection, item, person), equalTo(defaultLicense));

        //test collection template
        locale = Locale.ENGLISH;
        collection = Collection.create(context);
        collection.setLicense(template);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");        
        assertThat("testGetLicenseText_5args 3", LicenseUtils.getLicenseText(locale, collection, item, person), equalTo(templateResult));

        locale = Locale.GERMAN;
        collection = Collection.create(context);
        collection.setLicense(template);
        item = Item.create(context);
        person = EPerson.create(context);
        person.setFirstName("first name");
        person.setLastName("last name");
        person.setEmail("test@email.com");
        assertThat("testGetLicenseText_5args 4", LicenseUtils.getLicenseText(locale, collection, item, person), equalTo(templateResult));

        context.restoreAuthSystemState();
    }

    /**
     * Test of grantLicense method, of class LicenseUtils.
     */
    @Test
    public void testGrantLicense() throws Exception 
    {
        context.turnOffAuthorisationSystem();
        Item item = Item.create(context);
        String defaultLicense = ConfigurationManager.getDefaultSubmissionLicense();

        LicenseUtils.grantLicense(context, item, defaultLicense);

        StringWriter writer = new StringWriter();
        IOUtils.copy(item.getBundles("LICENSE")[0].getBitstreams()[0].retrieve(), writer);
        String license = writer.toString();

        assertThat("testGrantLicense 0",license, equalTo(defaultLicense));
        context.restoreAuthSystemState();
    }

}
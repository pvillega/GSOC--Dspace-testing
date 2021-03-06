/*
 * FormatIdentifierTest.java
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

import java.io.FileInputStream;
import java.io.File;
import org.dspace.AbstractUnitTest;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.* ;
import static org.hamcrest.CoreMatchers.*;

/**
 * Unit Tests for class FormatIdentifier
 * @author pvillega
 */
public class FormatIdentifierTest extends AbstractUnitTest
{

    /** log4j category */
    private static final Logger log = Logger.getLogger(FormatIdentifierTest.class);

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
     * Test of guessFormat method, of class FormatIdentifier.
     */
    @Test
    public void testGuessFormat() throws Exception
    {
        File f = new File(testProps.get("test.bitstream").toString());
        Bitstream bs = null; 
        BitstreamFormat result = null;
        BitstreamFormat pdf = BitstreamFormat.findByShortDescription(context, "Adobe PDF");
        
        //test null filename
        //TODO: the check if filename is null is wrong, as it checks after using a toLowerCase
        //which can trigger the NPE
        bs = Bitstream.create(context, new FileInputStream(f));
        context.commit();
        bs.setName(null);
        result = FormatIdentifier.guessFormat(context, bs);
        assertThat("testGuessFormat 0",result, nullValue());

        //test unknown format
        bs = Bitstream.create(context, new FileInputStream(f));
        bs.setName("file_without_extension.");
        context.commit();
        result = FormatIdentifier.guessFormat(context, bs);
        assertThat("testGuessFormat 1",result, nullValue());

        //test known format
        bs = Bitstream.create(context, new FileInputStream(f));
        bs.setName(testProps.get("test.bitstream").toString());
        context.commit();
        result = FormatIdentifier.guessFormat(context, bs);
        assertThat("testGuessFormat 2",result.getID(), equalTo(pdf.getID()));
        assertThat("testGuessFormat 3",result.getMIMEType(), equalTo(pdf.getMIMEType()));
        assertThat("testGuessFormat 4",result.getExtensions(), equalTo(pdf.getExtensions()));
    }

}
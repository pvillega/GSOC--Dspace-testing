/*
 * InstallItem.java
 *
 * Version: $Revision: 5244 $
 *
 * Date: $Date: 2010-08-08 08:35:37 +0100 (Sun, 08 Aug 2010) $
 *
 * Copyright (c) 2002-2010, The DSpace Foundation.  All rights reserved.
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

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.embargo.EmbargoManager;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;

/**
 * Support to install an Item in the archive.
 * 
 * @author dstuve
 * @version $Revision: 5244 $
 */
public class InstallItem
{
    /**
     * Take an InProgressSubmission and turn it into a fully-archived Item,
     * creating a new Handle.
     * 
     * @param c
     *            DSpace Context
     * @param is
     *            submission to install
     * 
     * @return the fully archived Item
     */
    public static Item installItem(Context c, InProgressSubmission is)
            throws SQLException, IOException, AuthorizeException
    {
        return installItem(c, is, null);
    }

    /**
     * Take an InProgressSubmission and turn it into a fully-archived Item.
     * 
     * @param c  current context
     * @param is
     *            submission to install
     * @param suppliedHandle
     *            the existing Handle to give to the installed item
     * 
     * @return the fully archived Item
     */
    public static Item installItem(Context c, InProgressSubmission is,
            String suppliedHandle) throws SQLException,
            IOException, AuthorizeException
    {
        Item item = is.getItem();
        String handle;
        
        // this is really just to flush out fatal embargo metadata
        // problems before we set inArchive.
        DCDate liftDate = EmbargoManager.getEmbargoDate(c, item);

        // create accession date
        DCDate now = DCDate.getCurrent();
        item.addDC("date", "accessioned", null, now.toString());
        
        // add date available if not under embargo, otherwise it will
        // be set when the embargo is lifted.
        if (liftDate == null)
            item.addDC("date", "available", null, now.toString());

        // create issue date if not present
        DCValue[] currentDateIssued = item.getDC("date", "issued", Item.ANY);

        if (currentDateIssued.length == 0)
        {
            DCDate issued = new DCDate();
            issued.setDateLocal(now.getYear(),now.getMonth(),now.getDay(),-1,-1,-1);
            item.addDC("date", "issued", null, issued.toString());
        }

        // if no previous handle supplied, create one
        if (suppliedHandle == null)
        {
            // create handle
            handle = HandleManager.createHandle(c, item);
        }
        else
        {
            handle = HandleManager.createHandle(c, item, suppliedHandle);
        }

        String handleref = HandleManager.getCanonicalForm(handle);

        // Add handle as identifier.uri DC value.
        // First check that identifier dosn't already exist.
        boolean identifierExists = false;
        DCValue[] identifiers = item.getDC("identifier", "uri", Item.ANY);
        for (DCValue identifier : identifiers)
        	if (handleref.equals(identifier.value))
        		identifierExists = true;
        if (!identifierExists)
        	item.addDC("identifier", "uri", null, handleref);

        String provDescription = "Made available in DSpace on " + now
                + " (GMT). " + getBitstreamProvenanceMessage(item);

        if (currentDateIssued.length != 0)
        {
            DCDate d = new DCDate(currentDateIssued[0].value);
            provDescription = provDescription + "  Previous issue date: "
                    + d.toString();
        }

        // Add provenance description
        item.addDC("description", "provenance", "en", provDescription);

        // create collection2item mapping
        is.getCollection().addItem(item);

        // set owning collection
        item.setOwningCollection(is.getCollection());

        // set in_archive=true
        item.setArchived(true);

        // save changes ;-)
        item.update();

        // Notify interested parties of newly archived Item
        c.addEvent(new Event(Event.INSTALL, Constants.ITEM, item.getID(),
                handle));

        // remove in-progress submission
        is.deleteWrapper();

        // remove the item's policies and replace them with
        // the defaults from the collection
        item.inheritCollectionDefaultPolicies(is.getCollection());
        
        // set embargo lift date and take away read access if indicated.
        if (liftDate != null)
            EmbargoManager.setEmbargo(c, item, liftDate);

        return item;
    }


    /**
     * Generate provenance-worthy description of the bitstreams contained in an
     * item.
     * 
     * @param myitem  the item generate description for
     * 
     * @return provenance description
     */
    public static String getBitstreamProvenanceMessage(Item myitem)
    						throws SQLException
    {
        // Get non-internal format bitstreams
        Bitstream[] bitstreams = myitem.getNonInternalBitstreams();

        // Create provenance description
        String mymessage = "No. of bitstreams: " + bitstreams.length + "\n";

        // Add sizes and checksums of bitstreams
        for (int j = 0; j < bitstreams.length; j++)
        {
            mymessage = mymessage + bitstreams[j].getName() + ": "
                    + bitstreams[j].getSize() + " bytes, checksum: "
                    + bitstreams[j].getChecksum() + " ("
                    + bitstreams[j].getChecksumAlgorithm() + ")\n";
        }

        return mymessage;
    }
}

/*
 * LDAPServlet.java
 *
 * Version: $Revision: 3705 $
 *
 * Date: $Date: 2009-04-11 18:02:24 +0100 (Sat, 11 Apr 2009) $
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

package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.dspace.app.webui.util.Authenticate;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.core.I18nUtil;
import java.util.Locale;

// Internal class to pass LDAP details from authentication method
class LDAPResult
{
    String email;
    String givenName;
    String surname;
    String phone;
}

/**
 * LDAP username and password authentication servlet.  Displays the
 * login form <code>/login/ldap.jsp</code> on a GET,
 * otherwise process the parameters as an ldap username and password.
 *
 * @author  John Finlay (Brigham Young University)
 * @version $Revision: 3705 $
 */
public class LDAPServlet extends DSpaceServlet
{
    /** log4j logger */
    private static Logger log = Logger.getLogger(LDAPServlet.class);

    protected void doDSGet(Context context,
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
        // check if ldap is enables and forward to the correct login form
        boolean ldap_enabled = ConfigurationManager.getBooleanProperty("ldap.enable");
        if (ldap_enabled)
            JSPManager.showJSP(request, response, "/login/ldap.jsp");
        else
            JSPManager.showJSP(request, response, "/login/password.jsp");
    }


    protected void doDSPost(Context context,
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException, SQLException, AuthorizeException
    {
        // Process the POSTed email and password
        String netid = request.getParameter("login_netid");
        String password = request.getParameter("login_password");
	 	String jsp = null;
		
		// Locate the eperson
        int status = AuthenticationManager.authenticate(context, netid, password,
                        null, request);


        if (status == AuthenticationMethod.SUCCESS)
        {
            // Logged in OK.
            Authenticate.loggedIn(context, request, context.getCurrentUser());

            // Set the Locale according to user preferences
            Locale epersonLocale = I18nUtil.getEPersonLocale(context.getCurrentUser());
            context.setCurrentLocale(epersonLocale);
            Config.set(request.getSession(), Config.FMT_LOCALE, epersonLocale);

            log.info(LogManager.getHeader(context, "login", "type=explicit"));

            // resume previous request
            Authenticate.resumeInterruptedRequest(request, response);

            return;
        }
        else if (status == AuthenticationMethod.CERT_REQUIRED)
		{
			jsp = "/error/require-certificate.jsp";
		}
        else
		{
			jsp = "/login/incorrect.jsp";
		}

		// If we reach here, supplied email/password was duff.
        log.info(LogManager.getHeader(context, "failed_login",
                "netid=" + netid + ", result=" + String.valueOf(status)));
        JSPManager.showJSP(request, response, jsp);
	}
}
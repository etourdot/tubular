/*
 * Copyright (C) 2010 Romain Deltour
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.trancecode.xproc;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.DataProvider;

/**
 * Provides the list of URLs to the XProc test files hosted on
 * http://svn.xproc.org/tests/ .
 * <p>
 * This provider finds the test URLs by scraping the HTML returned by an HTTP
 * GET request on the Subversion repository URL.
 * </p>
 * 
 * @author Romain Deltour
 */
public class RemoteXProcTestsProvider
{

    public final static String testBaseURI = "http://svn.xproc.org/tests/";

    @DataProvider(name = "xprocTests")
    public static Object[][] listTests(final Method method)
    {
        final List<Object[]> testFiles = new ArrayList<Object[]>();
        try
        {
            final URL url = new URL(testBaseURI + method.getName());
            final java.net.URLConnection con = url.openConnection();
            con.connect();
            final java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con
                    .getInputStream()));
            String line;
            final Pattern pattern = Pattern.compile(".*<a href=\"(.*)\\.xml\".*");
            while ((line = in.readLine()) != null)
            {
                final Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                {
                    testFiles.add(new String[] { method.getName() + "/" + matcher.group(1) + ".xml" });
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return testFiles.toArray(new Object[][] {});
    }
}

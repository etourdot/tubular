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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;

/**
 * Provides the list of URLs to local XProc test files
 * 
 * @author Romain Deltour
 */
public class LocalXProcTestsProvider
{
    /**
     * The name of the property pointing to the XProc test directory
     */
    public static final String TEST_DIR_PROPERTY = "org.trancecode.xproc.TEST_DIR";

    /**
     * The property for an optional name filter (the provider will return only
     * the test files whose name contains the value of this property).
     */
    private static final String TEST_FILTER_PROPERTY = "org.trancecode.xproc.TEST_FILTER";

    @DataProvider(name = "xprocTests")
    public static Object[][] getData(final Method method)
    {
        final List<URL[]> urls = new ArrayList<URL[]>();
        try
        {
            final String testDirPath = System.getProperty(TEST_DIR_PROPERTY);
            final String nameFilter = System.getProperty(TEST_FILTER_PROPERTY);
            final File testDir = new File(testDirPath + "/" + method.getName());
            for (final File file : testDir.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(final File dir, final String name)
                {
                    return name.endsWith(".xml") && (nameFilter == null || name.contains(nameFilter));
                }
            }))
            {
                urls.add(new URL[] { file.toURI().toURL() });
            }
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(e);
        }

        return urls.toArray(new Object[][] {});
    }
}

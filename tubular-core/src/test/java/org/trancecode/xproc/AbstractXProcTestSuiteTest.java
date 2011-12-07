/*
 * Copyright (C) 2011 Herve Quiroz
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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import java.io.File;
import java.net.URL;

import org.trancecode.io.Paths;
import org.trancecode.io.Urls;
import org.trancecode.lang.StringPredicates;

/**
 * @author Herve Quiroz
 */
public abstract class AbstractXProcTestSuiteTest extends AbstractXProcTest
{
    private File setupDirectoryListFiles() throws Exception
    {
        final File directory = File.createTempFile(getClass().getSimpleName(), "");
        directory.delete();
        directory.mkdir();
        final File directoryListTest = new File(directory, "directory-list-test");
        directoryListTest.mkdir();
        new File(directoryListTest, "afile").createNewFile();
        new File(directoryListTest, "bfile").createNewFile();
        final File adir = new File(directoryListTest, "adir");
        adir.mkdir();
        new File(adir, "cfile").createNewFile();
        final File bdir = new File(directoryListTest, "bdir");
        bdir.mkdir();
        bdir.setReadable(false);
        return directory;
    }

    private static final Iterable<String> DIRECTORY_LIST_TEST_PATTERNS = ImmutableSet.of("/directory-list-",
            "err-c0012-001", "err-c0017-001");

    @Override
    protected void test(final URL url, final String testSuite) throws Exception
    {
        if (Iterables.any(DIRECTORY_LIST_TEST_PATTERNS, StringPredicates.isContainedBy(url.toString())))
        {
            final File directory = setupDirectoryListFiles();
            final File pipeline = new File(directory, Paths.getName(url.getPath()));
            Files.copy(Urls.asInputSupplier(url), pipeline);
            super.test(pipeline.toURI().toURL(), "required");
        }
        else
        {
            super.test(url, testSuite);
        }
    }
}

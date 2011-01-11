/*
 * Copyright (C) 2010 Herve Quiroz
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
package org.trancecode.xproc.cli;

import com.beust.jcommander.internal.Lists;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.trancecode.AbstractTest;
import org.trancecode.io.Files;
import org.trancecode.io.IOUtil;
import org.trancecode.logging.Logger;

/**
 * Tests for {@link CommandLineExecutor}.
 * 
 * @author Herve Quiroz
 * @author Torsten Knodt
 */
public final class CommandLineExecutorTest extends AbstractTest
{
    private static final Logger LOG = Logger.getLogger(CommandLineExecutorTest.class);

    public static final class TestProvider
    {
        @DataProvider(name = "testDirectories")
        public static Object[][] listTests(final Method method) throws Exception
        {
            try
            {
                final List<Object[]> tests = Lists.newArrayList();

                final File testRootDirectory = new File(CommandLineExecutorTest.class.getResource(
                        "/" + CommandLineExecutorTest.class.getSimpleName() + "/root").toURI()).getParentFile();
                LOG.info("testRootDirectory = {}", testRootDirectory);
                assert testRootDirectory.isDirectory();
                for (final File testDirectory : Files.listDirectories(testRootDirectory))
                {
                    final Properties properties = new Properties();
                    properties.load(Files.newFileInputStream(new File(testDirectory, "test.properties")));
                    final String[] args = properties.getProperty("args")
                            .replace("${test.directory}", testDirectory.getAbsolutePath() + "/").split(" ");

                    final InputStream stdin;
                    if (properties.getProperty("stdin") != null)
                    {
                        final File stdinFile = new File(testDirectory, properties.getProperty("stdin"));
                        stdin = Files.newFileInputStream(stdinFile);
                    }
                    else
                    {
                        stdin = IOUtil.newNullInputStream();
                    }

                    final String expectedStdout;
                    if (properties.getProperty("stdout") != null)
                    {
                        final File stdoutFile = new File(testDirectory, properties.getProperty("stdout"));
                        expectedStdout = FileUtils.readFileToString(stdoutFile);
                    }
                    else
                    {
                        expectedStdout = null;
                    }

                    final int expectedExitCode = Integer.parseInt(properties.getProperty("exit.code", "0"));

                    tests.add(new Object[] { args, stdin, expectedStdout, expectedExitCode });
                }

                return tests.toArray(new Object[][] {});
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @Test(dataProvider = "testDirectories", dataProviderClass = TestProvider.class)
    public void test(final String[] args, final InputStream stdin, final String expectedStdout,
            final int expectedExitCode)
    {
        final ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
        final PrintStream stdout = new PrintStream(stdoutBytes);

        final int exitCode;
        try
        {
            exitCode = new CommandLineExecutor().execute(args, stdin, stdout, System.err);
        }
        finally
        {
            stdout.close();
        }
        final String stdoutContent = stdoutBytes.toString();

        Assert.assertEquals(exitCode, expectedExitCode);
        if (expectedStdout != null)
        {
            assertXmlEquals(stdoutContent, expectedStdout);
        }
        // TODO handle stderr
    }

    private static void assertXmlEquals(final String actual, final String expected)
    {
        try
        {
            XMLUnit.setIgnoreWhitespace(true);
            XMLAssert.assertXMLEqual(expected, actual);
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(String.format("expected:\n%s\nactual:\n%s", expected, actual), e);
        }
    }
}

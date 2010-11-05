/*
 * Copyright (C) 2010 TranceCode Software
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Herve Quiroz
 * @author Torsten Knodt
 */
public final class CommandLineExecutorTest
{

    @Test
    public void standardIOTest() throws IOException, InterruptedException
    {

        final ProcessBuilder processBuilder = new ProcessBuilder(new String[]
                {
                    "java",
                    "-cp", System.getProperty("java.class.path"),
                    CommandLineExecutor.class.getName(),
                    "--",
                    "--xpl", getClass().getResource("xproc-1.0.xml").toString()
                });
        final Process process = processBuilder.start();
        process.getOutputStream().flush();
        process.getOutputStream().close();
        final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        boolean outputOKFlag = false;
        boolean outputFoundFlag = false;
        while (br.ready())
        {
            final String line = br.readLine();
            outputFoundFlag = true;
            if (line.contains("Inline XML conversion with inline XSLT using XProc"))
            {
                outputOKFlag = true;
            }
        }
        br.close();
        // FIXME currently blocking: Assert.assertEquals(process.waitFor(), 0, "Checking exit value of tubular process");
        Assert.assertTrue(outputFoundFlag, "Did not find any output string");
        Assert.assertTrue(outputOKFlag, "Did not find expected output string");
    }
}

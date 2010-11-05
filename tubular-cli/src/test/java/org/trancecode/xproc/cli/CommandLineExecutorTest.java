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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Herve Quiroz
 * @author Torsten Knodt
 */
public final class CommandLineExecutorTest
{

    @Test
    public void standardIOTest() throws IOException, InterruptedException, ExecutionException
    {

        final ProcessBuilder processBuilder = new ProcessBuilder(new String[]
                {
                    "java",
                    "-cp", System.getProperty("java.class.path"),
                    CommandLineExecutor.class.getName(),
                    "--xpl", getClass().getResource("xproc-1.0.xml").toString()
                });
        final Process process = processBuilder.start();
        process.getOutputStream().flush();
        process.getOutputStream().close();
        process.getErrorStream().close();
        final FutureTask<String> bufferedInputStreamReaderFutureTask = new FutureTask<String>(new BufferedInputStreamReaderCallable(process.getInputStream()));
        final FutureTask<String> bufferedErrorStreamReaderFutureTask = new FutureTask<String>(new BufferedInputStreamReaderCallable(process.getErrorStream()));
        new Thread(bufferedInputStreamReaderFutureTask, "standardIOTest Output Handler").start();
        new Thread(bufferedErrorStreamReaderFutureTask, "standardIOTest Error Handler").start();
        final String output = bufferedInputStreamReaderFutureTask.get();
        Assert.assertEquals(process.waitFor(), 0, "Checking exit value of tubular process");
        process.destroy();
        Assert.assertTrue(!output.isEmpty(), "Did not find any output string");
        Assert.assertTrue(output.contains("Inline XML conversion with inline XSLT using XProc"), "Did not find expected output string");
    }

    private class BufferedInputStreamReaderCallable implements Callable<String>
    {

        private final BufferedReader br;
        private final StringBuilder sb = new StringBuilder();

        public BufferedInputStreamReaderCallable(final InputStream inputStream)
        {
            br = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public final String call() throws Exception
        {
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                System.out.println(line);
            }
            br.close();
            return sb.toString();
        }
    }
}

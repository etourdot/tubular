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
package org.trancecode.xproc.step;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.trancecode.AbstractTest;
import org.trancecode.xproc.PipelineProcessor;

/**
 * Tests for {@link Step}.
 * 
 * @author Herve Quiroz
 */
public final class StepTest extends AbstractTest
{
    @Test
    public void getSubpipelineStepDependencies()
    {
        final PipelineProcessor processor = new PipelineProcessor();
        final Source pipelineSource = new StreamSource(getClass().getResourceAsStream(
                "/StepTest/getSubpipelineStepDependencies.xpl"), "/StepTest/getSubpipelineStepDependencies.xpl");
        final Step pipeline = processor.buildPipeline(pipelineSource).getUnderlyingPipeline();
        final Map<Step, Step> dependencies = pipeline.getSubpipelineStepDependencies();

        final Step identity1 = pipeline.getStepByName("identity1");
        final Step identity2 = pipeline.getStepByName("identity2");
        final Step identity3 = pipeline.getStepByName("identity3");
        final Step identity4 = pipeline.getStepByName("identity4");
        final Step identity5 = pipeline.getStepByName("identity5");
        final Step identity6 = pipeline.getStepByName("identity6");
        final Step load1 = pipeline.getStepByName("load1");
        final Step store1 = pipeline.getStepByName("store1");
        final Step store2 = pipeline.getStepByName("store2");

        Assert.assertEquals(dependencies.get(identity1), null);
        Assert.assertEquals(dependencies.get(store1), identity1);
        Assert.assertEquals(dependencies.get(identity2), store1);
        Assert.assertEquals(dependencies.get(identity3), identity2);
        Assert.assertEquals(dependencies.get(store2), identity3);
        Assert.assertEquals(dependencies.get(identity4), identity3);
        Assert.assertEquals(dependencies.get(load1), store2);
        Assert.assertEquals(dependencies.get(identity5), load1);
        Assert.assertEquals(dependencies.get(identity6), identity2);
    }
}

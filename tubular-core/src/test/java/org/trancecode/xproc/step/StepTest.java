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

import com.google.common.collect.ImmutableSet;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.QName;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.trancecode.AbstractTest;
import org.trancecode.TcAssert;
import org.trancecode.xproc.PipelineProcessor;
import org.trancecode.xproc.port.Port;

/**
 * Tests for {@link Step}.
 * 
 * @author Herve Quiroz
 */
public final class StepTest extends AbstractTest
{
    @Test
    public void getSubpipelineStepDependencies1()
    {
        final PipelineProcessor processor = new PipelineProcessor();
        final Source pipelineSource = new StreamSource(getClass().getResourceAsStream(
                "/StepTest/getSubpipelineStepDependencies.xpl"), "/StepTest/getSubpipelineStepDependencies.xpl");
        final Step pipeline = processor.buildPipeline(pipelineSource).getUnderlyingPipeline();
        final Map<Step, Iterable<Step>> dependencies = pipeline.getSubpipelineStepDependencies();

        final Step identity1 = pipeline.getStepByName("identity1");
        final Step identity2 = pipeline.getStepByName("identity2");
        final Step identity3 = pipeline.getStepByName("identity3");
        final Step identity4 = pipeline.getStepByName("identity4");
        final Step identity5 = pipeline.getStepByName("identity5");
        final Step identity6 = pipeline.getStepByName("identity6");
        final Step load1 = pipeline.getStepByName("load1");
        final Step store1 = pipeline.getStepByName("store1");
        final Step store2 = pipeline.getStepByName("store2");

        TcAssert.assertSetEquals(dependencies.get(identity1), ImmutableSet.of());
        TcAssert.assertSetEquals(dependencies.get(store1), identity1);
        TcAssert.assertSetEquals(dependencies.get(identity2), store1);
        TcAssert.assertSetEquals(dependencies.get(identity3), identity2);
        TcAssert.assertSetEquals(dependencies.get(store2), identity3);
        TcAssert.assertSetEquals(dependencies.get(identity4), identity3);
        TcAssert.assertSetEquals(dependencies.get(load1), store2);
        TcAssert.assertSetEquals(dependencies.get(identity5), load1);
        TcAssert.assertSetEquals(dependencies.get(identity6), identity2);
    }

    @Test
    public void getSubpipelineStepDependencies2()
    {
        final PipelineProcessor processor = new PipelineProcessor();
        final Source pipelineSource = new StreamSource(getClass().getResourceAsStream(
                "/StepTest/getSubpipelineStepDependencies2.xpl"), "/StepTest/getSubpipelineStepDependencies2.xpl");
        final Step pipeline = processor.buildPipeline(pipelineSource).getUnderlyingPipeline();
        final Map<Step, Iterable<Step>> dependencies = pipeline.getSubpipelineStepDependencies();

        final Step wrap = pipeline.getStepByName("wrap");
        final Step escapeMarkup = pipeline.getStepByName("escape-markup");
        final Step choose = pipeline.getStepByName("choose");

        TcAssert.assertSetEquals(dependencies.get(wrap), ImmutableSet.of());
        TcAssert.assertSetEquals(dependencies.get(escapeMarkup), wrap);
        TcAssert.assertSetEquals(dependencies.get(choose), escapeMarkup);
    }

    @Test
    public void getSubpipelineStepDependencies3()
    {
        final PipelineProcessor processor = new PipelineProcessor();
        final Source pipelineSource = new StreamSource(getClass().getResourceAsStream(
                "/StepTest/getSubpipelineStepDependencies3.xpl"), "/StepTest/getSubpipelineStepDependencies3.xpl");
        final Step pipeline = processor.buildPipeline(pipelineSource).getUnderlyingPipeline();
        final Map<Step, Iterable<Step>> dependencies = pipeline.getSubpipelineStepDependencies();

        final Step identity1 = pipeline.getStepByName("identity1");
        final Step identity2 = pipeline.getStepByName("identity2");
        final Step identity3 = pipeline.getStepByName("identity3");
        final Step store1 = pipeline.getStepByName("store1");

        TcAssert.assertSetEquals(dependencies.get(identity1), ImmutableSet.of());
        TcAssert.assertSetEquals(dependencies.get(identity2), identity1);
        TcAssert.assertSetEquals(dependencies.get(identity3), identity2);

        // variable port binding
        TcAssert.assertSetEquals(dependencies.get(store1), identity1, identity2);
    }

    @Test
    public void getSubpipelineStepDependencies4()
    {
        final PipelineProcessor processor = new PipelineProcessor();
        final Source pipelineSource = new StreamSource(getClass().getResourceAsStream(
                "/StepTest/getSubpipelineStepDependencies4.xpl"), "/StepTest/getSubpipelineStepDependencies4.xpl");
        final Step pipeline = processor.buildPipeline(pipelineSource).getUnderlyingPipeline();
        final Map<Step, Iterable<Step>> dependencies = pipeline.getSubpipelineStepDependencies();

        final Step identity1 = pipeline.getStepByName("identity1");
        final Step identity2 = pipeline.getStepByName("identity2");
        final Step forEach = pipeline.getStepByName("for-each");

        TcAssert.assertSetEquals(dependencies.get(identity1), ImmutableSet.of());
        TcAssert.assertSetEquals(dependencies.get(forEach), identity1);
        TcAssert.assertSetEquals(forEach.getSubpipelineStepDependencies().get(identity2), ImmutableSet.of());
    }

    @Test
    public void getPrimaryInputPort()
    {
        Step step = Step.newStep(new QName("test"), StepProcessors.unsupportedStepProcessor(new QName("test")), false);
        final Port input1 = Port.newInputPort("input1");
        step = step.declarePort(input1);
        Assert.assertSame(step.getPrimaryInputPort(), input1);
        final Port parameters1 = Port.newParameterPort("parameters1");
        step = step.declarePort(parameters1);
        Assert.assertSame(step.getPrimaryInputPort(), input1);
        final Port input2 = Port.newInputPort("input2").setPrimary(true);
        step = step.declarePort(input2);
        Assert.assertSame(step.getPrimaryInputPort(), input2);
    }
}

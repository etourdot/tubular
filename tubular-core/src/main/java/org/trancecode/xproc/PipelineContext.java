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
package org.trancecode.xproc;

import java.util.Map;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import org.trancecode.concurrent.TaskExecutor;
import org.trancecode.event.EventDispatcher;
import org.trancecode.io.InputResolver;
import org.trancecode.io.OutputResolver;
import org.trancecode.xproc.event.XProcEvent;
import org.trancecode.xproc.step.StepProcessor;

/**
 * @author Herve Quiroz
 */
public interface PipelineContext
{
    Episode getEpisode();

    EventDispatcher<XProcEvent> getEventDispatcher();

    TaskExecutor getExecutor();

    InputResolver getInputResolver();

    OutputResolver getOutputResolver();

    Processor getProcessor();

    URIResolver getUriResolver();

    StepProcessor getStepProcessor(QName step);

    Map<QName, StepProcessor> getStepProcessors();

    PipelineLibrary getPipelineLibrary();

    Map<String, Object> getProperties();

    <T> T getProperty(String name);
}

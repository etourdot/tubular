/*
 * Copyright (C) 2008 TranceCode Software
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
 *
 * $Id$
 */
package org.trancecode.xproc;

import org.trancecode.AbstractTest;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Tests for {@link PipelineFactory}.
 * 
 * @author Herve Quiroz
 */
@Test
public class PipelineFactoryTest extends AbstractTest
{
    @Test
    public void newPipelineFactory()
    {
        final PipelineFactory pipelineFactory = new PipelineFactory();
        assert pipelineFactory.getLibrary() != null;
        AssertJUnit.assertEquals(45, pipelineFactory.getLibrary().size());
        assert pipelineFactory.getLibrary().containsKey(XProcSteps.COUNT);
        assert pipelineFactory.getLibrary().containsKey(XProcSteps.FOR_EACH);
    }
}

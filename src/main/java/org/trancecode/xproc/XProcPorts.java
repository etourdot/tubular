/*
 * Copyright (C) 2008 TranceCode Software
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
package org.trancecode.xproc;

/**
 * Common XProc ports.
 * 
 * @author Herve Quiroz
 * @version $Revision$
 */
public interface XProcPorts
{
	String PORT_ITERATION_NODE = "iteration-node";

	String PORT_ITERATION_SOURCE = "iteration-source";

	String PORT_SOURCE = "source";

	String PORT_RESULT = "result";

	String PORT_PARAMETERS = "parameters";

	String PORT_SECONDARY = "secondary";

	String PORT_STYLESHEET = "stylesheet";

	String PORT_XPATH_CONTEXT = "xpath-context";
}

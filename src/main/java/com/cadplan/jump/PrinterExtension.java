/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2006 Cadplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package com.cadplan.jump;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * User: geoff
 * Date: 22/12/2006
 * Time: 07:21:42
 * Copyright 2005 Geoffrey G Roy.
 */
public class PrinterExtension extends Extension
{
	@Override
	public void configure(PlugInContext context) throws Exception
	{
		new PrinterPlugIn().initialize(context);
		new SaveViewPlugIn().initialize(context);
	}

	@Override
	public String getVersion() {
		return "2.1.0 (2021-08-19)";
	}

	@Override
	public String getName() {
		return "Printer - © 2005 Geoffrey G Roy. Modified version by Giuseppe Aruta 2020";
	}
}

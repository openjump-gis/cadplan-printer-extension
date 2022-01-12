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
package com.cadplan.designer;
//===================================================
//  GridBagDesigner
//===================================================




import java.awt.*;
import javax.swing.*;

/**
 * @author Geoffrey G. Roy
 * @version 1.0
 */


public class GridBagDesigner {

private final GridBagLayout layout;
private final GridBagConstraints constraints;
private final Container container;

	/**
	 * Constructor for GridBagDesgner
	 *
	 * @param   container  
	 */
public GridBagDesigner(Container container)
{
	  layout = new GridBagLayout();
	  constraints = new GridBagConstraints();
	  container.setLayout(layout);
	  this.container = container;
	  setDefaults();
}



	/**
	 * Adds a component using currrent settings
	 *
	 * @param   component component to add
	 */

public void addComponent(Component component)
{
	layout.setConstraints(component, constraints);
	container.add(component);
	setDefaults();
}

public void addComponentRetain(Component component)
{
	layout.setConstraints(component, constraints);
	container.add(component);
}

    /**
	 * Adds a JComponent using current settings and fixes
	 * size to xSize * ySize
	 *
	 * @param   component component to add
	 * @param   xSize  width of the component
	 * @param   ySize  height of the component
	 */
public void addComponent(JComponent component, int xSize, int ySize)
{
	Dimension size = new Dimension(xSize, ySize);
	component.setMinimumSize(size);
	component.setMaximumSize(size);
	component.setPreferredSize(size);
	layout.setConstraints(component, constraints);
	container.add(component);
	setDefaults();
}

public void addComponentRetain(JComponent component, int xSize, int ySize)
{
	Dimension size = new Dimension(xSize, ySize);
	component.setMinimumSize(size);
	component.setMaximumSize(size);
	component.setPreferredSize(size);
	layout.setConstraints(component, constraints);
	container.add(component);
}

public void resetLayout()
{
    setDefaults();
}


    /**
	 * Sets grid position
	 * Default is (0,0);
	 *
	 * @param   gridx  column
	 * @param   gridy  row
	 */
public void setPosition(int gridx, int gridy)
{
	constraints.gridx = gridx;
	constraints.gridy = gridy;
}	


	/**
	 * Sets grid span
	 * Defaults are (1,1)
	 *
	 * @param   gridwidth  span of the component in the x direction
	 * @param   gridheight  span of the component in the y direction
	 */
public void setSpan(int gridwidth, int gridheight)
{
	constraints.gridwidth = gridwidth;
	constraints.gridheight = gridheight;
}	


	/**
	 * Sets cell weights
	 * Defaults are (0.0,0.0)
	 *
	 * @param   weightx  weight of the component width
	 * @param   weighty  weight of the component height
	 */
public void setWeight(double weightx, double weighty)
{
	constraints.weightx = weightx;
	constraints.weighty = weighty;
}	


	/**
	 * Sets cell fill type as one of:
	 * GridBagConstraints.{NONE, VERTICAL, HORIZONTAL, BOTH}
	 * Default is NONE
	 *
	 * @param   fill  set how the component must fill the space
	 */
public void setFill(int fill)
{
	constraints.fill = fill;
}	


	/**
	 * Sets cell anchor type as one of:
	 * GridBagConstraints.{NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, CENTER}
	 * Default is CENTER
	 *
	 * @param   anchor  anchor position of the component
	 */
public void setAnchor(int anchor)
{
	constraints.anchor = anchor;
}	



	/**
	 * Sets cell insets
	 * Defaults are (0,0,0,0)
	 *
	 * @param   top  top inset
	 * @param   left  left inset
	 * @param   bottom  bottom inset
	 * @param   right  right inset
	 */
public void setInsets(int top, int left, int bottom, int right)
{
	 constraints.insets = new Insets(top, left, bottom, right);
}
	

	/**
	 * Sets cell defaults
	 *
	 */
private void setDefaults()
{
	 constraints.gridx = 0;
	 constraints.gridy = 0;
	 constraints.gridwidth = 1;
	 constraints.gridheight = 1;
	 constraints.fill = GridBagConstraints.NONE;
	 constraints.anchor = GridBagConstraints.CENTER;
	 constraints.weightx = 0.0;
	 constraints.weighty = 0.0;
	 constraints.insets = new Insets(0,0,0,0);
}	


}

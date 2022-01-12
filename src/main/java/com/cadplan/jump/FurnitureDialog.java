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

import com.cadplan.designer.GridBagDesigner;
import com.vividsolutions.jump.I18N;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;

/**
 * User: geoff
 * Date: 10/01/2007
 * Time: 09:19:46
 * Copyright 2005 Geoffrey G Roy.
 */
public class FurnitureDialog extends JDialog implements ActionListener, ChangeListener {

  private static final I18N i18n = I18N.getInstance("skyprinter");
  JButton cancelButton, applyButton, closeButton;
  FurnitureTitlePanel titlePanel;
  FurnitureScalePanel scalePanel;
  FurnitureBorderPanel borderPanel;
  FurnitureNorthPanel northPanel;
  FurnitureNotePanel notePanel;
  FurnitureLegendPanel legendPanel;
  LayerLegendPanel layerLegendPanel;
  FurnitureImagePanel imagePanel;
  FurnitureTitle title;
  FurnitureScale scaleItem;
  FurnitureBorder border;
  Vector<FurnitureBorder> borders;
  FurnitureNorth north;
  FurnitureNote note;
  Vector<FurnitureNote> notes;
  FurnitureLegend legend;
  LayerLegend layerLegend;
  Vector<FurnitureImage> imageItems;
  PrinterSetup parent;
  JTabbedPane tabbedPane;

  public FurnitureDialog(JDialog parent, FurnitureTitle title, FurnitureScale scaleItem,
                         FurnitureBorder border, Vector<FurnitureBorder> borders, FurnitureNorth north, Vector<FurnitureNote> notes, FurnitureLegend legend,
                         LayerLegend layerLegend, Vector<FurnitureImage> imageItems) {
    super(parent, i18n.get("JumpPrinter.Furniture"), false);
    this.title = title;
    this.scaleItem = scaleItem;
    this.border = border;
    this.borders = borders;
    this.north = north;
    this.notes = notes;
    this.legend = legend;
    this.layerLegend = layerLegend;
    this.imageItems = imageItems;
    this.parent = (PrinterSetup) parent;
    init();
  }

  private void init() {

    GridBagDesigner gb = new GridBagDesigner(this);
    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.addChangeListener(this);


    titlePanel = new FurnitureTitlePanel(title);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Title"), titlePanel);

    scalePanel = new FurnitureScalePanel(scaleItem);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Scale"), scalePanel);

    borderPanel = new FurnitureBorderPanel(this, border, borders);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Border"), borderPanel);

    northPanel = new FurnitureNorthPanel(north);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.North"), northPanel);

    FurnitureNote note = notes.elementAt(0);
    notePanel = new FurnitureNotePanel(notes);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Note"), notePanel);

    legendPanel = new FurnitureLegendPanel(legend);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Legend"), legendPanel);

    layerLegendPanel = new LayerLegendPanel(layerLegend);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.LayerLegend"), layerLegendPanel);

    imagePanel = new FurnitureImagePanel(this, imageItems);
    tabbedPane.addTab(i18n.get("JumpPrinter.Furniture.Image"), imagePanel);

    gb.setPosition(0, 0);
    gb.setFill(GridBagConstraints.BOTH);
    gb.setWeight(1.0, 1.0);
    gb.setSpan(3, 1);
    gb.addComponent(tabbedPane);


    cancelButton = new JButton(i18n.get("JumpPrinter.Furniture.Cancel"));
    gb.setPosition(0, 1);
    gb.setInsets(0, 10, 0, 10);
    gb.addComponent(cancelButton);
    cancelButton.addActionListener(this);

    applyButton = new JButton(i18n.get("JumpPrinter.Furniture.Apply"));
    gb.setPosition(1, 1);
    gb.setInsets(0, 10, 0, 10);
    gb.addComponent(applyButton);
    applyButton.addActionListener(this);

    closeButton = new JButton(i18n.get("JumpPrinter.Furniture.Close"));
    gb.setPosition(2, 1);
    gb.setInsets(0, 10, 0, 10);
    gb.setAnchor(GridBagConstraints.WEST);
    gb.setWeight(1.0, 0.0);
    gb.addComponent(closeButton);
    closeButton.addActionListener(this);

    pack();
    setVisible(true);
  }

  public void actionPerformed(ActionEvent ev) {
    if (ev.getSource() == cancelButton) {
      dispose();
    }
    if (ev.getSource() == applyButton) {

      title = titlePanel.getTitle();
      //System.err.println("Apply button getting scale");
      scaleItem = scalePanel.getScale();
      if (!scaleItem.validItem) {
        JOptionPane.showMessageDialog(this,
            i18n.get("JumpPrinter.Furniture.Scale.Message4"),
            i18n.get("JumpPrinter.Error"), JOptionPane.ERROR_MESSAGE);
        //return;
      }
      border = borderPanel.getBorderItem();
      borders = borderPanel.getBorders();
      north = northPanel.getNorth();
      note = notePanel.getNote();
      legend = legendPanel.getLegend();
      layerLegend = layerLegendPanel.getLegend();
      imageItems = imagePanel.getImageItems();

      parent.updateDrawing();

    }
    if (ev.getSource() == closeButton) {
      title = titlePanel.getTitle();
      scaleItem = scalePanel.getScale();
      border = borderPanel.getBorderItem();
      borders = borderPanel.getBorders();
      north = northPanel.getNorth();
      note = notePanel.getNote();
      legend = legendPanel.getLegend();
      layerLegend = layerLegendPanel.getLegend();
      imageItems = imagePanel.getImageItems();

      parent.updateDrawing();
      dispose();

    }
  }

  public void stateChanged(ChangeEvent ev) {
    if (ev.getSource() == tabbedPane) {
      JPanel selection = (JPanel) ((JTabbedPane) ev.getSource()).getSelectedComponent();

      if (selection == titlePanel)               // MEASURING
      {

      }

      if (selection == scalePanel) {

      }
    }
  }
}

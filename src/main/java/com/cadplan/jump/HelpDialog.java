package com.cadplan.jump;

import com.cadplan.designer.GridBagDesigner;
import com.vividsolutions.jump.I18N;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * User: geoff
 * Date: 20/02/2007
 * Time: 12:35:08
 * Copyright 2005 Geoffrey G Roy.
 */
public class HelpDialog extends JDialog implements ActionListener {

  private static final I18N i18n = I18N.getInstance("skyprinter");

  JButton cancelButton;
  JLabel label;
  String text;

  public HelpDialog(JDialog parent) {
    super(parent, i18n.get("JumpPrinter.HelpDialog"), false);
    text = "<html>" +
        "<b><h3>" + i18n.get("JumpPrinter.HelpDialog.MouseOptions") + ":</h3></b>" +
        "<b>&lt;" + i18n.get("JumpPrinter.HelpDialog.MoveOption") + "&gt;</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message1") + "<br>" +
        "<b>&lt;" + i18n.get("JumpPrinter.HelpDialog.PanOption") + "&gt;</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message2") + "<br>" +
        "<b><h3>" + i18n.get("JumpPrinter.HelpDialog.ButtonOptions") + ":</h3></b>" +
        "<b>[+]</b> - " + i18n.get("JumpPrinter.HelpDialog.ZoomIn") + "<br>" +
        "<b>[O]</b> - " + i18n.get("JumpPrinter.HelpDialog.Zoom100") + "<br>" +
        "<b>[-]</b> - " + i18n.get("JumpPrinter.HelpDialog.ZoomOut") + "<br>" +
        "<b>[" + i18n.get("JumpPrinter.Setup.LoadCfg") + "]</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message3") + "<br>" +
        "<b>[" + i18n.get("JumpPrinter.Setup.SaveCfg") + "]</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message4") + "<br>" +
        "<b>[" + i18n.get("JumpPrinter.Furniture") + "]</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message5") + "<br>" +
        "<b><h3>" + i18n.get("JumpPrinter.HelpDialog.Options") + ":</h3></b>" +
        "<b>" + i18n.get("JumpPrinter.Setup.SinglePage") + "</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message6") + "<br>" +
        "<b>" + i18n.get("JumpPrinter.Setup.Quality") + "</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message7") + "<br>" +
        "<b><h3>" + i18n.get("JumpPrinter.HelpDialog.InputFields") + ":</h3></b>" +
        "<b>" + i18n.get("JumpPrinter.Setup.Scale") + "</b> - " +
        i18n.get("JumpPrinter.HelpDialog.Message8") + "<br>" +
        "</html>";
    init();
  }

  public void init() {
    GridBagDesigner gb = new GridBagDesigner(this);
    label = new JLabel(text);
    gb.setPosition(0, 0);
    gb.setInsets(5, 20, 20, 5);
    gb.addComponent(label);

    cancelButton = new JButton(i18n.get("JumpPrinter.HelpDialog.Close"));
    gb.setPosition(0, 1);
    gb.setInsets(0, 0, 10, 0);
    gb.addComponent(cancelButton);
    cancelButton.addActionListener(this);
    pack();
    setVisible(true);
  }

  public void actionPerformed(ActionEvent ev) {
    if (ev.getSource() == cancelButton) {
      dispose();
    }
  }
}

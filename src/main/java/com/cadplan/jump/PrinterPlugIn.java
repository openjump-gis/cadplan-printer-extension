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

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.*;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.task.TaskMonitor;
import com.cadplan.fileioA.FileChooser;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.DefaultFontMapper;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.io.File;


/**
 * User: geoff
 * Date: 22/12/2006
 * Time: 07:14:57
 * Copyright 2005 Geoffrey G Roy.
 */
public class PrinterPlugIn extends AbstractPlugIn implements ThreadedPlugIn {

  private static final I18N i18n = I18N.getInstance("skyprinter");
  String version = "2.1.0";
  Blackboard blackboard;
  //final Throwable[] throwable = new Throwable[]{null};
  PrinterSetup setup;
  PrinterPanel printer;
  PrinterPreview pp;
  boolean cancelled = false;
  Dimension printSize;
  Rectangle bounds;
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
  boolean qualityOption;
  int printMode;
  String homePath;
  boolean printSinglePage = false;


  public void initialize(PlugInContext context) {

    try {
      Class.forName("com.lowagie.text.pdf.PdfWriter"); // test if VertexSymbols plugin is installed
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, "This version of JumpPrinter requires the OpenPDF library\n" +
              "to also be installed.  This is available from https://github.com/LibrePDF/OpenPDF/" +
              "\n\nError: " + ex, "Error...",
          JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
    //I18Ntext.setName("JumpPrinter");

    String fileMenuName = MenuNames.FILE;
    String menuItemName = i18n.get("JumpPrinter.MenuItem");
    EnableCheckFactory check = context.getCheckFactory();
    EnableCheck layersOK = check.createAtLeastNLayersMustExistCheck(0);


    ImageIcon icon = new ImageIcon(this.getClass().getResource("/Resources/printer.png"));
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{fileMenuName}, menuItemName, false, icon, layersOK);

    //String dirName = context.getWorkbenchContext().getWorkbench().getPlugInManager().getPlugInDirectory().getAbsolutePath();

    //System.out.println("Printer Resource path: "+this.getClass().getResource("/Resources/jprinter.gif"));
    //IconLoader loader = new IconLoader(dirName,"JumpPrinter");
    //Image image = loader.loadImage("jprinter.gif");

    icon = new ImageIcon(this.getClass().getResource("/Resources/jprinter.gif"));
    //ImageIcon icon = new ImageIcon(image);
    WorkbenchToolBar toolBar = context.getWorkbenchFrame().getToolBar();

    JButton button = toolBar.addPlugIn(icon, this, layersOK, context.getWorkbenchContext());
    button.setToolTipText(i18n.get("JumpPrinter.MenuItem"));

    blackboard = new Blackboard();
    blackboard.put("Version", version);


  }


  public boolean execute(PlugInContext context) {
    try {
      Class.forName("com.cadplan.vertex_symbols.jump.utils.VertexStyler"); // test if VertexSymbols plugin is installed
    } catch (ClassNotFoundException ex) {
      JOptionPane.showMessageDialog(null, "This version of JumpPrinter requires the VertexSymbols plugin\n" +
              "to also be installed.  This is available from http://www.cadplan.com.au", "Error...",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
    File file = null;
    try {
      file = context.getTask().getProjectFile();
    } catch (Exception ignored) {

    }
    String path;
    String fileName = "JumpPrinter.xml";

    if (file == null) {
      Properties props = System.getProperties();
      path = props.getProperty("user.dir");
    } else {
      path = file.getParent();
      fileName = file.getName();
    }
    homePath = path;

    //String printerConfigFile = fileName.substring(0,fileName.indexOf("."))+"_PrinterProperties"+".xml";
    String printerConfigFileBase = fileName.substring(0, fileName.indexOf(".")) + "_PrinterProperties";
    //JOptionPane.showMessageDialog(null,"path="+path+" filename="+fileName+" printerConfigFile="+printerConfigFile);
    File cfilep = new File(path);

    String[] cfiles = cfilep.list(new ConfigFileFilter(printerConfigFileBase));
    if (cfiles == null || cfiles.length == 0) cfiles = new String[]{"Default"};
    Vector<String> configFiles = new Vector<>();
    for (String cFile : cfiles) {
      //System.out.println(cfiles[i]);
      String name = "Default";
      String tname = null;
      if (!cFile.equals("Default")) tname = cFile.substring(printerConfigFileBase.length(), cFile.lastIndexOf("."));
      if (tname != null && tname.length() > 1) name = tname.substring(1);
      //System.out.println("Adding name:"+name);
      configFiles.addElement(name);
    }

    String previousProject = (String) blackboard.get("ConfigFilePath", "None");
    if (!previousProject.equals("None") && !previousProject.equals(path + File.separator + printerConfigFileBase)) {
      //System.out.println("Project changed");
      JOptionPane.showMessageDialog(null,
          i18n.get("JumpPrinter.Setup.Message10"),
          i18n.get("JumpPrinter.Warning"), JOptionPane.WARNING_MESSAGE);
    }
    blackboard.put("ConfigFilePath", path + File.separator + printerConfigFileBase);
    blackboard.put("ConfigFiles", configFiles);
    //blackboard.put("ConfigItem",0);


    cancelled = false;
    bounds = context.getLayerViewPanel().getBounds();
    pp = new PrinterPreview(context);
    setup = new PrinterSetup(context, pp, blackboard);
    if (setup.cancelled) {
      if (pp.sb.toString().length() > 0) display(context, pp.sb.toString());
      cancelled = true;
//            context.getLayerViewPanel().getRenderingManager().setPaintingEnabled(true);
      return true;
    }
    qualityOption = setup.getQualityOption();
    printMode = setup.getPrintMode();
    printSinglePage = setup.getPrintSinglePage();
    printSize = pp.getPrintSize();
    title = pp.getTitle();
    scaleItem = pp.getScaleItem();
    border = pp.getBorderItem();
    borders = pp.getBorders();
    north = pp.getNorth();
    note = pp.getNote();
    notes = pp.getNotes();
    legend = pp.getLegend();
    layerLegend = pp.getLayerLegend();
    imageItems = pp.getImages();
    if (pp.sb.toString().length() > 0) display(context, pp.sb.toString());
    printSinglePage = setup.getPrintSinglePage();

    return true;
  }

  public void run(TaskMonitor monitor, PlugInContext context) {
    if (cancelled) {
      return;
    }
//        try
//        {
//        ArrayList layers = (ArrayList) context.getLayerViewPanel().getLayerManager().getLayerables(Class.forName("com.vividsolutions.jump.workbench.model.Layer"));
//        ArrayList rasterlayers = (ArrayList) context.getLayerViewPanel().getLayerManager().getLayerables(Class.forName("de.fhOsnabrueck.jump.pirol.utilities.RasterImageSupport.RasterImageLayer"));
//
//            Layer [] lay = context.getSelectedLayers();
//        System.out.println("Print Layers: "+layers.size()+":"+lay.length+":"+rasterlayers.size());
//        }
//        catch(Exception ex)
//        {
//            System.out.println("ERROR: "+ex);
//        }
    monitor.allowCancellationRequests();
    monitor.report(i18n.get("JumpPrinter.Preparing"));
    PageFormat pageFormat = setup.getPageFormat();
    PrinterJob pj = PrinterJob.getPrinterJob();
    if (pageFormat == null && !setup.saveAsImage) // user has not done a page setup
    {
      try {
        pageFormat = pj.pageDialog(pj.defaultPage());
        if (pageFormat != null) blackboard.put("PageFormat", pageFormat);
        else return;
      } catch (NullPointerException ex) {
        JOptionPane.showMessageDialog(null, "Printer not defined", "Error...", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    printer = new PrinterPanel(context, printSize.width, printSize.height, pageFormat, setup.getScale());
    printer.setDrawScale(pp.getDrawScale());
    printer.setOffsets(pp.getOffsets());
    printer.setTitle(title);
    printer.setScaleItem(scaleItem);
    printer.setBorder(border);
    printer.setBorders(borders);
    printer.setNorth(north);
    printer.setNote(note);
    printer.setNotes(notes);
    printer.setLegend(legend);
    printer.setLayerLegend(layerLegend);
    printer.setImages(imageItems);
    printer.setQualityOption(qualityOption);
    printer.setPrintMode(printMode);
    printer.setPrintSinglePage(printSinglePage);

    if (!setup.saveAsImage)          // ie print it
    {

      if (!pj.printDialog()) return;
      monitor.report(i18n.get("JumpPrinter.Printing"));

      int numPages = printer.getNumberOfPages();
      if (numPages > 50) {
        int result = JOptionPane.showConfirmDialog(null,
            i18n.get("JumpPrinter.NumberOfPages") + numPages + "\n" +
                i18n.get("JumpPrinter.OkToPrint"), "Message...",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) return;
      }
      pj.setPrintable(printer, pageFormat);
      pj.setPageable(printer);
      try {
        pj.print();
      } catch (PrinterException e) {
        printer.sb.append("ERROR in printing: " + e + "\n");
      }
      if (printer.sb.toString().length() > 0) display(context, printer.sb.toString());
    } else  // save as Image
    {
//            Properties props = System.getProperties();
//            String userDir = props.getProperty("user.dir");
      Dimension imageSize = pp.getPrintSize();
      ImageSelectorDialog isd = new ImageSelectorDialog(imageSize.width, imageSize.height, i18n);
      if (isd.cancelled) return;
      String type = isd.type;  // settings from isd
      int xSize = isd.xSize;
      int ySize = isd.ySize;
      int quality = isd.quality;

      FileChooser chooser = new FileChooser(null, homePath, "image", new String[]{type}, "Image files (" + type + ")", JFileChooser.SAVE_DIALOG);

      String dirName = chooser.getDir();
      String fileName = chooser.getFile();
      if (fileName == null) return;
      monitor.report(i18n.get("JumpPrinter.SavingImage"));

      try {
        //System.out.println("initial file:"+ fileName);

        if (fileName.lastIndexOf(".") < 0) {
          fileName = fileName + "." + type; // default = JPG
        }

        //System.out.println("file: "+dirName+File.separator+fileName+ " type="+type);

        if (type.equalsIgnoreCase("svg")) {
          //System.out.println("Preparing SVG image");
          DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
          String svgNS = "http://www.w3.org/2000/svg";
          Document document = domImpl.createDocument(svgNS, "svg", null);

          SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
          double svgFactor = isd.svgFactor;
          printer.setDrawingScaleFactor(svgFactor);
          imageSize = new Dimension((int) (imageSize.width * svgFactor), (int) (imageSize.height * svgFactor));
          //System.out.println("SVG image size: "+imageSize.width+","+imageSize.height+"  >>  factor="+svgFactor);
          svgGenerator.setSVGCanvasSize(imageSize);
          svgGenerator.setColor(Color.WHITE);
          svgGenerator.fillRect(0, 0, imageSize.width + 5, imageSize.height + 5);
          printer.paint(svgGenerator);
          boolean useCSS = true;
          Writer out = new OutputStreamWriter(new FileOutputStream(dirName + File.separator + fileName), "UTF-8");
          svgGenerator.stream(out, useCSS);

        }
        else if (type.equalsIgnoreCase("pdf")) {
          double xoffset = 50.0; //pageFormat.getImageableX();
          double yoffset = 50.0; //pageFormat.getImageableY();
          double xsize = 2 * xoffset + imageSize.width;
          double ysize = 2 * yoffset + imageSize.height;
          try {
            com.lowagie.text.Rectangle pageSize = new com.lowagie.text.Rectangle((float) xsize, (float) ysize);
            //System.out.println("xoffset="+xoffset+ "  yoffset="+yoffset+"  xsize="+xsize+"  ysize="+ysize);
            com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize);
            document.addCreator("Cadplan OpenJump Printer Plugin");
            document.addTitle(fileName);
            document.addAuthor("Geoffrey G. Roy, www.cadplan.com.au");
            document.addSubject("PDF file");
            //com.itextpdf.text.Rectangle pages = document.getPageSize();
            //System.out.println("Page size = "+pages);
            try {
              PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dirName + File.separator + fileName));
              document.open();
              PdfContentByte cb = writer.getDirectContent();
              //PdfTemplate tp = cb.createTemplate((int)xsize,(int)ysize);
              DefaultFontMapper fontMapper = new DefaultFontMapper();
              //fontMapper.insertDirectory("c:/windows/fonts");


              Graphics2D graphics2D = cb.createGraphics((int) xsize, (int) ysize, fontMapper);
              graphics2D.translate(xoffset, yoffset);
              printer.paint(graphics2D);
              graphics2D.dispose();
            } catch (Exception ex) {
              System.out.println("ERROR creating pdf: " + ex);
              ex.printStackTrace();
            }
            document.close();
          } catch (Exception ex) {
            System.out.println("ERROR: " + ex);
            JOptionPane.showMessageDialog(null, "To save images as PDF files, the iText library must be available", "Error...",
                JOptionPane.ERROR_MESSAGE);
          }
        }
        else if (type.equalsIgnoreCase("jpg") ||
            type.equalsIgnoreCase("png")) //jpg and png

        {
          double scalex = (double) xSize / (double) imageSize.width;
          double scaley = (double) ySize / (double) imageSize.height;
          //System.out.println("Scales x="+scalex+"  y="+scaley+"  xSize="+xSize+"  ySize="+ySize);
          if (type.equalsIgnoreCase("png")) {
            //System.out.println("creating image type="+type);
            //BufferedImage bimage = new BufferedImage(imageSize.width+5, imageSize.height+5, BufferedImage.TYPE_INT_RGB);
            BufferedImage bimage = new BufferedImage(xSize + 5, ySize + 5, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = bimage.createGraphics();
            ig.setColor(Color.WHITE);

            //ig.fillRect(0,0,imageSize.width+5, imageSize.height+5);
            ig.fillRect(0, 0, xSize + 5, ySize + 5);
            ig.scale(scalex, scaley);
            printer.paint(ig);
            ImageIO.write(bimage, type, new File(dirName + File.separator + fileName));
          } else {
            //BufferedImage bimage = new BufferedImage(imageSize.width+5, imageSize.height+5, BufferedImage.TYPE_INT_RGB);
            BufferedImage bimage = new BufferedImage(xSize + 5, ySize + 5, BufferedImage.TYPE_INT_RGB);
            Graphics2D ig = bimage.createGraphics();
            ig.setColor(Color.WHITE);
            //ig.fillRect(0,0,imageSize.width+5, imageSize.height+5);
            ig.fillRect(0, 0, xSize + 5, ySize + 5);
            ig.scale(scalex, scaley);
            printer.paint(ig);

            //System.out.println("Image size:"+bimage.getWidth()+"x"+bimage.getWidth());
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality((float) (quality / 100.0));
            File file = new File(dirName + File.separator + fileName);
            FileImageOutputStream output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage image = new IIOImage(bimage, null, null);
            writer.write(null, image, iwp);
            writer.dispose();
          }
        }
      } catch (IOException ex) {
        System.out.println("File write error: " + ex);
      } catch (Exception ex) {
        System.out.println("File type error: " + ex);
      }
    }
    //context.getLayerViewPanel().getRenderingManager().setPaintingEnabled(true);
  }

  public void display(PlugInContext context, String text) {
    context.getWorkbenchFrame().getOutputFrame().createNewDocument();
    context.getWorkbenchFrame().getOutputFrame().addText(text);
    context.getWorkbenchFrame().getOutputFrame().surface();
  }

  static class ConfigFileFilter implements FilenameFilter {
    String filename;

    ConfigFileFilter(String filename) {
      this.filename = filename;
    }

    public boolean accept(File dir, String name) {
      return name.toLowerCase(Locale.ROOT).endsWith(".xml")
          && name.contains(filename);
    }
  }
}

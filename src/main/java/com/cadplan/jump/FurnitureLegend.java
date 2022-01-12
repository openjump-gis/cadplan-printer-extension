package com.cadplan.jump;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.*;

import com.cadplan.vertex_symbols.vertices.renderer.style.AnyShapeVertexStyle;
import com.cadplan.vertex_symbols.vertices.renderer.style.ExternalSymbolsImplType;
import com.cadplan.vertex_symbols.vertices.renderer.style.ImageVertexStyle;
import com.cadplan.vertex_symbols.vertices.renderer.style.PolygonVertexStyle;
import com.cadplan.vertex_symbols.vertices.renderer.style.StarVertexStyle;
import com.cadplan.vertex_symbols.vertices.renderer.style.WKTVertexStyle;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;

/**
 * User: geoff
 * Date: 12/02/2007
 * Time: 14:47:51
 * Copyright 2005 Geoffrey G Roy.
 */
public class FurnitureLegend  extends Furniture {

	private final I18N i18n = I18N.getInstance("skyprinter");
	private final boolean debug = false;
	Vector<LegendElement> legendItems;
	boolean border = true;
	String legendName = null;
	boolean showTitle = true;
	Font legendFont = new Font("SansSerif", Font.PLAIN, 12);
	Color legendTitleColor = Color.BLACK;
	Color legendTextColor = Color.BLACK;
	Color legendBorderColor = Color.BLACK;
	int yspace = 14;
	int width = 48;  // must be 4*height
	int height = 12;
	int heightMin = 12;

	//Color noColor = Color.WHITE;
	double fscale = 1.0;
	int size;

	public FurnitureLegend(PlugInContext context, Rectangle location)
	{
		this.location = location;
		legendItems = new Vector<>(10,5);
		updateLegend(context);
		layerNumber = 60;
	}


	public void updateLegend(PlugInContext context)
	{
		Vector<LegendElement> tempItems = new Vector<>(10,5);
		boolean showLine;
		boolean showFill;
		if(i18n != null && legendName == null) legendName= i18n.get("JumpPrinter.Furniture.Legend");
		if(legendItems != null)
		{
			for (int i=0; i < legendItems.size(); i++)
			{
				tempItems.addElement(legendItems.elementAt(i));
			}
		}
		legendItems = new Vector<>(10,5);
		List<Layer> layerCollection = context.getLayerViewPanel().getLayerManager().getVisibleLayers(true);

		Iterator<Layer> i = layerCollection.iterator();
		int count = 0;
		while (i.hasNext())
		{
			Layer layer = i.next();
			String name = layer.getName();
			String desc = layer.getDescription();
			if(debug) System.out.println("layer "+count+":"+name+","+desc);
			BasicStyle basicStyle = layer.getBasicStyle();
			VertexStyle vertexStyle = layer.getVertexStyle();
			Collection<BasicStyle> themeStyles = null;

			ColorThemingStyle themeStyle = (ColorThemingStyle) layer.getStyle(ColorThemingStyle.class);
			if(debug) System.out.println("       Theming enabled: "+ themeStyle.isEnabled());
			if(themeStyle.isEnabled())
			{
				Map<Object,BasicStyle> themeMap =   themeStyle.getAttributeValueToBasicStyleMap();
				if(debug) System.out.println("Map:"+themeMap.toString());
				themeStyles = themeMap.values();

			}
			int alpha = basicStyle.getAlpha();
			Color lineColor = basicStyle.getLineColor();
			lineColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), alpha);
			showLine = true;
			if(!basicStyle.isRenderingLine()) showLine = false;

			Color fillColor = basicStyle.getFillColor();
			fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
			showFill = true;

			if(!basicStyle.isRenderingFill()) showFill = false;



			Stroke lineStroke = basicStyle.getLineStroke();
			Paint fillPattern = basicStyle.getFillPattern();
			if(!basicStyle.isRenderingFillPattern())  fillPattern = null;

			//System.out.println("        LineColor: "+basicStyle.getLineColor()+"  FillColor:"+basicStyle.getFillColor());
			// create new legend item, include by default
			LegendElement legendElement = new LegendElement(true, name, lineColor, lineStroke, fillColor, fillPattern,
					themeStyles, null, vertexStyle, showLine, showFill);

			//set include on existing items
			for (int k=0; k < tempItems.size(); k++)
			{
				LegendElement anitem = tempItems.elementAt(k);
				if(anitem.name.equals(legendElement.name))
				{
					legendElement.include = anitem.include;
				}
			}
			legendItems.addElement(legendElement);
			if(debug) System.out.println(legendElement);
			count ++;
		}
	}



	public void paint(Graphics g, double scale, double globalScale, int printingMode)
	{
		Graphics2D g2 = (Graphics2D) g;
		Font bigFont = new Font(legendFont.getFontName(), Font.BOLD,  legendFont.getSize()+2);
		Font smallFont = legendFont; //new Font("SansSerif", Font.PLAIN, size);
		FontMetrics fm = getFontMetrics(smallFont);
		FontMetrics fmt = getFontMetrics(bigFont);
		double maxVertexSize = 0.0;
		double vertexSize;
		yspace = (int) (1.5 * fm.getHeight());//**
		int y=0;
		height = heightMin;

		width = height*4;
		if(yspace > 2*height) 
		{
			height = yspace/2;
			width = 4*height;
		}
		int maxlen = 2*width; //fmt.stringWidth("Legend");
		if(scale > 0.0)
		{
			g2.scale(scale, scale);
			fscale = scale;
		}
		float factor = 1.0f;
		if(printingMode == 2) factor = (float) (1.0/MapImagePrinter.drawFactor);
		if(printingMode != 2) factor = (float) fscale;   // to fix line properties!
		g.setColor(legendTitleColor);
		g.setFont(bigFont);
		String name = legendName; 

		if(name == null) name = i18n.get("JumpPrinter.Furniture.Legend");
		int nameWidth = fmt.stringWidth(name);
		if(showTitle) 
		{
			g.drawString(name,location.x+5, location.y+yspace);
			y = y+yspace+5;
			if(nameWidth > maxlen) maxlen = nameWidth - width;
		}

		int extraBottomSpace = 0;


		for (int i=0; i < legendItems.size(); i++)
		{
			LegendElement element = legendItems.elementAt(i);
			VertexStyle vertexStyle = element.vertexStyle;
			vertexSize = vertexStyle.getSize();
			if(vertexStyle.getSize() > maxVertexSize) maxVertexSize = vertexStyle.getSize();

			int xp, yp;
			int xp0, yp0;

			y = y + yspace + extraBottomSpace;
			xp0 = ((location.x+5));
			yp0 = location.y+y-height;
			float lineWidth = 0.0f;

			if(element.include)
			{
				//y = y + yspace + extraBottomSpace;
				if(element.themeStyles == null)
				{
					g.setColor(element.fillColor);
					lineWidth = ((BasicStroke)element.lineStroke).getLineWidth()/factor;
					if(lineWidth < 0.0) lineWidth = 0.5f;
					int extraSpace = (int) (lineWidth);
					if(vertexStyle.getSize()/2 > extraSpace) extraSpace = vertexStyle.getSize()/2;
					y = y + extraSpace+3;
					extraBottomSpace = 0;
					if(vertexStyle.getSize()/2 > height) extraBottomSpace = vertexStyle.getSize()/2 - height;

					//System.out.println("height="+height+" size="+vertexStyle.getSize()+" extraBottmomSpace="+extraBottomSpace);


					if(element.fillPattern != null)
					{
						//TexturePaint paint = (TexturePaint)element.fillPattern;
						//BufferedImage textureImage = paint.getImage();
						//Rectangle2D  rect = ((TexturePaint)element.fillPattern).getAnchorRect();
						//Rectangle2D scaledRect = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth()*patternScale, rect.getHeight()*patternScale);
						//TexturePaint newPaint = new TexturePaint(textureImage,scaledRect);
						//g2.setPaint(newPaint);
						g2.setPaint(element.fillPattern);

					}
					xp0 = ((location.x+5));
					yp0 = location.y+y-height;
					if(element.showFill) g.fillRect(xp0, yp0, width, height);

					g.setColor(element.lineColor);

					Stroke newStroke;
					float[] dashArray = ((BasicStroke)element.lineStroke).getDashArray();
					float[] newArray;
					if(dashArray != null)
					{
						if(dashArray.length == 1)
						{
							newArray = new float[2];
							newArray[0] = dashArray[0]/factor;
							if(newArray[0] < 0.5) newArray[0] = 0.5f;
							newArray[1] = newArray[0];
							//System.out.println("dash : "+newArray[0]+","+newArray[1]);
						}
						else
						{
							newArray = new float[dashArray.length];
							for (int j=0; j < dashArray.length; j++)

							{
								newArray[j] = (float) Math.floor(dashArray[j]/factor);
								if(newArray[j] < 0.5) newArray[j] = 0.5f;
								//System.out.println("dash "+j+": "+newArray[j]);
							}
						}
						newStroke = new BasicStroke(lineWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10.0f,newArray,0.0f);
					}
					else
					{
						newStroke = new BasicStroke(lineWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
					}
					g2.setStroke(newStroke);
					if(element.showLine) g.drawLine(xp0, Math.round(yp0-lineWidth/2), xp0+width, Math.round(yp0-lineWidth/2));
					g2.setStroke(normalStroke);
					//                       xp = xp0+width*3 ;
					//                       yp = (int)Math.round(yp0-lineWidth/2);
				}
				else
				{
					Iterator<BasicStyle> it = element.themeStyles.iterator();
					int count = 0;
					int extraSpace = (int) (lineWidth);
					if(vertexStyle.getSize()/2 > extraSpace) extraSpace = vertexStyle.getSize()/2;
					y = y + extraSpace+3;
					yp0 = location.y+y-height;
					while (it.hasNext() && count < 4)
					{
						BasicStyle style = it.next();
						Color color = style.getFillColor();
						int alpha = style.getAlpha();
						Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
						g.setColor(fillColor);
						g2.setPaint(style.getFillPattern());
						g.fillRect(location.x+5+count*height, location.y+y-height, height, height);
						if(debug) System.out.println("    count="+count+" color:+"+fillColor);
						count++;
					}
				}

				// draw vertices
				//xp = xp0+width*3 ;
				xp = xp0+width;//**
				yp = Math.round(yp0-lineWidth/2);

				boolean symbols = false;
				if(vertexStyle.isEnabled())
				{
					try
					{
						Class.forName("com.cadplan.jump.VertexSymbols"); // test if VertexSymbols plugin is installed

						if(vertexStyle instanceof ExternalSymbolsImplType)
						{
							ExternalSymbolsImplType vertex = new ExternalSymbolsImplType();
							vertex.setColors(element.lineColor, element.fillColor);
							//System.out.println("Colors: "+element.lineColor+"::"+element.fillColor);
							vertex.setNumSides(((ExternalSymbolsImplType)vertexStyle).getNumSides());
							vertex.setOrientation(((ExternalSymbolsImplType)vertexStyle).getOrientation());
							vertex.setDotted(((ExternalSymbolsImplType)vertexStyle).getDotted());
							vertex.setShowLine(((ExternalSymbolsImplType)vertexStyle).getShowLine());
							vertex.setShowFill(((ExternalSymbolsImplType)vertexStyle).getShowFill());
							vertex.setSize((int)(((ExternalSymbolsImplType)vertexStyle).getSize()/factor));
							vertex.setSymbolName(((ExternalSymbolsImplType)vertexStyle).getSymbolName());
							vertex.setSymbolType(((ExternalSymbolsImplType)vertexStyle).getSymbolType());
							//System.out.println("Legend: symbolname="+vertex.getSymbolName()+" type="+vertex.getSymbolType());
							//System.out.println("showLine="+vertex.getShowLine()+"  fill:"+vertex.getShowFill());
							//System.out.println("xp="+xp+"  yp="+yp);
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D)g,new Point2D.Double(xp,yp));
							symbols = true;

						}
						else if(vertexStyle instanceof PolygonVertexStyle)
						{
							PolygonVertexStyle vertex = new PolygonVertexStyle();
							vertex.setColors(element.lineColor, element.fillColor);
							vertex.setNumSides(((PolygonVertexStyle)vertexStyle).getNumSides());
							vertex.setOrientation(((PolygonVertexStyle)vertexStyle).getOrientation());
							vertex.setDotted(((PolygonVertexStyle)vertexStyle).getDotted());
							vertex.setShowLine(((PolygonVertexStyle)vertexStyle).getShowLine());
							vertex.setShowFill(((PolygonVertexStyle)vertexStyle).getShowFill());
							vertex.setSize((int)(((PolygonVertexStyle)vertexStyle).getSize()/factor));
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D)g,new Point2D.Double(xp,yp));
							symbols = true;

						}
						else if(vertexStyle instanceof  StarVertexStyle)
						{
							StarVertexStyle vertex = new StarVertexStyle();
							vertex.setColors(element.lineColor, element.fillColor);
							vertex.setNumSides(((StarVertexStyle)vertexStyle).getNumSides());
							vertex.setOrientation(((StarVertexStyle)vertexStyle).getOrientation());
							vertex.setDotted(((StarVertexStyle)vertexStyle).getDotted());
							vertex.setShowLine(((StarVertexStyle)vertexStyle).getShowLine());
							vertex.setShowFill(((StarVertexStyle)vertexStyle).getShowFill());
							vertex.setSize((int)(((StarVertexStyle)vertexStyle).getSize()/factor));
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D) g,new Point2D.Double(xp,yp));
							symbols = true;

						}
						else if(vertexStyle instanceof  AnyShapeVertexStyle)
						{
							AnyShapeVertexStyle vertex = new AnyShapeVertexStyle();
							vertex.setColors(element.lineColor, element.fillColor);
							vertex.setType(((AnyShapeVertexStyle)vertexStyle).getType());
							vertex.setOrientation(((AnyShapeVertexStyle)vertexStyle).getOrientation());
							vertex.setDotted(((AnyShapeVertexStyle)vertexStyle).getDotted());
							vertex.setShowLine(((AnyShapeVertexStyle)vertexStyle).getShowLine());
							vertex.setShowFill(((AnyShapeVertexStyle)vertexStyle).getShowFill());
							vertex.setSize((int)(((AnyShapeVertexStyle)vertexStyle).getSize()/factor));
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D) g,new Point2D.Double(xp,yp));
							symbols = true;

						}
						else if(vertexStyle instanceof  ImageVertexStyle)
						{
							ImageVertexStyle vertex = new ImageVertexStyle();
							vertex.setOrientation(((ImageVertexStyle)vertexStyle).getOrientation());
							vertex.setScale((((ImageVertexStyle)vertexStyle).getScale()*factor));
							vertex.setName(((ImageVertexStyle)vertexStyle).getName());
							vertex.setSize((int)(((ImageVertexStyle)vertexStyle).getSize()/factor));
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D)g,new Point2D.Double(xp,yp));
							symbols = true;

						}
						else if(vertexStyle instanceof  WKTVertexStyle)
						{
							WKTVertexStyle vertex = new WKTVertexStyle();
							vertex.setColors(element.lineColor, element.fillColor);                                
							vertex.setOrientation(((WKTVertexStyle)vertexStyle).getOrientation());
							vertex.setScale((((WKTVertexStyle)vertexStyle).getScale()*factor));
							vertex.setName(((WKTVertexStyle)vertexStyle).getName());
							vertex.setShowLine(((WKTVertexStyle)vertexStyle).getShowLine());
							vertex.setShowFill(((WKTVertexStyle)vertexStyle).getShowFill());

							vertex.setSize((int)(((WKTVertexStyle)vertexStyle).getSize()/factor));
							vertex.setByValue(true);
							vertex.setAttributeName("");

							vertex.paint((Graphics2D)g,new Point2D.Double(xp,yp));
							symbols = true;

						}
					}
					catch(ClassNotFoundException ex)  // VertexSymbols plugin not installed
					{
						Logger.warn("VertexSymbolPlugIn is not installed");
					}
				}
				if(!symbols && vertexStyle.isEnabled())
				{
					GeneralPath path = new GeneralPath();
					int size = (vertexStyle.getSize()) ;//factor);

					path.moveTo(xp-size/2,yp-size/2);
					path.lineTo(xp+size/2,yp-size/2);
					path.lineTo(xp+size/2,yp+size/2);
					path.lineTo(xp-size/2,yp+size/2);
					path.lineTo(xp-size/2,yp-size/2);
					if(!element.showLine && !element.showFill) element.showLine = true;
					if(element.showLine)
					{
						g2.setColor(element.fillColor);
						g2.fill(path);
					}
					if(element.showFill)
					{
						g2.setColor(element.lineColor);
						g2.draw(path);
					}
				}

				g.setFont(smallFont);
				g.setColor(legendTextColor);
				int fontHeight = fm.getHeight();
				int texty = yp0 + fontHeight/2;
				//                  g.drawString(element.name,location.x+width+(int)maxVertexSize/2+10,location.y+y);
				g.drawString(element.name,location.x + (int)vertexSize/2 + 10, texty);

				int len = fm.stringWidth(element.name);
				if(len > maxlen) maxlen = len;

			}
		}
		g2.setStroke(new BasicStroke());
		location.height = y + yspace  + extraBottomSpace ;

		g.setColor(legendBorderColor);
		location.width = maxlen+10+width+(int)(maxVertexSize/2); //extraBottomSpace;
		if (border) g.drawRect(location.x, location.y, location.width, location.height);

		if (scale > 0.0)
		{
			g2.setColor(boundsColor);
			g2.setStroke(boundsStroke);
			g2.drawRect(location.x-2, location.y-2, location.width+4,  location.height+4);
			g2.setStroke(normalStroke);
			g2.scale(1.0/scale, 1.0/scale);
		}
	}

}

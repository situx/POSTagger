/*
 *  Copyright (C) 2017. Timo Homburg
 *  This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *   You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.github.situx.postagger.main.gui.util;

import javax.swing.text.*;
import java.awt.*;

/*
 *  Implements a simple highlight painter that renders a rectangle around the
 *  area to be highlighted.
 *
 */
public class RectanglePainter extends DefaultHighlighter.DefaultHighlightPainter
{
    private Color fillColor;

    private Color borderColor;

    public RectanglePainter(Color borderColor, Color fillColor)
    {
        super(borderColor);
        this.fillColor=fillColor;
        this.borderColor=borderColor;
    }

    /**
     * Paints a portion of a highlight.
     *
     * @param  g the graphics context
     * @param  offs0 the starting model offset >= 0
     * @param  offs1 the ending model offset >= offs1
     * @param  bounds the bounding box of the view, which is not
     *	       necessarily the region to paint.
     * @param  c the editor
     * @param  view View painting for
     * @return region drawing occured in
     */
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view)
    {
        Rectangle r = getDrawingArea(offs0, offs1, bounds, view);

        if (r == null) return null;

        //  Do your custom painting

        Color color = getColor();
        g.setColor(fillColor);

        //  Code is the same as the default highlighter except we use drawRect(...)

		g.fillRect(r.x, r.y, r.width, r.height);
        if(borderColor!=null){
            g.setColor(borderColor);
            g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        }


        // Return the drawing area

        return r;
    }


    public void setBorderColor(Color color){
        this.borderColor=color;
    }

    public void setFillColor(Color color){
        this.fillColor=color;
    }


    private Rectangle getDrawingArea(int offs0, int offs1, Shape bounds, View view)
    {
        // Contained in view, can just use bounds.

        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset())
        {
            Rectangle alloc;

            if (bounds instanceof Rectangle)
            {
                alloc = (Rectangle)bounds;
            }
            else
            {
                alloc = bounds.getBounds();
            }

            return alloc;
        }
        else
        {
            // Should only render part of View.
            try
            {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1,Position.Bias.Backward, bounds);
                Rectangle r = (shape instanceof Rectangle) ? (Rectangle)shape : shape.getBounds();

                return r;
            }
            catch (BadLocationException e)
            {
                // can't render
            }
        }

        // Can't render

        return null;
    }
}

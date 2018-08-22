/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.github.situx.postagger.main.gui.ime.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.SwingConstants;

/**
 * Icons that can be used in a component tha needs paging icons.
 * Just draws a triangle in the direction of the icon.
 * Icons are drawn dynamically so no image is needed.
 * 
 * @author Jordan Kiang
 */
public class PagingIcon implements Icon {
 
	private int direction;
    private Polygon arrow;
    
    private int width;
    private int height;
        
    private Color fg;
    private Color bg;

    ////////////////////
    
    /**
     * Build a PagingIcon with chosen foreground and background Colors.
     * @param direction the direction, one of SwingConstants.TOP, LEFT, BOTTOM, or RIGHT
     * @param width the width in pixels
     * @param height the height in pixels
     */
    public PagingIcon(int direction, int width, int height) {
    	if(direction < SwingConstants.TOP || direction > SwingConstants.RIGHT) {
        	// check relies on SwingConstants not being redefined non consecutively or in a different order...
        	throw new IllegalArgumentException("direction should one of SwingConstants.TOP, LEFT, BOTTOM, or RIGHT!");
        } else if(width <= 0 || height <= 0) {
        	throw new IllegalArgumentException("width and height dimensions must be positive!");
        }
        
        this.direction = direction;
        this.arrow = buildPagingArrow(direction, width, height);
        
        this.width = width;
        this.height = height;
    }
    
    ////////////////////
    
    /**
     * @return the drection of the icon, one of SwingConstants.TOP, LEFT, BOTTOM, RIGHT
     */
    public int getDirection() {
        return this.direction;
    }
    
    /**
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return this.width;
    }
   
    /**
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return this.height;
    }
    
    /**
     * @param bg the bacground color of the icon
     */
    public void setBackground(Color bg) {
    	this.bg = bg;
    }
    
    /**
     * @return the background color of the icon
     */
    public Color getBackground() {
    	return this.bg;
    }
    
    /**
     * @param fg the foreground color of the icon (color of the paging arrow)
     */
    public void setForeground(Color fg) {
    	this.fg = fg;
    }
    
    /**
     * @return the foreground color of the icon (color of the paging arrow)
     */
    public Color getForeground() {
    	return this.fg;
    }
    
    ////////////////////
    
    /**
     * @see Icon#paintIcon(Component, Graphics, int, int)
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Color inColor = g.getColor();
    	Graphics2D g2D = (Graphics2D)g;
        
        // render nice smooth antialiased triangle arrows
        RenderingHints savedHints = g2D.getRenderingHints();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color savedColor = g2D.getColor();
        g2D.translate(x, y);
        
        // use Swingish defaults if no colors are specified
        Color bg = null != this.bg ? this.bg : Color.LIGHT_GRAY;
        Color fg = null != this.fg ? this.fg : Color.BLACK;
        
        // fill the icon with the background color
        g2D.setColor(bg);
        g2D.fillRect(0, 0, this.width, this.height);
        
        // paint the arrow over in the foreground
        g2D.setColor(fg);
        g2D.fillPolygon(this.arrow);
        
        g2D.setRenderingHints(savedHints);
        g2D.setColor(savedColor);
        g2D.translate(-x, -y);
        
        // restore the color
        g.setColor(inColor);
    }
    
    /////////////////////////////////////////////////////
    // helper methods build triangular shaped arrow icons
    
    static private Polygon buildPagingArrow(int direction, int width, int height) {
        switch(direction) {
        case SwingConstants.TOP:
        	return buildUpArrow(width, height);
        case SwingConstants.LEFT:
        	return buildLeftArrow(width, height);
        case SwingConstants.BOTTOM:
        	return buildDownArrow(width, height);
        case SwingConstants.RIGHT:
        	return buildRightArrow(width, height);
        }
        
        // should have returned above
        throw new IllegalArgumentException("invalid direction!");
    }
    
    static private Polygon buildLeftArrow(int width, int height) {
        int[] xPoints = {width, 0, width};
        int[] yPoints = {0, height / 2, height};
        
        return new Polygon(xPoints, yPoints, 3);
    }
    
    static private Polygon buildRightArrow(int width, int height) {
        int[] xPoints = {0, width, 0};
        int[] yPoints = {0, height / 2, height};
        
        return new Polygon(xPoints, yPoints, 3);
    }

    static private Polygon buildUpArrow(int width, int height) {
        int[] xPoints = {0, width / 2, width};
        int[] yPoints = {height, 0, height};
    
        return new Polygon(xPoints, yPoints, 3);
    }
    
    static private Polygon buildDownArrow(int width, int height) {
        int[] xPoints = {0, width / 2, width};
        int[] yPoints = {0, height, 0};
        
        return new Polygon(xPoints, yPoints, 3);
    }

}

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

import com.github.situx.postagger.main.gui.tool.MainFrame;
import com.github.situx.postagger.util.VTextIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Created by timo on 4/12/15.
 */
public class DragTabbedPane extends JTabbedPane {
    private boolean dragging = false;
    private Image tabImage = null;
    private Point currentMouseLocation = null;
    private int draggedTabIndex = 0;

    public DragTabbedPane(final TabEnum enumpos,final Boolean vertical){
        super();
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {

                if(!dragging) {
                    // Gets the tab index based on the mouse position
                    int tabNumber = getUI().tabForCoordinate(DragTabbedPane.this, e.getX(), e.getY());

                    if(tabNumber >= 0) {
                        draggedTabIndex = tabNumber;
                        Rectangle bounds = getUI().getTabBounds(DragTabbedPane.this, tabNumber);


                        // Paint the tabbed pane to a buffer
                        Image totalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics totalGraphics = totalImage.getGraphics();
                        totalGraphics.setClip(bounds);
                        // Don't be double buffered when painting to a static image.
                        setDoubleBuffered(false);
                        paintComponent(totalGraphics);

                        // Paint just the dragged tab to the buffer
                        tabImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
                        Graphics graphics = tabImage.getGraphics();
                        graphics.drawImage(totalImage, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y+bounds.height, DragTabbedPane.this);

                        dragging = true;
                        repaint();
                    }
                } else {
                    currentMouseLocation = e.getPoint();

                    // Need to repaint
                    repaint();
                }

                super.mouseDragged(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {

                if(dragging) {
                    int tabNumber = getUI().tabForCoordinate(DragTabbedPane.this, e.getX(), 10);

                    if(tabNumber >= 0) {
                        Component comp = getComponentAt(draggedTabIndex);
                        String title = getTitleAt(draggedTabIndex);
                        Icon icon=getIconAt(draggedTabIndex);
                        removeTabAt(draggedTabIndex);
                        insertTab(title, icon, comp,enumpos, tabNumber);
                    }
                }

                dragging = false;
                tabImage = null;
            }
        });
    }


    public void insertTab(final String title,final Component component,final TabEnum tabIndex){
        this.insertTab(title,null,component,tabIndex,null);
    }

        public void insertTab(final String title,Icon icon,final Component component,final TabEnum tabIndex,final Integer pos){
        MainFrame.displayedTabs.get(tabIndex).add(title);
        if(tabIndex.getVertical()){
            this.addTab(null, new VTextIcon(component, title), component, title);
        } else{
            this.addTab(title, component);
        }
        int index=this.getTabCount()-1;
        if(pos!=null){
           index=pos;
        }

        JPanel pnlTab = new JPanel(new GridBagLayout());
        pnlTab.setOpaque(false);
        JButton btnClose = new JButton("x");
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.addActionListener(e -> {
            int i;
            for(i=0;i<=DragTabbedPane.this.getTabCount()-1;i++)//To find current index of tab
            {
                if(title.equals(DragTabbedPane.this.getTitleAt(i)))
                    break;
            }
            DragTabbedPane.this.removeTabAt(i);
            MainFrame.displayedTabs.get(tabIndex).remove(title);
        });
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel lblTitle = new JLabel();
        if(tabIndex.getVertical()){
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            if(icon!=null){
                lblTitle.setIcon(icon);
            }else{
                lblTitle.setIcon(new VTextIcon(lblTitle,title));
            }
            pnlTab.add(lblTitle, gbc);

            gbc.gridy++;
            gbc.weightx = 0;
            pnlTab.add(btnClose, gbc);
        }else{

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            lblTitle.setText(title);
            pnlTab.add(lblTitle, gbc);

            gbc.gridx++;
            gbc.weightx = 0;
            pnlTab.add(btnClose, gbc);
        }

        this.setTabComponentAt(index, pnlTab);
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Are we dragging?
        if(dragging && currentMouseLocation != null && tabImage != null) {
            // Draw the dragged tab
            g.drawImage(tabImage, currentMouseLocation.x, currentMouseLocation.y, this);
        }
    }

}

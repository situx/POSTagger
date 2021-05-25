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

import com.github.situx.postagger.dict.pos.POSTagger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

/**
 * Created by timo on 3/24/15.
 */
public class POSTableRenderer extends DefaultTableCellRenderer {
    /**Pattern for cells to be colored.*/
    private static final Pattern DOUBLE_PATTERN = Pattern.compile(
            "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
                    "([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|" +
                    "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
                    "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
    /**The current default background color used for description cells.*/
    Color backgroundColor = getBackground();

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        final Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        this.setOpaque(true);
        if(column==2){
            if(POSTagger.isRegex(value.toString())){
                c.setBackground(Color.green);
            }else{
                c.setBackground(Color.red);
            }
        }else{
            c.setBackground((column==table.getColumnCount()-1) && value.toString().startsWith("#")? POSTagger.parseHTMLColor(value.toString()) : Color.white);
        }
        if(column==table.getColumnCount()-1){
            TableModel model = table.getModel();
            if(model.getValueAt(row, column)==null)
                c.setForeground((Color) value);
            c.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent mouseEvent) {
                    Color newColor = JColorChooser.showDialog(
                            new JColorChooser(),
                            "Choose Color",
                            Color.yellow);
                    c.setBackground(newColor);
                }
            });
        }

        return c;
        /*JTextField editor = new JTextField();
        if (value != null)
            editor.setText(value.toString());

        return editor;
        /*TableModel model = (TableModel) table.getModel();
        if(model.getValueAt(row, column)==null){
            c.setBackground(backgroundColor);
        }else if (DOUBLE_PATTERN.matcher(model.getValueAt(row, column).toString()).matches() && Double.valueOf(model.getValueAt(row,column).toString())>=75.) {
            c.setBackground(Color.green);
        } else if (DOUBLE_PATTERN.matcher(model.getValueAt(row, column).toString()).matches() && Double.valueOf(model.getValueAt(row,column).toString())<75. && Double.valueOf(model.getValueAt(row,column).toString())>=50.) {
            c.setBackground(Color.yellow);
        }else if (DOUBLE_PATTERN.matcher(model.getValueAt(row, column).toString()).matches() && Double.valueOf(model.getValueAt(row,column).toString())<50. && Double.valueOf(model.getValueAt(row,column).toString())>=25.) {
            c.setBackground(Color.orange);
        } else if (DOUBLE_PATTERN.matcher(model.getValueAt(row, column).toString()).matches() && Double.valueOf(model.getValueAt(row,column).toString())<25. && Double.valueOf(model.getValueAt(row,column).toString())>=0.) {
            c.setBackground(Color.red);
        }else{
            c.setBackground(backgroundColor);
        }*/
        //return c;
    }


}


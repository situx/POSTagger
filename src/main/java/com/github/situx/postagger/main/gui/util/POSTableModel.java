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
import com.google.re2j.Pattern;
import com.github.situx.postagger.dict.pos.util.POSDefinition;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;

/**
 * Table model for displaying statistics.
 */
public class POSTableModel extends AbstractTableModel {
    /**The data to be displayed.*/
    POSTagger posTagger;
    /**The column names to be used:*/
    private String[] columnNames;

    private List<POSDefinition> posdefs;

    /**Constructor for this class.*/
    public POSTableModel(String[] columnames,POSTagger posTagger) {
        this.posTagger=posTagger;
        this.columnNames=columnames;
        this.posdefs=new LinkedList<>();
        for(List<POSDefinition> poss:posTagger.getClassifiers().values()){
            this.posdefs.addAll(poss);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return posdefs.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        POSDefinition pos=this.posdefs.get(row);
        switch (col){
            case 0:
                return pos.getTag();
            case 1:
                return pos.getDesc();
            case 2:
                return pos.getRegex();
            case 3:
                return pos.getEquals();
            case 4:
                StringBuilder valStr=new StringBuilder();
                valStr.append(" ");
                for(String val:pos.getValue()){
                    valStr.append(val);
                    valStr.append(";");
                }
                return valStr.substring(0,valStr.length()-1);
            case 5:
                return pos.getTargetScript();
            case 6:
                return pos.getClassification();
            case 7:
                return pos.getExtrainfo();
            case 8:
                return String.format("#%02x%02x%02x", posTagger.getPoscolors().get(pos.getPosTag().toString().toLowerCase()).getRed(),
                        posTagger.getPoscolors().get(pos.getPosTag().toString().toLowerCase()).getGreen(), posTagger.getPoscolors().get(pos.getPosTag().toString().toLowerCase()).getBlue());
            default:
                return pos.getDesc();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int col) {
        POSDefinition pos=this.posdefs.get(row);
        switch (col){
            case 1:
                pos.setTag(aValue.toString());
                break;
            case 2:
                pos.setDesc(aValue.toString());
                break;
            case 3:
                pos.setRegex(Pattern.compile(aValue.toString()));
                break;
            case 4:
                pos.setValue(aValue.toString().split(";"));
                break;
            case 5:
                pos.setTargetScript(aValue.toString());
                break;
            case 6:
                pos.setClassification(aValue.toString());
                break;
            case 7:
                pos.setExtrainfo(aValue.toString());
                break;
            case 8:
                posTagger.getPoscolors().put(pos.getPosTag().toString(),POSTagger.parseHTMLColor(aValue.toString()));
                break;
            default:
        }
    }

}


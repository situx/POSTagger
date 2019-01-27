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

import javax.swing.*;
import javax.swing.table.TableColumn;

/**
 * Created by timo on 3/24/15.
 */
public class LineNumberTable extends JTable {
    private JTable mainTable;

    public LineNumberTable(JTable table) {
        super();
        mainTable = table;
        setAutoCreateColumnsFromModel( false );
        setModel( mainTable.getModel() );
        setAutoscrolls( false );
        addColumn( new TableColumn() );
        getColumnModel().getColumn(0).setCellRenderer(mainTable.getTableHeader().getDefaultRenderer());
        //  mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getColumnModel().getColumn(0).setPreferredWidth(40);
        setPreferredScrollableViewportSize(getPreferredSize());

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return row + 1;
    }

    @Override
    public int getRowHeight(int row) {
        return mainTable.getRowHeight();
    }
}

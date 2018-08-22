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

package com.github.situx.postagger.main.gui.dictedit;

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by timo on 02.03.17 .
 */
public class DictTableModel extends AbstractTableModel {

    private CuneiDictHandler handler;

    private List<CuneiChar> modellist;

    public DictTableModel(CuneiDictHandler handler){
        this.handler=handler;
        this.modellist=new LinkedList<>();
        for(CuneiChar cunei:handler.dictionary.values()){
            modellist.add(cunei);
        }
    }

    @Override
    public int getRowCount() {
        return this.handler.dictionary.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.modellist.get(rowIndex);
    }
}

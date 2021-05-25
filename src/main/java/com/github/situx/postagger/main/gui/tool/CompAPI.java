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

package com.github.situx.postagger.main.gui.tool;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.main.gui.util.JTFontChooser;
import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.main.gui.util.TextLineNumber;
import com.github.situx.postagger.util.FontUtils;
import com.github.situx.postagger.util.NiceFont;
import com.github.situx.postagger.util.enums.methods.CharTypes;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timo on 4/10/15.
 */
public abstract class CompAPI extends MainAPI {

    protected JToolTipArea resultarea2;
    protected JScrollPane scrollPane2;
    protected CharTypes charType2;

    public CompAPI(final String title, final POSTagger postagger, final CharTypes originalType, final CharTypes destinationType) {
        super(postagger, originalType);
        this.charType2=destinationType;
        this.resultarea2 = new JToolTipArea(destinationType, destinationType.getCorpusHandlerAPI().getUtilDictHandler(), MainFrame.bundle, false,true) {
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        resultarea2.setEditable(false);
        this.resultarea2.setPostagger(originalType);
        this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, CUNEIFONTSIZE));
        this.scrollPane2 = new JScrollPane();
        this.scrollPane2.setViewportView(this.resultarea2);
        ToolTipManager.sharedInstance().registerComponent(resultarea2);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        TextLineNumber tln2 = new TextLineNumber(resultarea2, new TreeMap<>());
        scrollPane2.setRowHeaderView(tln2);
    }

    @Override
    public int createLegend(final Map<String, Color> legenddata,Boolean dummy,JTabbedPane tabbedPane,String legendTitle,Boolean refresh) {
        int y=super.createLegend(legenddata,dummy,tabbedPane,legendTitle,refresh);
        GridBagConstraints c=new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y+=2;
        c.gridwidth=1;
        final JButton chooseFont=new JButton("Choose Font Right");
        chooseFont.addActionListener(actionEvent -> {
            java.util.List<NiceFont> fonts= FontUtils.getCompatibleFonts(charType);
            System.out.println(resultarea2.getFont());
            resultarea2.setFont(JTFontChooser.showDialog(resultarea2,
                    resultarea2.getFont(),
                    fonts.toArray(new Font[fonts.size()]),
                    new int[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72},
                    charType2.getPreviewString()));
        });
        legendpanel.add(chooseFont,c);
        return y;
    }

    public abstract String translate(Boolean cuneiform);
}

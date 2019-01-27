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
import com.github.situx.postagger.main.gui.util.GUIWorker;
import com.github.situx.postagger.main.gui.util.HighlightData;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.List;

/**
 * Created by timo on 10/27/14.
 */
public class ToCuneiConvMain extends CompAPI {


    private final JCheckBox cuneiformCheckbox;

    public ToCuneiConvMain(final String originalStr, final POSTagger postagger, final CharTypes originalType, JTabbedPane tabbedPane, JTabbedPane mainTabbedPane) {
        super("To Cuneiform",postagger,originalType,originalType);
        this.postagger=postagger;
        GridBagConstraints c = new GridBagConstraints();
        this.diffbutton = new JButton("To Target Language");
        int y=0;
        JLabel original = new JLabel(MainFrame.bundle.getString("original"));
        JLabel generated = new JLabel(MainFrame.bundle.getString("generated"));
        c.fill = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        this.add(original, c);
        c.fill = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        this.add(generated, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        this.add(scrollPane, c);
        c.gridx = 1;
        this.add(scrollPane2, c);
        resultarea2.setPostagger(originalType);
        this.resultarea.setText(originalStr);
        resultarea.setEditable(false);
        this.scrollPane.setMinimumSize(new Dimension(530, ysize));
        this.scrollPane2.setMinimumSize(new Dimension(530, ysize));
        scrollPane.setPreferredSize(new Dimension(530, ysize));
        scrollPane2.setPreferredSize(new Dimension(530, ysize));;
        scrollPane.setPreferredSize(new Dimension(530, ysize));
        scrollPane2.setPreferredSize(new Dimension(530, ysize));
        JPanel charTypePanel = new JPanel();
        final JComboBox<CharTypes> chartypechooser = new JComboBox<>(CharTypes.values());
        this.cuneiformCheckbox = new JCheckBox();
        cuneiformCheckbox.setSelected(true);
        final JLabel cuneiformLabel = new JLabel(MainFrame.bundle.getString("transliteration"));
        JLabel chartype = new JLabel(MainFrame.bundle.getString("chartype") + ":");
        charTypePanel.add(cuneiformCheckbox);
        charTypePanel.add(cuneiformLabel);
        charTypePanel.add(chartype);
        charTypePanel.add(chartypechooser);
        JPanel optionsPanel=new JPanel();
        JPanel optionsPanel2=new JPanel();
        final JButton exportResult = new JButton("Export Original");
        final JButton exportResult2 = new JButton("Export Generated");
        optionsPanel.add(exportResult);
        optionsPanel2.add(exportResult2);
        JPanel charTypePanel2 = new JPanel();
        final JComboBox<CharTypes> chartypechooser2 = new JComboBox<>(CharTypes.values());
        final JCheckBox cuneiformCheckbox2 = new JCheckBox();
        cuneiformCheckbox2.setSelected(true);
        final JLabel cuneiformLabel2 = new JLabel(MainFrame.bundle.getString("transliteration"));
        JLabel chartype2 = new JLabel(MainFrame.bundle.getString("chartype") + ":");
        charTypePanel2.add(cuneiformCheckbox2);
        charTypePanel2.add(cuneiformLabel2);
        charTypePanel2.add(chartype2);
        charTypePanel2.add(chartypechooser2);
        chartypechooser.addItemListener(itemEvent ->{ this.setCharType((CharTypes) chartypechooser.getSelectedItem(),resultarea,mainTabbedPane);
            this.charType=(CharTypes)chartypechooser.getSelectedItem();});
        chartypechooser2.addItemListener(itemEvent -> {this.setCharType((CharTypes) chartypechooser2.getSelectedItem(),resultarea2,mainTabbedPane);
        this.charType2=(CharTypes) chartypechooser2.getSelectedItem();});
        chartypechooser.setSelectedItem(originalType);
        chartypechooser2.setSelectedItem(originalType);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        this.add(charTypePanel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        this.add(charTypePanel2, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        this.add(optionsPanel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        this.add(optionsPanel2, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 2;
        this.add(diffbutton, c);
        diffbutton.addActionListener(actionEvent -> ToCuneiConvMain.this.paintResultArea());
        exportResult.addActionListener(actionEvent -> this.saveFile(ToCuneiConvMain.this.resultarea.getText()));
        exportResult2.addActionListener(actionEvent -> this.saveFile(ToCuneiConvMain.this.resultarea2.getText()));
//        this.createLegend(this.postagger.getPoscolors(),true,tabbedPane,"Legend",true);
        this.setPreferredSize(new Dimension(1100, 1000));
        this.setVisible(true);
        this.postagger=postagger;
        this.charType=originalType;
    }

    @Override
    public void paintResultArea() {
        GUIWorker<Object,Void> sw = new GUIWorker<Object,Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                ToCuneiConvMain.this.paintPOSTags(resultarea, resultarea.getText(), !ToCuneiConvMain.this.cuneiformCheckbox.isSelected());
                ToCuneiConvMain.this.translate(false);
                ToCuneiConvMain.this.paintPOSTags(resultarea2, resultarea2.getText(), ToCuneiConvMain.this.cuneiformCheckbox.isSelected());

                return null;
            }
        };
        sw.execute();
    }

    public String translate(Boolean cuneiform) {
        //this.all = 0.;
        StringBuilder translateresult = new StringBuilder();
       // this.matches = 0.;
        String[] revised = this.resultarea.getText().split("[\\r\\n]+");
        final Highlighter highlighter2 = resultarea2.getHighlighter();
        if (!cuneiform) {
            //int position = 0, endposition = 0, transposition = 0;
            for (String revi : revised) {
                String[] revisedwords = revi.split(" \\[");
                for (String word : revisedwords) {
                    String temp = "";
                    if (word.matches(this.charType.getLegalTranslitCharsRegex())) {
                            temp = charType.getCorpusHandlerAPI().transliterationToText(word.toLowerCase(), 0, charType.getCorpusHandlerAPI().getUtilDictHandler(), false, true);
                            if (temp == null || temp.isEmpty() || temp.matches("[ ]+")) {
                                translateresult.append("[");
                                translateresult.append(word);
                                translateresult.append("]");
                            } else {
                                translateresult.append(temp.substring(0, temp.length()));
                                translateresult.append(" ");
                            }

                    } else {
                        temp = Methods.assignTransliteration(word.split(" "), charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.PROB,false) + "*";
                        if (temp.isEmpty() || temp.matches("[ ]+")) {
                            translateresult.append("");
                        } else {
                            translateresult.append(temp.substring(0, temp.length()));
                            translateresult.append(" ");
                        }
                    }
                    //position += temp.length();
                }
                translateresult.append(System.lineSeparator());
                //position++;
                //transposition++;
            }
            //System.out.println("Positions: " + positions);
        } else {
            System.out.println("POSTagging Cuneiform...");
            for (String revi : revised) {
                String[] revisedwords = revi.split(" ");
                for (String wordd:revisedwords) {
                    String word = wordd.trim();
                    System.out.println("Word: " + word);
                    List<Integer> result = this.postagger.getPosTag(word, this.resultarea.getDictHandler());
                    System.out.println("GetPosTag: " + result.toString());
                }
                translateresult.append(System.lineSeparator());
            }
        }
        this.resultarea2.setText(translateresult+"  ");
            for(HighlightData pos:positions){
                try {
                    highlighter2.addHighlight(pos.getStart(), pos.getEnd(), new DefaultHighlighter.DefaultHighlightPainter(this.postagger.getPoscolors().get(pos.getTag())));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        return translateresult.toString();
    }
}
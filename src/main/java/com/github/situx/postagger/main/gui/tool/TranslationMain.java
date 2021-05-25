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

import com.github.situx.postagger.dict.importhandler.cuneiform.SumDictImportHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.main.gui.util.*;
import com.github.situx.postagger.main.gui.util.*;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Created by timo on 13.10.14.
 */
public class TranslationMain extends MainAPI {
    private SumDictImportHandler sumdict;
    private Translator translator;
    private JTransToolTipArea resultarea2;
    private JCheckBox translitCheckbox;

    public TranslationMain(final String originalStr, final POSTagger postagger, final CharTypes originalType, CharTypes destinationType, final TranslationMethod translationMethod, JTabbedPane tabbedPane, JTabbedPane mainTabbedPane){
        super(postagger,originalType);
        System.out.println("Postagger: "+postagger.toString()+" - "+originalType.toString()+ " - "+destinationType.toString());
        this.resultarea2=new JTransToolTipArea(destinationType,destinationType.getCorpusHandlerAPI().getUtilDictHandler(),MainFrame.bundle,false,true,this.translator,this.positions)
        {
            public boolean getScrollableTracksViewportWidth()
            {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        resultarea2.setEditable(false);
        this.resultarea2.setPostagger(originalType);
        this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, CUNEIFONTSIZE));
        this.resultarea.setFont(new Font(this.resultarea.getFont().getName(), 0, TRANSLITFONTSIZE));
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setViewportView(this.resultarea2);
        ToolTipManager.sharedInstance().registerComponent(resultarea2);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        TextLineNumber tln2 = new TextLineNumber(resultarea2, new TreeMap<>());
        scrollPane2.setRowHeaderView(tln2);
        int y=0;
        GridBagConstraints c = new GridBagConstraints();
        this.diffbutton=new JButton("Translate");
        JLabel original=new JLabel(MainFrame.bundle.getString("original"));
        JLabel generated=new JLabel(MainFrame.bundle.getString("generated"));
        c.fill = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth=1;
        this.add(original,c);
        c.fill = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth=1;
        this.add(generated,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=1;
        this.add(scrollPane, c);
        c.gridx=1;
        this.add(scrollPane2,c);
        this.resultarea.setText(originalStr);
        resultarea.setEditable(false);
        int xsize=520;
        scrollPane.setMinimumSize(new Dimension(xsize, ysize));
        resultarea2.setEditable(false);
        scrollPane.setPreferredSize(new Dimension(xsize, ysize));
        this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, TRANSLITFONTSIZE));
        scrollPane2.setMinimumSize(new Dimension(xsize, ysize));
        scrollPane2.setPreferredSize(new Dimension(xsize, ysize));
        JPanel charTypePanel=new JPanel();
        final JComboBox<CharTypes> chartypechooser = new JComboBox<>(CharTypes.values());
        final JComboBox<CharTypes> chartypechooser2 = new JComboBox<>(CharTypes.values());
        chartypechooser.addItemListener(itemEvent -> {
            this.setCharType((CharTypes) chartypechooser.getSelectedItem(),resultarea,mainTabbedPane);
            this.postagger=((CharTypes) chartypechooser.getSelectedItem()).getCorpusHandlerAPI().getPOSTagger(false);
            TranslationMain.this.translator = Translator.getTranslator((CharTypes) chartypechooser.getSelectedItem(),
                    (CharTypes) chartypechooser2.getSelectedItem(),TranslationMain.this.postagger);
            TranslationMain.this.resultarea2.setTranslator(TranslationMain.this.translator);
        });
        chartypechooser2.addItemListener(itemEvent -> {
            this.setCharType((CharTypes) chartypechooser2.getSelectedItem(),resultarea2,mainTabbedPane);
            TranslationMain.this.translator = Translator.getTranslator((CharTypes) chartypechooser.getSelectedItem(),
                    (CharTypes) chartypechooser2.getSelectedItem(),TranslationMain.this.postagger);
            TranslationMain.this.resultarea2.setTranslator(TranslationMain.this.translator);
        });
        chartypechooser.setSelectedItem(originalType);
        chartypechooser2.setSelectedItem(destinationType);
        this.translitCheckbox = new JCheckBox();
        translitCheckbox.setSelected(true);
        final JLabel cuneiformLabel=new JLabel(MainFrame.bundle.getString("transliteration"));
        JLabel chartype = new JLabel(MainFrame.bundle.getString("chartype")+":");
        charTypePanel.add(translitCheckbox);
        charTypePanel.add(cuneiformLabel);
        charTypePanel.add(chartype);
        charTypePanel.add(chartypechooser);
        JPanel charTypePanel2=new JPanel();

        final JCheckBox cuneiformCheckbox2 = new JCheckBox();
        cuneiformCheckbox2.setSelected(true);
        JPanel optionsPanel=new JPanel();
        JPanel optionsPanel2=new JPanel();
        final JButton exportResult = new JButton("Export Original");
        final JButton exportResult2 = new JButton("Export Generated");
        optionsPanel.add(exportResult);
        optionsPanel2.add(exportResult2);
        final JLabel cuneiformLabel2=new JLabel(MainFrame.bundle.getString("transliteration"));
        JLabel chartype2 = new JLabel(MainFrame.bundle.getString("chartype")+":");
        charTypePanel2.add(cuneiformCheckbox2);
        charTypePanel2.add(cuneiformLabel2);
        charTypePanel2.add(chartype2);
        charTypePanel2.add(chartypechooser2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth=1;
        this.add(charTypePanel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth=1;
        this.add(charTypePanel2,c);
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
        c.gridwidth=2;
        this.add(diffbutton,c);
        diffbutton.addActionListener(actionEvent -> TranslationMain.this.paintResultArea());
        exportResult.addActionListener(actionEvent -> this.saveFile(this.resultarea.getText()));
        exportResult2.addActionListener(actionEvent -> this.saveFile(this.resultarea2.getText()));
        this.postagger=postagger;
        this.translator=Translator.getTranslator((CharTypes) chartypechooser.getSelectedItem(),
                (CharTypes) chartypechooser2.getSelectedItem(),TranslationMain.this.postagger);
        this.createLegend(postagger.getPoscolors(),true,tabbedPane,"Legend",true);
        this.setPreferredSize(new Dimension(1100, 1000));
        this.setVisible(true);
    }

    @Override
    public void paintResultArea() {
        GUIWorker sw = new GUIWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                /*if(TranslationMain.this.sumdict==null){
                    TranslationMain.this.sumdict=SumDictImportHandler.getInstance();
                    TranslationMain.this.translator.sumdict=TranslationMain.this.sumdict.getResult();
                }*/
                System.out.println("CuneiCheckBox isSelected? " + TranslationMain.this.translitCheckbox.isSelected());
                TranslationMain.this.paintPOSTags(resultarea, resultarea.getText(), TranslationMain.this.translitCheckbox.isSelected());
                TranslationMain.this.resultarea2.setText("");
                TranslationMain.this.translate(TranslationMain.this.translitCheckbox.isSelected());
                TranslationMain.this.resultarea2.setHighlights(TranslationMain.this.positions);
                return null;
            }
        };
        sw.execute();
    }


    public String translate(Boolean translit) {
        this.all = 0.;
        StringBuilder translateresult = new StringBuilder();
        this.matches = 0.;
        String[] revised= this.resultarea.getText().split("[\\r\\n]+");
        final Highlighter highlighter2 = resultarea2.getHighlighter();
        this.positions=new LinkedList<>();
        //if (translit) {
            int position = 0;
            for (String revi : revised) {
                System.out.println("Ravi: "+revi);
                    translator.wordByWordPOStranslate(revi, translit, position);
                try {
                    this.resultarea2.getDocument().insertString(this.resultarea2.getDocument().getLength(),translator.getResult()+System.lineSeparator(),null);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                translateresult.append(translator.getResult());
                positions.addAll(translator.getLength());
                position=positions.get(positions.size()-1).getEnd();
                translateresult.append(System.lineSeparator());
                position++;
            }
            System.out.println("Positions: "+positions);
        /*} else {
            System.out.println("POSTagging Cuneiform...");
            int position = 0, endposition = 0;
            for (String revi : revised) {
                String[] revisedwords = revi.split(" ");
                for (int w = 0; w < revisedwords.length; ) {
                    String word = revisedwords[w].trim();
                    System.out.println("Word: " + word);
                    position += word.length();
                    List <Integer> result = this.postagger.getPosTag(word, this.resultarea.getDictHandler());
                    System.out.println("GetPosTag: " + result.toString());
                    position = endposition + 1;
                    w++;
                }
                translateresult.append(System.lineSeparator());
                position++;
            }
        }*/
        //this.resultarea2.setText(translateresult);
        for(HighlightData pos:positions){
            try {
                highlighter2.addHighlight(pos.getStart(), pos.getEnd(), new RectanglePainter((pos.word.size() > 0 && !pos.getManydecs() ? Color.black  : null), this.postagger.getPoscolors().get(pos.getTag())));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return translateresult.toString();
    }

}

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
import com.github.situx.postagger.dict.semdicthandler.ConceptMatcher;
import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;
import com.github.situx.postagger.main.gui.util.GUIWorker;
import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.util.enums.methods.CharTypes;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.im.spi.InputMethodDescriptor;
import java.io.File;
import java.util.Locale;

/**
 * Created by timo on 24.05.16.
 */
public class SemanticMain extends MainAPI {

    private ConceptMatcher conceptMatcher;

    private JCheckBox translitCheckbox;

    private Integer y;

    public SemanticMain(final String originalStr, final POSTagger postagger, final CharTypes originalType, JTabbedPane tabbedPane, JTabbedPane mainTabbedPane) {
        super(null, CharTypes.AKKADIAN);
        this.translitCheckbox=new JCheckBox();
        y=0;
        GridBagConstraints c = new GridBagConstraints();
        this.diffbutton = new JButton("Semantic Extraction");
        diffbutton.addActionListener(actionEvent -> this.paintResultArea());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        this.add(this.scrollPane, c);
        this.conceptMatcher=ConceptMatcher.getInstance();
        this.postagger = CharTypes.AKKADIAN.getCorpusHandlerAPI().getPOSTagger(true);
        this.resultarea.setPostagger(charType);
        resultarea.setEditable(true);
        this.resultarea.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(final InputMethodEvent inputMethodEvent) {

            }

            @Override
            public void caretPositionChanged(final InputMethodEvent inputMethodEvent) {

            }
        });
        resultarea.setPreferredSize(new Dimension(xsize, ysize));
        resultarea.setMinimumSize(new Dimension(xsize,ysize));
        scrollPane.setViewportView(resultarea);
        scrollPane.getViewport().setPreferredSize(new Dimension(xsize,ysize));
        scrollPane.setMinimumSize(new Dimension(xsize,ysize));
        InputMethodDescriptor pigLatinDesc = new GenericInputMethodDescriptor();
        try {
            Locale[] inputLocales = pigLatinDesc.getAvailableLocales();
            for (Locale input:inputLocales) {
                System.out.println(input.toString());
            }
        } catch (java.awt.AWTException e) {
            e.printStackTrace();
        }
        /*try {
            if (resultarea.getInputContext().selectInputMethod(GenericInputMethodDescriptor.AKKADIAN)) {
                System.out.println("Pig latin set for JText");
            } else {
                System.out.println("Pig latin not set for JText");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        final JFileChooser trainfilechooser=new JFileChooser();
        trainfilechooser.setCurrentDirectory(new File("test/"));
        JLabel trainingfilelabel=new JLabel("Segmented File To Extract:");
        final JTextField trainingfilefield=new JTextField(25);
        trainingfilefield.setEnabled(false);
        final JComboBox<CharTypes> chartypechooser = new JComboBox<>(CharTypes.values());
        final JCheckBox checkbox=new JCheckBox();
        final JPanel trainfilepanel = new JPanel();
        final JButton trainfilebutton=new JButton(MainFrame.bundle.getString("choose"));
        trainfilebutton.addActionListener(actionEvent -> this.loadFile(charType, trainfilechooser, trainingfilefield, checkbox.isSelected(), translitCheckbox.isSelected()));
        final JButton clear=new JButton("Clear");
        clear.addActionListener(actionEvent -> {
            resultarea.setText("");
            SemanticMain.this.epochPredict.setText("");
        });
        trainfilepanel.add(trainingfilelabel);
        trainfilepanel.add(trainingfilefield);
        trainfilepanel.add(trainfilebutton);
        trainfilepanel.add(clear);
        trainfilepanel.add(chartypechooser);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=2;
        this.add(trainfilepanel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = ++y;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridwidth=2;
        this.add(diffbutton,c);

    }

    @Override
    protected void paintPOSTags(JToolTipArea resultarea, String translittext, Boolean translit) {
        int position = 0, endposition = 0;
        for(String word:translittext.split(" ")){
            String result=this.conceptMatcher.resolveConcept(word.trim(),"en");
            position += word.length();
            Color color=Color.orange;
            endposition = resultarea.getText().indexOf("]", position - word.length())+1;
            //String paintword = resultarea.getText().substring(position - word.length(), endposition);
            if(result!=null){
                System.out.println("Matched Concept for "+word.trim()+": "+result);
                try {
                    System.out.println("Add Highlight: "+(position-word.length())+" - "+position);
                    highlighter.addHighlight(position - word.length(), position/*+result.get(2)*/, color);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("Did not match concept for "+word.trim());
                try {
                    System.out.println("Add Highlight: "+(position-word.length())+" - "+position);
                    highlighter.addHighlight(position - word.length(), position/*+result.get(2)*/, Color.cyan);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
            position++;
        }

    }

    @Override
    public void paintResultArea() {
        if(!this.highlighted){
            GUIWorker sw = new GUIWorker() {
                @Override
                protected Object doInBackground() throws Exception {

                    SemanticMain.this.paintPOSTags(SemanticMain.this.resultarea, SemanticMain.this.resultarea.getText(), SemanticMain.this.translitCheckbox.isSelected());
                    SemanticMain.this.highlighted = true;
                    return null;
                }
            };
            sw.execute();
        }
    }
}

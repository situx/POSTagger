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
import com.github.situx.postagger.dict.utils.Translation;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by timo on 4/20/15.
 */
public class DictMain extends JPanel {

    public ResourceBundle bundle = ResourceBundle.getBundle("POSTagger", Locale.getDefault(), new UTF8Bundle("UTF-8"));

    public DictMain(final POSTagger postagger, final CharTypes originalType, final DictHandling dictHandling) {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints();
        JLabel label=new JLabel("Search: ");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth=1;
        this.add(label,c);
        final JToolTipArea jCharacterArea=new JToolTipArea(originalType,dictHandling,bundle,true,false);
        final JToolTipArea jTextArea=new JToolTipArea(originalType,dictHandling,bundle,false,false);
        final JToolTipArea jtranslitArea=new JToolTipArea(originalType,dictHandling,bundle,false,false);
        final JToolTipArea jtranslatArea=new JToolTipArea(originalType,dictHandling,bundle,false,false);
        JScrollPane textPane=new JScrollPane(jTextArea);
        final JScrollPane charPane=new JScrollPane(jCharacterArea);
        JScrollPane translitPane=new JScrollPane(jtranslitArea);
        JScrollPane translatPane=new JScrollPane(jtranslatArea);
        final JTextField inputfield=new JTextField();
        inputfield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
                super.keyTyped(e);
                if(e.getKeyCode()!=KeyEvent.VK_BACK_SPACE){
                    String text=inputfield.getText();
                    if((e.getKeyChar()+"").matches(originalType.getLegalTranslitCharsRegex())){
                       text+=e.getKeyChar();
                    }
                    System.out.println("Text: "+text);
                    LangChar charr;
                    if(text.matches(originalType.getLegalTranslitCharsRegex())){
                        charr=dictHandling.translitToChar(text);
                        if(charr==null){
                            charr=dictHandling.matchWordByTransliteration(text);
                        }
                    }else{
                        charr=dictHandling.getDictMap().get(text);
                        if(charr==null){
                            charr=dictHandling.getDictionary().get(text);
                        }
                    }
                    if(charr!=null){
                        jCharacterArea.setText(" "+charr.toString());
                        StringBuilder builder=new StringBuilder();
                        int counter=0;
                        for(Transliteration trans:charr.getTransliterationSet()){
                            builder.append(trans);
                            builder.append(" ");
                            if(counter>PaintMain.WRAP_COUNT){
                                builder.append(System.lineSeparator());
                                counter=0;
                            }else{
                                counter++;
                            }
                        }
                        jtranslitArea.setText(builder.toString());
                        builder=new StringBuilder();
                        counter=0;
                        if(charr.getTranslationSet(Locale.ENGLISH)!=null){
                            for(Translation trans:charr.getTranslationSet(Locale.ENGLISH).keySet()){
                                builder.append(trans);
                                builder.append(" ");
                                if(counter>PaintMain.WRAP_COUNT){
                                    builder.append(System.lineSeparator());
                                    counter=0;
                                }else{
                                    counter++;
                                }
                            }
                        }
                        jtranslatArea.setText(builder.toString());
                        jTextArea.setText(charr.getCharInformation(System.lineSeparator(),false,true));
                    }else{
                        jCharacterArea.setText("");
                        jTextArea.setText("");
                        jtranslatArea.setText("");
                        jtranslitArea.setText("");
                    }
                }else{
                    jCharacterArea.setText("");
                    jTextArea.setText("");
                    jtranslatArea.setText("");
                    jtranslitArea.setText("");
                }

            }
        });
        //ToolTipManager.sharedInstance().registerComponent(jCharacterArea);
        //ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth=1;
        this.add(inputfield,c);
        textPane.setMinimumSize(new Dimension(100, 170));
        textPane.setPreferredSize(new Dimension(100, 170));
        jTextArea.setEditable(false);
        try {
            jTextArea.setFont(jTextArea.getFont().deriveFont(20f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        charPane.setMinimumSize(new Dimension(100, 100));
        charPane.setPreferredSize(new Dimension(100, 100));
        jCharacterArea.setEditable(false);
        if(new File("fonts/"+originalType.getLocale()+".ttf").exists()){
            try {
                jCharacterArea.setFont(MainAPI.getFont("fonts/" + originalType.getLocale() + ".ttf", MainAPI.CUNEIFONTSIZE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            jCharacterArea.setFont(jCharacterArea.getFont().deriveFont(40f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        translitPane.setMinimumSize(new Dimension(100, 180));
        translitPane.setPreferredSize(new Dimension(100, 180));
        jtranslitArea.setEditable(false);
        try {
            jtranslitArea.setFont(jTextArea.getFont().deriveFont(20f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        translatPane.setMinimumSize(new Dimension(100, 140));
        translatPane.setPreferredSize(new Dimension(100, 140));
        jtranslatArea.setEditable(false);
        try {
            jtranslatArea.setFont(jTextArea.getFont().deriveFont(20f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=2;
        this.add(textPane,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=2;
        this.add(new JLabel("Transliteration"),c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=2;
        this.add(translitPane,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth=2;
        this.add(new JLabel("Translation"),c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth=2;
        this.add(translatPane,c);
        JLabel resultLabel=new JLabel("Information");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=2;
        this.add(resultLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=2;
        this.add(new JLabel("Dictionary"),c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=2;
        this.add(jCharacterArea,c);
        this.setPreferredSize(this.getPreferredSize());
    }
}

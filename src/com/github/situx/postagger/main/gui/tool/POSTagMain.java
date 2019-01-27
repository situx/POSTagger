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

import com.github.situx.postagger.main.gui.edit.RuleEditor;
import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;
import com.github.situx.postagger.main.gui.util.GUIWorker;
import com.github.situx.postagger.main.gui.util.TabEnum;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.EvaluationMethod;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.util.ExportMethods;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.im.spi.InputMethodDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by timo on 27.09.14.
 */
public class POSTagMain extends MainAPI {

    private final JCheckBox translitCheckbox;
    protected JButton exportResult;
    int y=0;

    public POSTagMain(final JTabbedPane tabbedPane,final JTabbedPane mainTabbedPane){
        super(null,CharTypes.AKKADIAN);
        GridBagConstraints c = new GridBagConstraints();
        this.diffbutton=new JButton(MainFrame.bundle.getString("postagging"));
        this.exportResult=new JButton(MainFrame.bundle.getString("exportResult"));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=2;
        this.add(this.scrollPane,c);
        this.translitCheckbox =new JCheckBox();
        this.translitCheckbox.setSelected(true);
        this.postagger=CharTypes.AKKADIAN.getCorpusHandlerAPI().getPOSTagger(true);
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
        final JFileChooser evalfilechooser=new JFileChooser();
        evalfilechooser.setCurrentDirectory(new File("test/"));
        trainfilechooser.setCurrentDirectory(new File("test/"));
        JLabel trainingfilelabel=new JLabel(MainFrame.bundle.getString("postagfile"));
        final JTextField trainingfilefield=new JTextField(25);
        trainingfilefield.setEnabled(false);
        final JTextField evaluationfilefield=new JTextField(25);
        trainingfilefield.setEnabled(false);
        final JComboBox<CharTypes> chartypechooser = new JComboBox<>(CharTypes.values());
        final JCheckBox checkbox=new JCheckBox();
        final JLabel cuneiformLabel=new JLabel(MainFrame.bundle.getString("transliteration"));
        final JButton trainfilebutton=new JButton(MainFrame.bundle.getString("choose"));
        trainfilebutton.addActionListener(actionEvent -> this.loadFile(charType, trainfilechooser, trainingfilefield, checkbox.isSelected(), translitCheckbox.isSelected()));
        final JButton definitionReload=new JButton(MainFrame.bundle.getString("reloadDefs"));
        final JButton clear=new JButton("Clear");
        clear.addActionListener(actionEvent -> {
            resultarea.setText("");
            POSTagMain.this.epochPredict.setText("");
        });
        final JButton translation=new JButton("Translation");
        translation.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Translation",TabEnum.MAINAREA)) {
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("Translation", new TranslationMain(resultarea.getText(), this.postagger, ((CharTypes) chartypechooser.getSelectedItem()), CharTypes.ENGLISH, TranslationMethod.LEMMA, tabbedPane, mainTabbedPane), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
        });
        final JButton regex=new JButton("Evaluate POSTag");
        regex.addActionListener(e -> {
            int returnVal = evalfilechooser.showOpenDialog(POSTagMain.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String texttoPrint=postagger.evaluatePosTagForText(evalfilechooser.getSelectedFile().getAbsolutePath(), POSTagMain.this.resultarea.getText(), EvaluationMethod.TOKENACC,this.translitCheckbox.isSelected());
                JPanel panel=new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setPreferredSize(new Dimension( 700, 500 ) );
                panel.setMaximumSize(new Dimension( 700, 500 ) );
                JLabel label=new JLabel(texttoPrint.substring(0,texttoPrint.indexOf('[')-1));
                JTextArea textArea = new JTextArea(texttoPrint.substring(texttoPrint.indexOf('[')));
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
                panel.add(label);
                panel.add(scrollPane);
                JOptionPane.showMessageDialog(null, panel, "POSTag Evaluation",
                        JOptionPane.OK_OPTION);
            }
        }
        );
        final JButton regex2=new JButton("Evaluate POSTag Basic");
        regex2.addActionListener(e -> {
                    int returnVal = evalfilechooser.showOpenDialog(POSTagMain.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String texttoPrint=postagger.evaluatePosTagForText(evalfilechooser.getSelectedFile().getAbsolutePath(), POSTagMain.this.resultarea.getText(), EvaluationMethod.TOKENACCBASIC,this.translitCheckbox.isSelected());
                        JPanel panel=new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setPreferredSize(new Dimension( 700, 500 ) );
                        panel.setMaximumSize(new Dimension( 700, 500 ) );
                        JLabel label=new JLabel(texttoPrint.substring(0,texttoPrint.indexOf('[')-1));
                        JTextArea textArea = new JTextArea(texttoPrint.substring(texttoPrint.indexOf('[')));
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
                        panel.add(label);
                        panel.add(scrollPane);
                        JOptionPane.showMessageDialog(null, panel, "POSTag Evaluation Basic",
                                JOptionPane.OK_OPTION);
                    }
                }
        );
        final JButton regex3=new JButton("WERate");
        regex3.addActionListener(e -> {
                    int returnVal = evalfilechooser.showOpenDialog(POSTagMain.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String texttoPrint=postagger.evaluatePosTagForText(evalfilechooser.getSelectedFile().getAbsolutePath(), POSTagMain.this.resultarea.getText(), EvaluationMethod.WERRATE,this.translitCheckbox.isSelected());
                        JPanel panel=new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setPreferredSize(new Dimension( 700, 500 ) );
                        panel.setMaximumSize(new Dimension( 700, 500 ) );
                        JLabel label=new JLabel(texttoPrint.substring(0,texttoPrint.indexOf('[')-1));
                        JTextArea textArea = new JTextArea(texttoPrint.substring(texttoPrint.indexOf('[')));
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
                        panel.add(label);
                        panel.add(scrollPane);
                        JOptionPane.showMessageDialog(null, panel, "Word Error Rate",
                                JOptionPane.OK_OPTION);
                    }
                }
        );
        final JButton options=new JButton("Options");
        options.addActionListener(actionEvent -> new RuleEditor(this.postagger,"Options"));
        this.createLegend(this.postagger.getPoscolors(),false,tabbedPane,"Legend",true);
        final JPanel trainfilepanel = new JPanel();
        final JLabel atflabel=new JLabel(MainFrame.bundle.getString("loadOriginalatf"));
        trainfilepanel.add(trainingfilelabel);
        trainfilepanel.add(trainingfilefield);
        trainfilepanel.add(trainfilebutton);
        trainfilepanel.add(clear);
        final JButton toTargetLanguage = new JButton("To Cuneiform");
        toTargetLanguage.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("To Cuneiform",TabEnum.MAINAREA)) {
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("To Cuneiform", new TranslationMain(resultarea.getText(), this.postagger, ((CharTypes) chartypechooser.getSelectedItem()),((CharTypes) chartypechooser.getSelectedItem()),TranslationMethod.LEMMA, tabbedPane, mainTabbedPane), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
            });
        JPanel configpanel=new JPanel();
        configpanel.add(checkbox);
        configpanel.add(atflabel);
        configpanel.add(definitionReload);
        configpanel.add(exportResult);
        configpanel.add(regex);
        configpanel.add(regex2);
        configpanel.add(regex3);
        configpanel.add(translation);
        configpanel.add(options);
        configpanel.add(toTargetLanguage);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=2;
        this.add(trainfilepanel,c);
        //final JPanel charTypePanel = new JPanel();
        JLabel chartype = new JLabel(MainFrame.bundle.getString("chartype")+":");
        trainfilepanel.add(translitCheckbox);
        trainfilepanel.add(cuneiformLabel);
        trainfilepanel.add(chartype);
        trainfilepanel.add(chartypechooser);
        chartypechooser.addItemListener(itemEvent -> {
                this.setCharType(((CharTypes) chartypechooser.getSelectedItem()), resultarea, mainTabbedPane);
                this.postagger=((CharTypes) chartypechooser.getSelectedItem()).getCorpusHandlerAPI().getPOSTagger(false);
            try {
                this.postagger.toHighlightJSON(((CharTypes) chartypechooser.getSelectedItem()).getLocale()+"_matches.js");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        diffbutton.addActionListener(actionEvent -> POSTagMain.this.paintResultArea());
        translitCheckbox.addActionListener(actionEvent -> resultarea.setCuneiFormFlag(!translitCheckbox.isSelected()));
        definitionReload.addActionListener(actionEvent -> this.definitionReload((CharTypes)chartypechooser.getSelectedItem(),mainTabbedPane));
        exportResult.addActionListener(actionEvent -> this.saveFile(POSTagMain.this.postagger.textToPosTagXML(POSTagMain.this.resultarea.getText())));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=2;
        this.add(configpanel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = ++y;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridwidth=2;
        this.add(diffbutton,c);
        if(MainFrame.checkTab("Draw",TabEnum.SIDEBAR))
            MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab("Draw",new PaintMain(postagger, charType, charType.getCorpusHandlerAPI().getUtilDictHandler()),TabEnum.SIDEBAR);
        if(MainFrame.checkTab("Dict",TabEnum.SIDEBAR))
            MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab("Dict",new DictMain(postagger, charType, charType.getCorpusHandlerAPI().getUtilDictHandler()),TabEnum.SIDEBAR);
        this.setVisible(true);
        try {
            this.charType.getCorpusHandlerAPI().getUtilDictHandler().toIME(ExportMethods.CUNEILISTJSON, this.charType.getCorpusHandlerAPI().getUtilDictHandler().getDictMap(), this.charType.getCorpusHandlerAPI().getUtilDictHandler().getDictionary(),"","testttit.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        new MainFrame("POSTagger");
    }


    public void paintResultArea() {
        if(!this.highlighted){
            GUIWorker sw = new GUIWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    POSTagMain.this.paintPOSTags(POSTagMain.this.resultarea, POSTagMain.this.resultarea.getText(), POSTagMain.this.translitCheckbox.isSelected());
                    POSTagMain.this.highlighted = true;
                    return null;
                }
            };
            sw.execute();
        }
    }
}

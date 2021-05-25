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
import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.main.gui.util.TabEnum;
import com.github.situx.postagger.methods.segmentation.dict.DictMethods;
import com.github.situx.postagger.methods.segmentation.rule.RuleMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timo on 10/27/14.
 */
public class SegmentationMain extends CompAPI {


    private final JComboBox<CharTypes> chartypechooser;
    private final JCheckBox translitCheckbox;
    private final JComboBox<ClassificationMethod> methodtypechooser;
    private String compareText;
    private boolean highlightedright;
    private DictMethods dictmethods;
    private RuleMethods rulemethods;

    public SegmentationMain(final String originalStr, final POSTagger postagger, final CharTypes originalType, JTabbedPane tabbedPane, JTabbedPane mainTabbedPane) {
        super("Segmentation",postagger,originalType,originalType);
        this.postagger=postagger;
        GridBagConstraints c = new GridBagConstraints();
        this.dictmethods=new DictMethods();
        this.rulemethods=new RuleMethods();
        this.diffbutton = new JButton("Segment");
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
        this.resultarea.setText(originalStr);
        resultarea.setEditable(false);
        int xsize=520;
        this.scrollPane.setMinimumSize(new Dimension(xsize, ysize));
        this.scrollPane2.setMinimumSize(new Dimension(xsize, ysize));
        scrollPane.setPreferredSize(new Dimension(xsize, ysize));
        scrollPane2.setPreferredSize(new Dimension(xsize, ysize));;
        scrollPane.setPreferredSize(new Dimension(xsize, ysize));
        scrollPane2.setPreferredSize(new Dimension(xsize, ysize));
        JPanel charTypePanel = new JPanel();
        this.chartypechooser = new JComboBox<>(CharTypes.values());
        this.methodtypechooser = new JComboBox<>(new ClassificationMethod[]{ClassificationMethod.AVGWORDLEN,
                ClassificationMethod.LCUMATCHING,
                ClassificationMethod.MAXMATCH,ClassificationMethod.MAXMATCHCOMBINED,ClassificationMethod.MINWCMATCH,
                ClassificationMethod.POSMATCH,ClassificationMethod.PREFSUFF,ClassificationMethod.RANDOMSEGMENTPARSE});
        this.translitCheckbox = new JCheckBox();
        translitCheckbox.setSelected(true);
        final JLabel cuneiformLabel = new JLabel(MainFrame.bundle.getString("transliteration"));
        JLabel chartype = new JLabel(MainFrame.bundle.getString("chartype") + ":");
        charTypePanel.add(translitCheckbox);
        charTypePanel.add(cuneiformLabel);
        charTypePanel.add(chartype);
        charTypePanel.add(chartypechooser);
        JPanel optionsPanel=new JPanel();
        JPanel optionsPanel2=new JPanel();
        final JButton exportResult = new JButton("Export Original");
        final JButton exportResult2 = new JButton("Export Generated");
        optionsPanel.add(exportResult);
        optionsPanel2.add(exportResult2);
        optionsPanel.add(new JLabel("SegmentationMethod: "));
        optionsPanel.add(methodtypechooser);
        JPanel charTypePanel2 = new JPanel();
        final JCheckBox cuneiformCheckbox2 = new JCheckBox();
        cuneiformCheckbox2.setSelected(true);
        final JFileChooser trainfilechooser=new JFileChooser();
        trainfilechooser.setCurrentDirectory(new File("test/"));
        final JTextField trainingfilefield=new JTextField(25);
        trainingfilefield.setEnabled(false);
        final JButton trainfilebutton=new JButton(MainFrame.bundle.getString("choose"));
        trainfilebutton.addActionListener(actionEvent -> {
            //Handle open trainfilebutton action.
            if (actionEvent.getSource() == trainfilebutton) {
                int returnVal = trainfilechooser.showOpenDialog(SegmentationMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = trainfilechooser.getSelectedFile();
                    trainingfilefield.setText(file.getAbsolutePath());
                    byte[] encoded = new byte[0];
                    try {
                        encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                        SegmentationMain.this.compareText= new String(encoded, "UTF-8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Opening: " + file.getName() + ".");
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });
        final JLabel cuneiformLabel2 = new JLabel(MainFrame.bundle.getString("transliteration"));
        charTypePanel2.add(cuneiformCheckbox2);
        charTypePanel2.add(cuneiformLabel2);
        charTypePanel2.add(trainingfilefield);
        charTypePanel2.add(trainfilebutton);
        chartypechooser.addItemListener(itemEvent -> {
            this.setCharType(((CharTypes) chartypechooser.getSelectedItem()),resultarea,mainTabbedPane);
            this.postagger=((CharTypes) chartypechooser.getSelectedItem()).getCorpusHandlerAPI().getPOSTagger(false);
        });
        chartypechooser.setSelectedItem(originalType);
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
        diffbutton.addActionListener(actionEvent -> this.paintResultArea());
        exportResult.addActionListener(actionEvent -> this.saveFile(this.resultarea.getText()));
        exportResult2.addActionListener(actionEvent -> this.saveFile(this.resultarea2.getText()));
        if(MainFrame.checkTab("Draw", TabEnum.SIDEBAR))
            MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab("Draw",new PaintMain(postagger, charType, charType.getCorpusHandlerAPI().getUtilDictHandler()),TabEnum.SIDEBAR);
        Map<String,Color> segmentationcolors=new TreeMap<>();
        segmentationcolors.put("correctsegment",Color.green);
        segmentationcolors.put("wrongsegment",Color.red);
        segmentationcolors.put("missingsegment",Color.cyan);
        segmentationcolors.put("correcttranslit",Color.green);
        segmentationcolors.put("wrongtranslit",Color.red);
        segmentationcolors.put("mediumtranslit",Color.yellow);
//        this.createLegend(segmentationcolors,true,tabbedPane,"SegLegend",true);
        this.setPreferredSize(new Dimension(1100, 1000));
        this.setVisible(true);
    }

    @Override
    public void paintResultArea() {
        GUIWorker sw = new GUIWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                //SegmentationMain.this.paintPOSTags(resultarea, resultarea.getText(), SegmentationMain.this.translitCheckbox.isSelected());
                SegmentationMain.this.resultarea2.getHighlighter().removeAllHighlights();
                SegmentationMain.this.resultarea2.setText("");
                SegmentationMain.this.translate(SegmentationMain.this.translitCheckbox.isSelected());
                SegmentationMain.this.highlightedright=false;
                SegmentationMain.this.paintPOSTags(resultarea2, resultarea2.getText(), true);
                return null;
            }
        };
        sw.execute();
    }

    public String translate(Boolean cuneiform) {
        ClassificationMethod method=(ClassificationMethod)methodtypechooser.getSelectedItem();
        CharTypes charType=(CharTypes)chartypechooser.getSelectedItem();
        try {
        switch (method){
            case AVGWORDLEN:
                this.resultarea2.setText(this.rulemethods.matchByAvgWordLength(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, false, true, false));
                break;
            case CHARSEGMENTPARSE:
                this.resultarea2.setText(this.rulemethods.charSegmentParse(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, false, true, false));
                break;
            case LCUMATCHING:
                this.resultarea2.setText(this.dictmethods.lcuMatching(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, true, false));
                break;
            case MAXMATCH:
                    this.resultarea2.setText(this.dictmethods.maxMatch(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), false, TransliterationMethod.FIRST, charType, true, false));
                    break;
            case MAXMATCHCOMBINED:
                this.resultarea2.setText(this.dictmethods.maxMatchCombined(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, true, false));
                break;
            case MINWCMATCH:
                this.resultarea2.setText(this.dictmethods.minWCMatching(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, true, false));
                break;
            case POSMATCH:
                this.resultarea2.setText(this.dictmethods.minWCPOSMatching(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, true, false));
                break;
            case PREFSUFF:
                this.resultarea2.setText(this.rulemethods.prefixSuffixMatching(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, false, true, false));
                break;
            case RANDOMSEGMENTPARSE:
                this.resultarea2.setText(this.rulemethods.randomSegmentParse(resultarea.getText(), "", charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.FIRST, charType, false, true, false));
                break;
            default:
                this.resultarea2.setText("");
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.resultarea2.setText(this.resultarea2.getText().replace(" ","   "));
        System.out.println("Brought to you by: "+charType.toString());
        return "";
    }

    @Override
    protected void paintPOSTags(final JToolTipArea resultarea, final String translittext, final Boolean translit) {
        if(!highlightedright && compareText!=null) {
            GUIWorker sw = new GUIWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    boolean wordfinished = true;
                    int originalk, revisedk;
                    String[] originalsplitword, revisedsplitword;
                    java.util.List<String> original;
                    java.util.List<String> revised;
                    original = Arrays.asList(compareText.split(System.lineSeparator()));
                    revised = Arrays.asList(SegmentationMain.this.resultarea2.getText().split(System.lineSeparator()));
                    if (!translitCheckbox.isSelected()){//(!switchflag1 && !switchflag2) || (switchflag1 && !switchflag2)) {
                        Highlighter highlighter = SegmentationMain.this.resultarea2.getHighlighter();
                        System.out.println(original);
                        System.out.println(revised);
                        int position = 0;
                        int j = 0;
                        String[] originalwords, revisedwords = null;
                        for (int i = 0; i < revised.size(); i++) {
                            if(revised.get(i).trim().isEmpty()){
                                position+=revised.get(i).length()+1;
                                continue;
                            }
                            if (revisedwords != null && j > revisedwords.length) {
                                try {
                                    highlighter.addHighlight(position, position + (revisedwords.length - j), new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }
                            originalwords = original.get(i).split(" ");
                            revisedwords = revised.get(i).split(" ");
                            j = 0;
                            revisedk = 0;
                            originalk = 0;
                            for (int w = 0; w < revisedwords.length; ) {
                                //System.out.println("w: "+w+" w-length:"+revisedwords.length+" j: "+j+" j-length: "+originalwords.length);
                                String word = revisedwords[w];
                                if (word.isEmpty()) {
                                    w++;
                                    j++;
                                    continue;
                                }
                                position += word.length();
                                System.out.println("Word.length() " + word.length());
                                if ((originalwords.length - 1) < j) {
                                    System.out.println("Missed Word: " + word + " - " + originalwords[j]);
                                    try {
                                        highlighter.addHighlight(position - word.length(), position, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                    j++;
                                    w++;
                                    revisedk = 0;
                                    originalk = 0;
                                    position++;
                                } else if (word.equals(originalwords[j])) {
                                    System.out.println("Correct word: " + word + " - " + originalwords[j]);
                                    try {
                                        System.out.println(SegmentationMain.this.resultarea2.getText().substring(position - word.length(), position));
                                        highlighter.addHighlight(position - word.length(), position, new DefaultHighlighter.DefaultHighlightPainter(Color.green));
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                    j++;
                                    w++;
                                    revisedk = 0;
                                    originalk = 0;
                                    position++;
                                } else {
                                    revisedsplitword = word.split("-");
                                    originalsplitword = originalwords[j].split("-");
                                    int interpos = position - word.length();
                                    if (wordfinished)
                                        originalk = 0;
                                    wordfinished = false;
                                    for (String syll : revisedsplitword) {
                                        System.out.println("originalk: " + originalk + " revisedk: " + revisedk);
                                        //System.out.println("Gimme da K yoa one moa time!: "+originalk+" strlen: "+originalsplitword.length);
                                        interpos += syll.length() + 1;
                                        if (originalk < originalsplitword.length) {
                                            System.out.println("OriginalSyll: " + syll + " syll.length " + syll.length());
                                            System.out.println("Syll: " + syll.replaceAll("\\[", "").replaceAll("]", "") + " Originalsplitword: " + originalsplitword[originalk].replaceAll("\\[", "").replaceAll("]", ""));
                                        }
                                        if (originalk > (originalsplitword.length - 1)) {
                                            System.out.println("Paint Red: " + syll + " Interpos: " + SegmentationMain.this.resultarea2.getText().substring(interpos - syll.length(), interpos));
                                            if (revisedk == revisedsplitword.length - 1) {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos - 1, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                        } else if (syll.replace("[", "").replace("]", "").equals(originalsplitword[originalk].replaceAll("\\[", "").replaceAll("]", ""))) {
                                            System.out.println("Paint Yellow: " + syll + " Interpos: " + resultarea2.getText().substring(interpos - syll.length(), interpos + 1));
                                            if (revisedk == revisedsplitword.length - 1) {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos - 1, new DefaultHighlighter.DefaultHighlightPainter(Color.yellow));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos, new DefaultHighlighter.DefaultHighlightPainter(Color.yellow));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        } else {
                                            System.out.println("Paint Red: " + syll + " Interpos: " + resultarea2.getText().substring(interpos - syll.length(), interpos + 1));
                                            if (revisedk == revisedsplitword.length - 1) {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos - 1, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    highlighter.addHighlight(interpos - syll.length() - 1, interpos, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        originalk++;
                                        revisedk++;
                                        System.out.println("Gimme da K yoa!: " + originalk + " strlen: " + originalsplitword.length);
                                        if (originalk == originalsplitword.length && revisedk < revisedsplitword.length) {
                                            originalsplitword = originalwords[++j].split("-");
                                            originalk = 0;
                                        } else if (revisedk == revisedsplitword.length && originalk < originalsplitword.length && w < revisedwords.length - 1) {
                                            revisedsplitword = revisedwords[++w].split("-");
                                            revisedk = 0;
                                        } else if ((revisedk == revisedsplitword.length && originalk == originalsplitword.length) || (w >= (revisedwords.length - 1) && revisedk == revisedsplitword.length)) {
                                            System.out.println("j++ k++");
                                            j++;
                                            w++;
                                            originalk = 0;
                                            revisedk = 0;
                                            break;
                                        }
                                    }
                                    position++;
                                }
                            }
                            position++;
                        }

                    } else if (translitCheckbox.isSelected()){//(!switchflag1 && switchflag2) || (switchflag1 && switchflag2)) {
                        original = Arrays.asList(compareText.split(System.lineSeparator()));
                        revised = Arrays.asList(SegmentationMain.this.resultarea2.getText().split(System.lineSeparator()));
                        Highlighter highlighter = SegmentationMain.this.resultarea2.getHighlighter();
                        System.out.println(original);
                        System.out.println(revised);
                        int position = 0;
                        for (int i = 0; i < revised.size(); i++) {
                             if(revised.get(i).isEmpty()){
                                position++;
                                continue;
                            }
                            String originalcharline=original.get(i).trim();
                            String revisedcharline=revised.get(i).trim();
                            System.out.println("Originalcharline: "+originalcharline+" RevisedCharline: "+revisedcharline);
                            for(String stopChar:SegmentationMain.this.charType.getCorpusHandlerAPI().getUtilDictHandler().getStopchars().keySet()){
                                originalcharline=originalcharline.replaceAll(stopChar,"");
                            }
                            String currentoriginal,currentrevised;
                            int revisedoffset=0,originaloffset=0;
                            for(int j=0;j<originalcharline.length() && j+revisedoffset+1<revisedcharline.length() && j+originaloffset+1<originalcharline.length()/*-chartype.getChar_length()*/;j+=SegmentationMain.this.charType.getChar_length()){
                                currentrevised=revisedcharline.substring(j+revisedoffset,j+revisedoffset+SegmentationMain.this.charType.getChar_length());
                                currentoriginal=originalcharline.substring(j+originaloffset,j+originaloffset+SegmentationMain.this.charType.getChar_length());
                                System.out.println("Currentrevised: ["+currentrevised+"] RevisedOffset: "+revisedoffset+" J:"+j);
                                System.out.println("Currentoriginal: ["+currentoriginal+"] Originaloffset: "+originaloffset+" J:"+j);
                                if(currentoriginal.substring(0,1).matches("[ ]+")){
                                    if(currentrevised.substring(0,1).matches("[ ]+")){
                                        while(j+originaloffset+1<originalcharline.length() && originalcharline.substring(j+originaloffset,j+originaloffset+1).equals(" ")){
                                            originaloffset++;
                                        }
                                        while(j+revisedoffset+1<revised.get(i).length() && revised.get(i).substring(j + revisedoffset, j + revisedoffset + 1).equals(" ")){
                                            revisedoffset++;
                                            position++;
                                        }
                                        try {
                                            highlighter.addHighlight(position-3, position, new DefaultHighlighter.DefaultHighlightPainter(Color.green));
                                        } catch (BadLocationException e) {
                                            e.printStackTrace();
                                        }
                                        position+=SegmentationMain.this.charType.getChar_length();
                                        //result+=("   "+currentoriginal);
                                    }else{
                                        while(j+originaloffset+1<originalcharline.length() && originalcharline.substring(j+originaloffset,j+originaloffset+1).equals(" ")){
                                            originaloffset++;
                                        }

                                        SegmentationMain.this.resultarea2.getDocument().insertString(position," ",null);
                                        try {
                                            highlighter.addHighlight(position, position + 1, new DefaultHighlighter.DefaultHighlightPainter(Color.cyan));
                                        } catch (BadLocationException e) {
                                            e.printStackTrace();
                                        }
                                        position++;
                                        j -=SegmentationMain.this.charType.getChar_length();
                                        //position+=dictHandler.getChartype().getChar_length();
                                    }

                                }else{
                                    if(currentrevised.substring(0,1).matches("[ ]+")){
                                        while(j+revisedoffset+1<revisedcharline.length() && revisedcharline.substring(j+revisedoffset,j+revisedoffset+1).equals(" ")){
                                            revisedoffset++;
                                            position++;
                                        }
                                        //originaloffset-=dictHandler.getChartype().getChar_length();
                                        try {
                                            highlighter.addHighlight(position-3, position, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
                                        } catch (BadLocationException e) {
                                            e.printStackTrace();
                                        }
                                        j-=SegmentationMain.this.charType.getChar_length();
                                        //position+=dictHandler.getChartype().getChar_length();
                                    }else{
                                        position+=SegmentationMain.this.charType.getChar_length();
                                    }


                                }
                            }
                            position+=System.lineSeparator().length();
                        }
                    }
                    return null;
                }
            };
            sw.execute();
            this.highlightedright=true;
        }

    }

}
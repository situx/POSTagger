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
import com.github.situx.postagger.dict.pos.cuneiform.CuneiPOSTagger;
import com.github.situx.postagger.main.gui.util.*;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.main.gui.util.*;
import com.github.situx.postagger.util.FontUtils;
import com.github.situx.postagger.util.NiceFont;
import com.github.situx.postagger.util.enums.util.Tags;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by timo on 3/24/15.
 */
public abstract class MainAPI extends JPanel {
    protected static Integer CUNEIFONTSIZE=16;
    protected static Integer CUNEISELECTORFONTSIZE=40;
    protected static Integer TRANSLITFONTSIZE=20;
    protected POSTagger postagger;
    protected JScrollPane scrollPane;
    protected JButton diffbutton;
    protected Double matches, all;
    protected JPanel legendpanel;
    protected CharTypes charType;
    protected int xsize,ysize;
    protected boolean highlighted;
    protected JToolTipArea resultarea;
    protected RepaintHighlighter highlighter;
    protected Map<Integer,HighlightData> wordCountToHighlightData;
    protected Map<String, List<Highlighter.Highlight>> caseToHighlights;
    protected List<HighlightData> positions;
    protected JLabel epochPredict;

    public MainAPI(POSTagger postagger,CharTypes originalType){
        ysize=((Double)(0.7*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getHeight())).intValue();
        xsize=((Double)(0.8*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getWidth())).intValue();
        this.postagger=postagger;
        this.wordCountToHighlightData=new TreeMap<>();
        this.epochPredict=new JLabel();
        this.charType=originalType;
        this.highlighter=new RepaintHighlighter();
        this.caseToHighlights = new TreeMap<>();
        this.positions=new LinkedList<>();
        this.setLayout(new GridBagLayout());
        this.resultarea = new JToolTipArea(originalType, originalType.getCorpusHandlerAPI().getUtilDictHandler(), MainFrame.bundle, false,true) {
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        if(new File("fonts/"+originalType.getLocale()+".ttf").exists()){
            try {
                Font font=MainAPI.getFont("fonts/" + originalType.getLocale() + ".ttf", MainAPI.CUNEIFONTSIZE);
                resultarea.setFont(font);
                GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                genv.registerFont(font);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ToolTipManager.sharedInstance().registerComponent(resultarea);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        this.resultarea.setFont(new Font(this.resultarea.getFont().getName(), 0, TRANSLITFONTSIZE));
        this.resultarea.setPostagger(this.postagger);
        this.resultarea.setHighlighter(this.highlighter);
        this.scrollPane = new JScrollPane();
        TextLineNumber tln = new TextLineNumber(resultarea,new TreeMap<>());
        scrollPane.setRowHeaderView(tln);
        this.scrollPane.setViewportView(this.resultarea);

    }


    protected int createLegend(final java.util.Map<String,Color> legenddata,Boolean twoTextPanes,JTabbedPane tabbedPane,String legendtitle,Boolean refresh){
        return MainFrame.createLegend(legenddata,twoTextPanes,tabbedPane,legendtitle,refresh);
    }


    public void changeLegendColors(Boolean leftMouse,final java.util.Map<String,Color> legenddata,JLabel nounoradj){
        java.util.List<Highlighter.Highlight> newhighlights=new LinkedList<>();
        if(leftMouse){
            if(MainAPI.this.postagger.getPoscolors().get(nounoradj.getName()).getRGB()==legenddata.get(Tags.DEFAULT.toString()).getRGB()){
                return;
            }
            Color paintcolor;
            //System.out.println("POSTagger: "+POSTagMain.this.postagger.getPoscolors().get(nounoradj.getName()).getRGB()+" - "+nounoradj.getBackground().getRGB());
            if(MainAPI.this.postagger.getPoscolors().get(nounoradj.getName()).getRGB()==nounoradj.getBackground().getRGB()) {
                //MainAPI.this.caseToHighlights.get(nounoradj.getName()).stream().map(high -> {
                //    ((RectanglePainter) high.getPainter()).setFillColor(MainAPI.this.postagger.getPoscolors().get(Tags.DEFAULT.toString()));
                //    return high;});
                paintcolor=MainAPI.this.postagger.getPoscolors().get(Tags.DEFAULT.toString());
                            /*painter= new RectanglePainter(null,MainAPI.this.postagger.getPoscolors().get(Tags.DEFAULT.toString()));

                            nounoradj.setBackground(legenddata.get(Tags.DEFAULT.toString()));*/
            }else {
                paintcolor=MainAPI.this.postagger.getPoscolors().get(nounoradj.getName());
                //painter = new RectanglePainter(null,MainAPI.this.postagger.getPoscolors().get(nounoradj.getName()));
                //nounoradj.setBackground(MainAPI.this.postagger.getPoscolors().get(nounoradj.getName()));
            }
            //System.out.println("Highlighter Color: "+POSTagMain.this.postagger.getPoscolors().get(nounoradj.getName()).getRGB());
            //System.out.println("CaseToHighlights: "+POSTagMain.this.caseToHighlights.get(nounoradj.getName()).size());
            //System.out.println(POSTagMain.this.caseToHighlights);

            for(Highlighter.Highlight high:MainAPI.this.caseToHighlights.get(nounoradj.getName())) {
                try {
                    newhighlights.add(highlighter.addHighlight(high.getStartOffset(), high.getEndOffset(), paintcolor));
                    highlighter.removeHighlight(high);
                } catch (BadLocationException e1) {
                    System.out.println(e1.getMessage());
                }
            }
        }else{
            Color newColor = JColorChooser.showDialog(
                    new JColorChooser(),
                    "Choose Color",
                    MainAPI.this.postagger.getPoscolors().get(nounoradj.getName()));
            if(newColor!=null){
                nounoradj.setBackground(newColor);
                MainAPI.this.postagger.getPoscolors().put(nounoradj.getName(),newColor);
                for(Highlighter.Highlight high:MainAPI.this.caseToHighlights.get(nounoradj.getName())){
                    try {
                        newhighlights.add(highlighter.addHighlight(high.getStartOffset(),high.getEndOffset(),newColor));
                        highlighter.removeHighlight(high);
                    } catch (BadLocationException e1) {
                        System.out.println(e1.getMessage());
                    }
                }
            }
        }
        MainAPI.this.caseToHighlights.put(nounoradj.getName(),newhighlights);
        MainAPI.this.repaint();
    }

    public static Font getFont(String name, Integer size) throws Exception {

        Font font = Font.createFont(Font.TRUETYPE_FONT, new File(name));
        return font.deriveFont((float)size);
    }

    public void chooseFont(){
        java.util.List<NiceFont> fonts= FontUtils.getCompatibleFonts(charType);
        System.out.println(resultarea.getFont());
        resultarea.setFont(JTFontChooser.showDialog(resultarea,
                resultarea.getFont(),
                fonts.toArray(new Font[fonts.size()]),
                new int[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72},
                charType.getPreviewString()));
    }

    protected void paintPOSTags(final JToolTipArea resultarea,final String translittext,final Boolean translit){
        if(this.postagger instanceof CuneiPOSTagger){
            this.epochPredict.setText("Epoch Prediction: "+((CuneiPOSTagger)this.postagger).detectEpoch(translittext,this.charType,translit));
        }
        this.all=0.;
        this.matches=0.;
        String[] revised=translittext.split("[\\r\\n]+");
        this.caseToHighlights.clear();
        this.wordCountToHighlightData.clear();
        this.postagger.reset();
        for(String pos:this.postagger.getPoscolors().keySet()){
            this.caseToHighlights.put(pos,new LinkedList<>());
        }
        //this.highlighter = (RepaintHighlighter)resultarea.getHighlighter();
        if (translit) {
            int position = 0, endposition = 0;
            for (String revi:revised) {
                String[] revisedwords = revi.split(" \\[");
                for (int w = 0; w < revisedwords.length; ) {
                    String word = revisedwords[w].trim();
                    System.out.println("Word: "+word);
                    position += word.length();
                    List<Integer> result=this.postagger.getPosTag(word, resultarea.getDictHandler());
                    System.out.println("GetPosTag: "+result.toString());
                    Color color=Color.white;
                    if(!result.isEmpty()){
                        color=new Color(result.get(0));
                    }else if(this.postagger.getPoscolors().containsKey(Tags.DEFAULT.toString())){
                        color=(this.postagger.getPoscolors().get(Tags.DEFAULT.toString()));
                    }
                    try {
                        endposition = resultarea.getText().indexOf("]", position - word.length())+1;
                        String paintword = resultarea.getText().substring(position - word.length(), endposition);
                        System.out.println(paintword);
                        if(!result.isEmpty() && (word.length()!=(result.get(2)+2) || result.get(1)!=0)){
                            int i=0,endindex,startindex;
                            while(i+2<result.size()){
                                endindex=endposition-word.length()+result.get(i+2);
                                System.out.println("Endposition: "+endposition);
                                System.out.println("Endindex: "+endindex);
                                System.out.println("Word: "+word+" "+word.length());
                                if(!resultarea.getText().substring(endindex-1,endindex).equals("-") && !resultarea.getText().substring(endindex-1,endindex).equals("]")) {
                                    int minusIndex = resultarea.getText().indexOf("-", endindex);
                                    int brackIndex = resultarea.getText().indexOf("]", endindex);
                                    if (minusIndex != -1 && minusIndex < brackIndex) {
                                        endindex = minusIndex+1;
                                    } else {
                                        endindex = brackIndex+1;
                                    }
                                }else if(resultarea.getText().substring(endindex-1,endindex).equals("-") && endindex+2==endposition && !resultarea.getText().substring(endindex,endindex+1).equals("]")){
                                    endindex=resultarea.getText().indexOf("]", endindex)+1;
                                }
                                startindex=position-word.length()+result.get(i+1);
                                if(!this.resultarea.getText().substring(startindex,startindex+1).matches("[\\[|-]")){
                                    if(this.resultarea.getText().substring(startindex+1,startindex+2).matches("[\\[|-]")){
                                        startindex++;
                                    }else if(this.resultarea.getText().substring(startindex-1,startindex).matches("[\\[|-]")){
                                        startindex--;
                                    }
                                }
                                this.caseToHighlights.get(this.postagger.getColorToPos().get(new Color(result.get(i)).getRGB())).add(highlighter.addHighlight(startindex,
                                        endindex, new Color(result.get(i))));
                                //highlighter.addHighlight(startindex, endindex, new DefaultHighlighter.DefaultHighlightPainter(new Color(result.get(i))));
                                i+=3;
                            }
                            if(this.postagger.getPoscolors().containsKey(Tags.DEFAULT.toString())){
                                this.caseToHighlights.get(Tags.DEFAULT.toString()).add(highlighter.addHighlight(position - word.length(), endposition, this.postagger.getPoscolors().get(Tags.DEFAULT.toString())));
                            }
                        }else{
                            this.caseToHighlights.get(this.postagger.getColorToPos().get(color.getRGB()))
                                    .add(highlighter.addHighlight(position - word.length(), endposition/*+result.get(2)*/, color));
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    position = endposition+1;
                    w++;
                }
                position++;
            }

        }else{
            System.out.println("POSTagging Cuneiform...");
            Boolean newLine=false;
            int position = 0, endposition = 0;
            for (String revi:revised) {
                String[] revisedwords = revi.split(" ");
                for (int w = 0; w < revisedwords.length; ) {
                    String word = revisedwords[w].trim();
                    System.out.println("Word: "+word);
                    position += word.length();
                    List<Integer> result=this.postagger.getPosTag(word,this.resultarea.getDictHandler());
                    System.out.println("GetPosTag: "+result.toString());
                    Color color=Color.white;
                    if(!result.isEmpty()){
                        color=new Color(result.get(0));
                    }else if(this.postagger.getPoscolors().containsKey(Tags.DEFAULT.toString())){
                        color=(this.postagger.getPoscolors().get(Tags.DEFAULT.toString()));
                    }
                    try {
                        int newline=resultarea.getText().indexOf(System.lineSeparator(), position - word.length());
                        int whitespace=resultarea.getText().indexOf(" ", position - word.length());
                        if(newline==-1 && whitespace==-1){
                            endposition=position+word.length();
                        }else if((newline==-1 || whitespace<newline) && whitespace!=-1){
                            endposition=whitespace+1;
                        }else if(whitespace==-1 || whitespace>newline){
                            endposition=newline;
                            newLine=true;
                        }
                        System.out.println("Endposition: "+endposition);
                        System.out.println("Position-Word.length(): "+(position - word.length()));
                        /*if(resultarea.getText().indexOf(" ", position - word.length())<resultarea.getText().indexOf(System.lineSeparator(), position - word.length())){
                            endposition = resultarea.getText().indexOf(" ", position - word.length())+1;
                        }else{
                            endposition=resultarea.getText().indexOf(System.lineSeparator(), position - word.length());
                            newLine=true;
                        }*/
                        String paintword = resultarea.getText().substring(position - word.length(), endposition);
                        System.out.println(paintword);
                        if(!result.isEmpty() && (word.length()!=(result.get(2)+2) || result.get(1)!=0)){
                            int i=0,endindex,startindex;
                            while(i+2<result.size()){
                                endindex=endposition-word.length()+result.get(i+2)-1;
                                startindex=position - word.length()>0?position - word.length()+result.get(i+1)-1:0;
                                this.caseToHighlights.get(this.postagger.getColorToPos().get(new Color(result.get(i)).getRGB()))
                                        .add(highlighter.addHighlight(startindex, endindex, new Color(result.get(i))));
                                i+=3;
                            }
                            if(this.postagger.getPoscolors().containsKey(Tags.DEFAULT.toString())){
                                this.caseToHighlights.get(Tags.DEFAULT.toString())
                                        .add(highlighter.addHighlight(position - word.length()-1>0?position - word.length()-1:0, endposition - 1, this.postagger.getPoscolors().get(Tags.DEFAULT.toString())));
                            }
                        }else{
                            this.caseToHighlights.get(this.postagger.getColorToPos().get(color.getRGB()))
                                    .add(highlighter.addHighlight(position - word.length()-1>0?position - word.length()-1:0, endposition - 1/*+result.get(2)*/, color));
                        }
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    position = endposition;
                    if(newLine){

                        newLine=false;
                    }else{
                        position++;
                    }
                    w++;
                }

                position++;
            }
        }
       /* Map<Integer,String> sentences=this.postagger.sentenceDetector(this.resultarea.getText().split("[\\s\\r\\n]+"),this.resultarea.getText().split(System.lineSeparator()));
        ((TextLineNumber)this.scrollPane.getRowHeader().getView()).setColorswitches(sentences);
        ((TextLineNumber)this.scrollPane.getRowHeader().getView()).setPostagger(this.postagger);
        ((TextLineNumber)this.scrollPane.getRowHeader().getView()).documentChanged();   */
        //this.scrollPane.setRowHeaderView(new TextLineNumber(resultarea,sentences));
        System.out.println("PostaggedSylls: "+this.matches+" All Sylls: "+this.all+" Postagged/All: "+this.matches/this.all);
    }


    public abstract void paintResultArea();

    public void clear(){
        this.resultarea.setText("");
        this.epochPredict.setText("");
    }

    protected void setCharType(CharTypes charType,JToolTipArea area,JTabbedPane tabbedPane){
        //this.postagger = charType.getCorpusHandlerAPI().getPOSTagger(false);
        //this.remove(this.legendpanel);
        this.repaint();
//        if (this.postagger != null)
//            this.createLegend(this.postagger.getPoscolors(),false,tabbedPane,"Legend",true);
        area.setPostagger(charType);
        area.setDictHandler(charType.getCorpusHandlerAPI().getUtilDictHandler());
        this.repaint();
    }

    protected void definitionReload(CharTypes charType,JTabbedPane tabbedPane){
        //Handle open trainfilebutton action.
        this.postagger=charType.getCorpusHandlerAPI().getPOSTagger(true);
        if(this.highlighter!=null){
            this.highlighter.removeAllHighlights();
        }
        this.highlighted=false;
        if(this.legendpanel!=null) {
            this.remove(this.legendpanel);
            this.repaint();
        }
        resultarea.setPostagger(this.postagger);
        resultarea.setDictHandler(charType.getCorpusHandlerAPI().getUtilDictHandler());
        this.createLegend(this.postagger.getPoscolors(),false,tabbedPane,"Legend",false);
        this.repaint();


    }

    protected void loadFile(CharTypes charType,JFileChooser trainfilechooser,JTextField trainingfilefield,Boolean atf,Boolean transliteration) {
        //Handle open trainfilebutton action.
        int returnVal = trainfilechooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = trainfilechooser.getSelectedFile();
            trainingfilefield.setText(file.getAbsolutePath());
            if (transliteration) {
                resultarea.setFont(new Font(new JLabel().getFont().getName(), 0, TRANSLITFONTSIZE));
            } else {
                try {
                    resultarea.setFont(new Font(new JLabel().getFont().getName(), 0, TRANSLITFONTSIZE));
                    //POSTagMain.this.resultarea.setFont(POSTagMain.getFont("cunei.ttf",CUNEIFONTSIZE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            resultarea.setText("");
            try {
                StringBuilder result = new StringBuilder();

                if (atf) {
                    BufferedReader reader = new BufferedReader(new FileReader(trainfilechooser.getSelectedFile()));
                    String temp;
                    while ((temp = reader.readLine()) != null) {
                        result.append(charType.getCorpusHandlerAPI().corpusToReformatted(temp));
                    }
                    reader.close();
                    resultarea.setText(result.toString());
                } else {
                    resultarea.read(new FileReader(trainfilechooser.getSelectedFile()), null);
                }
                TextLineNumber tln = new TextLineNumber(resultarea, new TreeMap<>());
                scrollPane.setRowHeaderView(tln);
                this.highlighted = false;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        e.getMessage(), MainFrame.bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
            System.out.println("Opening: " + file.getName() + ".");
            this.epochPredict.setText("");
        } else {
            System.out.println("Open command cancelled by user.");
        }
    }
    protected void saveFile(String text){
        JFrame parentFrame = new JFrame();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(parentFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                System.out.println("Save as file: " + fileToSave.getAbsolutePath());
                FileWriter writer = new FileWriter(fileToSave);
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

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

package com.github.situx.postagger.main.gui.comp;

import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.main.gui.util.TextLineNumber;
import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.MethodEnum;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * ComparingGUI for viewing results..
 */
abstract class CompGUI extends JPanel {


    protected static Integer CUNEIFONTSIZE=16;
    static Integer TRANSLITFONTSIZE=12;
    public ResourceBundle bundle=ResourceBundle.getBundle("Master", Locale.getDefault(),new UTF8Bundle("UTF-8"));
    JButton diffbutton;
    JComboBox<ClassificationMethod> featuresetchooser;
    protected String generatedFile="";
    private String originalfile="";
    protected Double matches,all;
    protected String origTranslit ="", genTranslit ="",origCunei="",genCunei="";
    JToolTipArea resultarea,resultarea2;
    private JScrollPane scrollPane;
    private JScrollPane scrollPane2;
    private Boolean selectedMethodsG1;
    protected JTextField statistics;
    JButton switchbutton;
    JButton switchbutton2;
    boolean switchflag1,switchflag2,highlightedleft=false,highlightedright=false;
    int y=0;

    /**
     * Constructor for CompGUI.
     * @param originalfile
     * @param generatedFile
     * @param selectedMethods
     */
    CompGUI(final String originalfile, final String generatedFile, final java.util.List<MethodEnum> selectedMethods){
        this.setLayout(new GridBagLayout());
        this.selectedMethodsG1=selectedMethods.size()>1;
        GridBagConstraints c = new GridBagConstraints();
        this.generatedFile=generatedFile;
        this.originalfile=originalfile;
        this.switchbutton=new JButton();
        this.switchbutton2=new JButton();
        this.diffbutton=new JButton();
        int ysize=540;
        JPanel mainPanel=new JPanel();
        JPanel mainPanel2=new JPanel();
        this.statistics=new JTextField();
        this.statistics.setEnabled(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y;
        c.gridwidth=1;
        this.add(statistics,c);
        if(selectedMethods.size()>1){
            JPanel featurePanel=new JPanel();
            JLabel featureset = new JLabel(bundle.getString("algorithm")+":");
            Set<MethodEnum> evalset=new TreeSet<>(selectedMethods);
            featuresetchooser = new JComboBox<ClassificationMethod>(evalset.toArray(new ClassificationMethod[evalset.size()]));

            ysize=530;
            featurePanel.add(featureset);
            featurePanel.add(featuresetchooser);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth=1;
            this.add(featurePanel,c);
        }else{
            y++;
        }
        JLabel original=new JLabel(bundle.getString("original"));
        JLabel generated=new JLabel(bundle.getString("generated"));
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
        c.gridy = y;
        c.gridwidth=1;
        this.add(mainPanel,c);
        c.gridx=1;
        this.add(mainPanel2,c);
        this.resultarea=new JToolTipArea(null,null,bundle,false,true)
        {
            public boolean getScrollableTracksViewportWidth()
            {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        this.scrollPane = new JScrollPane();
        this.scrollPane.setViewportView(this.resultarea);
        resultarea.setEditable(false);
        scrollPane.setPreferredSize(new Dimension(530,ysize));
        mainPanel.add(scrollPane);
        this.resultarea2=new JToolTipArea(null,null,bundle,false,true)
        {
            public boolean getScrollableTracksViewportWidth()
            {
                return getUI().getPreferredSize(this).width
                        <= getParent().getSize().width;
            }
        };
        TextLineNumber tln = new TextLineNumber(resultarea,new TreeMap<>());
        scrollPane.setRowHeaderView( tln );
        this.scrollPane2 = new JScrollPane();
        this.scrollPane2.setViewportView(this.resultarea2);
        resultarea2.setEditable(false);
        this.resultarea.setFont(new Font(this.resultarea.getFont().getName(), 0, TRANSLITFONTSIZE));
        scrollPane2.setPreferredSize(new Dimension(530,ysize));
        this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, TRANSLITFONTSIZE));
        TextLineNumber tln2 = new TextLineNumber(resultarea2,new TreeMap<>());
        scrollPane2.setRowHeaderView( tln2 );
        scrollPane.setPreferredSize(new Dimension(530,ysize));
        mainPanel.add(scrollPane);
        mainPanel.setPreferredSize(new Dimension(530,ysize));
        scrollPane2.setPreferredSize(new Dimension(530,ysize));
        mainPanel2.add(scrollPane2);
        mainPanel2.setPreferredSize(new Dimension(530,ysize));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = ++y;
        c.gridwidth=1;
        this.add(switchbutton,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth=1;
        this.add(switchbutton2,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridwidth=2;
        this.add(diffbutton,c);
    }

    public static String findMissingSegments(final String originalstr,final String revisedstr,final CharTypes chartype){
        String result="";
        java.util.List<String> original;
        java.util.List<String> revised;
        original = Arrays.asList(originalstr.split("\n"));
        revised = Arrays.asList(revisedstr.split("\n"));
        int position = 0;
        for (int i = 0; i < revised.size(); i+=chartype.getChar_length()) {
            String charline=original.get(i).replaceAll(" ","");
            String prev,fol="",currentoriginal,currentrevised;
            int revisedoffset=0,originaloffset=0;

            for(int j=0;j<charline.length()/*-chartype.getChar_length()*/;j+=chartype.getChar_length()){
                   prev=charline.substring(j,j+chartype.getChar_length());
                   //fol=charline.substring(j+chartype.getChar_length(),j+chartype.getChar_length()*2);
                   currentrevised=revised.get(i).substring(j+revisedoffset,j+revisedoffset+chartype.getChar_length());
                   currentoriginal=original.get(i).substring(j+originaloffset,j+originaloffset+chartype.getChar_length());
                   System.out.println("Currentrevised: "+currentrevised);
                   System.out.println("Currentoriginal: "+currentoriginal);
                   System.out.println("Prev: "+prev);
                   System.out.println("Fol: "+fol);
                   if(currentoriginal.matches("[ ]+")){
                       if(currentrevised.matches("[ ]+")){
                           while(original.get(i).substring(j+originaloffset,j+originaloffset+1).equals(" ")){
                               originaloffset++;
                           }
                           while(revised.get(i).substring(j+revisedoffset,j+revisedoffset+1).equals(" ")){
                               revisedoffset++;
                           }
                           result+=("   "+currentoriginal);
                       }else{
                           while(original.get(i).substring(j+originaloffset,j+originaloffset+1).equals(" ")){
                               originaloffset++;
                           }
                           //txt.replaceRange( replace, nextPosn, nextPosn + find.length() );
                           result+=" "+currentrevised;
                       }

                   }else{
                       if(currentrevised.matches("[ ]+")){
                           while(revised.get(i).substring(j+revisedoffset,j+revisedoffset+1).equals(" ")){
                               revisedoffset++;
                           }
                           result+="   "+currentoriginal;
                       }else{
                           result+=currentoriginal;
                       }
                   }
            }
        }
        System.out.println("Result: "+result);
        return result;
    }

    public void createLegend(java.util.Map<String,Color> legenddata){
        JPanel legendpanel=new JPanel();
        legendpanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int y=2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = y++;
        c.gridwidth=1;
        this.add(legendpanel,c);
        for(String key:legenddata.keySet()){
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth=1;
            JLabel nounoradj=new JLabel(bundle.getString(key));
            Border border = BorderFactory.createLineBorder(Color.BLACK, 1);

            // set the border of this component
            nounoradj.setBorder(border);
            nounoradj.setOpaque(true);
            nounoradj.setBackground(legenddata.get(key));
            legendpanel.add(nounoradj,c);
        }
    }

    public String exchangeMethodName(String filename,ClassificationMethod method){
        return filename.substring(0,filename.indexOf('_')+1)+method.toString().toLowerCase()+filename.substring(filename.indexOf('_',filename.indexOf('_')+1));
    }

    public abstract void paintResultArea(DictHandling dictHandler);

    public void setGeneratedContents(File file,Integer font1,Integer font2,Integer spaces) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        this.resultarea2.setText("");
        this.resultarea2.read(br, null);
        br.close();
        StringBuilder space=new StringBuilder();
        for(int i=0;i<spaces;i++){
            space.append(" ");
        }
        this.resultarea2.setText(this.resultarea2.getText().replaceAll(" ",space.toString()));
        if(switchflag2){

            this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, font1));
        }else{
            this.resultarea2.setFont(new Font(this.resultarea2.getFont().getName(), 0, font2));
        }
        TextLineNumber tln = new TextLineNumber(resultarea2,new TreeMap<>());
        scrollPane2.setRowHeaderView(tln);
        this.highlightedright=false;
    }

    public void setOriginalContents(File file,Integer font1,Integer font2,Integer spaces) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        this.resultarea.setText("");
        this.resultarea.read(br, null);
        br.close();
        StringBuilder space=new StringBuilder();
        for(int i=0;i<spaces;i++){
            space.append(" ");
        }
        this.resultarea.setText(this.resultarea.getText().replaceAll(" ",space.toString()));
        if(switchflag1){
            this.resultarea.setFont(new Font(this.resultarea.getFont().getName(), 0, font1));
        }else{
            this.resultarea.setFont(new Font(this.resultarea.getFont().getName(), 0, font2));
        }
        TextLineNumber tln = new TextLineNumber(resultarea,new TreeMap<>());
        scrollPane.setRowHeaderView( tln );
        this.highlightedleft=false;
    }

}

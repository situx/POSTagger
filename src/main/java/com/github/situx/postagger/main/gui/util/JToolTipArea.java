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

package com.github.situx.postagger.main.gui.util;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.corpusimport.CorpusHandlerAPI;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.pos.POSTags;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Created by timo on 06.10.14.
 */
public class JToolTipArea extends JEditorPane implements MouseListener
        //,GenericInputMethodComponent
{

    private final Boolean alltranslits;
    protected ResourceBundle bundle;
    protected POSTagger postagger;

    protected CorpusHandlerAPI corpusHandler;

    protected DictHandling dictHandler;

    private Boolean cuneiFormFlag;

    protected CharTypes charType;

//    private AkkInputMethodContext inputContext = new AkkInputMethodContext(this);

    public Boolean getCuneiFormFlag() {
        return cuneiFormFlag;
    }

    public void setCuneiFormFlag(final Boolean cuneiFormFlag) {
        this.cuneiFormFlag = cuneiFormFlag;
    }

    public JToolTipArea(CharTypes charType,DictHandling dictHandler,ResourceBundle bundle,Boolean cuneiFormFlag,Boolean alltranslits){
        this.postagger=charType.getCorpusHandlerAPI().getPOSTagger(false);
        this.dictHandler=dictHandler;
        this.corpusHandler=charType.getCorpusHandlerAPI();
        this.bundle=bundle;
        this.charType=charType;
        this.cuneiFormFlag=cuneiFormFlag;
        this.alltranslits=alltranslits;
        this.addMouseListener(this);
    }

    public POSTagger getPostagger() {
        return postagger;
    }

    protected String getCurrentWordFromView(Point point){
        Integer pos = this.viewToModel(point);
        String currentword = "";
        try {
            int end = Utilities.getWordEnd(this, pos), start = Utilities.getWordStart(this, pos);
            if (start != end) {
                while (!this.getText().substring(end - 1, end).equals("]") && !this.getText().substring(end - 1, end).equals(" ") && !this.getText().substring(end - 1, end).equals(System.lineSeparator())) {
                    end = Utilities.getWordEnd(this, end);
                }
                while (start>0 && !this.getText().substring(start, start + 1).matches("(\\[|\\])") && !this.getText().substring(start, start + 1).equals(" ") && !this.getText().substring(end - 1, end).equals(System.lineSeparator())) {
                        start = Utilities.getWordStart(this, start - 1);
                }
            }
            currentword = this.getText().substring(start, end);
            System.out.println("Currentword: " + currentword);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return currentword;
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        System.out.println("MouseClickedEvent");
        if(event.getClickCount()>=2 && SwingUtilities.isLeftMouseButton(event)) {
            String currentword=this.getCurrentWordFromView(event.getPoint());
            List<POSDefinition> posdefs=this.postagger.getPosTagDefs(currentword,dictHandler);
            StringBuilder result =new StringBuilder();
            result.append("<html>");
                if (!currentword.isEmpty() && !currentword.matches("[ ]+|\\[|\\]")) {
                    result.append(currentword.endsWith("-") ? currentword.substring(0, currentword.length() - 1) : currentword);
                    String temp="";
                    if (currentword.matches(charType.getLegalTranslitCharsRegex())) {
                        temp = corpusHandler.transliterationToText(currentword.toLowerCase(), 0, dictHandler, false, true);
                    } else {
                        if(posdefs.get(0).getValue().length==0){
                            temp = Methods.assignTransliteration(currentword.split(" "), dictHandler, TransliterationMethod.PROB,true);
                        }else{
                            temp=posdefs.get(0).getValue()[0];
                        }

                    }
                    if(!temp.isEmpty()) {
                        ClipboardHandler handler = new ClipboardHandler();
                        handler.setClipboardContents(temp);
                        JButton button = new JButton("");
                        button.setBackground(Color.lightGray);
                        button.setOpaque(true);
                        button.setText(temp + " copied to clipboard!");
                        final Popup popup = PopupFactory.getSharedInstance().getPopup(this, button, Double.valueOf(this.getBounds().getX()).intValue()+500,
                                Double.valueOf(this.getBounds().getY()+570).intValue());
                        popup.show();
                        ActionListener hider = e -> popup.hide();
                        // Hide popup in 3 seconds
                        Timer timer = new Timer(4000, hider);
                        timer.start();
                    }
            }else{
                    result.append(currentword);
                    result.append("</html>");
            }

        }else if(event.getClickCount()>=2 && SwingUtilities.isRightMouseButton(event)){
            String currentword=this.getCurrentWordFromView(event.getPoint());
            List<POSDefinition> posdefs=this.postagger.getPosTagDefs(currentword,dictHandler);
            LangChar temp=POSDefinition.getTargetWord(charType,posdefs.get(0).currentword,posdefs.get(0).getVerbStem());
            if(temp!=null && temp.getConceptURI()!=null){
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI(temp.getConceptURI().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if(SwingUtilities.isRightMouseButton(event) && event.getClickCount()<2){
            String currentword=this.getCurrentWordFromView(event.getPoint());
            List<POSDefinition> posdefs=this.postagger.getPosTagDefs(currentword,dictHandler);
            String temp=posdefs.get(posdefs.size()-1).getRegex().toString();
            ClipboardHandler handler = new ClipboardHandler();
            handler.setClipboardContents(temp);
            JButton button = new JButton("");
            button.setBackground(Color.lightGray);
            button.setOpaque(true);
            button.setText(temp + " copied to clipboard!");
            final Popup popup = PopupFactory.getSharedInstance().getPopup(this, button, Double.valueOf(this.getBounds().getX()).intValue()+500,
                    Double.valueOf(this.getBounds().getY()+570).intValue());
            popup.show();
            ActionListener hider = e -> popup.hide();
            // Hide popup in 3 seconds
            Timer timer = new Timer(4000, hider);
            timer.start();
        }
    }

   // @Override
   /* public AkkInputMethodContext getInputContext() {
        return this.inputContext;
    }*/

    @Override
    public void mousePressed(final MouseEvent event) {

    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {

    }

    public void setPostagger(final CharTypes charType) {
        this.postagger=charType.getCorpusHandlerAPI().getPOSTagger(true);
        this.corpusHandler=charType.getCorpusHandlerAPI();
        this.dictHandler=charType.getCorpusHandlerAPI().getUtilDictHandler();
        this.charType=charType;
    }

    public void setPostagger(final POSTagger postagger) {
        this.postagger=postagger;
    }

    public DictHandling getDictHandler() {
        return dictHandler;
    }

    public void setDictHandler(final DictHandling dictHandler) {
        this.dictHandler = dictHandler;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        StringBuilder result=new StringBuilder();

        SwingWorker<Object,Void> worker=new SwingWorker<Object,Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                String currentword=JToolTipArea.this.getCurrentWordFromView(event.getPoint());

                if(currentword.isEmpty() || currentword.matches("^[ ]+|\\[|\\]$")){
                    return null;
                }
                else{
                    System.out.println("CurrentWord passed: "+currentword);
                }
                //System.out.println("Postagger: "+this.postagger.toString());
//        System.out.println("DictHandler: "+this.dictHandler.toString());
                System.out.println("CharType: "+JToolTipArea.this.charType.toString());
                List<POSDefinition> posdefs=JToolTipArea.this.charType.getCorpusHandlerAPI().getPOSTagger(false).getPosTagDefs(currentword.trim(), dictHandler);
                System.out.println("POSDEFS: "+posdefs.isEmpty());
                if(!posdefs.isEmpty()) {
                    //Collections.reverse(posdefs);
                    int i = 0;
                    for (POSDefinition posdef : posdefs) {
                        if (i > 0)
                            result.append("--------------------<br>");
                        System.out.println("Convert Posdef to HTML String: "+posdef.toString());
                        result.append(posdef.toHTMLString(bundle, charType, alltranslits));
                        result.append("<br>");
                        i++;
                    }
                }else{
                    System.out.println("Currentword: "+currentword.trim());
                    result.append(currentword.trim());
                    result.append(POSDefinition.getTargetString(charType,currentword,"", POSTags.UNKNOWN.toString()));
                }
                return result;
            }
        };
        worker.execute();
        try {
            worker.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }
        if(result.length()==0 || result.toString().matches("[ ]+|\\[|\\]")){
            return null;
        }
        return "<html>"+result.toString()+"</html>";
    }

}

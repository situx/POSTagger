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

import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by timo on 10/27/14.
 */
public class JTransToolTipArea extends JToolTipArea {

    public void setHighlights(final List<HighlightData> highlights) {
        this.highlights = highlights;
    }

    private List<HighlightData> highlights;

    public void setTranslator(final Translator translator) {
        this.translator = translator;

    }

    private Translator translator;

    public Translator getTranslator() {
        return translator;
    }

    public JTransToolTipArea(final CharTypes charType, final DictHandling dictHandler, final ResourceBundle bundle, final Boolean cuneiFormFlag, Boolean alltranslits, final Translator translator, final List<HighlightData> highlights) {
        super(charType, dictHandler, bundle, cuneiFormFlag,alltranslits);
        this.translator=translator;
        this.highlights=highlights;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        HighlightData highlightData = this.getCurrentWordIndexFromView(event.getPoint());
        if (highlightData != null) {
            StringBuilder result =new StringBuilder();
            result.append("<html>");
            //POSDefinition.getTargetString(charType,highlightData.origword);
            if (!highlightData.origword.isEmpty()) {
                result.append(highlightData.origword.endsWith("-") ? highlightData.origword.substring(0, highlightData.origword.length() - 1) : highlightData.origword);
                if (!highlightData.posTag.getTargetScript().isEmpty()) {
                    result.append(" (");
                    result.append( highlightData.posTag.getTargetScript());
                    result.append(")");
                } else {
                    System.out.println("Currentword: "+highlightData.origword.trim());
                    result.append(highlightData.origword.trim());
                    result.append(POSDefinition.getTargetString(charType,highlightData.origword,"",null));

                }
            }
            if(highlightData.word.size()==1){
                result.append("<br> Translation:");
                result.append(highlightData.word.iterator().next().getOne());
                result.append("</html>");
            }else if(highlightData.getManydecs()){
                result.append("<br> Translation:");
                for (Tuple<String,POSDefinition> trans : highlightData.word) {
                    result.append(trans.getOne());
                    result.append(" ");
                }
                result.append("</html>");
            }else{
                result.append("<ul>");
                for (Tuple<String,POSDefinition> trans : highlightData.word) {
                    result.append("<li>");
                    result.append(trans.getOne());
                    result.append("</li>");
                }
                result.append("</ul></html>");
            }

            return result.toString();
        } else {
            return null;
        }

    }


    @Override
    public void mouseClicked(final MouseEvent event) {
        System.out.println("MouseClickedEvent");
        if(event.getClickCount()>=1 && SwingUtilities.isLeftMouseButton(event)) {
            System.out.println("Currentwordindex: "+this.getCurrentWordIndexFromView(event.getPoint()));
            final HighlightData highlightData=this.getCurrentWordIndexFromView(event.getPoint());
            if(highlightData!=null){
                final JPopupMenu menu=new JPopupMenu();
                menu.setLabel(highlightData.origword);
                JMenuItem currentitem;
                for (final Tuple<String,POSDefinition> trans : highlightData.word) {
                    currentitem=new JMenuItem(trans.getOne());
                    currentitem.addActionListener(actionEvent -> {
                        try {
                            JTransToolTipArea.this.requestFocusInWindow();
                            JTransToolTipArea.this.grabFocus();
                            JTransToolTipArea.this.getDocument().remove(highlightData.getStart()+1, highlightData.getEnd() - highlightData.getStart()-1);
                            JTransToolTipArea.this.getDocument().insertString(highlightData.getStart()+1, trans.getOne()+" ", null);
                            highlightData.setEnd(highlightData.getStart()+trans.getOne().length()+1);
                            JTransToolTipArea.this.getDocument().remove(highlightData.getStart(),1);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        menu.hide();
                    });
                    menu.add(currentitem);
                }
                if(highlightData.word.size()>1 && !highlightData.getManydecs())
                    menu.show(JTransToolTipArea.this, event.getX(), event.getY());
            }else{
                System.out.println("Highlightdata==null");
            }
        }
    }

    public List<HighlightData> getHighlights() {
        return highlights;
    }

    protected HighlightData getCurrentWordIndexFromView(Point point){
        Integer pos=this.viewToModel(point);
        Integer pos2=this.viewToModel(new Point(0,Double.valueOf(point.getY()).intValue()));
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
        int lineNumber=this.getLineNumber(pos2);
        System.out.println("Linenumber: "+lineNumber+" "+pos2);
        if(!this.translator.lineToWordCount.containsKey(lineNumber) || !this.translator.lineToWordCount.containsKey(lineNumber+1)){
             return null;
        }
        int highlightindexstart=this.translator.lineToWordCount.get(lineNumber);
        int highlightindexend=this.translator.lineToWordCount.get(lineNumber+1);
        System.out.println("Highlightindexstart: "+highlightindexstart);
        System.out.println("Highlightindexend: "+highlightindexend);
        if(highlightindexend>highlightindexstart) {
            for (HighlightData currenth:highlights) {
                /*System.out.println("High: "+currenth);
                System.out.println("Cur: "+currenth);*/
                //System.out.println("Current: "+current.getStart()+" "+current.getEnd()+" "+current.word.get(0).getOne());
                if (currenth.getStart() < pos && pos < currenth.getEnd()) {
                    return currenth;
                }
            }
        }
        return null;
    }

    /*
*	Get the line number to be drawn. The empty string will be returned
*  when a line of text has wrapped.
*/
    protected Integer getLineNumber(int rowStartOffset)
    {
        Element root = this.getDocument().getDefaultRootElement();
        int index = root.getElementIndex( rowStartOffset );
        Element line = root.getElement( index );
        return index+1;
    }

}

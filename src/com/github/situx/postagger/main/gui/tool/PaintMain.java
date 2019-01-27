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

import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.main.gui.ime.jquery.tree.IMETree;
import com.github.situx.postagger.main.gui.ime.jquery.tree.PaintIMETree;
import com.github.situx.postagger.main.gui.util.ClipboardHandler;
import com.github.situx.postagger.main.gui.util.JToolTipArea;
import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import hanzilookup.ui.CharacterCanvas;
import hanzilookup.ui.WrittenCharacter;
import kiang.swing.JClickableList;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 * Created by timo on 3/26/15.
 */
public class PaintMain extends JPanel {

    public static final int WRAP_COUNT = 3;
    private boolean autoLookup=true;
    public ResourceBundle bundle = ResourceBundle.getBundle("POSTagger", Locale.getDefault(), new UTF8Bundle("UTF-8"));
    private Integer looseness=0;

    private JClickableList lookUpList;

    private DictHandling dictHandler;

    private CharacterCanvas inputCanvas;
    private boolean mode1;

    private JToolTipArea result2;

    final JLabel aLabel = new JLabel("A:");
    final JLabel bLabel = new JLabel("B:");
    final JLabel cLabel = new JLabel("C:");
    final JLabel dLabel = new JLabel("D:");
    final JLabel gLabel = new JLabel("Strokes:");
    Integer a=0,b=0,c=0,d=0,g=0;

    public PaintMain(final POSTagger postagger, final CharTypes originalType,final DictHandling dictHandling) {
        this.inputCanvas = new CharacterCanvas();
        this.dictHandler=dictHandling;
        inputCanvas.setMinimumSize(new Dimension(100, 220));
        inputCanvas.setPreferredSize(new Dimension(100, 220));
        inputCanvas.addStrokesListener(PaintMain.this::strokeFinished);
        this.mode1=true;
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(inputCanvas, BorderLayout.CENTER);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter character"));
        this.setLayout(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints();
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 2));
        statusPanel.add(aLabel);
        statusPanel.add(bLabel);
        statusPanel.add(cLabel);
        statusPanel.add(dLabel);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=1;
        this.add(statusPanel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=1;
        this.add(inputPanel,c);
        // Have to make them final to make them accessible to the anonymous listener.
        final JButton lookupButton = new JButton("Lookup");
        final JButton clearButton = new JButton("Clear");
        final JButton undoButton = new JButton("Undo");
        // Anonymous listener handles button clicks.
        ActionListener canvasButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(ae.getSource() == lookupButton) {
                    lookupButton.setEnabled(false);
                    PaintMain.this.runLookup(false);
                    lookupButton.setEnabled(true);
                } else if (ae.getSource() == clearButton){
                    // by default must be that ae.getSource() == clearButton
                    inputCanvas.clear();
                    inputCanvas.repaint();
                    PaintMain.this.a=0;
                    PaintMain.this.b=0;
                    PaintMain.this.c=0;
                    PaintMain.this.d=0;
                    PaintMain.this.g=0;
                    PaintMain.this.aLabel.setText("A: ");
                    PaintMain.this.bLabel.setText("B: ");
                    PaintMain.this.cLabel.setText("C: ");
                    PaintMain.this.dLabel.setText("D: ");
                    PaintMain.this.gLabel.setText("Strokes: ");
                    PaintMain.this.result2.setText("");
                } else if (ae.getSource() == undoButton){
                    // by default must be that ae.getSource() == clearButton
                   inputCanvas.undo();
                }
            }

        };
        lookupButton.addActionListener(canvasButtonListener);
        clearButton.addActionListener(canvasButtonListener);
        undoButton.addActionListener(canvasButtonListener);
        final JCheckBox autoLookupCheckBox = new JCheckBox("Auto Lookup");
        autoLookupCheckBox.setSelected(this.autoLookup);
        autoLookupCheckBox.addActionListener(e -> PaintMain.this.autoLookup=autoLookupCheckBox.isSelected());
        final JCheckBox gottstein = new JCheckBox("Gottstein");
        gottstein.setSelected(this.mode1);
        gottstein.addActionListener(e -> {
            PaintMain.this.mode1=gottstein.isSelected();
            PaintMain.this.runLookup(false);
        });
        JPanel checkBoxes=new JPanel();
        checkBoxes.setLayout(new GridLayout(1, 2));
        checkBoxes.add(autoLookupCheckBox);
        checkBoxes.add(gottstein);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));	// equally space the buttons
        buttonPanel.add(lookupButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth=1;
        this.add(checkBoxes,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth=1;
        this.add(buttonPanel,c);
        final JPanel loosenessPanel=new JPanel();
        final JLabel loosenessLabel=new JLabel("Looseness:");
        final JSlider loosenessSlider = new JSlider(0, 20);
        loosenessSlider.setValue(this.looseness);
        loosenessSlider.setMaximum(10);
        loosenessSlider.setMinimum(0);
        loosenessSlider.setMajorTickSpacing(2);
        loosenessSlider.setMinorTickSpacing(1);
        loosenessSlider.setPaintTicks(true);
        loosenessSlider.setPaintLabels(true);
        loosenessSlider.addChangeListener(e -> {
            PaintMain.this.looseness = loosenessSlider.getValue();
            PaintMain.this.runLookup(false);
        });
        loosenessPanel.add(loosenessLabel);
        loosenessPanel.add(loosenessSlider);
        loosenessPanel.setMinimumSize(new Dimension(200,80));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        this.add(new JLabel("DrawPanel: "+this.dictHandler.getPaintTree().getWordCounter()+" Characters"),c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        this.add(gLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=1;
        this.add(loosenessPanel,c);
        this.lookUpList=new JClickableList();
        this.lookUpList.setModel(new DefaultListModel<PaintIMETree>());
        this.lookUpList.setListData(new String[]{"0", "1", "2"});
        this.result2=new JToolTipArea(originalType,this.dictHandler,this.bundle,null,false){
                @Override
                public void mouseClicked(final MouseEvent event) {
                    System.out.println("MouseClickedEvent");
                    if(event.getClickCount()>=2 && SwingUtilities.isLeftMouseButton(event)) {
                        String currentword=this.getCurrentWordFromView(event.getPoint());
                            if(!currentword.isEmpty()) {
                                ClipboardHandler handler = new ClipboardHandler();
                                handler.setClipboardContents(currentword);
                                JButton button = new JButton("");
                                button.setBackground(Color.lightGray);
                                button.setOpaque(true);
                                button.setText(currentword + " copied to clipboard!");
                                final Popup popup = PopupFactory.getSharedInstance().getPopup(this, button, Double.valueOf(this.getBounds().getX()).intValue()+500, Double.valueOf(this.getBounds().getY()+570).intValue());
                                popup.show();
                                ActionListener hider = e -> popup.hide();
                                // Hide popup in 3 seconds
                                Timer timer = new Timer(4000, hider);
                                timer.start();
                            }
                    } else if(SwingUtilities.isRightMouseButton(event)){
                        String currentword=this.getCurrentWordFromView(event.getPoint());
                        List<POSDefinition> posdefs=this.postagger.getPosTagDefs(currentword,dictHandler);
                        String temp=posdefs.get(posdefs.size()-1).getRegex().toString();
                        ClipboardHandler handler = new ClipboardHandler();
                        handler.setClipboardContents(temp);
                        JButton button = new JButton("");
                        button.setBackground(Color.lightGray);
                        button.setOpaque(true);
                        button.setText(temp + " copied to clipboard!");
                        final Popup popup = PopupFactory.getSharedInstance().getPopup(this, button, Double.valueOf(this.getBounds().getX()).intValue()+500, Double.valueOf(this.getBounds().getY()+570).intValue());
                        popup.show();
                        ActionListener hider = e -> popup.hide();
                        // Hide popup in 3 seconds
                        Timer timer = new Timer(4000, hider);
                        timer.start();
                    }
                }
        };
        this.result2.setText(System.lineSeparator()+System.lineSeparator()+System.lineSeparator()+System.lineSeparator()+System.lineSeparator());
        result2.setEditable(false);
        result2.setPreferredSize(new Dimension(100, 220));
        if(new File("fonts/"+originalType.getLocale()+".ttf").exists()){
            try {
                result2.setFont(MainAPI.getFont("fonts/"+originalType.getLocale()+".ttf",MainAPI.CUNEIFONTSIZE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            result2.setFont(result2.getFont().deriveFont(40f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        lookUpList.setMinimumSize(new Dimension(100, 220));
        lookUpList.setPreferredSize(new Dimension(100, 220));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        final JScrollPane scrollPane = new JScrollPane(result2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(100, 220));
        this.add(scrollPane,c);
        this.setPreferredSize(this.getPreferredSize());
    }

    private void undo() {
        this.lookUpList=new JClickableList();
        inputCanvas.clear();
        inputCanvas.repaint();
        this.result2.setText("");
    }

    private void runLookup(Boolean strokeWritten) {
        WrittenCharacter writtenCharacter = this.inputCanvas.getCharacter();
        if(writtenCharacter.getStrokeList().isEmpty()){
            return;
        }
        ToolTipManager.sharedInstance().registerComponent(result2);
        if(strokeWritten){
            WrittenCharacter.WrittenStroke stroke=(WrittenCharacter.WrittenStroke)writtenCharacter.getStrokeList().get(writtenCharacter.getStrokeList().size()-1);
            this.getDirection(stroke);
        }
        System.out.println("Strokelist Size: "+writtenCharacter.getStrokeList().size());
        java.util.List<? extends IMETree> toDisplay;
        if(this.mode1){
              toDisplay=this.dictHandler.getPaintTree().query(new Integer[]{a, b, c, d}, this.looseness);
        }else{
              toDisplay=this.dictHandler.getPaintTree().getLengthToSubTree().get(writtenCharacter.getStrokeList().size());
        }
        if(!toDisplay.isEmpty()){
            StringBuilder text=new StringBuilder();
            Set<String> chars=new TreeSet<String>();
            int wrapcounter=0;
            for(IMETree s:toDisplay){
                chars.add(s.getChars());
            }
            text.append(" ");
            for(String c:chars){
                if(wrapcounter==WRAP_COUNT){
                    text.append(c);
                    text.append(System.lineSeparator());
                    text.append(" ");
                    wrapcounter=0;
                }else{
                    text.append(c);
                    text.append(" ");
                    wrapcounter++;
                }
            }
            this.result2.setText(text.toString());
        }else{
            this.result2.setText("");
            System.out.println("No result");
        }
    }

    private Integer getDirection(WrittenCharacter.WrittenStroke stroke){
        Point first=(Point)stroke.getPointList().get(0);
        Point second=(Point)stroke.getPointList().get(stroke.getPointList().size()-1);
        System.out.println("Points of Stroke: " + first+" "+second);
        double delta_x = second.x - first.x;
        double delta_y = second.y - first.y;
        double m=delta_y/delta_x;
        double radius = Math.atan(m)*100;
        System.out.println("Angle: "+radius);
        this.gLabel.setText("Strokes: "+(++g));
        if(radius>140 && radius<200){
            System.out.println("Detected: A");
            this.aLabel.setText("A: "+(++a));
            return 0;
        }else if(radius>-30 && radius<30){
            System.out.println("Detected: B");
            this.bLabel.setText("B: "+(++b));
            return 1;
        }else if(radius<-30 && radius>-170){
            System.out.println("Detected: D");
            this.dLabel.setText("D: "+(++d));
            return 2;
        }else if(radius>30 && radius<150){
            System.out.println("Detected: C");
            this.cLabel.setText("C: "+(++c));
            return 3;
        }
        return 0;
    }


    private void strokeFinished(CharacterCanvas.StrokeEvent e) {
        if(this.autoLookup) {
            this.runLookup(true);
        }
    }

}

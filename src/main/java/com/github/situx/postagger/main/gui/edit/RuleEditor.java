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

package com.github.situx.postagger.main.gui.edit;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.main.gui.util.LineNumberTable;
import com.github.situx.postagger.main.gui.util.POSTableRenderer;
import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.main.gui.util.POSTableModel;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by timo on 3/23/15.
 */
public class RuleEditor extends JFrame {

    public ResourceBundle bundle=ResourceBundle.getBundle("POSTagger", Locale.getDefault(),new UTF8Bundle("UTF-8"));

    public RuleEditor(final POSTagger posTagger, String title){
        this.setTitle(title);
        this.setIconImage(new ImageIcon("img/akkadian.png").getImage());
        GridBagLayout layout=new GridBagLayout();
        this.setLayout(layout);
        String[] names=new String[]{"POSTag","Description",
                "Regex",
                "Equals",
                "Values","TargetScript","Classification","Extrainfo","Color"};
        JTable fscoretable=new JTable(new POSTableModel(names,posTagger));
        fscoretable.setAutoCreateRowSorter(true);
        fscoretable.putClientProperty("terminateEditOnFocusLost", true);
        fscoretable.setDefaultRenderer(String.class, new POSTableRenderer());
        GridBagConstraints c=new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        JScrollPane pane=new JScrollPane(fscoretable);
        pane.setRowHeaderView(new LineNumberTable(fscoretable));
        this.add(pane, c);
        final JButton exportButton=new JButton(bundle.getString("exportPOSRules"));
        final JFileChooser modelfilechooser=new JFileChooser();
        exportButton.addActionListener(actionEvent -> {
            if (actionEvent.getSource() == exportButton) {
                int returnVal = modelfilechooser.showSaveDialog(RuleEditor.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = modelfilechooser.getSelectedFile();
                    try {
                        posTagger.toXML(file.getAbsolutePath());
                    } catch (XMLStreamException | FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //This is where a real application would open the file.
                    System.out.println("Opening: " + file.getName() + ".");
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        this.add(exportButton,c);
        //this.setPreferredSize(new Dimension(1100, 1000));
        this.pack();
        this.show();
    }
}

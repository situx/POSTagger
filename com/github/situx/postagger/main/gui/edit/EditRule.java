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

import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by timo on 3/23/15.
 */
public class EditRule extends JFrame {

    public ResourceBundle bundle=ResourceBundle.getBundle("POSTagger", Locale.getDefault(),new UTF8Bundle("UTF-8"));

    public EditRule(String title){
        this.setTitle(title);
        GridBagLayout gridBagLayout=new GridBagLayout();
        this.setLayout(gridBagLayout);
        JTextField descriptionField=new JTextField("");
        JLabel descriptionLabel=new JLabel(bundle.getString("description"));
        JTextField equalsField=new JTextField("");
        JLabel equalsLabel=new JLabel(bundle.getString("equals"));
        JTextField regexField=new JTextField("");
        JLabel regexLabel=new JLabel(bundle.getString("regexEdit"));
        JComboBox<PersonNumberCases> caseBox=new JComboBox<PersonNumberCases>(PersonNumberCases.values());
        JLabel caseLabel=new JLabel(bundle.getString("editCase"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        this.add(descriptionLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        this.add(descriptionField,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 0;
        this.add(equalsLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 0;
        this.add(equalsField,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        this.add(regexLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        this.add(regexField,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = 1;
        this.add(caseLabel,c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 1;
        this.add(caseBox,c);
        this.pack();
    }


    public static void main(String[] args){
           EditRule edit=new EditRule("Test");
           edit.show();
    }
}

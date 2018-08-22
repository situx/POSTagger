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

package com.github.situx.postagger.main.gui.dictedit;

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.cuneiform.AkkadDictHandler;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.ParserConfigurationException;


public class MainWindow extends JFrame {
	JPanel panelleft=new JPanel();
	JPanel panelright=new JPanel();
	JLabel label=new JLabel("Edit field");
	JTextField transliteration=new JTextField();
	JTextField cuneiform=new JTextField();
	JTextField wordstem=new JTextField();
    JTextField search=new JTextField();

	JComboBox<String> wordtype=new JComboBox<>();
	JTable words=new JTable();
	JMenuBar menubar=new JMenuBar();
    final JFileChooser filechooser=new JFileChooser();
    TableRowSorter sorter;
	
	public MainWindow(){
		super();
		this.setSize(500, 500);
		this.buildGUI();
	}

	private void filterTable(){
        RowFilter<DictTableModel,Object> rf=null;
        try{
            rf=RowFilter.regexFilter(search.getText(),0);
        }catch (PatternSyntaxException e){
            return;
        }
        sorter.setRowFilter(rf);
    }

	private void buildGUI(){
        this.words.setAutoCreateRowSorter(true);
		this.setTitle("Cuneiform Dictionary Editor");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setIconImage(new ImageIcon("img/akkadian.png").getImage());
		this.setLayout(new GridBagLayout());
        this.filechooser.setCurrentDirectory(new File("dict/"));
        this.cuneiform.setSize(200,100);
        GridBagConstraints c=new GridBagConstraints();
        c.fill=GridBagConstraints.WEST;
        c.gridx=0;
        c.gridy=0;
		this.add(panelleft,c);
        c.fill=GridBagConstraints.EAST;
        c.gridx=1;
		this.add(panelright,c);
		this.setJMenuBar(menubar);
		JMenu menu=new JMenu("File");
		menubar.add(menu);
        JMenuItem open=new JMenuItem("Open");
        menu.add(open);
        open.addActionListener(e ->{
            int returnVal = filechooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = filechooser.getSelectedFile();
                AkkadDictHandler handler=new AkkadDictHandler(new LinkedList<String>());
                try {
                    handler.importDictFromXML(file.getAbsolutePath());
                    DictTableModel model=new DictTableModel(handler);
                    this.words.setModel(model);
                    sorter=new TableRowSorter<>(model);
                    ListSelectionModel lmodel=words.getSelectionModel();
                    lmodel.addListSelectionListener(e12 -> {
                        CuneiChar cunei= (CuneiChar) words.getValueAt(words.getSelectedRow(),words.getSelectedColumn());
                        this.cuneiform.setText(cunei.getCharacter());
                        this.transliteration.setText(cunei.getTransliterations().keySet().iterator().next().toString());


                    });
                } catch (ParserConfigurationException | SAXException | IOException e1) {
                    e1.printStackTrace();
                }




            }

        });
        JMenuItem save=new JMenuItem("Save");
        menu.add(save);
		JMenuItem quit=new JMenuItem("Quit");
		menu.add(quit);
		JMenu menu2=new JMenu("Export");
		menubar.add(menu2);
		JMenuItem exportlemon=new JMenuItem("Export as Lemon RDF");
		menu2.add(exportlemon);
		JMenuItem exportxml=new JMenuItem("Export as XML");
		menu2.add(exportxml);
		JMenuItem exportcsv=new JMenuItem("Export as CSV");
		menu2.add(exportcsv);
		JMenu menu3=new JMenu("Help");
		menubar.add(menu3);
		JMenuItem about=new JMenuItem("About...");
        about.addActionListener(actionEvent -> JOptionPane.showMessageDialog(this,"<html>Copyright by Timo Homburg<br>Published under GPLv3</html>"));
		menu3.add(about);
		panelleft.add(label);
		panelleft.add(transliteration);
		panelright.add(new JScrollPane(words));
	}
}

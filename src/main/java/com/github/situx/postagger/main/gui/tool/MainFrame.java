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

import com.github.situx.postagger.main.gui.util.UTF8Bundle;
import com.github.situx.postagger.main.gui.edit.RuleEditor;
import com.github.situx.postagger.main.gui.util.DragTabbedPane;
import com.github.situx.postagger.main.gui.util.TabEnum;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by timo on 4/11/15.
 */
public class MainFrame extends JFrame {

    public static ResourceBundle bundle = ResourceBundle.getBundle("POSTagger", Locale.getDefault(), new UTF8Bundle("UTF-8"));
    protected static DragTabbedPane tabs,tabbedPane;

    public static java.util.Map<TabEnum,java.util.Set<String>> displayedTabs=new TreeMap<>();
    public static java.util.Map<TabEnum,DragTabbedPane> tabCollect=new TreeMap<>();

    public MainFrame(String title){
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setIconImage(new ImageIcon("img/akkadian.png").getImage());
        this.setLayout(new GridBagLayout());
        displayedTabs.put(TabEnum.MAINAREA,new TreeSet<>());
        displayedTabs.put(TabEnum.SIDEBAR,new TreeSet<>());
        tabCollect.put(TabEnum.MAINAREA,new DragTabbedPane(TabEnum.MAINAREA,false));
        tabCollect.put(TabEnum.SIDEBAR,new DragTabbedPane(TabEnum.SIDEBAR,true));
        tabs=tabCollect.get(TabEnum.MAINAREA);

        GridBagConstraints c=new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        this.add(tabs,c);
        tabCollect.get(TabEnum.MAINAREA).insertTab("POSTagger", new POSTagMain(tabbedPane, tabs), TabEnum.MAINAREA);
        tabbedPane=tabCollect.get(TabEnum.SIDEBAR);
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected LayoutManager createLayoutManager() {
                return new TabbedPaneLayout(){
                    @Override
                    protected Dimension calculateSize(final boolean minimum) {
                        return new Dimension(((Double)(0.2*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getWidth())).intValue(),((Double)(0.95*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getHeight())).intValue());
                    }

                };
            }

            @Override
            protected int calculateTabWidth(
                    int tabPlacement, int tabIndex, FontMetrics metrics) {
                return 20; // the width of the tab
            }

        });
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected LayoutManager createLayoutManager() {
                return new TabbedPaneLayout(){
                    @Override
                    protected Dimension calculateSize(final boolean minimum) {
                        return new Dimension(((Double)(0.8*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getWidth())).intValue(),((Double)(0.95*GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getHeight())).intValue());
                    }

                };
            }

            /*@Override
            protected int calculateTabWidth(
                    int tabPlacement, int tabIndex, FontMetrics metrics) {
                return 20; // the width of the tab
            }*/

        });
        final JButton translation=new JButton("Translation");
        translation.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Translation",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("Translation", new TranslationMain(api.resultarea.getText(), api.postagger, api.charType, CharTypes.ENGLISH, TranslationMethod.LEMMA, tabbedPane, tabbedPane), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
        });
        final JButton toTargetLanguage = new JButton("To Cuneiform");
        toTargetLanguage.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("To Cuneiform",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("To Cuneiform",new ToCuneiConvMain(api.resultarea.getText(), api.postagger, api.charType,tabbedPane, tabbedPane), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
        });
        final JButton definitionReload=new JButton(bundle.getString("reloadDefs"));
        definitionReload.addActionListener(actionEvent -> {
            MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
            api.definitionReload(api.charType, tabbedPane);

        });


        final JButton regex=new JButton("Regex Tester");
        regex.addActionListener(actionEvent -> new RegexTester());
        final JButton options=new JButton("Options");
        options.addActionListener(actionEvent -> new RuleEditor(((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent()).postagger,"Options"));
        tabbedPane.setPreferredSize(new Dimension(150, ((Double) (1.1 * GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getBounds2D().getHeight())).intValue()));
        tabbedPane.setTabPlacement(JTabbedPane.RIGHT);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        c=new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth=1;
        c.gridheight=3;
        this.add(tabbedPane, c);
        final JFileChooser trainfilechooser=new JFileChooser();
        trainfilechooser.setCurrentDirectory(new File("test/"));
        JLabel trainingfilelabel=new JLabel(bundle.getString("postagfile"));
        final JTextField trainingfilefield=new JTextField(25);
        trainingfilefield.setEnabled(false);
        final JCheckBox checkbox=new JCheckBox();
        final JLabel cuneiformLabel=new JLabel(bundle.getString("transliteration"));
        final JButton trainfilebutton=new JButton(bundle.getString("choose"));
        final JCheckBox cuneiformCheckbox=new JCheckBox();
        trainfilechooser.setCurrentDirectory(new File("test/"));
        trainfilebutton.addActionListener(actionEvent -> {
            if (actionEvent.getSource() == trainfilebutton) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                api.loadFile(api.charType, trainfilechooser, trainingfilefield, cuneiformCheckbox.isSelected(), checkbox.isSelected());
            }
        });
        cuneiformCheckbox.addActionListener(actionEvent -> ((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent()).resultarea.setCuneiFormFlag(cuneiformCheckbox.isSelected()));
        final JButton clear=new JButton("Clear");
        clear.addActionListener(actionEvent -> ((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent()).clear());
        UIManager.getDefaults().put("TabbedPane.contentBorderInset",new Insets(0,0,0,0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder",true);
        final JLabel atflabel=new JLabel(bundle.getString("loadOriginalatf"));
        final JPanel loadpanel = new JPanel();
        final JPanel loadpanel2 = new JPanel();
        loadpanel.add(trainingfilelabel);
        loadpanel.add(trainingfilefield);
        loadpanel.add(trainfilebutton);
        loadpanel.add(clear);
        loadpanel.add(cuneiformLabel);
        loadpanel.add(cuneiformCheckbox);
        loadpanel2.add(checkbox);
        loadpanel2.add(atflabel);
        loadpanel2.add(definitionReload);
        loadpanel2.add(regex);
        loadpanel2.add(options);
        loadpanel2.add(translation);
        loadpanel2.add(toTargetLanguage);
        /*c=new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        this.add(loadpanel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        this.add(loadpanel2, c);*/
        this.makeMenuBar();
        this.pack();
        this.show();
    }


    public static Boolean checkTab(final String title,final TabEnum tabBar){
        return !displayedTabs.get(tabBar).contains(title);
    }



    private void makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        menu = new JMenu("Locale");
        JMenu help = new JMenu("?");
        JMenuItem about=new JMenuItem("About...");
        about.addActionListener(actionEvent -> JOptionPane.showMessageDialog(MainFrame.this,"<html>Copyright by Timo Homburg<br>Published under GPLv3</html>"));
        help.add(about);

        JMenu tools=new JMenu("Tools");
        JMenuItem postagging=new JMenuItem("POSTagger");
        postagging.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("POSTagger",TabEnum.MAINAREA)) {
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("POSTagger", new POSTagMain(tabbedPane, tabs), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount() - 1);
            }
        });
        tools.add(postagging);
        JMenuItem translation=new JMenuItem(bundle.getString("translation"));
        translation.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Translation",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("Translation", new TranslationMain(api.resultarea.getText(), api.postagger, api.charType, CharTypes.ENGLISH, TranslationMethod.LEMMA, tabbedPane, tabs), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount() - 1);
            }
        });
        tools.add(translation);
        JMenuItem targetLanguage=new JMenuItem("ToCuneiform");
        targetLanguage.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("To Cuneiform",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("To Cuneiform", new ToCuneiConvMain(api.resultarea.getText(), api.postagger, api.charType, tabbedPane, tabs), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }

        });
        tools.add(targetLanguage);
        JMenuItem segmentation=new JMenuItem("Segmentation");
        segmentation.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Segmentation",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("Segmentation", new SegmentationMain(api.resultarea.getText(), api.postagger, api.charType, tabbedPane, tabs), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
        });
        tools.add(segmentation);
        JMenuItem semantics=new JMenuItem("Semantic Extraction");
        semantics.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Semantic",TabEnum.MAINAREA)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.MAINAREA).insertTab("Semantic", new SemanticMain(api.resultarea.getText(), api.postagger, api.charType, tabbedPane, tabs), TabEnum.MAINAREA);
                MainFrame.tabCollect.get(TabEnum.MAINAREA).setSelectedIndex(MainFrame.tabCollect.get(TabEnum.MAINAREA).getTabCount()-1);
            }
        });
        tools.add(semantics);
        JMenuItem dictionary=new JMenuItem("Dictionary");
        dictionary.addActionListener(actionEvent -> {
              if(MainFrame.checkTab("Dict",TabEnum.SIDEBAR)) {
                  MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                  MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab("Dict", new DictMain(api.postagger, api.charType, api.charType.getCorpusHandlerAPI().getUtilDictHandler()), TabEnum.SIDEBAR);
              } });
        tools.add(dictionary);
        tools.add(targetLanguage);
        JMenuItem draw=new JMenuItem("Draw");
        draw.addActionListener(actionEvent -> {
            if(MainFrame.checkTab("Draw",TabEnum.SIDEBAR)) {
                MainAPI api=((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent());
                MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab("Draw", new PaintMain(api.postagger, api.charType, api.charType.getCorpusHandlerAPI().getUtilDictHandler()), TabEnum.SIDEBAR);
            }});
        tools.add(draw);
        menuBar.add(menu);
        menuBar.add(tools);
        menuBar.add(help);
        JMenuItem ibusexportitem = new JMenuItem("Akkadian");
        ibusexportitem.setMnemonic(KeyEvent.VK_A);
        ibusexportitem.addActionListener(event -> {
            /*if (!resultarea.getInputContext().selectInputMethod(GenericInputMethodDescriptor.AKKADIAN)) {
                Toolkit.getDefaultToolkit().beep();
            }*/
        });
        JMenuItem ibusexportitem2 = new JMenuItem("Hittite");
        ibusexportitem.setMnemonic(KeyEvent.VK_H);
        ibusexportitem.addActionListener(event -> {
            /*if (!resultarea.getInputContext().selectInputMethod(GenericInputMethodDescriptor.HITTITE)) {
                Toolkit.getDefaultToolkit().beep();
            } */
        });
        JMenuItem ibusexportitem3 = new JMenuItem("Sumerian");
        ibusexportitem.setMnemonic(KeyEvent.VK_S);
        ibusexportitem.addActionListener(event -> {
            /*if (!resultarea.getInputContext().selectInputMethod(GenericInputMethodDescriptor.SUMERIAN)) {
                Toolkit.getDefaultToolkit().beep();
            }*/
        });
        menu.add(ibusexportitem);
        menu.add(ibusexportitem2);
        menu.add(ibusexportitem3);
        this.setJMenuBar(menuBar);
    }


    protected static int createLegend(final java.util.Map<String,Color> legenddata,Boolean twoTextPanes,JTabbedPane tabbedPane,String legendtitle,Boolean refresh){
        int y=1;
        JPanel legendpanel;
        if(!MainFrame.checkTab(legendtitle,TabEnum.SIDEBAR)){
            return y;
        }
        if(refresh && MainFrame.checkTab(legendtitle,TabEnum.SIDEBAR)){
            legendpanel=new JPanel();
            MainFrame.tabCollect.get(TabEnum.SIDEBAR).insertTab(legendtitle,legendpanel,TabEnum.SIDEBAR);
        }else{
            legendpanel=(JPanel)MainFrame.tabCollect.get(TabEnum.SIDEBAR).getTabComponentAt(MainFrame.tabCollect.get(TabEnum.SIDEBAR).indexOfTab(legendtitle));
            legendpanel.removeAll();
        }
        legendpanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel epochPredict=new JLabel("");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth=1;
        legendpanel.add(epochPredict,c);
        for(String key:legenddata.keySet()){
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = y++;
            c.gridwidth=1;
            final JLabel nounoradj;
            if(!bundle.containsKey(key)){
                nounoradj=new JLabel(key);
            }else{
                nounoradj=new JLabel(bundle.getString(key));
            }
            nounoradj.setName(key);
            nounoradj.setToolTipText("<html>Left Click: Remove/Add color<br>Right Click: Choose color</html>");
            nounoradj.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    ((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent()).changeLegendColors(SwingUtilities.isLeftMouseButton(e), legenddata, nounoradj);
                }

            });
            Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
            // set the border of this component
            nounoradj.setBorder(border);
            nounoradj.setOpaque(true);
            nounoradj.setBackground(legenddata.get(key));
            legendpanel.add(nounoradj,c);
        }
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = y+=2;
        c.gridwidth=1;
        JButton chooseFont;
        if(twoTextPanes){
            chooseFont=new JButton("Choose Font Left");
        }else{
            chooseFont=new JButton("Choose Font");
        }
        chooseFont.addActionListener(e -> ((MainAPI)MainFrame.tabCollect.get(TabEnum.MAINAREA).getSelectedComponent()).chooseFont());
        legendpanel.add(chooseFont,c);
        return y;
    }
}

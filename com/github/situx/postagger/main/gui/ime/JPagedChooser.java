/*
 * Copyright (C) 2005 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.github.situx.postagger.main.gui.ime;

import com.github.situx.postagger.main.gui.tool.POSTagMain;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.main.gui.ime.jquery.InputTerm;
import com.github.situx.postagger.main.gui.ime.util.PagingIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;


/**
 * A Swing component listing numbered options that can be selected from.
 * One option is can be highlighted as selected, and the items can
 * be scrolled through, item by item, with paging.
 * Can be oriented horizontally or vertically as defined in the
 * constructors.
 *
 * Originally built to render below-the-spot options for an input method,
 * but may have other applications.
 *  
 * @param <V> the parameterized type of value contained in the chooser
 * @author Jordan Kiang
 */
public class JPagedChooser<V,S> extends JComponent {

    private Set<Integer> nextNumbers;
    // the options
    private List<Tuple<InputTerm<V>, String>> options;

    private List<String> optionAppend;
    
    // options are rendered in a JList
    private JList optionsList;
    
    private int currentPage = 0;
    private int itemsPerPage;
    private boolean showIndices;
    
    // buttons and icons
    JButton previousPageButton;
    private PagingIcon previousPageEnabledIcon;
    private PagingIcon previousPageDisabledIcon;
    
    JButton nextPageButton;
    private PagingIcon nextPageEnabledIcon;
    private PagingIcon nextPageDisabledIcon;
    
    // registered listeners are invoked when an item is selected
    private Set<SelectionListener<V>> selectionListeners = new LinkedHashSet<SelectionListener<V>>();
    
    /**
     * Build a new JPagedChooser.
     * @param options the options
     * @param horizontal true if should be oriented horizontally, false for vertically
     * @param showIndices true if numbered indices should be shown, false otherwise
     * @param itemsPerPage number of items to show on one page
     */
    public JPagedChooser(List<Tuple<InputTerm<V>, String>> options, boolean horizontal, boolean showIndices, int itemsPerPage) {
        super();
        
        this.options = options;
        this.showIndices = showIndices;
        this.itemsPerPage = itemsPerPage;
        
        this.setLayout(new BorderLayout());
        
        this.initOptionsList(horizontal);
        this.initPagingButtons(horizontal);
        
        String previousButtonLayoutPosition;
        String nextButtonLayoutPosition;
        if(horizontal) {
            previousButtonLayoutPosition = BorderLayout.WEST;
            nextButtonLayoutPosition = BorderLayout.EAST;
        } else {
            previousButtonLayoutPosition = BorderLayout.NORTH;
            nextButtonLayoutPosition = BorderLayout.SOUTH;
        }
        
        this.add(this.previousPageButton, previousButtonLayoutPosition);
        this.add(this.optionsList, BorderLayout.CENTER);
        this.add(this.nextPageButton, nextButtonLayoutPosition);
        
        this.setForeground(Color.BLACK);
        this.setBackground(Color.LIGHT_GRAY);
        try {
            this.setFont(POSTagMain.getFont("cunei.ttf",12));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /////////////////////
    
    /**
     * Initialize options JList instance variable.
     */
    @SuppressWarnings("unchecked")
    private void initOptionsList(boolean horizontal) {
        final JList optionsList = new JList();
        
        // custom cell renderer 
        optionsList.setCellRenderer(new ChooserCellRenderer(this.showIndices));
        
        if(horizontal) {
            optionsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            // all on one row
            optionsList.setVisibleRowCount(1);
        } else {
            optionsList.setLayoutOrientation(JList.VERTICAL);
        }
        
        // can only select one item at a time.
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
        // capture mouse clicks and callback any registered
        // listeners with the selected option.
        optionsList.addMouseListener(new MouseAdapter() {
        	/**
        	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
        	 */
        	@Override
            public void mouseClicked(MouseEvent e) {
                int clickedIndex = optionsList.locationToIndex(e.getPoint());
                
                if(clickedIndex > -1 && optionsList.isSelectedIndex(clickedIndex)) {
                    // Only fire the event if the clicked element was left in a selected
                    // state after the click event.  It is assumed that the normal mouse
                    // selection behavior already took place via an earlier registered
                    // MouseListener.
                
                	InputTerm<V> value = ((Tuple<InputTerm<V>,String>)optionsList.getSelectedValue()).getOne();
                    JPagedChooser.this.notifyListeners(value);
                }
            }
        });
        
        this.optionsList = optionsList;
        
        this.updateListCellRendererSize();
        this.loadCandidates(0, Math.min(this.itemsPerPage, this.options.size()));
    }
    
    /////////////////////
    
    private void initPagingButtons(boolean horizontal) {
        
        ActionListener listener = e -> {
            Object source = e.getSource();

            if(JPagedChooser.this.previousPageButton == source) {
                // previous button hit, move to the previous page
                JPagedChooser.this.previousPage();
            } else if(JPagedChooser.this.nextPageButton == source) {
                // next button hit, move to the next page
                JPagedChooser.this.nextPage();
            }
        };
        
        int previousPageDirection;
        int nextPageDirection;
        
        if(horizontal) {
            previousPageDirection = SwingConstants.LEFT;
            nextPageDirection = SwingConstants.RIGHT;
        } else {
            previousPageDirection = SwingConstants.TOP;
            nextPageDirection = SwingConstants.BOTTOM;
        }
        
        // initialize buttons and icons
        
        this.previousPageButton = this.buildPagingButton(previousPageDirection);
        this.previousPageButton.addActionListener(listener);
        this.previousPageEnabledIcon = (PagingIcon)this.previousPageButton.getIcon();
        this.previousPageDisabledIcon = (PagingIcon)this.previousPageButton.getDisabledIcon();
        this.previousPageButton.setEnabled(false);
        
        this.nextPageButton = this.buildPagingButton(nextPageDirection);
        this.nextPageButton.addActionListener(listener);
        this.nextPageEnabledIcon = (PagingIcon)this.nextPageButton.getIcon();
        this.nextPageDisabledIcon = (PagingIcon)this.nextPageButton.getDisabledIcon();
        this.nextPageButton.setEnabled(this.hasNextPage());
    }
    
    private JButton buildPagingButton(int direction) {
        // ICON dimension slightly smaller than the
    	// button dimension so there is some padding
    	final int ICON_DIMENSION = 12;
        final int BUTTON_DIMENSION = 16;
        
        // apply the colors of the chooser itself to the icons
        Color bg = this.getBackground();
        Color fg = this.getForeground();
        
        JButton pagingButton = new JButton();
        pagingButton.setPreferredSize(new Dimension(BUTTON_DIMENSION, BUTTON_DIMENSION));
        pagingButton.setBorderPainted(false);
        pagingButton.setBackground(bg);
        
        PagingIcon enabledIcon = new PagingIcon(direction, ICON_DIMENSION, ICON_DIMENSION);
        enabledIcon.setForeground(fg);
        
        // use gray as the foreground of a disabled button, hopefully that doesn't
        // conflict with the foreground settings of the chooser
        PagingIcon disabledIcon = new PagingIcon(direction, ICON_DIMENSION, ICON_DIMENSION);
        disabledIcon.setForeground(Color.GRAY);	// disabled arrow is gray
     
        pagingButton.setIcon(enabledIcon);
        pagingButton.setDisabledIcon(disabledIcon);
        
        return pagingButton;
    }
    
    //////////////////
    
    /**
     * JList cells have the same dimensions.
     * When we update the contents we need to recalculate
     * the dimensions of the cells to fit the biggest.
     */
    private void updateListCellRendererSize() {
    	final int BUFFER = 30;
        this.nextNumbers=new TreeSet<>();
        FontMetrics metrics = this.optionsList.getFontMetrics(this.optionsList.getFont());
        
        int maxWidth = 0;
        Iterator<Tuple<InputTerm<V>,String>> optionIter = this.options.iterator();
        for(int i = 0; optionIter.hasNext(); i++) {
        	Tuple<InputTerm<V>,String> optionTuple = optionIter.next();
            InputTerm<V> option=optionTuple.getOne();
        	
            String testString = option.toString();
            testString = this.showIndices ? toIndexedString(testString,"", i + 1) : testString;
            
            int candidateWidth = metrics.stringWidth(testString+optionTuple.getTwo().length()+(i+"").length()+2);
            maxWidth = Math.max(maxWidth, candidateWidth);
        }
        
        this.optionsList.setFixedCellWidth(maxWidth + BUFFER);
        this.optionsList.setFixedCellHeight(((Double)(1.45*metrics.getHeight())).intValue());
    }
    
    private String toIndexedString(String value,String append, int indexOnPage) {
    	int pageStart = this.currentPage * this.itemsPerPage;
        return (pageStart + indexOnPage) + ". " + value+append;
    }
    
    //////////////////
    
    private boolean hasNextPage() {
        return this.options.size() > (this.currentPage + 1) * this.itemsPerPage;
    }
    
    /**
     * Advance the pager to the next page, if there is a next page.
     * @return true if the page was advanced, false if there wasn't a next page
     */
    public boolean nextPage() {
        if(this.hasNextPage()) {
        	// next page exists, update the state
        	// of the chooser to reflect then next page.
        	
            this.currentPage++;
            
            int beginIndex = this.currentPage * this.itemsPerPage;
            this.loadCandidates(beginIndex, Math.min(this.itemsPerPage, this.options.size() - beginIndex));
            
            // when moving to the next page, the first item of the item page is selected
            this.optionsList.setSelectedIndex(0);
            
            this.previousPageButton.setEnabled(true);
            this.nextPageButton.setEnabled(this.hasNextPage());
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Advance the selection cursor to the next item.
     * If on the last item of a page, the page
     * will be advanced and the first item of the
     * next page is selected.
     * 
     * @return true if advanced to the next page, false if there was no next item
     */
    public boolean nextItem() {
        int selectedIndex = this.optionsList.getSelectedIndex();
        if(selectedIndex + 1 < this.optionsList.getModel().getSize()) {
        	// can advance to the next item on the current page
        	
            this.optionsList.setSelectedIndex(selectedIndex + 1);
            
            return true;
            
        } else if(this.nextPage()) {
        	// no more items on the current page, but was able
        	// to advance to the next page so we select the
        	// first item on that page.
        	
            this.optionsList.setSelectedIndex(0);
            
            return true;
        }
        
        // no more items
        return false;
    }
    
    /**
     * @return true if not already on the first page
     */
    private boolean hasPreviousPage() {
        return this.currentPage > 0;
    }
    
    /**
     * Move back to the previous page.
     * @return true if able to move to the previous page, false if already on it
     */
    public boolean previousPage() {
        if(this.hasPreviousPage()) {
        	// preivous page exists, update the state
        	// of the chooser to reflect then previous page.
        	this.currentPage--;
            
            this.loadCandidates(this.currentPage * this.itemsPerPage, this.itemsPerPage);
            
            // when moving to the previous page, the last item on the page becomes the selected.
            this.optionsList.setSelectedIndex(this.optionsList.getModel().getSize() - 1);
            
            this.previousPageButton.setEnabled(this.hasPreviousPage());
            this.nextPageButton.setEnabled(true);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Move the selection cursor back to the prior item,
     * if not already on the first item.
     * @return if moved back to the previous item, false if already on the first
     */
    public boolean previousItem() {
        int selectedIndex = this.optionsList.getSelectedIndex();
        if(selectedIndex - 1 >= 0 && this.optionsList.getModel().getSize() > 0) {
            this.optionsList.setSelectedIndex(selectedIndex - 1);
        
            return true;
        
        } else if(this.previousPage()) {
            this.optionsList.setSelectedIndex(this.optionsList.getModel().getSize() - 1);
            
            return true;
        }
        
        return false;
    }

    public Set<Integer> getNextNumbers() {
        return nextNumbers;
    }

    /**
     * Load the specified contiguous block of candidates
     * from the options in to the chooser.  Use this to load
     * a new page.
     * 
     * @param beginIndex the first index
     * @param count the number of candidates starting with the beginIndex
     */
    private void loadCandidates(int beginIndex, int count) {
        // copy the specified items to the JList
    	
    	Object[] loadedCandidates = new Object[count];
        Iterator<Tuple<InputTerm<V>,String>> optionIter = this.options.iterator();
        for(int i = 0; i < beginIndex && optionIter.hasNext(); i++) {
        	// skip past the options before the desired page
        	optionIter.next();
        }
        for(int i = 0; i < count && optionIter.hasNext(); i++) {
        	// now on the correct page of options, load these
        	loadedCandidates[i] = optionIter.next();
        }

        
        this.optionsList.setListData(loadedCandidates);
    }
    
    //////////////////
        
    /**
     * @return the currently selected value
     */
    @SuppressWarnings("unchecked")
    public InputTerm<V> getSelectedValue() {
        return ((Tuple<InputTerm<V>,String>)this.optionsList.getSelectedValue()).getOne();
    }
    
    /**
     * Get the value at the given index, indexed from 0.
     * Note that if you are showing indices, the first item
     * is displayed as item 1, but is stored at 0...
     * 
     * @param index the item index
     * @return the value for the index
     */
    @SuppressWarnings("unchecked")
    public InputTerm<V> getValue(int index) {
    	if(index < 1) {
    		throw new IndexOutOfBoundsException("items are indexed from 1");
    	}
    	
    	// indexed from 1 in the chooser, but from 0 in the model
        return ((Tuple<InputTerm<V>,String>)this.optionsList.getModel().getElementAt(index - 1)).getOne();
    }
    
    /**
     * @return the page size of the chooser
     */
    public int getPageSize() {
        return this.optionsList.getModel().getSize();
    }
     
    //////////////////
    
    /**
     * Set the foreground color of the chooser
     * @param fg the foreground color
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        
        this.optionsList.setForeground(fg);
        
        this.previousPageButton.setForeground(fg);
        this.previousPageEnabledIcon.setForeground(fg);
        
        this.nextPageButton.setForeground(fg);
        this.nextPageEnabledIcon.setForeground(fg);
        
        // doesn't change disabled icon foreground colors as they remain gray.
    }
    
    /**
     * Set the background color of the chooser (text and paging icons).
     * @param bg the background color
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        
        this.optionsList.setBackground(bg);
        
        this.previousPageButton.setBackground(bg);
        this.previousPageEnabledIcon.setBackground(bg);
        this.previousPageDisabledIcon.setBackground(bg);
        
        this.nextPageButton.setBackground(bg);
        this.nextPageEnabledIcon.setBackground(bg);
        this.nextPageDisabledIcon.setBackground(bg);
    }
   
    /**
     * @see javax.swing.JComponent#setFont(java.awt.Font)
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
       if(null != this.optionsList) {
    	   // additionally set the font on the options list
            this.optionsList.setFont(font);
            // since the font metrics have changed, update
            // the dimensions of the cells
            this.updateListCellRendererSize();
       }
    }
    
    //////////////////
    
    /**
     * Add a listener that gets invoked whenever a new item is selected.
     * @param listener
     */
	public void addSelectionListener(SelectionListener<V> listener) {
	    synchronized(this.selectionListeners) {
	        this.selectionListeners.add(listener);
	    }
	}
	
	/**
	 * Remove a SelectionListener.
	 * @param listener
	 */
	public void removeSelectionListener(SelectionListener<String> listener) {
	    synchronized(this.selectionListeners) {
	        this.selectionListeners.remove(listener);
	    }
	}
	
	/**
	 * Notify all the registered listeners of the given selection.
	 * @param selection
	 */
	void notifyListeners(InputTerm<V> selection) {
	    synchronized(this.selectionListeners) {
		    for(SelectionListener<V> listener : this.selectionListeners) {
		        listener.handleSelection(selection);
		    }
	    }
	}
    
    //////////////////
    
	/**
	 * A ListCellRenderer to use to render the individual cells of the chooser.
	 */
    private class ChooserCellRenderer extends JPanel implements ListCellRenderer {
        private boolean showIndices;
    	
    	ChooserCellRenderer(boolean showIndices) {
            this.setOpaque(true);
            this.showIndices = showIndices;
        }
        
        public Component getListCellRendererComponent(JList list,
                									  Object value,
                									  int index,
                									  boolean isSelected,
                									  boolean cellHasFocus) {
            

            JPanel panel=new JPanel();
            this.setFont(list.getFont());
            list.setFixedCellHeight(20);
            
            panel.setComponentOrientation(list.getComponentOrientation());
            //this.setHorizontalAlignment(SwingConstants.CENTER);
            
        	if (isSelected) {
        	    panel.setBackground(list.getSelectionBackground());
        	    panel.setForeground(list.getSelectionForeground());
        	} else {
        	    panel.setBackground(list.getBackground());
        	    panel.setForeground(list.getForeground());
        	}

        	String text = ((Tuple<InputTerm<V>,String>)JPagedChooser.this.options.get(index)).getOne().toString();//.toString().contains(" ")?value.toString().substring(0,value.toString().indexOf(" ")):value.toString();
            String append=((Tuple<InputTerm<V>,String>)JPagedChooser.this.options.get(index)).getTwo();//text.contains(" ")?value.toString().substring(value.toString().indexOf(" ")+1):"";
        	if(append.startsWith("[0-9]")){
                JPagedChooser.this.nextNumbers.add(Integer.valueOf(append.charAt(0)+""));
            }
        	//text = this.showIndices ? toIndexedString(text,"", index + 1) : text;
            final JLabel idxLabel=new JLabel((index+1)+".");
            final JLabel textLabel=new JLabel(text);
            textLabel.setFont(list.getFont());
            final JLabel addLabel=new JLabel("<html><font color=\"blue\">"+append+"</font></html>");
            idxLabel.setSize(new Dimension(idxLabel.getWidth(),14));
            addLabel.setSize(new Dimension(addLabel.getWidth(),14));
            textLabel.setSize(new Dimension(addLabel.getWidth(),14));
            textLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            addLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            idxLabel.setAlignmentY(Component.TOP_ALIGNMENT);
            panel.add(idxLabel);
        	panel.add(textLabel);
            panel.add(addLabel);
            panel.setAlignmentY(Component.TOP_ALIGNMENT);
            panel.setSize(panel.getWidth(),30);
            //this.setText(text);
        	
        	return panel;
        }
    }
    
    //////////////////
    
    /**
     * An interface for a listener that is invoked
     * when an item is selected from the chooser.
     * Register these through addSelectionListener.
     * 
     * @param <V> the parameterized value type
     */
    static public interface SelectionListener<V> extends EventListener {
    	/**
    	 * @param selection
    	 */
    	public void handleSelection(InputTerm<V> selection);
    }
    
    //////////////////

    /**
     * main method for testing
     * @param args
     */
    static public void main(String[] args) {
        JWindow window = new JWindow();
        //JFrame window = new JFrame();
        
        String[] test = {"\u4e00", "\u634a\u84ab", "\u51bc\u5ac5", "\u98ac\u54ac\u68af\u444a", "\u7843\u548a\u545c", "\u43ac\u5444\u4444", "\u43ac"};
        //String[] test = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        //String[] test = {"123123", "2", "332", "41", "5", "6321", "7", "8", "932131232321", "10", "11232", "122", "13111"};
        List<Tuple<String,String>> options = new ArrayList<Tuple<String,String>>(test.length);
        /*for(String option : test) {
        	options.add(new Tuple<InputTerm<String>,String>(new InputTerm<String>(new LinkedList<InputTermUnit<String>>()),""));
        }
        
        JPagedChooser<String,String> chooser = new JPagedChooser<String,String>(options, true, true, 10);
        window.getContentPane().add(chooser);
        //window.addKeyListener(chooser);
        
        chooser.setFont(new Font("SimSun", Font.PLAIN, 16));
        
        window.pack();
        window.setVisible(true);*/
    }
    
}

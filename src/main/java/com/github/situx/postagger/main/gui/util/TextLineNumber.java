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
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 *  This class will display line numbers for a related text component. The text
 *  component must use the same line height for each line. TextLineNumber
 *  supports wrapped lines and will highlight the line number of the current
 *  line in the text component.
 *
 *  This class was designed to be used as a component added to the row header
 *  of a JScrollPane.
 */
public class TextLineNumber extends JPanel
        implements DocumentListener, PropertyChangeListener,MouseListener
{
    public final static float LEFT = 0.0f;
    public final static float CENTER = 0.5f;
    public final static float RIGHT = 1.0f;

    private Color currentbgcolor=Color.black;

    private Map<Integer,String> colorswitches;

    public POSTagger getPostagger() {
        return postagger;
    }

    public void setPostagger(final POSTagger postagger) {
        this.postagger = postagger;
    }

    private POSTagger postagger;

    private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

    private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

    //  Text component this TextTextLineNumber component is in sync with

    private JToolTipArea component;

    //  Properties that can be changed

    private boolean updateFont,justChanged=true;
    private int borderGap;
    private Color currentLineForeground;
    private float digitAlignment;
    private int minimumDisplayDigits;

    //  Keep history information to reduce the number of times the component
    //  needs to be repainted

    private int lastDigits;
    private int lastHeight;
    private int lastLine;

    private HashMap<String, FontMetrics> fonts;

    /**
     *	Create a line number component for a text component. This minimum
     *  display width will be based on 3 digits.
     *
     *  @param component  the related text component
     */
    public TextLineNumber(JToolTipArea component,Map<Integer,String> switches)
    {
        this(component, 3,switches);
    }

    /**
     *	Create a line number component for a text component.
     *
     *  @param component  the related text component
     *  @param minimumDisplayDigits  the number of digits used to calculate
     *                               the minimum width of the component
     */
    public TextLineNumber(JToolTipArea component, int minimumDisplayDigits,Map<Integer,String> colorswitches)
    {
        this.component = component;
        this.colorswitches=colorswitches;
        setFont( component.getFont() );
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        setBorderGap( 5 );
        setCurrentLineForeground( Color.RED );
        setDigitAlignment( RIGHT );
        setMinimumDisplayDigits( minimumDisplayDigits );

        component.getDocument().addDocumentListener(this);
        component.addPropertyChangeListener("font", this);
        this.addMouseListener(this);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int linenumber=this.getLineNumberForToolTip(event.getY());
        if(colorswitches.containsKey(linenumber)){
            return POSDefinition.splitString("<html>"+this.colorswitches.get(linenumber)+"</html>"," <br> ",75);
        }
        return null;
    }

    /**
     *  Gets the update font property
     *
     *  @return the update font property
     */
    public boolean getUpdateFont()
    {
        return updateFont;
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        int rowStartOffset = component.viewToModel( new Point(0, mouseEvent.getY()) );
        int lineNumber=this.getLineNumber(rowStartOffset);
        TreeForTreeLayout<POSInBox> tree;
        if(SwingUtilities.isLeftMouseButton(mouseEvent)){
            tree = this.postagger.posDependencyTreeBuilder(lineNumber);
        }else if(SwingUtilities.isRightMouseButton(mouseEvent)){
            tree = this.postagger.buildConstituencyTree(lineNumber);
        }else {
            return;
        }
        // setup the tree layout configuration
        double gapBetweenLevels = 50;
        double gapBetweenNodes = 10;
        DefaultConfiguration<POSInBox> configuration = new DefaultConfiguration<>(
                gapBetweenLevels, gapBetweenNodes);

        // create the NodeExtentProvider for TextInBox nodes
        TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

        // create the layout
        TreeLayout<POSInBox> treeLayout = new TreeLayout<POSInBox>(tree,
                nodeExtentProvider, configuration);

        // Create a panel that draws the nodes and edges and show the panel
        TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);
        JFrame dialog = new JFrame();
        dialog.setTitle("Dependency Tree");
        Container contentPane = dialog.getContentPane();
        ((JComponent) contentPane).setBorder(BorderFactory.createEmptyBorder(
                10, 10, 10, 10));
        contentPane.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {

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

    /**
     *  Set the update font property. Indicates whether this Font should be
     *  updated automatically when the Font of the related text component
     *  is changed.
     *
     *  @param updateFont  when true update the Font and repaint the line
     *                     numbers, otherwise just repaint the line numbers.
     */
    public void setUpdateFont(boolean updateFont)
    {
        this.updateFont = updateFont;
    }

    /**
     *  Gets the border gap
     *
     *  @return the border gap in pixels
     */
    public int getBorderGap()
    {
        return borderGap;
    }

    /**
     *  The border gap is used in calculating the left and right insets of the
     *  border. Default value is 5.
     *
     *  @param borderGap  the gap in pixels
     */
    public void setBorderGap(int borderGap)
    {
        this.borderGap = borderGap;
        Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
        setBorder( new CompoundBorder(OUTER, inner) );
        lastDigits = 0;
        setPreferredWidth();
    }

    /**
     *  Gets the current line rendering Color
     *
     *  @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground()
    {
        return currentLineForeground == null ? getForeground() : currentLineForeground;
    }

    /**
     *  Gets the current line rendering Color
     *
     *  @return the Color used to render the current line number
     */
    public Color getCurrentLineForeground2(int pos)
    {
        System.out.println("Position: "+pos+" "+this.colorswitches.get(pos)+" "+this.colorswitches.get(pos-1));
        if(this.colorswitches.containsKey(pos) && !this.colorswitches.get(pos-1).equals(this.colorswitches.get(pos))){
                currentbgcolor=Color.red;

        }else{
            currentbgcolor=getForeground();
        }
        return currentbgcolor;
    }

    /**
     *  The Color used to render the current line digits. Default is Coolor.RED.
     *
     *  @param currentLineForeground  the Color used to render the current line
     */
    public void setCurrentLineForeground(Color currentLineForeground)
    {
        this.currentLineForeground = currentLineForeground;
    }

    /**
     *  Gets the digit alignment
     *
     *  @return the alignment of the painted digits
     */
    public float getDigitAlignment()
    {
        return digitAlignment;
    }

    /**
     *  Specify the horizontal alignment of the digits within the component.
     *  Common values would be:
     *  <ul>
     *  <li>TextLineNumber.LEFT
     *  <li>TextLineNumber.CENTER
     *  <li>TextLineNumber.RIGHT (default)
     *	</ul>
     */
    public void setDigitAlignment(float digitAlignment)
    {
        this.digitAlignment =
                digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
    }

    /**
     *  Gets the minimum display digits
     *
     *  @return the minimum display digits
     */
    public int getMinimumDisplayDigits()
    {
        return minimumDisplayDigits;
    }

    /**
     *  Specify the mimimum number of digits used to calculate the preferred
     *  width of the component. Default is 3.
     *
     *  @param minimumDisplayDigits  the number digits used in the preferred
     *                               width calculation
     */
    public void setMinimumDisplayDigits(int minimumDisplayDigits)
    {
        this.minimumDisplayDigits = minimumDisplayDigits;
        setPreferredWidth();
    }

    /**
     *  Calculate the width needed to display the maximum line number
     */
    private void setPreferredWidth()
    {
        Element root = component.getDocument().getDefaultRootElement();
        int lines = root.getElementCount();
        int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

        //  Update sizes when number of digits in the line number changes

        if (lastDigits != digits)
        {
            lastDigits = digits;
            FontMetrics fontMetrics = getFontMetrics( getFont() );
            int width = fontMetrics.charWidth( '0' ) * digits;
            Insets insets = getInsets();
            int preferredWidth = insets.left + insets.right + width;

            Dimension d = getPreferredSize();
            d.setSize(preferredWidth, HEIGHT);
            setPreferredSize( d );
            setSize( d );
        }
    }

    public Map<Integer, String> getColorswitches() {
        return colorswitches;
    }

    public void setColorswitches(final Map<Integer, String> colorswitches) {
        this.colorswitches = colorswitches;
    }

    /**
     *  Draw the line numbers
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        //	Determine the width of the space available to draw the line number

        FontMetrics fontMetrics = component.getFontMetrics( component.getFont() );
        Insets insets = getInsets();
        int availableWidth = getSize().width - insets.left - insets.right;

        //  Determine the rows to draw within the clipped bounds.

        Rectangle clip = g.getClipBounds();
        int rowStartOffset = component.viewToModel( new Point(0, clip.y) );
        int endOffset = component.viewToModel( new Point(0, clip.y + clip.height) );

        while (rowStartOffset <= endOffset)
        {
            try
            {

                //System.out.println("Repaintaaa");
                //  Get the line number as a string and then determine the
                //  "X" and "Y" offsets for drawing the string.

                String lineNumber = getTextLineNumber(rowStartOffset);
                int stringWidth = fontMetrics.stringWidth( lineNumber );
                int realLineNumber=this.getLineNumber(rowStartOffset);
                int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                int y = getOffsetY(rowStartOffset, fontMetrics);
                /*FontMetrics fm = g.getFontMetrics();
                Rectangle2D rect = fm.getStringBounds(lineNumber, g);*/
               /* if(!Integer.valueOf(0).equals(realLineNumber) && this.colorswitches.containsKey(realLineNumber) && !this.colorswitches.get(realLineNumber-1).equals(this.colorswitches.get(realLineNumber))){
                    if(currentbgcolor.equals(Color.black)){
                        currentbgcolor=Color.red;
                    }else{
                        currentbgcolor=Color.black;
                    }
                }
                g.setColor(this.currentbgcolor);*/
                /*g.fillRect(0,
                        y - fm.getAscent(),
                        getSize().width,
                        (int) rect.getHeight());*/
                if (isCurrentLine(rowStartOffset))
                    g.setColor( getCurrentLineForeground() );
                else
                    g.setColor( getCurrentLineForeground2(realLineNumber) );
                g.drawString(lineNumber, x, y);

                //  Move to the next row

                rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
            }
            catch(Exception e) {break;}
        }
    }

    /*
     *  We need to know if the caret is currently positioned on the line we
     *  are about to paint so the line number can be highlighted.
     */
    private boolean isCurrentLine(int rowStartOffset)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition);
    }

    /*
     *	Get the line number to be drawn. The empty string will be returned
     *  when a line of text has wrapped.
     */
    protected String getTextLineNumber(int rowStartOffset)
    {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex( rowStartOffset );
        Element line = root.getElement( index );

        if (line.getStartOffset() == rowStartOffset)
            return String.valueOf(index + 1);
        else
            return "";
    }

    /*
 *	Get the line number to be drawn. The empty string will be returned
 *  when a line of text has wrapped.
 */
    protected Integer getLineNumber(int rowStartOffset)
    {
        Element root = component.getDocument().getDefaultRootElement();
        int index = root.getElementIndex( rowStartOffset );
        Element line = root.getElement( index );

        if (line.getStartOffset() == rowStartOffset)
            return index + 1;
        else
            return 0;
    }

    protected int getLineNumberForToolTip(int pos){
        int posLine,y=0;
        /*try
        {*/
            /*Rectangle caretCoords = component.modelToView(pos);
            y = (int) caretCoords.getY();*/
            int lineHeight = component.getFontMetrics(component.getFont()).getHeight();
            posLine = (pos/ lineHeight) + 1;
            return posLine;
        /*}
        catch (BadLocationException ex)
        {
        } */
        //return 0;

    }

    /*
     *  Determine the X offset to properly align the line number when drawn
     */
    private int getOffsetX(int availableWidth, int stringWidth)
    {
        return (int)((availableWidth - stringWidth) * digitAlignment);
    }


    /*
     *  Determine the Y offset for the current row
     */
    private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
            throws BadLocationException
    {
        //  Get the bounding rectangle of the row

        Rectangle r = component.modelToView( rowStartOffset );
        int lineHeight = fontMetrics.getHeight();
        int y = r.y + r.height;
        int descent = 0;

        //  The text needs to be positioned above the bottom of the bounding
        //  rectangle based on the descent of the font(s) contained on the row.

        if (r.height == lineHeight)  // default font is being used
        {
            descent = fontMetrics.getDescent();
        }
        else  // We need to check all the attributes for font changes
        {
            if (fonts == null)
                fonts = new HashMap<>();

            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex( rowStartOffset );
            Element line = root.getElement( index );

            for (int i = 0; i < line.getElementCount(); i++)
            {
                Element child = line.getElement(i);
                AttributeSet as = child.getAttributes();
                String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
                Integer fontSize = (Integer)as.getAttribute(StyleConstants.FontSize);
                String key = fontFamily + fontSize;

                FontMetrics fm = fonts.get( key );

                if (fm == null)
                {
                    Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                    fm = component.getFontMetrics( font );
                    fonts.put(key, fm);
                }

                descent = Math.max(descent, fm.getDescent());
            }
        }

        return y - descent;
    }

    //
//  Implement DocumentListener interface
//
    @Override
    public void changedUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        documentChanged();
    }

    /*
     *  A document change may affect the number of displayed lines of text.
     *  Therefore the lines numbers will also change.
     */
    public void documentChanged()
    {
        //  View of the component has not been updated at the time
        //  the DocumentEvent is fired

        SwingUtilities.invokeLater(() -> {
            try
            {
                int endPos = component.getDocument().getLength();
                Rectangle rect = component.modelToView(endPos);

                if (rect != null && rect.y != lastHeight)
                {
                    setPreferredWidth();
                    repaint();
                    lastHeight = rect.y;
                }
            }
            catch (BadLocationException ex) { /* nothing to do */ }
        });
    }

    //
//  Implement PropertyChangeListener interface
//
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getNewValue() instanceof Font)
        {
            if (updateFont)
            {
                Font newFont = (Font) evt.getNewValue();
                setFont(newFont);
                lastDigits = 0;
                setPreferredWidth();
            }
            else
            {
                repaint();
            }
        }
    }
}
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

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

/**
 * Created by timo on 6/12/15.
 */
public class RepaintHighlighter extends DefaultHighlighter {

    // Convenience method to add a highlight with
    // the default painter.
    public Highlighter.Highlight addHighlight(int p0, int p1,Color fillColor,Color bordercolor) throws BadLocationException {
        return (Highlighter.Highlight) addHighlight(p0, p1, new RectanglePainter(bordercolor,fillColor));
    }

    // Convenience method to add a highlight with
    // the default painter.
    public Highlighter.Highlight addHighlight(int p0, int p1,Color fillColor) throws BadLocationException {
        return (Highlighter.Highlight) addHighlight(p0, p1, new RectanglePainter(null,fillColor));
    }


}

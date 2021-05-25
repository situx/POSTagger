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

import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.dict.pos.util.POSDefinition;

import javax.swing.text.Highlighter;
import java.util.List;

/**
 * Created by timo on 15.10.14.
 */
public class HighlightData implements Highlighter.Highlight{

    private Integer start;

    private Integer end;

    private RectanglePainter painter;

    protected POSDefinition posTag;

    public Boolean getManydecs() {
        return manydecs;
    }

    @Override
    public int getStartOffset() {
        return start;
    }

    @Override
    public int getEndOffset() {
        return end;
    }

    @Override
    public Highlighter.HighlightPainter getPainter() {
        return this.painter;
    }

    public void setManydecs(final Boolean manydecs) {
        this.manydecs = manydecs;
    }

    public List<Tuple<String,POSDefinition>> word;

    public String origword;

    private Boolean manydecs;

    public HighlightData(final Integer start, final Integer end, final String colorrgb,final POSDefinition postag,final List<Tuple<String,POSDefinition>> word,final String origword,final Boolean manydecs,RectanglePainter painter) {
        this.start = start;
        this.end = end;
        this.tag = colorrgb;
        this.posTag=postag;
        this.word=word;
        this.origword=origword;
        this.manydecs=manydecs;
    }

    private String tag;

    public Integer getStart() {
        return start;
    }

    public void setStart(final Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(final Integer end) {
        this.end = end;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append(start);
        builder.append(" - ");
        builder.append(end);
        builder.append(" - ");
        builder.append(tag);
        return builder.toString();
    }
}

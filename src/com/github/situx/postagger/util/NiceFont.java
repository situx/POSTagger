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

package com.github.situx.postagger.util;

import java.awt.*;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * Created by timo on 4/10/15.
 */
public class NiceFont extends Font {
    public NiceFont(final String name, final int style, final int size) {
        super(name, style, size);
    }

    public NiceFont(final Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        super(attributes);
    }

    protected NiceFont(final Font font) {
        super(font);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}

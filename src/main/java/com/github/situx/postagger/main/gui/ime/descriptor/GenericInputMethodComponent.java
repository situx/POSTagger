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

package com.github.situx.postagger.main.gui.ime.descriptor;

import com.github.situx.postagger.main.gui.ime.jquery.AkkInputMethodContext;

import java.awt.*;

/**
 * Created by timo on 11/3/14.
 */
public interface GenericInputMethodComponent  {

        /**
         * @return
         * @see Component#getInputContext()
         */
        public AkkInputMethodContext getInputContext();

        /**
         * Comes for free with any Component.
         * Useful to have accsssible on the interface.
         * @return the Font
         * @see Component#getFont
         */
        public Font getFont();
    }
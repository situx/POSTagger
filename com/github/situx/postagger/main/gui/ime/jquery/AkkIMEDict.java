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

package com.github.situx.postagger.main.gui.ime.jquery;

import java.util.List;

/**
 * Created by timo on 10/31/14.
 */
public interface AkkIMEDict  {

        /**
         * Find all the words in the dictionary whose Pinyin matches
         * the given input.  For input units with no specified tone,
         * the tone should be considered a wildcard and matches
         * for all tones should be returned.
         *
         * Returned entries will have at least as many characters as
         * units in the specified input.  If anticipating, then
         * longer words whose first characters match the input
         * can additionally be returned.
         *
         * Returned entries should be in order relative to their
         * frequency, with the most frequent words at the head
         * of the List.
         *
         * @param input the units of pinyin input
         * @param anticipate true if words that begin with the input should also be returned
         * @return words matched by the input
         */
        public List<AkkIMEEntry> lookup(List<AkkUnit> input, boolean anticipate);
}

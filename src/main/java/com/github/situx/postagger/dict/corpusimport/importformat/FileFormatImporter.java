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

package com.github.situx.postagger.dict.corpusimport.importformat;

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.dicthandler.DictHandling;

import java.io.IOException;

/**
 * Created by timo on 08.09.14.
 * Interface for importing from several file formats.
 */
public interface FileFormatImporter {
    /**
     * Imports from the format given in the class.
     * @param charType the chartype to produce
     * @param dictHandler the dicthandler to use
     * @throws IOException on error
     */
    public void importFromFormat(CharTypes charType, DictHandling dictHandler) throws IOException;
}

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

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * Created by timo on 10/23/14.
 */
public class ClipboardHandler implements ClipboardOwner {

        /**
         * Empty implementation of the ClipboardOwner interface.
         */
        @Override public void lostOwnership(Clipboard aClipboard, Transferable aContents){
            //do nothing
        }

        /**
         * Place a String on the clipboard, and make this class the
         * owner of the Clipboard's contents.
         */
        public void setClipboardContents(String aString){
            StringSelection stringSelection = new StringSelection(aString);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, this);
        }

        /**
         * Get the String residing on the clipboard.
         *
         * @return any text found on the Clipboard; if none found, return an
         * empty String.
         */
        public String getClipboardContents() {
            String result = "";
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            //odd: the Object param of getContents is not currently used
            Transferable contents = clipboard.getContents(null);
            boolean hasTransferableText =
                    (contents != null) &&
                            contents.isDataFlavorSupported(DataFlavor.stringFlavor)
                    ;
            if (hasTransferableText) {
                try {
                    result = (String)contents.getTransferData(DataFlavor.stringFlavor);
                }
                catch (UnsupportedFlavorException | IOException ex){
                    System.out.println(ex);
                    ex.printStackTrace();
                }
            }
            return result;
        }
    }

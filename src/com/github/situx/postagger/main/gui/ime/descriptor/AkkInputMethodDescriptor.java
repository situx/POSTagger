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

import com.github.situx.postagger.main.gui.ime.jquery.AkkInputTermSource;
import com.github.situx.postagger.main.gui.ime.jquery.tree.builder.TreeBuilder;

import java.awt.*;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;

/**
 * Created by timo on 10/31/14.
 */
public class AkkInputMethodDescriptor implements InputMethodDescriptor {
        public static Locale AKKADIAN = new Locale("akk", "", "x-akk-latin");

        public AkkInputMethodDescriptor() {
            System.setProperty("java.awt.im.style",
                    "below-the-spot");
        }

        /**
         * @see java.awt.im.spi.InputMethodDescriptor#
         */
        public InputMethod createInputMethod() throws Exception {
            System.out.println("Creating Cuneiform input method");
            return new GenericInputMethod<>(new AkkInputTermSource(
                    new TreeBuilder(this.getClass().getClassLoader().getResourceAsStream("ime/akkadian.xml"),
                            GenericInputMethodDescriptor.AKKADIAN)));
        }

        /**
         * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales
         */
        public Locale[] getAvailableLocales() {
            return new Locale[]{
                    AKKADIAN
            };
        }

        /**
         * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName
         */
        public synchronized String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
            String localeName = null;
            if (inputLocale == AKKADIAN) {
                localeName = "Akkadian";
            }else if (localeName != null) {

                return "Cuneiform - " + localeName;
            } else {
                return "Cuneiform";
            }
            return null;
        }

        /**
         * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon
         */
        public Image getInputMethodIcon(Locale inputLocale) {
            return null;
        }

        /**
         * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList
         */
        public boolean hasDynamicLocaleList() {
            return false;
        }
}

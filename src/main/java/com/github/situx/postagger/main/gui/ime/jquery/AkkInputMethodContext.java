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

import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;

import java.awt.*;

/**
 * Created by timo on 11/3/14.
 */
public class AkkInputMethodContext extends SetTableInputMethodContext {
    /**
     * Build a new instance for the given component, configuring it
     * with an InputMethod as determined by the given InputMethodDescriptor.
     *
     * @param clientComponent
     */
    public AkkInputMethodContext(Component clientComponent) {
        super(clientComponent, new GenericInputMethodDescriptor());
    }

    /**
     * Covariant return with the typed control Object.
     */
    @Override
    public GenericInputMethodControl getInputMethodControlObject() {
        return (GenericInputMethodControl)super.getInputMethodControlObject();
    }
}

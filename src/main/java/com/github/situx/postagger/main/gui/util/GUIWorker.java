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

import javax.swing.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by timo on 24.07.14.
 */
public class GUIWorker<T,V> extends SwingWorker<T,V> {

    @Override
    protected T doInBackground() throws Exception {
        return null;
    }

    @Override
    protected void done() { // called in the EDT. You can update the GUI here, show error dialogs, etc.
        try {
            Object meaningOfLife = get(); // this line can throw InterruptedException or ExecutionException
            //label.setText(meaningOfLife);
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause(); // if SomeException was thrown by the background task, it's wrapped into the ExecutionException
            if (cause instanceof Exception) {
                e.printStackTrace();
                // TODO handle SomeException as you want to
            }
            else { // the wrapped throwable is a runtime exception or an error
                // TODO handle any other exception as you want to
            }
        }
        catch (InterruptedException ie) {
            // TODO handle the case where the background task was interrupted as you want to
        }
    }
}

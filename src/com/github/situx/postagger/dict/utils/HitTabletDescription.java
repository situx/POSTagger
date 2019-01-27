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

package com.github.situx.postagger.dict.utils;

/**
 * Created by timo on 14.06.16.
 */
public class HitTabletDescription {

    public String cth;

    public String title;

    public String editor;

    public String url;

    public HitTabletDescription(String cth,String editor,String title, String url){
        this.cth=cth;
        this.editor=editor;
        this.title=title;
        this.url=url;
    }

    @Override
    public String toString() {
        return cth+" - "+title+" - "+editor+" - "+url;
    }
}

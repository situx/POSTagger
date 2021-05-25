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

/**
 * Tuple util class.
 * User: Timo Homburg
 * Date: 19.11.13
 * Time: 22:15
 * To change this template use File | Settings | File Templates.
 */
public class Tuple<T,T2> implements Comparable{
    T one;
    T2 two;

    public Tuple(T one, T2 two){
        this.one=one;
        this.two=two;
    }

    @Override
    public int compareTo(Object o) {
        Tuple t=(Tuple) o;
        if(t.two== this.two && this.one==t.one)
            return 0;
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tuple)
            return this.one.equals(((Tuple)obj).one) && this.two.equals(((Tuple)obj).two);
        return false;
    }

    public T getOne(){
        return one;
    }

    public void setOne(final T one) {
        this.one = one;
    }

    public T2 getTwo(){
        return two;
    }

    public void setTwo(final T2 two) {
        this.two = two;
    }

    @Override
    public String toString() {
        return "["+this.one+","+this.two+"]";
    }
}

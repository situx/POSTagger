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

/* Copyright (c) 2007 Jordan Kiang
* jordan-at-kiang.org
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
public class AkkIMEEntry implements AkkIMEEntryAPI {

        private String traditional;
        private String missingPart;
        private int frequency;

        /**
         * @param traditional the in traditional characters
         * @param frequency the frequency of that word, the higher the more frequent
         */
        public AkkIMEEntry(String traditional, int frequency,String missingPart) {
            if(null == traditional) {
                throw new NullPointerException("traditional cannot be null!");
            }
            System.out.println("Traditional: "+traditional);
            System.out.println("MissingPart: "+missingPart);
            this.traditional = traditional;
            this.frequency = frequency;
            this.missingPart=missingPart;
        }

        public String getTraditional() {
            return this.traditional;
        }

        /**
         */
        public String getSimplified() {
            return this.traditional;
            // convert traditional tp simplified on the fly
            //return TraditionalToSimplifiedConverter.toSimplified(this.traditional);
        }

    public String getMissingPart() {
        return missingPart;
    }
    public void setMissingPart(final String missingPart) {
        this.missingPart = missingPart;
    }

        /**
         * The relative frequency of this word.
         * The higher the number the more frequent.
         * @return frequency
         */
        public int getFrequency() {
            return this.frequency;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(AkkIMEEntry that) {
            int compareTo = that.getFrequency() - this.frequency;
            if(compareTo == 0) {
                // if the frequencies match, break the tie with the word

                compareTo = this.traditional.compareTo(that.getTraditional());
                if(compareTo==0){
                    compareTo=this.missingPart.compareTo(that.missingPart);
                }
            }

            return compareTo;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if(o instanceof AkkIMEEntry) {
                AkkIMEEntry that = (AkkIMEEntry)o;

                return this.traditional.equals(that.traditional);
                //&& this.frequency == that.frequency;

            }

            return false;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.traditional.hashCode();// * (this.frequency + 1);
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return this.traditional+" "+this.missingPart;
        }
}

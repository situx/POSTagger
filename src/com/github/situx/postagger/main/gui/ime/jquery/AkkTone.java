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

public enum AkkTone {

        /** first tone */
        FIRST(1),

        /** second tone */
        SECOND(2),

        /** third tone */
        THIRD(3),

        /** fourth tone */
        FOURTH(4),

        /** fifth tone */
        NEUTRAL(5);

        private int toneNum;

        private AkkTone(int toneNum) {
            this.toneNum = toneNum;
        }

        /**
         * Obtain the tone # of the Tone.
         * @return tone #
         */
        public int getToneNum() {
            return this.toneNum;
        }

        /**
         * Obtain the Tone instance for the given tone #
         * @param toneNum
         * @return the Tone
         * @throws IllegalArgumentException if there is no such tone
         */
        static public AkkTone valueOf(int toneNum) throws IllegalArgumentException {
            // cycle through the tones, return the
            // one that matches the given #
            AkkTone[] values = AkkTone.values();
            for(AkkTone tone : values) {
                if(tone.toneNum == toneNum) {
                    return tone;
                }
            }

            // prefer 5, but additionally accept 0
            // to indicate the neutral tone.
            if(toneNum == 0) {
                return NEUTRAL;
            }

            throw new IllegalArgumentException("tone " + toneNum + " does not exist!");
        }

}

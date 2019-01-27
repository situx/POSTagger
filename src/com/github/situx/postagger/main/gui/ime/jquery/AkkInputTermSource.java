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

import com.github.situx.postagger.main.gui.ime.jquery.tree.builder.TreeBuilder;
import com.github.situx.postagger.util.Tuple;

import java.util.*;

/**
 * Created by timo on 10/31/14.
 */
public class AkkInputTermSource implements InputTermSource<AkkUnit> {

        private TreeBuilder dictionary;
        private AkkadMethodControl control = new AkkadMethodControl();

        // partial syallable Strings that can be used to validate
        // raw input.  i.e. if "zhong" is a syllable, then
        // "z", "zh", "zho", "zhon", and "zhong" all are in the Set.
        private Set<String> syllablePartials;

        /**
         * Instantiate a new PinyinInputTermSource backed by the given
         * dictionary and using the given control Object.
         *
         * @param dictionary
         */
        public AkkInputTermSource(TreeBuilder dictionary) {
            if(null == dictionary) {
                throw new NullPointerException("dictionary cannot be null!");
            }

            this.dictionary = dictionary;
            //this.syllablePartials = this.initPartials();
        }

        /**
         * Initialize a Set of partials from valid Pinyin syllables.
         * @return Set of partials
         */
        /*private Set<String> initPartials() {
            Set<String> partials = new HashSet<String>();

            // add each substring starting at the beginning
            // of each syllable.
            AkkSyllable[] syllables = AkkSyllable.values();
            for(AkkSyllable syllable : syllables) {
                String syllableStr = syllable.name();
                for(int i = 1; i <= syllableStr.length(); i++) {

                    // convert to lower case since the input
                    // comes in lower case
                    partials.add(syllableStr.substring(0, i).toLowerCase());
                }
            }

            return partials;
        }*/

        /**
         * Generates a PinyinUnit from the raw input,
         * or null if the raw String isn't parseable
         * to a unit.
         *
         *
         */
        public AkkUnit getInputKey(String raw) {
            AkkUnit key = null;
            try {
                key = AkkUnit.parseValue(raw);
            } catch(IllegalArgumentException iae) {
                // not exceptional, just means
                // the input didn't amount to Pinyin,
                // just return null
            }

            return key;
        }

        /**
         * Checks whether the given raw input is valid.
         *
         *
         */
        public boolean isPartialInputKey(String raw) {
            // if the raw input is contained in the partials, then it's valid.
            //System.out.println("IsPartialInputKey: "+!this.dictionary.queryToMap(raw,1,false).isEmpty());
            return this.dictionary.queryToMap(raw,1,false).size()>1;
        }

        /**
         *
         */
        public String convertRawCharacter(char characterInput) {
            // deal only with lower case pinyin
            return Character.toString(characterInput).toLowerCase();
        }

        /**
         *
         */
        public List<Tuple<InputTerm<AkkUnit>,String>> lookupTerms(List<AkkUnit> pinyinKeys) {
            // don't anticipate with one character... too cluttered
            // with too many potential words returned.
            boolean anticipate = pinyinKeys.size()>1;
            List<AkkIMEEntry> entries = this.dictionary.lookup(pinyinKeys, anticipate);
            System.out.println("Query result: (entries)"+entries);
            final boolean simplified = this.control.getCharacterMode();
            if(anticipate) {
                // if we anticipated longer words, sort so that the shorter
                // words are first, with the frequencies as tiebreakers between
                // words of the same length.  if there was no anticipation,
                // then the entries are already sufficiently sorted.

                Collections.sort(entries, new Comparator<AkkIMEEntry>() {
                    public int compare(AkkIMEEntry o1, AkkIMEEntry o2) {
                        String word1 = simplified ? o1.getSimplified() : o1.getTraditional();
                        String word2 = simplified ? o2.getSimplified() : o2.getTraditional();

                        // first compare lengths, shorter first
                        int compareTo = word1.length() - word2.length();
                        if (compareTo == 0) {
                            // then compare frequencies, higher frequencies first
                            compareTo = o2.getFrequency() - o1.getFrequency();
                        }

                        return compareTo;
                    }
                });
            }

            List<Tuple<InputTerm<AkkUnit>,String>> terms = new ArrayList<Tuple<InputTerm<AkkUnit>,String>>(entries.size());

            // keep track of words that we've already added so that
            // we can exclude words that start with this word to
            // avoid repetition.
            Set<String> seenWords = new HashSet<String>();
            for(AkkIMEEntry entry : entries) {
                //System.out.println("Entry: "+entry.getTraditional());
                // obtain the appropriate representation of the word
                // depending on the current character mode.
                String word = simplified ?
                        entry.getSimplified() :
                        entry.getTraditional();

                int inputSize = pinyinKeys.size();
                List<InputTermUnit<AkkUnit>> termUnits = new ArrayList<InputTermUnit<AkkUnit>>();
                for(int i = 0; i < inputSize; i++) {
                    AkkUnit pinyinKey = pinyinKeys.get(i);

                    // each word should have at least as many chars as PinyinUnit keys.
                    // if for some reason it doesn't, we use a zero-length String
                    // for the unit display String.
                    String keyStr = i < word.length() ? Character.toString(word.charAt(i)) : "";

                    InputTermUnit<AkkUnit> unit = new InputTermUnit<AkkUnit>(pinyinKey, keyStr);
                    termUnits.add(unit);
                }

                // any characters beyond those matched to input
                // are entered as anticipated text on the term.
                String anticipatedSuffix = (word.length()<=inputSize?"":word.substring(inputSize));

                InputTerm<AkkUnit> term = new InputTerm<AkkUnit>(termUnits, anticipatedSuffix);

                // check the words that are already going
                // to be returned and don't return this
                // word if it starts with a previous word
                // to avoid some repetition.  i.e. if
                // we're returning AB and ABC is next,
                // then we don't return ABC.
                String termString = term.toAnticipatedString();
                boolean seen = false;
                if(seenWords.contains(termString)){
                    seen=true;
                }
                for(String seenWord : seenWords) {
                    if(termString.startsWith(seenWord)) {
                        seen = true;
                        break;
                    }
                }

                if(!seen) {
                    // haven't seen a word starting
                    // with the given word, so we
                    // can return this term.
                    seenWords.add(termString);
                    terms.add(new Tuple<>(term,entry.getMissingPart()));
                }
            }

            return terms;
        }

        /**
         *
         */
        public boolean shouldPassThrough(char characterInput) {
            // no pinyin syllable starts with any of the below.
            // don't accept them while the InputMethod is on.
            return !(characterInput+"").matches("[A-z0-9]");
        }

        /**
         * @return control ooject
         */
        public AkkadMethodControl getControlObject() {
            return this.control;
        }

        /**
         *
         */
        public boolean setLocale(Locale locale) {
            // we store the Locale setting (simplified or traditional)
            // on the control Object.  this allows programatic
            // toggling of the mode through the control independent
            // of having to adjust the Locale through the framework.

            if(Locale.SIMPLIFIED_CHINESE.equals(locale)) {
                this.control.setCharacterMode(true);
                return true;

            } else if(Locale.TRADITIONAL_CHINESE.equals(locale)) {
                this.control.setCharacterMode(false);
                return true;

            } else if(Locale.CHINESE.equals(locale)) {
                // just stick with whatever mode they're already in
                // (no sense in getting political about what "CHINESE" means...
                // but return true to indicate that the mode was accepted.
                return true;
            }

            // this InputMethod doesn't accept the given mode
            return false;
        }

        /**
         * @see java.awt.im.spi.InputMethod#getLocale()
         */
        public Locale getLocale() {
            // locale is determined by their character mode
            return this.control.getCharacterMode() ?
                    Locale.SIMPLIFIED_CHINESE :
                    Locale.TRADITIONAL_CHINESE;
        }

        /**
         * A control object, with toggleability of character mode.
         * Gives access to change the character mode without having
         * to set the Locale through the InputContext, which is not
         * normally publicly accessible.
         */
        public class AkkadMethodControl extends GenericInputMethodControl {
            // default to simplified
            private boolean characterMode = true;

            /**
             * @return true for simplified false for traditional
             */
            public boolean getCharacterMode() {
                return this.characterMode;
            }

            /**
             * Set the character mode.
             * @param simplified true for simplified false for traditional
             */
            public void setCharacterMode(boolean simplified) {
                this.characterMode = simplified;
            }
        }
}

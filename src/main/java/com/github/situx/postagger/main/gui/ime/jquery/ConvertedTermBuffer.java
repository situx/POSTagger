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

import com.github.situx.postagger.util.Tuple;

import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ConvertedTermBuffer<V> {

    // the buffer conents, a sequence of InputTerms.
    // use a LinkedList since there will be lots of
    // insertions/deletions.
    private List<InputTerm<V>> terms = new LinkedList<InputTerm<V>>();

    // the term source has the dictionary knowledge
    private InputTermSource<V> termSource;

    // index of the current term within the buffer.
    // -1 if no temr is selected
    private int termIndex;

    // index of the current selected unit within the
    // current term.  -1 if the term as a whole
    // is selected rather than an individual unit
    private int unitIndex;

    // the current cursor position within the buffer
    // relative to the entire concatenated contents
    // of the buffer.  newly entered text is inserted here
    private int insertionIndex;

    /**
     * Create a new InputTermBuffer that derives its terms from
     * the given source of terms.
     * @param termSource
     */
    public ConvertedTermBuffer(InputTermSource<V> termSource) {
        this.termSource = termSource;
        this.termIndex=0;

        // initialize buffer state
        this.clear();
    }

    /**
     * Clear the contents of the buffer.
     */
    public void clear() {
        this.terms.clear();

        // eset values to defaults
        this.termIndex = -1;
        this.unitIndex = -1;
        this.insertionIndex = 0;
    }

    /**
     * Helper gets the InputTerm using the given index,
     * or null if no InputTerm exists (as when the buffer is empty).
     *
     * @param index
     * @return the InputTerm
     */
    private InputTerm<V> getInputTerm(int index) {
        if(index >= 0 && index < this.terms.size()) {
            return this.terms.get(index);
        }

        return null;
    }

    /**
     * @return true if the buffer is empty, false it if contains one or more terms
     */
    public boolean isEmpty() {
        return this.terms.isEmpty();
    }

    /**
     * Get the currently selected contents of the buffer.
     * The selected contents is either the display string
     * of an InputTerm if a whole term is selected, or the
     * display term of an individual unit of a unit within
     * a term is selected
     *
     * @return display String
     */
    public String getCurrentSelection() {
        if(!this.isEmpty()) {
            InputTerm<V> currentTerm = this.terms.get(this.termIndex);

            if(this.isUnitSelected()) {
                // if a unit is selected then return the
                // String of the selected unit.
                InputTermUnit<V> unit = currentTerm.getUnits().get(this.unitIndex);
                return unit.toString();

            } else {
                // otherwise the selection is an entire
                // term. return the text of the whole term.
                return currentTerm.toAnticipatedString();
            }
        }

        // no current selection
        return null;
    }



    /**
     * The current insertion index, where new text is inserted.
     * This is the character index within the whole buffer if all the contents
     * of the buffer are concatenated together to from a String.
     * @return the current insertion index
     */
    public int getInsertionIndex() {
        return this.insertionIndex;
    }

    /**
     * Set the insertion index within the buffer.
     * Marks the insertion index at the term and unit
     * in which the click took place.
     *
     * @param insertionIndex the index
     * @return true if the click was within the buffer and the insertion index updated, false otherwise
     */
    public boolean setInsertionIndex(int insertionIndex) {
        // we need to calculate what term/unit will
        // be selected automatically selected by adopting the new insertionIndex.

        // index of the start of the current term in the whole
        // buffer if its contents are concatenated together
        int termPosition = 0;
        for(int termIndex = 0; termIndex < this.terms.size(); termIndex++) {
            InputTerm<V> nextTerm = this.terms.get(termIndex);

            int termLength = nextTerm.toMatchedString().length();
            if(termPosition + termLength == insertionIndex) {
                // if the insertion index is at the end of
                // the term, then the whole term is selected
                // (unitIndex = -1).

                this.insertionIndex = insertionIndex;
                this.termIndex = termIndex;
                this.unitIndex = -1;

                return true;

            } else if(termPosition + termLength > insertionIndex) {
                // else if the insertion index is within the term,
                // then we need to pick the unit that is selected

                int remaining = insertionIndex - termPosition;
                int unitPosition = 0;

                List<InputTermUnit<V>> nextTermUnits = nextTerm.getUnits();
                for(int i = 0; i < nextTermUnits.size(); i++) {
                    // set the insertion index to the end of the unit
                    // in which the click was made.

                    int unitLength = nextTermUnits.get(i).toString().length();
                    if(unitPosition + unitLength >= remaining) {
                        this.termIndex = termIndex;
                        this.unitIndex = i;
                        this.insertionIndex = termPosition + unitPosition + unitLength;

                        return true;
                    }
                }
            }

            int anticipatedLength = nextTerm.toAnticipatedString().length();
            termPosition += anticipatedLength;
        }

        // click wasn't within the buffer
        return false;
    }

    /**
     * @return true if an individual unit in a term is selected (rather than a whole term)
     */
    private boolean isUnitSelected() {
        return this.unitIndex >= 0;
    }

    /**
     * Move the selection cursor forward one, and move the insertion index to the
     * end of the new selection.  If the current selection is a whole term,
     * with > 1 unit, then the new selection is the first unit individually
     * within the term.  If the current selection is a unit and there are
     * more units within the term, then the selection is the next unit in the
     * term.  If the selection is the last unit in a term (or a whole term
     * with 1 unit), then the selection moves to the next term.
     *
     * @return true if the cursor was advanced, false if there isn't more to select
     */
    public boolean next() {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(!this.isUnitSelected() && null != currentTerm && currentTerm.getUnits().size() > 1) {
            // we are selecting a whole term, not an individual unit within the term.

            // we are now selecting a unit within the term, so we
            // have a non-negative unit index to point to the unit.
            this.unitIndex++;

            // the insertion index moves from after the term, to within
            // the term after newly selected unit.
            this.insertionIndex -= currentTerm.toMatchedString().length();
            this.insertionIndex += currentTerm.getUnits().get(this.unitIndex).toString().length();

            return true;

        } else if(this.isUnitSelected() && this.unitIndex < currentTerm.getUnits().size() - 1) {
            // else if a non-negative unitIndex and there are further units within
            // the current term, then we are currently selecting within a term
            // and we move to the next individually unit.

            // the insertion index advances past the unit
            this.insertionIndex += currentTerm.getUnits().get(this.unitIndex).toString().length();

            // advance to the next unit
            this.unitIndex++;

            return true;

        } else if(this.termIndex < this.terms.size() - 1) {
            // we are at a the last unit of a term, and there
            // are more terms.  advancing next means that
            // we select the next term in its entirety.

            // advance to the next term
            this.termIndex++;
            InputTerm<V> nextTerm = this.getInputTerm(this.termIndex);

            // TODO explain matched vs. anticipated
            if(null != currentTerm) {
                this.insertionIndex -= currentTerm.toMatchedString().length();
                this.insertionIndex += currentTerm.toAnticipatedString().length();
            }
            this.insertionIndex += nextTerm.toMatchedString().length();

            // selecting the whole term
            this.unitIndex = -1;

            return true;
        }

        // nothing next to move to
        return false;
    }

    /**
     * Moves the selection cursor back one and moves the insertion index to
     * the end of the new selection.
     *
     * @return true if can move to the previous, false otherwise
     */
    public boolean previous() {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(this.unitIndex == 0) {
            // if the unitIndex is 0, then we were selecting the first
            // unit of a term with more than one unit, and the new selection
            // is the same term in its entirety

            // insertion index is reset to the end of the whole term
            this.insertionIndex -= currentTerm.getUnits().get(0).toString().length();
            this.insertionIndex += currentTerm.toMatchedString().length();

            // unitIndex set to -1 when selecting a whole term
            this.unitIndex = -1;

            return true;

        } else if(this.unitIndex > 0) {
            // if selecting an individual unit within the term and there
            // are units previous to the selected, then the new selection
            // is the previous unit

            // move the insertion index to the end of the previous unit
            this.insertionIndex -= currentTerm.getUnits().get(this.unitIndex).toString().length();

            // pervious unit now selected
            this.unitIndex--;

            return true;

        } else if(this.termIndex > 0) {
            // else if there were previous terms, then the new selection
            // if there is a previous term with more than one unit,
            // then the selection is the last unit of the previous term.
            // if the previous term has only one unit, then the selection
            // is the whole previous term.

            // insertion index moves to before the current term
            this.insertionIndex -= currentTerm.toMatchedString().length();

            // previous term becomes the current term
            this.termIndex--;
            InputTerm<V> previousTerm = this.getInputTerm(this.termIndex);

            // if the previous term had more than one unit, the last unit
            // is selected, otherwise the whole term is (-1 unitIndex);
            int unitCount = previousTerm.getUnits().size();
            this.unitIndex = unitCount > 1 ? unitCount - 1 : -1;

            return true;

        } else if(this.termIndex == 0) {
            // else if we were on the first term, then previous
            // means we deselect everything.

            // both indices reset, insertion index is at the start of the buffer.
            this.termIndex = -1;
            this.unitIndex = -1;
            this.insertionIndex = 0;

            return true;
        }

        // nothing previous to move back to
        return false;
    }

    /**
     * Obtain alternatives to the current selection,
     * using the keys of the current selection to
     * look them up.
     *
     * @return alternative InputTerms
     */
    public List<Tuple<InputTerm<V>,String>> getAlternativesToSelection() {
        System.out.println("Get Alternatives to Selection");
        List<V> inputKeys;
        System.out.println("Termindex: "+termIndex);
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        System.out.println("Currentterm: "+currentTerm);
        if(null == currentTerm) {
            // nothing is currently selected
            return null;
        }

        if(this.unitIndex >= 0) {
            // an individual unit is selected, so we need
            // alternatives that use the key of the current unit.

            V key = currentTerm.getUnits().get(this.unitIndex).getInputKey();
            inputKeys = new ArrayList<V>(1);
            inputKeys.add(key);

        } else {
            // a term as a whole is selected.  so we need
            // alternatives using the same keys as the
            // current term.

            inputKeys = currentTerm.getInputKeys();
        }

        String currentSelection = this.getCurrentSelection();
        Tuple<InputTerm<V>,String> matchingTerm = null;
        List<Tuple<InputTerm<V>,String>> alternatives = this.termSource.lookupTerms(inputKeys);
        for(Iterator<Tuple<InputTerm<V>,String>> alternativesIter = alternatives.iterator(); alternativesIter.hasNext();) {
            Tuple<InputTerm<V>,String> alternative = alternativesIter.next();
            if(alternative.getOne().toAnticipatedString().equals(currentSelection)) {
                matchingTerm = alternative;
                alternativesIter.remove();
                break;
            }
        }

        if(null != matchingTerm && !alternatives.isEmpty()) {
            alternatives.add(0, matchingTerm);
        }

        return !alternatives.isEmpty() ? alternatives : null;
    }

    /**
     * Obtain alternatives to the current selection,
     * using the keys of the current selection to
     * look them up.
     *
     * @return alternative InputTerms
     */
    public List<Tuple<InputTerm<V>,String>> getAlternativesToSelection2(List<V> text) {
        System.out.println("Get Alternatives to Selection "+text);
        List<Tuple<InputTerm<V>,String>> alternatives = this.termSource.lookupTerms(text);

        return !alternatives.isEmpty() ? alternatives : null;
    }

    /**
     * Truncate current InputTerm so that it only consists of units
     * that matched the input, discarding any anticipated portion.
     *
     * @return true there is a current term and it changed because of this call (it had anticipated text)
     */
    public boolean truncateAnticipated() {
        InputTerm<V> inputTerm = this.getInputTerm(this.termIndex);
        if(null == inputTerm) {
            return false;
        }

        String anticipatedSuffix = inputTerm.getAnticipatedSuffix();
        if(null != anticipatedSuffix && anticipatedSuffix.length() > 0) {
            InputTerm<V> truncated = inputTerm.subTerm(0, inputTerm.getUnits().size());
            this.terms.set(this.termIndex, truncated);

            return true;
        }

        // the current term doesn't have an anticipated part
        return false;
    }

    /**
     * Delete the current selection.
     * If the selection is a whole term, then we delete the whole term.
     * If the selection is an individual unit, then we split the current
     * term into two terms from those units to the left of the deleted
     * unit, and those to the right.
     *
     * @return true if selection deleted, false if no current selection to delete
     */
    public boolean delete() {

        InputTerm<V> inputTerm = this.getInputTerm(this.termIndex);
        if(null == inputTerm) {
            // nothing is currently selected, nothing to delete
            return false;

        } else if(this.unitIndex == 0) {
            // currently the first unit within a term is selected.
            // deleting it means we lop the first unit off the term.
            // the term is now the trailing units.  this term may
            // or may not constitute a term that would otherwise
            // be generatable.  i.e. if the term was ABC and you
            // delete A, the new term is BC, which may not actually
            // be a term you can generate through new input.

            // reset the insertion index to before the unit we're
            // about to delete, we'll move it forward in a moment...
            List<InputTermUnit<V>> currentUnits = inputTerm.getUnits();
            this.insertionIndex -= currentUnits.get(0).toString().length();

            // the new term is composed of the units past the deleted unit. 
            InputTerm<V> rightTerm = inputTerm.subTerm(1, currentUnits.size());
            List<InputTermUnit<V>> rightUnits = rightTerm.getUnits();
            this.insertionIndex += rightUnits.get(0).toString().length();

            // replace the current term with the new term w/o the deleted first unit
            this.terms.set(this.termIndex, rightTerm);

            // if the new term has only one unit, then the whole term
            // is selected (unitIndex == -1). if it has more than one
            // unit, then the first unit is selected (don't need
            // to touch unitIndex).
            if(rightUnits.size() == 1) {
                this.unitIndex = -1;
            }

            return true;

        } else if(this.unitIndex > 0) {
            // if there is currently an individual unit beyond
            // the first unit selected within a term, then we need
            // to split the term.  we replace the current term with
            // a term composed of just the previous term units.  if there are
            // units after the deleted unit, then we also add a new term
            // composed of the those units.

            // move the insertion index back to before the unit
            // that we are deleting.
            List<InputTermUnit<V>> currentUnits = inputTerm.getUnits();
            this.insertionIndex -= currentUnits.get(this.unitIndex).toString().length();

            // we replace the current term with a new term composed of
            // the units before the deleted unit.
            InputTerm<V> leftTerm = inputTerm.subTerm(0, this.unitIndex);
            this.terms.set(this.termIndex, leftTerm);

            if(this.unitIndex < currentUnits.size() - 1) {
                // if there are additional units after the deleted unit,
                // then we additionally form a new term composed of those units,
                // and add it to this buffer.
                InputTerm<V> rightTerm = inputTerm.subTerm(this.unitIndex + 1, currentUnits.size());
                this.terms.add(this.termIndex + 1, rightTerm);
            }

            // the new selection becomes the unit to the left of the deleted unit.
            // if the split left term has only one unit, then that means the whole
            // term is selected (unitIndex == -1).
            this.unitIndex = leftTerm.getUnits().size() > 1 ? this.unitIndex - 1 : -1;

            return true;

        } else {
            // else the whole term is selected.
            // we delete the whole term and all its units.

            this.terms.remove(this.termIndex);

            // the insertionIndex moves back by the deleted term.
            // the new selected term is the previous term (if any).
            this.insertionIndex -= inputTerm.toMatchedString().length();
            this.termIndex--;

            return true;
        }
    }

    /**
     * Replace the currently selected term with the given term.
     * Use this when the an alternative term is selected from
     * a list of choices.
     *
     * @param inputTerm the new term to replace the selection with
     */
    public void replaceSelectionWithTerm(InputTerm<V> inputTerm) {
        if(this.termIndex < 0) {
            // nothing is selected, so we can't replace the selection
            return;

        } else if(this.unitIndex == -1) {
            // currently an entire term is selected.
            // we replace the term with the new term.

            // insertion index is positioned at the end
            // of the new term
            InputTerm<V> replacedTerm = this.getInputTerm(this.termIndex);
            this.insertionIndex -= replacedTerm.toMatchedString().length();
            this.insertionIndex += inputTerm.toMatchedString().length();

            // replace the term
            this.terms.set(this.termIndex, inputTerm);

        } else {
            // an individual unit is selected.
            // we need to replace just that unit with the new term,
            // splitting the previous term as necessary.

            // reset the insertion index to before the deleted unit
            InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
            List<InputTermUnit<V>> currentUnits = currentTerm.getUnits();

            if(this.unitIndex == 0) {
                // if the selected unit for deletion is the first unit,
                // then we need just need one new term of the subsequent
                // units in the deleted term.

                InputTerm<V> rightTerm = currentTerm.subTerm(1, currentUnits.size());

                // replace the current term with the new term,
                // add the split off right term after the inserted.
                // the new term is selected (same position so termIndex stays same)
                this.terms.add(this.termIndex, inputTerm);
                this.terms.set(this.termIndex + 1, rightTerm);

            } else if(this.unitIndex == currentUnits.size() - 1) {
                // else if it's the last unit, then we just need one
                // new term of the previous units in the deleted term.

                InputTerm<V> leftTerm = currentTerm.subTerm(0, currentUnits.size() - 1);

                // the old term is replaced with the split off left term,
                // and the new term is inserted after the left, and the new
                // term becomes the selected term.
                this.terms.set(this.termIndex, leftTerm);
                this.terms.add(++this.termIndex, inputTerm);

            } else {
                // else the selected unit is in the middle of the term.
                // we need to split the current term in two, adding
                // the new term between the split terms.

                InputTerm<V> leftTerm = currentTerm.subTerm(0, this.unitIndex);
                InputTerm<V> rightTerm = currentTerm.subTerm(this.unitIndex + 1, currentUnits.size());

                // replace the current term with the split off left term,
                // insert the new term after, and the split off right term
                // after the new term.  the new term becomes the selected term.
                this.terms.set(this.termIndex, leftTerm);
                this.terms.add(++this.termIndex, inputTerm);
                this.terms.add(this.termIndex + 1, rightTerm);
            }

            // the insertion index moves to after the newly inserted term.
            // after inserting a new term, the selection is always the new
            // term in its entirety, so unitIndex is reset to -1;
            this.insertionIndex -= currentUnits.get(this.unitIndex).toString().length();
            this.insertionIndex += inputTerm.toMatchedString().length();
            this.unitIndex = -1;
        }
    }

    /**
     * Add the new key to the buffer, updating its content
     * to reflect the added key.  Tries to contextually
     * add the new input as best possible according to
     * the current contents of the buffer.  So we try various
     * strategies starting with the most preferred and falling
     * back as necessary.
     *
     * @param inputKey the key
     * @return true if successful, false otherwise
     */
    public List<Tuple<InputTerm<V>,String>> insertKey(V inputKey) {
        List<Tuple<InputTerm<V>,String>> terms;

        if(this.isUnitSelected()) {
            // there is a currently selected unit...

            // attempt to insert the new key into the current term
            terms =	this.insertIntoCurrentTermBeforeCurrentUnit(inputKey);
            if(null != terms) {
                return terms;
            }

            // attempt to insert the key to the left, splitting off the right
            terms =	this.splitTermAppendLeft(inputKey);
            if(null != terms) {
                return terms;
            }

            // attempt to prepend the key to the right, splitting off the left
            terms = this.splitTermPrependRight(inputKey);
            if(null != terms) {
                return terms;
            }

            // attempt to insert the key by splitting the term in half
            terms = this.splitTermInsert(inputKey);
            if(null != terms) {
                return terms;
            }

        } else if(!this.isEmpty()) {
            // the buffer is not empty, but no unit is selected.
            // this means the current selection is a whole unit (unitIndex == -1).

            // attempt to append the key to the current term
            terms = this.appendToTerm(inputKey);
            if(null != terms) {
                return terms;
            }

            // attempt to prepend the key to the next term
            terms = this.prependToNextTerm(inputKey);
            if(null != terms) {
                return terms;
            }

            // attempt to add the key as its own term after the current term
            terms = this.addAsNewTerm(inputKey);
            if(null != terms) {
                return terms;
            }
        }

        // the buffer is empty, just try to add key as a new term
        terms = this.addAsNewTerm(inputKey);

        return terms;
    }

    /**
     * Attempt to insert the key into the current term positioned
     * at the currently selected unit of the term.  Successful if
     * a new term exists that consists of the keys of the existing
     * term with the new key inserted.  If so, the term is replaced
     * by this new term.
     *
     * i.e. if the current term is ABC and B is selected, inserting
     * D will be succesful if ADBC is a term.
     *
     * @param inputKey key to insert
     * @return true if insertion successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> insertIntoCurrentTermBeforeCurrentUnit(V inputKey) {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(null == currentTerm) {
            // can't insert into a term if there is no current term
            return null;
        }

        // lookup keys are the keys of the current term with the
        // input key inserted before the currently selected unit
        List<V> inputKeys = currentTerm.getInputKeys();
        inputKeys.add(this.unitIndex, inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(inputKeys);
        if(matchingTerms.isEmpty()) {
            // no terms for the key sequence
            return null;
        }

        Tuple<InputTerm<V>,String> newTerm = matchingTerms.get(0);
        // replace the current term with the new term
        this.terms.set(this.termIndex, newTerm.getOne());

        // the insertion index's is reset using the replacement term.
        // the unit index remains the same so the unit representing
        // the newly inserted key is selected.
        List<InputTermUnit<V>> currentTermUnits = currentTerm.getUnits();
        List<InputTermUnit<V>> newTermUnits = newTerm.getOne().getUnits();
        for(int i = 0; i < this.unitIndex; i++) {
            this.insertionIndex -= currentTermUnits.get(this.unitIndex).toString().length();
            this.insertionIndex += newTermUnits.get(this.unitIndex).toString().length();
        }

        // the new term is one unit longer, so we additionally account for the added unit
        this.unitIndex++;
        this.insertionIndex += newTermUnits.get(this.unitIndex).toString().length();

        return matchingTerms;
    }

    /**
     * Attempt to insert the key by splitting the current term at the selected
     * unit and appending the key to the left split.  Successful if the left
     * term appended with the new key forms a new term.
     *
     * i.e. if ABC is the current term positioned at unit B, inserting D is
     * successful if ABD is a term.  C will be split off as a separate term,
     * whether or not it would usually be considered a term.
     *
     * @param inputKey
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> splitTermAppendLeft(V inputKey) {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(null == currentTerm) {
            // can't append to the current term if there is no term
            return null;
        }

        // add the key to the keys of the left split and see if that
        // forms a new term.
        List<V> leftKeys = currentTerm.getInputKeys().subList(0, this.unitIndex + 1);
        leftKeys.add(inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(leftKeys);
        if(matchingTerms.isEmpty()) {
            // no terms match the left keys
            return null;
        }

        // appending the key to the left keys gives a new term
        Tuple<InputTerm<V>,String> leftTerm = matchingTerms.get(0);

        // replace the current term with the new term
        this.terms.set(this.termIndex, leftTerm.getOne());

        List<InputTermUnit<V>> currentTermUnits = currentTerm.getUnits();
        List<InputTermUnit<V>> leftTermUnits = leftTerm.getOne().getUnits();

        if(this.unitIndex < currentTermUnits.size() - 1) {
            // if the currently selected unit is less than the last unit
            // of the term, then we form a split right term from the right
            // units.  note that this term is not one that can be necessarily
            // generated independently.  i.e. if we split CD from ABCD, CD
            // might not itself be a term, but we allow it in this case.
            InputTerm<V> rightTerm = currentTerm.subTerm(this.unitIndex + 1, currentTermUnits.size());
            this.terms.add(this.termIndex + 1, rightTerm);
        }

        // reset the insertion index to reflect the new term
        for(int i = 0; i <= this.unitIndex; i++) {
            this.insertionIndex -= currentTermUnits.get(i).toString().length();
            this.insertionIndex += leftTermUnits.get(i).toString().length();
        }

        // account for the additional appended unit
        this.unitIndex++;
        this.insertionIndex += leftTermUnits.get(this.unitIndex).toString().length();

        return matchingTerms;
    }

    /**
     * Attempt to split the current term in two, prepending the given key
     * to the right term.  Successful if the right keys prepended with
     * the new key forms a new term.
     *
     * i.e. if ABC is the term positioned at unit B, inserting D will be
     * successful if DC is a term.  AB will then be split off as a separate
     * term whether or not it usually is itself a term. 
     *
     * @param inputKey the key
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> splitTermPrependRight(V inputKey) {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(null == currentTerm) {
            // can't split the current term if there is no current term
            return null;
        }

        List<InputTermUnit<V>> currentTermUnits = currentTerm.getUnits();

        // obtain the right split keys of a term prepending the key
        List<V> rightKeys = currentTerm.getInputKeys().subList(this.unitIndex + 1, currentTermUnits.size());
        rightKeys.add(0, inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(rightKeys);
        if(matchingTerms.isEmpty()) {
            // no termed matched the split off right keys
            return null;
        }

        Tuple<InputTerm<V>,String> rightTerm = matchingTerms.get(0);
        // a term exists to the right

        // form a new term by splitting off the left units.
        // it replaces the current term
        InputTerm<V> leftTerm = currentTerm.subTerm(0, this.unitIndex + 1);
        this.terms.set(this.termIndex, leftTerm);

        // insert the new right term and set it as the curent term
        this.terms.add(this.termIndex + 1, rightTerm.getOne());
        this.termIndex++;

        List<InputTermUnit<V>> rightTermUnits = rightTerm.getOne().getUnits();

        // if the right term has more than one unit, then the first
        // unit is selected.  if only one unit, then the whole term
        // is selected (-1).
        this.unitIndex = rightTermUnits.size() > 1 ? 0 : -1;

        // update the insertion index to reflect the added unit
        this.insertionIndex += rightTermUnits.get(0).toString().length();

        return matchingTerms;
    }

    /**
     * Split the term in two.
     * Try inserting the the key as a 1-unit term between the split terms.
     * Fails if the new key itself doesn't independently constitute a term.
     *
     * i.e. if the current term is AB positioned at A, then inserting C
     * produces three terms, A, C, and B in order.
     *
     * @param inputKey the key
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> splitTermInsert(V inputKey) {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(null == currentTerm) {
            // can't split the current term if there is no current term
            return null;
        }

        // we need a new key sequence to look up possibilities
        // for the new term.
        List<V> insertTermKeys = new ArrayList<V>(1);
        insertTermKeys.add(inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(insertTermKeys);
        if(matchingTerms.isEmpty()) {
            // no terms matched the input key
            return null;
        }

        Tuple<InputTerm<V>,String> insertTerm = matchingTerms.get(0);

        // a term exists that consists of just one unit for
        // the new key.

        List<InputTermUnit<V>> currentTermUnits = currentTerm.getUnits();
        if(this.unitIndex == currentTermUnits.size() - 1) {
            // if the last unit of a term is selected, then the
            // we just add the new term after the current term.

            this.terms.add(++this.termIndex, insertTerm.getOne());

        } else {
            // otherwise we need to split the current term in two
            // and insert the new term between the two split parts.

            InputTerm<V> splitLeftTerm = currentTerm.subTerm(0, this.unitIndex + 1);
            InputTerm<V> splitRightTerm = currentTerm.subTerm(this.unitIndex + 1, currentTermUnits.size());

            // insert the split terms where the current term is.
            // the selected term is the newly inserted term.
            this.terms.set(this.termIndex, splitLeftTerm);
            this.terms.add(++this.termIndex, insertTerm.getOne());
            this.terms.add(this.termIndex + 1, splitRightTerm);
        }

        // we have inserted a new term, so the entire term
        // is selected (-1).
        this.unitIndex = -1;
        this.insertionIndex += insertTerm.getOne().toMatchedString().length();

        return matchingTerms;
    }

    /**
     * Try appending the key to the current term.
     *
     * if ABC is the current term and adding D, successful if ABCD is a term.
     *
     * @param inputKey the input key
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> appendToTerm(V inputKey) {
        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        System.out.println("Append to term");
        System.out.println("Current term: "+currentTerm);
        if(null == currentTerm) {
            // can't append to current term if there isn't one
            return null;
        }

        List<V> inputKeys = currentTerm.getInputKeys();
        inputKeys.add(inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(inputKeys);
        if(matchingTerms.isEmpty()) {
            // no matching terms
            return null;
        }
        System.out.println("Matching terms");

        Tuple<InputTerm<V>,String> topTerm = matchingTerms.get(0);
        // a term exists for the keys appending the new key
        // replace the current term with the new term
        this.terms.set(this.termIndex, topTerm.getOne());

        // update the insertion index to reflect the new term
        this.insertionIndex = this.insertionIndex
                - currentTerm.toMatchedString().length()
                + topTerm.getOne().toMatchedString().length();

        return matchingTerms;
    }

    /**
     * Try prepending the new key to the next term, if it exists.
     *
     * if AB is the current term and CD is the next term, adding E is
     * successful if ECD is a term.
     *
     * @param inputKey the key
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> prependToNextTerm(V inputKey) {
        InputTerm<V> nextTerm = this.getInputTerm(this.termIndex + 1);
        if(null == nextTerm) {
            // can't prepend to next term if there isn't one
            return null;
        }

        // see if prepending the key to the next term keys
        // will give us a new term
        List<V> nextTermKeys = nextTerm.getInputKeys();
        nextTermKeys.add(0, inputKey);

        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(nextTermKeys);
        if(matchingTerms.isEmpty()) {
            // no matching terms
            return null;
        }

        // pick the first
        Tuple<InputTerm<V>,String> prependedTerm = matchingTerms.get(0);

        // a term exists, replace the next term with the new term.
        // the new selection becomes the first unit of the next term.

        this.termIndex++;
        this.unitIndex = 0;

        this.terms.set(this.termIndex, prependedTerm.getOne());

        // update the insertion index to reflect that the first
        // unit of the next term is now selected.
        this.insertionIndex += prependedTerm.getOne().getUnits().get(0).toString().length();

        return matchingTerms;
    }

    /**
     * Try to add the key as a new term in the buffer
     * after the currently selected term.
     *
     * fails if the inputKey itself doesn't constitute a term
     *
     * @param inputKey the key
     * @return true if successful, false otherwise
     */
    private List<Tuple<InputTerm<V>,String>> addAsNewTerm(V inputKey) {
        // see if there's a term the consists of just the key
        List<V> termInputKeys = new ArrayList<V>(1);
        termInputKeys.add(inputKey);
        System.out.println("New Term? "+inputKey);
        List<Tuple<InputTerm<V>,String>> matchingTerms = this.termSource.lookupTerms(termInputKeys);
        System.out.println("Matching terms "+matchingTerms);
        if(matchingTerms.isEmpty()) {
            // no matches
            return null;
        }

        Tuple<InputTerm<V>,String> topTerm = matchingTerms.get(0);
        // a term exists.  we insert it into the buffer
        // and make it the current selection.

        InputTerm<V> currentTerm = this.getInputTerm(this.termIndex);
        if(null != currentTerm) {
            // if the current term had an anticipated component and the
            // term wasn't committed (since it's still in the buffer),
            // then adding additional input beyond the current
            // term causes the anticipated portion of the current
            // term to be truncated.  anticipated text needs
            // can't be built on and needs to be committed.
            String currentTermAnticipatedSuffix = currentTerm.getAnticipatedSuffix();
            if(null != currentTermAnticipatedSuffix && currentTermAnticipatedSuffix.length() > 0) {
                InputTerm<V> matchedTerm = currentTerm.subTerm(0, currentTerm.getUnits().size());
                this.terms.set(this.termIndex, matchedTerm);
            }
        }

        this.termIndex++;

        // the whole term is selected
        this.unitIndex = -1;

        // update the insertion index to reflect the new term selection
        this.insertionIndex += topTerm.getOne().toMatchedString().length();

        this.terms.add(this.termIndex, topTerm.getOne());

        return matchingTerms;
    }

    /**
     * The display String of the buffer,
     * the display Strings of all the InputTerms in the buffer
     * concatenated together.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        for(InputTerm<V> term : this.terms) {
            sbuf.append(term.toAnticipatedString());
        }

        return sbuf.toString();
    }

    /**
     * When inserting on-the-spot (rather than in a below-the-spot
     * window), the raw text is inserted in-line within the text
     * of te buffer.  This method generates an AttributedString
     * with the text inserted and the raw text highlighted.
     *
     * @param rawInsert text to insert
     * @return the highlighted string
     */
    public AttributedString toAttributedStringWithRawInsert(String rawInsert) {
        if(rawInsert == null) {
            // not really sure why null would be passed in here,
            // but substitute a 0 length String for ease of use.
            rawInsert = "";
        }

        String toString = this.toString();

        StringBuilder sbuf = new StringBuilder();
        sbuf.append(toString.substring(0, this.insertionIndex));
        sbuf.append(rawInsert);
        sbuf.append(toString.substring(this.insertionIndex));

        AttributedString highlightedString = new AttributedString(sbuf.toString());

        // index of the end of the raw insert
        int rawInsertEndIndex = this.insertionIndex + rawInsert.length();

        // index of the end of the buffer contents including the raw insert
        int uncommittedLength = sbuf.length();

        // now highlight the String.
        // check first that the highlighted portion has a positive length,
        // adding a 0 length highlight blows up w/ an Exception

        // highlight the converted but uncommitted text in the buffer prior to the insert
        if(this.insertionIndex > 0) {
            highlightedString.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                    InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT,
                    0, this.insertionIndex);
        }

        // highlight the raw inserted text
        if(rawInsert.length() > 0) {
            highlightedString.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                    InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT,
                    this.insertionIndex, rawInsertEndIndex);
        }

        // highlight the converted but uncommitted text in the buffer after the insert
        if(rawInsertEndIndex < uncommittedLength) {
            highlightedString.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                    InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT,
                    rawInsertEndIndex, uncommittedLength);
        }

        return highlightedString;
    }

    /**
     * Generate an AttributedString that we can use to display the
     * uncommitted text represented by the buffer, including any
     * highlighting for selected sections.
     *
     * @return highlighted string
     */
    public AttributedString toAttributedString() {
        if(this.isEmpty()) {
            // if the buffer is empty, then we
            // don't have much to do
            return new AttributedString("");
        }

        // if the buffer isn't empty, so we generate an Attributed
        // String from the concatenated display Strings.

        String toString = this.toString();
        AttributedString highlightedString = new AttributedString(toString);

        // first apply an unselected highlight to the entire thing,
        // parts or all of which we may overwrite in a moment depending
        // on what is actually selected.
        highlightedString.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT);

        InputTerm<V> inputTerm = this.getInputTerm(this.termIndex);
        if(null != inputTerm) {
            // an InputTerm is currently selected, so we apply a selected
            // highlight to the selected portion, which we need to calculate.

            List<InputTermUnit<V>> termUnits = inputTerm.getUnits();

            // an individual unit within the current term is selected.
            // so the start of the highlight is the insertion index (which is at the end of the unit)
            // minus the length of the unit's display String.
            // if an individual unit is not selected, then we highlight the whole term.
            int selectionStart = this.isUnitSelected() ? this.insertionIndex - termUnits.get(this.unitIndex).toString().length() :
                    this.insertionIndex - inputTerm.toMatchedString().length();
            int selectionEnd = this.insertionIndex;

            highlightedString.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT,
                    InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT,
                    selectionStart,
                    selectionEnd);
        }

        return highlightedString;
    }
}

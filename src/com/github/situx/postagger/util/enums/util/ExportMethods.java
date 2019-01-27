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

package com.github.situx.postagger.util.enums.util;

/**
 * Enum for IME Export Methods.
 */
public enum ExportMethods {
    ANKI("Anki","anki/anki_",".csv",false),
    ANDROID("Android","ime/android/android_",".xml",false),
    IBUS("Ibus","ime/ibus/ibus_",".txt",true),
    JIME("JIME","ime/jime/jime_",".properties",false),
    JQUERY("JQuery","ime/jquery/jquery_",".xml",false),
    SCIM("SCIM","ime/scim/scim_",".txt",true),
    GOTTSTEINJSON("GOTTJS","ime/gottjs_",".json",false),
    GOTTSTEIN("GOTT","ime/gott_",".xml",false), CUNEILISTJSON("CUNEIJS", "ime/cuneijs_", ".json", false);
    public String fileformat;
    //UIM("UIM","ime/uim/uim_"),
    //XIM("XIM","ime/xim/xim_");
    /**The name of the method.*/
    public String methodname;
    /**The export path of the method.*/
    public String path;
    /**Indicator if there are options.*/
    private Boolean hasoptions;

    /**
     * Constructor for this class.
     * @param methodname the name of the method
     * @param path the export path
     * @param fileformat the file format
     * @param hasoptions option indicator
     */
    private ExportMethods(final String methodname,final String path,final String fileformat,final Boolean hasoptions){
        this.methodname=methodname;
        this.path=path;
        this.fileformat=fileformat;
        this.hasoptions=hasoptions;

    }

    /**
     * Gets the option indicator.
     * @return the indicator
     */
    public Boolean getHasoptions() {
        return hasoptions;
    }

    /**
     * Gets the export path.
     * @return Export path as String
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return this.methodname;
    }
}

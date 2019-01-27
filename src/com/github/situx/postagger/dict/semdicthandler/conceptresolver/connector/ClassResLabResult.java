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

package com.github.situx.postagger.dict.semdicthandler.conceptresolver.connector;

public class ClassResLabResult implements Comparable<ClassResLabResult> {

	public String classs;
	
	public String label;
	
	public String resource;

	public String classlabel;
	
	public ClassResLabResult(String classs,String classlabel,String resource, String label){
		this.classs=classs;
		this.label=label;
		this.resource=resource;
		this.classlabel=classlabel;
	}

	@Override
	public int compareTo(ClassResLabResult arg0) {
		return this.classs.compareTo(arg0.classs)+this.label.compareTo(arg0.label);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ClassResLabResult){
			return 	((ClassResLabResult)obj).classs.equals(classs) && ((ClassResLabResult)obj).label.equals(label) && ((ClassResLabResult)obj).resource.equals(resource);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return classs+" - "+label+" - "+resource;
	}
	
}

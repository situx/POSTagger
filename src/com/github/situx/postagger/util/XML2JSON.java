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

import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by timo on 7/2/15.
 */
public class XML2JSON {

    public static void main(String[] args) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("/home/timo/workspace2/POSTagger/dict/hit_map.xml")));
        JSONObject soapDatainJsonObject = XML.toJSONObject(content);
        System.out.println(soapDatainJsonObject);
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("hit_map.json")));
        writer.write(soapDatainJsonObject.toString());
        writer.close();
    }
}

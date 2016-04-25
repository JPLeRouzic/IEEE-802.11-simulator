/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package zzInfra.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class JEIO {
	
	public static void save(Object obj, String path) {
		File hibernationFile = new File(path + "\\hibernation.zip");
		FileOutputStream fileStream;
		try {
			fileStream = new FileOutputStream(hibernationFile);			
			ZipOutputStream zipStream = new ZipOutputStream(fileStream);
			ZipEntry entry = new ZipEntry("hibernation.xml");
			zipStream.putNextEntry(entry);
			XStream xStream = new XStream(new DomDriver());
			xStream.toXML(obj, zipStream);
			zipStream.closeEntry();
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object load(String path) {
		long before = System.currentTimeMillis();
		FileInputStream fileInputStream;
		Object obj = null;
		File file = null;
		try {
			file = new File(path);
			System.out.println("Resuming emulation from file: " + file.getAbsoluteFile());
			fileInputStream = new FileInputStream(file);
			ZipInputStream zipInput = new ZipInputStream(fileInputStream);		
			zipInput.getNextEntry();
			XStream inStream = new XStream(new DomDriver());
			obj = inStream.fromXML(zipInput);
		} catch (FileNotFoundException e) {
			System.err.println("File: " + file.getAbsoluteFile()+ " not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Resumation took:" + (System.currentTimeMillis() - before) + "ms");
		return obj;
	}

}

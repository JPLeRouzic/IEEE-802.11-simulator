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

package zzInfra.emulator;

import gui.conf_screen.FileCopy;
import gui.conf_screen.MainScreen;
import gui.conf_screen.ParseFor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import zzInfra.io.JEIO;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.JFrame;
import zzInfra.kernel.JETime;
import org.w3c.dom.Document;


public class JE802Starter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
            
                        MainScreen thisClass = new MainScreen();
                        thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        thisClass.setVisible(true);
	}

    public static void RunSimul() throws IOException {
        String aScenarioFile = "./scenarios/scenario.xml";
        Document configuration = ParseFor.parseDocument(aScenarioFile);
        Document amendment = ParseFor.parseDocument(aScenarioFile);
        boolean resume = ParseFor.parseForResume(configuration);
        boolean showGui = ParseFor.parseForShowGui(configuration);
        JE802Control control = null;
        if (resume) {
            control = resumeControl(configuration);
            if (control == null) {
                System.err.println("Resume failed");
                System.exit(0);
            }
        } else {
            control = new JE802Control(configuration, amendment, showGui);
            FileCopy.filecopy(aScenarioFile, control.getPath2Results());
        }
        
//        JEmulaPlot.EnterPlot(control.stations) ;
        
        control.startSimulation();
        if (!showGui) {
            JEIO.save(control, control.getPath2Results());
        }        
    }

    static JE802Control resumeControl(Document aDocument) {
        String hibernationFileName = ParseFor.parseForHibernationFile(aDocument);
        if (hibernationFileName == null) {
            System.err.println("No hibernation file specified," + " insert attribute  \"resumeFile\" in tag JE802Control");
            return null;
        }
        JE802Control control = (JE802Control) JEIO.load(hibernationFileName);
        JETime emulationEnd = new JETime(ParseFor.parseForDuration(aDocument)).plus(control.getSimulationTime());
        control.setSimulationEnd(emulationEnd);
        long randomSeed = ParseFor.parseForRandomSeed(aDocument);
        control.setRandomSeed(randomSeed);
        return control;
    }
    
	public static void filecopy(String sourceFileName, String destDirectory) {
		File source = new File(sourceFileName);
		File destination = new File(destDirectory + File.separatorChar
				+ source.getName());

		File directory = new File(destDirectory);
		if (!directory.exists()) { // directory does not exist
			try {
				System.out.println("creating new destination directory "
						+ destDirectory);
				directory.mkdirs();
			} catch (Exception e) {
				System.err
						.println("could not create the destination directory "
								+ destDirectory);
			}
		}

		FileChannel in = null, out = null;
		try {
			if (source.isDirectory()) {
				File newDirectory = new File(destination.getAbsolutePath());
				newDirectory.createNewFile();
				for (File file : source.listFiles()) {
					if (!file.getName().startsWith(".svn")) {
						filecopy(file.getAbsolutePath(), destDirectory);
					}
				}
			} else {
				in = new FileInputStream(source).getChannel();
				out = new FileOutputStream(destination).getChannel();
				long size = in.size();
				MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0,
						size);
				out.write(buf);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}    
}

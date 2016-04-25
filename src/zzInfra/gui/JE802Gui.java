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

package zzInfra.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import zzInfra.kernel.JETime;

public class JE802Gui extends JFrame {

	private static final long serialVersionUID = 1L;

	private JScrollPane jScrollPaneTime;

	private JE802GuiTimePanel jTimePanel;

	private JScrollBar scrollBar;

	private int theGuiWidth = 1100;

	private int theGuiHeight = 700;

	private JE802GuiTimePanel timePanel;

	private int scrollBarMax = 0;

	{
		// Set Look & Feel
		try {
			switch (this.getOS()) {
			case WINDOWS:
				javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				break;
			case UNIX:
				javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JE802Gui(String aScenario) {
		this.setMinimumSize(new Dimension(this.theGuiWidth, this.theGuiHeight));
		this.setPreferredSize(new Dimension(this.theGuiWidth, this.theGuiHeight));
		this.setTitle("Jemula802 emulation results. ( " + aScenario + " )");
		this.scrollBar = new JScrollBar();
		this.timePanel = new JE802GuiTimePanel(this.scrollBar);
		this.timePanel.setBackground(new Color(236, 236, 244));
		this.timePanel.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
		this.jScrollPaneTime = new JScrollPane(this.timePanel);
		this.getContentPane().setLayout(null);
		this.getContentPane().add(this.jScrollPaneTime);
		this.jScrollPaneTime.setWheelScrollingEnabled(false);
		this.addKeyListener(this.timePanel);
		this.jScrollPaneTime.addMouseListener(this.timePanel);
		this.jScrollPaneTime.addMouseWheelListener(this.timePanel);
		this.scrollBar.setMinimum(-10);
		this.scrollBar.setMaximum(0);
		this.scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		this.getContentPane().add(scrollBar);

		this.getContentPane().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Insets insets = JE802Gui.this.getInsets();
				JE802Gui.this.jScrollPaneTime.setBounds(0, 0, JE802Gui.this.getWidth() - insets.left - insets.right,
						JE802Gui.this.getHeight() - insets.top - insets.bottom - 16);
				JE802Gui.this.jScrollPaneTime.setViewportView(JE802Gui.this.timePanel);
				JE802Gui.this.scrollBar.setBounds(0, JE802Gui.this.getHeight() - insets.bottom - insets.top - 16,
						JE802Gui.this.getWidth() - insets.left - insets.right, 16);
				JE802Gui.this.scrollBar.revalidate();
			}
		});

		this.scrollBar.addAdjustmentListener(new AdjustmentListener() {
			int last = -10;

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (this.last == JE802Gui.this.scrollBar.getMaximum()) {
					double value = JE802Gui.this.scrollBar.getValue();
					double diff = value - JE802Gui.this.timePanel.getGuiContext().thePanel_min_ms;
					JE802Gui.this.timePanel.getGuiContext().thePanel_min_ms += diff;
					JE802Gui.this.timePanel.getGuiContext().thePanel_max_ms += diff;
					JE802Gui.this.timePanel.repaint();
				} else {
					this.last = JE802Gui.this.scrollBar.getMaximum();
				}
			}
		});

	}

	public void addBackoff(JETime aStart, JETime aDur, int aSta, int anAC, String aWindow, int aCW, JETime aCWTime, int channel) {
		/*
		 * if (this.jTabbedPane.getSelectedIndex() == 0) {
		 * this.jTimePanel.addBackoff(aStart, aDur, aSta, anAC, aWindow, aCW,
		 * aCWTime); } else if (this.jTabbedPane.getSelectedIndex() == 1) { }
		 * else if (this.jTabbedPane.getSelectedIndex() == 2) { }
		 */
		this.timePanel.addBackoff(aStart, aDur, aSta, anAC, aWindow, aCW, aCWTime, channel);
	}

	public int addFrame(JETime aStart, JETime aDur, int aChannel, int aSta, String aLabel, String a2ndLabel, String destLabel,
			int channel) {
		/*
		 * if (this.jTabbedPane.getSelectedIndex() == 0) { return
		 * this.jTimePanel.addFrame(aStart, aDur, aChannel, aSta, aLabel,
		 * a2ndLabel, destLabel); } else if (this.jTabbedPane.getSelectedIndex()
		 * == 1) { } else if (this.jTabbedPane.getSelectedIndex() == 2) { }
		 * return 0;
		 */
		int time = (int) aStart.plus(aDur).getTime();
		if (time > this.scrollBarMax) {
			this.scrollBarMax = time;
			this.scrollBar.setMaximum(time);
		}
		return this.timePanel.addFrame(aStart, aDur, aChannel, aSta, aLabel, a2ndLabel, destLabel, channel);
	}

	public void addLine(JETime aTime, Integer aSta, Integer anAC, String aColor, int channel) {
		/*
		 * if (this.jTabbedPane.getSelectedIndex() == 0) {
		 * this.jTimePanel.addLine(aTime, aSta, anAC, aColor); } else if
		 * (this.jTabbedPane.getSelectedIndex() == 1) { } else if
		 * (this.jTabbedPane.getSelectedIndex() == 2) { }
		 */
		this.timePanel.addLine(aTime, aSta, anAC, aColor, channel);
	}

	public void addNav(JETime aStart, JETime aDur, Integer aSta, Integer anAC, int channel) {
		/*
		 * if (this.jTabbedPane.getSelectedIndex() == 0) {
		 * this.jTimePanel.addNav(aStart, aDur, aSta, anAC); } else if
		 * (this.jTabbedPane.getSelectedIndex() == 1) { } else if
		 * (this.jTabbedPane.getSelectedIndex() == 2) { }
		 */
		this.timePanel.addNav(aStart, aDur, aSta, anAC, channel);
	}

	public void addText(JETime aTime, Integer aSta, String aText, int channel) {
		/*
		 * if (this.jTabbedPane.getSelectedIndex() == 0) {
		 * this.jTimePanel.addText(aTime, aSta, aText); } else if
		 * (this.jTabbedPane.getSelectedIndex() == 1) { } else if
		 * (this.jTabbedPane.getSelectedIndex() == 2) { }
		 */
		this.jTimePanel.addText(aTime, aSta, aText, channel);
	}

	public void changeFrameWidth(int index, JETime end) {
		this.jTimePanel.changeFrameWidth(index, end);
	}

	private OS getOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return OS.WINDOWS;
		} else if (os.contains("mac")) {
			return OS.OSX;
		} else {
			return OS.UNIX;
		}
	}

	private enum OS {
		WINDOWS, OSX, UNIX
	}
	
	public void setupStation(int number) {
		this.timePanel.setupStation(number);
	}

}

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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import zzInfra.kernel.JETime;

@SuppressWarnings("serial")
public class JE802GuiTimePanel extends JPanel implements KeyListener, MouseWheelListener, MouseListener {

	private Graphics graphics;

	private int maxSta;

	private Map<Integer, Integer> indexMap;

	private int lastTime = 0;

	private JE802GuiContext guiContext;

	private List<JE802Drawable> drawableList;

	private int lastStart = 0;

	private int lastEnd = -1;

	private JScrollBar scrollbar;

	private static Color butter = new Color(252, 233, 79);

	private static Color chameleon = new Color(138, 226, 52);

	private static Color red = new Color(239, 41, 41);

	private List<Integer> channels;

	private int currentChannel = 0;

	private int nextChannelIndex = 0;

	private int frameDrawingState = 0;
	
	private Map<Integer,Integer> verticalPositionMap;

	public JE802GuiTimePanel(JScrollBar scrollbar) {

		super();

		this.scrollbar = scrollbar;
		this.indexMap = new HashMap<Integer, Integer>();
		this.drawableList = new ArrayList<JE802Drawable>();
		this.channels = new ArrayList<Integer>();
		this.channels.add(0);

		this.indexMap.put(0, 0);
		
		this.verticalPositionMap = new HashMap<Integer,Integer>();

		guiContext = new JE802GuiContext();
		guiContext.thePanel_min_ms = -10.0;
		guiContext.thePixel_per_Station = 60;
		guiContext.thePixel_between_Stations = 25;
		guiContext.thePixel_per_ms = 10;
		guiContext.thePixel_per_ms_MAX = 100000;
		guiContext.thePixel_per_ms_MIN = .00000001;
		guiContext.thePixel_per_ms_LABELS = 1500;
		guiContext.thePanel_max_ms = guiContext.thePanel_min_ms + 1200.0 / guiContext.thePixel_per_ms;
		guiContext.backoffs_are_wanted = true;
		guiContext.navs_are_wanted = true;
		guiContext.frames_are_wanted = true;
		guiContext.frame_lines_are_wanted = true;
		guiContext.only_fill_frames = false;

	}

	private void redraw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.graphics = g;
		this.drawGrid(g);
		int size = this.drawableList.size();
		Integer guiStart = (int) (Math.round(this.guiContext.thePanel_min_ms));
		Integer guiEnd = (int) (Math.round(this.guiContext.thePanel_max_ms));
		int offset = (int) (100 / this.guiContext.thePixel_per_ms);
		if (offset < 2) {
			offset = 2;
		}
		guiStart = guiStart - offset;
		guiEnd = guiEnd + offset;
		if (guiStart < 0) {
			guiStart = 0;
		}
		if (guiEnd > this.lastTime) {
			guiEnd = this.lastTime;
		}

		Integer start = 0;
		Integer end = size;
		if (this.lastEnd == -1) {
			this.lastEnd = size;
		}

		if (!this.indexMap.isEmpty()) {

			// find start and end index
			start = this.indexMap.get(guiStart);
			if (start != null) {
				start = Math.max(this.indexMap.get(guiStart) - 1, 0);
			}
			end = this.indexMap.get(guiEnd);

			/*
			 * int counter = 0; //if no start point found, look for the closest
			 * one if (start==null) { if (guiStart<0) { start = 0; } else {
			 * while(true) { guiStart -= 2; counter++; start =
			 * this.indexMap.get(guiStart); if (start!=null || guiStart<0 ||
			 * counter > 5) { if (guiStart<0) { start = 0; } else if
			 * (start==null) { counter = 0; start = lastStart; } break; } } } }
			 * //if no end point found, look for the closest one if (end==null)
			 * { if (guiEnd>this.lastTime) { end = this.indexMap.get(lastTime);
			 * } else { while(true) { guiStart += 2; counter++; end =
			 * this.indexMap.get(guiEnd); if (end!=null || counter > 5) { if
			 * (guiEnd>this.lastTime) { end = this.indexMap.get(lastTime); }
			 * else if (end==null) { end = lastEnd; counter = 0; } break; } } }
			 * }
			 */

			if (start == null) {
				start = lastStart;
			}

			if (end == null) {
				end = lastEnd;
			}

			// System.out.println(guiStart+"->"+start);
			// System.out.println(guiEnd+"->"+end);

			// System.out.println(drawableList.size());

			lastEnd = end;
			lastStart = start;

		}

		// draw elements
		for (int i = start; i < end; i++) {
                    if(this.drawableList == null)
                        {
                            break ;
                        }
                    
                    JE802Drawable drawable = this.drawableList.get(i);
                    if (this.currentChannel == 0 || this.currentChannel == drawable.getChannel()) {
                            drawable.draw(g, this.guiContext);
                    }
		}

		this.graphics.setFont(new Font("Verdana", Font.PLAIN, 10));

		// draw station labels
		for (Integer stationNumber : this.verticalPositionMap.keySet()) {
			int verticalPos = this.verticalPositionMap.get(stationNumber);
			int y = 92 + ((verticalPos-1) * (this.guiContext.thePixel_between_Stations + this.guiContext.thePixel_per_Station));
			this.graphics.setColor(Color.white);
			this.graphics.fillRect(0, y, 70, 17);
			this.graphics.setColor(Color.black);
			this.graphics.drawRect(0, y, 70, 17);
			this.graphics.drawString("Station " + stationNumber, 2, y + 12);
		}
	}
	
	public void setupStation(int number) {
		this.getVeritcalPosition(number);
	}

	private synchronized void drawGrid(Graphics g) {
		if (this.isVisible()) {
			if (g != null) {
				this.guiContext.thePanel_max_ms = this.guiContext.thePanel_min_ms + this.getWidth()
						/ this.guiContext.thePixel_per_ms;
				g.setFont(new Font("Verdana", 1, g.getFont().getSize()));
				for (double grid = this.guiContext.thePanel_max_ms; grid > this.guiContext.thePanel_min_ms; grid = grid - 0.1) {
					if (Math.abs(Math.round(grid / 100.0) * 100.0 - grid) < 0.05) {
						if (this.guiContext.thePixel_per_ms > 0.2) {
							g.setColor(Color.lightGray);
							g.fillRect(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue() - 2, 5, 3, this.getHeight() - 10);
							if (this.guiContext.thePixel_per_ms > 0.6) {
								// if it fits, draw time labels
								g.setColor(Color.black);
								g.drawString(Math.round(grid) + "ms", (new Double(grid * this.guiContext.thePixel_per_ms
										- this.guiContext.thePanel_min_ms * this.guiContext.thePixel_per_ms + 4).intValue()), 16);
							}
						}
					} else if (Math.abs(Math.round(grid / 10.0) * 10.0 - grid) < 0.05) {
						if (this.guiContext.thePixel_per_ms > 2) {
							g.setColor(Color.lightGray);
							g.fillRect(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue() - 2, 5, 2, this.getHeight() - 10);
							if (this.guiContext.thePixel_per_ms > 6) {
								// if it fits, draw time labels
								g.setColor(Color.black);
								g.drawString(new Integer(new Double(Math.round(new Double(grid))).intValue()).toString() + "ms",
										(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
												* this.guiContext.thePixel_per_ms + 4).intValue()), 16);
							}
						}
					} else if (Math.abs(Math.round(new Double(grid)) - new Double(grid)) < 0.05) {
						if (this.guiContext.thePixel_per_ms > 20) {
							g.setColor(Color.lightGray);
							g.drawLine(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue(), 5, (new Double(grid
									* this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue()), this.getHeight() - 7);
							if (this.guiContext.thePixel_per_ms > 60) {
								// if it fits, draw time labels
								g.setColor(Color.black);
								g.drawString(Math.round(grid) + "ms", (new Double(grid * this.guiContext.thePixel_per_ms
										- this.guiContext.thePanel_min_ms * this.guiContext.thePixel_per_ms + 4).intValue()), 16);
							}
						}
					} else {
						if (this.guiContext.thePixel_per_ms > 200) {
							// if helpful, draw subgrid as well
							g.setColor(Color.lightGray);
							g.drawLine(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue(), 25, (new Double(grid
									* this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
									* this.guiContext.thePixel_per_ms).intValue()), this.getHeight() - 27);
							if (this.guiContext.thePixel_per_ms > 600) {
								// if it fits, draw time labels
								g.setColor(Color.black);
								g.setFont(new Font("Verdana", 1, g.getFont().getSize() - 4));
								g.drawString(new Double(Math.round(new Double(grid * 100.0)) / 100.0).toString() + "ms",
										(new Double(grid * this.guiContext.thePixel_per_ms - this.guiContext.thePanel_min_ms
												* this.guiContext.thePixel_per_ms + 4).intValue()), 16);
								g.setFont(new Font("Verdana", 1, g.getFont().getSize() + 4));
							}
						}
					}
				}
				g.setFont(new java.awt.Font("Verdana", 1, g.getFont().getSize()));
			}
		}
	}

	public void updateIndexMap(JETime time) {
		int aTime = (int) time.getTime();
		int diff = aTime - this.lastTime;
		if (diff > 1) {
			for (int i = this.lastTime + 1; i < aTime; i++) {
				this.indexMap.put(i, this.drawableList.size() + 1);
			}
		}
		this.lastTime = aTime;
		if (aTime == 0) {
			aTime = 1;
		}
		this.indexMap.put(aTime, this.drawableList.size() + 1);
	}

	public void changeFrameWidth(int index, JETime end) {
		JE802Frame frame = ((JE802Frame) this.drawableList.get(index));
		JETime start = frame.getaTime();
		JETime dur = new JETime(0.0);
		dur = end.minus(start);
		frame.setDur(dur);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.redraw(g);
	}

	public int addFrame(JETime aStart, JETime aDur, int aChannel, int aSta, String aLabel, String a2ndLabel, String destLabel, int channel) {
		if (!this.channels.contains(channel)) {
			this.channels.add(channel);
			Collections.sort(this.channels);
		}
		this.updateIndexMap(aStart);
		int index = this.drawableList.size();
		this.drawableList.add(new JE802Frame(aStart, aDur, aChannel, this.getVeritcalPosition(aSta), aLabel, a2ndLabel, destLabel, channel));
		this.repaint();
		return index;
	}

	public void addLine(JETime aTime, int aSta, int anAC, String aColor, int channel) {
		updateIndexMap(aTime);
		this.drawableList.add(new JE802Line(aTime, this.getVeritcalPosition(aSta), anAC, aColor, channel));
	}

	public void addNav(JETime aStart, JETime aDur, Integer aSta, Integer anAC, int channel) {
		updateIndexMap(aStart);
		this.drawableList.add(new JE802Nav(aStart, aDur, this.getVeritcalPosition(aSta), anAC, channel));
	}

	public void addBackoff(JETime aStart, JETime aDur, Integer aSta, Integer anAC, String aWindow, Integer aCW, JETime aCWTime,
			int channel) {
		updateIndexMap(aStart);
		this.drawableList.add(new JE802Backoff(aStart, aDur, this.getVeritcalPosition(aSta), anAC, aWindow, aCW, aCWTime, channel));
	}

	public void addText(JETime aTime, Integer aSta, String aText, int channel) {
		updateIndexMap(aTime);
		this.drawableList.add(new JE802Text(aTime, this.getVeritcalPosition(aSta), aText, channel));
	}

	public static Color channel2color(int aChannel) {
		// for maximum 80 channels (Bluetooth), calculate an individual color
		int c = Math.min(255, Math.max(0, Math.round((aChannel * 255 / 80))));
		// System.out.println("int c: " + c);
		return new Color(255 - c, 255 - c, c);
	}

	public static Color phyMode2Color(int id) {
		switch (id) {
		case 1:
			return new Color(255, 255, 255);
		case 2:
			return new Color(255, 223, 223);
		case 3:
			return new Color(255, 191, 191);
		case 4:
			return new Color(255, 159, 159);
		case 5:
			return new Color(255, 127, 127);
		case 6:
			return new Color(255, 95, 95);
		case 7:
			return new Color(255, 63, 63);
		case 8:
			return new Color(255, 32, 32);
		default:
			return Color.black;
		}
	}
	
	private int getVeritcalPosition(int stationNumber) {
		Integer position = this.verticalPositionMap.get(stationNumber);
		if (position==null) {
			position = this.verticalPositionMap.size()+1;
			this.verticalPositionMap.put(stationNumber, position);
			int height = (position * (this.guiContext.thePixel_between_Stations + this.guiContext.thePixel_per_Station) + 100);
			this.setPreferredSize(new Dimension(0, height));
			this.setSize(this.getWidth(), height);
		}
		return position;
	}

	@Override
	public void keyTyped(KeyEvent evt) {
		if (evt.getKeyChar() == 'b' || evt.getKeyChar() == 'B') {
			this.guiContext.backoffs_are_wanted = !this.guiContext.backoffs_are_wanted;
			this.repaint();
		} else if (evt.getKeyChar() == 'n' || evt.getKeyChar() == 'N') {
			this.guiContext.navs_are_wanted = !this.guiContext.navs_are_wanted;
			this.repaint();
		} else if (evt.getKeyChar() == 'f' || evt.getKeyChar() == 'F') {
			this.frameDrawingState = (this.frameDrawingState + 1) % 4;
			switch (this.frameDrawingState) {
			case 0:
				this.guiContext.frames_are_wanted = true;
				this.guiContext.frame_lines_are_wanted = true;
				break;
			case 1:
				this.guiContext.only_fill_frames = true;
				break;
			case 2:
				this.guiContext.only_fill_frames = false;
				this.guiContext.frames_are_wanted = false;
				this.guiContext.frame_lines_are_wanted = true;
				break;
			case 3:
				this.guiContext.frames_are_wanted = false;
				this.guiContext.frame_lines_are_wanted = false;
				break;
			}
			this.repaint();
		} else if (evt.getKeyChar() == 'c' || evt.getKeyChar() == 'C') {
			this.nextChannelIndex++;
			if (this.channels.size() == this.nextChannelIndex) {
				this.nextChannelIndex = 0;
			}
			this.currentChannel = this.channels.get(this.nextChannelIndex);
			this.repaint();
		} else if (evt.getKeyChar() == 'y') {
			AdjustmentListener[] listeners = this.scrollbar.getAdjustmentListeners();
			this.scrollbar.removeAdjustmentListener(listeners[0]);
			this.scrollbar.setValue((int) (this.guiContext.thePanel_min_ms + (40.0 / this.guiContext.thePixel_per_ms)));
			this.guiContext.thePanel_min_ms = this.guiContext.thePanel_min_ms + (40.0 / this.guiContext.thePixel_per_ms);
			this.guiContext.thePanel_max_ms = this.guiContext.thePanel_max_ms + (40.0 / this.guiContext.thePixel_per_ms);
			this.scrollbar.addAdjustmentListener(listeners[0]);
			this.repaint();
		} else if (evt.getKeyChar() == '<') {
			AdjustmentListener[] listeners = this.scrollbar.getAdjustmentListeners();
			this.scrollbar.removeAdjustmentListener(listeners[0]);
			this.scrollbar.setValue((int) (this.guiContext.thePanel_min_ms - (40.0 / this.guiContext.thePixel_per_ms)));
			this.guiContext.thePanel_min_ms = this.guiContext.thePanel_min_ms - (40.0 / this.guiContext.thePixel_per_ms);
			this.guiContext.thePanel_max_ms = this.guiContext.thePanel_max_ms - (40.0 / this.guiContext.thePixel_per_ms);
			this.scrollbar.addAdjustmentListener(listeners[0]);
			this.repaint();
		} else if (evt.getKeyChar() == '+' || evt.getKeyChar() == 'i' || evt.getKeyChar() == 'I') {
			double aPanelCenterTime = this.guiContext.thePanel_min_ms
					+ (this.guiContext.thePanel_max_ms - this.guiContext.thePanel_min_ms) / 2.0;
			// zoom in
			if (this.guiContext.thePixel_per_ms < this.guiContext.thePixel_per_ms_MAX) {
				this.guiContext.thePixel_per_ms = Math.round(this.guiContext.thePixel_per_ms * 1.2);
			} else {
				// max zoom
				this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MAX;
			}
			this.guiContext.thePanel_min_ms = aPanelCenterTime - this.getWidth() / 2.0 / this.guiContext.thePixel_per_ms;
			this.guiContext.thePanel_max_ms = this.guiContext.thePanel_min_ms + this.getWidth() / this.guiContext.thePixel_per_ms;
			this.repaint();
		} else if (evt.getKeyChar() == '-' || evt.getKeyChar() == 'o' || evt.getKeyChar() == 'O') {
			// zoom out
			if (this.guiContext.thePixel_per_ms > this.guiContext.thePixel_per_ms_MIN) {
				this.guiContext.thePixel_per_ms = Math.round(this.guiContext.thePixel_per_ms / 1.2);
			} else {
				// max zoom
				this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MIN;
			}
			double aPanelCenterTime = this.guiContext.thePanel_min_ms
					+ (this.guiContext.thePanel_max_ms - this.guiContext.thePanel_min_ms) / 2.0;
			this.guiContext.thePanel_min_ms = aPanelCenterTime - this.getWidth() / 2.0 / this.guiContext.thePixel_per_ms;
			this.guiContext.thePanel_max_ms = this.guiContext.thePanel_min_ms + this.getWidth() / this.guiContext.thePixel_per_ms;
			this.repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// do nothing;

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent evt) {
		if (evt.isControlDown()) {
			double aMousePosTime = this.guiContext.thePanel_min_ms + this.getMousePosition().x / this.guiContext.thePixel_per_ms;
			if (evt.getWheelRotation() >= 1) {
				// zoom in
				if (this.guiContext.thePixel_per_ms < this.guiContext.thePixel_per_ms_MAX) {
					this.guiContext.thePixel_per_ms = Math.round(this.guiContext.thePixel_per_ms * 1.2);
				} else {
					// max zoom
					this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MAX;
				}
			} else {
				// zoom out
				if (this.guiContext.thePixel_per_ms > this.guiContext.thePixel_per_ms_MIN) {
					this.guiContext.thePixel_per_ms = Math.round(this.guiContext.thePixel_per_ms / 1.2);
				} else {
					// max zoom
					this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MIN;
				}
			}
			this.guiContext.thePanel_min_ms = aMousePosTime - evt.getX() / this.guiContext.thePixel_per_ms;
			this.guiContext.thePanel_max_ms = this.guiContext.thePanel_min_ms + this.getWidth() / this.guiContext.thePixel_per_ms;

		} else if (evt.isAltDown()) {
			if (evt.getWheelRotation() >= 1) {
				this.nextChannelIndex++;
				if (this.channels.size() == this.nextChannelIndex) {
					this.nextChannelIndex = 0;
				}
				this.currentChannel = this.channels.get(this.nextChannelIndex);
			} else {
				this.nextChannelIndex--;
				if (this.nextChannelIndex < 0) {
					this.nextChannelIndex = this.channels.size() - 1;
				}
				this.currentChannel = this.channels.get(this.nextChannelIndex);
			}
		} else {
			AdjustmentListener[] listeners = this.scrollbar.getAdjustmentListeners();
			this.scrollbar.removeAdjustmentListener(listeners[0]);
			if (evt.getWheelRotation() >= 1) {
				this.guiContext.thePanel_min_ms = this.guiContext.thePanel_min_ms + (40.0 / this.guiContext.thePixel_per_ms);
				this.guiContext.thePanel_max_ms = this.guiContext.thePanel_max_ms + (40.0 / this.guiContext.thePixel_per_ms);
				this.scrollbar.setValue((int) ((this.guiContext.thePanel_min_ms + (40.0 / this.guiContext.thePixel_per_ms))));
			} else {
				this.guiContext.thePanel_min_ms = this.guiContext.thePanel_min_ms - (40.0 / this.guiContext.thePixel_per_ms);
				this.guiContext.thePanel_max_ms = this.guiContext.thePanel_max_ms - (40.0 / this.guiContext.thePixel_per_ms);
				this.scrollbar.setValue((int) ((this.guiContext.thePanel_min_ms - (40.0 / this.guiContext.thePixel_per_ms))));
			}
			this.scrollbar.addAdjustmentListener(listeners[0]);
		}
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		Double aClickedTime = new Double(this.guiContext.thePanel_min_ms
				+ new Double(new Double(evt.getX()).doubleValue() / new Double(this.guiContext.thePixel_per_ms).doubleValue()));
		if (evt.getModifiers() == 16) {
			// left mouse click
			if (this.guiContext.thePixel_per_ms < this.guiContext.thePixel_per_ms_MAX) {
				this.guiContext.thePixel_per_ms = new Double(
						java.lang.Math.round(new Double(this.guiContext.thePixel_per_ms * 2))).intValue();
			} else {
				// max zoom
				this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MAX;
			}
		} else if (evt.getModifiers() == 4) {
			// right mouse click
			if (this.guiContext.thePixel_per_ms > this.guiContext.thePixel_per_ms_MIN) {
				this.guiContext.thePixel_per_ms = new Double(Math.round(new Double(this.guiContext.thePixel_per_ms / 2)))
						.intValue();
			} else {
				// max zoom
				this.guiContext.thePixel_per_ms = this.guiContext.thePixel_per_ms_MIN;
			}
		}
		this.guiContext.thePanel_min_ms = aClickedTime
				- new Double(new Double(evt.getX()).doubleValue() / new Double(this.guiContext.thePixel_per_ms).doubleValue());
		this.guiContext.thePanel_max_ms = this.guiContext.thePanel_min_ms
				+ new Double(new Double(this.getWidth()).doubleValue()
						/ new Double(this.guiContext.thePixel_per_ms).doubleValue());
		this.repaint();
	}

	public JE802GuiContext getGuiContext() {
		return guiContext;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// do nothing
	}

}

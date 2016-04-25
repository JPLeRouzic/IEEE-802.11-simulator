package zzInfra.gui;

import java.awt.Graphics;

import zzInfra.kernel.JETime;

public abstract class JE802Drawable {

	protected JETime aTime;
	protected int aSta;
	protected int anAC;
	protected int channel;

	public JE802Drawable(JETime aTime, int aSta, int anAC, int channel) {
		this.aTime = aTime;
		this.aSta = aSta;
		this.anAC = anAC;
		this.channel = channel;
	}

	public abstract void draw(Graphics g, JE802GuiContext context);

	public int getAnAC() {
		return anAC;
	}

	public int getaSta() {
		return aSta;
	}

	public JETime getaTime() {
		return aTime;
	}

	public int getChannel() {
		return channel;
	}

}

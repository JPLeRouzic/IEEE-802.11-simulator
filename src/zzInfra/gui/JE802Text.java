package zzInfra.gui;

import java.awt.Color;
import java.awt.Graphics;

import zzInfra.kernel.JETime;

public class JE802Text extends JE802Drawable {

	private String text;

	public JE802Text(JETime aTime, Integer aSta, String aText, int channel) {
		super(aTime, aSta, 0, channel);
		this.text = aText;
	}

	@Override
	public void draw(Graphics g, JE802GuiContext context) {
		double X = aTime.getTime();
		X = X * context.thePixel_per_ms - context.thePanel_min_ms * context.thePixel_per_ms;
		int aXPos = (int) Math.round(X) - 4;
		int aYPos = aSta * new Double(context.thePixel_per_Station + context.thePixel_between_Stations).intValue() + 2;
		g.setColor(Color.black);
		g.drawString(text, aXPos, aYPos);
	}

}

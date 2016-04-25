package zzInfra.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import zzInfra.kernel.JETime;

public class JE802Line extends JE802Drawable {

	protected String color;

	public JE802Line(JETime aTime, int aSta, int anAC, String aColor, int channel) {
		super(aTime, aSta, anAC, channel);
		this.color = aColor;
	}

	@Override
	public void draw(Graphics g, JE802GuiContext context) {
		Rectangle rect = new Rectangle();
		double X = this.aTime.getTime();
		X = X * context.thePixel_per_ms - context.thePanel_min_ms * context.thePixel_per_ms;
		rect.x = (int) Math.round(X) - 1;
		rect.height = new Double(context.thePixel_per_Station).intValue();
		rect.height = rect.height / 4;
		rect.width = 2;
		rect.y = (anAC) * (rect.height) + aSta
				* new Double(context.thePixel_per_Station + context.thePixel_between_Stations).intValue();
		if (color.equalsIgnoreCase("red")) {
			g.setColor(Color.red);
		} else if (color.equalsIgnoreCase("green")) {
			g.setColor(Color.green);
		} else if (color.equalsIgnoreCase("blue")) {
			g.setColor(Color.blue);
		} else if (color.equalsIgnoreCase("magenta")) {
			g.setColor(Color.magenta);
		} else if (color.equalsIgnoreCase("black")) {
			g.setColor(Color.black);
		} else if (color.equalsIgnoreCase("orange")) {
			g.setColor(Color.orange);
		} else if (color.equalsIgnoreCase("yellow")) {
			g.setColor(Color.yellow);
		} else {
			g.setColor(Color.black);
		}
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

}

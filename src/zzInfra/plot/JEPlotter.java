/*
 * 
 * This is jemula.
 *
 *    Copyright (c) 2006-2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
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

package zzInfra.plot;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A (non real time) plot which contains a single series within a single plot area.
 * 
 * This class is a helper class which allows the convenient application
 * of the JFreeChart library. However it is not part of the JFreeChart
 * library.
 * 
 * @author Laurent Zimmerli - laurentz@student.ethz.ch
 *
 */
@SuppressWarnings("serial")
public class JEPlotter extends ApplicationFrame {

    /** The XY series data. */
    private XYSeries series;
    
    /** The title of the XY series. */
    private String seriesTitle;
    
    /** X-axis label (time axis). */
    private String xAxisLabel;

    /** Y-axis label. */
    private String yAxisLabel;
    
    /** Duration of the plot area. */
    private double duration;
    
    /** Shoe the legend on the plot. */
    private boolean showLegend;
    
    /** Minimum value of the y axis. */
    private double yMin;
    
    /** Maximum value of the y axis. */
    private double yMax;

    /**
     * Create a simple plotter with automatic range.
     * 
     * @param seriesTitle The title of the data series.
     * @param xAxisLabel  The label of the x-axis (time axis).
     * @param yAxisLabel  The label of the y-axis.
     * @param duration    The time span which is covered by the y-axis.
     * @param showLegend  Display the legend on the plot?
     */
    public JEPlotter(String seriesTitle, String xAxisLabel, String yAxisLabel, double duration, boolean showLegend) {
        super("Live Plotter");
    	
        // Set the parameters provided by the client:
    	this.seriesTitle = seriesTitle;
    	this.xAxisLabel = xAxisLabel;
    	this.yAxisLabel = yAxisLabel;
    	this.duration = duration;
    	this.showLegend = showLegend;

    	// Initialize the data series and create the chart:
        series = new XYSeries(seriesTitle);
        XYDataset dataset = new XYSeriesCollection(series);
        JFreeChart chart = createChart(dataset);

        // Create the GUI:
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(640, 280));

        setContentPane(content);
    }
    
    /**
     * Create a simple plotter with defined range.
     * 
     * @param seriesTitle The title of the data series.
     * @param xAxisLabel  The label of the x-axis (time axis).
     * @param yAxisLabel  The label of the y-axis.
     * @param duration    The time span which is covered by the y-axis.
     * @param showLegend  Display the legend on the plot?
     * @param yMin        Fixed minimum value of the y axis.
     * @param yMax        Fixed maximum value of the y axis.
     */
    public JEPlotter(String seriesTitle, String xAxisLabel, String yAxisLabel, double duration, boolean showLegend, double yMin, double yMax) {
        super("Live Plotter");
    	
        // Set the parameters provided by the client:
    	this.seriesTitle = seriesTitle;
    	this.xAxisLabel = xAxisLabel;
    	this.yAxisLabel = yAxisLabel;
    	this.duration = duration;
    	this.showLegend = showLegend;
    	this.yMin = yMin;
    	this.yMax = yMax;

    	// Initialize the data series and create the chart:
        series = new XYSeries(seriesTitle);
        XYDataset dataset = new XYSeriesCollection(series);
        JFreeChart chart = createChart(dataset);

        // Create the GUI:
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(640, 280));
        setContentPane(content);
    }

    /**
     * Create the chart.
     * 
     * @param dataset Data set whose values are plotted.
     * @return The chart.
     */
    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart result = ChartFactory.createXYLineChart(
            seriesTitle, 
            xAxisLabel,
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            showLegend,
            false, // tooltips
            false  // urls
        );
        XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(duration);
        axis = plot.getRangeAxis();
        if (yMax > yMin)
        	axis.setRange(yMin, yMax);
        else
        	axis.setAutoRange(true);
        return result;
    }
    
    /**
     * Show the plot.
     */
    public void display() {
    	this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    /**
     * Plot a given value at a given time.
     * 
     * @param time  The time at which the value should be plotted.
     * @param value The value to be plotted.
     */
    public void plot(double time, double value) {
        series.add(time, value);
    }
}

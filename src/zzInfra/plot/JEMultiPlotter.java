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
 * A (non real-time) plot which can contain multiple series within one single plot area.
 * 
 * This class is a helper class which allows the convenient application
 * of the JFreeChart library. However it is not part of the JFreeChart
 * library.
 * 
 * @author Laurent Zimmerli - laurentz@student.ethz.ch
 *
 */
@SuppressWarnings("serial")
public class JEMultiPlotter extends ApplicationFrame {
	
	/** The data sets containing the values of the series. */
    private XYSeriesCollection dataset;
    
    /** The title of the plot. */
    private String plotTitle;
    
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
     * Create a plotter with multiple data series and dynamic range. The first series is provided upon creation.
     * Additional series can be added.
     * 
     * @param plotTitle   Title of the plot.
     * @param seriesTitle Title of the first series.
     * @param xAxisLabel  Label of the x axis (time axis).
     * @param yAxisLabel  Label of the y axis.
     * @param duration    Duration of the plot area.
     * @param showLegend  Show the legend on the plot?
     */
    public JEMultiPlotter(String plotTitle, String seriesTitle, String xAxisLabel, String yAxisLabel, double duration, boolean showLegend) {
        super(plotTitle);
    	
        // Set the parameters provided by the client:
    	this.plotTitle = plotTitle;
    	this.xAxisLabel = xAxisLabel;
    	this.yAxisLabel = yAxisLabel;
    	this.duration = duration;
    	this.showLegend = showLegend;

    	// Initialize the data series and create the chart:
        XYSeries series = new XYSeries(seriesTitle);
        dataset = new XYSeriesCollection(series);
        JFreeChart chart = createChart(dataset);

        // Create the GUI:
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(640, 280));
        setContentPane(content);
    }
    
    /**
     * Create a plotter with multiple data series and fixed range. The first series is provided upon creation.
     * Additional series can be added.
     * 
     * @param plotTitle   Title of the plot.
     * @param seriesTitle Title of the first series.
     * @param xAxisLabel  Label of the x axis (time axis).
     * @param yAxisLabel  Label of the y axis.
     * @param duration    Duration of the plot area.
     * @param showLegend  Show the legend on the plot?
     * @param yMin        Fixed minimum value of the y axis.
     * @param yMax        Fixed maximum value of the y axis.
     */
    public JEMultiPlotter(String plotTitle, String seriesTitle, String xAxisLabel, String yAxisLabel, double duration, boolean showLegend, double yMin, double yMax) {
        super(plotTitle);
    	
        // Set the parameters provided by the client:
    	this.plotTitle = plotTitle;
    	this.xAxisLabel = xAxisLabel;
    	this.yAxisLabel = yAxisLabel;
    	this.duration = duration;
    	this.showLegend = showLegend;
    	this.yMin = yMin;
    	this.yMax = yMax;

    	// Initialize the data series and create the chart:
        XYSeries series = new XYSeries(seriesTitle);
        dataset = new XYSeriesCollection(series);
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
     * @param dataset The data set holding the XY series.
     * @return The chart.
     */
    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart result = ChartFactory.createXYLineChart(
            plotTitle, 
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
        //axis.setAutoRange(true);
        axis.setRange(0,duration);
        axis.setFixedAutoRange(duration);
        axis = plot.getRangeAxis();
        if (yMax > yMin)
        	axis.setRange(yMin, yMax);
        else
        	axis.setAutoRange(true);
        return result;
    }
    
    /**
     * Add an additional series to the plot.
     * 
     * @param seriesTitle The title of the series.
     */
    public void addSeries(String seriesTitle) {
    	XYSeries series = new XYSeries(seriesTitle);
	    dataset.addSeries(series);
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
     * Plot a given value at a given time in a specified series.
     * 
     * @param time        The time at which the value should be plotted (x-value).
     * @param value       The value to be plotted.
     * @param seriesIndex The index of the series to which the value belongs. 
     */
    public void plot(double time, double value, int seriesIndex) {
    	dataset.getSeries(seriesIndex).add(time, value);
    }
}

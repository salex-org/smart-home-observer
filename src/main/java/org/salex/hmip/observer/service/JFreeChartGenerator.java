package org.salex.hmip.observer.service;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.salex.hmip.observer.data.ClimateMeasurement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class JFreeChartGenerator implements ChartGenerator {
    @Override
    public byte[] create24HourChart(List<ClimateMeasurement> data) throws IOException {
        // Sort data by timestamp ascending
//        Collections.sort(data, new Comparator<ClimateMeasurement>() {
//            public int compare(Measurement one, Measurement another) {
//                return one.getTimestamp().compareTo(another.getTimestamp());
//            }
//        });
//
//        // Create plot with axis
//        final XYPlot plot = new XYPlot();
//        final NumberAxis temperatureAxis = new NumberAxis("Temperatur in °C");
//        temperatureAxis.setRange(-15, 40);
//        temperatureAxis.setTickUnit(new NumberTickUnit(5));
//        final NumberAxis humidityAxis = new NumberAxis("Relative Luftfeuchtigkeit in %");
//        humidityAxis.setRange(0, 100);
//        humidityAxis.setTickUnit(new NumberTickUnit(10));
//        final DateAxis timeAxis = new DateAxis("Zeit in Stunden");
//        if(!data.isEmpty()) {
//            timeAxis.setRange(data.get(0).getTimestamp(), data.get(data.size()-1).getTimestamp());
//        }
//        timeAxis.setDateFormatOverride(new SimpleDateFormat("HH"));
//        timeAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
//        plot.setRangeAxis(0, temperatureAxis);
//        plot.setRangeAxis(1, humidityAxis);
//        plot.setDomainAxis(timeAxis);
//
//        // Create time series with collection and plot with renderers
//        final Map<Sensor, TimeSeries> tempSeries = new HashMap<Sensor, TimeSeries>();
//        final Map<Sensor, TimeSeries> humSeries = new HashMap<Sensor, TimeSeries>();
//        int datasetNumber = 0;
//        for(Sensor sensor : this.sensors) {
//            if(sensor.getType().equals(Sensor.Type.DHT22)) {
//                // Create temperature series for sensor
//                TimeSeries series = new TimeSeries(sensor.getName());
//                tempSeries.put(sensor, series);
//                plot.setDataset(datasetNumber, createTimeSeriesCollection(series));
//                plot.setRenderer(datasetNumber, createRenderer(sensor.getColor(), false));
//                plot.mapDatasetToRangeAxis(datasetNumber, 0);
//                datasetNumber++;
//
//                // Create humidity series for sensor
//                series = new TimeSeries(sensor.getName());
//                humSeries.put(sensor, series);
//                plot.setDataset(datasetNumber, createTimeSeriesCollection(series));
//                plot.setRenderer(datasetNumber, createRenderer(sensor.getColor(), true));
//                plot.mapDatasetToRangeAxis(datasetNumber, 1);
//                datasetNumber++;
//            }
//        }
//
//        // Transfer reading data into time series
//        for(Measurement measurement : data) {
//            for(Reading reading : measurement.getReadings()) {
//                if(reading.getSensor().getType().equals(Sensor.Type.DHT22)) {
//                    tempSeries.get(reading.getSensor()).add(new Minute(measurement.getTimestamp()), reading.getTemperature());
//                    humSeries.get(reading.getSensor()).add(new Minute(measurement.getTimestamp()), reading.getHumidity());
//                }
//            }
//        }
//
//        // generate the chart and return png as byte array
//        JFreeChart chart = new JFreeChart(null, null, plot, false);
//        chart.setBackgroundPaint(null);
//        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ChartUtilities.writeChartAsPNG(baos, chart, 600, 300);
//        return baos.toByteArray();
        return null;
    }
}

package org.salex.hmip.observer.service;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.HomematicMeasurement;
import org.salex.hmip.observer.data.Sensor;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class JFreeChartGenerator implements ChartGenerator {
    @Override
    public byte[] create24HourChart(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data) throws IOException {
        // Create plot with axis
        final var plot = new XYPlot();
        final var temperatureAxis = new NumberAxis("Temperatur in °C");
        temperatureAxis.setRange(-15, 40);
        temperatureAxis.setTickUnit(new NumberTickUnit(5));
        final var humidityAxis = new NumberAxis("Relative Luftfeuchtigkeit in %");
        humidityAxis.setRange(0, 100);
        humidityAxis.setTickUnit(new NumberTickUnit(10));
        final var timeAxis = new DateAxis("Zeit in Stunden");
        if(!data.isEmpty()) {
            timeAxis.setRange(start, end);
        }
        timeAxis.setDateFormatOverride(new SimpleDateFormat("HH"));
        timeAxis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        plot.setRangeAxis(0, temperatureAxis);
        plot.setRangeAxis(1, humidityAxis);
        plot.setDomainAxis(timeAxis);

        // Create time series with collection and plot with renderers
        int datasetNumber = 0;
        for(Sensor sensor : data.keySet()) {
            // Create temperature series for sensor
            final var tempSeries = new TimeSeries(sensor.getName());
            plot.setDataset(datasetNumber, createTimeSeriesCollection(tempSeries));
            plot.setRenderer(datasetNumber, createRenderer(Color.decode(sensor.getColor()), false));
            plot.mapDatasetToRangeAxis(datasetNumber, 0);
            datasetNumber++;

            // Create humidity series for sensor
            final var humSeries = new TimeSeries(sensor.getName());
            plot.setDataset(datasetNumber, createTimeSeriesCollection(humSeries));
            plot.setRenderer(datasetNumber, createRenderer(Color.decode(sensor.getColor()), true));
            plot.mapDatasetToRangeAxis(datasetNumber, 1);
            datasetNumber++;

            // Add data to series
            final var sensorData = data.get(sensor);
            Collections.sort(sensorData, Comparator.comparing(HomematicMeasurement::getMeasuringTime));
            for(var measurement : sensorData) {
                tempSeries.add(new Minute(measurement.getMeasuringTime()), measurement.getTemperature());
                humSeries.add(new Minute(measurement.getMeasuringTime()), measurement.getHumidity());
            }
        }

        // Generate the chart and return PNG as byte array
        JFreeChart chart = new JFreeChart(null, null, plot, false);
        chart.setBackgroundPaint(null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(baos, chart, 600, 300);
        return baos.toByteArray();
    }

    @Override
    public byte[] create365DayTemperatureChart(Date start, Date end, List<ClimateMeasurement> data, Sensor sensor) throws IOException {
        // TODO implement
        return new byte[0];
    }

    @Override
    public byte[] create365DayHumidityChart(Date start, Date end, List<ClimateMeasurement> data, Sensor sensor) throws IOException {
        // TODO implement
        return new byte[0];
    }

    private XYDataset createTimeSeriesCollection(TimeSeries series) {
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    private XYSplineRenderer createRenderer(final Paint paint, final boolean dashed) {
        final XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setSeriesPaint(0, paint);
        renderer.setSeriesShapesVisible(0, false);
        if (dashed) {
            renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
                    new float[] { 2.0f, 5.0f }, 0.0f));
        } else {
            renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f));
        }
        return renderer;
    }
}

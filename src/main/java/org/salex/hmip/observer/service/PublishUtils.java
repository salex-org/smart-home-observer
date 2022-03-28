package org.salex.hmip.observer.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PublishUtils {
	public static final SimpleDateFormat timestampFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
	public static final NumberFormat temperatureFormatter = createNumberFormat("+##0.0;-");
	public static final NumberFormat humidityFormatter = createNumberFormat("##0.0");	
	
	public static String getTempColor(double temp) {
		if (temp <= 0.0) {
			return "#180795"; // dark blue
		} else if (temp < 10.0) {
			return "#0056d6"; // blue
		} else if (temp < 25.0) {
			return "#099f23"; // green
		} else if (temp < 35.0) {
			return "#dd7b1d"; // orange
		} else {
			return "#d60a13"; // red
		}
	}

	public static String getHumidityColor(double humidity) {
		if (humidity < 10.0 || humidity > 90.0) {
			return "#d60a13"; // red
		} else if (humidity < 25.0 || humidity > 75.0) {
			return "#dd7b1d"; // orange
		} else {
			return "#099f23"; // green
		}
	}
	
	private static NumberFormat createNumberFormat(String pattern) {
		final NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		if(format instanceof DecimalFormat) {
			((DecimalFormat) format).applyPattern(pattern);
 		}
		return format;
	}
}

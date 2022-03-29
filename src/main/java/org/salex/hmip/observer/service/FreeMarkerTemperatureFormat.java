package org.salex.hmip.observer.service;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;

import java.util.Locale;

import static freemarker.core.TemplateFormatUtil.checkHasNoParameters;

public class FreeMarkerTemperatureFormat extends FreeMarkerColoredDoubleFormat {
    public static TemplateNumberFormatFactory factory() {
        return new TemplateNumberFormatFactory() {
            @Override
            public TemplateNumberFormat get(String parameters, Locale locale, Environment environment) throws TemplateValueFormatException {
                checkHasNoParameters(parameters);
                return FreeMarkerTemperatureFormat.INSTANCE;
            }
        };
    }

    private static final FreeMarkerTemperatureFormat INSTANCE = new FreeMarkerTemperatureFormat();

    private FreeMarkerTemperatureFormat() {
        super("+##0.0;-");
    }

    @Override
    public String getDescription() {
        return "formatted temperature as a span";
    }

    @Override
    public String getUnitOfMeasurement() {
        return "&nbsp;&deg;C";
    }

    @Override
    public String getColor(double temp) {
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
}

package org.salex.hmip.observer.service;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;

import java.util.Locale;

import static freemarker.core.TemplateFormatUtil.checkHasNoParameters;

public class FreeMarkerCPUTemperatureFormat extends FreeMarkerColoredDoubleFormat {
    public static TemplateNumberFormatFactory factory() {
        return new TemplateNumberFormatFactory() {
            @Override
            public TemplateNumberFormat get(String parameters, Locale locale, Environment environment) throws TemplateValueFormatException {
                checkHasNoParameters(parameters);
                return FreeMarkerCPUTemperatureFormat.INSTANCE;
            }
        };
    }

    private static final FreeMarkerCPUTemperatureFormat INSTANCE = new FreeMarkerCPUTemperatureFormat();

    private FreeMarkerCPUTemperatureFormat() {
        super("+##0.0;-");
    }

    @Override
    public String getDescription() {
        return "formatted cpu temperature as a span";
    }

    @Override
    public String getUnitOfMeasurement() {
        return "&nbsp;&deg;C";
    }

    @Override
    public String getColor(double temp) {
        if (temp > 48.0) {
            return "#d60a13"; // red
        } else if (temp > 47.0) {
            return "#dd7b1d"; // orange
        } else {
            return "#099f23"; // green
        }
    }
}

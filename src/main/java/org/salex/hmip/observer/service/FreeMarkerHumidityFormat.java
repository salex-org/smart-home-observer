package org.salex.hmip.observer.service;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;

import java.util.Locale;

import static freemarker.core.TemplateFormatUtil.checkHasNoParameters;

public class FreeMarkerHumidityFormat extends FreeMarkerColoredDoubleFormat {
    public static TemplateNumberFormatFactory factory() {
        return new TemplateNumberFormatFactory() {
            @Override
            public TemplateNumberFormat get(String parameters, Locale locale, Environment environment) throws TemplateValueFormatException {
                checkHasNoParameters(parameters);
                return FreeMarkerHumidityFormat.INSTANCE;
            }
        };
    }

    private static final FreeMarkerHumidityFormat INSTANCE = new FreeMarkerHumidityFormat();

    private FreeMarkerHumidityFormat() {
        super("##0.0");
    }

    @Override
    public String getDescription() {
        return "formatted humidity as a span";
    }

    @Override
    public String getUnitOfMeasurement() {
        return "&nbsp;%";
    }

    @Override
    public String getColor(double humidity) {
        if (humidity < 10.0 || humidity > 90.0) {
            return "#d60a13"; // red
        } else if (humidity < 25.0 || humidity > 75.0) {
            return "#dd7b1d"; // orange
        } else {
            return "#099f23"; // green
        }
    }
}



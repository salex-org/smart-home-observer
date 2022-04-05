package org.salex.hmip.observer.service;

import freemarker.core.Environment;
import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateNumberFormatFactory;
import freemarker.core.TemplateValueFormatException;

import java.util.Locale;

import static freemarker.core.TemplateFormatUtil.checkHasNoParameters;

public class FreeMarkerMemUsageFormat extends FreeMarkerColoredDoubleFormat {
    public static TemplateNumberFormatFactory factory() {
        return new TemplateNumberFormatFactory() {
            @Override
            public TemplateNumberFormat get(String parameters, Locale locale, Environment environment) throws TemplateValueFormatException {
                checkHasNoParameters(parameters);
                return FreeMarkerMemUsageFormat.INSTANCE;
            }
        };
    }

    private static final FreeMarkerMemUsageFormat INSTANCE = new FreeMarkerMemUsageFormat();

    private FreeMarkerMemUsageFormat() {
        super("##0.0");
    }

    @Override
    public String getDescription() {
        return "formatted memory usage as a span";
    }

    @Override
    public String getUnitOfMeasurement() {
        return "&nbsp;%";
    }

    @Override
    public String getColor(double usage) {
        if (usage > 90.0) {
            return "#d60a13"; // red
        } else if (usage > 80.0) {
            return "#dd7b1d"; // orange
        } else {
            return "#099f23"; // green
        }
    }
}

package org.salex.hmip.observer.service;

import freemarker.core.TemplateNumberFormat;
import freemarker.core.TemplateValueFormatException;
import freemarker.core.UnformattableValueException;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static freemarker.core.TemplateFormatUtil.getNonNullNumber;

public abstract class FreeMarkerColoredDoubleFormat extends TemplateNumberFormat {
    private final DecimalFormat format;

    public FreeMarkerColoredDoubleFormat(String format) {
        this.format = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
        this.format.applyPattern(format);
    }

    @Override
    public String formatToPlainText(TemplateNumberModel numberModel) throws TemplateValueFormatException, TemplateModelException {
        var number = getNonNullNumber(numberModel);
        try {
            final var color = getColor(number.doubleValue());
            final var value = format.format(number.doubleValue());
            return "<span style=\"color: " + color + "\">" + value + getUnitOfMeasurement() + "</span>";
        } catch (ArithmeticException e) {
            throw new UnformattableValueException(String.format("%s doesn't fit into a double", number));
        }
    }

    @Override
    public boolean isLocaleBound() {
        return false;
    }

    public abstract String getColor(double value);

    public abstract String getUnitOfMeasurement();
}

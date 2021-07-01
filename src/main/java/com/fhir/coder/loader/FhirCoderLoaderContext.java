package com.fhir.coder.loader;

import javax.annotation.Nonnull;

public class FhirCoderLoaderContext {
    private String year;
    private String report;
    public FhirCoderLoaderContext(@Nonnull final String year, @Nonnull final String report) {
        this.year = year;
        this.report = report;
    }

    public String getYear() {
        return year;
    }

    public String getReport() {
        return report;
    }

}

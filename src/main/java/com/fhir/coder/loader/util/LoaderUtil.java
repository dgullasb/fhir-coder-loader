package com.fhir.coder.loader.util;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.ValueSet;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class LoaderUtil {
    private static FhirContext ctx = FhirContext.forR4();

    public static ValueSet loadResourceFromFile(final String filename) throws FileNotFoundException {
        return (ValueSet) ctx.newJsonParser().parseResource(new FileReader(filename));
    }
}

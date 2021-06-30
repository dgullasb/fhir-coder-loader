package com.fhir.coder.loader;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The StrictFhirCoderLoader expects the user to supply a valid contextual Year and CMS Report Name.
 * The simple index of all system#code(s) are loaded based on the user's requested context.
 *
 * It's recommended the constructor gets called within either singleton or session scope to avoid unnecessary indexing.
 */
public class StrictFhirCoderLoader {
    private FhirContext ctx = FhirContext.forR4();
    private FhirCoderLoaderContext loaderCtx;
    private Map<String, String> indexMap = new HashMap<>();
    private StrictFhirCoderLoader() { }
    public StrictFhirCoderLoader(@Nonnull final FhirCoderLoaderContext loaderCtx) throws IOException {
        this.loaderCtx = loaderCtx;
        loadIndex();
    }

    private void loadIndex() throws IOException {
        Path dir = Paths.get("target/gen-resources/" + loaderCtx.getYear() + "/" + loaderCtx.getReport());
        Files.walk(dir).forEach(path -> {
            try {
                processFile(path.toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private void processFile(File entry) throws FileNotFoundException {
        if (entry.getPath().endsWith(".json")) {
//            System.out.println("file: " + entry.getPath());
            ValueSet valueSet = loadResourceFromFile(entry.getPath());
            valueSet.getCompose().getInclude().forEach(include -> indexMap.put(include.getSystem() + "#" + include.getConcept().get(0).getCode(), entry.getPath()));
        }
    }

    /**
     * TODO: determine if the user's supplied FhirContext can return either DSTU2, DSTU3, or R4 ValueSet.
     */
    private ValueSet loadResourceFromFile(final String filename) throws FileNotFoundException {
        return (ValueSet) ctx.newJsonParser().parseResource(new FileReader(filename));
    }

    //load only when necessary, referenced by measure-ops report
    public Bundle getParentOID(@Nonnull final String ...systemPoundCodes) {
        Bundle bundle = new Bundle();
        Arrays.stream(systemPoundCodes).iterator().forEachRemaining(key -> {
            try {
                bundle.addEntry().setResource(loadResourceFromFile(indexMap.get(key)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        return bundle;
    }

    public static void main(String[] args) throws IOException {
        Bundle bundle = new StrictFhirCoderLoader(new FhirCoderLoaderContext("2022", "CMS9"))
                .getParentOID("http://hl7.org/fhir/sid/icd-10-cm#E74.20",
                        "http://snomed.info/sct#434781000124105");
        System.out.println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));
    }
}

package com.fhir.coder.loader;

import ca.uhn.fhir.context.FhirContext;
import com.fhir.coder.loader.util.LoaderUtil;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ValueSet;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The FhirCoderLoader expects the user to supply a valid contextual Year and optional CMS Report Name.
 * The simple index of all system#code(s) are loaded based on the user's requested context.
 *
 * It's recommended the constructor gets called within either singleton or session scope to avoid unnecessary indexing.
 */
public class FhirCoderLoader {
    //TODO: determine if the user's supplied FhirContext shall return either DSTU2, DSTU3, or R4 ValueSet.
    private FhirContext ctx = FhirContext.forR4();
    private FhirCoderLoaderContext loaderCtx;
    private Map<String, List<String>> indexMap = new HashMap<>();
    private FhirCoderLoader() { }
    public FhirCoderLoader(@Nonnull final FhirCoderLoaderContext loaderCtx) {
        this.loaderCtx = loaderCtx;
        loadIndex();
    }

    private void loadIndex() {
        Path dir = Paths.get("target/gen-resources/" + loaderCtx.getYear());
        try {
            Files.walk(dir).forEach(path -> {
                try {
                    File entry = path.toFile();
                    if (entry.getPath().endsWith(".json")) {
                        System.out.println("file: " + entry.getPath());
                        ValueSet valueSet = LoaderUtil.loadResourceFromFile(entry.getPath());
                        for (ValueSet.ConceptSetComponent include : valueSet.getCompose().getInclude()) {
                            String key = include.getSystem() + "#" + include.getConcept().get(0).getCode();
                            if (indexMap.containsKey(key)) {
                                indexMap.get(key).add(entry.getPath());
                            } else {
                                indexMap.put(include.getSystem() + "#" + include.getConcept().get(0).getCode(), new ArrayList<>(Arrays.asList(entry.getPath())));
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //find the parent (umbrella) oid relevant to a list of system#code(s)
    public Bundle getParentOID(@Nonnull final String ...systemPoundCodes) {
        Bundle bundle = new Bundle();
        for (String key : systemPoundCodes) {
            if (indexMap.containsKey(key)) {
                for (String file : indexMap.get(key)) {
                    try {
                        bundle.addEntry().setResource(LoaderUtil.loadResourceFromFile(file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bundle;
    }

    public static void main(String[] args) {
        //TODO: http://www.ama-assn.org/go/cpt#99315 is referenced by 9 types of parent OID's
        //Do we need request-context allowing the user to specify category context?
        Bundle bundle = new FhirCoderLoader(new FhirCoderLoaderContext("2022", "any-report"))
                .getParentOID("http://hl7.org/fhir/sid/icd-10-cm#E74.20",
                        "http://snomed.info/sct#434781000124105",
                        "http://www.ama-assn.org/go/cpt#99315");
        System.out.println(FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle));
        System.out.println("number of parent OID ValueSet(s) matching: " + bundle.getEntry().size());
    }
}

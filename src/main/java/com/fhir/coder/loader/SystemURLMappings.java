package com.fhir.coder.loader;

import java.util.HashMap;
import java.util.Map;

/**
 * https://cts.nlm.nih.gov/fhir/
 * Available Code Systems in VSAC:
 */
public class SystemURLMappings {
    public static Map<String, String> shortHandToSystemUrl;
    static {
        shortHandToSystemUrl = new HashMap<>();
        shortHandToSystemUrl.put("ActCode", "http://hl7.org/fhir/v3/ActCode");
        shortHandToSystemUrl.put("ActMood", "http://hl7.org/fhir/v3/ActMood");
        shortHandToSystemUrl.put("ActPriority", "http://hl7.org/fhir/v3/ActPriority");
        shortHandToSystemUrl.put("ActReason", "http://hl7.org/fhir/v3/ActReason");
        shortHandToSystemUrl.put("ActRelationshipType", "http://hl7.org/fhir/v3/ActRelationshipType");
        shortHandToSystemUrl.put("ActStatus", "http://hl7.org/fhir/v3/ActStatus");
        shortHandToSystemUrl.put("AddressUse", "http://hl7.org/fhir/v3/AddressUse");
        shortHandToSystemUrl.put("AdministrativeGender", "http://hl7.org/fhir/v3/AdministrativeGender");
        shortHandToSystemUrl.put("AdministrativeSex", "http://hl7.org/fhir/v2/0001");
        shortHandToSystemUrl.put("CDT", "http://www.ada.org/cdt");
        shortHandToSystemUrl.put("CPT", "http://www.ama-assn.org/go/cpt");
        shortHandToSystemUrl.put("CVX", "http://hl7.org/fhir/sid/cvx");
        shortHandToSystemUrl.put("Confidentiality", "http://hl7.org/fhir/v3/Confidentiality");
        shortHandToSystemUrl.put("DischargeDisposition", "http://hl7.org/fhir/v2/0112");
        shortHandToSystemUrl.put("EntityNamePartQualifier", "http://hl7.org/fhir/v3/EntityNamePartQualifier");
        shortHandToSystemUrl.put("EntityNameUse", "http://hl7.org/fhir/v3/EntityNameUse");
        shortHandToSystemUrl.put("ICD10CM", "http://hl7.org/fhir/sid/icd-10-cm");
        shortHandToSystemUrl.put("ICD10PCS", "http://www.icd10data.com/icd10pcs");
        shortHandToSystemUrl.put("ICD9CM", "http://hl7.org/fhir/sid/icd-9-cm");
        shortHandToSystemUrl.put("LOINC", "http://loinc.org");
        shortHandToSystemUrl.put("LanguageAbilityMode", "http://hl7.org/fhir/v3/LanguageAbilityMode");
        shortHandToSystemUrl.put("LanguageAbilityProficiency", "http://hl7.org/fhir/v3/LanguageAbilityProficiency");
        shortHandToSystemUrl.put("LivingArrangement", "http://hl7.org/fhir/v3/LivingArrangement");
        shortHandToSystemUrl.put("MaritalStatus", "http://hl7.org/fhir/v3/MaritalStatus");
        shortHandToSystemUrl.put("MED-RT", "http://www.nlm.nih.gov/research/umls/MED-RT");
        shortHandToSystemUrl.put("NCI", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl");
        shortHandToSystemUrl.put("NDFRT", "http://hl7.org/fhir/ndfrt");
        shortHandToSystemUrl.put("NUCCPT", "http://nucc.org/provider-taxonomy");
        shortHandToSystemUrl.put("NullFlavor", "http://hl7.org/fhir/v3/NullFlavor");
        shortHandToSystemUrl.put("ObservationInterpretation", "http://hl7.org/fhir/v3/ObservationInterpretation");
        shortHandToSystemUrl.put("ObservationValue", "http://hl7.org/fhir/v3/ObservationValue");
        shortHandToSystemUrl.put("ParticipationFunction", "http://hl7.org/fhir/v3/ParticipationFunction");
        shortHandToSystemUrl.put("ParticipationMode", "http://hl7.org/fhir/v3/ParticipationMode");
        shortHandToSystemUrl.put("ParticipationType", "http://hl7.org/fhir/v3/ParticipationType");
        shortHandToSystemUrl.put("PresentOnAdmission", "https://www.cms.gov/Medicare/Medicare-Fee-for-Service-Payment/HospitalAcqCond/Coding");
        shortHandToSystemUrl.put("RXNORM", "http://www.nlm.nih.gov/research/umls/rxnorm");
        shortHandToSystemUrl.put("ReligiousAffiliation", "http://hl7.org/fhir/v3/ReligiousAffiliation");
        shortHandToSystemUrl.put("RoleClass", "http://hl7.org/fhir/v3/RoleClass");
        shortHandToSystemUrl.put("RoleCode", "http://hl7.org/fhir/v3/RoleCode");
        shortHandToSystemUrl.put("RoleStatus", "http://hl7.org/fhir/v3/RoleStatus");
        shortHandToSystemUrl.put("SNOMEDCT", "http://snomed.info/sct");
        shortHandToSystemUrl.put("SOP", "https://nahdo.org/sopt");
        shortHandToSystemUrl.put("UCUM (Common UCUM Units)", "http://unitsofmeasure.org");
        shortHandToSystemUrl.put("UMLS", "http://www.nlm.nih.gov/research/umls");
        shortHandToSystemUrl.put("UNII", "http://fdasis.nlm.nih.gov");
        shortHandToSystemUrl.put("mediaType", "http://hl7.org/fhir/v3/MediaType");
    }
}

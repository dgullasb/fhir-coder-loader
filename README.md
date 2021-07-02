# fhir-coder-loader
This project is a proof of concept that attempts to tackle the loading of third party ValueSet(s) referenced by FHIR R4 Profiles USCDI & Qi Core. Obtained from the VSAC download site, spreadsheets are processed to build static JSON resources:

https://vsac.nlm.nih.gov/download/ecqm

# load strategies:
The number of codes are excessive, so they should only be loaded [/ValueSet/$expand] when referenced within measure-ops.

FhirCoderLoader - expects the user to supply a valid contextual Year and optional CMS Report Name.


# ambitions:
> VSACS downloads cover RxNorm (generic medications) but in addition we need to accurately code NDC (National Drug Code). We need to find out if there is a downloadable package for NDC codes covering all permutations of manufactured drugs:

https://mor.nlm.nih.gov/RxNav

> Multiple build artifacts can be constructed with classifiers(s) so other projects can choose to only get indexed json files relevant to their report(s):
    Ex: fhir-coder-loader-ALL.jar, fhir-coder-loader-CMS104-2017.jar, fhir-coder-loader-NDC-2021.jar

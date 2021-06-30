# fhir-coder-loader
This project is a proof of concept that attempts to tackle the loading of third party ValueSet(s) referenced by FHIR R4 Profiles USCDI & Qi Core.

Load strategies:
The number of codes are excessive, so they should only be loaded [/ValueSet/$expand] when referenced within measure-ops.

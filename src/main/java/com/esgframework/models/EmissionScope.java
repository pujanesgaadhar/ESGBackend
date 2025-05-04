package com.esgframework.models;

public enum EmissionScope {
    SCOPE_1,  // Direct emissions from owned or controlled sources
    SCOPE_2,  // Indirect emissions from purchased electricity, steam, heating and cooling
    SCOPE_3,  // All other indirect emissions in company's value chain
    SOLVENT,  // Solvent consumption and emissions
    SINK      // Carbon sink data (e.g., reforestation)
}

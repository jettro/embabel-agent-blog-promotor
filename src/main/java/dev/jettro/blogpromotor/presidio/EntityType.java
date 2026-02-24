package dev.jettro.blogpromotor.presidio;

public enum EntityType {
    // Global
    CREDIT_CARD,
    CRYPTO,
    DATE_TIME,
    EMAIL_ADDRESS,
    IBAN_CODE,
    IP_ADDRESS,
    NRP,
    LOCATION,
    PERSON,
    PHONE_NUMBER,
    MEDICAL_LICENSE,
    URL,

    // USA
    US_BANK_NUMBER,
    US_DRIVER_LICENSE,
    US_ITIN,
    US_PASSPORT,
    US_SSN,

    // UK
    UK_NHS,

    // Spain
    ES_NIF,

    // Italy
    IT_FISCAL_CODE,
    IT_DRIVER_LICENSE,
    IT_VAT_CODE,
    IT_PASSPORT,
    IT_IDENTITY_CARD,

    // Poland
    PL_PESEL,

    // Singapore
    SG_NRIC_FIN,
    SG_UEN,

    // Australia
    AU_ABN,
    AU_ACN,
    AU_TFN,
    AU_MEDICARE,

    // India
    IN_PAN,
    IN_AADHAAR,
    IN_VEHICLE_REGISTRATION
}

package com.sis.util;

public  class Constants {

    /* Validation Regex */
    public static final  String ENGLISH_CHARACTERS = "^[a-zA-Z ]+$" ;
    public static final  String ENGLISH_CHARACTERS_OR_DIGITS = "^[a-zA-Z \\d]+$" ;
    public static final  String ARABIC_CHARACTERS = "^[\\u0621-\\u064A ]+$" ;
    public static final  String ARABIC_CHARACTERS_OR_DIGITS = "^[\\u0621-\\u064A \\d]+$" ;
    public static final  String DIGITS_ONLY_14 = "^[0-9]{14}$" ;
    public static final  String DIGITS_ONLY_11 = "^[0-9]{11}$" ;
    public static final  String DIGITS_ONLY = "^[0-9]+$" ;
}
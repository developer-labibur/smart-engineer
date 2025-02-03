package com.softylasa.smartengineer.utils.helpers

class ConvertFB {
    companion object{

        fun covertDepNameToCode(name: Any): String {
            val codeStr = name.toString()  // Convert any type to string for matching
            return when (codeStr) {
                "61 => Architecture Technology" -> "61"
                "64 => Civil Technology" -> "64"
                "85 => Computer Science and Technology" -> "85"
                "67 => Electrical Technology" -> "67"
                "68 => Electronics Technology" -> "68"
                "69 => Food Technology" -> "69"
                "70 => Mechanical Technology" -> "70"
                "88 => Construction Technology" -> "88"
                "71 => Power Technology" -> "71"
                else -> ""
            }
        }
        fun covertDepartmentToCode(name: Any): String {
            val codeStr = name.toString()  // Convert any type to string for matching
            return when (codeStr) {
                "Architecture Technology" -> "61"
                "Civil Technology" -> "64"
                "Computer Science and Technology" -> "85"
                "Electrical Technology" -> "67"
                "Electronics Technology" -> "68"
                "Food Technology" -> "69"
                "Mechanical Technology" -> "70"
                "Construction Technology" -> "88"
                "Power Technology" -> "71"
                "BSC Engineering" -> "101"
                "Other" -> "102"
                else -> ""
            }
        }
        fun covertDepCodeToName(code: Any): String {
            val codeStr = code.toString()  // Convert any type to string for matching
            return when (codeStr) {
                "61" -> "Architecture Technology"
                "64" -> "Civil Technology"
                "85" -> "Computer Science and Technology"
                "67" -> "Electrical Technology"
                "68" -> "Electronics Technology"
                "69" -> "Food Technology"
                "70" -> "Mechanical Technology"
                "88" -> "Construction Technology"
                "71" -> "Power Technology"
                else -> "Unknown Department"
            }
        }
        fun covertSemNameToTag(name: Any): String {
            val codeStr = name.toString()  // Convert any type to string for matching
            return when (codeStr) {
                "1st Semester => Architecture" -> "AIDT1"
                "2nd Semester => Architecture" -> "AIDT2"
                "3rd Semester => Architecture" -> "AIDT3"
                "4th Semester => Architecture" -> "AIDT4"
                "5th Semester => Architecture" -> "AIDT5"
                "6th Semester => Architecture" -> "AIDT6"
                "7th Semester => Architecture" -> "AIDT7"
                "1st Semester => Civil" -> "CT1"
                "2nd Semester => Civil" -> "CT2"
                "3rd Semester => Civil" -> "CT3"
                "4th Semester => Civil" -> "CT4"
                "5th Semester => Civil" -> "CT5"
                "6th Semester => Civil" -> "CT6"
                "7th Semester => Civil" -> "CT7"
                "1st Semester => Computer" -> "CST1"
                "2nd Semester => Computer" -> "CST2"
                "3rd Semester => Computer" -> "CST3"
                "4th Semester => Computer" -> "CST4"
                "5th Semester => Computer" -> "CST5"
                "6th Semester => Computer" -> "CST6"
                "7th Semester => Computer" -> "CST7"
                "1st Semester => Electrical" -> "ET1"
                "2nd Semester => Electrical" -> "ET2"
                "3rd Semester => Electrical" -> "ET3"
                "4th Semester => Electrical" -> "ET4"
                "5th Semester => Electrical" -> "ET5"
                "6th Semester => Electrical" -> "ET6"
                "7th Semester => Electrical" -> "ET7"
                "1st Semester => Electronics" -> "ENT1"
                "2nd Semester => Electronics" -> "ENT2"
                "3rd Semester => Electronics" -> "ENT3"
                "4th Semester => Electronics" -> "ENT4"
                "5th Semester => Electronics" -> "ENT5"
                "6th Semester => Electronics" -> "ENT6"
                "7th Semester => Electronics" -> "ENT7"
                "1st Semester => Mechanical" -> "MT1"
                "2nd Semester => Mechanical" -> "MT2"
                "3rd Semester => Mechanical" -> "MT3"
                "4th Semester => Mechanical" -> "MT4"
                "5th Semester => Mechanical" -> "MT5"
                "6th Semester => Mechanical" -> "MT6"
                "7th Semester => Mechanical" -> "MT7"
                "1st Semester => Construction" -> "CNT1"
                "2nd Semester => Construction" -> "CNT2"
                "3rd Semester => Construction" -> "CNT3"
                "4th Semester => Construction" -> "CNT4"
                "5th Semester => Construction" -> "CNT5"
                "6th Semester => Construction" -> "CNT6"
                "7th Semester => Construction" -> "CNT7"
                "1st Semester => Power" -> "PT1"
                "2nd Semester => Power" -> "PT2"
                "3rd Semester => Power" -> "PT3"
                "4th Semester => Power" -> "PT4"
                "5th Semester => Power" -> "PT5"
                "6th Semester => Power" -> "PT6"
                "7th Semester => Power" -> "PT7"
                "1st Semester => Food" -> "FT1"
                "2nd Semester => Food" -> "FT2"
                "3rd Semester => Food" -> "FT3"
                "4th Semester => Food" -> "FT4"
                "5th Semester => Food" -> "FT5"
                "6th Semester => Food" -> "FT6"
                "7th Semester => Food" -> "FT7"
                else -> ""
            }
        }

    }
}
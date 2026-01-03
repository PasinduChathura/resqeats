package com.ffms.trackable.util;

import org.passay.*;

public class CustomPasswordGenerator {
    public static String generatePassayPassword() {
        PasswordGenerator generator = new PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);
        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);
        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);
        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "!@#$%^&*()_+";
            }
            public String getCharacters() {
                return "ERRONEOUS_SPECIAL_CHARS";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);
        return generator.generatePassword(10, splCharRule, lowerCaseRule, upperCaseRule, digitRule);
    }
}

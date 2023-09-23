package com.evansloan.collectionlog.luck;

import org.junit.Test;

import static org.junit.Assert.*;

public class FuzzyStringMatchTest {

    @Test
    public void getLongestCommonSubstringLength_nomatch() {
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("asdf", "qwerty"));
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("", "qwerty"));
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("asdf", ""));
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("", ""));
    }

    @Test
    public void getLongestCommonSubstringLength_fullMatch() {
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("", ""));
        assertEquals(4, FuzzyStringMatch.getLongestCommonSubstringLength("1234", "1234"));
        assertEquals(10, FuzzyStringMatch.getLongestCommonSubstringLength("asdfqwerty", "asdfqwerty"));
    }

    @Test
    // Note: this function should be called with toLower() strings if case insensitivity is desired
    public void getLongestCommonSubstringLength_caseSensitive() {
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("ASDFQUERTY", "asdfqwerty"));
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("asdfqwerty", "ASDFQUERTY"));
        assertEquals(0, FuzzyStringMatch.getLongestCommonSubstringLength("AsDfQwErTy", "aSdFqWeRtY"));
    }

    @Test
    public void getLongestCommonSubstringLength_basicCase() {
        assertEquals(9, FuzzyStringMatch.getLongestCommonSubstringLength("big beans in soup", "i really like big beans"));
        assertEquals(14, FuzzyStringMatch.getLongestCommonSubstringLength("favorite ice cream", "favourite ice cream"));
    }

    @Test
    public void getLevenshteinEditDistance_fullMatch() {
        assertEquals(0, FuzzyStringMatch.getLevenshteinEditDistance("", ""));
        assertEquals(0, FuzzyStringMatch.getLevenshteinEditDistance("1234", "1234"));
        assertEquals(0, FuzzyStringMatch.getLevenshteinEditDistance("asdfqwerty", "asdfqwerty"));
    }

    @Test
    // Note: this function should be called with toLower() strings if case insensitivity is desired
    public void getLevenshteinEditDistance_caseSensitive() {
        assertEquals(10, FuzzyStringMatch.getLevenshteinEditDistance("ASDFQUERTY", "asdfqwerty"));
        assertEquals(10, FuzzyStringMatch.getLevenshteinEditDistance("asdfqwerty", "ASDFQUERTY"));
        assertEquals(10, FuzzyStringMatch.getLevenshteinEditDistance("AsDfQwErTy", "aSdFqWeRtY"));
    }

    @Test
    public void getLevenshteinEditDistance_basicCase() {
        assertEquals(5, FuzzyStringMatch.getLevenshteinEditDistance("big beans", "beean"));
        assertEquals(1, FuzzyStringMatch.getLevenshteinEditDistance("favorite ice cream", "favourite ice cream"));
    }

}
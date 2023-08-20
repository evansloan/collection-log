package com.evansloan.collectionlog.luck;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionLogItemAliasesTest {

    @Test
    public void aliasItemName_replacesAlias() {
        final String expectedItemName = "Abyssal orphan";

        final String actualItemName = CollectionLogItemAliases.aliasItemName("abyssal sire pet");

        assertEquals(expectedItemName, actualItemName);
    }

    @Test
    public void aliasItemName_replacesAliasIgnoringCase() {
        final String expectedItemName = "Abyssal orphan";

        final String actualItemName = CollectionLogItemAliases.aliasItemName("ABySSal Sire PET");

        assertEquals(expectedItemName, actualItemName);
    }

    @Test
    public void aliasItemName_missingAliasReturnsInput() {
        final String expectedItemName = "Some unrecognized item name";

        final String actualItemName = CollectionLogItemAliases.aliasItemName(expectedItemName);

        assertEquals(expectedItemName, actualItemName);
    }
}
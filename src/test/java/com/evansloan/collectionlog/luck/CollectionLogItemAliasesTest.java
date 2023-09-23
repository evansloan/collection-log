package com.evansloan.collectionlog.luck;

import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

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
        final String expectedItemName = "xxxxxxxxxxxxxxx";

        final String actualItemName = CollectionLogItemAliases.aliasItemName(expectedItemName);

        assertEquals(expectedItemName, actualItemName);
    }

    @Test
    public void aliasItemName_backupFuzzyMatch_alwaysReturnsExactMatch() {
        for (LogItemInfo logItemInfo : LogItemInfo.getAllLogItemInfos()) {
            String name = logItemInfo.getItemName();
            assertEquals(name, CollectionLogItemAliases.aliasItemName(name));
        }
    }

    @Test
    public void aliasItemName_backupFuzzyMatch_pets() {
        assertEquals("Abyssal orphan", CollectionLogItemAliases.aliasItemName("pet abyssal orphan"));

        assertEquals("Pet dagannoth rex", CollectionLogItemAliases.aliasItemName("rex pet"));
        assertEquals("Pet dagannoth rex", CollectionLogItemAliases.aliasItemName("dag rex pet"));
        assertEquals("Pet dagannoth rex", CollectionLogItemAliases.aliasItemName("dagannoth pet rex"));

        assertEquals("Vet'ion jr.", CollectionLogItemAliases.aliasItemName("veti jr"));
        assertEquals("Vet'ion jr.", CollectionLogItemAliases.aliasItemName("vetion pet"));

        assertEquals("Pet smoke devil", CollectionLogItemAliases.aliasItemName("thermy pet"));
        assertEquals("Pet smoke devil", CollectionLogItemAliases.aliasItemName("pet thermy"));
        assertEquals("Pet smoke devil", CollectionLogItemAliases.aliasItemName("smoke deveil pet"));

        assertEquals("Callisto cub", CollectionLogItemAliases.aliasItemName("callisto pet"));
        assertEquals("Callisto cub", CollectionLogItemAliases.aliasItemName("pet callisto"));

        assertEquals("Venenatis spiderling", CollectionLogItemAliases.aliasItemName("venenatis pet"));
        assertEquals("Venenatis spiderling", CollectionLogItemAliases.aliasItemName("pet venenatis"));
    }

    @Test
    public void aliasItemName_backupFuzzyMatch_stripsPunctuation() {
        // This used to return "Skeleton champ*ion* scroll" to match vet'*ion*
        assertEquals("Skull of vet'ion", CollectionLogItemAliases.aliasItemName("vetion skull"));

        assertEquals("Rune helm (h5)", CollectionLogItemAliases.aliasItemName("rune helm 5"));
    }

    @Test
    public void aliasItemName_backupFuzzyMatch_aFewPopularItems() {
        assertEquals("Enhanced crystal weapon seed", CollectionLogItemAliases.aliasItemName("enh crystal weapon seed"));
        assertEquals("Enhanced crystal weapon seed", CollectionLogItemAliases.aliasItemName("enh weapon seed"));
        assertEquals("Enhanced crystal weapon seed", CollectionLogItemAliases.aliasItemName("enhanced weapon seed"));
    }

    @Test
    public void aliasItemName_backupFuzzyMatch_horribleSpellingErrors() {
        assertEquals("Celestial ring (uncharged)", CollectionLogItemAliases.aliasItemName("celstal ring"));
        assertEquals("Angler boots", CollectionLogItemAliases.aliasItemName("aglar bots"));
        assertEquals("Awakener's orb", CollectionLogItemAliases.aliasItemName("Awakner ORB"));
        assertEquals("Little nightmare", CollectionLogItemAliases.aliasItemName("mini nightmare"));
    }

    @Test
    @Ignore("Slow test - manual run only")
    public void aliasItemName_backupFuzzyMatch_stableToSmallPerturbations_charReplace() {
        Random random = new Random(1234);
        for (LogItemInfo logItemInfo : LogItemInfo.getAllLogItemInfos()) {
            String name = logItemInfo.getItemName();

            // make sure the space can't be replaced
            String nameWithExtraSpace = name.replaceAll(" ", "  ");

            int editIndex = random.nextInt(nameWithExtraSpace.length());

            // Skip items like Odium Shard 1, Adamant platebody (g), heraldic helms (h3), etc. that can't tolerate
            // a single-character replacement
            List<Character> bannedReplacementCharacters = ImmutableList.of('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'h', 'g', 't');
            if (bannedReplacementCharacters.stream()
                    .anyMatch(c -> c == nameWithExtraSpace.charAt(editIndex))) {
                continue;
            }
            String perturbedName = nameWithExtraSpace.substring(0, editIndex) + "X" + nameWithExtraSpace.substring(editIndex+1);

            assertEquals("Failed match with perturbation: " + perturbedName, name, CollectionLogItemAliases.aliasItemName(perturbedName));
        }
    }

    @Test
    @Ignore("Slow test - manual run only")
    public void aliasItemName_backupFuzzyMatch_stableToSmallPerturbations_charDelete() {
        Random random = new Random(2576);
        for (LogItemInfo logItemInfo : LogItemInfo.getAllLogItemInfos()) {
            String name = logItemInfo.getItemName();

            // make sure the space can't be replaced
            String nameWithExtraSpace = name.replaceAll(" ", "  ");

            int editIndex = random.nextInt(nameWithExtraSpace.length());

            // Skip items like Odium Shard 1, Adamant platebody (g), heraldic helms (h3), etc. that can't tolerate
            // a single-character replacement
            List<Character> bannedReplacementCharacters = ImmutableList.of('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'h', 'g', 't');
            if (bannedReplacementCharacters.stream()
                    .anyMatch(c -> c == nameWithExtraSpace.charAt(editIndex))) {
                continue;
            }
            String perturbedName = nameWithExtraSpace.substring(0, editIndex) + nameWithExtraSpace.substring(editIndex+1);

            assertEquals("Failed match with perturbation: " + perturbedName, name, CollectionLogItemAliases.aliasItemName(perturbedName));
        }
    }

    @Test
    @Ignore("Slow test - manual run only")
    public void aliasItemName_backupFuzzyMatch_stableToSmallPerturbations_charAdd() {
        Random random = new Random(1357);
        for (LogItemInfo logItemInfo : LogItemInfo.getAllLogItemInfos()) {
            String name = logItemInfo.getItemName();

            int editIndex = random.nextInt(name.length());
            String perturbedName = name.substring(0, editIndex) + "X" + name.substring(editIndex);

            assertEquals("Failed match with perturbation: " + perturbedName, name, CollectionLogItemAliases.aliasItemName(perturbedName));
        }
    }

}
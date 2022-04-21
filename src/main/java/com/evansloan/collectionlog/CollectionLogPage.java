package com.evansloan.collectionlog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@AllArgsConstructor
public class CollectionLogPage
{
    @Getter
    private final String name;

    @Getter
    private final List<CollectionLogItem> items;

    @Getter
    private final List<CollectionLogKillCount> killCounts;

    public static String aliasPageName(String pageName)
    {
        switch (pageName.toLowerCase())
        {
            // Bosses Tab
            case "sire":
            case "abyssal sire":
                return "Abyssal Sire";
            case "hydra":
            case "alchemical hydra":
                return "Alchemical Hydra";
            case "byro":
            case "bryophyta":
                return "Bryophyta";
            case "barrows":
            case "barrows chests":
                return "Barrows Chests";
            case "callisto":
                return "Callisto";
            case "cerberus":
            case "cerb":
                return "Cerberus";
            case "chaos elemental":
            case "chaos ele":
                return "Chaos Elemental";
            case "chaos fanatic":
                return "Chaos Fanatic";
            case "commander zilyana":
            case "sara":
            case "saradomin":
            case "zilyana":
            case "zily":
                return "Commander Zilyana";
            case "corporeal beast":
            case "corp":
                return "Corporeal Beast";
            case "crazy archaeologist":
            case "crazy arch":
                return "Crazy Archaeologist";
            case "dagannoth kings":
            case "dks":
                return "Dagannoth Kings";
            case "the fight caves":
            case "jad":
            case "fight caves":
            case "fc":
                return "The Fight Caves";
            case "the gauntlet":
            case "gaunt":
            case "gauntlet":
            case "cgaunt":
            case "cgauntlet":
            case "the corrupted gauntlet":
            case "cg":
                return "The Gauntlet";
            case "giant mole":
            case "mole":
                return "Giant Mole";
            case "general graardor":
            case "bando":
            case "bandos":
            case "graardor":
                return "General Graardor";
            case "grotesque guardians":
            case "dusk":
            case "dawn":
            case "gargs":
            case "ggs":
            case "gg":
                return "Grotesque Guardians";
            case "hespori":
                return "Hespori";
            case "the inferno":
            case "zuk":
            case "inferno":
                return "The Inferno";
            case "kalphite queen":
            case "kq":
                return "Kalphite Queen";
            case "king black dragon":
            case "kbd":
                return "King Black Dragon";
            case "kraken":
            case "cave kraken":
                return "Kraken";
            case "kree'arra":
            case "arma":
            case "kree":
            case "kreearra":
            case "armadyl":
                return "Kree'arra";
            case "k'ril tsutsaroth":
            case "zammy":
            case "zamorak":
            case "kril":
            case "kril trutsaroth":
                return "K'ril Tsutsaroth";
            case "nex":
                return "Nex";
            case "the nightmare":
            case "nightmare":
            case "nm":
            case "tnm":
            case "nmare":
            case "pnm":
            case "phosani":
            case "phosanis":
            case "phosani nm":
            case "phosani nightmare":
            case "phosanis nightmare":
                return "The Nightmare";
            case "obor":
                return "Obor";
            case "sarachnis":
                return "Sarachnis";
            case "scorpia":
                return "Scorpia";
            case "skotizo":
                return "Skotizo";
            case "tempoross":
            case "temp":
                return "Tempoross";
            case "thermonuclear smoke devil":
            case "smoke devil":
            case "thermy":
                return "Thermonuclear Smoke Devil";
            case "venenatis":
            case "vene":
                return "Venenatis";
            case "vet'ion":
            case "vetion":
                return "Vet'ion";
            case "vorkath":
            case "vork":
                return "Vorkath";
            case "wintertodt":
            case "wt":
            case "todt":
                return "Wintertodt";
            case "zalcano":
            case "zalc":
                return "Zalcano";
            case "zulrah":
            case "zul":
                return "Zulrah";

            // Raids Tab
            case "cox":
            case "xeric":
            case "chambers":
            case "olm":
            case "raids":
            case "cox cm":
            case "xeric cm":
            case "chambers cm":
            case "olm cm":
            case "raids cm":
            case "chambers of xeric - challenge mode":
                return "Chambers of Xeric";
            case "tob":
            case "theatre":
            case "verzik":
            case "verzik vitur":
            case "raids 2":
            case "theatre of blood: story mode":
            case "tob sm":
            case "tob story mode":
            case "tob story":
            case "Theatre of Blood: Entry Mode":
            case "tob em":
            case "tob entry mode":
            case "tob entry":
            case "theatre of blood: hard mode":
            case "tob cm":
            case "tob hm":
            case "tob hard mode":
            case "tob hard":
            case "hmt":
                return "Theatre of Blood";

            // Clues Tab
            case "beginner treasure trails":
            case "begs":
            case "beg clues":
            case "beginners":
            case "beginner clues":
            case "beginner clue":
                return "Beginner Treasure Trails";
            case "easy treasure trails":
            case "easy clues":
            case "easy clue":
            case "easies":
                return "Easy Treasure Trails";
            case "medium treasure trails":
            case "meds":
            case "mediums":
            case "medium clues":
            case "medium clue":
                return "Medium Treasure Trails";
            case "hard treasure trails":
            case "hards":
            case "hard clues":
            case "hard clue":
                return "Hard Treasure Trails";
            case "master treasure trails":
            case "masters":
            case "master clues":
            case "master clue":
                return "Master Treasure Trails";
            case "elite treasure trails":
            case "elites":
            case "elite clues":
            case "elite clue":
                return "Elite Treasure Trails";
            case "hard treasure trails (rare)":
            case "hards rare":
            case "hard clues rare":
            case "hard clue rare":
            case "hards rares":
            case "hard clues rares":
            case "hard clue rares":
            case "rare hards":
            case "rare hard":
                return "Hard Treasure Trails (Rare)";
            case "elite treasure trails (rare)":
            case "elites rare":
            case "elite clues rare":
            case "elite clue rare":
            case "elites rares":
            case "elite clues rares":
            case "elite clue rares":
            case "rare elite":
            case "rare elites":
                return "Elite Treasure Trails (Rare)";
            case "master treasure trails (rare)":
            case "master treasure trails rare":
            case "masters rare":
            case "master clues rare":
            case "master clue rare":
            case "master treasure trails rares":
            case "masters rares":
            case "master clues rares":
            case "master clue rares":
            case "rare master":
            case "rare masters":
                return "Master Treasure Trails (Rare)";
            case "shared treasure trail rewards":
            case "shared rewards":
            case "shared clue":
            case "shared clues":
            case "shared":
                return "Shared Treasure Trail Rewards";

            // Minigames Tab
            case "barbarian assault":
            case "ba":
            case "barb":
                return "Barbarian Assault";
            case "brimhaven agility arena":
            case "brimhaven agility":
            case "brimhaven":
            case "brim agility":
            case "brim":
                return "Brimhaven Agility Arena";
            case "castle wars":
            case "cw":
            case "cwars":
                return "Castle Wars";
            case "fishing trawler":
            case "trawler":
                return "Fishing Trawler";
            case "gnome restaurant":
            case "gnome":
            case "restaurant":
                return "Gnome Restaurant";
            case "guardians of the rift":
            case "guardian of the rift":
            case "gotr":
            case "grift":
            case "grifting":
            case "guardians":
            case "runetodt":
                return "Guardians of the Rift";
            case "hallowed sepulchre":
            case "hs":
            case "sepulchre":
                return "Hallowed Sepulchre";
            case "last man standing":
            case "lms":
                return "Last Man Standing";
            case "magic training arena":
            case "mta":
                return "Magic Training Arena";
            case "mahogany homes":
            case "mah homes":
            case "mh":
                return "Mahogany Homes";
            case "pest control":
            case "pc":
                return "Pest Control";
            case "rogues' den":
            case "rogues den":
            case "rogue den":
            case "rogues":
            case "rogue":
                return "Rogues' Den";
            case "shades of mort'ton":
            case "shades of mortton":
            case "shades":
                return "Shades of Mort'ton";
            case "soul wars":
            case "soul war":
            case "sw":
                return "Soul Wars";
            case "temple trekking":
            case "trekking":
                return "Temple Trekking";
            case "tithe farm":
            case "tithe":
                return "Tithe Farm";
            case "trouble brewing":
            case "brewing":
                return "Trouble Brewing";
            case "volcanic mine":
            case "vm":
                return "Volcanic Mine";

            // Other Tab
            case "aerial fishing":
            case "aerial fish":
                return "Aerial Fishing";
            case "all pets":
            case "pets":
            case "pet":
                return "All Pets";
            case "camdozaal":
                return "Camdozaal";
            case "champion's challenge":
            case "champions challenge":
            case "champion challenge":
            case "champion scrolls":
            case "champ scrolls":
                return "Champion's Challenge";
            case "chaos druids":
                return "Chaos Druids";
            case "chompy bird hunting":
            case "chompy":
            case "chompies":
                return "Chompy Bird Hunting";
            case "creature creation":
            case "tower of life":
                return "Creature Creation";
            case "cyclopes":
            case "defenders":
            case "defender":
                return "Cyclopes";
            case "fossil island notes":
            case "fossil island note":
            case "fossil island":
                return "Fossil Island Notes";
            case "glough's experiments":
            case "gloughs experiments":
            case "demonics":
            case "demonic gorilla":
            case "demonic gorillas":
                return "Glough's Experiments";
            case "monkey backpacks":
            case "monkey backpack":
            case "ape atoll":
            case "backpack":
            case "backpacks":
                return "Monkey Backpacks";
            case "motherlode mine":
            case "motherlode":
            case "mm":
                return "Motherlode Mine";
            case "my notes":
            case "notes":
                return "My Notes";
            case "random events":
            case "random event":
            case "random":
            case "randoms":
                return "Random Events";
            case "revenants":
            case "revs":
                return "Revenants";
            case "rooftop agility":
            case "rooftop":
            case "rooftops":
            case "graceful":
                return "Rooftop Agility";
            case "shayzien armour":
            case "shayzien":
                return "Shayzien Armour";
            case "shooting stars":
            case "shooting star":
            case "star mining":
            case "ss":
                return "Shooting Stars";
            case "skilling pets":
            case "skilling pet":
            case "skill pets":
            case "skill pet":
                return "Skilling Pets";
            case "slayer":
                return "Slayer";
            case "tzhaar":
            case "tz":
            case "tzh":
                return "TzHaar";
            case "miscellaneous":
            case "misc":
                return "Miscellaneous";

            default:
                return pageName;
        }
    }
}

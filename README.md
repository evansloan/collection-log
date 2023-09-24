
# Collection Log Plugin ![Plugin Installs](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/collection-log) ![Plugin Rank](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/collection-log)

A [Runelite](https://github.com/runelite/runelite) plugin that adds visual improvements to the collection log and integrates with [collectionlog.net](https://collectionlog.net).

## Features
* Upload collection log data to [collectionlog.net](https://collectionlog.net)
* Display total item counts alongside unique item counts
* Display collection log progress as a percentage
* Custom page highlight colors
* Export collection log data to file
* Display collection log data in chat with a command
* Display how lucky or dry you are for most items in the collection log
* Share item counts, collection log progress, luck, etc. with others using a chat command.

## Usage
Open the collection log and click through all the pages to get your total progress. Pages that have not yet been clicked on will be marked with `*`

### Upload to collectionlog.net
1. Enable "Allow collectionlog.net connections" under the "Exporting" section in the plugin settings
2. Log into OSRS with the account you would like to upload your collection log for
3. In the Collection log plugin side panel, follow the "Quick start" instructions in the info tab

### Command
Either type `!log` to display the total unique items obtained or `!log page name` where page name is the name of the collection page.

The command will try to alias common ways to refer to a piece of content to the correct page, but the full page name will also work.

- `!log cox` -> Chambers of Xeric
- `!log wt` -> Wintertodt
- `!log ba` -> Barbarian Assault
- `!log rooftop` -> Rooftop Agility
- `!log pets` -> All Pets
- `!log brim agility` -> Brimhaven Agility Arena
- `!log shared clues` -> Shared Treasure Trail Rewards

<img src="https://i.imgur.com/U2C2t1G.gif" width="500" />

The command output can be filtered by providing specific keywords to the command:
- `obtained`: Show all obtained items for a given page (This filter is used by default when one is not provided)
- `missing`: Show all missing items for a given page
- `dupes`: Show all duplicate obtained items for a given page

Examples:
```
!log obtained sire
!log missing nightmare
!log dupes zulrah
```

### Luck Commands
Type `!log luck` followed by an item name. Alternatively, click "Check" on any item icon in the collection log pane to 
show your luck stats for that item.

<img src="https://i.imgur.com/5pPIw0C.png" width="500" />

The command will try to infer the intended item name if you abbreviate or misspell it.

<img src="https://i.imgur.com/AHNv1V0.gif" width="500" />

Examples:
```
!log luck tumeken's shadow
!log luck enhanced weapon seed
!log luck zulrah pet
```

In the future, luck for an entire page or your entire account can be shared rather than single items.

### Luck Configuration

Luck display can be disabled in the plugin configuration.

If you enable detailed luck stats in the plugin configuration, you will see luck and dryness separately. Luck is the
percent of players that you are luckier than. Dryness is the percent of player that you are drier than. By default,
these are combined into a single "overall" luck meter. An overall luck near 50% or luck/dryness near 0 might just mean
you have low KC and luck calculation is unreliable at that point.

The collection log does not have enough information to calculate luck for every item in the game, for example if KC is
not tracked for some minigames or monsters. In some cases, providing additional information can allow a 
fairly accurate calculation. Double check the luck calculation settings for best results:

<img src="https://i.imgur.com/E2z85Ub.png" width="180" />

### Exporting

To save a copy of your collection log in it's current state, right click the collection log item or the collection log section in the character summary tab and select "export" to save your collection log data into a .json file within the Runelite directory.

Save locations:

Windows: `%USERPROFILE%/.runelite/collectionlog/exports/`

Mac/Linux: `$HOME/.runelite/collectionlog/exports/`

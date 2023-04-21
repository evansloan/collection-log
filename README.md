
# Collection Log Plugin ![Plugin Installs](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/collection-log) ![Plugin Rank](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/collection-log)

A [Runelite](https://github.com/runelite/runelite) plugin that adds visual improvements to the collection log and integrates with [collectionlog.net](https://collectionlog.net).

## Features
* Upload collection log data to [collectionlog.net](https://collectionlog.net)
* Display total item counts alongside unique item counts
* Display collection log progress as a percentage
* Custom page highlight colors
* Export collection log data to file
* Display collection log data in chat with a command

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

![](https://i.imgur.com/U2C2t1G.gif)

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

### Exporting

To save a copy of your collection log in it's current state, right click the collection log item or the collection log section in the character summary tab and select "export" to save your collection log data into a .json file within the Runelite directory.

Save locations:

Windows: `%USERPROFILE%/.runelite/collectionlog/exports/`

Mac/Linux: `$HOME/.runelite/collectionlog/exports/`

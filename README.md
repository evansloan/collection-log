
# Collection Log Plugin ![Plugin Installs](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/collection-log) ![Plugin Rank](https://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/collection-log)

A [Runelite](https://github.com/runelite/runelite) plugin that adds visual improvements to the collection log.

![](https://i.imgur.com/XsIk092.png)

## Features
* Display total item counts alongside unique item counts
* Display collection log progress as a percentage
* Custom completed category colors
* Export collection log data to file
* Upload collection log data to [collectionlog.net](https://collectionlog.net)
* Display collection log data in chat with a command

## Usage
Open the collection log and click through all the categories to get your total progress.

If the "Upload collection log data to collectionlog.net" config is enabled, your collection log data will be shared on logout to [collectionlog.net](https://collectionlog.net).

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

### Exporting

![](https://i.imgur.com/zz90og4.png)

Right click the collection log item or the collection log section in the character summary tab and select "export" to save your collection log data into a .json file within the Runelite directory.

Windows location: `%USERPROFILE%/.runelite/collectionlog/exports/`

Mac/Linux location: `$HOME/.runelite/collectionlog/exports/`

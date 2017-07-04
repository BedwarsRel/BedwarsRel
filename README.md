[![CircleCI](https://circleci.com/gh/BedwarsRel/BedwarsRel.svg?style=shield)](https://circleci.com/gh/BedwarsRel/BedwarsRel)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df02ec6676b8457aa7fcb4be1ba44729)](https://www.codacy.com/app/BedwarsRel/BedwarsRel)
[![Gitter](https://badges.gitter.im/BedwarsRel/BedwarsRel.svg)](https://gitter.im/BedwarsRel/BedwarsRel)

# BedwarsRel - Minecraft Bedwars Plugin
![](https://github.com/BedwarsRel/BedwarsRel/blob/master/logo.png)

Bedwars is a [Minecraft](http://www.minecraft.net) minigame where teams (max. 15) try to destroy the other teams' beds. But there is more: On the whole map there are ressource spawners spawning items. Some items are more worth than others and with these you can buy different things in the so called "Villager Shop". You will find powerful weapons or potions or simple blocks to get to the other bases. Get to the others bases? Yes, every team starts on an island and you have to get to the others with blocks which you can buy in the "Villager Shop". When you meet a enemy you have to try to kill him so he'll lose all the equipment which he had in his inventory. As soon as a team's bed is destroyed they cannot respawn again and last team standing wins.

# Server Owners
For general information about BedwarsRel **[go to www.spigotmc.org](https://www.spigotmc.org/resources/bedwars-rel.6799/)**. For more information about configuring and running the plugin on your server, please **[have a look on the wiki](https://github.com/BedwarsRel/BedwarsRel/wiki)**.

If you are experiencing any problems with BedwarsRel, please open a new issue. Remember the content of the wiki-page **["HowTo: Write an issue"](https://github.com/BedwarsRel/BedwarsRel/wiki/HowTo:-Write-an-Issue)** you already read before.

BedwarsRel is compatible with `CraftBukkit/Spigot 1.8 - 1.8.8`, `CraftBukkit/Spigot 1.9 - 1.9.4`, `CraftBukkit/Spigot 1.10 - 1.10.2`, `CraftBukkit/Spigot 1.11 - 1.11.2` and `CraftBukkit/Spigot 1.12`.

# Developers
## Contributing to BedwarsRel
If you would like to contribute to this repository, feel free to [fork the repo](https://help.github.com/articles/fork-a-repo/) and then [create a pull request](https://help.github.com/articles/creating-a-pull-request/) to our current `dev` branch. This project uses [Project Lombok](https://projectlombok.org), so you will need to have this [installed in your IDE](https://projectlombok.org/download.html). For code formatting, we recommand using the [Google Java Style](https://google.github.io/styleguide/javaguide.html) ([Eclipse Profile](https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml)).

## Implementing BedwarsRel
```xml
<dependency>
  <groupId>io.github.bedwarsrel</groupId>
  <artifactId>BedwarsRel</artifactId>
  <version>1.3.7</version>
</dependency>
```
package io.github.yannici.bedwarsreloaded;

import io.github.yannici.bedwarsreloaded.Game.TeamColor;
import org.bukkit.DyeColor;

public class ColorConverter {

    public ColorConverter() {
        super();
    }

    public static DyeColor teamColorToDye(TeamColor col) {
        for(DyeColor color : DyeColor.values()) {
            if(col.toString().equals(color.toString())) {
                return color;
            }
        }

        return DyeColor.WHITE;
    }

}

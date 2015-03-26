package io.github.yannici.bedwarsreloaded;

import java.util.ArrayList;

public final class Utils {

    public static String implode(String glue, ArrayList<String> strings) {
        if(strings.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(strings.remove(0));

        for(String str : strings) {
            builder.append(glue);
            builder.append(str);
        }

        return builder.toString();
    }
    
    public static boolean isNumber(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }

}

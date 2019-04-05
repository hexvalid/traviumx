package com.traviumx.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static int ParseInt(String s) {

        Pattern p = Pattern.compile("-?[1-9]\\d*|0");
        Matcher m = p.matcher(s.replaceAll("−‭", "-"));
        if (m.find())
            return Integer.parseInt(m.group(0));
        else {
            return 0;
        }
    }
}

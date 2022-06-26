package com.funnyboyroks;

import com.funnyboyroks.painter.RegionDrawer;

import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class Main {

    private static final String USAGE = "Usage: WorldPainter.jar <region_size|chunk_size|chunk_time> <hue|alpha> <#bgHex|transparent> <regionDirectories>";

    public static void main(String[] args) {

        if (
            args.length < 4
            || DrawType.get(args[0]) == null
            || !(args[1].equalsIgnoreCase("hue") || args[1].equalsIgnoreCase("alpha"))
        ) {
            System.err.println(USAGE);
            System.exit(1);
        }

        if (args[2].startsWith("#")) {
            try {
                Integer.parseInt(args[2].substring(1), 16);
            } catch (NumberFormatException ex) {
                System.err.println(USAGE);
                System.exit(1);
            }
        } else if (!args[2].equalsIgnoreCase("transparent")) {
            System.err.println(USAGE);
            System.exit(1);
        }

        File file = new File(args[3]);
        if (!file.exists() || !file.isDirectory()) {
            System.err.println(USAGE);
            System.exit(1);
        }

        RegionDrawer regionDrawer = new RegionDrawer(file, args[1].equalsIgnoreCase("alpha") ? RegionDrawer.ColorMode.ALPHA : RegionDrawer.ColorMode.HUE, 100);
        regionDrawer.fillBackground = args[2].equalsIgnoreCase("transparent") ? null : new Color(Integer.parseInt(args[2].substring(1), 16));
        switch (DrawType.get(args[0])) {
            case CHUNK_SIZE -> regionDrawer.drawChunkSize();
            case CHUNK_TIME -> regionDrawer.drawChunkTimestamp();
            case REGION_SIZE -> regionDrawer.drawFileSize();
        }
        String key = regionDrawer.saveImage();
        System.out.println("Image saved to https://bytebin.lucko.me/" + key);


    }


    public enum DrawType {
        REGION_SIZE, CHUNK_SIZE, CHUNK_TIME;

        public static DrawType get(String name) {
            return Arrays.stream(values()).filter(n -> n.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }
}
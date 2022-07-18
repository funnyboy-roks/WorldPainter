package com.funnyboyroks;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DrawOne {

    public static void run(String[] argsArr) {
        Queue<String> args = new LinkedList<>(List.of(argsArr));

        DrawType type = null;
        RegionDrawer.ColorMode colourMode = null;
        Color background = null;
        int worldborder = -1;
        File file = null;

        try {
            type = DrawType.get(args.poll());

            colourMode = args.poll().equalsIgnoreCase("alpha") ? RegionDrawer.ColorMode.ALPHA : RegionDrawer.ColorMode.HUE;

            background = args.peek().startsWith("#") ? new Color(Integer.parseInt(args.peek().substring(1), 16)) : null;
            args.poll();

            if (args.peek().matches("\\d+")) {
                worldborder = Integer.parseInt(args.poll());
            }

            file = new File(args.poll());
        } catch (NullPointerException ex) {
            System.err.println("Usage: WorldPainter.jar 1 <region|chunk> <hue|alpha> <#bgHex|transparent> [worldborder] <regionDirectory>");
            System.exit(1);
        }

        RegionDrawer regionDrawer = new RegionDrawer(file, colourMode, worldborder);
        regionDrawer.fillBackground = background;
        switch (type) {
            case CHUNK -> regionDrawer.drawChunkSize();
            case REGION -> regionDrawer.drawFileSize();
        }

        String key = regionDrawer.saveImage();
        if (key != null) {
            System.out.println("https://bytebin.lucko.me/" + key);
        }
    }


    public enum DrawType {
        REGION, CHUNK;

        public static DrawType get(String name) {
            return Arrays.stream(values()).filter(n -> n.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }
}
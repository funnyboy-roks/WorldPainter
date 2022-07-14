package com.funnyboyroks;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Main {

    private static final String USAGE = "Usage: WorldPainter.jar <region|chunk> <hue|alpha> <#bgHex|transparent> [worldborder] <regionDirectory>";

    public static void main(String[] argsArr) {
        if (argsArr.length > 1 && argsArr[0].equals("2")) {
            String[] args2 = new String[argsArr.length - 1];
            System.arraycopy(argsArr, 1, args2, 0, args2.length);
            try {
                Main2.main(args2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
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

            try {
                worldborder = Integer.parseInt(args.peek());
                args.poll();
            } catch (Exception ignored) {
            }

            file = new File(args.poll());
        } catch (NullPointerException ex) {
            System.err.println(USAGE);
            ex.printStackTrace();
            System.exit(1);
        }

        RegionDrawer regionDrawer = new RegionDrawer(file, colourMode, worldborder);
        regionDrawer.fillBackground = background;
        switch (type) {
            case CHUNK -> regionDrawer.drawChunkSize();
            case REGION -> regionDrawer.drawFileSize();
        }

//        String key = regionDrawer.saveImage();
//        System.out.println("Image saved to https://bytebin.lucko.me/" + key);

        regionDrawer.saveImage("draw-" + file.getName());


    }


    public enum DrawType {
        REGION, CHUNK;

        public static DrawType get(String name) {
            return Arrays.stream(values()).filter(n -> n.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }
}
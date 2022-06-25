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

        var regionDrawer = new RegionDrawer(file, args[1].equalsIgnoreCase("alpha") ? RegionDrawer.ColorMode.ALPHA : RegionDrawer.ColorMode.HUE, 100);
        regionDrawer.fillBackground = args[2].equalsIgnoreCase("transparent") ? null : new Color(Integer.parseInt(args[2].substring(1), 16));
        switch (DrawType.get(args[0])) {
            case CHUNK_SIZE -> regionDrawer.drawChunkSize();
            case CHUNK_TIME -> regionDrawer.drawChunkTimestamp();
            case REGION_SIZE -> regionDrawer.drawFileSize();
        }
        var key = regionDrawer.saveImage();
        System.out.println("Image saved to https://bytebin.lucko.me/" + key);


//        List<RegionDrawer> drawers = new ArrayList<>();
//
//        for (int i = 3; i < args.length; i++) {
//            File file = new File(args[i]);
//
//            drawers.add(new RegionDrawer(file, args[1].equalsIgnoreCase("alpha") ? RegionDrawer.ColorMode.ALPHA : RegionDrawer.ColorMode.HUE, 100))
//        }
//
//        var images = drawers
//            .stream()
//            .map(d -> switch (DrawType.get(args[0])) {
//                case CHUNK_SIZE -> d.drawChunkSize();
//                case CHUNK_TIME -> d.drawChunkTimestamp();
//                case REGION_SIZE -> d.drawFileSize();
//            })
//            .toList();
//
//        var maxHeight = images.stream().mapToInt(BufferedImage::getHeight).max().orElseThrow();
//        var maxWidth = images.stream().mapToInt(BufferedImage::getHeight).max().orElseThrow();

//        var directory = new File("/home/funnyboy_roks/Documents/temp/fl/1.19-Update/region-stuff/fl-regions/region");
//
//        RegionDrawer draw = new RegionDrawer(directory, RegionDrawer.ColorMode.ALPHA, 100);
////        draw.fillBackground = Color.BLACK;
//        draw.padding = 10;
//
//        draw.colorMode = RegionDrawer.ColorMode.HUE;
//        BufferedImage fileSize = draw.drawChunkSize();
//        draw.saveImage("hue");
//        System.out.println("Hue drawn");
//
//        draw.colorMode = RegionDrawer.ColorMode.ALPHA;
//        draw.drawChunkSize();
//        draw.saveImage("alpha");
//        System.out.println("Alpha Drawn");
//
////        System.out.println("https://bytebin.lucko.me/" + saveKey);

    }


    public enum DrawType {
        REGION_SIZE, CHUNK_SIZE, CHUNK_TIME;

        public static DrawType get(String name) {
            return Arrays.stream(values()).filter(n -> n.name().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }
}
package com.funnyboyroks;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.function.Function;

public class Main2 {

    private static final String USAGE = "Usage: WorldPainter.jar <regionDirectories...>";

    public static void main(String[] argsArr) throws IOException {
//        argsArr = new String[]{
//            "/home/funnyboy_roks/servers/1.19/Paper/world/region", "25000",
////            "/home/funnyboy_roks/Documents/fl-server/regions/world", "25000", "#005207",
//            "/home/funnyboy_roks/servers/1.19/Paper/pocket/region", "5000", "#0", //"#370101",
//            "/home/funnyboy_roks/servers/1.19/Paper/world_the_end/DIM1/region", "5000", "#0f0022",
//            };

        Queue<String> args = new LinkedList<>(List.of(argsArr));
        List<RegionDrawer> drawers = new ArrayList<>();
        RegionDrawer.ColorMode colourMode = RegionDrawer.ColorMode.HUE;

        while (!args.isEmpty()) {
            File file = new File(args.poll());
            int worldBorder = Integer.parseInt(args.poll());
            int background = 0;
            if (!args.isEmpty() && args.peek().startsWith("#")) {
                background = Integer.parseInt(args.poll().substring(1), 16);

            }
            var drawer = new RegionDrawer(file, colourMode, worldBorder);
            drawer.fillBackground = new Color(background);
            drawers.add(drawer);
        }

        drawers.forEach(RegionDrawer::parseRegions);
        MinMax chunkSizes = new MinMax(drawers.stream().map(d -> d.chunkSizes).toArray(MinMax[]::new));
        for (RegionDrawer drawer : drawers) drawer.chunkSizes = chunkSizes;

        System.out.println("Drawing...");
        drawers.forEach(RegionDrawer::drawChunkSize);

        System.out.println("Merging...");
        int height = drawers.stream().mapToInt(d -> d.image.getWidth()).max().orElseThrow();
        int width = height * drawers.size();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        canvas.fillRect(0, 0, image.getWidth(), image.getHeight());

        for (int i = 0; i < drawers.size(); i++) {
            RegionDrawer d = drawers.get(i);

            canvas.setColor(d.fillBackground);
            canvas.fillRect(i * height, 0, height, height);

            int x = i * height + height / 2;
            int y = height / 2;

            x -= d.image.getWidth() / 2;
            y -= d.image.getHeight() / 2;

            canvas.drawImage(
                d.image,
                x, y,
                x + d.image.getWidth(), y + d.image.getHeight(),
                0, 0,
                d.image.getWidth(), d.image.getHeight(),
                null);
        }
        System.out.println("Uploading...");
        String key = RegionDrawer.uploadImage(image);
        System.out.println("https://bytebin.lucko.me/" + key);
//        ImageIO.write(image, "png", new File(drawers.size() + "-worlds-chunks.png"));


    }

}
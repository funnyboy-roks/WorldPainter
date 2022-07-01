package com.funnyboyroks.painter;

import com.funnyboyroks.parser.Chunk;
import com.funnyboyroks.parser.Region;
import com.funnyboyroks.parser.Util;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RegionDrawer {

    public final List<File>    regionFiles;
    public       List<Region>  regions;
    public final String        name;
    public       BufferedImage image;
    public       Graphics2D    canvas;
    public       int           regionSize;
    public       ColorMode     colorMode;
    public       Color         fillBackground = null;
    public       int           padding        = 5; // regions of padding on the image


    public RegionDrawer(File inputDirectory, ColorMode colorMode, int maxRadius) {
        if (!inputDirectory.isDirectory()) {
            throw new RuntimeException("inputDirectory is not a directory");
        }
        File[] files = inputDirectory.listFiles((f, s) -> s.matches("r\\.(-?\\d+)\\.(-?\\d+)\\.mca$"));
        this.name = inputDirectory.getName();
        if (files == null) throw new RuntimeException();
        this.regionFiles = Arrays.stream(files).toList();

        this.regions = null;

        this.regionSize = this.regionFiles.stream()
            .map(File::getName)
            .mapToInt(s -> {
                String[] parts = s.split("\\.");
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);
                return Math.max(Math.abs(x), Math.abs(z));
            })
            .filter(n -> n < maxRadius)
            .max()
            .orElseThrow();

        this.colorMode = colorMode;

    }

    private void parseRegions() {
        if (this.regions != null) {
            return;
        }
        try (
            ProgressBar pb = new ProgressBarBuilder()
                .setMaxRenderedLength(150)
                .setTaskName("Reading directory " + this.name + "/")
                .setInitialMax(this.regionFiles.size())
                .build()
        ) {
            this.regions = this.regionFiles.stream().map(file -> {
                pb.step();
                try {
                    return Region.from(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).toList();
        }

    }

    public String saveImage() {
        return this.saveImage(null);
    }

    public String saveImage(String name) {
        try {
            if (name != null) throw new RuntimeException("Bad Code, but who gives a shit");
            URL url = new URL("https://bytebin.lucko.me/post");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestProperty("Content-Type", "image/png");
            http.setRequestProperty("User-Agent", "WorldPainter");
            ImageIO.write(image, "png", http.getOutputStream());

            String responseJson = new String(http.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return responseJson.substring(8, responseJson.length() - 2);


        } catch (Exception e) {
            if (name == null) {
                e.printStackTrace();
                System.err.println("Unable to contact bytebin! Saving image as " + name + ".png");
            }
            try {
                ImageIO.write(this.image, "png", new File(name + ".png"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }
    }

    private void point(Color colour, int x, int y) {

        x += this.image.getWidth() / 2;
        y += this.image.getHeight() / 2;

        Color prev = this.canvas.getColor();
        this.canvas.setColor(colour);
        this.canvas.drawRect(x, y, 1, 1);
        this.canvas.setColor(prev);

    }

    public BufferedImage drawFileSize() {
        this.createImage((this.regionSize + padding) * 2, (this.regionSize + padding) * 2);

        long minSize = Integer.MAX_VALUE;
        long maxSize = 0;

        for (File f : regionFiles) {
            if (f.length() > maxSize) maxSize = f.length();
            if (f.length() < minSize) minSize = f.length();
        }

        for (File f : regionFiles) {
            String[] parts = f.getName().split("\\.");
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            switch (colorMode) {
                case HUE -> point(Color.getHSBColor(Util.map(f.length(), minSize, maxSize, .4f, 0), 1, 1), x, z);
                case ALPHA -> point(new Color(255, 0, 0, (int) Util.map(f.length(), minSize, maxSize, 1, 255)), x, z);
            }
        }


        return this.image;
    }

    public BufferedImage drawChunkSize() {
        this.parseRegions();
        this.createImage((this.regionSize + padding) * 2 * 32, (this.regionSize + padding) * 2 * 32);

        long minSize = Integer.MAX_VALUE;
        long maxSize = 0;

        for (Region r : regions) {
            long size = Arrays.stream(r.chunks).filter(Objects::nonNull).mapToLong(c -> c.length).max().orElseThrow();
            if (size > maxSize) maxSize = size;
            if (size < minSize) minSize = size;
        }

        for (Region r : regions) {
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    Chunk chunk = r.getChunk(x, z);
                    if (chunk == null) continue;
                    long len = Math.min(10000, chunk.length);
                    switch (colorMode) {
                        case HUE ->
                            point(Color.getHSBColor(Util.map(len, minSize, 10000, 0.25f, 0), 1, 1), r.x * 32 + x, r.z * 32 + z);
                        case ALPHA ->
                            point(new Color(255, 0, 0, (int) Util.map(len, minSize, 10000, 1, 255)), r.x * 32 + x, r.z * 32 + z);
                    }
                }
            }
        }


        return this.image;
    }

    public BufferedImage drawChunkTimestamp() {
        this.parseRegions();
        this.createImage((this.regionSize + padding) * 2 * 32, (this.regionSize + padding) * 2 * 32);

        long minTS = Integer.MAX_VALUE;
        long maxTS = 0;

        for (Region r : regions) {
            long size = Arrays.stream(r.chunks).filter(Objects::nonNull).mapToLong(c -> c.timestamp).max().orElseThrow();
            if (size > maxTS) maxTS = size;
            if (size < minTS) minTS = size;
        }

        for (Region r : regions) {
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    Chunk chunk = r.getChunk(x, z);
                    if (chunk == null) continue;
                    switch (colorMode) {
                        case HUE ->
                            point(Color.getHSBColor(Util.map(chunk.timestamp, minTS, maxTS, 0.5f, 0), 1, 1), r.x * 32 + x, r.z * 32 + z);
                        case ALPHA ->
                            point(new Color(255, 0, 0, (int) Util.map(chunk.timestamp, minTS, maxTS, 1, 255)), r.x * 32 + x, r.z * 32 + z);
                    }
                }
            }
        }


        return this.image;
    }

    private void createImage(int width, int height) {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.canvas = this.image.createGraphics();
        if (this.fillBackground != null) {
            this.canvas.setColor(this.fillBackground);
            this.canvas.fillRect(0, 0, width, height);
        }
    }

    public enum ColorMode {
        HUE,
        ALPHA,
    }

}

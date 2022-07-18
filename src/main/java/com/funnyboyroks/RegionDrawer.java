package com.funnyboyroks;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

public class RegionDrawer {

    private static final int TERMINAL_WIDTH;

    static {
        try {
            TERMINAL_WIDTH = Integer.parseInt(new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{ "bash", "-c", "tput cols 2> /dev/tty" }).getInputStream())).readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final File[]        regionFiles;
    public final File          folder;
    public final int           regionSize;
    public       Region[]      regions;
    public       BufferedImage image;
    public       Graphics2D    canvas;
    public       ColorMode     colorMode;
    public       Color         fillBackground = null;
    public       int           padding        = 5; // regions of padding on the image
    public       int           worldBorder;
    public       MinMax        chunkSizes;

    public RegionDrawer(File inputDirectory, ColorMode colorMode, int worldBorder) {
        this.worldBorder = worldBorder;
        this.folder = inputDirectory;
        this.regions = null;

        if (!this.folder.isDirectory()) throw new RuntimeException(this.folder + " is not a directory");
        File[] files = inputDirectory.listFiles((f, s) -> s.matches("r\\.(-?\\d+)\\.(-?\\d+)\\.mca$"));
        if (files == null) throw new RuntimeException();

        this.regionFiles = files;

        if (worldBorder < 0) {
            this.regionSize = Arrays.stream(this.regionFiles)
                .map(File::getName)
                .mapToInt(s -> {
                    String[] parts = s.split("\\.");
                    int x = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    return Math.max(Math.abs(x), Math.abs(z));
                })
                .max()
                .orElseThrow();
        } else {
            this.regionSize = (int) Math.ceil(worldBorder / 16.0 / 32.0);
        }

        this.colorMode = colorMode;
    }

    public static float map(float n, float start1, float stop1, float start2, float stop2) {
        return ((n - start1) / (stop1 - start1)) * (stop2 - start2) + start2;
    }

    public static String uploadImage(BufferedImage img) {
        try {
            URL url = new URL("https://bytebin.lucko.me/post");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestProperty("Content-Type", "image/png");
            http.setRequestProperty("User-Agent", "WorldPainter");
            http.setRequestProperty("Content-Encoding", "gzip");

            GZIPOutputStream os = new GZIPOutputStream(http.getOutputStream());

            ImageIO.write(img, "png", os);
            os.close();

            String responseJson = new String(http.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return responseJson.substring(8, responseJson.length() - 2);
        } catch (Exception e) {
            e.printStackTrace();
            String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".png";
            System.out.println("Unable to upload image to bytebin, saving file as " + name);
            try {
                ImageIO.write(img, "png", new File(name));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }
    }

    public void parseRegions() {
        if (this.regions != null) return;

        try (
            ProgressBar pb = new ProgressBarBuilder()
                .setMaxRenderedLength(TERMINAL_WIDTH)
                .setTaskName("Reading directory " + this.folder + "/")
                .setInitialMax(this.regionFiles.length)
                .build()
        ) {
            this.regions = new Region[this.regionFiles.length];
            this.chunkSizes = new MinMax();
            for (int i = 0; i < this.regionFiles.length; i++) {
                File file = this.regionFiles[i];
                if (file.length() == 0) continue;
                try {
                    Region region = new Region(file);
                    this.regions[i] = region;
                    this.chunkSizes.add(region.sizes());
                    pb.step();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public String saveImage() {
        return uploadImage(this.image);
    }

    public void saveImage(String name) {
        try {
            ImageIO.write(this.image, "png", new File(name + ".png"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void point(Color colour, int x, int y) {

        x += this.image.getWidth() / 2;
        y += this.image.getHeight() / 2;

        if (x >= this.image.getWidth() || x < 0 || y >= this.image.getHeight() || y < 0) return;

        this.image.setRGB(x, y, colour.getRGB());
    }

    public void drawFileSize() {
        this.createImage((this.regionSize + padding) * 2, (this.regionSize + padding) * 2);

        MinMax sizes = new MinMax(Arrays.stream(regionFiles).mapToLong(File::length).toArray());

        for (File f : regionFiles) {
            String[] parts = f.getName().split("\\.");
            int x = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            switch (colorMode) {
                case HUE -> point(
                    Color.getHSBColor(map(f.length(), sizes.min, sizes.max, .4f, 0), 1, 1),
                    x, z
                );
                case ALPHA -> point(
                    new Color(255, 0, 0, (int) map(f.length(), sizes.min, sizes.max, 1, 255)),
                    x, z
                );
            }
        }

        this.drawWorldBorder(false);
    }

    public void drawChunkSize() {
        this.parseRegions();
        this.createImage((this.regionSize + padding) * 2 * 32, (this.regionSize + padding) * 2 * 32);

        try (
            ProgressBar pb = new ProgressBarBuilder()
                .setMaxRenderedLength(TERMINAL_WIDTH)
                .setTaskName("Drawing Regions from " + this.folder + "/")
                .setInitialMax(this.regionFiles.length)
                .build()
        ) {
            for (Region r : regions) {
                if (r == null) continue;
                pb.step();
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        long chunk = r.chunks()[x * 32 + z];
                        if (chunk == -1) continue;
                        long len = Math.min(this.chunkSizes.max, chunk);
                        switch (colorMode) {
                            case HUE -> point(
                                Color.getHSBColor(map(len, this.chunkSizes.min, this.chunkSizes.max, 0.25f, 0), 1, 1),
                                r.x() * 32 + x, r.z() * 32 + z
                            );
                            case ALPHA -> point(
                                new Color(255, 0, 0, (int) map(len, this.chunkSizes.min, this.chunkSizes.max, 1, 255)),
                                r.x() * 32 + x, r.z() * 32 + z
                            );
                        }
                    }
                }
            }
        }

        this.drawWorldBorder(true);
    }

    private void drawWorldBorder(boolean chunks) {
        if (this.regionSize <= 0) return;
        this.canvas.setColor(
            this.fillBackground == null
                ? Color.BLACK
                : new Color(
                    255 - this.fillBackground.getRed(),
                    255 - this.fillBackground.getGreen(),
                    255 - this.fillBackground.getBlue(),
                    chunks ? 0xff : 0xaa
                )
        );
        int radius = (int) Math.ceil(this.worldBorder / 16.0 / 32.0) * (chunks ? 32 : 1) + 1;
        int centreX = this.image.getWidth() / 2;
        int centreY = this.image.getHeight() / 2;
        this.canvas.drawRect(
            centreX - radius - 1,
            centreY - radius - 1,
            radius * 2 + 2,
            radius * 2 + 2
        );
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

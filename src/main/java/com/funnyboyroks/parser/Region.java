package com.funnyboyroks.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Region {

    public final int size;
    public final int  x, z;
    public final Chunk[] chunks;
    
    private Region(int x, int z, int size, Chunk[] chunks) {
        this.x = x;
        this.z = z;
        this.size = size;
        this.chunks = chunks;
    }

    public static Region from(File file) throws IOException {

        String[] parts = file.getName().split("\\.");

        byte[] data = Files.readAllBytes(file.toPath());
        if(data.length == 0) return null;

        return new Region(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), data.length, parseChunks(data));

    }

    public Chunk getChunk(int x, int z) {
        return this.chunks[x * 32 + z];
    }

    private static Chunk[] parseChunks(byte[] input) throws IOException {
        Chunk[] out = new Chunk[32 * 32];
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                out[x * 32 + z] = Chunk.from(input, x, z);
            }
        }
        return out;
    }

    @Override
    public String toString() {
        return "Region{" +
               "size=" + size +
               ", x=" + x +
               ", z=" + z +
               ", chunks=" + Arrays.toString(chunks) +
               '}';
    }
}

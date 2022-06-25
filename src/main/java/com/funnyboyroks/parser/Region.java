package com.funnyboyroks.parser;

import me.nullicorn.nedit.type.NBTCompound;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Region {

    public final int size;
    public final int  x, z;
    public final Chunk[] chunks;

    public Region(File file) throws IOException {

        String[] parts = file.getName().split("\\.");
        this.x = Integer.parseInt(parts[1]);
        this.z = Integer.parseInt(parts[2]);

        byte[] data = Files.readAllBytes(file.toPath());
        this.size = data.length;

        this.chunks = this.parseChunks(data);

    }

    public Chunk getChunk(int x, int z) {
        return this.chunks[x * 32 + z];
    }

    private Chunk[] parseChunks(byte[] input) throws IOException {
        Chunk[] out = new Chunk[32 * 32];
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                out[x * 32 + z] = Chunk.from(input, this, x, z);
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

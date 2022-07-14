package com.funnyboyroks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public record Region(int x, int z, long[] chunks, MinMax sizes) {

    private Region(int x, int z, Object[] chunksAndSizes) {
        this(x, z, (long[]) chunksAndSizes[0], (MinMax) chunksAndSizes[1]);
    }

    private Region(String[] parts, byte[] data) {
        this(
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            parseChunks(data)
        );
    }

    public Region(File file) throws IOException {
        this(file.getName().split("\\."), Files.readAllBytes(file.toPath()));
    }

    public static long slice4BE(byte[] input, int offset) {
        if (offset + 4 > input.length) return 0;
        return (input[offset] & 0xffL) << 24
               | (input[offset + 1] & 0xffL) << 16
               | (input[offset + 2] & 0xffL) << 8
               | input[offset + 3] & 0xffL;
    }

    private static Object[] parseChunks(byte[] input) {
        long[] out = new long[32 * 32];
        MinMax sizes = new MinMax();
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                long location = slice4BE(input, 4 * ((x & 31) + (z & 31) * 32));
                if (location == 0) {
                    out[x * 32 + z] = -1;
                    continue;
                }
                sizes.add(out[x * 32 + z] = slice4BE(input, (int) (location >> 8) * 0x1000));
            }
        }
        return new Object[]{ out, sizes.constrain(0, 10000) };
    }

    @Override
    public String toString() {
        return "Region{r.%s.%s.mca : %.02f%% chunks }".formatted(x, z, Arrays.stream(chunks).filter(l -> l >= 0).count() / 1024.0 * 100);
    }

}

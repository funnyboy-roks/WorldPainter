package com.funnyboyroks.parser;

import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

public class Chunk {

    public final int         offset;
    public final byte        sectorCount;
    public final long        timestamp;
    public final long        length;
    public final byte        compressionType;
    public final byte[]      compressedData;
    public final byte[]      uncompressedData;
    public final NBTCompound nbt;
    public final Region      parent;


    private static int getChunkIndex(int x, int z) {
        return 4 * ((x & 31) + (z & 31) * 32);
    }

    public static Chunk from(byte[] fileData, Region parent, int x, int z) throws IOException {

        int index = getChunkIndex(x, z);

        long location = Util.sliceBE(fileData, index, 4);
        int offset = (int) (location >> 8);
        byte sectorCount = (byte) (location & 0xff);

        if (offset + sectorCount == 0) return null;

        long timestamp = Util.sliceBE(fileData, index + 0x1000, 4);

        long length = Util.sliceBE(fileData, offset * 0x1000, 4);
        byte compressionType = fileData[offset * 0x1000 + 4];

//        byte[] compressedData = Util.slice(fileData, offset * 0x1000 + 5, (int) length * (sectorCount & 0xff) - 2);
//        byte[] uncompressedData = new InflaterInputStream(new ByteArrayInputStream(compressedData)).readAllBytes();
//        NBTCompound nbt = NBTReader.read(new ByteArrayInputStream(uncompressedData));
//
//        return new Chunk(parent, offset, sectorCount, timestamp, length, compressionType, compressedData, uncompressedData, nbt);
        return new Chunk(parent, offset, sectorCount, timestamp, length, compressionType, null, null, null);
    }


    private Chunk(Region parent, int offset, byte sectorCount, long timestamp, long length, byte compressionType, byte[] compressedData, byte[] uncompressedData, NBTCompound nbt) {
        this.offset = offset;
        this.sectorCount = sectorCount;
        this.timestamp = timestamp;
        this.length = length;
        this.compressionType = compressionType;
        this.compressedData = compressedData;
        this.uncompressedData = uncompressedData;
        this.nbt = nbt;
        this.parent = parent;
    }


    @Override
    public String toString() {
        return "Chunk{" + "offset=" + offset + ", sectorCount=" + sectorCount + ", timestamp=" + timestamp + ", length=" + length + ", compressionType=" + compressionType + ", parent=" + String.format("r.%s.%s.mca", parent.x, parent.z) + ", nbt=" + nbt + '}';
    }
}
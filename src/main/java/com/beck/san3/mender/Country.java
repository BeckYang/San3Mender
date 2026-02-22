package com.beck.san3.mender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Country {
    public static final int BYTE_LENGTH = 48;

    private boolean dirty;
    private final int no;
    int lord;//太守
    int homelessCity;//流浪中所在城
    short homelessGold;//黃金
    short homelessRice;//糧

    int alliance;//同盟 bit flag:
    int[] hostility;//敵對 total:21
    byte[] data;
    
    public Country(int no) {
        this.no = no;
    }
    
    public int getNo() {
        return no;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
    }

    public Country readFrom(final ByteBuffer buffer) {
        this.data = Arrays.copyOf(buffer.array(), BYTE_LENGTH);
        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.lord = buffer.getShort(0);
        
        this.alliance = buffer.getInt(8);
        this.hostility = new int[21];
        buffer.position(14);
        for (int i = 0; i < 21; i++) {
            this.hostility[i] = buffer.get();
        }
        this.homelessCity = buffer.get(41);
        this.homelessGold = buffer.getShort(42);
        this.homelessRice = buffer.getShort(44);
        return this;
    }
    

    public ByteBuffer toRaw() {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(8, alliance);
        buffer.position(14);
        for (int i = 0; i < 21; i++) {
            buffer.put((byte) hostility[i]);
        }
        buffer.put(41,  (byte)homelessCity);
        buffer.putShort(42, (short)homelessGold);
        buffer.putShort(44, (short)homelessRice);
        buffer.position(0);
        return buffer;
    }
    
    @Override
    public String toString() {
        return lord + "\t" + homelessCity +
                " " + homelessGold +
                " " + homelessRice +
                " " + alliance + 
                " " + Arrays.toString(hostility);
    }

    public boolean isAlliance(int idx) {
        int flag = alliance & (1 << idx);
        return flag > 0;
    }

    public void setAlliance(final int idx, final boolean flag) {
        int bit = 1 << idx;
        if (flag) {
            alliance = alliance | bit;
        } else if (isAlliance(idx)){
            alliance = alliance ^ bit;
        }
    }
}

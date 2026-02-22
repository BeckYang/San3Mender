package com.beck.san3.mender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class City {
    public static final int BYTE_LENGTH = 74;
    
    public static List<ComboItem> ADMIN_ITEMS = Arrays.asList(
            new ComboItem(0x30, "直轄", true), new ComboItem(0x31, "軍事", true), 
            new ComboItem(0x32, "生產", true), new ComboItem(0x33, "適應", true));
    public static boolean showCityNo = true;
    
    private boolean dirty;
    private final int no;
    String name;
    int mayor;//太守
    int homeless;//在野
    int standby;//可搜索
    int counsellor;//軍師
    short population;//人口
    short gold;//黃金
    int rice;//糧
    int status;
    int admin;//委任
    int cultivation;//開發
    int farming;//耕作
    int irrigation;//灌溉
    int water;//治水
    short business;//商業 *2
    int taxRate;//稅率
    int faithful;//民忠
    short bow;//弩 *2
    short powerfulBow;//強弩 *2
    short horse;//軍馬 *2
    int boat1;//戰艦
    int boat2;//重艦
    int boat3;//輕艦
    int riceSell;//賣米
    int riceBuy;//買米
    int bowBuy;//弩
    int powerfulBowBuy;//強弩
    int horseBuy;//軍馬
    byte[] data;

    public City(int no) {
        this.no = no;
    }
    
    public int getNo() {
        return no;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
    }
    public static Class<?>[] getColumnType() {
        return new Class[]{Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
    }

    public static String[] getColumnHeader() {
        return new String[]{"序號", "名稱", "開發", "耕作", "灌溉", "治水", "商業", "稅率", "民忠"};
    }

    public String getColumnText(int index) {
        switch (index) {
            case 0: return isDirty() ? no + "*" : String.valueOf(no+1);
            case 1: return name;
            case 2: return String.valueOf(cultivation);
            case 3: return String.valueOf(farming);
            case 4: return String.valueOf(irrigation);
            case 5: return String.valueOf(water);
            case 6: return String.valueOf(business);
            case 7: return String.valueOf(taxRate);
            case 8: return String.valueOf(faithful);
            default: return "";
        }
    }

    public City readFrom(final ByteBuffer buffer) {
        data = Arrays.copyOf(buffer.array(), BYTE_LENGTH);
        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        mayor = buffer.getShort(8);
        homeless = buffer.getShort(10);
        standby = buffer.getShort(12);
        counsellor = buffer.getShort(14);
        population = buffer.getShort(24);
        gold = buffer.getShort(26);
        rice = buffer.getInt(28);
        status = buffer.get(36);
        admin = buffer.get(37);
        cultivation = buffer.get(42);//開發
        farming = buffer.get(43);//耕作
        irrigation = buffer.get(44);//灌溉
        water = buffer.get(45);//治水
        business = buffer.getShort(46);//商業 *2
        taxRate = buffer.get(48);//稅率
        faithful = buffer.get(49);
        bow = buffer.getShort(50);//弩 *2
        powerfulBow = buffer.getShort(52);//強弩 *2
        horse = buffer.getShort(54);//軍馬 *2
        boat1 = buffer.get(57);//戰艦
        boat2 = buffer.get(58);//重艦
        boat3 = buffer.get(59);//輕艦
        riceSell = buffer.get(60);//賣米
        riceBuy = buffer.get(61);//買米
        bowBuy = buffer.get(62);//弩
        powerfulBowBuy = buffer.get(63);//強弩
        horseBuy = buffer.get(64);//軍馬
        String name1 = StringTool.encodeHex(buffer.get(65), buffer.get(66));
        String name2 = StringTool.encodeHex(buffer.get(67), buffer.get(68));
        name = StringTool.translate(name1, name2);
        return this;
    }
    
    public ByteBuffer toRaw() {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(8,  (short)mayor);
        buffer.putShort(10, (short)homeless);
        buffer.putShort(12, (short)standby);
        buffer.putShort(14, (short)counsellor);
        buffer.putShort(24, population);
        buffer.putShort(26, gold);
        buffer.putInt(28, rice);
        buffer.put(36, (byte)status);
        buffer.put(37, (byte)admin);
        buffer.put(42, (byte)cultivation);
        buffer.put(43, (byte)farming);
        buffer.put(44, (byte)irrigation);
        buffer.put(45, (byte)water);
        buffer.putShort(46, business);
        buffer.put(48, (byte)taxRate);
        buffer.put(49, (byte)faithful);
        buffer.putShort(50, bow);
        buffer.putShort(52, powerfulBow);
        buffer.putShort(54, horse);
        buffer.put(57, (byte)boat1);
        buffer.put(58, (byte)boat2);
        buffer.put(59, (byte)boat3);
        buffer.put(60, (byte)riceSell);
        buffer.put(61, (byte)riceBuy);
        buffer.put(62, (byte)bowBuy);
        buffer.put(63, (byte)powerfulBowBuy);
        buffer.put(64, (byte)horseBuy);
        
        buffer.position(0);
        return buffer;
    }
    
    public void setStatus(final boolean commandEnd, final boolean tradeable) {
        int x = status;
        x = EditConfig.editByte(x, 0x80, commandEnd);
        status = EditConfig.editByte(x, 0x20, tradeable);
    }
    public boolean isCommandEnd() {
        return (status & 0x80) > 0;
    }
    public boolean isTradeable() {
        return (status & 0x20) > 0;
    }

    @Override
    public String toString() {
        return name + "\t" + admin +
                " " + cultivation +
                " " + farming +
                " " + irrigation +
                " " + water +
                " " + business +
                " " + taxRate +
                " " + faithful +
                " " + bow +
                " " + boat1 +
                " " + boat2 +
                " " + boat3 +
                " " + riceSell +
                " " + riceBuy +
                " " + bowBuy +
                " " + powerfulBowBuy +
                " " + horseBuy;
    }
    
    public static ComboItem newComboItem(final City city) {
        final String s = showCityNo ? ((city.no + 1) + "." + city.name) : city.name;
        return new ComboItem(city.no, s, true);
    }

    public static List<ComboItem> getItems(final List<City> cityList) {
        final List<ComboItem> items = cityList.stream()
                .map(City::newComboItem)
                .collect(Collectors.toList());
        return items;
    }

    public static void clearDirty(final City ...changedCity) {
        for (City city : changedCity) {
            city.dirty = false;
        }
    }

}

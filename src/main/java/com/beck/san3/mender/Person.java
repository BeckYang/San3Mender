package com.beck.san3.mender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Stream;

public class Person {
    public static int BYTE_LENGTH = 49;//JP=62
    public static int NAME_OFFSET = 42;//JP=48
    public static final int WORK_SPY = 7;

    public static List<ComboItem> WORK_ITEMS = Arrays.asList(
            new ComboItem(0, "士氣", false), new ComboItem(1, "訓練", false), new ComboItem(2, "搜索", false), new ComboItem(3, "開發", false), new ComboItem(4, "耕作", false),
            new ComboItem(5, "治水", false), new ComboItem(6, "商業", false), new ComboItem(WORK_SPY, "間諜", false));
    
    private boolean dirty;
    private final int no;
    String name;
    //short face;
    int nextPersonNo;//次席
    short soldier;//士兵
    int treasure;//寶物
    int ambition;//野心、運氣
    int lucky;
    int calmness;//冷靜、勇猛
    int brave;
    int status;//行動
    int health;//傷病
    int lifespan;//壽命
    int ambush;//埋伏
    int role;
    int abilityLand;//陸指
    int abilityWater;//水指
    int strength;//武
    int intelligence;//知
    int governing;//政
    int charm;//魅力
    int match;//相性
    int rational;//義理
    int faithful;//忠
    int location;//所在
    int country;//君主
    int seniority;//仕官
    int countryActual;//裏君主
    int seniorityActual;//裏仕官
    int consanguinity;//血緣
    int training;//訓練
    int morale;//士氣
    int birthYear;//生年
    int work;//工作: 00-士氣 01-訓練 02-搜索 03-開發 04-耕作 05-治水 06-商業 07-間諜(供參考 必須搭配一起改!)
    byte workMonth;//工月: 工作 下半byte剩餘月數 上半byte長期工作完成月數
    int workEffect;//工效
    int workCity;//工城
    byte[] data;

    public int getNo() {
        return no;
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public void setDirty() {
        dirty = true;
    }
    
    public String getName() {
        return name;
    }

    public static Optional<Person> readFrom(final ByteBuffer buffer, final int no) {
        int strength = buffer.get(18);
        int intelligence = buffer.get(19);
        if (strength < 1 || intelligence < 1) {
            return Optional.empty();
        }
        return Optional.of(new Person(buffer, strength, intelligence, no));
    }

    private Person(final ByteBuffer buffer, final int strength, final int intelligence, int no) {
/*
03 00 00 00 cc 42 00 02 BC A6 05 00 60 00 00 3C 1D 44 4E 52 63 4B 64 00 16 00
^^^^^ ^^^^^ ^^^^^ ^^^^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^ ^^
臉    次 席  士 兵 寶 物 野 冷 行 傷 壽 埋 官 陸 水 武 知 政 魅 相 義 忠 所 君
                      運 猛 動 病 命 伏    指 指 力 力 治 力 性 理 誠 在 主
07 FF 00 01 19 0A 00 00 00 FF FF 00 00 00 00 ?? ?? ?? ?? ?? ??
            ^^ ^^          ^^ ^^ ^^ ^^ ^^    ^^
仕 裏 裏 血 訓 士          生 工 工 工 工    人名
官 君 仕 緣 練 氣          年 作 月 效 城
 */
        this.data = Arrays.copyOf(buffer.array(), BYTE_LENGTH);
        this.no = no;
        this.strength = strength;//武
        this.intelligence = intelligence;//知

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        nextPersonNo = buffer.getShort(3);
        soldier = buffer.getShort(5);
        treasure = buffer.getShort(7);
        byte b = buffer.get(9);
        ambition = EditConfig.highByte(b);//野心、運氣
        lucky = EditConfig.lowByte(b);
        b = buffer.get(10);
        calmness = EditConfig.highByte(b);//冷靜、勇猛
        brave = EditConfig.lowByte(b);
        status = buffer.get(11);//行動
        health = buffer.get(12);//傷病
        lifespan = EditConfig.highByte(buffer.get(13));//壽命
        ambush = buffer.get(14);
        role = buffer.get(15);
        abilityLand = buffer.get(16);//陸指
        abilityWater = buffer.get(17);//水指
        governing = buffer.get(20);//政
        charm = buffer.get(21);//魅力
        match = buffer.get(22);//相性
        rational = buffer.get(23);//義理
        faithful = buffer.get(24);//忠
        location = buffer.get(25);//所在
        country = buffer.get(26);//君主
        seniority = buffer.get(27);//仕官
        countryActual = buffer.get(28);//裏君主
        seniorityActual = buffer.get(29);//裏仕官
        consanguinity = buffer.get(30);//血緣
        training = buffer.get(31);//訓練
        morale = buffer.get(32);//士氣
        birthYear = Byte.toUnsignedInt(buffer.get(36));//生年
        work = buffer.get(37);
        workMonth = buffer.get(38);
        workEffect = Byte.toUnsignedInt(buffer.get(39));
        workCity = buffer.get(40);

        buffer.position(NAME_OFFSET);
        String name1 = StringTool.encodeHex(buffer.get(), buffer.get());
        String name2 = StringTool.encodeHex(buffer.get(), buffer.get());
        String name3 = StringTool.encodeHex(buffer.get(), buffer.get());
        name = StringTool.translate(name1, name2, name3);
        /*try {
            if (BYTE_LENGTH == 62) {
                String jp = new String(data, 48, 6, Charset.forName("Shift-JIS"));//need mapping `JIS.txt`
                name = jp;
            }
        } catch (Exception e) {
             e.printStackTrace();
        }*/
    }

    //public void writeTo(final ByteBuffer buffer) {
    public ByteBuffer toRaw() {
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(3, (short) nextPersonNo);
        buffer.putShort(5, soldier);
        buffer.putShort(7, (short) treasure);
        buffer.put(9, EditConfig.highAndLow(ambition, lucky));
        buffer.put(10, EditConfig.highAndLow(calmness, brave));
        buffer.put(11, (byte)status);
        buffer.put(12, (byte)health);
        buffer.put(13, EditConfig.highAndLow(lifespan, 0));
        buffer.put(14, (byte)ambush);
        if (EditConfig.roleEditable) {
            buffer.put(15, (byte)role);
        }
        
        buffer.put(16, (byte)abilityLand);
        buffer.put(17, (byte)abilityWater);
        buffer.put(18, (byte)strength);
        buffer.put(19, (byte)intelligence);
        buffer.put(20, (byte)governing);
        buffer.put(21, (byte)charm);
        buffer.put(22, (byte)match);
        buffer.put(23, (byte)rational);
        buffer.put(24, (byte)faithful);
        buffer.put(25, (byte)location);
        buffer.put(26, (byte)country);
        buffer.put(27, (byte)seniority);
        buffer.put(28, (byte)countryActual);
        buffer.put(29, (byte)seniorityActual);
        buffer.put(30, (byte)consanguinity);
        buffer.put(31, (byte)training);
        buffer.put(32, (byte)morale);
        buffer.put(36, (byte)birthYear);
        buffer.put(37, (byte)work);
        buffer.put(38, (byte)workMonth);
        buffer.put(39, (byte)workEffect);
        buffer.put(40, (byte)workCity);
        buffer.position(0);
        return buffer;
    }
    
    public void doSpy(final int city) {
        setDirty();
        work = WORK_SPY;
        workMonth = 1;
        workCity = city;
        health = EditConfig.editByte(health, 0x40, true);
    }
    
    public boolean isSick() {
        return (health & 0x15) > 0;
    }
    public void setSick(boolean sick) {
        if (sick) {
            if (!isSick()) {
                health = (health & 0xF0) | 0x09;
            }
        } else {
            health = health & 0xF0;
        }
    }
    
    public boolean isAmbush() {
        return (ambush & 0x08) > 0;
    }
    public void setAmbush(boolean flag) {
        ambush = EditConfig.editByte(ambush, 0x08, flag);
    }
    
    public void setStatus(final boolean commandEnd, final boolean dead) {
        int x = status;
        x = EditConfig.editByte(x, 0x01, commandEnd);
        status = EditConfig.editByte(x, 0x10, dead);
    }
    public boolean isCommandEnd() {
        return (status & 0x01) > 0;
    }
    public boolean isDead() {
        return (status & 0x10) > 0;
    }
    public int getWorkMonthTotal() {
        return EditConfig.lowByte(workMonth);
    }
    public int getWorkMonthDone() {
        return EditConfig.highByte(workMonth);
    }
    
    public static Class<?>[] getColumnType() {
        return new Class[]{Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
    }
    public static String[] getColumnHeader() {
        return new String[]{"序號", "姓　名", "武力", "智力", "政治", "魅力", "陸指", "水指", "忠誠"};
    }
    public String getColumnText(int index) {
        switch (index) {
            case 0: return isDirty() ? no + "*" : String.valueOf(no);
            case 1: return name;
            case 2: return String.valueOf(strength);
            case 3: return String.valueOf(intelligence);
            case 4: return String.valueOf(governing);
            case 5: return String.valueOf(charm);
            case 6: return String.valueOf(abilityLand);
            case 7: return String.valueOf(abilityWater);
            case 8: return String.valueOf(faithful);
            default: return "";
        }
    }

    @Override
    public String toString() {
        return name + '\t' +
                " " + abilityLand +
                " " + abilityWater +
                " " + strength +
                " " + intelligence +
                " " + governing +
                " " + charm +
                " " + faithful +
                " " + location +
                " " + country +
                " " + soldier +
                ", nextPersonNo->" + nextPersonNo;
    }
    
    public static Stream<Person> countryLord(final List<Person> personList) {
        return personList.stream().filter(p -> p.role == 0 && p.birthYear > 0);
    }

    public static List<ComboItem> getCountryItems(final List<Person> personList, boolean editable) {
        final List<ComboItem> list = new ArrayList<>();
        if (editable) {
            list.add(new ComboItem(-1, "", true));
        }
        countryLord(personList).forEach(person -> {
            list.add(new ComboItem(person.country, person.name, editable));
        });
        return list;
    }

    public static void clearDirty(final Person ...changedPerson) {
        for (Person person : changedPerson) {
            person.dirty = false;
        }
    }

    public void updateNextPersonNo(final int nextPersonNo, final int cityNo) {
        if (this.nextPersonNo == nextPersonNo && this.location == cityNo) {
            return;
        }
        setDirty();
        this.nextPersonNo = nextPersonNo;
        this.location = cityNo;
    }

}

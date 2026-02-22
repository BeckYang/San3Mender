package com.beck.san3.mender;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

public class SaveSlotIndex {

    public static final int BYTE_LENGTH = 14;

    public static Optional<String> readFrom(final ByteBuffer buffer, final int sequense) {
        /*
year   year   month  ????   city   劇本   LV     name
<bh:00><bh:00><bh:00><bh:00><bh:ff><bh:00><bh:ff><bh:8c>N      <bh:8e><bh:e5><bh:96><bh:bc><bh:00>
<bh:c2><bh:00><bh:01><bh:01><bh:04><bh:02><bh:01><bh:9c><bh:af><bh:9e><bh:d4><bh:00><bh:00><bh:00>
         */
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int scenario = buffer.get(5);
        if (scenario > 0) {
            int year = buffer.getShort(0);
            int month = buffer.get(2);
            int city = buffer.get(4);
            buffer.position(6);
            int level = buffer.get();
            String name1 = StringTool.encodeHex(buffer.get(), buffer.get());
            String name2 = StringTool.encodeHex(buffer.get(), buffer.get());
            String name3 = StringTool.encodeHex(buffer.get(), buffer.get());
            final String name = StringTool.translate(name1, name2, name3);
            final String separator = " : ";
            return Optional.of(sequense + ". " + year + "年" + month + "月" + separator +
                    name + separator + getCityName(city) + separator + 
                    "劇本" + scenario + separator + (level==1?"高級":"初級"));
        }
        return Optional.empty();
    }


    public static String getCityName(int no) {
        String[] citys = {"襄平","北平","代縣","晉陽","南皮","平原","鄴","北海","濮陽","陳留",
                "洛陽","弘農","長安","安定","天水","西涼","下邳","徐州","許昌","譙",
                "汝南","宛","新野","襄陽","上庸","江夏","江陵","武陵","長沙","桂陽",
                "零陵","壽春","建業","吳郡","會稽","廬江","柴桑","漢中","下弁","梓潼",
                "成都","永安","江州","建寧","雲南"};
        return (no >= 0 && no < citys.length) ? citys[no] : "";
    }
    
}

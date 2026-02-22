package com.beck.san3.mender;

public enum SaveEdition {
    TC_DOS("san3tc.txt", 0xce01, 49, 42), JP_DOS("san3jp.txt", 0xec79, 62, 48);
    
    private final String nameMappingResource;
    private final long slotSize;
    //private final long personOffset;
    private final int personLength;
    private final int personNameOffset;
    
    private SaveEdition(final String name, final long slot, final int personLength, final int personNameOffset) {
        this.nameMappingResource = name;
        this.slotSize = slot;
        this.personLength = personLength;
        this.personNameOffset = personNameOffset;
        //this.personOffset = personOffset;
    }
    
    public String nameMappingResource() {
        return nameMappingResource;
    }
    
    public void initBufferSize() {
        Person.BYTE_LENGTH = personLength;
        Person.NAME_OFFSET = personNameOffset;
    }
    
    public long cityOffset(final int slotIndex) {
        return slotIndex * slotSize + 0x1b8;
    }
    
    public long countryOffset(int slotIndex) {
        return slotIndex * slotSize + 0x1130;
    }
    
    public long personOffset(final int slotIndex) {
        return slotIndex * slotSize + 0x544e;
    }
    
    public static SaveEdition detect(long fileSize) {
        if (fileSize == 605520) {
            return JP_DOS;
        }
        return TC_DOS;//
    }

}

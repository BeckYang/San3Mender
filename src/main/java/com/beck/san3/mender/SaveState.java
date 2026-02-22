package com.beck.san3.mender;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class SaveState {
    private Path path;
    private FileTime lastReadTime;
    public int slotIndex;
    public List<City> cityList = new ArrayList<>();
    public List<Person> personList = new ArrayList<>();
    public List<Country> countryList = new ArrayList<>();
    private final Map<Integer, Person> personNoMap = new HashMap<>();
    private SaveEdition saveEdition;
    private Runnable dirtListener = null;
    
    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }
    
    public void save() throws Exception {
        final City[] changedCity = cityList.stream().filter(City::isDirty).toArray(City[]::new);
        final Person[] changedPerson = personList.stream().filter(Person::isDirty).toArray(Person[]::new);
        final Country[] changedCountry = countryList.stream().filter(Country::isDirty).toArray(Country[]::new);
        if (changedCity.length == 0 && changedPerson.length == 0 && changedCountry.length == 0) {
            return;
        }
        final long countryOffset = saveEdition.countryOffset(slotIndex);
        final long cityOffset = saveEdition.cityOffset(slotIndex);
        final long personOffset = saveEdition.personOffset(slotIndex);
        try (final SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            for (final City city : changedCity) {
                channel.position(city.getNo() * City.BYTE_LENGTH + cityOffset);
                channel.write(city.toRaw());
            }
            for (final Country c : changedCountry) {
                channel.position(c.getNo() * Country.BYTE_LENGTH + countryOffset);
                channel.write(c.toRaw());
            }
            for (final Person person : changedPerson) {
                channel.position(person.getNo() * Person.BYTE_LENGTH + personOffset);
                channel.write(person.toRaw());
            }
        }
        lastReadTime = Files.getLastModifiedTime(path);
        City.clearDirty(changedCity);
        Person.clearDirty(changedPerson);
    }

    public void readSlot(final int slotIndex) throws Exception {
        this.slotIndex = slotIndex;
        countryList.clear();
        cityList.clear();
        personList.clear();
        personNoMap.clear();
        saveEdition.initBufferSize();
        final long countryOffset = saveEdition.countryOffset(slotIndex);
        final long cityOffset = saveEdition.cityOffset(slotIndex);
        final long personOffset = saveEdition.personOffset(slotIndex);
        try (final SeekableByteChannel channel = Files.newByteChannel(path)) {
            StringTool.load(saveEdition.nameMappingResource());
            //read city
            channel.position(cityOffset);
            final ByteBuffer buffer = ByteBuffer.allocate(City.BYTE_LENGTH);
            for (int i = 0; i < 45; i++) {
                buffer.position(0);
                channel.read(buffer);
                cityList.add(new City(i).readFrom(buffer));
            }
//cityList.stream().map(City::getName).forEach(System.err::println);
            //read country
            channel.position(countryOffset);
            final ByteBuffer cbuffer = ByteBuffer.allocate(Country.BYTE_LENGTH);
            for (int i = 0; i < 21; i++) {
                cbuffer.position(0);
                channel.read(cbuffer);
                countryList.add(new Country(i).readFrom(cbuffer));
            }
            //read person
            channel.position(personOffset);
            final ByteBuffer pBuffer = ByteBuffer.allocate(Person.BYTE_LENGTH);
            for (int i = 0; i < 600; i++) {//max:600
                pBuffer.position(0);
                channel.read(pBuffer);
                final Optional<Person> optional = Person.readFrom(pBuffer, i);
                if (optional.isPresent()) {
                    final Person p = optional.get();
                    personList.add(p);
                    personNoMap.put(Integer.valueOf(p.getNo()), p);
                }
            }
        }
        lastReadTime = Files.getLastModifiedTime(path);
    }

    public boolean isModified() {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return !lastReadTime.equals(lastModifiedTime);
        } catch (IOException e) {
            return true;
        }
    }

    public List<ComboItem> readIndex() throws Exception {
        StringTool.load(saveEdition.nameMappingResource());
        final List<ComboItem> ret = new ArrayList<>();
        try (final SeekableByteChannel channel = Files.newByteChannel(path)) {
            channel.position(10);
            final ByteBuffer buffer = ByteBuffer.allocate(SaveSlotIndex.BYTE_LENGTH);
            for (int i = 0; i < 10; i++) {
                buffer.position(0);
                channel.read(buffer);
                final Optional<String> optional = SaveSlotIndex.readFrom(buffer, i+1);
                if (optional.isPresent()) {
                    ret.add(new ComboItem(i, optional.get(), true));
                }
            }
        }
        return ret;
    }
    
    public static SaveState loadFile(final File file) throws Exception {
        Path path = file.toPath();
        final SaveState me = new SaveState();
        me.path = path;
        me.lastReadTime = Files.getLastModifiedTime(path);
        me.saveEdition = SaveEdition.detect(file.length());
        return me;
    }
    
    public Object[] getPersonByCity(final int cityNo) {
        final Optional<City> cityOpt = getCity(cityNo);
        if (cityOpt.isPresent()) {
            final City city = cityOpt.get();
            final List<Person> list = getPersonChain(city.mayor);
            list.addAll(getPersonChain(city.homeless));
            list.addAll(getPersonChain(city.standby));
            return list.toArray();
        }
        return null;
    }

    public Object[] getPersonByCountry(final int country) {
        return personList.stream().filter(p -> p.country == country).toArray();
    }
    
    public Optional<Person> getPerson(final int personNo) {
        if (personNo < 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(personNoMap.get(personNo));
    }
    
    public List<Person> getPersonChain(final int personNo) {
        final List<Person> list = new ArrayList<>();
        int no = personNo;
        while (no >= 0) {
            Person person = personNoMap.get(no);
            if (person == null) {
                no = -1;
            } else {
                list.add(person);
                no = person.nextPersonNo;
            }
        }
        return list;
    }

    public Optional<City> getCity(final int cityNo) {
        if (cityNo < 0) {
            return Optional.empty();
        }
        return cityList.stream().filter(c -> c.getNo() == cityNo).findFirst();
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public void updateLocation(final Person person, final int moveToCityNo, final PersonIdentity personIdentity) {
        final Optional<City> cityOpt = getCity(person.location);
        if (cityOpt.isPresent()) {
            removePerson(cityOpt.get(), person, personIdentity);
        }
        if (personIdentity == PersonIdentity.none) {
            person.updateNextPersonNo(-1, -1);//未登場
            return;
        }
        final Optional<City> newCityOpt = getCity(moveToCityNo);
        if (!newCityOpt.isPresent()) {
            return;
        }
        final City city = newCityOpt.get();
        List<Person> newList = getPersonChain(personIdentity.getFirstPerson(city));
        newList.add(person);
        updatePersonOrder(city, newList, personIdentity);
        personIdentity.updatePersonRole(person);
    }
    
    private void removePerson(final City city, final Person person, final PersonIdentity personIdentity) {
        List<Person> list = getPersonChain(city.mayor);
        if (list.remove(person)) {
            updatePersonOrder(city, list, PersonIdentity.hired);
            return;
        }
        list = getPersonChain(city.homeless);
        if (list.remove(person)) {
            updatePersonOrder(city, list, PersonIdentity.homeless);
            return;
        }
        list = getPersonChain(city.standby);
        if (list.remove(person)) {
            updatePersonOrder(city, list, PersonIdentity.standby);
            return;
        }
    }

    public void updatePersonOrder(final City city, final List<Person> newList, final PersonIdentity personIdentity) {
        if (newList.isEmpty()) {
            personIdentity.updateFirstPerson(city, -1);
            return;
        }
        final int cityNo = city.getNo();
        final Iterator<Person> newIterator = newList.iterator();
        Person person = newIterator.next();
        personIdentity.updateFirstPerson(city, person.getNo());
        while (newIterator.hasNext()) {
            Person nextPerson = newIterator.next();
            person.updateNextPersonNo(nextPerson.getNo(), cityNo);
            person = nextPerson;
        }
        person.updateNextPersonNo(-1, cityNo);
    }
    
    public void addDirtyListener(final Runnable dirtListener) {
        this.dirtListener = dirtListener;
    }

    public void fireDirtyEvent() {
        if (dirtListener != null) {
            dirtListener.run();
        }
    }

}

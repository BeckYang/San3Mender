package com.beck.san3.mender;

import java.util.Arrays;
import java.util.List;

public enum PersonIdentity {
    none {
        public int getFirstPerson(final City city) {
            return -1;
        }
    }, homeless {
        public int getFirstPerson(final City city) {
            return city.homeless;
        }

        public void updateFirstPerson(final City city, final int personNo) {
            if (city.homeless != personNo) {
                city.homeless = personNo;
                city.setDirty();
            }
        }
        
        public void updatePersonRole(final Person person) {
            if (person.role != 5) {
                person.role = 5;
            }
        }
    }, standby {
        public int getFirstPerson(final City city) {
            return city.standby;
        }

        public void updateFirstPerson(final City city, final int personNo) {
            if (city.standby != personNo) {
                city.standby = personNo;
                city.setDirty();
            }
        }

        public void updatePersonRole(final Person person) {
            if (person.role != 6) {
                person.role = 6;
            }
        }
    }, hired {
        public int getFirstPerson(final City city) {
            return city.mayor;
        }

        public void updateFirstPerson(final City city, final int personNo) {
            if (city.mayor != personNo) {
                city.mayor = personNo;
                city.setDirty();
            }
        }
        
        public void updatePersonRole(final Person person) {
            if (person.role > 4) {
                person.role = 3;
                if (person.faithful == 0) {
                    person.faithful = 100;
                }
            }
        }
    };

    public static List<ComboItem> ROLE_ITEMS = Arrays.asList(
            new ComboItem(0, "君主", false), new ComboItem(1, "軍師", true), new ComboItem(2, "將軍", true), new ComboItem(3, "武官", true), new ComboItem(4, "文官", true),
            new ComboItem(5, "在野", false), new ComboItem(6, "可搜索", false), new ComboItem(7,"未登場", false));

    public abstract int getFirstPerson(final City city);
    public void updateFirstPerson(final City city, final int personNo) {
        //do nothing
    }
    public void updatePersonRole(final Person person) {
        //do nothing
    }
    public static boolean notHired(Person person) {
        return person.role > 4;
    }
}

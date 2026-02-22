package com.beck.san3.mender;

import org.eclipse.jface.viewers.TableViewer;

public class PersonUIProvider extends BaseUIProvider {
    private final boolean allowSort;
    PersonUIProvider(final TableViewer tableViewer ,final boolean allowSort) {
        super(tableViewer);
        this.allowSort = allowSort;
    }

    @Override
    public String getColumnText(Object data, int index) {
        final Person person = (Person) data;
        return person.getColumnText(index);
    }

    @Override
    public void initColumnsAndEvent() {
        initColumnsAndEvent(Person.getColumnHeader(),
                null, Person.getColumnType(), allowSort);
    }

}

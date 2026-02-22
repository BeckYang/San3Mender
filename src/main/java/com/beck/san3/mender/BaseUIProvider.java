package com.beck.san3.mender;

//import beck.html.ui.UISetting;
//import com.beck.bi.charts.ui.ColumnSortType;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.Collection;
import java.util.Comparator;

public abstract class BaseUIProvider extends LabelProvider implements Comparator<String>, Listener, ITableLabelProvider, IStructuredContentProvider {
    private static final String DATA_TYPE = "dType";
    public static final Comparator<String> CMP_INTEGER = BaseUIProvider::compareInt;

    protected int colIndex = 0;
    protected boolean isUP;
    protected TableViewer fTable;
    protected ViewerComparator fTableComparator;

    protected Comparator<String> columnSorter;
    protected String cfgWidth;
    protected TableColumn[] columns;

    /* Content provider implement */
    protected Object[] data;
    public Object[] getElements(Object input) {
        if (input instanceof Object[]) {
            data = (Object[])input;
        } else if (input instanceof Collection) {
            data = ((Collection<?>)input).toArray();
        }
        return data;
    }

    public BaseUIProvider(TableViewer table) {
        fTable = table;
        columnSorter = String::compareTo;
        fTableComparator = new ViewerComparator(this);
    }

    private static int compareInt(final String a, final String b) {
        try {
            int ia = Integer.parseInt(a);
            int ib = Integer.parseInt(b);
            return Integer.compare(ia, ib);
        } catch (Exception e) {
            return 0;
        }
    }

    void setSortByColumnIndex(int index, final TableColumn sortColumn) {
        colIndex = index;
        Object dType = sortColumn.getData(DATA_TYPE);
        if (dType == Integer.class) {
            columnSorter = CMP_INTEGER;
        } else {
            columnSorter = String::compareTo;
        }
    }

    public void setSortDirection(boolean up) {
        isUP = up;
    }

    public int compare(String o1, String o2) {
        int n = columnSorter.compare(o1, o2);
        if (isUP) {
            n = n * -1;
        }
        return n;
    }

    public String getText(Object element) {
        return getColumnText(element, colIndex);
    }

    public void handleEvent(Event event) {
        if (event.type == SWT.Resize) {
            //TODO
        } else {
            sortTableColumn(event);
        }
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    protected void sortTableColumn(Event e) {
        Table tb = fTable.getTable();
        TableColumn currColumn = tb.getSortColumn();
        TableColumn sortColumn = (TableColumn)e.widget;
        int direction = tb.getSortDirection();
        if (currColumn == sortColumn) {
            if (direction == SWT.UP) {
                direction = SWT.DOWN;
            } else {
                direction = SWT.UP;
            }
            setSortDirection(direction==SWT.UP);
            tb.setSortDirection(direction);
        } else {
            tb.setSortColumn(sortColumn);
        }

        int cc = tb.getColumnCount();
        for (int i = 0; i < cc; i++) {
            if (tb.getColumn(i) == sortColumn) {
                setSortByColumnIndex(i, sortColumn);
                i = cc;
            }
        }
        if (fTable.getComparator() == null) {
            fTable.setComparator(fTableComparator);
        }
        fTable.refresh();
    }

    public int getSortByDirection() {
        return isUP?SWT.UP:SWT.DOWN;
    }

    public int getSortByColumnIndex() {
        return colIndex;
    }

    public void sort(int direction, int columnIndex) {
        Table tb = fTable.getTable();
        TableColumn column = tb.getColumn(columnIndex);
        tb.setSortColumn(column);
        tb.setSortDirection(direction);
        setSortDirection(direction==SWT.UP);
        setSortByColumnIndex(columnIndex, column);
        if (fTable.getComparator() == null) {
            fTable.setComparator(fTableComparator);
        }
        fTable.refresh();
    }

    public static void packColumns(TableViewer table) {
        Table tb = table.getTable();
        TableColumn sortColumn = tb.getSortColumn();
        TableColumn[] cols = tb.getColumns();
        int twidth = tb.getClientArea().width / 2;
        int[] width = new int[cols.length];
        int totalWidth = 0;
        for (int i = 0; i < cols.length; i++) {
            cols[i].pack();
            width[i] = cols[i].getWidth();
            int newWidth = -1;
            if (cols[i] == sortColumn) {
                newWidth = width[i] + 30;
                width[i] = newWidth;
            }
            totalWidth = totalWidth + width[i];
            if (twidth >0 && width[i] > twidth) {
                newWidth = twidth;
            }
            if (newWidth > 0) {
                cols[i].setWidth(newWidth);
            }
        }
    }

    public void dispose() {
        fTableComparator = null;
        data = null;
        disconnect();
    }

    /* LabelProvider implement */
    abstract public String getColumnText(Object data, int index);

    abstract public void initColumnsAndEvent();

    public void disconnect() {
        if (columns == null) {
            //prevent the call from TableViewer.internalDisposeLabelProvider() ==> LabelProvider change
            return;
        }
        for (int i = 0; i < columns.length; i++) {
            //columns[i].removeListener(SWT.Resize, this);
            columns[i].removeListener(SWT.Selection, this);
            columns[i].dispose();
        }
        columns = null;
    }

    protected void initColumnsAndEvent(String[] heads, int[] widths, Class<?>[] columnTypes, boolean allowSort) {
        Table table = fTable.getTable();
        //UISetting uisetting = UISetting.getInstance();
        columns = new TableColumn[heads.length];
        for (int j = 0; j < heads.length; j++) {
            TableColumn tc = new TableColumn(table,SWT.LEFT);
            tc.setMoveable(true);
            tc.setText(heads[j]);
            if (allowSort) {
                tc.addListener(SWT.Selection, this);
            }
            //tc.addListener(SWT.Resize, this);
            //tc.setWidth(uisetting.getInt(cfgWidth+j, widths == null ? 100 : widths[j]));
            tc.setWidth(widths == null ? 100 : widths[j]);
            if (columnTypes != null) {
                tc.setData(DATA_TYPE, columnTypes[j]);
            }
            columns[j] = tc;
        }
        packColumns(fTable);
    }
/*
    protected void addColumn(String label) {
        int len = columns.length;
        TableColumn[] ncolumns = new TableColumn[len+1];
        Table table = fTable.getTable();
        boolean allowSort = table.getSortColumn() != null;
        TableColumn tc = new TableColumn(table,SWT.LEFT);
        tc.setMoveable(true);
        tc.setText(label);
        if (allowSort) {
            tc.addListener(SWT.Selection, this);
        }
        tc.addListener(SWT.Resize, this);
        tc.setWidth(UISetting.getInstance().getInt(cfgWidth+len, 100));
        System.arraycopy(columns, 0, ncolumns, 0, len);
        ncolumns[len] = tc;
        columns = ncolumns;
    }*/

}

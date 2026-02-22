package com.beck.san3.mender;

import org.eclipse.swt.widgets.Spinner;

public interface Refreshable {
    public void refresh();

    public void maxAndApply(final Spinner spinner);
}

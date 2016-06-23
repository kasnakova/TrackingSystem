package tu.tracking.system.interfaces;

import java.util.GregorianCalendar;

public interface ShouldNotMoveDialogListener {
    void onShouldNotMoveDialogDone(boolean shouldNotMove, GregorianCalendar shouldNotMoveUntil);
}

package fragments;

import java.util.List;

import scheduleModule.CalendarEntry;
import scheduleModule.studyDay;

/**
 * Created by Anton on 11/06/2015.
 */
public interface CalendarFragmentCommunicator {
    public void passDataToFragment(studyDay day, String idnumber);
}

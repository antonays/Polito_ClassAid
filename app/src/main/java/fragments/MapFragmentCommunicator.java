package fragments;

import classAidManager.Room;
import scheduleModule.studyDay;

public interface MapFragmentCommunicator {
    public void passDataToMapFragment(String nextRoom, boolean isExistNextRoom);
}

package classAidManager;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import scheduleModule.*;

public class Student {

	private String id;
	private HashSet<Course> coursesFollowed;
	
	public Student(String id) {
		this.id = id;
		coursesFollowed = new HashSet<>();
	}
	
	public void addFollowingCourseToStudent (Course course) {
		coursesFollowed.add(course);
	}
	
	public HashSet<Course> getListOfFollowedCourses() {
		return coursesFollowed;
	}

    //NOTE: changed stream here
    public Collection<CalendarEntry> studentListCalendarEntries(GregorianCalendar date) {
        HashSet<CalendarEntry> studentListCalendarEntries = new HashSet<>();
        for (Course c: coursesFollowed){
            for (CalendarEntry ce : c.getTimetable()) {
                if (ce.getStartTimeDate().substring(0, 10).
                        equals((new SimpleDateFormat("dd/MM/yyyy")).format(date.getTime()))) {
                    studentListCalendarEntries.add(ce);
                }
            }
        }
        return studentListCalendarEntries;
    }
	
}

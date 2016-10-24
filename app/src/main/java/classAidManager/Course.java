package classAidManager;
import java.util.HashSet;

import scheduleModule.*;

public class Course {
	private String name;
	private String teacher;
	private HashSet<CalendarEntry> timetable;
	
	public Course(String name, String teacher) {
		this.name = name;
		this.teacher = teacher;
		this.timetable = new HashSet<>();
	}
	
	public String getName(){
		return name;
	}
	
	public String getTeacher(){
		return teacher;
	}
	
	public void addSchedule(CalendarEntry entry){
		timetable.add(entry);
	}
	
	public HashSet<CalendarEntry> getTimetable(){
		return timetable;
	}	
	
	@Override
	public boolean equals(Object arg0) {
		Course arg = (Course)arg0;
		if (this.name.equals(arg.getName()) && this.teacher.equals(arg.getTeacher())){
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode()*teacher.hashCode();
	}
	
	
}

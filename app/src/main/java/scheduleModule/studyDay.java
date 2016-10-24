package scheduleModule;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import classAidManager.Room;

/**
 * Created by Anton on 01/06/2015.
 */
public class studyDay implements Serializable{
    private int dayNumber;
    private CalendarEntry CE08301000;
    private CalendarEntry CE10001130;
    private CalendarEntry CE11301300;
    private CalendarEntry CE13001430;
    private CalendarEntry CE14301600;
    private CalendarEntry CE16001730;
    private CalendarEntry CE17301900;

    public studyDay(int i){
        dayNumber = i;
        CE08301000 = null;
        CE10001130 = null;
        CE11301300 = null;
        CE13001430 = null;
        CE14301600 = null;
        CE16001730 = null;
        CE17301900 = null;
    }
    public String getDayText(){
        switch (dayNumber){
            case 1: return "Sunday";
            case 2: return "Monday";
            case 3: return "Tueday";
            case 4: return "Wednesday";
            case 5: return "Thursday";
            case 6: return "Friday";
            case 7: return "Saturday";
            default: return null;
        }
    }

    public String StrCE08301000() { return "8:30-10:00"; }
    public String StrCE10001130() { return "10:00-11:30"; }
    public String StrCE11301300() { return "11:30-13:00"; }
    public String StrCE13001430() { return "13:00-14:30"; }
    public String StrCE14301600() { return "14:30-16:00"; }
    public String StrCE16001730() { return "16:00-17:30"; }
    public String StrCE17301900() { return "17:30-19:00"; }

    public List<CalendarEntry> getHourSlotAsCollection(){
        List<CalendarEntry> ls = new LinkedList<>();
        ls.add(CE08301000);
        ls.add(CE10001130);
        ls.add(CE11301300);
        ls.add(CE13001430);
        ls.add(CE14301600);
        ls.add(CE16001730);
        ls.add(CE17301900);
        return ls;
    }
    public void addEntry(CalendarEntry c){
        int start = c.getStart().get(GregorianCalendar.HOUR_OF_DAY);
        int end = c.getEnd().get(GregorianCalendar.HOUR_OF_DAY);
        if ((start==8) && (end>=10)){
            CE08301000 = c;
        }
        if ((start<=10) && (end>=11)){
            CE10001130 = c;
        }
        if ((start<=11) && (end>=13)){
            CE11301300 = c;
        }
        if ((start<=13) && (end>=14)){
            CE13001430 = c;
        }
        if ((start<=14) && (end>=16)){
            CE14301600 = c;
        }
        if ((start<=16) && (end>=17)){
            CE16001730 = c;
        }
        if ((start<=17) && (end==19)){
            CE17301900 = c;
        }
    }

    public CalendarEntry getNextCalendarEntry(Date d){
        int nowHours = d.getHours();
        int nowMinutes = d.getMinutes();
        //int nowHours = d.get(GregorianCalendar.HOUR_OF_DAY);
        //int nowMinutes = d.get(GregorianCalendar.HOUR_OF_DAY);
        if (nowHours<8){
            return CE08301000;
        }
        if ((nowHours>=8) && ((nowHours<10))){
            return CE10001130;
        }
        if ((nowHours>=10) && (((nowHours==11) &&(nowMinutes<30)) || (nowHours<11))){
            return CE11301300;
        }
        if ((nowHours>=11) && ((nowHours<13))){
            return CE13001430;
        }
        if ((nowHours>=13) && (((nowHours==14) &&(nowMinutes<30)) || (nowHours<14))){
            return CE14301600;
        }
        if ((nowHours>=14) && ((nowHours<16))){
            return CE16001730;
        }
        if (((nowHours>=16) && (((nowHours==17) &&(nowMinutes<30)) || (nowHours<17)))){
            return CE17301900;
        }
        else{
            return null;
        }
    }

}

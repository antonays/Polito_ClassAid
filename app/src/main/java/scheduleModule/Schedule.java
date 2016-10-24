package scheduleModule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Schedule {
    private Set<CalendarEntry> scheduleSet;    // list of all schedule entries for all courses of all years
    private List<List<String>> allCourses;          // will contain all possible courses in areas: engineering, architecture
    private int iteration;

    public enum selection {FILE, SERVER}

    ;
    private static final String SCHEDULEFILENAME = "schedule.txt";

    public Schedule(String weekStart, selection selector, File fileBasePath) throws IOException {

        scheduleSet = new HashSet<>();

        if (selector.equals(selection.FILE)) {
            scheduleSet = loadFromFile(fileBasePath);
        } else if (selector.equals(selection.SERVER)) {
            scheduleSet = loadFromServer(weekStart, fileBasePath);
        }


    }

    // load all years from server and saves to file
    private Set<CalendarEntry> loadFromServer(String weekStart, File fileBasePath) throws IOException {
        Set<CalendarEntry> tempSched = new HashSet<>();
        Set<CalendarEntry> outSched = new HashSet<>();
        tempSched.addAll(initiateMagistrale(weekStart, "Laurea Magistrale"));
        saveToFile(tempSched, fileBasePath + "/classAidRaw/" + "MagistaleBackup.txt");
        outSched.addAll(tempSched);
        tempSched.addAll(initiateLaurea(weekStart, "Laurea"));
        saveToFile(tempSched, fileBasePath + "/classAidRaw/" + "LaureaBackup.txt");
        outSched.addAll(tempSched);

        saveToFile(outSched, fileBasePath + "/classAidRaw/" + SCHEDULEFILENAME);

        return outSched;
    }

    private Set<CalendarEntry> initiateMagistrale(String weekStart, String degree) {
        iteration =0;
        Set<CalendarEntry> sched = new HashSet<>();
        allCourses = new LinkedList<>();
        try {
            sched.addAll(initiateScheduleFromServer(weekStart, "2", "AAAA", degree));
        }
        catch (ScheduleGetException e){}
        try {
            sched.addAll(initiateScheduleFromServer(weekStart, "1", "AAAA", degree));
        } catch (ScheduleGetException e){}
        return sched;
    }

    private Set<CalendarEntry> initiateLaurea(String weekStart, String degree) {
        Set<CalendarEntry> sched = new HashSet<>();
        //List<CalendarEntry> scheduleList;
        String[] firtsYearInitials = {
                "AAAA",
                "BARB",
                "BOTT",
                "CAS",
                "CORD",
                "DIG",
                "FFFF",
                "GGGG",
                "HHHH",
                "LLLL",
                "MMMM",
                "MOOO",
                "PPP",
                "RRR",
                "SSSS",
                "TTTT"
        };

        String[] secondYearInitials = {
                "AAAA",
                "MMMM"
        };
        iteration =0;
        allCourses = new LinkedList<>();
        try {
            sched.addAll(initiateScheduleFromServer(weekStart, "3", "AAAA", degree));
            //sched = toSet(scheduleList);
        }
        catch (ScheduleGetException e){}
        for (String s : secondYearInitials) {
            try {
                sched.addAll(initiateScheduleFromServer(weekStart, "2", s, degree));
            } catch (ScheduleGetException e) {
                continue;
            }
        }
        //sched.addAll(scheduleList);
        for (String s : firtsYearInitials) {
            try {
                //scheduleList = initiateScheduleFromServer(weekStart, "1", s, degree);
                //sched.addAll(scheduleList);
                sched.addAll(initiateScheduleFromServer(weekStart, "1", s, degree));
            } catch (ScheduleGetException e) {
                continue;
            }
        }
        return sched;
    }

    // retrieves a set from the file
    private Set<CalendarEntry> loadFromFile(File fileBasePath) throws IOException {
        BufferedReader br;
        Set<CalendarEntry> ls = new HashSet<>();
        String line;
        br = new BufferedReader(new FileReader(fileBasePath + "/classAidRaw/" + SCHEDULEFILENAME));
        while ((line = br.readLine()) != null) {
            ls.add(new CalendarEntry(line));
        }
        br.close();
        return ls;
    }

    private void saveToFile(Set<CalendarEntry> ls, String path) throws IOException {
        BufferedWriter bw;
        File file = new File(path);
        bw = new BufferedWriter(new FileWriter(file));
        for (CalendarEntry ce : ls) {
            bw.write(ce.stringToFile());
            bw.newLine();
        }
        bw.flush();
        bw.close();

        /*FileWriter bw;
        File newf = new File(fileBasePath+"/testing.txt");
        if (!newf.exists()){
            newf.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(newf);
        bw = new FileWriter(fos.getFD());
        bw.write("abcdefg");
        bw.close();
        fos.getFD().sync();
        fos.close();*/
    }

    public Set<CalendarEntry> getList() {
        return scheduleSet;
    }

    private List<CalendarEntry> initiateScheduleFromServer(String WeekStart, String year, String studentInitials, String degree) throws ScheduleGetException{
        List<CalendarEntry> chIngInitial = new LinkedList<>();  // will contain first request for engineering courses, with the course name for computer engineering
        List<CalendarEntry> chArchInitial = new LinkedList<>(); // will contain first architecture request for architecture science
        List<CalendarEntry> output = new LinkedList<>();

        //first call - will initiate first query and populate scheduleList using this courses
        //will also exploit the html scraping of formSender to collect information about all courses
        if (iteration == 0) {
            try {
                if (degree.equals("Laurea")) {
                    chIngInitial = scheduleInstance("Ingegneria", "INGEGNERIA INFORMATICA INGEGNERIA DELL'INFORMAZIONE", WeekStart, year, studentInitials, degree);
                    iteration++;
                    chArchInitial = scheduleInstance("Architettura", "ARCHITETTURA SCIENZE DELL'ARCHITETTURA", WeekStart, year, studentInitials, degree);
                    iteration++;
                }
                if (degree.equals("Laurea Magistrale")) {
                    chIngInitial = scheduleInstance("Ingegneria", "INGEGNERIA CHIMICA E DEI PROCESSI SOSTENIBILI INGEGNERIA CHIMICA", WeekStart, year, studentInitials, degree);
                    iteration++;
                    chArchInitial = scheduleInstance("Architettura", "ECODESIGN DESIGN", WeekStart, year, studentInitials, degree);
                    iteration++;
                }
            } catch (Exception e) {
                throw new ScheduleGetException();
            }
            output.addAll(chIngInitial);
            output.addAll(chArchInitial);
        }

        // second call - use the information about all courses, to iterate over them and collect all courses schedules
        for (String course : allCourses.get(0)) {
            try {
                output.addAll(scheduleInstance("Ingegneria", course, WeekStart, year, studentInitials, degree));
                iteration++;
            } catch (Exception e) {
                continue;
            }

        }
        for (String course : allCourses.get(1)) {

            try {
                if (year.equals("1") && degree.equals("Laurea")) {
                    if (studentInitials.equals("AAAA") || studentInitials.equals("CAS") || studentInitials.equals("DIG") || studentInitials.equals("LLLL")
                            || studentInitials.equals("PPP") || studentInitials.equals("SSSS"))
                        output.addAll(scheduleInstance("Architettura", course, WeekStart, year, studentInitials, degree));
                        iteration++;
                }
                else
                {
                    output.addAll(scheduleInstance("Architettura", course, WeekStart, year, studentInitials, degree));
                    iteration++;
                }
            } catch (Exception e) {
                continue;
            }

        }
        return output;
    }

    private List<CalendarEntry> scheduleInstance(String area, String course, String date, String year,
                                                 String Nameinitials, String degree)
            throws IOException, ScheduleGetException {

        List<CalendarEntry> scheduleInstance = new LinkedList<>(); // this will contain a list of calendar entries --> generic class
        CalendarHandler ch;

        try {
            ch = new CalendarHandler();

            //ch.setFieldDescriptionContains(OrariLezioni.ANNO_ACCADEMICO,"2014/2015");
            //ch.setFieldDescriptionContains(OrariLezioni.SEDE, "TORINO");

            ch.setFieldDescriptionContains(CalendarHandler.LAUREA, degree);
            ch.setFieldDescriptionContains(CalendarHandler.AREA, area);
            ch.setFieldDescriptionContains(CalendarHandler.CORSO, course);

            // get second form
            ch.next();

            ch.setField(CalendarHandler.ANNO, year);
            ch.setField(CalendarHandler.ORIENTAMENTO, "");
            ch.setField(CalendarHandler.INIZIALI, Nameinitials);

            // the start of the week of the schedule
            ch.setDate(CalendarEntry.stringToDate(date));

            // use poli app object CalendarHandler (original orariLezioni) to populate the calendar for this week
            scheduleInstance = ch.schedule();

            // get the list of all possible courses from select box in the form, then remove the current course
            // do this only for the first two iterations, when doing initial query with
            if (iteration < 2) {
                List<String> tmpOptions = getMaxList(ch.getOptionsFromFormSender());
                tmpOptions.remove(getMatch(tmpOptions, course));
                allCourses.add(tmpOptions);
            }
        }
        catch (ScheduleGetException e){
            throw e;
        } catch (IOException e){
            throw e;
        }catch (Exception e) {
            throw new ScheduleGetException();
        }

        return scheduleInstance;
    }

    private int getMatch(List<String> ls, String s) {
        int i = 0;
        for (String st : ls) {
            if (st.equals(s)) {
                return i;
            }
            i++;
        }
        return i;
    }

    private List<String> getMaxList(List<List<String>> ls) {
        List<String> tmp = ls.get(0);
        for (List<String> list : ls) {
            if (list.size() > tmp.size()) {
                tmp = list;
            }
        }
        return tmp;
    }
}

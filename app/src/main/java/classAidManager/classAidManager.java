package classAidManager;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import scheduleModule.CalendarEntry;
import scheduleModule.ParserUtil;
import scheduleModule.Schedule;


public class classAidManager {
    Set<CalendarEntry> scheduleList = new HashSet<>();
    Map<String, Room> rooms = new HashMap<>();
    Map<String, Course> courses = new HashMap<>();
    Map<String, Student> studentDatabase = new HashMap<>();
    SimpleWeightedGraph<Room, DefaultWeightedEdge> roomGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    String roomFilePath;
    String studentsFilePath;
    private static final String ROOMSFILENAME="Rooms.txt";
    private static final String STUDENTSFILENAME="students.txt";

    public classAidManager(File appDirPath, Set<CalendarEntry> sched) throws IOException{
        roomFilePath = appDirPath+"/classAidRaw/"+ROOMSFILENAME;
        studentsFilePath = appDirPath+"/classAidRaw/"+STUDENTSFILENAME;
        scheduleList = sched;

        setUpRooms();
        setUpRoomSchedule();

        loadStudents();

        //setNearFreeRooms();
    }

    public String getNearFreeRoomName(Room r){
        return r.recursiveFreeRoomFromGraph(roomGraph).getName();
    }

    public Map<String, Room> getRooms(){
        return rooms;
    }

    public void setUpRooms() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(roomFilePath));

        for (String line; (line = br.readLine()) != null; ) {
            try {
                String[] splitted = ParserUtil.text(line).replace("\n", ";").split(";");
                String initial = splitted[0];
                if (!rooms.containsKey(initial)) {
                    Room newroom = new Room(initial);
                    rooms.put(initial, newroom);
                    roomGraph.addVertex(newroom);
                }
                for (int i = 1; i < splitted.length; i++) {
                    String[] secondSplit = ParserUtil.text(splitted[i]).split(",");
                    if (!rooms.containsKey(secondSplit[0])) {
                        Room newroom = new Room(secondSplit[0]);
                        rooms.put(secondSplit[0], newroom);
                        roomGraph.addVertex(newroom);
                    }
                    if (!roomGraph.containsEdge(rooms.get(initial), rooms.get(secondSplit[0]))) {
                        DefaultWeightedEdge e1 = roomGraph.addEdge(rooms.get(initial), rooms.get(secondSplit[0]));
                        roomGraph.setEdgeWeight(e1, Integer.parseInt(secondSplit[1]));

                    }
                    //DefaultWeightedEdge e = roomGraph.addEdge(rooms.get(initial), rooms.get(secondSplit[0]));
                    //if (e != null) {
                    //    int tmpxx = Integer.parseInt(secondSplit[1]);
                    //    roomGraph.setEdgeWeight(e, tmpxx);
                    //}
                }
            }
            catch (Exception e){
                throw new IOException("rooms file loading problem");
            }
        }
        br.close();
    }

    public void setNearFreeRooms(){
        for (Room r : roomGraph.vertexSet()) {
            for (DefaultWeightedEdge e : roomGraph.edgesOf(r)) { //iterate through every edge reaching r
                if (roomGraph.getEdgeSource(e).equals(r)) {
                    if (roomGraph.getEdgeTarget(e).getAvailability()) {
                        if (r.getNearestFreeRoom()==null) {r.setNearestFreeRoom(roomGraph.getEdgeTarget(e));}
                        else {
                            if (roomGraph.getEdgeWeight(roomGraph.getEdge(r, roomGraph.getEdgeTarget(e)))
                                    < roomGraph.getEdgeWeight(e)) {
                                r.setNearestFreeRoom(roomGraph.getEdgeTarget(e));
                            }
                        }
                    }
                } else if (roomGraph.getEdgeTarget(e).equals(r)) {
                    if (roomGraph.getEdgeSource(e).getAvailability()) {
                        if (r.getNearestFreeRoom()==null) {r.setNearestFreeRoom(roomGraph.getEdgeSource(e));}
                        else {
                            if (roomGraph.getEdgeWeight(roomGraph.getEdge(r, roomGraph.getEdgeSource(e)))
                                    < roomGraph.getEdgeWeight(e)) {
                                r.setNearestFreeRoom(roomGraph.getEdgeSource(e));
                            }
                        }
                    }
                }
            }
        }
    }

    public void setUpRoomSchedule(){
        for (CalendarEntry ce : scheduleList){
            try {
                rooms.get(ce.getRoom()).addScheduleToRoom(ce);
                if (courses.containsKey(ce.getName()+ce.getTeacher())) {
                    courses.get(ce.getName()+ce.getTeacher()).addSchedule(ce);
                } else {
                    courses.put(ce.getName()+ce.getTeacher(), new Course(ce.getName(), ce.getTeacher()));
                    courses.get(ce.getName()+ce.getTeacher()).addSchedule(ce);
                }
            } catch (Exception e) {
                //DO NOTHING: we just try-catch exception to run the program even if some calendarentries in the txt are not formatted
                //correctly
            }
        }
    }
    public void loadStudents() throws IOException{
        try {
            BufferedReader br = new BufferedReader(new FileReader(studentsFilePath));
            for (String line; (line = br.readLine()) != null; ) {
                String[] splitted = ParserUtil.text(line).split("@");
                Student s = new Student(splitted[0]);
                studentDatabase.put(splitted[0], s);
                for (int i = 1; i < splitted.length; i++) {
                    studentDatabase.get(splitted[0])
                            .addFollowingCourseToStudent(courses.get(splitted[i]));
                }
            }
        } catch (IOException e) {
               throw new IOException("Problem loading student File");
        }
    }

    public Student getStudentByID(String ID){
        if (studentDatabase.containsKey(ID)){
            return studentDatabase.get(ID);
        }
        else{
            return null;
        }
    }
}

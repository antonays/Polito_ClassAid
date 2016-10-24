package classAidManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import scheduleModule.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Room implements Serializable{

    private String name;
	private boolean occupiedBoolean = false;
	private HashSet<CalendarEntry> calendar;
    private Room nearestFreeRoom;

	public Room(String name) {
		this.name = name;
		this.calendar = new HashSet<>();
        checkAvailability();
	}

	public String getName() {
		return name;
	}
	
	public void getMap(){
		//TODO
		//return null;
	}

    public boolean getAvailability(){
        boolean b = checkAvailability();
        return b;
    }
		
	public void addScheduleToRoom (CalendarEntry entry){
		calendar.add(entry);
	}
	
	public HashSet<CalendarEntry> getCalendar(){
		return calendar;
	}
	
	public boolean checkAvailability(){
		GregorianCalendar now = new GregorianCalendar();
		Boolean occupied = false;
		
		for (CalendarEntry ce : calendar) {
			if ((now.after(ce.getStart()) && now.before(ce.getEnd())) || occupiedBoolean) {
				occupied = true;
                return false;
			}			
		}
		setOccupy(occupied);
		return true;
	}
	
	public void setOccupy(boolean occupied) {
		this.occupiedBoolean = occupied;
	}

    public void setNearestFreeRoom (Room r) {
        nearestFreeRoom = r;
    }

    public Room getNearestFreeRoom () {
        return nearestFreeRoom;
    }

    public Room recursiveFreeRoomFromGraph (SimpleWeightedGraph<Room, DefaultWeightedEdge> roomGraph) {
        		for (DefaultWeightedEdge ed : roomGraph.edgesOf(this)) {
            			if (roomGraph.getEdgeSource(ed).equals(this)) {
                				if (roomGraph.getEdgeTarget(ed).getAvailability()) {
                    					if (this.getNearestFreeRoom()==null) {this.setNearestFreeRoom(roomGraph.getEdgeTarget(ed));}
                    					else {
                        						if (roomGraph.getEdgeWeight(roomGraph.getEdge(this, roomGraph.getEdgeTarget(ed)))
                                								< roomGraph.getEdgeWeight(ed)) {
                            							this.setNearestFreeRoom(roomGraph.getEdgeTarget(ed));
                            						}
                        					}
                    				}
                			} else if (roomGraph.getEdgeTarget(ed).equals(this)) {
                				if (roomGraph.getEdgeSource(ed).getAvailability()) {
                    					if (this.getNearestFreeRoom()==null) {this.setNearestFreeRoom(roomGraph.getEdgeSource(ed));}
                    					else {
                        						if (roomGraph.getEdgeWeight(roomGraph.getEdge(this, roomGraph.getEdgeSource(ed)))
                                								< roomGraph.getEdgeWeight(ed)) {
                            							this.setNearestFreeRoom(roomGraph.getEdgeSource(ed));
                            						}
                        					}
                    				}
                			}

                    			if (this.getNearestFreeRoom()==null) {
                				if (roomGraph.getEdgeSource(ed).equals(this)) {
                    					setNearestFreeRoom(roomGraph.getEdgeTarget(ed).recursiveFreeRoomFromGraph(roomGraph));
                    				}
                				if (roomGraph.getEdgeTarget(ed).equals(this)) {
                    					setNearestFreeRoom(roomGraph.getEdgeSource(ed).recursiveFreeRoomFromGraph(roomGraph));
                    				}
                			}
            		}

                		return this.getNearestFreeRoom();
        	}
}

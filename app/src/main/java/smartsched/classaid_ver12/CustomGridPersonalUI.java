package smartsched.classaid_ver12;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import scheduleModule.CalendarEntry;
import scheduleModule.studyDay;

public class CustomGridPersonalUI extends BaseAdapter{
    private Context mContext;
    private String[] professorValues;
    private String[] courseValues;
    private String[] roomValues;


    public CustomGridPersonalUI(Context c, studyDay sd) {
        mContext = c;

        setUpGridStrings(sd);
    }

    public void setUpGridStrings(studyDay sd){
        professorValues = new String[] {
                "",""
                ,"8:30->10:00",""
                ,"10:00->11:30",""
                ,"11:30->13:00",""
                ,"13:00->14:30",""
                ,"14:30->16:00",""
                ,"16:00->17:30",""
                ,"17:30->19:00",""};
        courseValues = new String[] {
                "",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""};
        roomValues = new String[] {
                "",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""
                ,"",""};


        try{
            professorValues[1] = sd.getDayText();
            int j=0;
            for (int i=3;i<16;i=i+2){
                CalendarEntry ce = sd.getHourSlotAsCollection().get(j);
                if (ce!=null) {
                    professorValues[i] = ce.getName();
                    courseValues[i] = ce.getTeacher();
                    roomValues[i] = ce.getRoom();
                }
                j++;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return professorValues.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            //Inserting data in the grid from the 3 arrays
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            textView.setText(professorValues[position]);
            TextView textView2 = (TextView) grid.findViewById(R.id.grid_text2);
            textView2.setText(courseValues[position]);
            TextView textView3 = (TextView) grid.findViewById(R.id.grid_text3);
            textView3.setText(roomValues[position]);
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
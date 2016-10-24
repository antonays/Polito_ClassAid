package smartsched.classaid_ver12;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import scheduleModule.CalendarEntry;
import scheduleModule.studyDay;

public class CustomGrid extends BaseAdapter{
    private Context mContext;
    private String[] professorValues;
    private String[] courseValues;
    private String[] roomValues;

    private int CELLCOUNT = 6*8;

    public CustomGrid(Context c, Map<Integer,studyDay> pd) {
        mContext = c;
        setUpGridStrings(pd);
    }

    private void setUpGridStrings(Map<Integer,studyDay> perDays) {
        professorValues = new String[] {
                "","Monday","Tuesday","Wednesday","Thursday","Friday"
                ,"8:30->10:00","","","","",""
                ,"10:00->11:30","","","","",""
                ,"11:30->13:00","","","","",""
                ,"13:00->14:30","","","","",""
                ,"14:30->16:00","","","","",""
                ,"16:00->17:30","","","","",""
                ,"17:30->19:00","","","","",""};
        courseValues = new String[] {
                "","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""};
        roomValues = new String[] {
                "","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""
                ,"","","","","",""};

        try {
            int k=0;
            for (int i = 8; i <= 48; i += 6) {

                for (int j = 2; j <= 6; j++) {
                        CalendarEntry ce = perDays.get(j).getHourSlotAsCollection().get(k);
                        if (ce!=null) {
                            professorValues[i+j-3] = ce.getName();
                            courseValues[i+j-3] = ce.getTeacher();
                            //roomValues[i+j-3] = ce.getRoom();
                        }

                }
                k++;

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return CELLCOUNT;
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
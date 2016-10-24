package fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import scheduleModule.studyDay;
import smartsched.classaid_ver12.CustomGridPersonalUI;
import smartsched.classaid_ver12.MainActivity;
import smartsched.classaid_ver12.R;

public class StudentViewCalendarFragment extends Fragment implements CalendarFragmentCommunicator {
    public Context context;
    String studentId;
    View view;
    GridView gridFragment;
    studyDay daySchedule;
    TextView studentSSN;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.student_info_fragment,container,false);


        return view;
    }

    /*@Override
    public void onStop(){
        super.onStop();
        gridFragment = null;
        daySchedule = null;
        studentSSN = null;
    }*/

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = getActivity();
        ((MainActivity)context).fragmentCommunicator = this;
    }

    private void setStudentNumber(String id, studyDay sd){
        studentId = id;
        studentSSN = (TextView) view.findViewById(R.id.studentInfo);
        studentSSN.setText("Student: "+id);
        gridFragment = (GridView) view.findViewById(R.id.gridViewFragmentId);
        CustomGridPersonalUI adapter = new CustomGridPersonalUI(view.getContext(),sd);
        gridFragment.setAdapter(adapter);
    }

    @Override
    public void passDataToFragment(studyDay someValue, String idnumber) {
        daySchedule = someValue;
        studentId = idnumber;
        setStudentNumber(idnumber,daySchedule);
    }
}


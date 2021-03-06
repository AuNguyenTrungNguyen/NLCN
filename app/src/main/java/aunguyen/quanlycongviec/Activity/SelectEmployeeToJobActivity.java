package aunguyen.quanlycongviec.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import aunguyen.quanlycongviec.Adapter.SelectEmployeeToJobAdapter;
import aunguyen.quanlycongviec.Object.Constant;
import aunguyen.quanlycongviec.Object.EmployeeObject;
import aunguyen.quanlycongviec.R;

public class SelectEmployeeToJobActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbarEmployee;

    private Button btnAdd;
    private TextView tvMessage;

    private RecyclerView rvEmployee;
    private List<EmployeeObject> listEmployees;
    private SelectEmployeeToJobAdapter employeeAdapter;
    private List<Boolean> listCheck;
    private List<EmployeeObject> temp = new ArrayList<>();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_selected);

        setUpToolbar();

        addControls();

        addEvents();

        loadDataFromFireBase();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent != null) {
            List<EmployeeObject> listEmployeeAdded = (List<EmployeeObject>) intent.getSerializableExtra("LIST_EMPLOYEE_ADDED");

            temp.addAll(listEmployees);

            //Loại bỏ bị trùng
            for (int i = 0; i < listEmployeeAdded.size(); i++) {
                EmployeeObject added = listEmployeeAdded.get(i);
                for (int j = 0; j < temp.size(); j++) {
                    EmployeeObject employee = temp.get(j);
                    if (added.getIdEmployee().equals(employee.getIdEmployee())) {
                        int posi = j - i;
                        listEmployees.remove(posi);
                        listCheck.remove(posi);
                        employeeAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            progressDialog.dismiss();
        }
    }

    private void loadDataFromFireBase() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.dialog));
        progressDialog.show();
        SharedPreferences preferences = this.getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        final String id = preferences.getString(Constant.PREFERENCE_KEY_ID, null);

        if (id != null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constant.NODE_NHAN_VIEN);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvMessage.setVisibility(View.VISIBLE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        EmployeeObject employeeObject = snapshot.getValue(EmployeeObject.class);

                        assert employeeObject != null;
                        if (id.equals(employeeObject.getIdManage())) {
                            listEmployees.add(employeeObject);
                            listCheck.add(false);
                            employeeAdapter.notifyDataSetChanged();
                        }
                    }

                    if (listEmployees.size() > 0) {
                        tvMessage.setVisibility(View.GONE);
                    }
                    getData();
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressDialog.dismiss();
                }
            });

        } else {
            progressDialog.dismiss();
        }
    }

    private void addEvents() {
        btnAdd.setOnClickListener(this);
    }

    private void addControls() {
        btnAdd = findViewById(R.id.btn_add);
        tvMessage = findViewById(R.id.tv_message);

        rvEmployee = findViewById(R.id.rv_employee);
        listEmployees = new ArrayList<>();
        listCheck = new ArrayList<>();
        employeeAdapter = new SelectEmployeeToJobAdapter(this, listEmployees, listCheck);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rvEmployee.setAdapter(employeeAdapter);
        rvEmployee.setLayoutManager(manager);

    }

    private void setUpToolbar() {
        toolbarEmployee = findViewById(R.id.toolbar_employee);
        setSupportActionBar(toolbarEmployee);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                addEmployees();
                break;
        }
    }

    private void addEmployees() {
        ArrayList<EmployeeObject> list = new ArrayList<>();
        for (int i = 0; i < listCheck.size(); i++) {
            if (listCheck.get(i)) {
                list.add(listEmployees.get(i));
            }
        }

        Intent intent = new Intent();
        intent.putExtra("LIST_EMPLOYEE", list);
        intent.putExtra("MAX", temp.size());
        setResult(Constant.RESULT_CODE, intent);
        finish();
    }

}

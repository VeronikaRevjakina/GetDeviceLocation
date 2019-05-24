package com.test.getdevicelocation2;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MaleRMRActivity extends MainActivity {

    private Button buttonCalc;
    private EditText ageEdit;
    private EditText heightEdit;
    private EditText weightEdit;
    private TextView resultRMRText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_find_rmr);

        buttonCalc=findViewById(R.id.buttonCaclRMR);
        ageEdit=findViewById(R.id.ageEdit);
        heightEdit=findViewById(R.id.heightEdit);
        weightEdit=findViewById(R.id.weightEdit);
        resultRMRText=findViewById(R.id.resultCal);

        buttonCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result="Your BMR: ";
                getMaleRMRUsingUsersEditParameters();
            }


        });
    }

    private void getMaleRMRUsingUsersEditParameters() {
        int sex=0;
        if(ageEdit.getText().length()!=0 & heightEdit.getText().length()!=0 & weightEdit.getText().length()!=0) {
            int age = Integer.parseInt(ageEdit.getText().toString());
            double weight=Double.parseDouble(weightEdit.getText().toString());
            double height=Double.parseDouble(heightEdit.getText().toString());
            if(ageEdit.getText().length()!=1 & heightEdit.getText().length()!=1 & weightEdit.getText().length()!=1) {
                double resultRMR=countRMRUsingMifflinJeorEquation(sex,weight,height,age);
                resultRMRText.setText(String.valueOf(resultRMR));
                setRMR(resultRMR);
            }
            else{
                Toast.makeText(MaleRMRActivity.this, "Incorrect data.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(MaleRMRActivity.this, "Incorrect data.", Toast.LENGTH_SHORT).show();

        }
    }
}

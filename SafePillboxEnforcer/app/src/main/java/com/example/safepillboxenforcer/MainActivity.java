package com.example.safepillboxenforcer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.asmeta.framework.enforcer.Enforcer;
import org.asmeta.framework.enforcer.FeedbackLoop;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final boolean SELECT_FILE = false;
    public static final int REQUEST_CODE_SELECT_SPEC = 1;
    public static final int cursorIndexURI = 0;
    public static final int cursorIndexFileName = 2;
    public static final int TEXT_SIZE = 20;
    public String path="";
    PillBox managedSystem;
    EnforcerPillBox e;
    ArrayList<Switch> switchListMed= new ArrayList<>();
    ArrayList<String> medicineList= new ArrayList<>();
    LinearLayout switchLayout;
    TextView patientOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        switchLayout=(LinearLayout) findViewById(R.id.linearLayoutUser);
        selectSpec();
        userLayout();
    }

    private void userLayout(){
        LinearLayout userLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutUser);
        //Patient output message
        patientOutput = new TextView(this);
        patientOutput.setTextSize(18);
        patientOutput.setHint("Patient Output");
        patientOutput.setId(R.id.patient_output);
        patientOutput.setTextSize(TEXT_SIZE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,20,0,20);
        patientOutput.setLayoutParams(params);
        userLinearLayout.addView(patientOutput);
        //First med layout
        LinearLayout med1Layout=new LinearLayout(this);
        med1Layout.setId(R.id.led_Layout);
        med1Layout.setOrientation(LinearLayout.HORIZONTAL);
        userLinearLayout.addView(med1Layout,1);
        //Btn first med to simulate led
        Button redLed1 = new Button(this);
        redLed1.setId(R.id.red_led_1);
        redLed1.setText(getString(R.string._360));
        redLed1.setTextSize(TEXT_SIZE);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(20,0,20,0);
        redLed1.setLayoutParams(params1);
        med1Layout.addView(redLed1);
        //First med switch
        Switch switchMed1 = new Switch(this);
        switchMed1.setText(getString(R.string.fosamax));
        switchMed1.setTextSize(18);
        switchMed1.setId(R.id.switch_comp1);
        switchListMed.add(switchMed1);
        switchMed1.setTextSize(TEXT_SIZE);
        med1Layout.addView(switchMed1);
        //Second med layout
        LinearLayout med2Layout=new LinearLayout(this);
        med2Layout.setId(R.id.led_Layout);
        med2Layout.setOrientation(LinearLayout.HORIZONTAL);
        userLinearLayout.addView(med2Layout,2);
        //Btn second med to simulate led
        Button redLed2 = new Button(this);
        redLed2.setId(R.id.red_led_2);
        redLed2.setText(getString(R.string._730));
        redLed2.setLayoutParams(params1);
        redLed2.setTextSize(TEXT_SIZE);
        med2Layout.addView(redLed2);
        //Second med switch
        Switch switchMed2 = new Switch(this);
        switchMed2.setText(getString(R.string.moment));
        switchMed2.setTextSize(18);
        switchMed2.setId(R.id.switch_comp2);
        switchListMed.add(switchMed2);
        switchMed2.setTextSize(TEXT_SIZE);
        med2Layout.addView(switchMed2);
        //Simulated Time input
        EditText insertTime = new EditText(this);
        insertTime.setTextSize(18);
        insertTime.setHint("Insert simulated time here (minutes)");
        insertTime.setId(R.id.edit_Time);
        insertTime.setInputType(InputType.TYPE_CLASS_NUMBER);
        insertTime.setTextSize(TEXT_SIZE);
        userLinearLayout.addView(insertTime);
    }


    private void selectSpec() {
        //If SELECT_FILE is false use default path
        if (!SELECT_FILE) {
            path = "storage/11F6-2C09/Asmeta/";
            initEnforcer(path);
        }else
        {
            Button selectButton = new Button(this);
            selectButton.setId(R.id.select_specification);
            selectButton.setText(R.string.select_specification);
            selectButton.setOnClickListener(v -> selectFile(v));
            switchLayout.addView(selectButton);
        }
    }

    //If select specification button is visible open the intent to select the file
    public void selectFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_SPEC);
    }

    /*
    Request permissions
     */
    private boolean requestPermission(){
        boolean request=true;
        String[] permissions={Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        if (permissions.length!=0){
            ActivityCompat.requestPermissions(this,permissions,102);
            request= true;
        }
        else{
            System.out.println("Permissions not allowed by User...");
            request=false;
        }
        return request;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_SPEC) {
            path = "storage/"+getRealPathFromURI(getApplicationContext(), data.getData());
            initEnforcer(path);
        }
    }

    /*
    Get path given the URI
     */
    private String getRealPathFromURI(Context context, Uri contentURI) {
        String filePath = null;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            filePath = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            filePath = cursor.getString(cursorIndexURI);
            String fileName = cursor.getString(cursorIndexFileName);
            filePath=filePath.substring(0,(filePath.length()-fileName.length())).replace(":","/");
            cursor.close();
        }
        return filePath;
    }

    /*
    Init the Enforcer
     */
    public void initEnforcer(String path){
        managedSystem = new PillBox(path+"pillbox.asm");
        KnowledgePillBox k = new KnowledgePillBox();
        FeedbackLoop loop = new FeedbackLoopPillBox(managedSystem.getProbe(),managedSystem.getEffector(),k,findViewById(R.id.tableOutput));
        Enforcer.setConfigFile(path+"config.properties");
        e = new EnforcerPillBox(managedSystem,k,loop,path);
    }

    //Build user input for the enforcer
    public String inputEnforcer(){
        String in = "systemTime ";
        EditText insertTime =findViewById(R.id.edit_Time);
        Switch switchComp1= findViewById(R.id.switch_comp1);
        Switch switchComp2= findViewById(R.id.switch_comp2);
        in+=insertTime.getText().toString();
        in+=" openSwitch(compartment1) ";
        in +=switchComp1.isChecked() ;
        in+=" openSwitch(compartment2) ";
        in +=switchComp2.isChecked() + " ";
        System.out.println(in);
        return in;
    }

    //RUN ENFORCER STEP
    public void runStepEnforcer(View v){
        String output="";
        e.sanitiseInput(inputEnforcer());
        output+="(Sanitised) User input:~$ "+e.getSanitisedInput().toString()+"\n";
        output+="Pillbox running..."+"\n";
        managedSystem.run(e.getSanitisedInput()); //the managed system runs
        output+="Output to patient:~$"+managedSystem.getOutputToPatient().toString()+"\n";
        output+="Output for probing:~$ "+ managedSystem.getOutputForProbing().toString()+"\n";
        output+="Enforcement feedback loop starting..."+"\n";
        long execTime = e.runLoop(); //system output sanitisation by monitoring and adaptation
        output+="Enforcement feedback loop execution time (in milliseconds): "+execTime/ 1000000 +" ms";
        System.out.println(output);
        printPatientOutput(managedSystem.getOutputToPatient());
        printProbingOutput(managedSystem.getOutputForProbing());
    }

    //Format probing output based on the layout
    private void printProbingOutput(Map<String, String> outputForProbing) {
        TextView x = null;
        for (String key : outputForProbing.keySet()) {
            //Display actual time consumption
            if (key.startsWith("actual_time_consumption")) {
                String compartmentName = key.substring(key.length() - 13, key.length() - 1);
                int index = Integer.parseInt(outputForProbing.get("drugIndex(" + compartmentName + ")"));
                if (compartmentName.equalsIgnoreCase(getString(R.string.compartment1))){
                    x = (TextView) findViewById(R.id.timeConsumptionRow2);
                    String timeConsumption = outputForProbing.get(key);
                    x.setText(timeConsumption.substring(1,timeConsumption.length()-1));
                }
                if (compartmentName.equalsIgnoreCase(getString(R.string.compartment2))){
                    String timeConsumption = outputForProbing.get(key);
                    String[] timeSplit=timeConsumption.substring(1,timeConsumption.length()-1).split(",");
                    if (index==1) {
                       x = (TextView) findViewById(R.id.timeConsumptionRow3);
                       x.setText(timeSplit[0]);
                   }else if (index==0){
                       x = (TextView) findViewById(R.id.timeConsumptionRow4);
                       x.setText(timeSplit[1]);
                   }
                }
            }
            Button b = null;
            if (key.startsWith("time_consumption")) {
                String compartmentName = key.substring(key.length() - 13, key.length() - 1);
                int index = Integer.parseInt(outputForProbing.get("drugIndex(" + compartmentName + ")"));
                if (compartmentName.equalsIgnoreCase(getString(R.string.compartment1))){
                    b = (Button) findViewById(R.id.red_led_1);
                    String time = outputForProbing.get(key);
                    b.setText(time.substring(1,time.length()-1));
                }
                if (compartmentName.equalsIgnoreCase(getString(R.string.compartment2))){
                    if (index==0) {
                        b = (Button) findViewById(R.id.red_led_2);
                    }else if (index==1){
                        b = (Button) findViewById(R.id.red_led_2);
                    }
                    String time = outputForProbing.get(key);
                    time = time.substring(1,time.length()-1);
                    String[] timeSplit=time.split(",");
                    b.setText(timeSplit[index]);
                }
            }
        }
    }

    private void printPatientOutput(Map<String, String> outputToPatient) {
        String outMess="";
        for (String key : outputToPatient.keySet()) {
            if (key.startsWith("outMess")){
               outMess = outMess+ outputToPatient.get(key)+" - " ;
            }else if (key.startsWith("redLed(compartment1)")){
                setLedColor(R.id.red_led_1,outputToPatient.get(key));
            }else if(key.startsWith("redLed(compartment2)")){
                setLedColor(R.id.red_led_2,outputToPatient.get(key));
            }
        }
        patientOutput.setText(outMess);
    }

    private void setLedColor(int red_led, String status) {
        Button red_led_btn=findViewById(red_led);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the blinking time with this parameter
        anim.setStartOffset(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        if (status.equalsIgnoreCase("BLINKING")){
            red_led_btn.startAnimation(anim);
            red_led_btn.setBackgroundColor(Color.RED);
        }else if(status.equalsIgnoreCase("ON")){
            red_led_btn.clearAnimation();
            anim.cancel();
            red_led_btn.setBackgroundColor(Color.RED);
        }else if(status.equalsIgnoreCase("OFF")){
            red_led_btn.clearAnimation();
            anim.cancel();
            red_led_btn.setBackgroundResource(android.R.drawable.btn_default);
        }
    }


}
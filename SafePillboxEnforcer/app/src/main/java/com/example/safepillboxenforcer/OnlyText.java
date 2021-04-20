package com.example.safepillboxenforcer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.asmeta.framework.enforcer.Enforcer;
import org.asmeta.framework.enforcer.FeedbackLoop;
import org.asmeta.framework.enforcerPillBox1.EnforcerPillBox;
import org.asmeta.framework.enforcerPillBox1.FeedbackLoopPillBox;
import org.asmeta.framework.enforcerPillBox1.KnowledgePB;
import org.asmeta.framework.pillBox.PillBoxNotSing;

public class OnlyText extends AppCompatActivity {

    public static final int REQUEST_CODE_SELECT_SPEC = 1;
    public static final int cursorIndexURI = 0;
    public static final int cursorIndexFileName = 2;
    public String path="";
    PillBoxNotSing managedSystem;
    EnforcerPillBox e;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        Button btnSelectSpec = findViewById(R.id.btnSelect);
        //If button is invisible use default path
        if (btnSelectSpec.getVisibility()== View.INVISIBLE) {
            path = "storage/11F6-2C09/Asmeta/";
            initEnforcer(path);
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
        managedSystem = new PillBoxNotSing(path+"pillbox.asm");
        KnowledgePB k = new KnowledgePB();
        FeedbackLoop loop = new FeedbackLoopPillBox(managedSystem.getProbe(),managedSystem.getEffector(),k);
        Enforcer.setConfigFile(path+"config.properties");
        e = new EnforcerPillBox(managedSystem,k,loop,path);
    }

    //RUN ENFORCER STEP
    public void runStep(View v){
        String output="";
        TextView outText = findViewById(R.id.textOutput);
        outText.setText("");
        EditText input = findViewById(R.id.editInput);
        e.sanitiseInput(input.getText().toString());
        output+="(Sanitised) User input:~$ "+e.getSanitisedInput().toString()+"\n";
        output+="Pillbox running..."+"\n";
        managedSystem.run(e.getSanitisedInput()); //the managed system runs
        output+="Output to patient:~$"+managedSystem.getOutputToPatient().toString()+"\n";
        output+="Output for probing:~$ "+ managedSystem.getOutputForProbing().toString()+"\n";
        output+="Enforcement feedback loop starting..."+"\n";
        long execTime = e.runLoop(); //system output sanitisation by monitoring and adaptation
        output+="Enforcement feedback loop execution time (in milliseconds): "+execTime/ 1000000 +" ms";
        outText.setText(output);
    }

}
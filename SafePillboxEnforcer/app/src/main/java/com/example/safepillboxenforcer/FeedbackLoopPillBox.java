package com.example.safepillboxenforcer;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import org.asmeta.framework.enforcer.FeedbackLoop;
import org.asmeta.framework.enforcer.Knowledge;
import org.asmeta.framework.managedSystem.Effector;
import org.asmeta.framework.managedSystem.Probe;
import org.asmeta.runtime_container.Esit;
import org.asmeta.runtime_container.RunOutput;

import java.util.HashMap;
import java.util.Map;

public class FeedbackLoopPillBox extends FeedbackLoop {
    KnowledgePillBox kPB; //casted version
    PillBox probePB, effectorPB;
    Map<String, String> myState;
    TableLayout v;

    public FeedbackLoopPillBox(Probe probe, Effector effector, Knowledge k, TableLayout v) {
        super(probe, effector, k);
        kPB = (KnowledgePillBox) this.getKnowledge();
        probePB = (PillBox) this.getProbe();
        effectorPB = (PillBox) this.getEffector();
        this.v = v;

    }

    @Override
    public void monitor() {
        //Read and save the observed probe values from the PillBox into the knowledge
        //and check if system changed
        if (kPB.systemStateChanged(probePB.getOutputForProbing()))
            //if system changed, perform analysis (enforcement by adaptation)
            analysis();

    }


    @Override
    public void analysis() {
        // analyze all knowledge settings
        boolean adaptationRequired = analyzeKnowledge();
        // if adaptation is required, invoke the planner
        if (adaptationRequired) {
            planning();
        }
    }


    private boolean analyzeKnowledge() {
        return true; //We could invoke here only the ASM pillbox_sanitiser for checking invariants (safety assertions) only with the model rolling back in case of violation
    }

    @Override
    public void planning() {
        //Output sanitisation made by the ASM runtime model: make an ASM evaluation step from the current system state and the new user input (as saved/sanitized into the knowledge)
        System.out.println("Output sanitisation made by the enforcement model...");
        RunOutput result = eval(kPB.getProbes());
        //Usage:
        //result.getEsit(); //SAFE or UNSAFE
        //result.getResult(); //Timeout expired or not
        //result.getControlledvalues(); //Output values from the ASM model
        if (result.getEsit() == Esit.SAFE) {
            //store the adaptation plan as computed by the ASM runtime model into the knowledge and trigger execution
            Map<String, String> tmp = new HashMap<>();
            //iterating over keys only and selects those location values for the effectors
            for (String key : result.getControlledvalues().keySet()) {
                if (key.startsWith("setNewTime") || key.startsWith("newTime") || key.startsWith("skipNextPill") || key.startsWith("setOriginalTime"))
                    tmp.put(key, result.getControlledvalues().get(key));
            }
            if (!tmp.isEmpty()) { //to add the other monitored locations as required in input by the Pillbox
                //iterating over keys of the input stored into the knowledge and selects those starting with "systemTime" and "openSwitch"
                for (String key : kPB.getInput().keySet()) {
                    if (key.startsWith("openSwitch") || key.startsWith("systemTime"))
                        tmp.put(key, kPB.getInput().get(key));
                }
                //Add locations in the effectors (they are monitored for Pillbox!) only if not changed!
                //Namely: setNewTime, newTime, skipNextPill, setOriginalTime
                //TODO Patrizia: forse non necessario qui, ma nel metodo run della classe PillBoxNotSing.java
            }
            kPB.setEffectors(tmp);
            System.out.println("Enforcer output for effectors:~$ " + tmp.toString());
            execution();
            printOutput();
        } else {
            System.out.println("Error: something got wrong with the output sanitisation made by the ASM runtime model. No enforcement applied.");

        }
    }

    public void printOutput() {
        if (!kPB.getEffectors().isEmpty()) {
            for (String key : myState.keySet()) {
                if (key.startsWith("skipNextPill") && myState.get(key).equalsIgnoreCase("true") && key.length() > 30) {
                    String compartmentName = key.substring(key.length() - 13, key.length() - 1);
                    int index = Integer.parseInt(myState.get("drugIndex(" + compartmentName + ")"));
                    TextView x;
                    if (compartmentName.equalsIgnoreCase("compartment1")) {
                        showSkipPill(R.id.skipRow2);
                    }
                    if (compartmentName.equalsIgnoreCase("compartment2")) {
                        if (index == 1) {
                            showSkipPill(R.id.skipRow3);
                        } else {
                            showSkipPill(R.id.skipRow4);
                        }
                    }
                }
                if (key.startsWith("newTime")) {
                    String newTime = myState.get(key);
                    String compartmentName = key.substring(key.length() - 13, key.length() - 1);
                    int index = Integer.parseInt(myState.get("drugIndex(" + compartmentName + ")"));
                    if (compartmentName.equalsIgnoreCase("compartment1")) {
                        showNewTime(newTime,R.id.timeRow2, R.id.skipRow2, R.id.newTimeRow2);
                    }
                    if (compartmentName.equalsIgnoreCase("compartment2")) {
                        if (index == 0) {
                            showNewTime(newTime,R.id.timeRow3, R.id.skipRow3, R.id.newTimeRow3);
                        } else {
                            showNewTime(newTime,R.id.timeRow4, R.id.skipRow4, R.id.newTimeRow4);
                        }
                    }
                }
            }
        }
    }

    private void showSkipPill(int skipRow2) {
        TextView x = (TextView) v.findViewById(R.id.skipRow2);
        x.setText("YES");
    }

    private void showNewTime(String newTime, int timeRow2, int skipRow2, int newTimeRow2) {
        TextView x;
        TextView originalTime = (v.findViewById(timeRow2));
        if (newTime.equalsIgnoreCase((String) originalTime.getText())){
            x = (TextView) v.findViewById(skipRow2);
            x.setText("YES");
        }
        else{
            x = (TextView) v.findViewById(newTimeRow2);
            x.setText(newTime);
        }
    }

    @Override
    public void execution() {
        //If enforcement is required, force the system as planned by actuating the Pillbox effectors
        if (!kPB.getEffectors().isEmpty()) {
            System.out.println("The Pillbox returns into a safe state with the enforced input:~$ " + kPB.getEffectors().toString());
            myState = effectorPB.run(kPB.getEffectors()); //the managed system runs again to return in a safe region
        }

    }


}

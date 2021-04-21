# MRM
#### Medicine Reminder and Monitoring System - software enforcer for an existing smart pill box.  
This repository contains the application of the proposed safety enforcement mechanism by monitoring and adaptation of the pill box device to address the three enforcement scenarios (MP1,MP2,LP1).

## MRM case study description
The proposed case study is inspired by a real smart drug system consisting ofa daily electronic pill reminder&dispenser (a daily pill box), a remote control app(e.g., a mobile Android app running on a smartphone or tablet) that monitorsand controls the pill box.   

Initially, the caregiver or the patient through the mobile app downloads a drug file record (e.g., a JSON file) containing all information about the medicines prescribed by the doctor: medicine name, number of doses per day, time schedule, minimum separation (in terms of time) from the medicine M to the interferer N and between the same medicine, delta time added to the original time schedule to remember the medicine again if a dose is missed. Then, the user has to manually fill the pill box's compartment with the medicines (one medicine type per each compartment) on a daily basis according to the given prescription for the overall treatment duration.  
Once the medicines are added into the pill box and the pill box actual configuration is checked against the prescription via the remote control app, the patient is notified by the pill box when a medicine has to be taken. A red led, corresponding to the compartment where the medicine to be taken is located, is turned on and the compartment is unlocked. The patient/care giver has to open and then close the compartment to report to the remote control app that the medicine was effectively taken. If after T minutes (e.g., 10 minutes) from the expected time the medicine is not taken, the red led starts flashing for T minutes further, after which the pill is considered missed by the system. 

The implemented enforcement scenarios are:  
MP1 - Pill missed, it is postponed if there isn't overlapping with the next pill  
MP2 - Pill missed, it is skipped because a postponement causes overlapping with the next pill  
LP1 - Pill taken later, skip next pill if the new consumption time causes and overlapping with the next pill 

### Enforcer framewrok
The framework is available for download within the ASMETA GitHub source repository https://github.com/asmeta/asmeta/tree/master/code/experimental/asmeta.enforcer 

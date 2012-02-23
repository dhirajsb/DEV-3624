#!/bin/bash
<<COMMENT
THIS FILE IS FOR TAKING SNAPSHOTS OF PROCESS PERFORMENCE PARAMETERS 
Change The sleep time accordingly to set the interval
and also the number in for loop according ,  how my number of time you want to run the performance test , 4 is default value
COMMENT
echo -ne " Running Performance Test now and storing results in Performance_output.txt \n Have Patience or run me in background \n"
for i in 1 2 3 4 5 6 7 8 9 
do 
date >> Performance_output.txt
echo -ne "\nUSER PID PCPU COMMAND \n" >> Performance_output.txt
ps -eo user,pid,pcpu,command | grep java >> Performance_output.txt
echo -ne "\n\n\n" >> Performance_output.txt
ysar -interval 5 | tail -20 >> Performance_output.txt
echo -ne "\n Number Of threads of broker1 process\n" >> Performance_output.txt
ps uH `ps -eo pid,command | grep activemq-yahoo | cut -f1 -d' '`  | wc -l >> Performance_output.txt
echo -ne "\n Number Of threads of broker2 process\n" >> Performance_output.txt
ps uH `ps -eo pid,command | grep activemq-yahoo1 | cut -f1 -d' '`  | wc -l >>Performance_output.txt
echo -ne "\n Number Of threads of Test program broker1 process\n" >> Performance_output.txt
ps uH `ps -eo pid,command | grep broker1 | cut -f1 -d' '`  | wc -l >> Performance_output.txt
echo -ne "\n Number Of threads of broker1 process\n" >> Performance_output.txt
ps uH `ps -eo pid,command | grep broker2 | cut -f1 -d' '`  | wc -l >> Performance_output.txt
echo -ne "\n" >> Performance_output.txt
sleep 1000
done
echo -ne " Done ... Can check Performance_output.txt \n"

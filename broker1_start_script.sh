#!/bin/bash
FILE=broker1_output.txt;
if [ -f $FILE ]
then
echo -ne " \n OUTPUT OF THE NEXT RUN \n\n\n\n" ;
fi
echo -ne " SCEANRIO 1 STARRTING   \n ";
date >> broker1_output.txt ;
mvn -Pamq5.5 -Pbroker1_sunnyday >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 1 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 2 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_backlog >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 2 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 3 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainage >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 3 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
<<COMM
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";

date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
COMM
<<COMMENT
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_output.txt;
mvn -Pamq5.5 -Pbroker1_drainageonly >> broker1_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
COMMENT


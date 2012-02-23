#!/bin/bash
echo -ne " SCEANRIO 1 STARRTING   \n ";
date >> broker2_output.txt ;
mvn -Pamq5.6 -Pbroker2_sunnyday >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 1 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 2 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_backlog >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 2 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 3 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainage >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 3 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainageonly >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
<<COMM
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainageonly >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainageonly >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainageonly >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker2_output.txt;
mvn -Pamq5.6 -Pbroker2_drainageonly >> broker2_output.txt ;
sleep 10 ;
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
COMM
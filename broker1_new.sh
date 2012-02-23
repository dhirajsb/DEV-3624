#!/bin/bash
FILE=broker1_99percentissue.txt;
if [ -f $FILE ]
then
echo -ne " \n OUTPUT OF THE NEXT RUN \n\n\n\n" ;
fi
echo -ne "  \n  SCEANRIO 2 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_backlog >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 2 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";
echo -ne "  \n  SCEANRIO 4 STARTING \n ";
date >> broker1_99percentissue.txt;
mvn -Pamq5.6 -Pbroker1_drainageonly >> broker1_99percentissue.txt ;
sleep 10 ;
du -h /home/harishs/apache-activemq-5.6-SNAPSHOT/data/
ysar -interval 1 | tail -30
echo -ne "  \n \n \n SCEANRIO 4 COMPLETE  \n ";

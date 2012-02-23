#!/bin/bash
APPEND="tee -a apollo_output.txt "

# if [ ! -f $FILE ] ; then
# 	echo "=======================================================================" | $APPEND
# 	echo "OUTPUT OF THE NEXT RUN" | $APPEND
#   echo "=======================================================================" | $APPEND
# fi

# echo "=======================================================================" | $APPEND
# echo "SCENARIO Sunny STARTING: " $(date) | $APPEND
# echo "=======================================================================" | $APPEND
# mvn -Pamq5.5 -Papollo_sunnyday | $APPEND

sleep 10 
echo "=======================================================================" | $APPEND
echo "SCENARIO Backlog STARTING: " $(date) | $APPEND
echo "=======================================================================" | $APPEND
mvn -Pamq5.5 -Papollo_backlog | $APPEND

# sleep 10 ;
# echo "=======================================================================" | $APPEND
# echo "SCENARIO Drainage STARTING: " $(date) | $APPEND
# echo "=======================================================================" | $APPEND
# mvn -Pamq5.5 -Papollo_drainage | $APPEND

# for (( i=0; i<4; i++ )); do
#   sleep 10 
#   echo "=======================================================================" | $APPEND
#   echo "SCENARIO Drainage Only STARTING: " $(date) | $APPEND
#   echo "=======================================================================" | $APPEND
#   mvn -Pamq5.5 -Papollo_drainageonly | $APPEND
# done

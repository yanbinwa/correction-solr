#!/usr/bin/expect 

set timeout 120

set HOST_USERNAME "deployer"
set PASSWD "Emotibot1"
set RUNSCRIPT "run.sh"
set ENVSCRIPT "idc.env"
set SOURCE_PATH "docker"
set TARGET_PATH "/tmp"
set CONTAINER [lindex $argv 0]
set TAG [lindex $argv 1]
set HOST_IP [lindex $argv 2]

spawn scp -r $SOURCE_PATH/$RUNSCRIPT $SOURCE_PATH/$ENVSCRIPT $HOST_USERNAME@$HOST_IP:$TARGET_PATH
expect {
    "(yes/no)?" {  
        send "yes\r"  
        expect "password:"  
        send "$PASSWD\r"  
    }  
    "password:" {  
        send "$PASSWD\r" 
    }
}
expect eof

spawn ssh -l $HOST_USERNAME $HOST_IP
expect {
    "(yes/no)?" {  
        send "yes\r"  
        expect "password:"  
        send "$PASSWD\r"  
    }  
    "password:" {  
        send "$PASSWD\r" 
    }
}
expect "]\\$"
send "docker stop $CONTAINER\r"
expect "]\\$"
send "docker rm $CONTAINER\r"
expect "]\\$"
send "$TARGET_PATH/$RUNSCRIPT $TARGET_PATH/$ENVSCRIPT $TAG\r"
expect "]\\$"

exit

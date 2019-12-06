#! /bin/sh

gcc -o src/main/C/Server src/main/C/Server.c src/main/C/buddy.c -lpthread -lm
gnome-terminal --command='./src/main/C/Server'
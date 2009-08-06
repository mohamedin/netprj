+++++++++++++++++++++++++++++++++++++++++++++++
				R E A D   M E
+++++++++++++++++++++++++++++++++++++++++++++++

SWITCH WORKSPACE
================
	From eclipse "File" menu choose "Switch Workspace" then "Others"
	Type the path of your Home directory at cygwin for example
		C:\cygwin\home\Admin
	Check-out the last version of the project
__________________________________________________________________________________________________________________________________________________

RUNNING NS2
===========
	1) Open cygwin window
	2) Type startx
	3) go to folder NS2 using command
		cd NS2
	4) run the script using the command and arguments below
	5) check the logs at folder /NS2/logs/
__________________________________________________________________________________________________________________________________________________

SCRIPTS
=======
	* Wireless Topology Grid
	* Wired Topology Grid
	* Wireless Topology Infrastructure
	* Wired Topology Star
	* Wireless Topology Line
	* Wireless Topology Grid Heterogeneous [COMMING SOON]
	* Wireless Topology Grid Mobile	[COMMING SOON]
__________________________________________________________________________________________________________________________________________________

Wireless Topology Grid
-----------------------
wireless/grid.tcl <grid dimension> <seeder index> <upload data rate> <distance between nodes> <inter-arrival range>

ex: ns wireless/grid.tcl 3 4 100 50 100

here the grid will be as following
   0     1     2
   3     4     5
   6     7     8
argument-1) the grid dimension is 3*3
argument-2) seeder index is 4 so the seeder in the middle
argument-3) data rate is 4 * 100 = 400 Kbps as I assume given parameter is upload rate so download rate is 4 * 100
argument-4) distance between each node is 40 meter [OPTIONAL default 40]
argument-5) maximum inter-arrival time between node [OPTIONAL default 10]

__________________________________________________________________________________________________________________________________________________

Wired Topology Grid
--------------------
wired/grid.tcl <grid dimension> <seeder index> <upload data rate>

ex: ns wired/grid.tcl 3 4 100

here the grid will be as following
   0     1     2
   3     4     5
   6     7     8
argument-1) the grid dimension is 3*3
argument-2) seeder index is 4 so the seeder in the middle
argument-3) upload rate = 100 Kbps , download rate = 400 Kbps as I assume given parameter is upload rate so download rate is 4 * 100

__________________________________________________________________________________________________________________________________________________

Wireless Topology Infrastructure
--------------------------------
wireless/ap.tcl <grid dimension> <base station index> <upload data rate> <distance between nodes> <inter-arrival range>

ex: ns wireless/ap.tcl 3 4 100 50 20

here the grid will be as following
   0     1     2
   3     *     5
   6     7     8
argument-1) the grid dimension is 3*3
argument-2) base station index is 4 so the seeder in the middle
argument-3) data rate is 4 * 100 = 400 Kbps as I assume given parameter is upload rate so download rate is 4 * 100
argument-4) distance between each node is 40 meter [OPTIONAL default 40]
argument-5) maximum inter-arrival time between node [OPTIONAL default 10]

Note: I assume that seeder always at index 0. So the possible values for Base Station index can not be 0.
__________________________________________________________________________________________________________________________________________________

Wired Topology Star
--------------------
wired/star.tcl <number of peers> <upload data rate>

ex: ns wired/star.tcl 5 100

argument-1) the number of peers including the seeder
argument-2) upload rate = 100 Kbps , download rate = 400 Kbps as I assume given parameter is upload rate so download rate is 4 * 100

__________________________________________________________________________________________________________________________________________________

Wireless Topology Line
-----------------------
wireless/line.tcl <number of peers> <seeder index> <upload data rate> <distance between nodes> <inter-arrival range>

ex: ns wireless/line.tcl 10 4 100 50 100

here the grid will be as following
   0	1	2	3	4	5	6	7	8	9

argument-1) the number of nodes/peers
argument-2) seeder index is 4 so the seeder in the middle
argument-3) data rate is 4 * 100 = 400 Kbps as I assume given parameter is upload rate so download rate is 4 * 100
argument-4) distance between each node is 40 meter [OPTIONAL default 40]
argument-5) maximum inter-arrival time between node [OPTIONAL default 10]


__________________________________________________________________________________________________________________________________________________

Wireless Topology Grid Mobile
-----------------------------

From NS2 directory type the following command
 ./../ns-allinone-2.29/ns-2.29/indep-utils/cmu-scen-gen/setdest/setdest.exe  -n 12 -p 2.0 -s 1 -M 10 -t 200 -x 500 -y 500 > wireless/mobile/scen.tcl 
 
This command generate a new movement pattern file name "scen" with the following options
	12 movable nodes indicated by  	-n 12
 	10 meter maximum speed  		-M 10
 	200 seconds of movement then they stop	-t 200
 	
So all you have to bother your self with is Number of nodes, and movement time set it to suitable time for your experiment.
Note: u can backup this file as next simulation will override it

No run the experiment using the following command

wireless/mobile.tcl <grid dimension> <seeder index> <upload data rate> <distance between nodes> <inter-arrival range>

The parameters are the same as Wireless Grid but here nodes are mobile according to "scen" file, you provided
#Reading args
	global argv

	if { $argc > 0 } {
		set i 1
		foreach arg $argv {
		
			if {$i==1} {
				set no_of_peers $arg
			}
			if {$i==2} {
				set s $arg
			}
			if {$i==3} {
				set C_up [expr $arg * 1000]
			}			
			if {$i==4} {
				set grid_side $arg
			}			
			if {$i==5} {
				set interarrival $arg
			}			
		
			incr i
		}
	}

	if {$argc < 3} {
		puts "Error: wrong parameters ->  peers  run upload_cap\[kBits/s\]"
		exit 0
	}
	if {$argc < 4} {
		set grid_side 40
	}
	if {$argc < 5} {
		set interarrival 10
	}


	
#Define a 'finish' procedure
	proc done {} {
		global FinishedPeers PeersCount
		
		puts "Peer complete"
			
		incr FinishedPeers
		if {$FinishedPeers == $PeersCount } {
			puts "Ending Simulation..."
				
			global ns fh app
			global p2pNAMTrace nf
	
			for {set i 0} {$i < $PeersCount } {incr i} {
				$app($i) stop
			}
			
			close $fh
			
			$ns flush-trace
			#Close the trace file
			close $nf
			#Execute nam on the trace file
			#exec nam $p2pNAMTrace &
			exit 0
		}
	}

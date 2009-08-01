
# BitTorrent P2P Simulation
# Flashcrowd
# PARAMETERS: N_P SEEDER_INDEX C_up

global argv

# topology: GRID

if { $argc > 0 } {
	set i 1
        foreach arg $argv {
			if {$i==1} {
				set grid_length $arg
			}
			if {$i==2} {
				set s $arg
			}
			if {$i==3} {
				set C_up [expr $arg * 1000]
			}			
            incr i
        }
}

if {$argc != 3} {
	puts "Error: wrong parameters ->  peers  run upload_cap\[kBits/s\]"
	exit 0
}

#Create a simulator object
	set ns [new Simulator]
	
	remove-all-packet-headers
	add-packet-header IP TCP Flags
	
	$ns use-scheduler Heap

#set the routing protocol
	$ns rtproto DV


# Simulation Parameters:
	set interarrival 10
	source bittorrent_default.tcl
	
	# number of peers
	set N_P [expr $grid_length * $grid_length ]
	
	# seeder index
	set N_S $s
# End of SimulationParameters

	set FinishedPeers 0

# NAME OF TRACE FILE
	set traceDir	logs/grid
	append traceDir _Seeder=
	append traceDir $s
	
	source traces.tcl

# set MSS for all FullTCP connections
	Agent/TCP/FullTcp set segsize_ 1460
	Queue set limit_ $Q_access

# Create Connections
	proc fully_meshed2 {from_peer to_peer} {
		global ns peer C_up C_down_fac DelayMin DelayMax
		
		set e2eDelayRng [new RNG]
		set e2eDelay [expr round([$e2eDelayRng uniform $DelayMin $DelayMax])]
		
		# upstream
		$ns simplex-link $peer($to_peer) $peer($from_peer) $C_up [expr $e2eDelay]ms DropTail
		# downstream
		$ns simplex-link $peer($from_peer) $peer($to_peer) [expr $C_down_fac * $C_up] [expr $e2eDelay]ms DropTail
		
		return 0
	}

	proc done {} {
		global app FinishedPeers N_P fh ns
		
		incr FinishedPeers
			
		puts "Peer complete"
		
		if {$FinishedPeers == $N_P} {
			for {set i 0} {$i < $N_P} {incr i} {
				$app($i) stop
			}
		
			close $fh
			puts [$ns now]
			
			global ns nf tracefd
			global p2pNAMTrace
			
			$ns flush-trace
			#Close the trace file
			close $nf
			close $tracefd
			#Run NAM to see the simulation
			exec nam $p2pNAMTrace &
			
			exit 0
		}
	}

# create tracker
# Parameters: File Size [B], Chunk Size [B]
	set go [new BitTorrentTracker $S_F $S_C]
	$go tracefile $p2ptrace	
	
# Create nodes
	for {set i 0} {$i < $N_P} {incr i} {
		set peer($i) [$ns node]
	}

# Create the topology
	for {set i 0} {$i < $grid_length} {incr i} {
		for {set j 0} {$j < [expr $grid_length - 1]} {incr j} {
			fully_meshed2 [expr $i + $grid_length * $j] [expr $i + $grid_length * ($j+1)]
		}	
	}	
	for {set i 0} {$i < $grid_length} {incr i} {
		for {set j 0} {$j < [expr $grid_length - 1]} {incr j} {
			fully_meshed2 [expr $j + $grid_length * $i] [expr $j + 1 + $grid_length * $i]
		}	
	}	
	
# Create Applications
	for {set i 0} {$i < $N_P} {incr i} {	
		if {$i == $N_S} {
			set app($i) [new BitTorrentApp 1 $C_up $go $peer($i)]
			
			$app($i) set super_seeding 1
			$app($i) tracefile $p2ptrace
			
			# start apps
			$ns at 0.0 "$app($i) start"
	
			incr FinishedPeers		
		} else {
			set app($i) [new BitTorrentApp 0 $C_up $go $peer($i)]
			
			$app($i) tracefile $p2ptrace
		
			# start apps
			set interArr [$t_offset value]
			puts "Peer $i starts at $interArr"
			$ns at $interArr "$app($i) start"
		}	
	}

# Run the simulation
	$ns run
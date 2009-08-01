
# BitTorrent P2P Simulation
# Flashcrowd
# PARAMETERS: N_P RNG_SEED C_up

global argv

# topology: STAR
	
	if { $argc > 0 } {
		set i 1
	        foreach arg $argv {
				if {$i==1} {
					set no_of_peers $arg
				}
				if {$i==2} {
					set C_up [expr $arg * 1000]
				}			
	            incr i
	        }
	}
	
	if {$argc != 2} {
		puts "Error: wrong parameters ->  peers  run upload_cap\[kBits/s\]"
		exit 0
	}
	
#Create a simulator object
	set ns [new Simulator]
	
	remove-all-packet-headers
	add-packet-header IP TCP Flags
	
	$ns use-scheduler Heap
	
#set the routing protocol
	$ns rtproto Manual

# Simulation Parameters:
	set interarrival 10
	source bittorrent_default.tcl

	# number of peers
	set N_P $no_of_peers
	
	# number of seeds
	set N_S 1
# End of SimulationParameters

	set peerCount 0
	set FinishedPeers 0

# NAME OF TRACE FILE
	set traceDir	logs/star
	
	source traces.tcl

# set MSS for all FullTCP connections
	Agent/TCP/FullTcp set segsize_ 1460
	Queue set limit_ $Q_access

# Create Connections
	proc fully_meshed2 {no_of_peers} {
		global ns peer router C_up C_down_fac DelayMin DelayMax
		
		
		set e2eDelayRng [new RNG]
		set e2eDelay [expr round([$e2eDelayRng uniform $DelayMin $DelayMax])]
		
		# upstream
		$ns simplex-link $peer($no_of_peers) $router $C_up [expr $e2eDelay]ms DropTail
		# downstream
		$ns simplex-link $router $peer($no_of_peers) [expr $C_down_fac * $C_up] [expr $e2eDelay]ms DropTail
		
		# do the routing manually between peer and router
		[$peer($no_of_peers) get-module "Manual"] add-route-to-adj-node -default [$router id]
		[$router get-module "Manual"] add-route-to-adj-node -default [$peer($no_of_peers) id]
		
		[$router get-module "Manual"] add-route [$peer($no_of_peers) id] [[$ns link $router $peer($no_of_peers)] head]
		
		[$peer($no_of_peers) get-module "Manual"] add-route [$router id] [[$ns link  $peer($no_of_peers) $router] head]
		
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
			exit 0
		}
	}

# create tracker
# Parameters: File Size [B], Chunk Size [B]
	set go [new BitTorrentTracker $S_F $S_C]
	$go tracefile $p2ptrace	
	
# Create router node
	set router [$ns node]

# Create Seeds
	for {set i 0} {$i < $N_P} {incr i} {
		
		# make nodes
		set peer($i) [$ns node]
		
		# make links
		fully_meshed2 $i
		
		if {$i < $N_S} {
			set app($peerCount) [new BitTorrentApp 1 $C_up $go $peer($i)]
			
			$app($peerCount) set super_seeding 1
			$app($peerCount) tracefile $p2ptrace
			
			# start apps
			$ns at 0.0 "$app($peerCount) start"
	
			incr FinishedPeers		
		} else {
			set app($peerCount) [new BitTorrentApp 0 $C_up $go $peer($i)]
			
			$app($peerCount) tracefile $p2ptrace
		
			# start apps
			set interArr [$t_offset value]
			puts "Peer $i starts at $interArr"
			$ns at $interArr "$app($peerCount) start"
		}
		
		incr peerCount
	}

# Run the simulation
	$ns run
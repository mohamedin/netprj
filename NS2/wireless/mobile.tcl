# ======================================================================
# Read arguments
# ======================================================================

# topology: WIRELESS-FIXED GRID
	source wireless/utils/args.tcl
	
# ======================================================================
# Define options
# ======================================================================

# P2P Bittorrent Options
#-------------------
	source bittorrent_default.tcl
	
	# number of peers
	set N_P [expr $no_of_peers * $no_of_peers]
	set PeersCount $N_P
	# seeder index
	set N_S $s

# Wireless Options
#-------------------
	source wireless/utils/options.tcl
		
# =====================================================================
# Main Program
# ======================================================================

# Initialize Global Variables
	set FinishedPeers 0

# create simulator instance
	set ns		[new Simulator]

# setup topography object
	set topo	[new Topography]

# define topology
	$topo load_flatgrid $x $y

# Create God Object
	set god_ [create-god $N_P]

# create trace object for ns, p2p and nam 
	set traceDir	logs/wirelessGrid
	append traceDir _Distance=
	append traceDir $grid_side
	append traceDir _Seeder=
	append traceDir $s
	
	source traces.tcl

#global node setting
	source wireless/utils/defaultNode.tcl
	
# Create tracker
	set go [new BitTorrentTracker $S_F $S_C]
	$go tracefile $p2ptrace	

#  Create the specified number of nodes [$N_P] and "attach" them to the channel. 
	for {set i 0} {$i < $N_P } {incr i} {
		set node_($i) [$ns node]	
	
		$ns initial_node_pos $node_($i) 20
		
		set X_loc			[expr ($i % $no_of_peers) * $grid_side + 30]
		set Y_loc			[expr ($i / $no_of_peers) * $grid_side + 30]
	
		$node_($i)	random-motion	0		;# disable random motion
		$node_($i)	set	X_	$X_loc
		$node_($i)	set	Y_	$Y_loc
		$node_($i)	set Z_	0.0
	}
	
# Create Seeder
	set app($N_S) [new BitTorrentApp 1 $C_up $go $node_($N_S)]
	
	$app($N_S) set super_seeding 1
	$app($N_S) tracefile $p2ptrace
	
# start apps
	$ns at 0.0 "$app($N_S) start"
	
	incr FinishedPeers
	
	# Create Leechers
	for {set i 0} {$i < $N_P} {incr i} {
		if { $i != $N_S } {
			set app($i) [new BitTorrentApp 0 $C_up $go $node_($i)]
			
			$app($i) tracefile $p2ptrace
	
			# start apps
			set interArr [$t_offset value]
			puts "Peer $i starts at $interArr"
			$ns at $interArr "$app($i) start"
		}	
	}

# Movement file
set ns_ ns
source wireless/mobile/scen.tcl

# Starting simulation
	puts "Starting Simulation..."
	$ns run
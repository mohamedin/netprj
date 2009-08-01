# ======================================================================
# Read arguments
# ======================================================================

# topology: WIRELESS-FIXED CENTRAL ACCESS POINT
	source wireless/utils/args.tcl

# ======================================================================
# Define options
# ======================================================================

# P2P Bittorrent Options
#-------------------
	source bittorrent_default.tcl
	
	# number of peers
	set N_P [expr $no_of_peers * $no_of_peers]
	# base station index
	set N_S $s

# Wireless Options
#-------------------
	source wireless/utils/options.tcl
	
# =====================================================================
# Main Program
# ======================================================================

# Initialize Global Variables
	set FinishedPeers 0
	set PeersCount 0

# create simulator instance
	set ns		[new Simulator]

# setup topography object
	set topo	[new Topography]

# define topology
	$topo load_flatgrid $x $y

# Create God Object
	set god_ [create-god $N_P]

# create trace object for ns, p2p and nam 
	set traceDir	logs/ap
	append traceDir _Distance=
	append traceDir $grid_side
	append traceDir _AccessPoint=
	append traceDir $s
	
	source traces.tcl

#global node setting
	source wireless/utils/defaultNode.tcl
	$ns node-config	-addressType hierarchical
	
# Create tracker
	set go [new BitTorrentTracker $S_F $S_C]
	$go tracefile $p2ptrace	

# Domain
	AddrParams set domain_num_ 1           ;# number of domains
	lappend cluster_num 1                ;# number of clusters in each domain
	
	AddrParams set cluster_num_ $cluster_num
	lappend eilastlevel 9              ;# number of nodes in each cluster
	AddrParams set nodes_num_ $eilastlevel ;# for each domain
	
	# Create the specified number of nodes [$N_P] and "attach" them to the channel. 
	set temp {0.0.0 0.0.1 0.0.2 0.0.3 0.0.4 0.0.5 0.0.6 0.0.7 0.0.8}   ;# hier address to be used for wireless domain

# Create base station
	set node_($N_S) [ $ns node [lindex $temp $N_S]] ;# provide each mobilenode with hier address of its base-station
	
	$ns initial_node_pos $node_($N_S) 20
		
	set X_loc			[expr ($N_S % $no_of_peers) * $grid_side + 30]
	set Y_loc			[expr ($N_S / $no_of_peers) * $grid_side + 30]
	
	$node_($N_S)	random-motion	0		;# disable random motion
	$node_($N_S)	set	X_	$X_loc
	$node_($N_S)	set	Y_	$Y_loc
	$node_($N_S)	set Z_	0.0
	
	for {set i 0} {$i < $N_P } {incr i} {
		if { $i != $N_S } { 	
			set node_($i) [ $ns node [lindex $temp $i]] ;# provide each mobilenode with hier address of its base-station
			$node_($i) base-station [AddrParams addr2id [$node_($N_S) node-addr]]   
			
			$ns initial_node_pos $node_($i) 20
			
			set X_loc			[expr ($i % $no_of_peers) * $grid_side + 30]
			set Y_loc			[expr ($i / $no_of_peers) * $grid_side + 30]
	
			$node_($i)	random-motion	0		;# disable random motion
			$node_($i)	set	X_	$X_loc
			$node_($i)	set	Y_	$Y_loc
			$node_($i)	set Z_	0.0
		}
	}

# Create Seeder
	set app($PeersCount) [new BitTorrentApp 1 $C_up $go $node_($PeersCount)]
	
	$app($PeersCount) set super_seeding 1
	$app($PeersCount) tracefile $p2ptrace

# start apps
	$ns at 0.0 "$app($PeersCount) start"
	
	incr FinishedPeers
	incr PeersCount

# Create Leechers
	for {set i 1} {$i < $N_P} {incr i} {
		if { $i != $N_S } {
			set app($PeersCount) [new BitTorrentApp 0 $C_up $go $node_($i)]
			
			$app($PeersCount) tracefile $p2ptrace
	
			# start apps
			set interArr [$t_offset value]
			puts "Peer $i starts at $interArr"
			$ns at $interArr "$app($PeersCount) start"
			
			incr PeersCount
		}	
	}

# Starting simulation
	puts "Starting Simulation..."
	$ns run
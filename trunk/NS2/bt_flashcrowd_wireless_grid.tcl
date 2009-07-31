# ======================================================================
# Read arguments
# ======================================================================

global argv

# topology: WIRELESS-FIXED GRID

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
    
        incr i
    }
}

if {$argc < 3} {
	puts "Error: wrong parameters ->  peers  run upload_cap\[kBits/s\]"
	exit 0
}
if {$argc == 3} {
	set grid_side 40
}

# ======================================================================
# Define options
# ======================================================================

# P2P Bittorrent Options
#-------------------
source bittorrent/bittorrent_default.tcl
BitTorrentApp set leave_option -1
# number of peers
set N_P [expr $no_of_peers * $no_of_peers]
# seeder index
set N_S $s
# upload capacity in bytes
set C_up_bytes [expr $C_up / 8.0 ]
# factor that download capacity is higher than upload capacity
set C_down_fac 4
# queue size at access links (default 50)
set Q_access 25
# delay
set DelayMin 1
set DelayMax 50
# file size
set S_F_MB 1
set S_F [expr $S_F_MB * 1024.0 *1024]
set S_C [expr 256.0 *1024]
set N_C [format %.0f [expr ceil($S_F / $S_C)]]
# set the seed for the RNG (0: non-deterministic, 1 - MAXINT (2147483647))
set rng_seed 0


# Wireless Options
#-------------------

# Antenna specs
set chan       Channel/WirelessChannel
set prop       Propagation/TwoRayGround
set netif      Phy/WirelessPhy
set mac        Mac/802_11
set ifq        CMUPriQueue
set ll         LL
set ant        Antenna/OmniAntenna
# X dimension of the topography
set x              670
#Y dimension of the topography
set y              670
# max packet in ifq
set ifqlen         50
# routing protocol
set adhocRouting DSR

Mac/802_11 set dataRate_   [expr $C_down_fac * $C_up]            ;# bps  

# Seed the default RNG 
global defaultRNG
$defaultRNG seed $rng_seed
puts "Random Number Generator Seed: [$defaultRNG seed]"

# =====================================================================
# Main Program
# ======================================================================

# Initialize Global Variables
set FinishedPeers 0

# create simulator instance
set ns_		[new Simulator]

# setup topography object
set topo	[new Topography]

# define topology
$topo load_flatgrid $x $y

# Create God Object
set god_ [create-god $N_P]

# create trace object for ns, p2p and nam 
set traceDir	bittorrent/results_flash_packet_wireless_
append traceDir $S_F_MB
append traceDir MB_N_P_
append traceDir $N_P
append traceDir _C_
append traceDir $C_up_bytes
append traceDir Bps
append traceDir _seed_
append traceDir [$defaultRNG seed]
append traceDir _dist_
append traceDir $grid_side

exec mkdir $traceDir
puts $traceDir

exec cp bittorrent/bittorrent_default.tcl $traceDir

set p2pTrace $traceDir
append p2pTrace /log
set fh [open $p2pTrace w]

set p2pNAMTrace $traceDir
append p2pNAMTrace /out.nam
set nf [open $p2pNAMTrace w]
$ns_ namtrace-all-wireless $nf $x $y	 

set nsTrace $traceDir
append nsTrace /wireless-out.tr
set tracefd	[open $nsTrace w]
$ns_ trace-all $tracefd

#Define a 'finish' procedure
proc done {} {
	global FinishedPeers N_P
	
	puts "Peer completes transfer..."	
	incr FinishedPeers
	if {$FinishedPeers == $N_P} {
		puts "Ending Simulation..."
			
		global ns_  fh app
		#global p2pNAMTrace nf

		for {set i 0} {$i < $N_P} {incr i} {
			$app($i) stop
		}
		
		close $fh
		
		$ns_ flush-trace
		#Close the trace file
		#close $nf
		#Execute nam on the trace file
		#exec nam $p2pNAMTrace &
		exit 0
	}
}

#global node setting
$ns_ node-config -adhocRouting $adhocRouting \
                 -llType $ll \
                 -macType $mac \
                 -ifqType $ifq \
                 -ifqLen $ifqlen \
                 -antType $ant \
                 -propType $prop \
                 -phyType $netif \
                 -channelType $chan \
				 -topoInstance $topo \
				 -agentTrace ON \
                 -routerTrace OFF \
                 -macTrace OFF 

# Create tracker
set go [new BitTorrentTracker $S_F $S_C]
$go tracefile $p2pTrace	

# uniform start offset for peers
set t_offset_rng [new RNG]
set t_offset [new RandomVariable/Uniform]
$t_offset set min_ 0
$t_offset set max_ [BitTorrentApp set choking_interval]
$t_offset use-rng $t_offset_rng	

#  Create the specified number of nodes [$N_P] and "attach" them to the channel. 
for {set i 0} {$i < $N_P } {incr i} {
	set node_($i) [$ns_ node]	

	$ns_ initial_node_pos $node_($i) 20
	
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
$app($N_S) tracefile $p2pTrace

# start apps
$ns_ at 0.0 "$app($N_S) start"

incr FinishedPeers

# Create Leechers
for {set i 0} {$i < $N_P} {incr i} {
	if { $i != $N_S } {
		set app($i) [new BitTorrentApp 0 $C_up $go $node_($i)]
		
		$app($i) tracefile $p2pTrace

		# start apps
		$ns_ at [$t_offset value] "$app($i) start"
	}	
}

# Starting simulation
puts "Starting Simulation..."
$ns_ run
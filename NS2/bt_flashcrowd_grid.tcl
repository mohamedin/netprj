
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
source bittorrent/bittorrent_default.tcl

BitTorrentApp set leave_option -1

# number of peers
set N_P [expr $grid_length * $grid_length ]

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

# End of SimulationParameters

set FinishedPeers 0


# NAME OF TRACE FILE
set p2ptrace	bittorrent/results_flash_packet_grid_
append p2ptrace $S_F_MB
append p2ptrace MB_N_P_
append p2ptrace $N_P
append p2ptrace _C_
append p2ptrace $C_up_bytes
append p2ptrace Bps
append p2ptrace _seed_
append p2ptrace $s
append p2ptrace _
append p2ptrace [clock seconds]

exec mkdir $p2ptrace
puts $p2ptrace

set p2ptrace2 $p2ptrace
append p2ptrace /log

#exec cp bittorrent/scripts/bt_flashcrowd_star.tcl $p2ptrace2
exec cp bittorrent/bittorrent_default.tcl $p2ptrace2

set fh [open $p2ptrace w]

#Open the nam trace file
set p2pNAMTrace $p2ptrace2
append p2pNAMTrace /out.nam

set nf [open $p2pNAMTrace w]
$ns namtrace-all $nf

set nsTrace $p2ptrace2
append nsTrace /out.tr
set tracefd	[open $nsTrace w]
$ns trace-all $tracefd


# set MSS for all FullTCP connections
Agent/TCP/FullTcp set segsize_ 1460
Queue set limit_ $Q_access

# Seed the default RNG 
global defaultRNG
$defaultRNG seed $rng_seed
puts "Random Number Generator Seed: [$defaultRNG seed]"


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
	
# uniform start offset for peers
set t_offset_rng [new RNG]
set t_offset [new RandomVariable/Uniform]
$t_offset set min_ 0
$t_offset set max_ [BitTorrentApp set choking_interval]
$t_offset use-rng $t_offset_rng	


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
		$ns at [$t_offset value] "$app($i) start"
	}	
}

# Run the simulation
$ns run
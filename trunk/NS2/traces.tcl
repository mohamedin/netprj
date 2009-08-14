append traceDir _File=
append traceDir $S_F_MB
append traceDir MB_Peers=
append traceDir $N_P
append traceDir _C=
append traceDir $C_up_bytes
append traceDir Bps
append traceDir _RandomSeed=
append traceDir [$defaultRNG seed]

exec mkdir $traceDir
puts $traceDir

exec cp bittorrent_default.tcl $traceDir

set p2ptrace $traceDir
append p2ptrace /log
set fh [open $p2ptrace w]

set timeTrace $traceDir
append timeTrace /time.txt
set ft [open $timeTrace w]

#Open the nam trace file
set p2pNAMTrace $traceDir
append p2pNAMTrace /out.nam
set nf [open $p2pNAMTrace w]
$ns namtrace-all $nf

set nsTrace $traceDir
append nsTrace /out.tr
set tracefd	[open $nsTrace w]
$ns trace-all $tracefd

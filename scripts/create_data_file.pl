#!/usr/bin/perl

#$v = "2D77E8";
#$value = hex($v);
#print $value;


$cnt = 0;
$file = "data.txt";

open (my $fh, "<", $file) || die "Cannot open $file";

while ($line = <$fh>)
{
 if ($line =~ /^RS: (.*)/) {
   $measurement = $1;
   $measurement =~ /(.*), (.*), (.*)/;
   $vx = $1;
   $vy = $2;
   $vz = $3;
#   print $vx . "  " . $vy . "  " . $vz . "\r\n";
 } else {
   $time = $line;
   $time =~ /(.*) (.*) (.*) (.*)/;
   $d1 = $4;
   $d2 = $3;
   $d3 = $2;
   $d4 = $1;
   $tt = $d1 + (256 * $d2) + (256 * 256 * $d3) + (256 * 256 * 256 * $d4);
#   print "Time: " . $1 . " " . $2 . " " . $3 . " " . $4 . "\r\n";
#   print "Time: " . $tt . "\r\n";

   # We ignore first record...we want second record to be 'start' of timings.
   if ($cnt < 2) {
      $start_time = $tt;
      $time_ms = 0;
      # print "Start time: $start_time\n";
      $cnt++;
   } else {
      $time_ms = $tt - $start_time;
      print $time_ms . "," . $vx . "," . $vy . "," . $vz . "\r\n";
   }
 }
 

}


close($fh);

<?php

$sday = $_GET['sday'];
$eday = $_GET['eday'];


list($syear, $smonth,$days)=explode("-", $sday);
list($eyear, $emonth,$daye)=explode("-", $eday);


$valid = checkdate($smonth, $days, $syear);
$valid &= checkdate($emonth, $daye, $eyear);


if(!$valid){
  echo("
<a href=\"javascript:void(0);\" onclick=\"showTop5('date'); return false;\">Top 5 flights</a><br/>
<font color=\"red\">date format error</font>
");
  return -1;
}
/*$tsday = mktime(0,0,0,$smonth,$days,$syear);
  $teday = mktime(0,0,0,$emonth,$daye,$eyear);*/
$tsday = $syear.'-'.$smonth.'-'.$days;
$teday = $eyear.'-'.$emonth.'-'.$daye;
/*if($tsday > $teday)){
  echo("
  <a href=\"javascript:void(0);\" onclick=\"showTop5('date'); return false;\">Top 5 flights</a><br/>
<font color=\"red\">time format error</font>
");
  return -1;
  }*/
//echo $tsday.", ".$teday.'<br/>';

include 'sunapeedb.php';
$db = new SunapeeDB();
$db->connect();

print '<a href="javascript:void(0);" onclick="showTop5(\'date\'); return false;">Top 5 flights</a>';

$db->get_table_("flight", TRUE, 'SELECT * FROM flight WHERE TIMESTAMPDIFF(DAY,\''.$tsday.'\' , flight.d_time)>=0 AND TIMESTAMPDIFF(DAY,\''.$teday.'\' , flight.d_time)<=0 ORDER BY occupied DESC LIMIT 5;');
 

$db->disconnect();
?>
<?php

include 'sunapeedb.php';
$db = new SunapeeDB();
$db->connect();

session_start();
$id=$_SESSION['uid'];


$call_type=$_GET["ct"];

switch($call_type){
    case "flight":
    	print 'Flights schedules&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse(\'flight\'); return false;">Collapse results</a>';
	$db->get_table_("flight", TRUE, 'SELECT flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city FROM `flight` WHERE TIMESTAMPDIFF(SECOND, NOW(), `d_time`)>0');
    	break;
    case "reserve":
    	print 'Reservations&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_res(\'reserve\'); return false;">Collapse results</a>';
	$db->get_table_("flight", TRUE, 'SELECT flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, reservation.seat FROM `flight`,reservation WHERE reservation.customer_c_id='.$id.' and flight.f_id=reservation.flight_f_id');
    	break;
    case "available":
    	print 'Available flights&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_ava(\'available\'); return false;">Collapse results</a><br/>Click on a flight to book';
	$db->get_table_("flight", TRUE, 'SELECT flight.f_id, flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, flight.capacity, flight.occupied FROM `flight` WHERE TIMESTAMPDIFF(SECOND, NOW(), `d_time`)>0 and capacity > occupied', 2);
    	break;
    case "future":
    	print 'My future reservations &nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_fut(\'future\'); return false;">Collapse results</a><br/>Click on a flight to cancel';
	$db->get_table_("flight", TRUE, 'SELECT flight.f_id, flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, reservation.seat FROM `flight`,reservation WHERE reservation.customer_c_id='.$id.' and flight.f_id=reservation.flight_f_id and TIMESTAMPDIFF(SECOND, NOW(), flight.d_time)>0', 3);
    	break;
    case "past":
    	print 'My past flights&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_pas(\'past\'); return false;">Collapse results</a>';
	$db->get_table_("flight", TRUE, 'SELECT flight.f_id, flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, reservation.seat FROM `flight`,reservation WHERE reservation.customer_c_id='.$id.' and flight.f_id=reservation.flight_f_id and TIMESTAMPDIFF(SECOND, NOW(), flight.d_time)<0');
    	break;
case "pfwp"://past flight with passenger
    	print 'Past flights with passengers&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_pfwp(\'pfwp\'); return false;">Collapse results</a>';
	$db->get_table_("flight", TRUE, 'SELECT flight.f_id, flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, flight.occupied FROM `flight` WHERE TIMESTAMPDIFF(SECOND, NOW(), flight.d_time)<0  and flight.occupied > 0', 4);
      break;
case "ffwp"://future flight with passenger
    	print 'Future flights with passengers&nbsp;&nbsp;<a href="javascript:void(0);" onclick="collapse_ffwp(\'ffwp\'); return false;">Collapse results</a>';
	$db->get_table_("flight", TRUE, 'SELECT flight.f_id, flight.flight_number, flight.d_time, flight.duration, flight.d_city, flight.a_city, flight.occupied FROM `flight` WHERE  TIMESTAMPDIFF(SECOND, NOW(), flight.d_time)>0 and flight.occupied > 0', 4);
      break;
    default:
	echo "nothing";
}

$db->disconnect();
?>
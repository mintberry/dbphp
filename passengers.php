<?php
session_start();
//print($_SESSION['uid']);
if(!isset($_SESSION['uid'])){
    //session not set, back to login
    header("Location: main.php");
}

$fid = $_GET['f_id'];

include 'sunapeedb.php';
$db = new SunapeeDB();
$db->connect();

echo("
<!DOCTYPE html>
 <html>
 <head>

 <link rel=\"stylesheet\" href=\"dbstyle.css\"> 
 <title>Passenger list</title>
 </head>
<body>
<div id=\"wrapper\">
<header>
<h2>
Passenger list of flight ".$fid."
</h2>
<div class=\"main\">

");
$db->get_table_("flight", TRUE, 'SELECT customer.c_id, customer.id_number, customer.name, customer.nationality, customer.gender, customer.birthday, reservation.seat FROM customer, reservation WHERE reservation.customer_c_id=customer.c_id and reservation.flight_f_id='.$fid);

echo("
</div>

<br/>
<br/>
</header>
</div>
<div id=\"footer\">
assembled by Xiaochen Qi, on ne_db. CS61 - Winter 2013

</div>
</body>
</html>
");

$db->disconnect();
?>
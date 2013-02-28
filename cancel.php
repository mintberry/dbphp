<?php
session_start();
$uid = $_SESSION['uid'];

$fid = $_GET['f_id'];
$ret = 0;

/*$dbh = new PDO('mysql:host=sunapee.cs.dartmouth.edu;dbname=ne_db', 'ne', 'littne', array( PDO::ATTR_PERSISTENT => false));

$stmt = $dbh->prepare("CALL book(?, ?, ?)");

$stmt->bindParam(1, $uid, PDO::PARAM_INT); 
$stmt->bindParam(2, $fid, PDO::PARAM_INT); 
$stmt->bindParam(3, $ret, PDO::PARAM_INT); 
$test = $stmt->execute();

if($test)
  $_SESSION["errno"]= 1;
else
$_SESSION["errno"] = 0;*/

$mysqli = new mysqli("sunapee.cs.dartmouth.edu", "ne", "littne", "ne_db");

if ($mysqli->connect_errno) {
  echo "Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error;
}

if (!$mysqli->query("SET @errno = 0") ||!$mysqli->query("CALL cancel(".$uid.",".$fid.", @errno)")) {
  echo "CALL failed: (" . $mysqli->errno . ") " . $mysqli->error;
}

if (!($res = $mysqli->query("SELECT @errno as _p_out"))) {
  echo "Fetch failed: (" . $mysqli->errno . ") " . $mysqli->error;
}
$row = $res->fetch_assoc();
$_SESSION['errno']=$row['_p_out'];
$mysqli->close();
header("Location: customer.php");
?>
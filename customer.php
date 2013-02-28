<?php
session_start();
//session check
  if(!isset($_SESSION['uid'])){
    //session not set, back to login
    header("Location: main.php");
    }
//print 'errno:'.$_SESSION['errno'];
?>
<!DOCTYPE html>
 <html>
 <head>
<meta http-equiv="cache-control" content="max-age=0" />
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="expires" content="0" />
<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
<meta http-equiv="pragma" content="no-cache" />
<script>
    //send ajax request
function showFlight(str)
{
  //alert(str);
if (str=="")
  {
p//  document.getElementById("flight").innerHTML="";
//  return;
  } 

if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
  xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

xmlhttp.onreadystatechange=function()
  {
  if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
    document.getElementById(str).innerHTML=xmlhttp.responseText;
    }
  }
//alert(str);
xmlhttp.open("GET","dbcalls.php?ct="+str,true);
xmlhttp.send();
}
//restore functions
function collapse(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'flight\'); return false;">Flight schedules</a>';
}
function collapse_res(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'reserve\'); return false;">Reservations</a>';
}
function collapse_ava(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'available\'); return false;">Available flights</a>';
}
function collapse_fut(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'future\'); return false;">My future flights</a>';
}
function collapse_pas(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'past\'); return false;">My past flights</a>';
}
//table entry functions
function GoTo(Url, val){
  //  alert(val);
  document.location.href = Url + "?f_id=" + val;
}
function ChangeColor(row, light){
  if (light)
    {
      row.style.backgroundColor = '#ecea89';
      row.style.cursor = 'hand';
    }
  else
    {
      row.style.backgroundColor = 'white';
      row.style.cursor = 'pointer';
    }
}
</script>
 <link rel="stylesheet" href="dbstyle.css"> 
 <title>Customer interface</title>
 </head>
				
<body>
<div id="wrapper">
<header>
<h1>
<?php
       include 'sunapeedb.php';
       $db = new SunapeeDB();
       $db->connect();

session_start();
$id=$_SESSION['uid'];

$db->get_table_('customer', TRUE, 'SELECT name FROM customer WHERE c_id='.$id, 1);
?>
</h1>
<h2>My TreeTop</h2>
<div align="right"><a href="logout.php" ALIGN=Right>log out</a> </div>

<form action="<?php $_PHP_SELF ?>" method="post">

</form>
<hr/>
<div class="main" id="flight">
<a href="javascript:void(0);" onclick="showFlight('flight'); return false;">Flight schedules</a>
</div>
<hr/>
<div class="main" id="reserve">
<a href="javascript:void(0);" onclick="showFlight('reserve'); return false;">Reservations</a>
</div><hr/>
<div class="main" id="available">
<a href="javascript:void(0);" onclick="showFlight('available'); return false;">Available flights</a>
</div><hr/>
<div class="main" id="future">
<a href="javascript:void(0);" onclick="showFlight('future'); return false;">My future flights</a>
</div><hr/>
<div class="main" id="past">
<a href="javascript:void(0);" onclick="showFlight('past'); return false;">My past flights</a>
</div>
<br/>
<br/>
</header>
</div>
<div id="footer">
assembled by Xiaochen Qi, on ne_db. CS61 - Winter 2013

</div>
</body>
</html>

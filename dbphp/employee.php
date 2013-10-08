<?php
session_start();
//print($_SESSION['uid']);
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
function collapse_pfwp(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'pfwp\'); return false;">Past flights with passengers</a>';
}
function collapse_ffwp(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showFlight(\'ffwp\'); return false;">Future flights with passengers</a>';
}
function collapse_date(str){
    document.getElementById(str).innerHTML='<a href="javascript:void(0);" onclick="showTop5(\'date\'); return false;">Top 5 flights</a>';
}
//table entry functions
function GoTo(Url, val){
  //alert(val);
  //document.location.href = Url + "?f_id=" + val;
  window.open(
	      Url + "?f_id=" + val,
	      '_blank' 
	      );
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
function showTop5(str){
  //validate date
  //  alert(document.getElementsByName("sday")[0].value);
var sday = document.getElementsByName("sday")[0].value;
var eday = document.getElementsByName("eday")[0].value;

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
//alert(sday+eday);
xmlhttp.open("GET","top5.php?ct="+str+"&sday="+sday+"&eday="+eday,true);
xmlhttp.send();
}
</script>
 <link rel="stylesheet" href="dbstyle.css"> 
 <title>Employee interface</title>
 </head>
				
<body>
<div id="wrapper">
<header>
<h1 style="color:blue">
<?php
       include 'sunapeedb.php';
       $db = new SunapeeDB();
       $db->connect();

session_start();
$id=$_SESSION['uid'];

$db->get_table_('employee', TRUE, 'SELECT name FROM employee  WHERE e_id='.$id, 1);
?>
</h1>
<h2>control panel</h2>
<div align="right"><a href="logout.php" ALIGN=Right>log out</a> </div>

<form action="<?php $_PHP_SELF ?>" method="post">

</form>
<hr/>
<div class="main" id="pfwp">
<a href="javascript:void(0);" onclick="showFlight('pfwp'); return false;">Past flights with passengers</a>
</div>
<hr/>

<div class="main" id="ffwp">
<a href="javascript:void(0);" onclick="showFlight('ffwp'); return false;">Future flights with passengers</a>
</div><hr/>
<div class="main">
  Input date(yyyy&ndash;mm&ndash;dd) to get top 5 booked flights<br/>
  Start date <input type="date" name="sday">
  End date   <input type="date" name="eday">
</div>
<div class="main" id="date">
<!-- date control?-->
  <!--form action="top5.php"-->
<a href="javascript:void(0);" onclick="showTop5('date'); return false;">Top 5 flights</a><br/>
<!--/form-->

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

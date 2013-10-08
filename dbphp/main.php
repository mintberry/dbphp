<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="dbstyle.css"> 
<title>Main interface</title>
</head>

<body>
<div id="wrapper">
<header class="main">
<h1>TreeTop Airline</h1>
<h2>User login</h2>
<form action="<?php $_PHP_SELF ?>" method="post">
<input type="radio" name="type" value="employee" >Employee
<input type="radio" name="type" value="user" checked>Customer<br><br>
Username(ID)<input type="text" name="uid">
Password<input type="password" name="pwd"><br><br>
<input type="submit">&nbsp;&nbsp;
<input type="reset">
</form>
<?php
   
   mb_internal_encoding('UTF-8');
   mb_http_output('UTF-8');
   
   if(!strcmp($_POST["type"], "employee") || !strcmp($_POST["type"], "user"))
   {

       include 'sunapeedb.php';
       $db = new SunapeeDB();
       $db->connect();

       $auth = $db->author($_POST["type"], $_POST["uid"], $_POST["pwd"]);
       if($auth == -1){
           print("<font color=\"red\">username or password error.</font>");
       }

       $db->disconnect();
   } else { /*print("No table selected.");*/ }
?>
</header>

</div>
<div id="footer">
assembled by Xiaochen Qi, on ne_db. CS61 - Winter 2013
<a href='README' target='_blank'>manual</a>
<a href='README.TESTING' target='_blank'>test</a>

</div>
</body>
</html>

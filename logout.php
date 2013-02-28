<?php
  /*session_start();
unset($_SESSION["id"]);  // where $_SESSION["nome"] is your own variable. if you do not have one use only this as follow **session_unset();**
header("Location: main.php");*/


session_start();
  if(isset($_SESSION['uid']))
    unset($_SESSION['uid']);
  session_destroy();
  header("Location: main.php");

?>
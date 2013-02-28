<?php
class SunapeeDB
{
    const HOST = "sunapee.cs.dartmouth.edu";
    const USER = "ne";
    const PASS = "littne";
    const DB   = "ne_db";
    private $con = NULL;

    public function connect()
    {
        $this->con = mysql_connect(self::HOST, self::USER, self::PASS);
	if(!$this->con) { die("SQL Error: " . mysql_error()); }
	mysql_select_db(self::DB, $this->con);
	mysql_set_charset("utf8mb4");
    }

    public function get_table($table)
    {
	if($this->con === NULL) { return; }
	
	$result = mysql_query("SELECT * FROM $table;");

	if(!$result) { die("SQL Error: " . mysql_error()); }

	$this->print_table($result);

	mysql_free_result($result);
    }

    public function insert_student($id, $name, $dept, $credits)
    {
        if($this->con === NULL) { return false; }

	$result = mysql_query("INSERT INTO student (ID, name, dept_name, tot_cred) VALUES (" . $id . ",\"" . $name . "\",\"" . $dept . "\"," . $credits . ");");

	if(!$result) { die("SQL Error: " . mysql_error()); }

	mysql_free_result($result);

	return true;
    }

    private function print_table($result)
    {
      $rcnt = mysql_num_rows($result);
      if($rcnt == 0){
	print("<br/>No entry available");
      }else{

     	print("<table>\n<thead><tr>");
	for($i=0; $i < mysql_num_fields($result); $i++) {
	    print("<th>" . mysql_field_name($result, $i) . "</th>");
	}
	print("</tr></thead>\n");
	
	while ($row = mysql_fetch_assoc($result)) {
    	      print("\t<tr>\n");
    	      foreach ($row as $col) {
       	          print("\t\t<td>$col</td>\n");
    	      }
    	      print("\t</tr>\n");
	}
	print("</table>\n");    }
    }	

    //enhanced print table
    private function print_table_($result, $target='book.php')
    {
      $rcnt = mysql_num_rows($result);
      if($rcnt == 0){
	print("<br/>No entry available");
      }else{
     	print("<table>\n<thead><tr>");
	for($i=1; $i < mysql_num_fields($result); $i++) {
	    print("<th>" . mysql_field_name($result, $i) . "</th>");
	}
	print("</tr></thead>\n");
        $id_name = mysql_field_name($result, 0);
	while ($row = mysql_fetch_assoc($result)) {

	  print("\t<tr onmouseover=\"ChangeColor(this, true);\" 
              onmouseout=\"ChangeColor(this, false);\" 
              onclick=\"GoTo('".$target."', ".$row[$id_name].");\" >\n");
	  unset($row[$id_name]);
    	      foreach ($row as $col) {
       	          print("\t\t<td>$col</td>\n");
    	      }
    	      print("\t</tr>\n");
	}
	print("</table>\n");    }
    }	

    //authorization
    public function author($type, $id, $passwd)
    {
	if(!strcmp($type, "employee")){
	    $pwd = mysql_query("select password from employee where $id = e_id");
	if(!pwd) { die("SQL Error: " . mysql_error()); }
	$pwd = mysql_result($pwd, 0);
	//print($passwd."::".$pwd);
	    if(!strcmp($pwd, $passwd)){
		 //go to employee page
		 print("employee login");
		 session_start();
		 // store session data
		 $_SESSION['uid']=$id;
		 header("Location: employee.php");
	     }else
	     {
		 return -1;
	     }
	 }else if(!strcmp($type, "user")){
	     $pwd = mysql_query("SELECT password FROM user WHERE $id= c_id");
	 if(!pwd) { die("SQL Error: " . mysql_error()); }
	 $pwd = mysql_result($pwd, 0);
	 //	 print($passwd."::".$pwd."||".$id.".");
	     if(!strcmp($pwd, $passwd)){
		 //go to customer page
		 print("customer login");
		 session_start();
		 // store session data
		 $_SESSION['uid']=$id;
       		 header("Location: customer.php");
		 
	     }else
	     {
		 return -1;
	     }
	}
    }

    //enhanced get table
    public function get_table_($table, $where=FALSE, $query, $simple=0)
    {
	if($this->con === NULL) { return; }

	  if(!$where){

	  $result = mysql_query("SELECT * FROM $table;");
	 }else{
	$result = mysql_query($query);		
	 }

	if(!$result) { die("SQL Error: " . mysql_error()); }
	if(!$simple){
	  $this->print_table($result);
	}else if($simple == 1){
	print("<br/>\n");
	
	while ($row = mysql_fetch_assoc($result)) {
    	      print("\t\n");
    	      foreach ($row as $col) {
       	          print("\t\t$col\n");
    	      }
    	      print("\t\n");
	}

	}else if($simple == 2){
	  $this->print_table_($result);
	}else if($simple == 3){
	  $this->print_table_($result, 'cancel.php');
	}else if($simple == 4)
	{
	  $this->print_table_($result, 'passengers.php'); 
	}
	mysql_free_result($result);
    }


    public function disconnect()
    {
	if($this->con != NULL) { mysql_close($this->con);}
    }

}
?>
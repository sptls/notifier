<?php
    $pass = $_POST["pass"];
    if($pass != "2137")
    {
        echo "ACCESS DENIED";
    }
    else
    {
        $user = $_POST['user'];
        $msg_id = $_POST['id'];

        $msg_file = fopen("users/$user/$msg_id", "r");
        $msg = fgets($msg_file);
        echo "$msg";
        $msg = fgets($msg_file);
        echo "$msg";
        fclose($msg_file);
    }
?>
<?php
    $user = $_POST["user"];
    $counter_file = fopen("users/$user/counter", "r");
    $counter_val = fgets($counter_file);
    fclose($counter_file);

    echo $counter_val;
?>
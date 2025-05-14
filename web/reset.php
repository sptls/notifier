<?php

function rrmdir($src) {
    $dir = opendir($src);
    while(false !== ( $file = readdir($dir)) ) {
        if (( $file != '.' ) && ( $file != '..' )) {
            $full = $src . '/' . $file;
            if ( is_dir($full) ) {
                rrmdir($full);
            }
            else {
                unlink($full);
            }
        }
    }
    closedir($dir);
    rmdir($src);
}

$pass = $_POST["pass"];

if($pass == "2137")
{
    $user = $_POST["user"];

    rrmdir("users/$user");

    mkdir("users/$user");
    $counter_file = fopen("users/$user/counter", "w");
    fwrite($counter_file, "1");
    echo "OK";
}
else
{
    echo "FAIL";
}

?>
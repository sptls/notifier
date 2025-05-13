<?php
    if(isset($_POST["pass"]))
    {
        $pass = $_POST["pass"];
        if($pass != "2137")
        {
            echo "Wrong password";
        }
        else
        {
            $user = $_POST["user"];
            $msg = $_POST["msg"];
            if(isset($_POST["title"]))
            {
                $title = $_POST["title"];
            }
            else
            {
                $title = "Notification";
            }

            $user_msg_counter_file = fopen("users/$user/counter", "r");
            $counter = intval(fgets($user_msg_counter_file));
            fclose($user_msg_counter_file);



            echo "User: $user<br>";
            echo "Msg $msg<br>";
            echo "Counter: $counter<br>";

            $counter++;

            $user_msg_counter_file = fopen("users/$user/counter", "w");
            fwrite($user_msg_counter_file, $counter);
            fclose($user_msg_counter_file);

            $message_file = fopen("users/$user/$counter", "w");
            fwrite($message_file, $title);
            fwrite($message_file, "\n");
            fwrite($message_file, $msg);
            fclose($message_file);
        }
    }
    else
    {
        echo "Missing password";
    }
?>
<?php

require_once( './RemoteLogin.php' );

$rl = new RemoteLogin();
$rl->verifyToken();
$rl->redirect($_REQUEST['requestUrl']);
?>




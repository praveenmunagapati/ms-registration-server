<?php

require_once('../api/RemoteLogin.php');

$rl = new RemoteLogin();
$rl->endSession();
$rl->redirect($rl->getBaseUrl() . 'index.php');

?>


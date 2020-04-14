<?php

require_once( '../api/RemoteLogin.php' );

$rl = new RemoteLogin();
if ($rl->checkSession()) {
	echo 'Authentication passed.';
} else {
	echo 'Error: need to re-authenticate [will redirect to the login page]';
}

require_once( './head.php');
?>

<?php require_once( './foot.php'); ?>

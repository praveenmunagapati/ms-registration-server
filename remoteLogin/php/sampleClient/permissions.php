<?php

require_once('../api/RemoteLogin.php');

$rl = new RemoteLogin();
try {
	$bits = $rl->getPermissions($_REQUEST['labkeyFolder']);
} catch ( Exception_RL $e ) {
	if ($e->getCode() == Exception_RL::BAD_TOKEN) {
		$rl->redirect( $rl->buildAuthUrl( $rl->getBaseUrl() 
				. 'permissions.php?labkeyFolder=' . $_REQUEST['labkeyFolder'] ) );
	}
}
require_once('./head.php');
echo '<div style="border: solid 1px; width: 400px; padding:20px;margin: 20px;">';
echo '<h2>Permission Test</h2>';
echo 'These pages will first check the session data, then will check verifyToken.view and log in to the authentication server if required.';
echo "<ul>";
echo "<li>Permission (int): " . $bits . "</li>";
echo "<li>Read: " . ($bits & RemoteLogin::PERMISSION_READ ? 'true' : 'false') . "</li>";
echo "<li>Insert: " . ($bits & RemoteLogin::PERMISSION_INSERT ? 'true' : 'false') . "</li>"; 
echo "<li>Update: " . ($bits & RemoteLogin::PERMISSION_UPDATE ? 'true' : 'false') . "</li>";
echo "<li>Admin: " . ($bits & RemoteLogin::PERMISSION_ADMIN ? 'true' : 'false') . "</li>";
echo "</ul>";
echo "</div>";
require_once('./foot.php'); 

?>
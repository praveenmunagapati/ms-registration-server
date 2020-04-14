<?php

require_once('../api/RemoteLogin.php');

$rl = new RemoteLogin();
$authUrl = $rl->buildAuthUrl($rl->getBaseUrl() . 'authPage.php');

$currentStatus = $rl->checkSession();
$loginUrl = $rl->buildAuthUrl($rl->getBaseUrl() . 'index.php');
require_once('./head.php');
?>

<div style="border: solid 1px; width: 400px; padding:20px;margin: 20px;">
	<h2>Auth Page</h2>
	
	<a href="<?php echo $authUrl ?>">A page requiring authentication</a>
	
</div>

<div style="border: solid 1px; width: 400px; padding:20px;margin: 20px;">
	<h2>Test Permission Pages</h2>
	<p>
	These folders on the authentication server have been set up with the 
	following permissions. 
	</p>
	<ul>
	<li><a href="./permissions.php?labkeyFolder=/test/remote login test/admin">/remote login test/admin</a></li>
	<li><a href="./permissions.php?labkeyFolder=/test/remote%20login%20test/author">/remote login test/author</a></li>
	<li><a href="./permissions.php?labkeyFolder=/test/remote%20login%20test/editor">/remote login test/editor</a></li>
	<li><a href="./permissions.php?labkeyFolder=/test/remote%20login%20test/none">/remote login test/none</a></li>
	<li><a href="./permissions.php?labkeyFolder=/test/remote%20login%20test/read">/remote login test/read</a></li>
	<li><a href="./permissions.php?labkeyFolder=/test/remote%20login%20test/submitter">/remote login test/submitter</a></li>
	</ul>
</div>

<div style="border: solid 1px; width: 400px; padding:20px;margin: 20px;">
	<h2>Status</h2>
	<p>Status [on this server]: <strong><?php echo $currentStatus ? 'Logged In' : 'Logged out'; ?></strong></p>
	<p><a href="<?php echo $loginUrl ?>">Log In</a> [checks your login status on labkey.org, logs you on here after you log in there]</p>
	<p><a href="./logout.php">Log Out</a> [just logs out of this server, not authenticating server]</p>
</div>

<?php require_once('./foot.php'); ?>
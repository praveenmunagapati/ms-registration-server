<?php


class RemoteLogin {
	# authenticating server URLs
	const REMOTE_BASE_URL = 'https://www.example.com/Login';
	
	# this server's PATH info
	const LOCAL_API_PATH = '/labkey/php/api/';
	const LOCAL_BASE_PATH = '/labkey/php/sampleClient/';
	
	const PERMISSION_READ = 1;
	const PERMISSION_INSERT = 2;
	const PERMISSION_UPDATE = 4;
	const PERMISSION_ADMIN = 32768;
	
	public function __construct () {
		error_reporting(E_ALL | E_STRICT);
		ini_set('session.use_only_cookies', true);
		session_name('LABKEYSESSION'); # in case there are other PHP sessions on this server
		session_start();
	}
	
	/**
	 * Returns parts of current URL
	 *
	 * @return array of URL parts
	 */
	public function getUrlParts () {
		$thisUrl = 'http' . (  isset($_SERVER["HTTPS"]) && $_SERVER["HTTPS"] == "on" ?  "s" : '' ) . '://'
				. $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];
		return parse_url($thisUrl);
	}

	/**
	 * Gets the URL of the Remote Login API
	 *
	 * @return string
	 */
	public function getApiUrl () {
		$urlParts = $this->getUrlParts();
		$path = $urlParts['scheme'] . '://' . $urlParts['host'] 
				. self::LOCAL_API_PATH;
		if ( substr($path, strlen($path) - 2, 1) ) {
			$path .= '/';
		}
		return $path;
	}
	
	/**
	 * Gets the base URL
	 *
	 * @return string
	 */
	public function getBaseUrl () {
		$urlParts = $this->getUrlParts();
		$path =  $urlParts['scheme'] . '://' . $urlParts['host'] 
				. self::LOCAL_BASE_PATH;
		if ( substr($path, strlen($path) - 2, 1) ) {
			$path .= '/';
		}
		return $path;
	}
	
	/**
	 * Builds a URL to access a page requiring authentication
	 * 
	 * If user is not logged in, user will get authenticating server's login 
	 * page, then redirected to start session page, then requested page.
	 * 
	 * If already logged in, URL redirects to start session page, then the 
	 * requested page.
	 * 
	 * @param string $requestUrl
	 * @return string URL
	 */
	public function buildAuthUrl ($requestUrl) {
		if (isset($_SESSION['labkeyToken']) and strlen($_SESSION['labkeyToken'])) {
			$url = $requestUrl;	
		} else {
			$url = self::REMOTE_BASE_URL . '/createToken.view?returnUrl=' . urlencode( 
					$this->getApiUrl() . 'session.php?requestUrl=' . urlencode( $requestUrl ) );
		}
		return $url;
	}
	
	
	/**
	 * Checks the token we received from createToken to verify login before 
	 * starting session.
	 * 
	 * This method is called each time we start a session and each time
	 * we need to check for a new folder's permissions.
	 *
	 * @param string $token
	 * @return boolean
	 */
	public function verifyToken () {
		$token = $_REQUEST['labkeyToken'];
		$page = $this->sendVerifyToken(self::REMOTE_BASE_URL, $token);
		
		$doc = new DOMDocument();
		$doc->loadXML($page);
		if ($doc->firstChild->nodeName != 'TokenAuthentication') {
			throw new Exception('Bad XML received from authenticating server');
		}
		if ($doc->firstChild->getAttribute('success') == 'true') {
			# add labkeyToken (effectively logs user in)
			$_SESSION['labkeyToken'] = $token;
			$permissions = $doc->firstChild->getAttribute('permissions');
			if (!is_null($permissions)) {
				$_SESSION['permissionMap']['/'] = $permissions;
			}
			return TRUE;
		} else {
			return FALSE;
		}
	}
		
	/**
	 * Sends the actual request to the authenticating server
	 *
	 * @param string $baseUrl
	 * @param string $token
	 * @return string of the page's contents
	 */
	private function sendVerifyToken ($baseUrl, $token) {
		$ch = curl_init();
		$url = str_replace(' ', '%20', $baseUrl) . '/verifyToken.view?labkeyToken=' . $token;
		
		curl_setopt($ch, CURLOPT_URL, $url );
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 30);
		$page = curl_exec($ch);
		// <TokenAuthentication success="true" token="bb8aeb0de92f02175f92bb60fb84c8b7" email="user@example.com" permissions="17"/>
		curl_close($ch);
		return $page;
	}
	
	/**
	 * Checks if we're logged in by checking if there's a labkeyToken stored in
	 * the session vars.
	 *
	 * @return boolean, TRUE if logged in
	 */
	public function checkSession () {
		return (isset($_SESSION['labkeyToken']) and strlen($_SESSION['labkeyToken'])) 
				? TRUE : FALSE;
	}
	
	/**
	 * Checks session var to determine if user has permission to specified
	 * folder. If folder is not in session var, it uses sendVerifyToken() to 
	 * check new folder.
	 *
	 * @param string $path - folder path to check
	 */
	public function getPermissions ($labkeyFolderPath) {
		if (substr($labkeyFolderPath,0,1) != '/') {
			$labkeyFolderPath = '/' . $labkeyFolderPath;
		}
		if (substr($labkeyFolderPath,0,1) != '/') {
			$labkeyFolderPath = substr($labkeyFolderPath, 0, strlen($labkeyFolderPath)-1);
		}
		
		if (isset($_SESSION['permissionMap'][$labkeyFolderPath])) {
			return $_SESSION['permissionMap'][$labkeyFolderPath];
		} else {
			if (!isset($_SESSION['labkeyToken'])) {
				throw new Exception_RL( 'not logged in / bad labkeyToken', 
						Exception_RL::BAD_TOKEN);
			}
			$page = $this->sendVerifyToken(self::REMOTE_BASE_URL . $labkeyFolderPath, $_SESSION['labkeyToken']);
			
			try {
				$doc = new DOMDocument();
				$doc->loadXML($page);
				if ($doc->firstChild->nodeName != 'TokenAuthentication') {
					throw new Exception();
				}
			} catch ( Exception $e ) {
			 	throw new Exception_RL('Bad XML received from authenticating server',
			 			Exception_RL::BAD_XML);
			}
			
			if ($doc->firstChild->getAttribute('success') != 'true'){
				throw new Exception_RL( 'not logged in / bad labkeyToken', 
						Exception_RL::BAD_TOKEN);
			}
			$permissions = $doc->firstChild->getAttribute('permissions');
			if (is_null($permissions)) {
				throw new Exception_RL('Bad XML received from authenticating server',
			 			Exception_RL::BAD_XML);
			}
			$_SESSION['permissionMap'][$labkeyFolderPath] = $permissions;
			return $permissions;
		}
	}
	
	/**
	 * Removes session and effectively logs user out.
	 */
	public function endSession () {
		session_unset();
		session_destroy();
		$_SESSION = array();
		session_regenerate_id();
	}
	
	/**
	 * Redirect utility
	 *
	 * @param string $url of redirect target
	 */
	public function redirect ($url) {
		header("Location: " . $url);
	}
}

class Exception_RL extends Exception {
	const BAD_TOKEN = 1;
	const BAD_XML = 2;
	
	public function __construct($message, $code) {
		parent::__construct($message, $code);
	}

}

?>
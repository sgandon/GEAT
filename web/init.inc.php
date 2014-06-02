<?php
session_start();
require('config_defaults.php');
require('config_local.php');
require('functions.inc.php');

## CONNECT TO DATA SOURCE ##
$popeye = mysql_connect(
  $conf['db_host'],
  $conf['db_user'],
  $conf['db_pass']
  )
  or die('Connection failed: '.mysql_error());

mysql_select_db($conf['db_name'], $popeye);

$cache = array();

load_result_status_cache();

// branches
$page['branches'] = get_branches();

if (isset($_POST['switch_branch'])) {
  $_SESSION['branch'] = $_POST['branch'];
}

$branch_ids = array();
foreach ($page['branches'] as $branch) {
  array_push($branch_ids, $branch['id']);
}

if (!isset($_SESSION['branch']) or !in_array($_SESSION['branch'], $branch_ids)) {
  $_SESSION['branch'] = $page['branches'][0]['id'];
}

$page['charts'] = get_charts($conf['charts_dir']);
?>
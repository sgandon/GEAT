<?php
## RETRIEVE THE AVAILBLE ##
$list_language = mysql_query("SELECT DISTINCT(language) FROM results_def");
$list_inf = mysql_query("SELECT DISTINCT(revision) FROM executions");
$list_sup = mysql_query("SELECT DISTINCT(revision) FROM executions ORDER BY revision DESC");
$list_revision = mysql_query("SELECT DISTINCT(revision) FROM executions ORDER BY revision DESC");

## SET SQL CONDITION STRING TEMPORAL VARIABLES ##
## BOOLEAN SET AS TRUE WHEN A BUTTON IS CLICKED ##
$olive = (!$_GET['sort_switcher'] && !$_GET['page'] && !$_GET['hide_filter'] && !$_GET['hide_manager']);
## KEY WORD ##
if($_POST['keywd']){
	$_SESSION['keywd'] = $_POST['keywd'];
}
if($_POST['do_search'] && $_POST['keywd'] == "" || !isset($_SESSION['keywd'])){
	$_SESSION['keywd'] = "";
	$condition_keywd = null;
}else{
	$str_keywd = str_replace("%","\%",$_SESSION['keywd']);
	$str_keywd = str_replace(" ","%' AND j.name LIKE '%",$str_keywd);
	$condition_keywd = " AND j.name LIKE '%".$str_keywd."%'";
}

## LANGUAGE ##
if (empty($_POST['language'])){
  unset($_SESSION['language']);
}
else {
  $_SESSION['language'] = $_POST['language'];
}

if (empty($_SESSION['language'])) {
  $_SESSION['language'] = array('any');
}

if (in_array('any', $_SESSION['language'])) {
  $condition_language = null;
  $_SESSION['filter_language'] = null;
}
else{
  $condition_language = sprintf(
    ' AND j.language IN (%s)',
    implode(
      ', ',
      prepend_append_array_items($_SESSION['language'], "'", "'")
      )
    );
}

## STATUS ##
if (empty($_POST['status'])) {
  unset($_SESSION['status']);
  $_SESSION['status'] = array('any');
}
else {
  $_SESSION['status'] = $_POST['status'];
}

if (empty($_SESSION['status'])) {
  $_SESSION['status'] = array('any');
}

if (in_array('any', $_SESSION['status'])) {
  $condition_status = null;
  $_SESSION['filter_status'] = null;
}
else{
  $condition_status =
    sprintf(
      ' AND id_status IN (%s)',
      implode(
        ',',
        $_SESSION['status']
        )
      );
}

## BRANCH ##
$condition_branch = " AND branch = '".$_SESSION['branch']."'";

## REVISION ##
if($_POST['rev_select']){
	$_SESSION['rev_select'] = $_POST['rev_select'];
}
if($_POST['revision_inf']){
	$_SESSION['revision_inf'] = $_POST['revision_inf'];
}
if($_POST['revision_sup']){
	$_SESSION['revision_sup'] = $_POST['revision_sup'];
}
if($_POST['revision_last']){
	$_SESSION['revision_last'] = $_POST['revision_last'];
}
if($_POST['selected_revision']=="" && $olive){
	unset($_SESSION['selected_revision']);
	$_SESSION['selected_revision'][0] = "all";
}else{
	if($_POST['selected_revision']){
		$_SESSION['selected_revision'] = $_POST['selected_revision'];
	}
}
switch ($_SESSION['rev_select']){
	case "by_range":
	## BY RANGE ##
	unset($_SESSION['revision_last']);
	unset($_SESSION['selected_revision']);
	if(!isset($_SESSION['revision_inf']) || $_SESSION['revision_inf'] == "earliest"){
		$str_inf = "";
	}else{
		$str_inf = " AND revision >= ".$_SESSION['revision_inf'];
	}
	if(!isset($_SESSION['revision_sup']) || $_SESSION['revision_sup'] == "latest"){
		$str_sup = "";
	}else{
		$str_sup = " AND revision <= ".$_SESSION['revision_sup'];
	}
	$condition_revision = " AND revision in (SELECT revision FROM results_def"
		." WHERE revision > 0".$str_inf.$str_sup.")";
	break;
	case "by_list":
	## BY LIST ##
	unset($_SESSION['revision_last']);
	unset($_SESSION['revision_inf']);
	unset($_SESSION['revision_sup']);
	if ($_SESSION['selected_revision'][0] == "" || $_SESSION['selected_revision'][0] == "all"){
		$condition_revision = null;
		$_SESSION['filter_select_rev'] = null;
	}
        else {
          $condition_revision = sprintf(
            ' AND revision IN (%s)',
            implode(
              ',',
              $_SESSION['selected_revision']
              )
            );
	}
	break;
	case "by_last":
	## LAST ##
	unset($_SESSION['selected_revision']);
	unset($_SESSION['revision_inf']);
	unset($_SESSION['revision_sup']);
	$last_revision_no = mysql_result(mysql_query("SELECT MAX(revision) FROM executions"), 0);
	if(!isset($_SESSION['revision_last']) || $_POST['do_search'] && $_POST['revision_last'] == ""){
		$_SESSION['revision_last'] = $last_revision_no;
		$condition_revision = null;
	}else{
		$condition_revision = " AND revision BETWEEN "
			.($last_revision_no - $_SESSION['revision_last'] + 1)
			." AND ".$last_revision_no;
	}
	break;
	default:
	$condition_revision = null;
}

## PRIMARY ORDER ##
if($_POST['primary_order']){$_SESSION['primary_order'] = $_POST['primary_order'];}
if (!isset($_SESSION['primary_order']) || $_SESSION['primary_order'] == 'revision'){
	$condition_primary_order = " ORDER BY revision";
}else{
	$condition_primary_order = " ORDER BY ".$_SESSION['primary_order'];
}
if($_POST['primary_desc_tag']){$_SESSION['primary_desc_tag'] = $_POST['primary_desc_tag'];}
if (!isset($_SESSION['primary_desc_tag']) || $_SESSION['primary_desc_tag'] == 'desc'){
	$condition_primary_desc_tag = " DESC";
}else{
	$condition_primary_desc_tag = " ASC";
}

## SECONDARY ORDER ##
$sort_switcher = $_GET['sort_switcher'];
if($_GET['sort_switcher']){
	if(!isset($_SESSION['sort_tag'])){$_SESSION['sort_tag'] = 0;}
	$_SESSION['sort_tag']++;
	$_SESSION['sort_switcher'] = $_GET['sort_switcher'];
}
if($_SESSION['sort_tag']%2 == 0 || $_POST['do_search']){
	$secondary_desc_tag = "";
}else{
	$secondary_desc_tag = " DESC";
}
switch ($sort_switcher){
case "1":
	$secondary_order = ", j.name".$secondary_desc_tag;
	break;
	case "2":
	$secondary_order = ", e.revision".$secondary_desc_tag;
	break;
	case "3":
	$secondary_order = ", j.language".$secondary_desc_tag;
	break;
	case "4":
	$secondary_order =  ", r.moment".$secondary_desc_tag;
	break;
	case "5":
	$secondary_order = ", r.status".$secondary_desc_tag.", r.substatus".$secondary_desc_tag;
	break;
	default:
	$secondary_order = null;
}

// how many lines in total
$query = "
SELECT
    COUNT(*)
  FROM results_def AS r
    INNER JOIN executions AS e ON e.id = id_exec
    INNER JOIN jobs AS j ON j.id = r.id_job
  WHERE 1 = 1
    ".$condition_status."
    ".$condition_substatus."
    ".$condition_keywd."
    ".$condition_language."
    ".$condition_revision."
    ".$condition_branch."
";
list($page['nb_results']) = mysql_fetch_row(pwg_query($query));

// results for the current page
if (!isset($_SESSION['results_per_page'])){
  $_SESSION['results_per_page'] = $conf['default_results_per_page'];
}

if ($_POST['do_search']) {
  if (!$_POST['results_per_page'] || intval($_POST['results_per_page']) == 0) {
    $_SESSION['results_per_page'] = $conf['default_results_per_page'];
  }
  else{
    $_SESSION['results_per_page'] = intval($_POST['results_per_page']);
  }
}

$limit = isset($_GET['page'])
    ? ($_GET['page'] - 1) * $_SESSION['results_per_page']
    : '0'
;
$offset = $_SESSION['results_per_page'];

$query = "
SELECT
    e.branch,
    e.revision,
    j.name AS job,
    j.language,
    r.id_status,
    r.moment,
    r.description
  FROM results_def AS r
    INNER JOIN executions AS e ON e.id = r.id_exec
    INNER JOIN jobs AS j ON j.id = r.id_job
  WHERE 1 = 1
    ".$condition_status."
    ".$condition_substatus."
    ".$condition_keywd."
    ".$condition_language."
    ".$condition_revision."
    ".$condition_branch."
  ".$condition_primary_order." ".$condition_primary_desc_tag."
  ".$secondary_order."
  LIMIT ".$limit.", ".$offset."
";

$result = pwg_query($query);
$page['results'] = array();
while ($row = mysql_fetch_assoc($result)) {
  array_push($page['results'], $row);
}

$page['num_pages'] = ceil($page['nb_results']/$_SESSION['results_per_page']);

## SHARING FILTER MANAGEMENT ##
## LOAD ##
if($_POST['load_filter']){
	$_SESSION['fn'] = $_POST['filter_loader'];
	$record = mysql_query("SELECT * FROM filter WHERE filter_no = ".$_SESSION['fn']);
	$filter_loaded = mysql_fetch_array($record);
	$_SESSION['filter_name'] = $filter_loaded['filter_name'];
	$unserialized_filter = unserialize(stripslashes($filter_loaded['filter_info']));
	$_SESSION['keywd'] = $unserialized_filter[0];
	$_SESSION['language'] = $unserialized_filter[1];
	$_SESSION['substatus'] = $unserialized_filter[2];
	$_SESSION['rev_select'] = $unserialized_filter[3];
	$_SESSION['revision_inf'] = $unserialized_filter[4];
	$_SESSION['revision_sup'] = $unserialized_filter[5];
	$_SESSION['revision_last'] = $unserialized_filter[6];
	$_SESSION['selected_revision'] = $unserialized_filter[7];
        $_SESSION['branch'] = $unserialized_filter[8];
}
## SAVE ##
if($_POST['save_filter']){
	$filter_no = mysql_result(mysql_query("SELECT MAX(filter_no) FROM filter"), 0)+1;
	$msg_save_filter = "
INSERT INTO filter
  VALUE (".$filter_no.", '".$_POST['name_filter']."', 'true', '";
	$filter_array = array (
			$_SESSION['keywd'],
			$_SESSION['language'],
			$_SESSION['substatus'],
			$_SESSION['rev_select'],
			$_SESSION['revision_inf'],
			$_SESSION['revision_sup'],
			$_SESSION['revision_last'],
			$_SESSION['selected_revision'],
                        $_SESSION['branch'],
			);
	$serialized_filter = addslashes(serialize ($filter_array));
	$msg_save_filter .= $serialized_filter."')";
	$is_filter_saved = mysql_query($msg_save_filter);
}
## DELETE ##
if($_POST['delete_filter']){
	$msg_delete_filter = "UPDATE filter"
		." SET enabled = 'false' WHERE filter_no = ".$_POST['filter_loader'];
	mysql_query($msg_delete_filter);
}
## ERASE ##
if($_POST['erase_filter']){
	$msg_erase_filter = "DELETE FROM filter WHERE enabled = 'false'";
	mysql_query($msg_erase_filter);
}
## RELOAD FILTER LIST ##
$list_filter = mysql_query("
SELECT
    filter_no,
    filter_name
  FROM filter
  WHERE enabled = 'true'
  ORDER BY filter_no DESC
;");

## RESET CONDITION ##
if ($_POST['reset_condition']){
	$_SESSION['keywd'] = "";
	unset($_SESSION['language']);
        unset($_SESSION['branch']);
	unset($_SESSION['substatus']);
	$_SESSION['rev_select'] = "any";
	$_SESSION['revision_last'] = "";
	unset($_SESSION['selected_revision']);
	$_SESSION['revision_inf'] = "earliest";
	$_SESSION['revision_sup'] = "latest";
	$_SESSION['primary_order'] = "Revision";
	$_SESSION['primary_desc_tag'] = "DESC";
	$_SESSION['sort_tag'] =	0;
	unset($_SESSION['sort_switcher']);
	unset($_SESSION['executed']);
	$_SESSION['hide_filter'] = 0;
	$reset = "disable";
	unset($_POST['reset_condition']);
}else{
	$reset = "enable";
}

// echo '<pre>'; print_r($page['results']); echo '</pre>';
if (isset($_GET['page'])) {
  $_SESSION['selected_page'] = $_GET['page'];
}
else {
  $_SESSION['selected_page'] = 1;
}

## DISCONNECT ##
# mysql_close($popeye);
?>
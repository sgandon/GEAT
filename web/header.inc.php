<html>
  <head>
    <title><?php echo $conf['application_name'].' '.$conf['version'] ?></title>
    <link href="style.php" rel="stylesheet" type="text/css" />
    <script language="javascript" src="rev_selection.js"></script>
<?php
if (isset($conf['specific_header_filepath'])) {
  ob_start();
  include($conf['specific_header_filepath']);
  $specific_header = ob_get_contents();
  ob_end_clean();
  echo $specific_header;
}
?>
  </head>

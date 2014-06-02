<?php
$memory = ((memory_get_peak_usage() / 1024) / 1024);
?>
<!-- <?php echo sprintf('%.1f MB', $memory); ?> -->
<p id="footer">powered by <?php echo $conf['application_name'].' '.$conf['version'] ?></p>

<?php
if ($conf['footer_filepath']) {
  ob_start();
  include($conf['footer_filepath']);
  $footer = ob_get_contents();
  ob_end_clean();

  echo $footer;
}
?>
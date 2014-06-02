<?php
if (isset($conf['banner_filepath']))
{
  ob_start();
  include($conf['banner_filepath']);
  $banner = ob_get_contents();
  ob_end_clean();
  echo $banner;
}

?><div style="float:left;">Currently Running : r<?=getCurrentRunningRevision() ?></div> <?

if (count($page['branches']) > 1) {
?>
<form method="POST" id="branchSelector">
  category
  <select name="branch">
<?php
foreach ($page['branches'] as $branch) {
  $selected = '';
  
  if ($branch['id'] == $_SESSION['branch']) {
    $selected = 'selected="selected"';
  }
  
  echo sprintf(
    '
    <option value="%s" %s>%s</option>',
    $branch['id'],
    $selected,
    $branch['name']
    );
}
?>

  </select>
  <input name="switch_branch" type="submit" value="Switch"/>
</form>

<?php
}

$menu_pages = array();
$menu_pages[] = 'executions';
$menu_pages[] = 'jobs';

$query = '
SELECT COUNT(*)
  FROM components
;';
list($nb_components) = mysql_fetch_row(pwg_query($query));
if ($nb_components > 0) {
  $menu_pages[] = 'components';
}

$menu_pages[] = 'search';

if ($page['charts'] !== false and count($page['charts']) > 0) {
  $menu_pages[] = 'charts';
}

$menu_pages[] = 'about';

$current_script = basename($_SERVER[SCRIPT_FILENAME]);

$menu_lines = array();
foreach ($menu_pages as $menu_page) {
  $script = $menu_page.'.php';
  
  $class = '';
  if ($current_script == $script) {
    $class = 'class="navBarSelected"';
  }

  array_push(
    $menu_lines,
    sprintf(
      '<a %s href="%s">%s</a>',
      $class,
      $script,
      $menu_page
      )
    );
}

$html_menu = implode("\n  | ", $menu_lines);
?>

<p id="navBar">
  <?php echo $html_menu ?>
</p>

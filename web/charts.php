<?php
require('init.inc.php');

if ($page['charts'] === false) {
  die('The charts directory "'.$conf['charts_dir'].'" is probably missing');
}

include('header.inc.php');
?>

  <body id="charts">
<?php
include('top_menu.inc.php');
?>

<?php
foreach ($page['charts'] as $chart) {
  $date = date ("Y-m-d H:i:s", filemtime($chart));
?>
    <div class="chart">
      <img src="<?php echo $chart ?>">
      <br>Chart file modified on <?php echo $date ?>
    </div>
<?php
}
?>
<?php
include('footer.inc.php');
?>
  </body>
</html>

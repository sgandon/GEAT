<?php
require('init.inc.php');

$template = array();

// +-----------------------------------------------------------------------+
// |                           URL parameters                              |
// +-----------------------------------------------------------------------+

if (isset($_GET['id'])) {
  $page['id'] = intval($_GET['id']);
}
else {
  die('id is required');
}

// +-----------------------------------------------------------------------+
// |                         General information                           |
// +-----------------------------------------------------------------------+

$query = '
SELECT
    e.id,
    e.branch,
    e.revision,
    e.launch_date,
    e.status,
    e.log_files,
    e.duration
  FROM executions AS e
  WHERE id = '.$page['id'].'
;';
$result = pwg_query($query);
$row = mysql_fetch_assoc($result);

$template = array_merge(
  $template,
  $row
  );

$page['status'] = $row['status'];

$status = $row['status'];
if (!isset($conf['executions_status'][$status])) {
  $status = 'default';
}

$template['color'] = $conf['executions_status'][$status]['color'];
$template['bg_color'] = $conf['executions_status'][$status]['bg_color'];

// +-----------------------------------------------------------------------+
// |                              log files                                |
// +-----------------------------------------------------------------------+

// searching for all *.log files
$log_files = array();
if ($handle = opendir($row['log_files']))
{
  while (false !== ($file = readdir($handle)))
  {
    if (preg_match('/\.log$/', $file))
    {
      $conf_log_file = 'default';
      if (isset($conf['log_files'][$file])) {
        $conf_log_file = $file;
      }
      
      $log_file = $conf['log_files'][$conf_log_file];
      $log_file['filename'] = $file;
      
      array_push($log_files, $log_file);
    }
  }
}

foreach ($log_files as $id => $log_file) {
  $filepath = $row['log_files'].'/'.$log_file['filename'];

  if (!file_exists($filepath) and file_exists($filepath.'.zip')) {
    $zip = new ZipArchive;
    if ($zip->open($filepath.'.zip') === true) {
      $zip->extractTo($row['log_files']);
      $zip->close();
    }
    else {
      $message = $filepath.'.zip extraction failed';
      echo '<strong>', $message, '<strong><br />';
    }
  }

  if (!isset($log_file['h3'])) {
    $log_files[$id]['h3'] = $log_file['filename'];
  }

  $techname = preg_replace('/\.log$/', '', $log_file['filename']);
  if (isset($log_file['techname'])) {
    $techname = $log_file['techname'];
  }
  else {
    $log_files[$id]['techname'] = $techname;
  }
    
  // how many lines to display?
  $lines_to_display = 0;
  if (isset($_GET[$techname.'_last_lines'])) {
    if ($_GET[$techname.'_last_lines'] == 'all') {
      $lines_to_display = 'all';
    }
    else {
      $lines_to_display = abs(intval($_GET[$techname.'_last_lines']));
    }
  }
  else if ($page['status'] == $log_file['status']
           or !isset($log_file['status'])) {
    $lines_to_display = $log_file['last_lines'];
  }

  // file content, line by line
  $log_files[$id]['total_lines'] = 0;
  
  if (file_exists($filepath)) {
    $log_files[$id]['filepath'] = $filepath;
    $log_files[$id]['total_lines'] = countLines($filepath);

    if ($lines_to_display === 'all'
        or $lines_to_display > $log_files[$id]['total_lines']) {
      $lines_to_display = $log_files[$id]['total_lines'];
    }

    $log_files[$id]['displayed_lines'] = $lines_to_display;
  }
  else {
    $log_files[$id]['displayed_lines'] = 0;
  }

  // view options
  $log_files[$id]['all_url'] = sprintf(
    'execution_details.php%s&amp;'.$techname.'_last_lines=all',
    get_query_string_diff(
      array($techname.'_last_lines')
      )
    );

  // if we already display some lines, we propose to display more lines, if
  // possible
  $nb_last =
    $log_files[$id]['displayed_lines']
    + $log_file['step_lines']
    ;

  // in case we are already displaying all lines, we propose to display the
  // default number of lines
  if ($log_files[$id]['displayed_lines'] == $log_files[$id]['total_lines']) {
    $nb_last = $log_file['last_lines'];
  }

  if ($nb_last < $log_files[$id]['total_lines']) {
    $log_files[$id]['N_last_url'] = sprintf(
      'execution_details.php%s&amp;'.$techname.'_last_lines=%u',
      get_query_string_diff(array($techname.'_last_lines')),
      $nb_last
      );

    $log_files[$id]['nb_last_to_display'] = $nb_last;
  }
}

// print_array($log_files);

// +-----------------------------------------------------------------------+
// |                                HTML                                   |
// +-----------------------------------------------------------------------+

include('header.inc.php');
?>

  <body id="ed">
    <h1>Execution details</h1>

<?php
include('top_menu.inc.php');
?>

    <h2>Summary</h2>

    <table class="jed_header">
      <tr>
        <th>category / revision</th>
        <td><?php echo $template['branch'].' / '.$template['revision'] ?></td>
      </tr>
      <tr>
        <th>status</th>
<?php
echo sprintf(
  '        <td style="background-color: %s; color: %s;">%s</td>
',
  $template['bg_color'],
  $template['color'],
  $template['status']
  );
?>
      </tr>
    </table>

    <h2>Execution log files</h2>
<?php
foreach ($log_files as $id => $log_file) {
?>
    <h3><?php echo $log_file['h3'] ?></h3>
<?php
  if ($log_file['total_lines'] > 0) {
?>
    <p class="log_switchers">
<?php
    echo sprintf(
      '%u lines (%u displayed) : ',
      $log_file['total_lines'],
      $log_file['displayed_lines']
      );

    if (isset($log_file['N_last_url'])) {
      echo sprintf(
        '<a href="%s"> View %u last lines</a> | ',
        $log_file['N_last_url'],
        $log_file['nb_last_to_display']
        );
    }
    
    echo sprintf(
      '<a href="%s">View all</a>',
      $log_file['all_url']
      );
?>
    </p>
<?php
    if ($log_file['displayed_lines'] > 0) {
      $display_from = $log_file['total_lines'] - $log_file['displayed_lines'];
?>
    <pre class="log_content">
<?php
      $line_number_size = strlen($log_file['total_lines']);

      $handle = fopen($log_file['filepath'], 'r');
      $line_number = 0;
      while ($line = fgets($handle)) {
        $line_number++;
        if ($line_number >= $display_from) {
          echo sprintf(
            '[%'.$line_number_size.'u] %s',
            $line_number,
            $line
            );
        }
      }
      fclose($handle);
?></pre>

<?php
    }
  }
  else {
?>
    <p class="noLog">No log available</p>
<?php 
  }
}
?>
  </body>
</html>

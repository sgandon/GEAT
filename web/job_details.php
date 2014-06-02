<?php
require('init.inc.php');

$page['id_job'] = intval($_GET['id_job']);
$page['id_exec'] = intval($_GET['id_exec']);

if ($conf['enable_job_validation'] and isset($_GET['validate'])) {
  $query = '
UPDATE jobs j
  SET j.id_validation = (
    SELECT
        vs.id
      FROM validation_status vs
      WHERE vs.name = "Tech. ok"
  )
  WHERE j.id = '.$page['id_job'].'
;';
  pwg_query($query);

  $query = '
DELETE
  FROM results_def
  WHERE id_job = '.$page['id_job'].'
;';
  pwg_query($query);

  $query = '
DELETE
  FROM components_jobs
  WHERE id_job = '.$page['id_job'].'
;';
  pwg_query($query);

  echo '<div style="font-size:40px;font-family:sans-serif;text-align:center;width:400px;margin:0 auto;">TUJ validated, <a href="jobs.php?mode=draft">return to draft jobs</a></div>';
  exit();
}

$template = array();

$query = '
SELECT
    j.name AS job_name,
    j.language,
    j.id_validation,
    e.branch,
    e.revision,
    CONCAT(
      e.log_files,
      \'/\',
      j.language,
      \'/\',
      j.name
    ) AS result_destination_dir,
    r.origin,
    r.moment,
    r.description,
    r.id_status
  FROM results_def AS r
    INNER JOIN executions AS e ON e.id = r.id_exec
    INNER JOIN jobs AS j ON j.id = r.id_job
  WHERE id_job = '.$page['id_job'].'
    AND id_exec = '.$page['id_exec'].'
;';
$result = pwg_query($query);
$is_first = true;

$origins = array();
$origin_status = array();

while ($row = mysql_fetch_assoc($result)) {
  if ($is_first) {
    $is_first = false;

    $template['language'] = $row['language'];
    $template['job'] = $row['job_name'];
    $template['branch'] = get_branch_name_of($row['branch']);
    $template['revision'] = $row['revision'];

    // check if colibri is running on dali and not talendforge.org
    if ($conf['enable_job_validation']) {
      if (0 == $row['id_validation']) {
        $template['validate'] = true;
      }
    }
    
    $page['result_destination_dir'] = $row['result_destination_dir'];
  }

  array_push(
    $origin_status,
    $row['id_status']
    );
  
  array_push(
    $origins,
    array(
      'id' => $row['origin'],
      'moment' => $row['moment'],
      'description' => $row['description'],
      'status'   => $cache['result_status'][ $row['id_status'] ]['label'],
      'bg_color' => $cache['result_status'][ $row['id_status'] ]['bg_color'],
      'color'    => $cache['result_status'][ $row['id_status'] ]['color'],
      )
    );
}

// what is the job status, once computing all origins
$job_status = get_computed_status($origin_status);

$template = array_merge(
  $template,
  array(
    'status'   => $cache['result_status'][$job_status]['label'],
    'color'    => $cache['result_status'][$job_status]['color'],
    'bg_color' => $cache['result_status'][$job_status]['bg_color'],
    )
  );

$has_data = false;

if (!empty($page['result_destination_dir'])
    and file_exists($page['result_destination_dir'])) {
  $has_data = true;
  // log execution files
  $stdout = $page['result_destination_dir'].'/log/'.$template['job'].'_stdout';
  $stderr = $page['result_destination_dir'].'/log/'.$template['job'].'_stderr';
  $exit = $page['result_destination_dir'].'/log/'.$template['job'].'_exit';
  $exception = $page['result_destination_dir'].'/log/exception';
  $execlog = $page['result_destination_dir'].'/execLog';
  
  // data execution files
  $data_files = array();
  $data_dir = $page['result_destination_dir'].'/data';
  $data_files = find_files($data_dir, '//');
}

// print_array($data_files);

// job execution history
$query = '
SELECT
    branch,
    revision,
    id_status
  FROM results_def AS r
    INNER JOIN executions AS e ON e.id = r.id_exec
  WHERE id_job = '.$page['id_job'].'
;';
$result = pwg_query($query);
$branches = array();
while ($row = mysql_fetch_assoc($result)) {
  if (!isset($branches[ $row['branch'] ])) {
    $branches[ $row['branch'] ] = array();
  }

  if (!isset($branches[ $row['branch'] ][ $row['revision'] ])) {
    $branches[ $row['branch'] ][ $row['revision'] ] = array();
    $branches[ $row['branch'] ][ $row['revision'] ]['status_list'] = array();
  }

  array_push(
    $branches[ $row['branch'] ][ $row['revision'] ]['status_list'],
    $row['id_status']
    );
}

// print_array($branches);

$branch_bounds = array();
foreach ($branches as $branch_id => $branch) {
  $revisions = array_keys($branch);
  $branch_bounds[$branch_id] = array(
    'first' => min($revisions),
    'last' => max($revisions),
    );
}

// compute a status foreach branch/rev
$status_counts = array();
$total_count = 0;
foreach ($branches as $branch_id => $branch) {
  foreach ($branch as $revision_id => $revision) {
    $status = get_computed_status($revision['status_list']);

    $status_counts[$status]++;
    $total_count++;
  }
}
// print_array($status_counts);

// prepare url for charts made by Google
$ok_count = 0;
$failure_count = 0;
$error_count = 0;

foreach ($status_counts as $status_id => $count) {
  $status = $cache['result_status'][$status_id]['label'];

  if (strpos($status, 'Success') === 0) {
    $ok_count+= $count;
  }
  else if (strpos($status, 'Failure') === 0) {
    $failure_count+= $count;
  }
  else {
    $error_count+= $count;
  }
}

$ok_percent = 100 * $ok_count / $total_count;
$failure_percent = 100 * $failure_count / $total_count;
$error_percent = 100 * $error_count / $total_count;

$chart_url = 'http://chart.apis.google.com/chart';
$chart_url.= '?cht=p3';
$chart_url.= sprintf(
  '&chd=t:%s',
  implode(
    ',',
    array(
      $ok_percent,
      $failure_percent,
      $error_percent
      )
    )
  );
$chart_url.= '&chs=450x200';
$chart_url.= '&chl=Ok|Failure|Error';
$chart_url.= sprintf(
  '&chco=%s',
  implode(
    ',',
    array(
      str_replace('#', '', $ok_status['background_color']),
      str_replace('#', '', $failure_status['background_color']),
      str_replace('#', '', $ko_status['background_color'])
      )
    )
  );
  
include('header.inc.php');
?>

  <body id="jed">

<?php
include('top_menu.inc.php');
?>

<?php
if (isset($template['validate'])) {
?>
<div style="text-align:right; display:block"><a href="job_details.php?id_job=<?php echo $page['id_job'] ?>&amp;validate=1">Validate this job</a></div>
<?php
}
?>

    <h1>Job execution details</h1>

    <h2>Summary</h2>

    <table class="jed_header">
      <tr>
        <th>language</th>
        <td><?php echo $template['language'] ?></td>
      </tr>
      <tr>
        <th>job</th>
        <td><?php echo $template['job'] ?></td>
      </tr>
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

    <h2>Origin details</h2>

    <table class="jed_origins">
      <tr>
        <th width="1%">origin</th>
        <th width="1%">moment</th>
        <th width="1%">status</th>
        <th>description</th>
      </tr>
<?php
foreach ($origins as $origin) {
  echo sprintf(
    '
      <tr style="background-color: %s; color: %s;">
        <td>%s</td>
        <td class="jed_origins_moment">%s</td>
        <td class="jed_origins_status">%s</td>
        <td>%s</td>
      </tr>',
    $origin['bg_color'],
    $origin['color'],
    $origin['id'],
    $origin['moment'],
    $origin['status'],
    $origin['description']
    );
?>
<?php
}
?>
    </table>
<?php
if ($job_status != 1) {
?>
    <h2>Execution log files</h2>

    <h3>execLog</h3>
<?php
  if ($has_data and file_exists($execlog) and filesize($execlog) > 0) {
?>
    <pre class="stdout"><?php readfile($execlog); ?></pre>
<?php
  }
  else {
?>
    <p>No execLog was produced.</p> 
<?php
  }
?>

       <h3>STDOUT</h3>
<?php
  if ($has_data and file_exists($stdout) and filesize($stdout) > 0) {
?>
    <pre class="stdout"><?php readfile($stdout); ?></pre>
<?php
  }
  else {
?>
    <p>No STDOUT was produced.</p> 
<?php
  }
?>
    
    <h3>STDERR</h3>
<?php
  if ($has_data and file_exists($stderr) and filesize($stderr) > 0) {
?>
    <pre class="stderr"><?php readfile($stderr); ?></pre>
<?php
  }
  else {
?>
    <p>No STDERR was produced.</p> 
<?php
  }
?>

    <h3>EXIT</h3>
<?php
  if ($has_data and file_exists($exit) and filesize($exit) > 0) {
?>
    <pre class="stderr"><?php readfile($exit); ?></pre>
<?php
  }
  else {
?>
    <p>No exit file was found.</p> 
<?php
  }
?>

       <h3>Exception</h3>
<?php
  if ($has_data and file_exists($exception) and filesize($exception) > 0) {
?>
    <pre class="stderr"><?php readfile($exception); ?></pre>
<?php
  }
  else {
?>
    <p>No exception was produced.</p>
<?php
  }
?>
<?php
  if ($has_data and count($data_files) > 0) {
?>
    <h2>Execution data files</h2>
<?php
    foreach ($data_files as $data_file) {
        $content = htmlspecialchars(
          file_get_contents(
            $data_file
            )
          );
?>
    <h3><?php echo basename($data_file) ?></h3> 
    <pre class="stdout"><?php echo $content; ?></pre> 
<?php
    }
  }
}
?>

    <h2>History</h2>
    <h3>By category</h3>
    <table class="jed_header">
      <tr>
       <th></th>
       <th>First revision</th>
       <th>Last revision</th>
       <th>Total executions</th>
      </tr>
<?php
foreach ($branch_bounds as $branch_id => $branch_info) {
  echo sprintf(
        '
      <tr>
        <th>%s</th>
        <td>r%u</td>
        <td>r%u</td>
        <td>%u</td>
      </tr>',
      get_branch_name_of($branch_id),
      $branch_info['first'],
      $branch_info['last'],
      count(array_keys($branches[$branch_id]))
    );
?>
<?php
}
?>
    </table>

    <h3>By status</h3>
    <table class="jed_header">
      <tr>
       <th></th>
       <th>Occurences</th>
       <th>%</th>
      </tr>
<?php
foreach ($status_counts as $status_id => $count) {
  echo sprintf(
        '
      <tr>
        <th>%s</th>
        <td>%u</td>
        <td>%u%%</td>
      </tr>',
      $cache['result_status'][$status_id]['label'],
      $count,
        ($count/$total_count)*100
    );
?>
<?php
}
?>
    </table>
    <img style="margin:0 auto; display: block" src="<?php echo $chart_url?>">
  </body>
</html>

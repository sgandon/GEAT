<?php
require('init.inc.php');

// +-----------------------------------------------------------------------+
// |                           URL parameters                              |
// +-----------------------------------------------------------------------+

$page['page'] = 1;
if (isset($_GET['page'])) {
  $page['page'] = abs(intval($_GET['page']));

  if ($page['page'] == 0) {
    $page['page'] = 1;
  }
}

// +-----------------------------------------------------------------------+
// |                           pagination bar                              |
// +-----------------------------------------------------------------------+

$nb_executions = get_nb_executions();

$page['pagination_bar'] = create_pagination_bar(
  'executions.php',
  get_nb_pages(
    $nb_executions,
    $conf['executions_per_page']
    ),
  $page['page'],
  'page'
  );

// +-----------------------------------------------------------------------+
// |                            Execution list                             |
// +-----------------------------------------------------------------------+

$first = ($page['page'] - 1) * $conf['executions_per_page'];

$query = '
SELECT
    id,
    branch,
    revision,
    launch_date,
    end_date,
    status,
    duration,
    nb_jobs
  FROM executions AS e
  WHERE branch = \''.$_SESSION['branch'].'\'
  ORDER BY
    revision DESC
  LIMIT '.($conf['executions_per_page']*3).' OFFSET '.$first.'
;';
$exec_list = array();
$exec_ids = array();
$result = pwg_query($query);
while ($row = mysql_fetch_assoc($result)) {
  $status = $row['status'];
  if (!isset($conf['executions_status'][$status])) {
    $status = 'default';
  }
  
  $row['status_color'] = $conf['executions_status'][$status]['color'];
  $row['status_bgcolor'] = $conf['executions_status'][$status]['bg_color'];
  $row['branch'] = get_branch_name_of($row['branch']);

  $row['raw_revision'] = $row['revision'];
  
  if (empty($row['revision'])) {
    $row['revision'] = '<span style="font-style:italic;">unknown</span>';
  }
  else {
    $label = 'r'.$row['revision'];
    
    if (isset($conf['changeset_url'])) {
      $label = sprintf(
        '<a href="'.$conf['changeset_url'].'">%s</a>',
        $row['revision'],
        $label
        );
    }
    
    $row['revision'] = $label;
  }
  
  array_push(
    $exec_list,
    $row
    );

  array_push(
    $exec_ids,
    $row['id']
    );
}

$exec_stats = array();
if (count($exec_ids) > 0) {
  $query = '
SELECT
    id_exec,
    language,
    nb_jobs,
    success_percent,
    success_nb,
    failure_percent,
    failure_nb,
    error_percent,
    error_nb
  FROM exec_stats
  WHERE id_exec IN ('.implode(',', $exec_ids).')
;';
  $result = pwg_query($query);
  while ($row = mysql_fetch_assoc($result)) {
    $exec_stats[ $row['id_exec'] ][ $row['language'] ] = $row;
  }
}

// +-----------------------------------------------------------------------+
// |                     Last execution informations                       |
// +-----------------------------------------------------------------------+

$last_exec_id = null;
$nb_jobs_in_language = null;

$branch_name_tokens = explode('/', $_SESSION['branch']);
$branch_name = $branch_name_tokens[count($branch_name_tokens) - 1];
$branch_name = str_replace('-', '_', $branch_name);

$query = "show tables like 'results_temp_".$branch_name."'";
$result = pwg_query($query);
$table_exists = false;
while ($row = mysql_fetch_row($result)) {
  $table_exists = true;
}

if ($table_exists) {
  $query = '
SELECT
    language,
    count(distinct job) AS counter
  FROM `results_temp_'.$branch_name.'`
  group by language
;';
  $nb_jobs_in_language = simple_hash_from_query(
    $query,
    'language',
    'counter'
    );
  $language_list = get_languages();
  foreach ($language_list as $language) {
    if (!isset($nb_jobs_in_language[$language])) {
      $nb_jobs_in_language[$language] = 0;
    }
  }

  $query = '
SELECT
    MAX(id)
  FROM executions AS e
  WHERE branch = \''.$_SESSION['branch'].'\'
;';
  list($last_exec_id) = mysql_fetch_row(pwg_query($query));
}

// +-----------------------------------------------------------------------+
// |                                HTML                                   |
// +-----------------------------------------------------------------------+

include('header.inc.php');
?>

  <body id="exec">
<?php
include('top_menu.inc.php');
?>
    <div class="pages">
      <div class="paginationBar"><?php echo $page['pagination_bar']?></div>
    </div>

    <table border="0" cellspacing="0" cellpadding="0">
      <tr class="row-12">
        <th rowspan="2"></th>
        <th rowspan="2">category</th>
        <th rowspan="2">revision</th>
        <th rowspan="2">launch date</th>
        <th rowspan="2">duration</th>
        <th rowspan="2">nb jobs</th>
        <th rowspan="2">status</th>
        <th colspan="5">language stats</th>
      </tr>
      <tr class="row-12">
        <th class="subtitle">Language</th>
        <th class="subtitle">Success</th>
        <th class="subtitle">Failure</th>
        <th class="subtitle">Error</th>
        <th class="subtitle">Total</th>
      </tr>
<?php
$line_number = 1;
foreach ($exec_list as $exec_num => $exec) {
  if ($line_number > $conf['executions_per_page']) {
    break;
  }
  
  if ($line_number % 2 == 0) {
    $bg_color = $exec['status_bgcolor'];
  }
  else {
    $bg_color = darkenColour($exec['status_bgcolor']);
  }
  
  $duration = '';

  if (!empty($exec['duration'])) {
    $duration = sprintf(
      '%.1f',
      $exec['duration'] / 60
      );
  }

  $stats = array();
  $rowspan = 1;
  if (isset($exec_stats[ $exec['id'] ])) {
    $rowspan = count($exec_stats[ $exec['id'] ]);
  }

  $details_link = sprintf(
    '<a href="%s"><img src="icons/details.png" /></a>',
    'execution_details.php?id='.$exec['id']
    );

  $status = $exec['status'];
  if ($exec['id'] == $last_exec_id and $exec['status'] == 'Build ok') {
    $language_strings = array();
    foreach ($language_list as $language) {
      array_push(
        $language_strings,
        sprintf(
          '[%s: %u]',
          $language,
          $nb_jobs_in_language[$language]
          )
        );
    }
    // print_array($language_strings);
    $status.= ' '.implode('', $language_strings);
  }
  
  echo sprintf(
    '
      <tr style="color:%s;background-color:%s">
        <td style="width:1%%;" rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>
        <td rowspan="'.$rowspan.'">%s</td>',
    $exec['status_color'],
    $bg_color,
    $details_link,
    $exec['branch'],
    $exec['revision'],
    $exec['launch_date'],
    $duration,
    $exec['nb_jobs'],
    $status
    );

  if (isset($exec_stats[ $exec['id'] ])) {
    $is_first = true;

    // what is the previous execution that can be compared to the current
    // one?
    //
    // it must be the nearest execution in the past that has some stats
    // (if an execution fails, there are no stats)
    $prev_exec_num = $exec_num;
    $prev_exec_id = null;
    while (!isset($prev_exec_id)) {
      $prev_exec_num++;
      if (isset($exec_ids[$prev_exec_num])) {
        if (isset($exec_stats[ $exec_ids[$prev_exec_num] ])) {
          $prev_exec_id = $exec_ids[$prev_exec_num];
        }
        else {
          // there is no stat related to this execution, we go to previous
          // one.
          continue;
        }
      }
      else {
        // we are at the end of the array, no other solution than going
        // away
        break;
      }
    }
    
    $languages = array('java', 'perl');
    foreach ($languages as $language) {
      if (isset($exec_stats[ $exec['id'] ][$language])) {
        $language_stats = $exec_stats[ $exec['id'] ][$language];
      }
      else {
        continue;
      }
      if (!$is_first) {
        echo sprintf(
          '
      <tr style="color:%s;background-color:%s">',
          $exec['status_color'],
          $bg_color
          );
      }
      
      $bell_on = array();
      if (isset($prev_exec_id)) {
        foreach (array('failure', 'error') as $status) {
          $prev_value = $exec_stats[$prev_exec_id][$language][$status.'_nb'];
          $cur_value = $language_stats[$status.'_nb'];

          if ($cur_value > $prev_value) {
            $bell_on[$status] = sprintf(
              '<img src="icons/worse.png" title="%u more compared to r%u">',
              ($cur_value - $prev_value),
              $exec_list[$prev_exec_num]['raw_revision']
              );
          }
          else if ($cur_value < $prev_value) {
            $bell_on[$status] = sprintf(
              '<img src="icons/better.png" title="%u less compared to r%u">',
              ($prev_value - $cur_value),
              $exec_list[$prev_exec_num]['raw_revision']
              );
          }
        }
      }

      echo sprintf(
      '
          <td>%s</td>
          <td style="text-align:right;">%.1f%%(%u)</td>
          <td style="text-align:right;">%s%.1f%%(%u)</td>
          <td style="text-align:right;">%s%.1f%%(%u)</td>
          <td style="text-align:right;">%u</td>',
      $language_stats['language'],
      $language_stats['success_percent']*100,
      $language_stats['success_nb'],
      $bell_on['failure'],
      $language_stats['failure_percent']*100,
      $language_stats['failure_nb'],
      $bell_on['error'],
      $language_stats['error_percent']*100,
      $language_stats['error_nb'],
      $language_stats['nb_jobs']
      );


      echo '
      </tr>';
      
      $is_first = false;        
    }
  }
  else {
    echo '
        <td colspan="5"></td>
      </tr>';
  }

  echo "\n";

  $line_number++;
}
?>
    </table>
    
    <div class="pages">
      <div class="paginationBar"><?php echo $page['pagination_bar']?></div>
    </div>
<?php
include('footer.inc.php');
?>
  </body>
</html>

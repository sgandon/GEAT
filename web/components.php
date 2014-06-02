<?php
require('init.inc.php');

function cmpComponentName($a, $b) {
  global $name_of_component;

  return strcmp($name_of_component[$a], $name_of_component[$b]);
}

$filter = array(
  'languages' => array('any'),
  'last_executions' => $conf['default_last_executions'],
  'minimum_error_rate' => '0.1',
  );

// +-----------------------------------------------------------------------+
// |                        page input parameters                          |
// +-----------------------------------------------------------------------+

if (isset($_POST['filter'])) {
  $_SESSION['languages'] = $_POST['languages'];
}

foreach (array_keys($filter) as $filter_key) {
  if (isset($_POST[$filter_key])) {
    $filter[$filter_key] = $_POST[$filter_key];
  }

  if ($filter_key == 'minimum_error_rate') {
    $filter[$filter_key] = str_replace('%', '', $filter[$filter_key]);
  }
}

// +-----------------------------------------------------------------------+
// |                           executions list                             |
// +-----------------------------------------------------------------------+

$exec_ids = get_last_execution_ids($filter['last_executions']);
$exec_infos_of = array();

if (count($exec_ids) > 0) {
  $query = '
SELECT
    branch,
    revision,
    id
  FROM executions
  WHERE id IN ('.implode(',', $exec_ids).')
;';
  $result = pwg_query($query);
  while ($row = mysql_fetch_assoc($result)) {
    $exec_infos_of[ $row['id'] ] = array(
      'id' => $row['id'],
      'branch' => $row['branch'],
      'branch_name' => get_branch_name_of($row['branch']),
      'revision' => $row['revision'],
      'id_exec' => $row['id'],
      );
  }
}

// +-----------------------------------------------------------------------+
// |                              job status                               |
// +-----------------------------------------------------------------------+

$languages = array_merge(
  array('any'),
  get_languages()
  );

$branch_job_ids = get_branch_job_ids($_SESSION['branch']);

$clauses = array();

if (!in_array('any', $filter['languages'])) {
  $language_clauses = array();

  foreach ($filter['languages'] as $language) {
    array_push(
      $language_clauses,
      "language = '".$language."'"
      );
  }

  array_push(
    $clauses,
    implode(' OR ', $language_clauses)
    );
}

if (count($branch_job_ids) > 0) {
  array_push(
    $clauses,
    'id IN ('.implode(',', $branch_job_ids).')'
    );
}

if (count($clauses) > 0) {
  $where_string = '  WHERE ';
  $where_string.= implode(
    '
    AND ',
    prepend_append_array_items(
      $clauses,
      '(',
      ')'
      )
    );
}
else {
  $where_string = null;
}

$job_ids = array();
$job_infos_of = array();

$query = '
SELECT
    id,
    name,
    language,
    id_criticality
  FROM jobs
  '.$where_string.'
  ORDER BY
    language ASC,
    name ASC
;';
$result = pwg_query($query);
while ($row = mysql_fetch_assoc($result)) {
  array_push($job_ids, $row['id']);

  $job_infos_of[ $row['id'] ] = array(
    'name' => $row['name'],
    'language' => $row['language'],
    'id_criticality' => $row['id_criticality'],
    );
}

$status_of_job = array();

if (count($exec_ids) > 0 and count($job_ids) > 0) {
  $query = '
SELECT
    id_status,
    id_exec,
    id_job
  FROM results_def
  WHERE id_exec IN ('.implode(',', $exec_ids).')
    AND id_job  IN ('.implode(',', $job_ids).')
;';
  $status_list_of = array();

  $result = pwg_query($query);
  while ($row = mysql_fetch_assoc($result)) {
    $status_id = $row['id_status'];
    $job_id = $row['id_job'];
    $exec_id = $row['id_exec'];
    
    if (!isset($status_list_of[$job_id][$exec_id])) {
      $status_list_of[$job_id][$exec_id] = array();
    }

    array_push(
      $status_list_of[$job_id][$exec_id],
      $status_id
      );
  }

  foreach (array_keys($status_list_of) as $job_id) {
    foreach (array_keys($status_list_of[$job_id]) as $exec_id) {
      $status_of_job[$job_id][$exec_id] = get_computed_status(
        $status_list_of[$job_id][$exec_id]
        );
    }
  }

  // print_array($status_of_job);
}

// +-----------------------------------------------------------------------+
// |                           component list                              |
// +-----------------------------------------------------------------------+

$component_exec = array();
$component_ids_to_show = array();

if (count($exec_ids) > 0 and count($job_ids) > 0) {
  $query = '
SELECT
    id_component,
    id_exec,
    id_job
  FROM components_jobs
  WHERE id_exec IN ('.implode(',', $exec_ids).')
    AND id_job  IN ('.implode(',', $job_ids).')
;';
  $result = pwg_query($query);
  while ($row = mysql_fetch_assoc($result)) {
    $component_id = $row['id_component'];
    $job_id = $row['id_job'];
    $exec_id = $row['id_exec'];

    if (!isset($component_exec[$component_id][$exec_id])) {
      $component_exec[$component_id][$exec_id] = array();
    }

    $component_exec[$component_id][$exec_id]['total']++;
    
    if ($status_of_job[$job_id][$exec_id] != 1) {
      $component_exec[$component_id][$exec_id]['error']++;
    }
  }
}

foreach ($component_exec as $component_id => $executions) {
  $show_component = false;
  
  foreach ($executions as $exec_id => $exec) {
    if ($filter['smart_display']) {
      if (isset($exec['error'])) {
        if ($exec['error'] > 0) {
          $show_component = true;
        }
      }
    }

    $error_rate = sprintf(
      '%.1f',
      100 * ($exec['error'] / $exec['total'])
      );
    
    $component_exec[$component_id][$exec_id]['error_rate'] = $error_rate;

    if ($error_rate >= $filter['minimum_error_rate']) {
      $show_component = true;
    }
  }

  if ($show_component) {
    array_push($component_ids_to_show, $component_id);
  }
}

// print_array($component_exec);

$name_of_component = array();
$query = '
SELECT
    id,
    name
  FROM components
;';
$result = pwg_query($query);
while ($row = mysql_fetch_assoc($result)) {
  $name_of_component[ $row['id'] ] = $row['name'];
}

// print_array($name_of_component);

usort($component_ids_to_show, 'cmpComponentName');

// +-----------------------------------------------------------------------+
// |                                 HTML                                  |
// +-----------------------------------------------------------------------+

include('header.inc.php');
?>

  <body>
<?php
include('top_menu.inc.php');
?>

    <form action="components.php" method="post">
      <div class="filter">
        <table border="0" cellspacing="0" cellpadding="0">
          <tr class="row-12">
            <td>Language</td>
            <td>Last executions</td>
            <td>Minimum error rate</td>
          </tr>
          <tr class="row-11">
            <td><!-- LANGUAGE -->
              <select name="languages[]" size="3" multiple="multiple">
<?php
foreach ($languages as $language) {
  $selected = '';
  
  if (in_array($language, $filter['languages'])) {
    $selected = 'selected="selected"';
  }

  echo sprintf(
    '
                <option value="%s" %s>%s</option>',
    $language,
    $selected,
    $language
    );
}
?>
              </select>
	    </td>

            <td><!-- Last executions -->
              <input
                name="last_executions"
                type="text"
                value="<?php echo $filter['last_executions'] ?>"
              />
            </td>

            <td><!-- Minimum error rate -->
              <input
                name="minimum_error_rate"
                type="text"
                value="<?php echo $filter['minimum_error_rate'] ?>%"
              />
            </td>
          </tr>
                
	  <tr>
	    <td colspan="1">
              <input name="filter" type="submit" value = "Search"/>
              <input type="reset" value="Reset"/> 
	    </td>
          </tr>
                
        </table>
      </div>
    </form>
    <table id="matrix">
      <tr>
        <th>revision</th>
<?php
foreach ($component_ids_to_show as $component_id) {
  $name = $name_of_component[$component_id];
  if (strlen($name) > 5) {
    $shortname = 't'.preg_replace('/[a-z]/', '', $name);
  }
  else {
    $shortname = $name;
  }

  echo "\n        ";
  echo sprintf(
    '<th><a href="%s">%s</a></th>',
    'jobs.php?component='.$component_id,
    $name
    );
}
?>
      </tr>
<?php
$odd_line = true;
foreach ($exec_ids as $exec_id) {
  $exec_infos = $exec_infos_of[$exec_id];

  $line_title = sprintf(
    '%s r%u',
    $exec_infos['branch_name'],
    $exec_infos['revision']
    );
  
  echo '
      <tr>
        <td>'.$line_title.'</td>';
  
  $odd_col = true;
    
  foreach ($component_ids_to_show as $component_id) {
    $col_title = $name_of_component[$component_id];
    
    if (isset($component_exec[$component_id][$exec_id])) {
      $rate = sprintf(
        '%s%% (%u/%u)',
        $component_exec[$component_id][$exec_id]['error_rate'],
        $component_exec[$component_id][$exec_id]['error'],
        $component_exec[$component_id][$exec_id]['total']
        );
    }
    else {
      $rate = 'N/A';
    }

    // what is the background color of the cell?
    $color_index = ($odd_line ? 'o' : 'e').($odd_col ? 'o' : 'e');
    $bgcolor = $conf['components_colors'][$color_index];
    
    echo "\n        ";
    echo sprintf(
      '<td title="%s" class="componentMatrix" style="background-color:%s">%s</td>',
      $line_title.' on '.$col_title,
      $bgcolor,
      $rate
      );

    $odd_col = !$odd_col;
  }
  echo "\n      </tr>";

  $odd_line = !$odd_line;
}
?>
    </table>
<?php
include('footer.inc.php');
?>
  </body>
</html>
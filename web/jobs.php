<?php
require('init.inc.php');

// print_array($_POST);

$filter = array(
  'languages' => array('any'),
  'last_executions' => $conf['default_last_executions'],
  'smart_display' => true,
  'components' => array('any'),
  );

// +-----------------------------------------------------------------------+
// |                        page input parameters                          |
// +-----------------------------------------------------------------------+

if (isset($_POST['filter'])) {
  $_SESSION['languages'] = $_POST['languages'];

  if (!isset($_POST['smart_display'])) {
    $filter['smart_display'] = false;
  }
}

foreach (array_keys($filter) as $filter_key) {
  if (isset($_POST[$filter_key])) {
    $filter[$filter_key] = $_POST[$filter_key];
  }
}

if (isset($_GET['component']) and is_numeric($_GET['component'])) {
  $filter['components'] = array(intval($_GET['component']));
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
// |                               job list                                |
// +-----------------------------------------------------------------------+

$languages = array_merge(
  array('any'),
  get_languages()
  );

$branch_job_ids = get_branch_job_ids($_SESSION['branch']);

$clauses = array();

if (!isset($_GET['mode'])) {
  $id_validation = 1;
}
else {
  if ($_GET['mode'] == 'draft') {
    $id_validation = 0;
  }
  if ($_GET['mode'] == 'all') {
    $id_validation = null;
  }
}

if (isset($id_validation)) {
  array_push(
    $clauses,
    'id_validation = '.$id_validation
    );
}

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

if (!in_array('any', $filter['components'])) {
  $query = '
SELECT
    id_job
  FROM components_jobs
  WHERE id_component IN ('.implode(',', $filter['components']).')
;';
  $component_job_ids = array_from_query($query, 'id_job');

  if (count($component_job_ids) > 0) {
    array_push(
      $clauses,
      'id IN ('.implode(',', $component_job_ids).')'
      );
  }
  else {
    array_push(
      $clauses,
      '1=0'
      );
  }
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

// +-----------------------------------------------------------------------+
// |                         status matrix build                           |
// +-----------------------------------------------------------------------+

$all_jobs = array();

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
    
    array_push($all_jobs, $job_id);
  }
}

$all_jobs = array_unique($all_jobs);

// what is the list of job ids to show ?
$jobs_to_show = array();

if ($filter['smart_display']) {
  foreach ($job_ids as $job_id) {
    foreach ($exec_ids as $exec_id) {
      // in the "smart display" mode, we only show jobs having a problem or
      // jobs having a not executed revision
      if (isset($status_list_of[$job_id][$exec_id])) {
        foreach ($status_list_of[$job_id][$exec_id] as $status_id) {
          if (is_problem_status($status_id)) {
            array_push($jobs_to_show, $job_id);
          }
        }
      }
      else {
        array_push($jobs_to_show, $job_id);
      }
    }
  }
}
else {
  $jobs_to_show = $all_jobs;
}

$jobs_to_show = array_unique($jobs_to_show);
// +-----------------------------------------------------------------------+
// |                                 HTML                                  |
// +-----------------------------------------------------------------------+

include('header.inc.php');
?>

  <body>
<?php
include('top_menu.inc.php');
?>

<?php
$action = 'jobs.php';
if (isset($_GET['mode'])) {
  if (in_array($_GET['mode'], array('draft', 'all'))) {
    $action.= '?mode='.$_GET['mode'];
  }
}
?>
    <form action="<?php echo $action ?>" method="post">
      <div class="filter">
        <table border="0" cellspacing="0" cellpadding="0">
          <tr class="row-12">
            <td>Language</td>
            <td>Last executions</td>
            <td>Smart display</td>
            <td>Components</td>
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

            <td><!-- Smart display -->
              <input
                name="smart_display"
                type="checkbox"
<?php
if ($filter['smart_display']) {
  echo '
                checked="checked"';
}
?>                
              />
            </td>

            <td><!-- component -->
              <select name="components[]" size="5" multiple="multiple">
<?php
$selected = '';
  
if (in_array('any', $filter['components'])) {
  $selected = 'selected="selected"';
}

echo sprintf(
  '
              <option value="%s" %s>%s</option>',
  'any',
  $selected,
  'any'
  );

$query = '
SELECT
    id,
    name
  FROM components
  ORDER BY name ASC
;';
$result = pwg_query($query);
while ($row = mysql_fetch_assoc($result)) {
  $selected = '';
  
  if (in_array($row['id'], $filter['components'])) {
    $selected = 'selected="selected"';
  }

  echo sprintf(
    '
                <option value="%s" %s>%s</option>',
    $row['id'],
    $selected,
    $row['name']
    );
}
?>
              </select>
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
        <th>job</th>
<?php
foreach ($exec_ids as $exec_id) {
  echo '<th>';
  if (isset($conf['changeset_url'])) {
    echo sprintf(
      '<a href="'.$conf['changeset_url'].'">%s r%u</a>',
      $exec_infos_of[$exec_id]['revision'],
      $exec_infos_of[$exec_id]['branch_name'],
      $exec_infos_of[$exec_id]['revision']
      );
  }
  else {
    echo $exec_infos_of[$exec_id]['branch_name'].' r'.$exec_infos_of[$exec_id]['revision'];
  }
  echo '</th>';  
}
?>
      </tr>
<?php
foreach ($job_ids as $job_id) {
  if (!in_array($job_id, $jobs_to_show)) {
    continue;
  }

  $job_infos = $job_infos_of[$job_id];

  $icon = 'icons/criticality_'.$job_infos['id_criticality'].'.png';
  if (file_exists($icon)) {
    $icon_html = '<img src="'.$icon.'">';
  }
  else {
    $icon_html = '';
  }

  $job_title = $icon_html.' ';

  $label_html = $job_infos['language'];
  $label_html.= ' / ';
  $label_html.= '<span title="'.$job_infos['name'].'">';
  if (strlen($job_infos['name']) > $conf['job_max_display_length']) {
    $label_html.= substr($job_infos['name'], 0, $conf['job_max_display_length']).'...';
  }
  else {
    $label_html.= $job_infos['name'];
  }
  $label_html.= '</span>';

  if (isset($conf['tuj_log_url'])) {
    $job_title.= sprintf(
      '<a href="%s">%s</a>',
      get_tuj_log_url(
        $_SESSION['branch'],
        $job_infos['language'],
        $job_infos['name']
        ),
      $label_html
      );
  }
  else {
    $job_title.= $label_html;
  }
  
  echo '
      <tr>
        <td>'.$job_title.'</td>';
    foreach ($exec_ids as $exec_id) {
      if (isset($status_list_of[$job_id][$exec_id])) {
        $status = get_computed_status($status_list_of[$job_id][$exec_id]);
        // echo 'before: '; print_r($status_list_of[$job_id][$exec_id]); echo ' after: ', $status, '<br>';

        $color = $cache['result_status'][$status]['color'];
        $bg_color = $cache['result_status'][$status]['bg_color'];
        $short_name = $cache['result_status'][$status]['short_name'];
      }
      else {
        $status = 'undefined';

        $color = $conf['undefined_result_status']['color'];
        $bg_color = $conf['undefined_result_status']['bg_color'];
        $short_name = $conf['undefined_result_status']['short_name'];
      }
    
      echo sprintf(
        '
        <td style="color: %s; background-color: %s">',
        $color,
        $bg_color
        );

      if ($status != 'undefined') {
        $url = 'job_details.php';
        $url.= '?id_job='.$job_id;
        $url.= '&amp;id_exec='.$exec_id;
      
        echo sprintf(
          '<a href="%s" title="see details">?</a> %s',
          $url,
          $short_name
          );
      }

      echo '</td>';
    }
    echo '
      </tr>';
}  
?>
    </table>
<?php
include('footer.inc.php');
?>
  </body>
</html>
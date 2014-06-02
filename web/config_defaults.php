<?php
// database connection parameters
$conf['db_host'] = "localhost";
$conf['db_name'] = "colibri";
$conf['db_user'] = "root";
$conf['db_pass'] = "root";

$conf['show_queries'] = false;

$conf['application_name'] = 'Talend Testing Platform';
$conf['version'] = '1.0.0';

$ok_status = array(
  'color' => '#370',
  'background_color' => '#77ee77',
  );

$ko_status = array(
  'color' => '#800',
  'background_color' => '#ff4444',
  );

$failure_status = array(
  'color' => '#900',
  'background_color' => '#eeee00',
  );

$running_status = array(
  'color' => '#900',
  'background_color' => '#eeee00',
  );

$conf['color_for_main_status'] = array(
  'Success' => array(
    'color' => $ok_status['color'],
    'background_color' => $ok_status['background_color'],
    ),
  'Failure' => array(
    'color' => $failure_status['color'],
    'background_color' => $failure_status['background_color'],
    ),
  'Error' => array(
    'color' => $ko_status['color'],
    'background_color' => $ko_status['background_color'],
    ),
  );

// in revisions.php, when no result available in a given revision for a
// given test
$conf['undefined_result_status'] = array(
  'color' => 'white',
  'bg_color' => 'black',
  'short_name' => '',
  );

$conf['default_result_status'] = array(
  'color' => '#800',
  'bg_color' => '#c0c0c0',
  );

// Here you can ask for a display name that would be different from the
// technical name found in the database. You don't need to declare each
// branch, by default we will display the name "as is".
//
// we just keep an example to illustrate the purpose of this configuration
// parameter
$conf['branch_name_of'] = array(
  'branches/branch-2_2' => '2.2',
  );

$conf['default_results_per_page'] = 20;

$conf['executions_status'] = array(
  'Done' => array(
    'color' => $ok_status['color'],
    'bg_color' => $ok_status['background_color'],
    ),
  'Build ok' => array(
    'color' => $running_status['color'],
    'bg_color' => $running_status['background_color'],
    ),
  'default' => array(
    'color' => $ko_status['color'],
    'bg_color' => $ko_status['background_color'],
    ),
  );

$conf['default_last_executions'] = 10;

$conf['log_files'] = array(
  'exec.log' => array(
    'techname' => 'exec',
    'h3' => 'Execution log',
    'last_lines' => 30,
    'step_lines' => 30,
    ),
  'svnup.log' => array(
    'techname' => 'svnup',
    'status' => 'Svn update failed',
    'h3' => 'Subversion update log',
    'last_lines' => 20,
    'step_lines' => 20,
    ),
  'build.log' => array(
    'techname' => 'build',
    'status' => 'Build failed',
    'h3' => 'Build log',
    'last_lines' => 50,
    'step_lines' => 50,
    ),
  'default' => array(
    'last_lines' => 30,
    'step_lines' => 30,
    ),
  );

$conf['paginate_pages_around'] = 2;
$conf['executions_per_page'] = 30;

$conf['components_colors'] = array(
  'oo' => '#bbf',
  'oe' => '#aaf',
  'eo' => '#bbc',
  'ee' => '#aaa',
  );

$conf['changeset_url'] = null;
$conf['tuj_log_url'] = null;

$conf['job_max_display_length'] = 30;

$conf['enable_job_validation'] = true;

$conf['default_page'] = 'executions.php';

$conf['charts_dir'] = './charts';
?>

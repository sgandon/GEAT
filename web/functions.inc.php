<?php
function get_branch_name_of($technical_name) {
  global $conf;

  if (isset($conf['branch_name_of'][$technical_name])) {
    return $conf['branch_name_of'][$technical_name];
  }
  else {
    return $technical_name;
  }
}

function pwg_query($query)
{
  global $conf,$page,$debug,$t2;

  $start = get_moment();
  $result = mysql_query($query) or my_error($query."\n");

  $time = get_moment() - $start;

  if (!isset($page['count_queries']))
  {
    $page['count_queries'] = 0;
    $page['queries_time'] = 0;
  }

  $page['count_queries']++;
  $page['queries_time']+= $time;

  if ($conf['show_queries'])
  {
    $output = '';
    $output.= '<pre>['.$page['count_queries'].'] ';
    $output.= "\n".$query;
    $output.= "\n".'(this query time : ';
    $output.= '<b>'.number_format($time, 3, '.', ' ').' s)</b>';
    $output.= "\n".'(total SQL time  : ';
    $output.= number_format($page['queries_time'], 3, '.', ' ').' s)';
    $output.= "</pre>\n";

    $debug .= $output;

    echo $output;
  }

  return $result;
}

// my_error returns (or send to standard output) the message concerning the
// error occured for the last mysql query.
function my_error($header)
{
  global $conf;

  $error = '<pre>';
  $error.= $header;
  $error.= '[mysql error '.mysql_errno().'] ';
  $error.= mysql_error();
  $error.= '</pre>';

  if ($conf['die_on_sql_error'])
  {
    die($error);
  }
  else
  {
    echo $error;
  }
}

// The function get_moment returns a float value coresponding to the number
// of seconds since the unix epoch (1st January 1970) and the microseconds
// are precised : e.g. 1052343429.89276600
function get_moment()
{
  $t1 = explode( ' ', microtime() );
  $t2 = explode( '.', $t1[0] );
  $t2 = $t1[1].'.'.$t2[1];
  return $t2;
}

function get_languages()
{
  global $conf, $cache;

  if (isset($cache['languages'])) {
    return $cache['languages'];
  }
  
  $query = '
SELECT DISTINCT
    language
  FROM jobs
;';

  $cache['languages'] = array_from_query($query, 'language');

  return $cache['languages'];
}

/**
 * creates an array based on a query, this function is a very common pattern
 * used here
 *
 * @param string $query
 * @param string $fieldname
 * @return array
 */
function array_from_query($query, $fieldname)
{
  $array = array();

  $result = pwg_query($query);
  while ($row = mysql_fetch_array($result))
  {
    array_push($array, $row[$fieldname]);
  }

  return $array;
}

function print_array($array)
{
  echo '<pre>';
  print_r($array);
  echo '</pre>';
}

/**
 * Prepends and appends a string at each value of the given array.
 *
 * @param array
 * @param string prefix to each array values
 * @param string suffix to each array values
 */
function prepend_append_array_items($array, $prepend_str, $append_str)
{
  array_walk(
    $array,
    create_function('&$s', '$s = "'.$prepend_str.'".$s."'.$append_str.'";')
    );

  return $array;
}

// the job status is the status of the highest priority among origin of the
// same job (a job can have several "origin" status, ie several tAssert or
// compilation error)
function get_computed_status($status_ids) {
  global $cache;

  if (count($status_ids) == 0) {
    die('[function get_computed_status] at least one input status required');
  }

  if (!isset($cache['result_status'])) {
    load_result_status_cache();
  }
  
  $current_priority = -1;
  $current_status = null;
  
  foreach ($status_ids as $status_id) {
    $comparison_priority = $cache['result_status'][$status_id]['priority'];

    if ($comparison_priority > $current_priority) {
      $current_priority = $comparison_priority;
      $current_status = $status_id;
    }
  }

  return $current_status;
}

function load_result_status_cache() {
  global $cache, $conf;

  $cache['result_status'] = array();
  
  $query = '
SELECT
    id,
    main_status,
    sub_status,
    short_name,
    priority
  FROM status
;';
  $result = pwg_query($query);
  while ($row = mysql_fetch_assoc($result)) {
    $cache['result_status'][ $row['id'] ] = array(
      'id' => $row['id'],
      'label' => $row['main_status'].' / '.$row['sub_status'],
      'priority' => $row['priority'],
      'short_name' => $row['short_name'],
      'color' => $conf['color_for_main_status'][ $row['main_status'] ]['color'],
      'bg_color' => $conf['color_for_main_status'][ $row['main_status'] ]['background_color'],
      );
  }

  // print_array($cache['result_status']);
}

function darkenColour($color, $diff=30){
    $i = hexdec($color);
    $rgb['r'] = 0xFF & ($i >> 0x10);
    $rgb['g'] = 0xFF & ($i >> 0x8);
    $rgb['b'] = 0xFF & $i;
    $hex = "#";
    foreach($rgb as $v) {
        $hex .= ($v <= $diff) ? '00' : dechex($v-$diff);
    } 
    return $hex;
}

// get_extension returns the part of the string after the last "."
function get_extension( $filename )
{
  return substr( strrchr( $filename, '.' ), 1, strlen ( $filename ) );
}

function get_last_execution_ids($nb_last) {
  global $conf;
  
  $query = '
SELECT
    id
  FROM executions
  WHERE status = \'Done\'
    AND branch = \''.$_SESSION['branch'].'\'
  ORDER BY revision DESC
  LIMIT '.$nb_last.'
;';

  return array_from_query($query, 'id');
}

function is_problem_status($status_id) {
  global $cache;

  $label = $cache['result_status'][$status_id]['label'];

  if (strpos($label, 'Success') === 0) {
    return false;
  }
  else {
    return true;
  }
}

/**
 * returns $_SERVER['QUERY_STRING'] whitout keys given in parameters
 *
 * @param array $rejects
 * @param boolean $escape - if true escape & to &amp; (for html)
 * @returns string
 */
function get_query_string_diff($rejects=array(), $escape=true)
{
  $query_string = '';

  $str = $_SERVER['QUERY_STRING'];
  parse_str($str, $vars);

  $is_first = true;
  foreach ($vars as $key => $value)
  {
    if (!in_array($key, $rejects))
    {
      $query_string.= $is_first ? '?' : ($escape ? '&amp;' : '&' );
      $is_first = false;
      $query_string.= $key.'='.$value;
    }
  }

  return $query_string;
}

function create_pagination_bar(
  $base_url, $nb_pages, $current_page, $param_name
  )
{
  global $conf;

  $url =
    $base_url.
    (preg_match('/\?/', $base_url) ? '&amp;' : '?').
    $param_name.'='
    ;

  $pagination_bar = '';

  // current page detection
  if (!isset($current_page)
      or !is_numeric($current_page)
      or $current_page < 0)
  {
    $current_page = 1;
  }

  // navigation bar useful only if more than one page to display !
  if ($nb_pages > 1)
  {
    // link to first page?
    if ($current_page > 1)
    {
      $pagination_bar.=
        "\n".'&nbsp;'
        .'<a href="'.$url.'1" rel="start" class="FirstActive">'
        .'&lt;&lt;first'
        .'</a>'
        ;
    }
    else
    {
      $pagination_bar.=
        "\n".'&nbsp;<span class="FirstInactive">&lt;&lt;first</span>';
    }

    // link on previous page ?
    if ($current_page > 1)
    {
      $previous = $current_page - 1;
      
      $pagination_bar.=
        "\n".'&nbsp;'
        .'<a href="'.$url.$previous.'" rel="prev" class="PrevActive">'
        .'&lt;prev'.'</a>'
        ;
    }
    else
    {
      $pagination_bar.=
        "\n".'&nbsp;<span class="PrevInactive">&lt;prev</span>';
    }

    $min_to_display = $current_page - $conf['paginate_pages_around'];
    $max_to_display = $current_page + $conf['paginate_pages_around'];
    $last_displayed_page = null;

    for ($page_number = 1; $page_number <= $nb_pages; $page_number++)
    {
      if ($page_number == 1
          or $page_number == $nb_pages
          or ($page_number >= $min_to_display
              and $page_number <= $max_to_display)
        )
      {
        if (isset($last_displayed_page)
            and $last_displayed_page != $page_number - 1
          )
        {
          $pagination_bar.=
            "\n".'&nbsp;<span class="inactive">...</span>'
            ;
        }
        
        if ($page_number == $current_page)
        {
          $pagination_bar.=
            "\n".'&nbsp;'
            .'<span class="currentPage">'.$page_number.'</span>'
            ;
        }
        else
        {
          $pagination_bar.=
            "\n".'&nbsp;'
            .'<a href="'.$url.$page_number.'">'.$page_number.'</a>'
            ;
        }
        $last_displayed_page = $page_number;
      }
    }
    
    // link on next page?
    if ($current_page < $nb_pages)
    {
      $next = $current_page + 1;
      
      $pagination_bar.=
        "\n".'&nbsp;'.
        '<a href="'.$url.$next.'" rel="next" class="NextActive">next&gt;</a>'
        ;
    }
    else
    {
      $pagination_bar.=
        "\n".'&nbsp;<span class="NextInactive">next&gt;</span>'
        ;
    }

    // link to last page?
    if ($current_page != $nb_pages)
    {
      $pagination_bar.=
        "\n".'&nbsp;'.
        '<a href="'.$url.$nb_pages.'" rel="last" class="LastActive">'
        .'last&gt;&gt;</a>'
        ;
    }
    else
    {
      $pagination_bar.=
        "\n".'&nbsp;<span class="LastInactive">last&gt;&gt;</span>';
    }
  }
  
  return $pagination_bar;
}

/**
 * Returns the number of pages to display in a pagination bar, given the
 * number of items and the number of items per page.
 *
 * @param int number of items
 * @param int number of items per page
 * @return int
 */
function get_nb_pages($nb_items, $nb_items_per_page) {
  return intval(($nb_items - 1) / $nb_items_per_page) + 1;
}


function getCurrentRunningRevision() {
	$query = "SELECT CONCAT(revision,' on ',branch) from executions where status like 'Running%' or status like '' order by id desc limit 0,1";
	list($revision) = mysql_fetch_row(pwg_query($query));
	return $revision;
}

function get_nb_executions() {
  global $conf;

  $query = '
SELECT
    COUNT(*)
  FROM executions
  WHERE branch = \''.$_SESSION['branch'].'\'
;';
  list($count) = mysql_fetch_row(pwg_query($query));

  return $count;
}

function simple_hash_from_query($query, $keyname, $valuename)
{
  $array = array();

  $result = pwg_query($query);
  while ($row = mysql_fetch_array($result))
  {
    $array[ $row[$keyname] ] = $row[$valuename];
  }

  return $array;
}

function get_branches() {
  global $conf;
  
  $query = '
SELECT
    DISTINCT(branch) AS branch_id
  FROM executions
  ORDER BY branch DESC
;';

  $result = pwg_query($query);

  $branches = array();
  while($row = mysql_fetch_assoc($result)) {
    array_push(
      $branches,
      array(
        'id' => $row['branch_id'],
        'name' => get_branch_name_of($row['branch_id']),
        )
      );
  }

  return $branches;
}

/**
 * retrieve job list that are related to the given branch
 */
function get_branch_job_ids($branch) {
  global $conf;

  $query = '
SELECT
    DISTINCT id_job
  FROM results_def AS r
    INNER JOIN executions AS e ON e.id = r.id_exec
  WHERE e.branch = \''.$branch.'\'
;';

  return array_from_query($query, 'id_job');
}

function get_tuj_log_url($branch, $language, $job) {
  global $conf;

  return sprintf(
    $conf['tuj_log_url'],
    $branch,
    $language,
    $job
    );
}

function find_files($path, $pattern) {
  $path = rtrim(str_replace("\\", "/", $path), '/') . '/';
  $matches = Array();
  $entries = Array();
  $dir = dir($path);
  while (false !== ($entry = $dir->read())) {
    $entries[] = $entry;
  }
  $dir->close();
  foreach ($entries as $entry) {
    $fullname = $path . $entry;
    if ($entry != '.' && $entry != '..' && is_dir($fullname)) {
      $matches = find_files($fullname, $pattern);
    }
    else if (is_file($fullname) && preg_match($pattern, $entry)) {
      $matches[] = $fullname;
    }
  }
  return $matches;
}

function get_charts($charts_dir) {
  if (!file_exists($charts_dir)) {
    return false;
  }

  $charts = array();

  if ($handle = opendir($charts_dir)) {
    /* This is the correct way to loop over the directory. */
    while (false !== ($file = readdir($handle))) {
      if ($file != '.' and $file != '..') {
        $extension = get_extension($file);
        
        if ($extension == 'png') {
          array_push(
            $charts,
            $charts_dir.'/'.$file
            );
        }
      }
    }
    
    closedir($handle);
  }

  rsort($charts);

  return $charts;
}

/**
 *
 * @Count lines in a file
 *
 * @param string filepath 
 *
 * @return int
 *
 */
function countLines($filepath) {
  $handle = fopen( $filepath, "r" );
  $count = 0;
  while (fgets($handle)) {
    $count++;
  }
  fclose($handle);
  return $count;
}
?>

<?php
require('init.inc.php');

require ('query.php');
require ('print_library.php');

if(!isset($_SESSION['hide_filter'])){$_SESSION['hide_filter'] = 0;}
if(!isset($_SESSION['hide_manager'])){$_SESSION['hide_manager'] = 0;}
if($_GET['hide_filter']){$_SESSION['hide_filter']++;}
if($_GET['hide_manager']){$_SESSION['hide_manager']++;}

// echo '<pre>', print_r($_SESSION); echo '</pre>';
// echo '<pre>', print_r($_POST); echo '</pre>';
// print_array(get_languages());

include('header.inc.php');
?>

	<body>
<?php
include('top_menu.inc.php');
?>

		<form action = "search.php" method = "post">
			<div class = "filter">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr 
					<?php 
						if($_SESSION['hide_filter']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-12\"";
						}
					?>>
						<td width=25%>
							Revision
							<select name = "rev_select" onchange = "select_rev(this.options[this.selectedIndex].value)">
								<option value = 'any' 
								<?php
									if(!isset($_SESSION['rev_select']) || $_SESSION['rev_select'] == "any"){
										echo "selected=\"selected\"";
									}
								?>>any</option>
								<option value = 'by_range' 
								<?php 
									if($_SESSION['rev_select'] == "by_range"){
										echo "selected=\"selected\"";
									}
								?>>By Range</option>
								<option value = 'by_last' 
								<?php 
									if($_SESSION['rev_select'] == "by_last"){
										echo "selected=\"selected\"";
									}
								?>>Get Latest</option>
								<option value = 'by_list' 
								<?php 
									if($_SESSION['rev_select'] == "by_list"){
										echo "selected=\"selected\"";
									}
								?>>Select in list</option>
							</select>
						</td>
						<td width=25%>Language</td>
						<td colspan="2">Status</td>
					</tr>
					<tr 
					<?php 
							if($_SESSION['hide_filter']%2==1){
								echo "class=\"hide\"";
							}else{
								echo "class=\"row-11\"";
							}
					?>>
						<td><!-- REVISION -->
							<div id="range" 
							<?php 
								if($_SESSION['rev_select'] != "by_range"){echo "class=\"rrange\"";}
							?>>From
								<select name = "revision_inf">
									<option 
									<?php 
										if($_SESSION['revision_inf'] == "earliest"){
											echo "selected = \"selected\"";
										}
									?>>earliest</option>
									<?php
										while($revi = mysql_fetch_array($list_inf)){
											echo "<option value = \"".$revi['revision']."\" ";
											if(!$reset_revision && $revi['revision'] == $_SESSION['revision_inf']){
												echo "selected = \"selected\"";
											}
											echo ">".$revi['revision']."</option>";
										}
									?>
								</select>
								<br><br>
								To
								<select name = "revision_sup">
									<option 
									<?php 
										if($_SESSION['revision_sup'] == "latest"){
											echo "selected = \"selected\"";
										}
									?>>latest</option>
									<?php
										while($revi = mysql_fetch_array($list_sup)){
											echo "<option value = \"".$revi['revision']."\" ";
											if(!$reset_revision && $revi['revision'] == $_SESSION['revision_sup']){
												echo "selected = \"selected\"";
											}
											echo ">".$revi['revision']."</option>";
										}
									?>
								</select>
							</div>
							<div id="last" 
							<?php 
								if($_SESSION['rev_select'] != "by_last"){
									echo "class=\"rlast\"";
								}
							?>>Get last
								<input type = "text" class = "input_rev" name = "revision_last" 
								<?php 
									echo "value = \"".$_SESSION['revision_last']."\"";
								?>/>
							</div>
							<div id="rev_list" 
							<?php 
								if($_SESSION['rev_select'] != "by_list"){
									echo "class=\"rlist\"";
								}
							?>>
								<select name = "selected_revision[]" size = "6" multiple = "multiple">
									<option value = "all" 
									<?php 
										if($_SESSION['selected_revision'][0] == "all"){
											echo "selected = \"selected\"";
										}
									?>>all</option>
									<?php
										while($revi = mysql_fetch_array($list_revision)){
											echo "<option value=\"".$revi['revision']."\" ";
											if(!$reset_revision){
												is_selected($revi['revision'], $_SESSION['selected_revision']);
												}
											echo ">".$revi['revision']."</option>";
										}
									?>
								</select>
							</div>
						</td>
						<td><!-- LANGUAGE -->
							<select name = "language[]" size = "6" multiple = "multiple">
								<option value = "any" 
									<?php 
										if($_SESSION['language'][0] == "any"){
											echo "selected = \"selected\"";
										}
									?>>any</option>
									<?php
                                                                        
                                                                                foreach (get_languages() as $language) {
											echo '<option value="'.$language.'" ';
											if(!$_POST['reset_condition']){
												is_selected($language, $_SESSION['language']);
											}
											echo ">".$language."</option>";
										}
									?>
							</select>
						</td>
						<td colspan="2"><!-- STATUS -->
							<select name = "status[]" size = "6" multiple = "multiple">
<?php
    $selected = '';
    if (in_array('any', $_SESSION['status'])) {
      $selected = 'selected="selected"';
    }
    echo sprintf(
      '
                                                          <option value="any" %s>any</option>',
      $selected
      );

    foreach ($cache['result_status'] as $status) {
      $selected = '';
      if (in_array($status['id'], $_SESSION['status'])) {
        $selected = 'selected="selected"';
      }

      echo sprintf(
        '
                                                          <option value="%u" %s>%s</option>',
        $status['id'],
        $selected,
        $status['label']
        );
    }
?>
							</select>
						</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_filter']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-12\"";
						}
					?>>
						<td>Key word</td>
						<td>Results per page</td>
						<td width=25%>Sort by</td>
						<td>Sort order</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_filter']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-11\"";
						}
					?>>
						<td><!-- KEY WORD -->
							<input type="text" class="input_key" name="keywd" value="
							<?php 
								echo $_SESSION['keywd'];
							?>"/>
						</td>
						<td>
							<input type = "text" class = "input_num" name = "results_per_page" 
							<?php 
								echo "value = \"".$_SESSION['results_per_page']."\"";
							?>/>
						</td>
						<td><!-- PRIMARY SORT -->
							<select name = "primary_order">
								<option value = 'revision' 
								<?php 
									if($_SESSION['primary_order'] == "revision"){echo "selected=\"selected\"";}
								?>>Revision</option>
								<option value = 'job' 
								<?php 
									if($_SESSION['primary_order'] == "job"){echo "selected=\"selected\"";}
								?>>Job name</option>
								<option value = 'substatus' 
								<?php 
									if($_SESSION['primary_order'] == "substatus"){echo "selected=\"selected\"";}
								?>>Status</option>
								<option value = 'language' 
								<?php 
									if($_SESSION['primary_order'] == "language"){echo "selected=\"selected\"";}
								?>>Language</option>
								<option value = 'moment' 
								<?php 
									if($_SESSION['primary_order'] == "moment"){echo "selected=\"selected\"";}
								?>>Test time</option>
							</select>
						</td>
						<td><!-- PRIMARY SORT -->
							<select name = "primary_desc_tag">
								<option value = 'desc' 
								<?php 
									if($_SESSION['primary_desc_tag'] == "desc"){echo "selected=\"selected\"";}
								?>>DESC</option>
								<option value = 'asc' 
								<?php 
									if($_SESSION['primary_desc_tag'] == "asc"){echo "selected=\"selected\"";}
								?>>ASC</option>
							</select>
						</td>
					</tr>
					<tr>
						<td colspan = "4">
							<?php
								if($_SESSION['hide_filter']%2==1){
									echo "<a href=\"search.php?hide_filter=do\">
										<img border=\"0\" src=\"plus.png\"></a>";
								}else{
									echo "<a href=\"search.php?hide_filter=do\">
										<img border=\"0\" src=\"minus.png\"></a>";
								}
							?>
							<input type = "submit" name = "do_search" value = "Search"/>
							<input type = "reset" value="Reset"/> 
						</td>
					</tr>
				</table>
			</div>
			<br>
			<div class="filtre_manager">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>
							<?php
								if($_SESSION['hide_manager']%2==1){
									echo "<a href=\"search.php?hide_manager=do\">
										<img border=\"0\" src=\"plus.png\"></a>";
								}else{
									echo "<a href=\"search.php?hide_manager=do\">
										<img border=\"0\" src=\"minus.png\"></a>";
								}
							?>&nbsp;Filter Manager
						</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_manager']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-22\"";
						}
					?>>
						<td colspan="2">Load filter</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_manager']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-21\"";
						}
					?>>
						<td><!-- use select & dynamic filter "no/name" list -->
							<select name = "filter_loader"
							<?php
								if($_POST['load_filter']){
									echo "disabled = \"true\"";
								}
								echo ">";
								while($filt = mysql_fetch_array($list_filter)){
									echo "<option ";
									if($_SESSION['fn'] == $filt['filter_no']){
										echo "selected = \"selected\"";
									}
									echo " value = ".$filt['filter_no'];
									echo ">";
									echo "#".$filt['filter_no']." | ".$filt['filter_name'];							
									echo "</option>";
								}
							?>
							</select>
							<input type = "submit" name = "load_filter" value = "Load filter"
							<?php
								if($_POST['load_filter']){
									echo "disabled = \"true\"";
								}
								echo "/>";
							?>
							<input type = "submit" name = "reset_condition" value = "Unload filter" 
							<?php 
								if($reset == "disable"){
									echo "disabled = \"true\"";
								}
							?>/>
							<input type = "submit" name = "delete_filter" value = "Delete filter"
							<?php
								if($_POST['load_filter']){
									echo "disabled = \"true\"";
								}
								echo "/>";
								if($_POST['load_filter']){
									if($record){
										echo "<span class=\"fine\">Loading filter \"#".$_SESSION['fn']
											.":".$_SESSION['filter_name']
											."\" accomplished, click \"Search\" button to query.</span>";
										unset($_POST['load_filter']);
									}else{
										echo "<span class=\"fine\">Failed to load filter.</span>";
									}
								}
							?>
						</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_manager']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-22\"";
						}
					?>>
						<td colspan="2">Save current filter</td>
					</tr>
					<tr 
					<?php 
						if($_SESSION['hide_manager']%2==1){
							echo "class=\"hide\"";
						}else{
							echo "class=\"row-21\"";
						}
					?>>
						<td>
							as
							<input type="text" name="name_filter" class="filter_name"/>
							<input type = "submit" name = "save_filter" value = "Save"
							<?php 
								echo "/>";
								if($_POST['save_filter']){
									if($is_filter_saved){
										echo "<span class=\"fine\">Filter \"".$_POST['name_filter']
											."\" has been saved at slot No.".$filter_no.".</span>";
										unset($_POST['save_filter']);
									}else{
										echo "<span class=\"fine\">Failed to save filter.</span>";
									}
								}
							?>
						</td>
					</tr>
				</table>
			</div>
		</form>
		<br>
		<form action = "search.php" method = "get">
			<div class = "reports">
<?php
if ($page['nb_results'] > 0) {
	echo '
<table border="0" cellspacing="0" cellpadding="0">';
	if (isset($page['num_pages']) and $page['num_pages'] != 1) {
          echo
            "<tr><td class=\"form-title\" colspan=\"5\">Viewing results (",
            sprintf(
              '%u - %u / %u',
              ($_SESSION['selected_page'] - 1) * $_SESSION['results_per_page'] + 1,
              min($_SESSION['selected_page'] * $_SESSION['results_per_page'], $page['nb_results']),
              $page['nb_results']
              ),
            ")</td>"
            ;
          
          echo "<td class=\"right\" colspan=\"5\">";
          print_page_index($_SESSION['selected_page'], $page['num_pages']);
          echo "</td></tr>";
	}
        else {
          echo "<tr><td class=\"form-title\" colspan=\"5\">Viewing All Results (".$page['nb_results']." in all) </td></tr>";
	}
	echo '
  <tr class="row-category2">
    <th>CATEGORY</th>
    <th>';
        
        
	if($_SESSION['primary_order']!='revision' && isset($_SESSION['primary_order'])){
		echo "<a href=\"search.php?page=".$_SESSION['selected_page']."&sort_switcher=2\">REVISION</a>";
	}else{
		echo "<a>REVISION</a>";
	}
	print_arrow('revision', '2');
	echo "
    </th>
    <th>";
	if($_SESSION['primary_order']!='job'){
		echo "<a href=\"search.php?page=".$_SESSION['selected_page']."&sort_switcher=1\">JOB NAME</a>";
	}else{
		echo "<a>JOB NAME</a>";
	}
	print_arrow('job', '1');
	echo "
    </th>
    <th>";
	if($_SESSION['primary_order']!='substatus'){
		echo "<a href=\"search.php?page=".$_SESSION['selected_page']."&sort_switcher=5\">STATUS</a>";
	}else{
		echo "<a>STATUS</a>";
	}	
	print_arrow('substatus', '5');
	echo "
    </th>
    <th>";
	if($_SESSION['primary_order']!='language'){
		echo "<a href=\"search.php?page=".$_SESSION['selected_page']."&sort_switcher=3\">LANGUAGE</a>";
	}else{
		echo "<a>LANGUAGE</a>";
	}
	print_arrow('language', '3');
	echo "
    </th>
    <th>";
	if($_SESSION['primary_order']!='moment'){
		echo "<a href=\"search.php?page=".$_SESSION['selected_page']."&sort_switcher=4\">TEST TIME</a>";
	}else{
		echo "<a>TEST TIME</a>";
	}
	print_arrow('moment', '4');
	echo "
    </th>
    <th>DESCRIPTION</th>
  </tr>
  <tr>
    <td></td>
  </tr>";
        foreach ($page['results'] as $row) {
          echo "
  <tr ";
          $id_status = $row['id_status'];
                
          if (!isset($conf['result_status'][$id_status])) {
            $color = $conf['default_result_status']['color'];
            $bg_color = $conf['default_result_status']['bg_color'];
          }
          else {
            $color = $conf['result_status'][$id_status]['color'];
            $bg_color = $conf['result_status'][$id_status]['bg_color'];
          }
          
          echo sprintf(
            'style="color: %s; background-color: %s"',
            $color,
            $bg_color
            );
                
          echo '>
    <td>', get_branch_name_of($row['branch']), '</td>
    <td>', $row['revision'], '</td>
    <td>', $row['job'], '</td>
    <td style="white-space: nowrap;">',
            $cache['result_status'][$id_status]['label'],
            '</td>
    <td>', $row['language'], '</td>
    <td style="white-space: nowrap;">', $row['moment'], '</td>
    <td>', $row['description'], '</td>
  </tr>';
	}
        
	if(isset($page['num_pages']) and $page['num_pages'] != 1){
		echo "<tr><td class=\"right\" colspan=\"7\">";
		print_page_index($_SESSION['selected_page'], $page['num_pages']);
		echo "</td></tr>";
	}
	echo "</table>";
}else{
	echo "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>";
	echo "<td class=\"form-title\">No Results Found</td></tr></table>";
}
?>
			</div>
		</form>
<?php
include('footer.inc.php');
?>
	</body>
</html>

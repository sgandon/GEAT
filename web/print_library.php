<?php
function is_selected($element, $set){
	if($set){
		foreach ($set as $element_in_set){
			if ($element_in_set == $element){
				echo "selected = \"selected\" ";
			}
		}
	}
}

function print_arrow($name_col, $switch_no){
	if($name_col == $_SESSION['primary_order'] && isset($_SESSION['primary_order'])){
		if($_SESSION['primary_desc_tag']=='desc'){
			echo "<img src=\"down.gif\" width=\"14\" height=\"14\">";
		}else{
			echo "<img src=\"up.gif\" width=\"14\" height=\"14\">";
		}
	}
	if($_SESSION['sort_switcher']==$switch_no && isset($_SESSION['sort_switcher']) && !$_POST['reset_condition'] && !$_POST['do_search']){
		if($_SESSION['sort_tag']%2 == 0){
			echo "<img src=\"up.gif\" width=\"9\" height=\"9\">";
		}else{
			echo "<img src=\"down.gif\" width=\"9\" height=\"9\">";
		}
	}
}

function print_page_index($sg, $np){
	echo "<span class = \"small\"> [";
	if($sg == 1){
		echo  "<b>  First  </b>";
		echo  "<c>  Previous  </c>";
	}else{
		echo "<a href=\"search.php?page=1\">  First  </a>";
		echo "<a href=\"search.php?page=".max($sg-1, 1)."\">  Previous  </a>";
	}
	if($np < 11){
		for ($count_page = 1; $count_page < $np+1; $count_page++){
			if($count_page == $sg){
				echo "<b>  ".$count_page."  </b>";
			}else{
				echo "<a href=\"search.php?page=".$count_page."\">  ".$count_page."  </a>";
			}
		}
	}else{
		if(min(max(1, $sg - 5), $np-10) != 1){
			echo "...";
		}
		for (	$count_page = min(max(1, $sg - 5), $np-10); 
				$count_page < max(12, min($np+1, $sg + 6)); 
				$count_page++){
			if($count_page == $sg){
				echo "<b>  ".$count_page."  </b>";
			}else{
				echo "<a href=\"search.php?page=".$count_page."\">  ".$count_page."  </a>";
			}
		}
		if(max(12, min($np+1, $sg + 6))-1 != $np){
			echo "...";
		}
	}
	if($sg == $np){
		echo  "<c>  Next  </c>";
		echo  "<b>  Last  </b>";
	}else{
		echo "<a href=\"search.php?page=".min($sg+1, $np)
			."\">  Next  </a>";
		echo "<a href=\"search.php?page=".$np."\">  Last  </a>";
	}
	echo "] </span>";
}
?>

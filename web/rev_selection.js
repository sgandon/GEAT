function select_rev(x){
	switch (x){
		case "any":
		document.getElementById('range').style.display = 'none';
		document.getElementById('last').style.display = 'none';
		document.getElementById('rev_list').style.display = 'none';
		break;
		case "by_range":
		document.getElementById('range').style.display = 'block';
		document.getElementById('last').style.display = 'none';
		document.getElementById('rev_list').style.display = 'none';
		break;
		case "by_last":
		document.getElementById('range').style.display = 'none';
		document.getElementById('last').style.display = 'block';
		document.getElementById('rev_list').style.display = 'none';
		break;
		case "by_list":
		document.getElementById('range').style.display = 'none';
		document.getElementById('last').style.display = 'none';
		document.getElementById('rev_list').style.display = 'block';
		break;
	}
}
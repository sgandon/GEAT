<?php
require('init.inc.php');

include('header.inc.php');
?>

  <body id="about">
<?php
include('top_menu.inc.php');
?>

  <div>
    This application shows the results of automated tests on Talend Open Studio generated code.</br>
    What comes into play:
    <ul>
      <li>code generation engine</li>
      <li>components code</li>
      <li>common code</li>
    </ul>

    It should allow our development team to:
    <ul>
      <li>detect regressions</li>
      <li>ensure backward compatibility</li>
      <li>follow-up bug fixing</li>
    </ul>
    And fix bugs if needed as fast as possible using "all revisions" testing.
  </div>
  <div style="float: right;"><img src="icons/colibri.png" width="200"></div>
  <div>
    </br></br>
    For further information:
    <ul>
      <li><a href="http://talendforge.org/wiki/doku.php?id=tests:automated_tests:results">how to use this web-app</a></li>
      <li><a href="http://talendforge.org/wiki/doku.php?id=tests:automated_tests:design">how to design a tuj</a></li>
      <li><a href="http://talendforge.org/wiki/doku.php?id=tests:automated_tests:validation_rules">will my tuj work on the automation platform ?</a></li>
    </ul>
  </div>
<?php
include('footer.inc.php');
?>
  </body>
</html>


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `components`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `components` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `components` WRITE;
/*!40000 ALTER TABLE `components` DISABLE KEYS */;
/*!40000 ALTER TABLE `components` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `components_jobs`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `components_jobs` (
  `id_component` varchar(20) default NULL,
  `nbOccurences` int(11) default NULL,
  `id_exec` int(11) default NULL,
  `id_job` int(11) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `components_jobs` WRITE;
/*!40000 ALTER TABLE `components_jobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `components_jobs` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `criticality`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `criticality` (
  `id` int(11) NOT NULL auto_increment,
  `label` varchar(50) NOT NULL,
  `description` varchar(250) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `criticality` WRITE;
/*!40000 ALTER TABLE `criticality` DISABLE KEYS */;
INSERT INTO `criticality` VALUES (1,'Critical','Jobs that can alter other jobs results'),(2,'Block','Jobs that block a public build'),(3,'Low','Default criticality');
/*!40000 ALTER TABLE `criticality` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `exec_stats`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `exec_stats` (
  `id_exec` int(11) NOT NULL,
  `nb_jobs` int(11) NOT NULL,
  `language` varchar(5) NOT NULL default '',
  `error_nb` int(5) default NULL,
  `failure_nb` int(5) default NULL,
  `error_percent` float(5,4) default NULL,
  `success_percent` float(5,4) default NULL,
  `failure_percent` float(5,4) default NULL,
  `success_nb` int(5) default NULL,
  PRIMARY KEY  (`id_exec`,`language`),
  KEY `language` (`language`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `exec_stats` WRITE;
/*!40000 ALTER TABLE `exec_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `exec_stats` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `executions`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `executions` (
  `id` int(11) NOT NULL auto_increment,
  `branch` varchar(50) NOT NULL,
  `revision` varchar(50) NOT NULL,
  `launch_date` datetime NOT NULL,
  `status` varchar(250) default NULL,
  `log_files` varchar(250) default NULL,
  `end_date` datetime default NULL,
  `duration` int(10) default NULL,
  `nb_jobs` int(5) default NULL,
  `duration_per_job` float(12,4) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `executions` WRITE;
/*!40000 ALTER TABLE `executions` DISABLE KEYS */;
/*!40000 ALTER TABLE `executions` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `filter_serial`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `filter_serial` (
  `filter_no` int(10) NOT NULL,
  `filter_name` varchar(100) NOT NULL,
  `enabled` enum('true','false') NOT NULL,
  `filter_info` text NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `filter_serial` WRITE;
/*!40000 ALTER TABLE `filter_serial` DISABLE KEYS */;
/*!40000 ALTER TABLE `filter_serial` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `jobs`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `jobs` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(100) default NULL,
  `language` varchar(5) default NULL,
  `id_criticality` int(11) default '3',
  `id_validation` int(11) default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `jobs` WRITE;
/*!40000 ALTER TABLE `jobs` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobs` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `jobs_changes`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `jobs_changes` (
  `id_job` int(11) NOT NULL,
  `id_exec` int(11) NOT NULL,
  `md5sum` varchar(50) default NULL,
  PRIMARY KEY  (`id_job`,`id_exec`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `jobs_changes` WRITE;
/*!40000 ALTER TABLE `jobs_changes` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobs_changes` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `jobs_resp`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `jobs_resp` (
  `id_job` int(11) NOT NULL,
  `mail` varchar(50) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `jobs_resp` WRITE;
/*!40000 ALTER TABLE `jobs_resp` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobs_resp` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `jobs_stats`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `jobs_stats` (
  `id_exec` int(11) NOT NULL,
  `id_job` int(11) NOT NULL,
  `duration` int(10) default NULL,
  `importduration` int(10) default NULL,
  `executionduration` int(10) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `jobs_stats` WRITE;
/*!40000 ALTER TABLE `jobs_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `jobs_stats` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `port_activity`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `port_activity` (
  `port` int(11) NOT NULL,
  `branch` varchar(150) NOT NULL,
  `job_name` varchar(150) NOT NULL,
  `language` varchar(10) NOT NULL,
  `launch_date` datetime default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `port_activity` WRITE;
/*!40000 ALTER TABLE `port_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `port_activity` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `results_def`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `results_def` (
  `moment` datetime default NULL,
  `origin` varchar(50) default NULL,
  `description` varchar(255) default NULL,
  `id_exec` int(11) default NULL,
  `id_status` int(11) default NULL,
  `id_job` int(11) default NULL,
  KEY `id_exec` (`id_exec`,`id_status`,`id_job`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `results_def` WRITE;
/*!40000 ALTER TABLE `results_def` DISABLE KEYS */;
/*!40000 ALTER TABLE `results_def` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `status`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `status` (
  `id` int(11) NOT NULL auto_increment,
  `main_status` varchar(50) NOT NULL,
  `sub_status` varchar(50) NOT NULL,
  `short_name` varchar(20) NOT NULL,
  `priority` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `status` WRITE;
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` VALUES (1,'Success','--','Ok',10),(2,'Failure','Test logically failed','Logic',20),(3,'Error','Job execution error','Exec',30),(4,'Error','Unresolved compilation problem','Compil',40),(5,'Error','Failed to generate code','CodeGen',40),(6,'Error','Missing module','Miss.Mod.',40),(7,'Error','Didn\'t run','Didn\'t run',40),(8,'Error','Unknown','??',50),(9,'Error','No assert logged/no error file','No assert',45),(10,'Error','Timeout','Timeout',40);
/*!40000 ALTER TABLE `status` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `validation_status`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `validation_status` (
  `id` int(11) NOT NULL,
  `name` varchar(55) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

LOCK TABLES `validation_status` WRITE;
/*!40000 ALTER TABLE `validation_status` DISABLE KEYS */;
INSERT INTO `validation_status` VALUES (0,'Draft'),(1,'Tech. ok');
/*!40000 ALTER TABLE `validation_status` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

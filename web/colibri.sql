-- MySQL dump 10.11
--
-- Host: localhost    Database: test_results
-- ------------------------------------------------------
-- Server version	5.0.45-Debian_1ubuntu3.3-log

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

--
-- Table structure for table `components`
--

DROP TABLE IF EXISTS `components`;
CREATE TABLE `components` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=171 DEFAULT CHARSET=latin1;

--
-- Table structure for table `components_jobs`
--

DROP TABLE IF EXISTS `components_jobs`;
CREATE TABLE `components_jobs` (
  `id_component` varchar(20) default NULL,
  `nbOccurences` int(11) default NULL,
  `id_exec` int(11) default NULL,
  `id_job` int(11) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `criticality`
--

DROP TABLE IF EXISTS `criticality`;
CREATE TABLE `criticality` (
  `id` int(11) NOT NULL auto_increment,
  `label` varchar(50) NOT NULL,
  `description` varchar(250) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Table structure for table `exec_stats`
--

DROP TABLE IF EXISTS `exec_stats`;
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

--
-- Table structure for table `executions`
--

DROP TABLE IF EXISTS `executions`;
CREATE TABLE `executions` (
  `id` int(11) NOT NULL auto_increment,
  `branch` varchar(50) NOT NULL,
  `revision` int(11) NOT NULL,
  `launch_date` datetime NOT NULL,
  `status` varchar(250) default NULL,
  `log_files` varchar(250) default NULL,
  `end_date` datetime default NULL,
  `duration` int(10) default NULL,
  `nb_jobs` int(5) default NULL,
  `duration_per_job` float(12,4) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3925 DEFAULT CHARSET=latin1;

--
-- Table structure for table `filter_serial`
--

DROP TABLE IF EXISTS `filter_serial`;
CREATE TABLE `filter_serial` (
  `filter_no` int(10) NOT NULL,
  `filter_name` varchar(100) NOT NULL,
  `enabled` enum('true','false') NOT NULL,
  `filter_info` text NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `flowcatcher`
--

DROP TABLE IF EXISTS `flowcatcher`;
CREATE TABLE `flowcatcher` (
  `moment` datetime default NULL,
  `pid` varchar(20) default NULL,
  `father_pid` varchar(20) default NULL,
  `root_pid` varchar(20) default NULL,
  `system_pid` bigint(20) default NULL,
  `project` varchar(50) default NULL,
  `job` varchar(50) default NULL,
  `job_repository_id` varchar(255) default NULL,
  `job_version` varchar(255) default NULL,
  `context` varchar(50) default NULL,
  `origin` varchar(255) default NULL,
  `label` varchar(255) default NULL,
  `count` int(11) default NULL,
  `reference` int(11) default NULL,
  `thresholds` varchar(255) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
CREATE TABLE `jobs` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(55) default NULL,
  `language` varchar(5) default NULL,
  `id_criticality` int(11) default '3',
  `id_validation` int(11) default '0',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=589 DEFAULT CHARSET=latin1;

--
-- Table structure for table `jobs_changes`
--

DROP TABLE IF EXISTS `jobs_changes`;
CREATE TABLE `jobs_changes` (
  `id_job` int(11) NOT NULL,
  `id_exec` int(11) NOT NULL,
  `md5sum` varchar(50) default NULL,
  PRIMARY KEY  (`id_job`,`id_exec`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `jobs_resp`
--

DROP TABLE IF EXISTS `jobs_resp`;
CREATE TABLE `jobs_resp` (
  `id_job` int(11) NOT NULL,
  `mail` varchar(50) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `logcatcher`
--

DROP TABLE IF EXISTS `logcatcher`;
CREATE TABLE `logcatcher` (
  `moment` datetime default NULL,
  `pid` varchar(20) default NULL,
  `root_pid` varchar(20) default NULL,
  `father_pid` varchar(20) default NULL,
  `project` varchar(50) default NULL,
  `job` varchar(50) default NULL,
  `context` varchar(50) default NULL,
  `priority` int(3) default NULL,
  `type` varchar(255) default NULL,
  `origin` varchar(255) default NULL,
  `message` varchar(255) default NULL,
  `code` int(3) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `results_def`
--

DROP TABLE IF EXISTS `results_def`;
CREATE TABLE `results_def` (
  `moment` datetime default NULL,
  `origin` varchar(50) default NULL,
  `description` varchar(255) default NULL,
  `id_exec` int(11) default NULL,
  `id_status` int(11) default NULL,
  `id_job` int(11) default NULL,
  KEY `id_exec` (`id_exec`,`id_status`,`id_job`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `results_temp_trunk`
--

DROP TABLE IF EXISTS `results_temp_trunk`;
CREATE TABLE `results_temp_trunk` (
  `moment` datetime default NULL,
  `pid` varchar(20) default NULL,
  `project` varchar(50) default NULL,
  `job` varchar(55) default NULL,
  `language` varchar(5) default NULL,
  `origin` varchar(50) default NULL,
  `status` varchar(10) default NULL,
  `substatus` varchar(255) default NULL,
  `description` varchar(255) default NULL,
  `result_destination_dir` varchar(255) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `statcatcher`
--

DROP TABLE IF EXISTS `statcatcher`;
CREATE TABLE `statcatcher` (
  `moment` datetime default NULL,
  `pid` varchar(20) default NULL,
  `father_pid` varchar(20) default NULL,
  `root_pid` varchar(20) default NULL,
  `system_pid` bigint(20) default NULL,
  `project` varchar(50) default NULL,
  `job` varchar(50) default NULL,
  `job_repository_id` varchar(255) default NULL,
  `job_version` varchar(255) default NULL,
  `context` varchar(50) default NULL,
  `origin` varchar(255) default NULL,
  `message_type` varchar(255) default NULL,
  `message` varchar(255) default NULL,
  `duration` bigint(20) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `status`
--

DROP TABLE IF EXISTS `status`;
CREATE TABLE `status` (
  `id` int(11) NOT NULL auto_increment,
  `main_status` varchar(50) NOT NULL,
  `sub_status` varchar(50) NOT NULL,
  `short_name` varchar(20) NOT NULL,
  `priority` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;

--
-- Table structure for table `unitest`
--

DROP TABLE IF EXISTS `unitest`;
CREATE TABLE `unitest` (
  `job` varchar(100) default NULL,
  `status` varchar(50) NOT NULL,
  `revision` int(6) NOT NULL,
  `substatus` varchar(50) NOT NULL,
  `language` varchar(50) NOT NULL,
  `moment` char(100) default NULL,
  `branch` varchar(20) default NULL,
  `description` varchar(255) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `validation_status`
--

DROP TABLE IF EXISTS `validation_status`;
CREATE TABLE `validation_status` (
  `id` int(11) NOT NULL,
  `name` varchar(55) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2008-06-13 11:25:21

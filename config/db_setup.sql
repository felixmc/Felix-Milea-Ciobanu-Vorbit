SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT=0;
START TRANSACTION;
SET time_zone = "+00:00";

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_dataset_ngrams`(IN dataset INT)
BEGIN

	DELETE FROM `1grams` WHERE `dataset` = dataset;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_dataset_ngram_comparisons`(IN dataset INT)
BEGIN
	DELETE FROM `subset_ngram_comparisons` WHERE `dataset`=dataset;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_dataset_symbols`(IN dataset INT)
BEGIN
	DELETE FROM `symbols` WHERE `dataset` = dataset;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_edition_ngrams`(IN dataset INT, IN edition INT)
BEGIN

	DELETE FROM `1grams` WHERE `dataset` = dataset AND `edition` = edition;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_edition_ngram_comparisons`(IN dataset INT, IN edition INT)
BEGIN
	DELETE FROM `subset_ngram_comparisons` WHERE `dataset`=dataset AND `edition`=edition;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `clear_edition_symbols`(IN dataset INT, IN edition INT)
BEGIN
	DELETE FROM `symbols` WHERE `dataset` = dataset AND `edition` = edition;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `create_dataset`(IN `n` VARCHAR(128) CHARSET latin1, OUT `result` INT)
BEGIN
SELECT `id` INTO result FROM `datasets` WHERE `name` = n LIMIT 1;
IF result IS NULL THEN
    INSERT INTO `datasets`(`name`) VALUES (n);
    SET result = LAST_INSERT_ID();
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `create_edition`(IN `n` VARCHAR(128), OUT `result` INT)
BEGIN
SELECT `id` INTO result FROM `editions` WHERE `name` = n LIMIT 1;
IF result IS NULL THEN
    INSERT INTO `editions`(`name`) VALUES (n);
    SET result = LAST_INSERT_ID();
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `create_mining_task`(IN `dataset` INT, IN `name` VARCHAR(128))
    MODIFIES SQL DATA
BEGIN
	INSERT INTO `dataset_mining_tasks` VALUES(NULL, dataset, name, NULL);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `create_subset`(IN `n` VARCHAR(128), OUT `result` INT)
BEGIN
SELECT `id` INTO result FROM `subsets` WHERE `name`=n LIMIT 1;
IF result IS NULL THEN
    INSERT INTO `subsets`(`name`) VALUES (n);
    SET result = LAST_INSERT_ID();
END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_dataset`(IN `id` INT)
    NO SQL
BEGIN

DELETE FROM `datasets` WHERE `id` = id;
CALL delete_reddit_corpus(id);

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_edition`(IN id INT)
BEGIN

DELETE FROM `editions` WHERE `id` = id;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_subset`(IN id INT)
BEGIN

DELETE FROM `subsets` WHERE `id` = id;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `record_1gram`(IN `datasetId` INT, IN `subsetId` INT, IN `editionId` INT, IN `gram` VARCHAR(400), OUT `gramId` INT)
BEGIN

    INSERT INTO `1grams` VALUES (NULL, datasetId, subsetId, editionId, gram, 1) ON DUPLICATE KEY UPDATE `freq` = `freq` + 1;
    SELECT `id` INTO gramId FROM `1grams` WHERE `1gram` = gram AND `dataset` = datasetId AND `subset` = subsetId AND `edition` = editionId LIMIT 1;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `record_ngram`(IN `n` INT, IN `data` VARCHAR(128))
BEGIN

    SET @s = CONCAT('INSERT INTO `', n, 'grams` VALUES (NULL,', data, ',1) ON DUPLICATE KEY UPDATE `freq` = `freq` + 1');
    PREPARE stm FROM @s;
    EXECUTE stm;

END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `record_ngram_comparison`(IN dataset INT, IN edition INT, IN n INT, IN subset1 INT, IN subset2 INT, IN ngram1 INT, IN ngram2 INT)
BEGIN
	INSERT INTO `subset_ngram_comparisons` VALUES(NULL, dataset, edition, subset1, subset2, ngram1, ngram2, n, 1) ON DUPLICATE KEY UPDATE `freq` = `freq` + 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `record_symbol`(IN `dataset` INT, IN `subset` INT, IN `edition` INT, IN `symbol` VARCHAR(16), IN `bf` INT, IN `af` INT)
BEGIN
	INSERT INTO `symbols` VALUES(NULL, dataset, subset, edition, bf, af, symbol, 1) ON DUPLICATE KEY UPDATE `freq` = `freq` + 1;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `update_mining_task`(IN `dataset` INT, IN `name` VARCHAR(128))
BEGIN
	UPDATE `dataset_mining_tasks` SET `last_performed`=NOW() WHERE `dataset`=dataset AND `name`=name;
END$$

DELIMITER ;

CREATE TABLE IF NOT EXISTS `1grams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataset` int(11) NOT NULL,
  `subset` int(11) NOT NULL,
  `edition` int(11) NOT NULL,
  `1gram` varchar(400) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset` (`dataset`,`subset`,`edition`,`1gram`),
  KEY `subset` (`subset`),
  KEY `edition` (`edition`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `2grams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gram1` int(11) NOT NULL,
  `gram2` int(11) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `gram1_2` (`gram1`,`gram2`),
  KEY `gram1` (`gram1`,`gram2`),
  KEY `gram2` (`gram2`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `3grams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gram1` int(11) NOT NULL,
  `gram2` int(11) NOT NULL,
  `gram3` int(11) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `gram1_2` (`gram1`,`gram2`,`gram3`),
  KEY `gram1` (`gram1`,`gram2`,`gram3`),
  KEY `gram2` (`gram2`),
  KEY `gram3` (`gram3`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `4grams` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gram1` int(11) NOT NULL,
  `gram2` int(11) NOT NULL,
  `gram3` int(11) NOT NULL,
  `gram4` int(11) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `gram1_2` (`gram1`,`gram2`,`gram3`,`gram4`),
  KEY `gram1` (`gram1`,`gram2`,`gram3`,`gram4`),
  KEY `gram1_3` (`gram1`),
  KEY `gram2` (`gram2`),
  KEY `gram3` (`gram3`),
  KEY `gram4` (`gram4`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `datasets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `dataset_mining_tasks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataset` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `last_performed` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset_2` (`dataset`,`name`),
  KEY `dataset` (`dataset`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `editions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `reddit_accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(64) NOT NULL,
  `modhash` varchar(128) DEFAULT NULL,
  `cookie` varchar(128) DEFAULT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `date_created` datetime NOT NULL,
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `reddit_corpus` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reddit_id` varchar(32) NOT NULL,
  `parent` varchar(32) DEFAULT NULL,
  `type` enum('t1','t3') NOT NULL,
  `author` varchar(20) NOT NULL,
  `subreddit` varchar(20) NOT NULL,
  `title` varchar(300) DEFAULT NULL,
  `content` text NOT NULL,
  `children_count` int(11) NOT NULL,
  `ups` int(11) NOT NULL,
  `downs` int(11) NOT NULL,
  `gilded` int(11) DEFAULT NULL,
  `date_posted` datetime NOT NULL,
  `date_mined` datetime NOT NULL,
  `dataset` int(11) NOT NULL,
  `subset` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `reddit_id` (`reddit_id`,`dataset`,`subset`),
  KEY `dataset` (`dataset`,`subset`),
  KEY `subset` (`subset`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `subsets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `subset_ngram_comparisons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataset` int(11) NOT NULL,
  `edition` int(11) NOT NULL,
  `subset1` int(11) NOT NULL,
  `subset2` int(11) NOT NULL,
  `ngram1` int(11) NOT NULL,
  `ngram2` int(11) NOT NULL,
  `n` int(11) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset_2` (`dataset`,`edition`,`subset1`,`subset2`,`ngram1`,`ngram2`,`n`),
  KEY `dataset` (`dataset`,`edition`,`subset1`,`subset2`,`ngram1`,`ngram2`),
  KEY `edition` (`edition`),
  KEY `subset2` (`subset2`),
  KEY `ngram1` (`ngram1`),
  KEY `ngram2` (`ngram2`),
  KEY `subset1` (`subset1`),
  KEY `subset2_2` (`subset2`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `symbols` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataset` int(11) NOT NULL,
  `subset` int(11) NOT NULL,
  `edition` int(11) NOT NULL,
  `before` int(11) NOT NULL,
  `after` int(11) NOT NULL,
  `symbol` varchar(16) NOT NULL,
  `freq` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dataset_2` (`dataset`,`subset`,`edition`,`before`,`after`,`symbol`),
  KEY `dataset` (`dataset`,`subset`,`edition`,`before`,`after`),
  KEY `subset` (`subset`),
  KEY `edition` (`edition`),
  KEY `before` (`before`),
  KEY `after` (`after`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


ALTER TABLE `1grams`
  ADD CONSTRAINT `1grams_ibfk_1` FOREIGN KEY (`dataset`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `1grams_ibfk_4` FOREIGN KEY (`subset`) REFERENCES `subsets` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `1grams_ibfk_5` FOREIGN KEY (`edition`) REFERENCES `editions` (`id`) ON UPDATE CASCADE;

ALTER TABLE `2grams`
  ADD CONSTRAINT `2grams_ibfk_1` FOREIGN KEY (`gram1`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `2grams_ibfk_2` FOREIGN KEY (`gram2`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `3grams`
  ADD CONSTRAINT `3grams_ibfk_1` FOREIGN KEY (`gram1`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `3grams_ibfk_2` FOREIGN KEY (`gram2`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `3grams_ibfk_3` FOREIGN KEY (`gram3`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `4grams`
  ADD CONSTRAINT `4grams_ibfk_1` FOREIGN KEY (`gram1`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `4grams_ibfk_2` FOREIGN KEY (`gram2`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `4grams_ibfk_3` FOREIGN KEY (`gram3`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `4grams_ibfk_4` FOREIGN KEY (`gram4`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `dataset_mining_tasks`
  ADD CONSTRAINT `dataset_mining_tasks_ibfk_1` FOREIGN KEY (`dataset`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `reddit_corpus`
  ADD CONSTRAINT `reddit_corpus_ibfk_1` FOREIGN KEY (`dataset`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `reddit_corpus_ibfk_2` FOREIGN KEY (`subset`) REFERENCES `subsets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `subset_ngram_comparisons`
  ADD CONSTRAINT `subset_ngram_comparisons_ibfk_1` FOREIGN KEY (`dataset`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `subset_ngram_comparisons_ibfk_2` FOREIGN KEY (`edition`) REFERENCES `editions` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `subset_ngram_comparisons_ibfk_3` FOREIGN KEY (`subset2`) REFERENCES `subsets` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `subset_ngram_comparisons_ibfk_4` FOREIGN KEY (`subset1`) REFERENCES `subsets` (`id`) ON UPDATE CASCADE;

ALTER TABLE `symbols`
  ADD CONSTRAINT `symbols_ibfk_1` FOREIGN KEY (`dataset`) REFERENCES `datasets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `symbols_ibfk_2` FOREIGN KEY (`subset`) REFERENCES `subsets` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `symbols_ibfk_3` FOREIGN KEY (`edition`) REFERENCES `editions` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `symbols_ibfk_4` FOREIGN KEY (`before`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `symbols_ibfk_5` FOREIGN KEY (`after`) REFERENCES `1grams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;
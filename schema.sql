SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `ne_db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `ne_db` ;

-- -----------------------------------------------------
-- Table `ne_db`.`customer`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`customer` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`customer` (
  `c_id` INT NOT NULL AUTO_INCREMENT ,
  `id_number` VARCHAR(15) NOT NULL ,
  `name` VARCHAR(30) NOT NULL ,
  `nationality` VARCHAR(30) NOT NULL ,
  `gender` ENUM('male', 'female', 'N/A') NOT NULL ,
  `birthday` DATE NOT NULL ,
  PRIMARY KEY (`c_id`) ,
  UNIQUE INDEX `id_number_UNIQUE` (`id_number` ASC) ,
  UNIQUE INDEX `c_id_UNIQUE` (`c_id` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ne_db`.`flight`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`flight` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`flight` (
  `f_id` INT NOT NULL AUTO_INCREMENT ,
  `flight_number` DECIMAL(4,0) NOT NULL ,
  `d_time` DATETIME NOT NULL ,
  `duration` TIME NOT NULL ,
  `d_city` VARCHAR(20) NOT NULL ,
  `a_city` VARCHAR(20) NOT NULL ,
  `capacity` INT NOT NULL ,
  `occupied` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`f_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ne_db`.`flight_status`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`flight_status` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`flight_status` (
  `f_id` INT NOT NULL ,
  `status` VARCHAR(20) NOT NULL DEFAULT 'scheduled' ,
  PRIMARY KEY (`f_id`) ,
  CONSTRAINT `f_id`
    FOREIGN KEY (`f_id` )
    REFERENCES `ne_db`.`flight` (`f_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ne_db`.`reservation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`reservation` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`reservation` (
  `customer_c_id` INT NOT NULL ,
  `flight_f_id` INT NOT NULL ,
  `seat` INT NOT NULL ,
  PRIMARY KEY (`customer_c_id`, `flight_f_id`) ,
  INDEX `fk_customer_has_flight_flight1_idx` (`flight_f_id` ASC) ,
  INDEX `fk_customer_has_flight_customer_idx` (`customer_c_id` ASC) ,
  CONSTRAINT `fk_customer_has_flight_customer`
    FOREIGN KEY (`customer_c_id` )
    REFERENCES `ne_db`.`customer` (`c_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_customer_has_flight_flight1`
    FOREIGN KEY (`flight_f_id` )
    REFERENCES `ne_db`.`flight` (`f_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ne_db`.`employee`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`employee` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`employee` (
  `e_id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(30) NULL ,
  `password` VARCHAR(16) NOT NULL ,
  PRIMARY KEY (`e_id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ne_db`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ne_db`.`user` ;

CREATE  TABLE IF NOT EXISTS `ne_db`.`user` (
  `c_id` INT NOT NULL ,
  `password` VARCHAR(16) NULL ,
  PRIMARY KEY (`c_id`) ,
  CONSTRAINT `c_id`
    FOREIGN KEY (`c_id` )
    REFERENCES `ne_db`.`customer` (`c_id` )
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

USE `ne_db` ;

-- -----------------------------------------------------
-- procedure book
-- -----------------------------------------------------

USE `ne_db`;
DROP procedure IF EXISTS `ne_db`.`book`;

DELIMITER $$
USE `ne_db`$$
CREATE PROCEDURE `ne_db`.`book` (IN cid INT, IN fid INT, OUT errno INT)
BEGIN
	DECLARE oc INT;
	DECLARE cp INT;
	SET @st = 1;
	SELECT occupied INTO oc FROM flight WHERE f_id = fid;
	SELECT capacity INTO cp FROM flight WHERE f_id = fid;
	IF EXISTS (SELECT * FROM reservation WHERE flight_f_id=fid AND customer_c_id=cid) THEN
		SET errno = -1;
	ELSE
		IF oc<cp THEN
			DROP TEMPORARY TABLE IF EXISTS `ava`;
			CREATE TEMPORARY TABLE ava AS (SELECT seat FROM reservation WHERE flight_f_id=fid);
			WHILE EXISTS (SELECT * FROM ava WHERE ava.seat=@st) DO
				SET @st = @st + 1;
			END WHILE;
			INSERT INTO `reservation` (`customer_c_id`, `flight_f_id`, `seat`) VALUES (cid, fid, @st);
			UPDATE flight SET occupied = occupied + 1 WHERE f_id = fid;
			DROP TEMPORARY TABLE ava;
			SET errno = 0;
		ELSE
			SET errno = -2;
		END IF;
		
	END IF;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure cancel
-- -----------------------------------------------------

USE `ne_db`;
DROP procedure IF EXISTS `ne_db`.`cancel`;

DELIMITER $$
USE `ne_db`$$
CREATE PROCEDURE `ne_db`.`cancel` (IN cid INT, IN fid INT, OUT errno INT)
BEGIN
	IF NOT EXISTS (SELECT * FROM reservation WHERE flight_f_id=fid AND customer_c_id=cid) THEN
		SET errno = -1;
	ELSE	
		DELETE from `reservation` WHERE flight_f_id=fid AND customer_c_id=cid;
		UPDATE flight SET occupied = occupied - 1 WHERE f_id = fid;
		SET errno = 0;
	END IF;
END$$

DELIMITER ;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 06, 2024 at 05:39 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `generateurquiz`
--

-- --------------------------------------------------------

--
-- Table structure for table `options`
--

CREATE TABLE `options` (
  `id` int(11) NOT NULL,
  `texte` varchar(255) NOT NULL,
  `correcte` tinyint(1) NOT NULL,
  `idQuestion` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `id` int(11) NOT NULL,
  `texte` varchar(255) NOT NULL,
  `idSujet` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `question`
--

-- --------------------------------------------------------

--
-- Table structure for table `questionmcq`
--

CREATE TABLE `questionmcq` (
  `id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `questionmcq`
--

-- --------------------------------------------------------

--
-- Table structure for table `questionremplirblanc`
--

CREATE TABLE `questionremplirblanc` (
  `id` int(11) NOT NULL,
  `reponse` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `questionremplirblanc`
--

-- --------------------------------------------------------

--
-- Table structure for table `questionvraisfaux`
--

CREATE TABLE `questionvraisfaux` (
  `id` int(11) NOT NULL,
  `reponseCorrecte` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `questionvraisfaux`
--

-- --------------------------------------------------------

--
-- Table structure for table `sujet`
--

CREATE TABLE `sujet` (
  `id` int(11) NOT NULL,
  `texte` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sujet`
--

INSERT INTO `sujet` (`id`, `texte`) VALUES
(1, 'Science'),
(2, 'General'),
(3, 'Art');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `options`
--
ALTER TABLE `options`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_QuestionMCQ_Option` (`idQuestion`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK_Question_Sujet` (`idSujet`);

--
-- Indexes for table `questionmcq`
--
ALTER TABLE `questionmcq`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `questionremplirblanc`
--
ALTER TABLE `questionremplirblanc`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `questionvraisfaux`
--
ALTER TABLE `questionvraisfaux`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `sujet`
--
ALTER TABLE `sujet`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `options`
--
ALTER TABLE `options`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `options`
--
ALTER TABLE `options`
  ADD CONSTRAINT `FK_QuestionMCQ_Option` FOREIGN KEY (`idQuestion`) REFERENCES `questionmcq` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `FK_Question_Sujet` FOREIGN KEY (`idSujet`) REFERENCES `sujet` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `questionmcq`
--
ALTER TABLE `questionmcq`
  ADD CONSTRAINT `FK_Question_MCQ` FOREIGN KEY (`id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `questionremplirblanc`
--
ALTER TABLE `questionremplirblanc`
  ADD CONSTRAINT `FK_Question_RB` FOREIGN KEY (`id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `questionvraisfaux`
--
ALTER TABLE `questionvraisfaux`
  ADD CONSTRAINT `FK_Question_VF` FOREIGN KEY (`id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

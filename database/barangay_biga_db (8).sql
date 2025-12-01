-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 30, 2025 at 03:31 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `barangay_biga_db`
--

-- --------------------------------------------------------

--
-- Stand-in structure for view `adults_view`
-- (See below for the actual view)
--
CREATE TABLE `adults_view` (
`resident_id` int(11)
,`household_id` int(11)
,`first_name` varchar(100)
,`middle_name` varchar(100)
,`last_name` varchar(100)
,`birth_date` date
,`age` bigint(11)
,`gender` varchar(20)
,`contact_no` varchar(20)
,`email` varchar(150)
,`created_at` timestamp
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Table structure for table `barangay_officials`
--

CREATE TABLE `barangay_officials` (
  `id` int(11) NOT NULL,
  `position_title` varchar(100) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL,
  `display_order` int(11) NOT NULL DEFAULT 0,
  `is_active` enum('Yes','No') DEFAULT 'Yes',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `barangay_officials`
--

INSERT INTO `barangay_officials` (`id`, `position_title`, `full_name`, `image_path`, `display_order`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'Barangay Chairman', 'Hon. Juan Dela Cruz', 'default_chairman.png', 1, 'Yes', '2025-11-26 05:09:08', '2025-11-29 01:07:47'),
(2, 'Barangay Secretary', 'Maria Santos', 'default_secretary.png', 2, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(3, 'Barangay Treasurer', 'Pedro Reyes', 'default_treasurer.png', 3, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(4, 'Barangay Kagawad 1', 'Rosa Martinez', 'default_kagawad.png', 4, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(5, 'Barangay Kagawad 2', 'Carlos Ramos', 'default_kagawad.png', 5, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(6, 'Barangay Kagawad 3', 'Elena Fernandez', 'default_kagawad.png', 6, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(7, 'Barangay Kagawad 4', 'Miguel Castro', 'default_kagawad.png', 7, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(8, 'Barangay Kagawad 5', 'Teresa Aquino', 'default_kagawad.png', 8, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(9, 'Barangay Kagawad 6', 'Antonio Villanueva', 'default_kagawad.png', 9, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(10, 'Barangay Kagawad 7', 'Lorna Bautista', 'default_kagawad.png', 10, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(11, 'SK Chairman', 'Jose Garcia Jr.', 'default_sk.png', 11, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08'),
(12, 'Barangay Tanod', 'Roberto Cruz', 'default_tanod.png', 12, 'Yes', '2025-11-26 05:09:08', '2025-11-26 05:09:08');

-- --------------------------------------------------------

--
-- Table structure for table `barangay_projects`
--

CREATE TABLE `barangay_projects` (
  `project_id` int(11) NOT NULL,
  `project_name` varchar(255) NOT NULL,
  `project_description` text DEFAULT NULL,
  `project_status` enum('Planning','Ongoing','Completed','On Hold','Cancelled') NOT NULL DEFAULT 'Planning',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `completion_date` date DEFAULT NULL,
  `proponent` varchar(255) NOT NULL,
  `beneficiaries` text DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `total_budget` decimal(15,2) NOT NULL DEFAULT 0.00,
  `budget_utilized` decimal(15,2) NOT NULL DEFAULT 0.00,
  `budget_remaining` decimal(15,2) GENERATED ALWAYS AS (`total_budget` - `budget_utilized`) STORED,
  `funding_source` varchar(255) DEFAULT NULL,
  `project_category` varchar(100) DEFAULT NULL,
  `priority_level` enum('High','Medium','Low') DEFAULT 'Medium',
  `progress_percentage` int(11) DEFAULT 0 CHECK (`progress_percentage` >= 0 and `progress_percentage` <= 100),
  `remarks` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `barangay_projects`
--

INSERT INTO `barangay_projects` (`project_id`, `project_name`, `project_description`, `project_status`, `start_date`, `end_date`, `completion_date`, `proponent`, `beneficiaries`, `location`, `total_budget`, `budget_utilized`, `funding_source`, `project_category`, `priority_level`, `progress_percentage`, `remarks`, `created_at`, `updated_at`) VALUES
(1, 'Barangay Hall Renovation', 'Complete renovation of the barangay hall including structural repairs, repainting, and installation of new fixtures.', 'Ongoing', '2024-01-15', '2024-06-30', NULL, 'Barangay Council', 'All residents', 'Barangay Hall Complex', 500000.00, 250000.00, 'Local Government Unit', 'Infrastructure', 'High', 60, 'Project is on schedule. Phase 1 completed.', '2025-11-26 03:23:56', '2025-11-26 03:56:13'),
(2, 'Community Health Program', 'Free medical check-ups, vaccination drives, and health awareness seminars for residents.', 'Completed', '2023-03-01', '2023-12-31', NULL, 'Barangay Health Center', 'All residents, especially children and seniors', 'Barangay Health Center', 150000.00, 145000.00, 'Department of Health', 'Health', 'High', 100, 'Successfully served 1,500+ residents', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(3, 'Street Lighting Installation', 'Installation of solar-powered LED street lights in dark areas to improve safety and security.', 'Planning', '2024-07-01', '2024-12-31', NULL, 'Barangay Peace and Order Committee', 'All residents', 'Main Roads and Dark Alleys', 800000.00, 0.00, 'National Government', 'Infrastructure', 'High', 0, 'Awaiting approval and procurement process', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(4, 'Livelihood Training Program', 'Skills training in baking, sewing, and handicraft making for unemployed residents.', 'Ongoing', '2024-02-01', '2024-11-30', NULL, 'Barangay Development Council', 'Unemployed residents and housewives', 'Barangay Multi-Purpose Hall', 200000.00, 120000.00, 'DSWD', 'Livelihood', 'Medium', 40, '50 participants enrolled. 2 batches completed.', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(5, 'Drainage System Improvement', 'Construction and repair of drainage systems to prevent flooding during heavy rains.', 'Completed', '2023-05-01', '2023-10-31', NULL, 'Barangay Engineering Office', 'Residents in flood-prone areas', 'Sitio A and Sitio B', 1200000.00, 1180000.00, 'Local Government Unit', 'Infrastructure', 'High', 100, 'Flooding incidents reduced by 80%', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(6, 'Youth Sports Development Program', 'Basketball and volleyball leagues, sports equipment provision, and coaching clinics.', 'Ongoing', '2024-01-01', '2024-12-31', NULL, 'Barangay Youth Council', 'Youth aged 15-30', 'Barangay Basketball Court', 100000.00, 65000.00, 'Local Government Unit', 'Youth Development', 'Medium', 35, 'League ongoing. 8 teams participating.', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(7, 'Senior Citizens Support Program', 'Monthly cash assistance, free medicine, and recreational activities for senior citizens.', 'Ongoing', '2024-01-01', '2024-12-31', NULL, 'Office of Senior Citizens Affairs', 'Senior citizens (60 years old and above)', 'Barangay Hall', 300000.00, 150000.00, 'DSWD and LGU', 'Social Services', 'High', 50, '120 senior citizens enrolled', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(8, 'Solid Waste Management Initiative', 'Implementation of waste segregation program and establishment of Materials Recovery Facility (MRF).', 'Planning', '2024-08-01', '2025-03-31', NULL, 'Barangay Environmental Committee', 'All residents', 'Barangay premises', 600000.00, 0.00, 'Department of Environment', 'Environment', 'High', 0, 'Awaiting environmental clearance', '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(9, 'Operation Tuli only for TanTan', 'This project is only for Tantan because he is still uncircumcised, so he can feel more confident as he grows up.', 'Planning', '2025-12-05', '2025-12-05', NULL, 'TULI', 'Youth', 'Biga', 2000.00, 2500.00, 'Local Government', 'Health', 'High', 0, 'fwienADFMVEUDJFGDHVN WEIUSDJGHVN', '2025-11-26 03:58:16', '2025-11-26 03:58:16'),
(10, 'wefweffewfwefwff', 'cwcedwwc', 'Planning', '2025-10-31', '2025-11-19', NULL, 'feewf', 'fewefe', 'fefwwffwefwf', 12000000.00, 10000.00, 'efewf', '', 'Medium', 10, 'fewwfwef', '2025-11-29 12:11:55', '2025-11-29 12:11:55');

-- --------------------------------------------------------

--
-- Table structure for table `blotter_incidents`
--

CREATE TABLE `blotter_incidents` (
  `incident_id` int(11) NOT NULL,
  `case_number` varchar(50) NOT NULL,
  `incident_type` enum('Complaint','Dispute','Noise Complaint','Domestic Issue','Theft','Assault','Vandalism','Public Disturbance','Other') NOT NULL DEFAULT 'Complaint',
  `incident_date` date NOT NULL,
  `incident_time` time DEFAULT NULL,
  `incident_location` text NOT NULL,
  `complainant_name` varchar(255) NOT NULL,
  `complainant_address` text DEFAULT NULL,
  `complainant_contact` varchar(50) DEFAULT NULL,
  `respondent_name` varchar(255) NOT NULL,
  `respondent_address` text DEFAULT NULL,
  `respondent_contact` varchar(50) DEFAULT NULL,
  `incident_description` text NOT NULL,
  `witnesses` text DEFAULT NULL,
  `incident_status` enum('Pending','Under Investigation','For Mediation','Resolved','Closed','Escalated') NOT NULL DEFAULT 'Pending',
  `priority_level` enum('High','Medium','Low') DEFAULT 'Medium',
  `assigned_to` varchar(255) DEFAULT NULL,
  `filed_by` varchar(255) DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `blotter_incidents`
--

INSERT INTO `blotter_incidents` (`incident_id`, `case_number`, `incident_type`, `incident_date`, `incident_time`, `incident_location`, `complainant_name`, `complainant_address`, `complainant_contact`, `respondent_name`, `respondent_address`, `respondent_contact`, `incident_description`, `witnesses`, `incident_status`, `priority_level`, `assigned_to`, `filed_by`, `remarks`, `created_at`, `updated_at`) VALUES
(1, 'BLT-2024-001', 'Noise Complaint', '2024-11-20', '23:30:00', 'Purok 3, Main Street', 'Juan Dela Cruz', 'Block 5 Lot 12, Purok 3', '0912-345-6789', 'Pedro Santos', 'Block 5 Lot 15, Purok 3', '0923-456-7890', 'Loud music and karaoke late at night disturbing the neighborhood. Complainant reported that this has been ongoing for several nights.', 'Maria Garcia (neighbor), Jose Reyes (neighbor)', 'Resolved', 'Low', 'Barangay Tanod - Roberto Cruz', 'Desk Officer - Ana Lopez', 'Issue resolved through mediation. Respondent agreed to lower volume and observe quiet hours.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(2, 'BLT-2024-002', 'Dispute', '2024-11-22', '14:00:00', 'Purok 1, Property Line between Lot 8 and Lot 9', 'Rosa Martinez', 'Block 2 Lot 8, Purok 1', '0918-765-4321', 'Carlos Ramos', 'Block 2 Lot 9, Purok 1', '0917-654-3210', 'Property boundary dispute. Complainant claims respondent built a fence encroaching on her property. Both parties present conflicting survey documents.', 'Surveyor - Engr. Manuel Torres', 'Under Investigation', 'High', 'Barangay Chairman - Antonio Mendoza', 'Secretary - Linda Cruz', 'Scheduled for mediation on December 1, 2024. Both parties to bring original land titles and survey plans.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(3, 'BLT-2024-003', 'Theft', '2024-11-23', '03:00:00', 'Purok 2, Residential Area', 'Elena Fernandez', 'Block 3 Lot 20, Purok 2', '0915-234-5678', 'Unknown Suspect', 'Unknown', 'N/A', 'Complainant reported theft of motorcycle (Plate No. ABC-1234) parked in front of house. CCTV footage shows two unidentified individuals at 3:00 AM.', 'CCTV footage available', 'Escalated', 'High', 'Barangay Police - Sgt. Ramon Diaz', 'Desk Officer - Ana Lopez', 'Case escalated to municipal police station. Police report filed. Investigation ongoing.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(4, 'BLT-2024-004', 'Domestic Issue', '2024-11-24', '20:15:00', 'Purok 4, Inside Residence', 'Anonymous Neighbor', 'Withheld for safety', 'Withheld', 'Miguel Castro', 'Block 6 Lot 30, Purok 4', '0920-111-2222', 'Loud argument and shouting heard from residence. Concerned neighbor reported possible domestic violence. Children crying heard.', 'Anonymous caller, Barangay Tanod who responded', 'For Mediation', 'High', 'VAWC Desk Officer - Dr. Sofia Reyes', 'Emergency Hotline', 'VAWC protocol activated. Social worker and counselor assigned. Family temporarily separated for cooling period.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(5, 'BLT-2024-005', 'Public Disturbance', '2024-11-25', '18:00:00', 'Barangay Basketball Court', 'Multiple Residents', 'Purok 5 Residents', 'N/A', 'Group of Teenagers', 'Various addresses in Purok 5', 'N/A', 'Group of teenagers drinking alcohol and causing disturbance at basketball court. Refusing to leave public area. Using profane language.', 'Multiple residents, Barangay Tanod', 'Closed', 'Medium', 'Barangay Tanod - Roberto Cruz', 'Resident - Mario Santos', 'Teenagers were escorted home. Parents were called and advised. Liquor bottles confiscated.', '2025-11-26 04:23:07', '2025-11-26 04:24:23'),
(6, 'BLT-2024-006', 'Complaint', '2024-11-25', '09:00:00', 'Purok 3, Drainage Area', 'Community Association', 'Purok 3 Residents', '0919-888-7777', 'Construction Company XYZ', 'Construction Site, Purok 3', '0928-999-8888', 'Ongoing construction blocking drainage system causing flooding in nearby houses. Debris and materials obstructing water flow.', 'Photos and video documentation, Multiple affected residents', 'Pending', 'High', 'Barangay Engineering - Engr. Pablo Cruz', 'Kagawad - Teresa Aquino', 'Notice to comply issued to construction company. Site inspection scheduled for November 28, 2024.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(7, 'BLT-2024-007', 'Assault', '2024-11-24', '16:30:00', 'Purok 2, Sari-sari Store', 'Antonio Villanueva', 'Block 4 Lot 25, Purok 2', '0916-333-4444', 'Ricardo Bautista', 'Block 4 Lot 28, Purok 2', '0917-555-6666', 'Physical altercation over unpaid debt. Complainant sustained minor injuries (bruises on face and arms). Respondent admits to pushing complainant.', 'Store owner - Lorna Santos, Customer - Felix Gomez', 'For Mediation', 'High', 'Barangay Chairman - Antonio Mendoza', 'Barangay Tanod - Roberto Cruz', 'Medical certificate issued by barangay health center. Both parties agree to mediation on November 27, 2024.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(8, 'BLT-2024-008', 'Vandalism', '2024-11-23', '02:00:00', 'Barangay Hall Wall', 'Barangay Hall Staff', 'Barangay Hall', '555-1234', 'Unknown Vandals', 'Unknown', 'N/A', 'Graffiti spray-painted on barangay hall exterior wall. Offensive and inappropriate content. Estimated damage and repainting cost: 5,000 pesos.', 'Security guard, CCTV footage (unclear)', 'Under Investigation', 'Medium', 'Barangay Tanod - Roberto Cruz', 'Security Guard - Domingo Reyes', 'Photos taken for documentation. Reviewing CCTV footage. Asking nearby residents for information.', '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(9, 'ebbej2be', 'Complaint', '2001-12-11', '10:08:00', 'fefefee', 'fefe', NULL, NULL, 'fefe', NULL, NULL, '', NULL, 'For Mediation', 'Medium', NULL, NULL, NULL, '2025-11-30 06:39:02', '2025-11-30 06:39:02');

-- --------------------------------------------------------

--
-- Stand-in structure for view `childrens_view`
-- (See below for the actual view)
--
CREATE TABLE `childrens_view` (
`resident_id` int(11)
,`household_id` int(11)
,`first_name` varchar(100)
,`middle_name` varchar(100)
,`last_name` varchar(100)
,`birth_date` date
,`age` bigint(11)
,`gender` varchar(20)
,`contact_no` varchar(20)
,`email` varchar(150)
,`created_at` timestamp
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `current_balance`
-- (See below for the actual view)
--
CREATE TABLE `current_balance` (
`total_income` decimal(34,2)
,`total_expense` decimal(34,2)
,`net_balance` decimal(35,2)
);

-- --------------------------------------------------------

--
-- Table structure for table `financial_reports`
--

CREATE TABLE `financial_reports` (
  `report_id` int(11) NOT NULL,
  `report_name` varchar(255) NOT NULL,
  `report_type` enum('Monthly','Quarterly','Annual','Custom') NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `total_income` decimal(12,2) DEFAULT 0.00,
  `total_expense` decimal(12,2) DEFAULT 0.00,
  `net_balance` decimal(12,2) DEFAULT 0.00,
  `generated_by` int(11) DEFAULT NULL,
  `generated_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Stand-in structure for view `financial_summary`
-- (See below for the actual view)
--
CREATE TABLE `financial_summary` (
`year` int(4)
,`month` int(2)
,`transaction_type` enum('Income','Expense')
,`category` varchar(100)
,`total_amount` decimal(34,2)
,`transaction_count` bigint(21)
);

-- --------------------------------------------------------

--
-- Table structure for table `financial_transactions`
--

CREATE TABLE `financial_transactions` (
  `transaction_id` int(11) NOT NULL,
  `transaction_date` date NOT NULL,
  `transaction_type` enum('Income','Expense') NOT NULL,
  `category` varchar(100) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `description` text DEFAULT NULL,
  `reference_number` varchar(50) DEFAULT NULL,
  `payee_payer` varchar(255) DEFAULT NULL,
  `payment_method` enum('Cash','Check','Bank Transfer','Online Payment','Other') DEFAULT 'Cash',
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `financial_transactions`
--

INSERT INTO `financial_transactions` (`transaction_id`, `transaction_date`, `transaction_type`, `category`, `amount`, `description`, `reference_number`, `payee_payer`, `payment_method`, `created_by`, `created_at`, `updated_at`) VALUES
(2, '2024-01-20', 'Income', 'Community Tax', 15000.00, 'Community tax collection for January', NULL, 'Residents', 'Cash', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(3, '2024-01-25', 'Income', 'Permit Fees', 8000.00, 'Business permit fees collection', NULL, 'Business Owners', 'Cash', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(4, '2024-02-01', 'Expense', 'Infrastructure', 25000.00, 'Road repair and maintenance project', NULL, 'ABC Construction', 'Check', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(5, '2024-02-05', 'Expense', 'Office Supplies', 3500.00, 'Paper, pens, folders, and office materials', NULL, 'Office Supply Store', 'Cash', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(6, '2024-02-10', 'Expense', 'Utilities', 4200.00, 'Electricity and water bills for February', NULL, 'Utility Company', 'Bank Transfer', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(7, '2024-03-01', 'Income', 'Barangay Allocation', 50000.00, 'Monthly allocation from city government', NULL, 'City Government', 'Bank Transfer', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(8, '2024-03-15', 'Expense', 'Salaries', 35000.00, 'Staff salaries for March', NULL, 'Barangay Staff', 'Bank Transfer', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(9, '2024-03-20', 'Income', 'Donations', 12000.00, 'Community donation for health program', NULL, 'Community Members', 'Cash', NULL, '2025-11-26 02:55:37', '2025-11-26 02:55:37'),
(10, '2025-02-13', 'Income', 'nweoihjreio', 123456.00, 'nwkgjkjvmtroitk', '123456789', 'barangay staf', 'Cash', NULL, '2025-11-26 03:55:19', '2025-11-26 03:55:19');

-- --------------------------------------------------------

--
-- Table structure for table `households`
--

CREATE TABLE `households` (
  `household_id` int(11) NOT NULL,
  `family_no` int(11) NOT NULL,
  `household_head_id` int(11) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `income` decimal(12,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `households`
--

INSERT INTO `households` (`household_id`, `family_no`, `household_head_id`, `address`, `income`, `created_at`, `updated_at`) VALUES
(1, 1, NULL, '123 Main St, Barangay Biga, Central 1', 250003.00, '2025-11-28 14:14:02', '2025-11-30 13:27:39'),
(2, 2, NULL, '456 Rizal St, Barangay Biga, Central 2', 32000.00, '2025-11-28 14:14:02', '2025-11-30 13:27:39'),
(3, 3, NULL, '789 Mabini St, Barangay Biga, Central 3', 28000.00, '2025-11-28 14:14:02', '2025-11-30 13:27:39'),
(4, 4, NULL, 'random_lmao', 9999999999.99, '2025-11-29 03:27:55', '2025-11-30 13:27:39'),
(5, 6, NULL, 'Kasily scm', 1234567.00, '2025-11-29 05:44:40', '2025-11-30 13:27:39'),
(6, 7, NULL, 'dwdw', 1999.00, '2025-11-29 12:52:13', '2025-11-30 13:27:39'),
(12, 1, NULL, 'lcnknce', 100.00, '2025-11-30 13:36:44', '2025-11-30 13:36:44'),
(13, 11, NULL, 'cdd', 0.00, '2025-11-30 13:36:57', '2025-11-30 13:36:57'),
(14, 10, NULL, 'rr', 100.00, '2025-11-30 13:46:45', '2025-11-30 13:46:45'),
(15, 100, NULL, 'vrvr', 100.00, '2025-11-30 14:07:29', '2025-11-30 14:07:29');

-- --------------------------------------------------------

--
-- Stand-in structure for view `household_members_view`
-- (See below for the actual view)
--
CREATE TABLE `household_members_view` (
);

-- --------------------------------------------------------

--
-- Table structure for table `incident_resolutions`
--

CREATE TABLE `incident_resolutions` (
  `resolution_id` int(11) NOT NULL,
  `incident_id` int(11) NOT NULL,
  `resolution_date` date NOT NULL,
  `resolution_type` enum('Mediation','Settlement','Referral','Dismissed','Other') NOT NULL,
  `resolution_details` text NOT NULL,
  `resolved_by` varchar(255) DEFAULT NULL,
  `settlement_amount` decimal(15,2) DEFAULT 0.00,
  `agreement_terms` text DEFAULT NULL,
  `follow_up_required` enum('Yes','No') DEFAULT 'No',
  `follow_up_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `incident_resolutions`
--

INSERT INTO `incident_resolutions` (`resolution_id`, `incident_id`, `resolution_date`, `resolution_type`, `resolution_details`, `resolved_by`, `settlement_amount`, `agreement_terms`, `follow_up_required`, `follow_up_date`, `created_at`, `updated_at`) VALUES
(1, 1, '2024-11-21', 'Mediation', 'Both parties attended mediation session. Respondent acknowledged the noise complaint and agreed to be more considerate of neighbors. Complainant accepted apology.', 'Lupong Tagapamayapa - Kagawad Maria Santos', 0.00, 'Respondent agrees to: 1) Stop karaoke after 9:00 PM on weekdays and 10:00 PM on weekends. 2) Keep music volume at reasonable levels. 3) Inform neighbors in advance of any special occasions. Complainant agrees to inform respondent first before filing future complaints.', 'No', NULL, '2025-11-26 04:23:07', '2025-11-26 04:23:07'),
(2, 5, '2024-11-25', 'Other', 'Parents of all teenagers involved were contacted and came to barangay hall. Counseling session conducted. Teenagers issued warning.', 'Barangay Tanod - Roberto Cruz with SK Chairman', 0.00, 'Teenagers agree to: 1) Not drink alcohol in public places. 2) Respect community rules. 3) Attend SK-organized community service for 3 weekends. Parents agree to monitor children more closely.', 'Yes', NULL, '2025-11-26 04:23:07', '2025-11-26 04:23:07');

-- --------------------------------------------------------

--
-- Stand-in structure for view `incident_statistics`
-- (See below for the actual view)
--
CREATE TABLE `incident_statistics` (
`total_incidents` bigint(21)
,`pending_incidents` decimal(22,0)
,`investigating_incidents` decimal(22,0)
,`mediation_incidents` decimal(22,0)
,`resolved_incidents` decimal(22,0)
,`closed_incidents` decimal(22,0)
,`escalated_incidents` decimal(22,0)
,`high_priority_total` decimal(22,0)
,`complaint_count` decimal(22,0)
,`dispute_count` decimal(22,0)
,`theft_count` decimal(22,0)
,`assault_count` decimal(22,0)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `incident_summary`
-- (See below for the actual view)
--
CREATE TABLE `incident_summary` (
`incident_status` enum('Pending','Under Investigation','For Mediation','Resolved','Closed','Escalated')
,`incident_count` bigint(21)
,`high_priority_count` decimal(22,0)
,`medium_priority_count` decimal(22,0)
,`low_priority_count` decimal(22,0)
);

-- --------------------------------------------------------

--
-- Table structure for table `project_budgets`
--

CREATE TABLE `project_budgets` (
  `budget_id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `budget_item` varchar(255) NOT NULL,
  `budget_description` text DEFAULT NULL,
  `allocated_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
  `spent_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
  `remaining_amount` decimal(15,2) GENERATED ALWAYS AS (`allocated_amount` - `spent_amount`) STORED,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `project_budgets`
--

INSERT INTO `project_budgets` (`budget_id`, `project_id`, `budget_item`, `budget_description`, `allocated_amount`, `spent_amount`, `created_at`, `updated_at`) VALUES
(1, 1, 'Construction Materials', 'Cement, steel, paint, fixtures', 300000.00, 180000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(2, 1, 'Labor Cost', 'Skilled and unskilled workers', 150000.00, 70000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(3, 1, 'Equipment Rental', 'Scaffolding, tools, machinery', 50000.00, 0.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(4, 2, 'Medical Supplies', 'Vaccines, medicines, vitamins', 80000.00, 78000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(5, 2, 'Personnel', 'Doctors, nurses, medical staff', 50000.00, 48000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(6, 2, 'Promotional Materials', 'Flyers, banners, tarpaulins', 20000.00, 19000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(7, 4, 'Training Materials', 'Baking tools, sewing machines, materials', 120000.00, 80000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(8, 4, 'Trainers Fee', 'Professional trainers and instructors', 60000.00, 40000.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56'),
(9, 4, 'Starter Kits', 'Initial business starter kits for graduates', 20000.00, 0.00, '2025-11-26 03:23:56', '2025-11-26 03:23:56');

-- --------------------------------------------------------

--
-- Stand-in structure for view `project_statistics`
-- (See below for the actual view)
--
CREATE TABLE `project_statistics` (
`total_projects` bigint(21)
,`planning_projects` decimal(22,0)
,`ongoing_projects` decimal(22,0)
,`completed_projects` decimal(22,0)
,`onhold_projects` decimal(22,0)
,`cancelled_projects` decimal(22,0)
,`total_budget` decimal(37,2)
,`total_utilized` decimal(37,2)
,`total_remaining` decimal(37,2)
,`avg_progress` decimal(14,4)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `project_summary`
-- (See below for the actual view)
--
CREATE TABLE `project_summary` (
`project_status` enum('Planning','Ongoing','Completed','On Hold','Cancelled')
,`project_count` bigint(21)
,`total_budget` decimal(37,2)
,`total_utilized` decimal(37,2)
,`total_remaining` decimal(37,2)
,`avg_progress` decimal(14,4)
);

-- --------------------------------------------------------

--
-- Table structure for table `residents`
--

CREATE TABLE `residents` (
  `resident_id` int(11) NOT NULL,
  `household_id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) NOT NULL,
  `suffix` varchar(20) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `age` int(11) NOT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `contact_no` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `residents`
--

INSERT INTO `residents` (`resident_id`, `household_id`, `first_name`, `middle_name`, `last_name`, `suffix`, `birth_date`, `age`, `gender`, `contact_no`, `email`, `created_at`, `updated_at`) VALUES
(1, 3, 'Anna', 'Santos', 'Dela Vega', '0', '2015-02-10', 10, 'Female', '09170000001', 'anna@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(2, 2, 'Luis', 'Reyes', 'Dela Cruz', '0', '2012-06-15', 13, 'Male', '09170000002', 'luis@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(3, 1, 'Juan', 'Santos', 'Dela Cruz', '0', '1985-05-20', 40, 'Male', '09170000003', 'juan@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(4, 1, 'Rosa', 'Lopez', 'Dela Cruz', '0', '1987-11-12', 38, 'Female', '09170000004', 'rosa@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(5, 4, 'Maria', 'Lopez', 'Santos', '0', '2014-09-20', 9, 'Female', '09170000006', 'maria@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(6, 3, 'Pedro', 'Garcia', 'Santos', '0', '2011-12-02', 12, 'Male', '09170000007', 'pedro@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(7, 1, 'Carlos', 'Santos', 'Santos', '0', '1990-07-25', 33, 'Male', '09170000008', 'carlos@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(8, 5, 'Lucia', 'Diaz', 'Santos', '0', '1989-10-11', 34, 'Female', '09170000009', 'lucia@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(9, 1, 'Aling', 'Ramos', 'Santos', '0', '1958-08-08', 65, 'Female', '09170000010', 'aling@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(10, 2, 'Bong', 'Santos', 'Santos', '0', '1995-04-15', 28, 'Male', '09170000011', 'bong@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(11, 3, 'Ella', 'Reyes', 'Reyes', '0', '2013-03-05', 10, 'Female', '09170000012', 'ella@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(12, 5, 'Mark', 'Cruz', 'Reyes', '0', '2016-11-20', 7, 'Male', '09170000013', 'mark@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(13, 2, 'Pedro', 'Reyes', 'Reyes', '0', '1980-01-22', 43, 'Male', '09170000014', 'pedro_r@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(14, 4, 'Ana', 'Lopez', 'Reyes', '0', '1982-06-30', 41, 'Female', '09170000015', 'ana@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(15, 1, 'Lola', 'Santos', 'Reyes', '0', '1952-12-12', 71, 'Female', '09170000016', 'lola@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(16, 5, 'Miguel', 'Garcia', 'Reyes', '0', '1992-05-14', 31, 'Male', '09170000017', 'miguel@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(17, 2, 'Clara', 'Reyes', 'Reyes', '0', '2010-07-09', 13, 'Female', '09170000018', 'clara@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(18, 2, 'Luis', 'Diaz', 'Reyes', '0', '2009-09-11', 14, 'Male', '09170000019', 'luis_d@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(19, 3, 'Sophia', 'Lopez', 'Reyes', '0', '2008-02-20', 15, 'Female', '09170000020', 'sophia@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(20, 1, 'Antonio', 'Perez', 'Reyes', '0', '1975-04-05', 48, 'Male', '09170000021', 'antonio@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(21, 1, 'Beatriz', 'Garcia', 'Reyes', '0', '1968-07-07', 55, 'Female', '09170000022', 'beatriz@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(22, 2, 'Daniel', 'Santos', 'Reyes', '0', '2006-12-11', 16, 'Male', '09170000023', 'daniel@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(23, 3, 'Isabel', 'Diaz', 'Reyes', '0', '2005-08-25', 17, 'Female', '09170000024', 'isabel@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(24, 5, 'Ricardo', 'Lopez', 'Reyes', '0', '2004-03-17', 19, 'Male', '09170000025', 'ricardo@gmail.com', '2025-11-28 14:17:25', '2025-11-29 11:45:19'),
(25, 1, 'Lloyd', 'Principe', 'Punzalan', '0', '2005-05-16', 20, '0', '09502737590', 'punzalanlloyd3@gmail.com', '2025-11-28 14:52:35', '2025-11-29 11:45:19'),
(26, 2, 'fuefbefbekfkebfkle', 'buebewbk', 'fbekfbkefe', '0', '2025-10-28', 100, '0', '101001010100101', 'testing@gmail.com', '2025-11-29 03:29:06', '2025-11-29 11:45:19'),
(27, 5, 'grrr', 'grgr', 'grgrgr', '0', '2005-11-12', 20, 'Male', '100101010', 'dDD@gmail.com', '2025-11-29 07:01:03', '2025-11-29 11:45:19'),
(28, 5, 'tsrytfjlj\'pkn;m', 'jguubbm', 'mb, k', '0', '2005-02-28', 20, '0', '898998', 'bfnfnabfjkvjks@gmail.com', '2025-11-29 07:06:29', '2025-11-29 11:45:19'),
(29, 6, 'cecce', 'ccecece', 'efefe', '0', '2021-11-18', 4, 'Male', '999999', 'fefe@gmail.com', '2025-11-29 12:52:56', '2025-11-29 12:52:56'),
(30, 14, 'vffv', 'vfvf', 'vff', '0', '2001-11-10', 10, 'Male', '21212', '21212', '2025-11-30 13:47:31', '2025-11-30 13:47:31'),
(31, 14, 'btgtg', 'gtgt', 'gtg', '0', '1989-10-10', 22, 'Male', '5555', 'crcr', '2025-11-30 13:48:45', '2025-11-30 13:48:45');

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`) VALUES
(1, 'Administrator'),
(2, 'Staff'),
(3, 'User');

-- --------------------------------------------------------

--
-- Stand-in structure for view `seniors_view`
-- (See below for the actual view)
--
CREATE TABLE `seniors_view` (
`resident_id` int(11)
,`household_id` int(11)
,`first_name` varchar(100)
,`middle_name` varchar(100)
,`last_name` varchar(100)
,`birth_date` date
,`age` bigint(11)
,`gender` varchar(20)
,`contact_no` varchar(20)
,`email` varchar(150)
,`created_at` timestamp
,`updated_at` timestamp
);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `user_code` varchar(20) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  `hashed_password` varchar(255) NOT NULL,
  `salt` varchar(64) NOT NULL,
  `fullname` varchar(150) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `status` char(10) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `profile_picture` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `user_code`, `username`, `hashed_password`, `salt`, `fullname`, `email`, `role_id`, `status`, `created_at`, `updated_at`, `profile_picture`) VALUES
(1, NULL, 'administrator', 'tdScuWHZrS0VFsuKFOsfO0C41B2yx4FcgjxELCQbAU8=', 'm6ebDFcqQHWcny+5', 'JULIETA QUINTO', 'quinto.julie123@gmail.com', 1, 'active', '2025-11-15 10:33:41', '2025-11-30 07:42:44', 'U002_1764326156.jpg'),
(2, NULL, 'staff', 'qujVhyCxfUpIBR/NU4sZunsL1xIEHfnttZvEoDdHs14=', 'rqv6bnJkTtC9O9oJteqDCg==', 'MARICRIS BAÑAREZ', 'staff@gmail.com', 2, 'active', '2025-11-15 11:26:41', '2025-11-30 07:27:19', 'U003_1763207089.jpeg'),
(3, NULL, 'juan123', '', '', 'Juan Dela Cruz', 'juan@example.com', 3, 'active', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png'),
(4, NULL, 'maria456', '', '', 'Maria Santos', 'maria@example.com', 1, 'inactive', '2025-11-24 15:55:35', '2025-11-30 06:47:30', 'default.png'),
(5, NULL, 'pedro789', '', '', 'Pedro Gomez', 'pedro@example.com', 3, 'inactive', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png'),
(6, NULL, 'ana321', '', '', 'Ana Reyes', 'ana@example.com', 3, 'active', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png'),
(7, NULL, 'carlos654', '', '', 'Carlos Mendoza', 'carlos@example.com', 3, 'active', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png'),
(8, NULL, 'lloyd007', '', '', 'Lloyd Punzalan', 'lloyd@example.com', 3, 'active', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png'),
(9, NULL, 'kristine123', '', '', 'Kristine Reyes', 'kristine@example.com', 3, 'inactive', '2025-11-24 15:55:35', '2025-11-24 16:21:30', 'default.png');

-- --------------------------------------------------------

--
-- Table structure for table `user_logs`
--

CREATE TABLE `user_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` varchar(255) NOT NULL,
  `log_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `ip_address` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_logs`
--

INSERT INTO `user_logs` (`log_id`, `user_id`, `action`, `log_time`, `ip_address`) VALUES
(171, 1, 'User logged in', '2025-11-30 05:33:20', '127.0.0.1'),
(172, 1, 'User logged in', '2025-11-30 05:37:56', '127.0.0.1'),
(173, 1, 'User logged in', '2025-11-30 06:32:44', '127.0.0.1'),
(174, 1, 'User logged in', '2025-11-30 06:38:03', '127.0.0.1'),
(175, 1, 'User logged in', '2025-11-30 07:21:30', '127.0.0.1'),
(176, 1, 'User logged out', '2025-11-30 07:26:44', '127.0.0.1'),
(177, 2, 'User logged in', '2025-11-30 07:27:19', '127.0.0.1'),
(178, 2, 'User logged in', '2025-11-30 07:29:14', '127.0.0.1'),
(179, 2, 'User logged in', '2025-11-30 07:42:07', '127.0.0.1'),
(180, 1, 'User logged in', '2025-11-30 07:47:24', '127.0.0.1'),
(181, 1, 'User logged in', '2025-11-30 07:50:55', '127.0.0.1'),
(182, 1, 'User logged in', '2025-11-30 07:54:46', '127.0.0.1'),
(183, 1, 'User logged in', '2025-11-30 07:55:30', '127.0.0.1'),
(184, 1, 'User logged in', '2025-11-30 08:07:34', '127.0.0.1'),
(185, 2, 'User logged in', '2025-11-30 08:16:45', '127.0.0.1'),
(186, 2, 'User logged in', '2025-11-30 08:30:19', '127.0.0.1'),
(187, 1, 'User logged in', '2025-11-30 08:31:04', '127.0.0.1'),
(188, 2, 'User logged in', '2025-11-30 11:49:43', '127.0.0.1'),
(189, 1, 'User logged in', '2025-11-30 11:56:28', '127.0.0.1'),
(190, 2, 'User logged in', '2025-11-30 12:59:48', '127.0.0.1'),
(191, 1, 'User logged in', '2025-11-30 13:08:15', '127.0.0.1'),
(192, 1, 'User logged in', '2025-11-30 13:08:57', '127.0.0.1'),
(193, 1, 'User logged in', '2025-11-30 13:17:54', '127.0.0.1'),
(194, 1, 'User logged in', '2025-11-30 13:35:59', '127.0.0.1'),
(195, 1, 'User logged in', '2025-11-30 13:46:33', '127.0.0.1'),
(196, 1, 'User logged in', '2025-11-30 14:07:09', '127.0.0.1'),
(197, 1, 'User logged in', '2025-11-30 14:16:03', '127.0.0.1');

-- --------------------------------------------------------

--
-- Structure for view `adults_view`
--
DROP TABLE IF EXISTS `adults_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `adults_view`  AS SELECT `residents`.`resident_id` AS `resident_id`, `residents`.`household_id` AS `household_id`, `residents`.`first_name` AS `first_name`, `residents`.`middle_name` AS `middle_name`, `residents`.`last_name` AS `last_name`, `residents`.`birth_date` AS `birth_date`, cast(`residents`.`age` as signed) AS `age`, `residents`.`gender` AS `gender`, `residents`.`contact_no` AS `contact_no`, `residents`.`email` AS `email`, `residents`.`created_at` AS `created_at`, `residents`.`updated_at` AS `updated_at` FROM `residents` WHERE `residents`.`age` >= 18 ;

-- --------------------------------------------------------

--
-- Structure for view `childrens_view`
--
DROP TABLE IF EXISTS `childrens_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `childrens_view`  AS SELECT `residents`.`resident_id` AS `resident_id`, `residents`.`household_id` AS `household_id`, `residents`.`first_name` AS `first_name`, `residents`.`middle_name` AS `middle_name`, `residents`.`last_name` AS `last_name`, `residents`.`birth_date` AS `birth_date`, cast(`residents`.`age` as signed) AS `age`, `residents`.`gender` AS `gender`, `residents`.`contact_no` AS `contact_no`, `residents`.`email` AS `email`, `residents`.`created_at` AS `created_at`, `residents`.`updated_at` AS `updated_at` FROM `residents` WHERE `residents`.`age` < 18 ;

-- --------------------------------------------------------

--
-- Structure for view `current_balance`
--
DROP TABLE IF EXISTS `current_balance`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `current_balance`  AS SELECT (select coalesce(sum(`financial_transactions`.`amount`),0) from `financial_transactions` where `financial_transactions`.`transaction_type` = 'Income') AS `total_income`, (select coalesce(sum(`financial_transactions`.`amount`),0) from `financial_transactions` where `financial_transactions`.`transaction_type` = 'Expense') AS `total_expense`, (select coalesce(sum(`financial_transactions`.`amount`),0) from `financial_transactions` where `financial_transactions`.`transaction_type` = 'Income') - (select coalesce(sum(`financial_transactions`.`amount`),0) from `financial_transactions` where `financial_transactions`.`transaction_type` = 'Expense') AS `net_balance` ;

-- --------------------------------------------------------

--
-- Structure for view `financial_summary`
--
DROP TABLE IF EXISTS `financial_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `financial_summary`  AS SELECT year(`financial_transactions`.`transaction_date`) AS `year`, month(`financial_transactions`.`transaction_date`) AS `month`, `financial_transactions`.`transaction_type` AS `transaction_type`, `financial_transactions`.`category` AS `category`, sum(`financial_transactions`.`amount`) AS `total_amount`, count(0) AS `transaction_count` FROM `financial_transactions` GROUP BY year(`financial_transactions`.`transaction_date`), month(`financial_transactions`.`transaction_date`), `financial_transactions`.`transaction_type`, `financial_transactions`.`category` ORDER BY year(`financial_transactions`.`transaction_date`) DESC, month(`financial_transactions`.`transaction_date`) DESC ;

-- --------------------------------------------------------

--
-- Structure for view `household_members_view`
--
DROP TABLE IF EXISTS `household_members_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `household_members_view`  AS SELECT `h`.`household_id` AS `household_id`, `h`.`family_no` AS `family_no`, `h`.`full_name` AS `household_head`, `h`.`address` AS `address`, `h`.`income` AS `income`, `r`.`resident_id` AS `resident_id`, concat(`r`.`first_name`,' ',coalesce(`r`.`middle_name`,''),' ',`r`.`last_name`) AS `resident_full_name`, `r`.`birth_date` AS `birth_date`, cast(`r`.`age` as signed) AS `age`, `r`.`gender` AS `gender`, `r`.`contact_no` AS `contact_no`, `r`.`email` AS `email`, `r`.`created_at` AS `resident_created_at`, `r`.`updated_at` AS `resident_updated_at` FROM (`households` `h` join `residents` `r` on(`h`.`household_id` = `r`.`household_id`)) ;

-- --------------------------------------------------------

--
-- Structure for view `incident_statistics`
--
DROP TABLE IF EXISTS `incident_statistics`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `incident_statistics`  AS SELECT count(0) AS `total_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'Pending' then 1 else 0 end) AS `pending_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'Under Investigation' then 1 else 0 end) AS `investigating_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'For Mediation' then 1 else 0 end) AS `mediation_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'Resolved' then 1 else 0 end) AS `resolved_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'Closed' then 1 else 0 end) AS `closed_incidents`, sum(case when `blotter_incidents`.`incident_status` = 'Escalated' then 1 else 0 end) AS `escalated_incidents`, sum(case when `blotter_incidents`.`priority_level` = 'High' then 1 else 0 end) AS `high_priority_total`, sum(case when `blotter_incidents`.`incident_type` = 'Complaint' then 1 else 0 end) AS `complaint_count`, sum(case when `blotter_incidents`.`incident_type` = 'Dispute' then 1 else 0 end) AS `dispute_count`, sum(case when `blotter_incidents`.`incident_type` = 'Theft' then 1 else 0 end) AS `theft_count`, sum(case when `blotter_incidents`.`incident_type` = 'Assault' then 1 else 0 end) AS `assault_count` FROM `blotter_incidents` ;

-- --------------------------------------------------------

--
-- Structure for view `incident_summary`
--
DROP TABLE IF EXISTS `incident_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `incident_summary`  AS SELECT `blotter_incidents`.`incident_status` AS `incident_status`, count(0) AS `incident_count`, sum(case when `blotter_incidents`.`priority_level` = 'High' then 1 else 0 end) AS `high_priority_count`, sum(case when `blotter_incidents`.`priority_level` = 'Medium' then 1 else 0 end) AS `medium_priority_count`, sum(case when `blotter_incidents`.`priority_level` = 'Low' then 1 else 0 end) AS `low_priority_count` FROM `blotter_incidents` GROUP BY `blotter_incidents`.`incident_status` ;

-- --------------------------------------------------------

--
-- Structure for view `project_statistics`
--
DROP TABLE IF EXISTS `project_statistics`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `project_statistics`  AS SELECT count(0) AS `total_projects`, sum(case when `barangay_projects`.`project_status` = 'Planning' then 1 else 0 end) AS `planning_projects`, sum(case when `barangay_projects`.`project_status` = 'Ongoing' then 1 else 0 end) AS `ongoing_projects`, sum(case when `barangay_projects`.`project_status` = 'Completed' then 1 else 0 end) AS `completed_projects`, sum(case when `barangay_projects`.`project_status` = 'On Hold' then 1 else 0 end) AS `onhold_projects`, sum(case when `barangay_projects`.`project_status` = 'Cancelled' then 1 else 0 end) AS `cancelled_projects`, sum(`barangay_projects`.`total_budget`) AS `total_budget`, sum(`barangay_projects`.`budget_utilized`) AS `total_utilized`, sum(`barangay_projects`.`budget_remaining`) AS `total_remaining`, avg(`barangay_projects`.`progress_percentage`) AS `avg_progress` FROM `barangay_projects` ;

-- --------------------------------------------------------

--
-- Structure for view `project_summary`
--
DROP TABLE IF EXISTS `project_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `project_summary`  AS SELECT `barangay_projects`.`project_status` AS `project_status`, count(0) AS `project_count`, sum(`barangay_projects`.`total_budget`) AS `total_budget`, sum(`barangay_projects`.`budget_utilized`) AS `total_utilized`, sum(`barangay_projects`.`budget_remaining`) AS `total_remaining`, avg(`barangay_projects`.`progress_percentage`) AS `avg_progress` FROM `barangay_projects` GROUP BY `barangay_projects`.`project_status` ;

-- --------------------------------------------------------

--
-- Structure for view `seniors_view`
--
DROP TABLE IF EXISTS `seniors_view`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `seniors_view`  AS SELECT `residents`.`resident_id` AS `resident_id`, `residents`.`household_id` AS `household_id`, `residents`.`first_name` AS `first_name`, `residents`.`middle_name` AS `middle_name`, `residents`.`last_name` AS `last_name`, `residents`.`birth_date` AS `birth_date`, cast(`residents`.`age` as signed) AS `age`, `residents`.`gender` AS `gender`, `residents`.`contact_no` AS `contact_no`, `residents`.`email` AS `email`, `residents`.`created_at` AS `created_at`, `residents`.`updated_at` AS `updated_at` FROM `residents` WHERE `residents`.`age` >= 60 ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `barangay_officials`
--
ALTER TABLE `barangay_officials`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_position` (`position_title`),
  ADD KEY `idx_position_title` (`position_title`),
  ADD KEY `idx_display_order` (`display_order`),
  ADD KEY `idx_is_active` (`is_active`);

--
-- Indexes for table `barangay_projects`
--
ALTER TABLE `barangay_projects`
  ADD PRIMARY KEY (`project_id`),
  ADD KEY `idx_status` (`project_status`),
  ADD KEY `idx_start_date` (`start_date`),
  ADD KEY `idx_priority` (`priority_level`),
  ADD KEY `idx_category` (`project_category`);

--
-- Indexes for table `blotter_incidents`
--
ALTER TABLE `blotter_incidents`
  ADD PRIMARY KEY (`incident_id`),
  ADD UNIQUE KEY `case_number` (`case_number`),
  ADD KEY `idx_case_number` (`case_number`),
  ADD KEY `idx_incident_date` (`incident_date`),
  ADD KEY `idx_status` (`incident_status`),
  ADD KEY `idx_type` (`incident_type`),
  ADD KEY `idx_priority` (`priority_level`);

--
-- Indexes for table `financial_reports`
--
ALTER TABLE `financial_reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `idx_report_type` (`report_type`),
  ADD KEY `idx_date_range` (`start_date`,`end_date`);

--
-- Indexes for table `financial_transactions`
--
ALTER TABLE `financial_transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `idx_transaction_date` (`transaction_date`),
  ADD KEY `idx_transaction_type` (`transaction_type`),
  ADD KEY `idx_category` (`category`);

--
-- Indexes for table `households`
--
ALTER TABLE `households`
  ADD PRIMARY KEY (`household_id`),
  ADD KEY `fk_household_head` (`household_head_id`);

--
-- Indexes for table `incident_resolutions`
--
ALTER TABLE `incident_resolutions`
  ADD PRIMARY KEY (`resolution_id`),
  ADD KEY `idx_incident_id` (`incident_id`),
  ADD KEY `idx_resolution_date` (`resolution_date`);

--
-- Indexes for table `project_budgets`
--
ALTER TABLE `project_budgets`
  ADD PRIMARY KEY (`budget_id`),
  ADD KEY `idx_project_id` (`project_id`);

--
-- Indexes for table `residents`
--
ALTER TABLE `residents`
  ADD PRIMARY KEY (`resident_id`),
  ADD KEY `household_id` (`household_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `role_id` (`role_id`);

--
-- Indexes for table `user_logs`
--
ALTER TABLE `user_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `barangay_officials`
--
ALTER TABLE `barangay_officials`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `barangay_projects`
--
ALTER TABLE `barangay_projects`
  MODIFY `project_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `blotter_incidents`
--
ALTER TABLE `blotter_incidents`
  MODIFY `incident_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `financial_reports`
--
ALTER TABLE `financial_reports`
  MODIFY `report_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `financial_transactions`
--
ALTER TABLE `financial_transactions`
  MODIFY `transaction_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `households`
--
ALTER TABLE `households`
  MODIFY `household_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `incident_resolutions`
--
ALTER TABLE `incident_resolutions`
  MODIFY `resolution_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `project_budgets`
--
ALTER TABLE `project_budgets`
  MODIFY `budget_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `residents`
--
ALTER TABLE `residents`
  MODIFY `resident_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `user_logs`
--
ALTER TABLE `user_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=198;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `households`
--
ALTER TABLE `households`
  ADD CONSTRAINT `fk_household_head` FOREIGN KEY (`household_head_id`) REFERENCES `residents` (`resident_id`);

--
-- Constraints for table `incident_resolutions`
--
ALTER TABLE `incident_resolutions`
  ADD CONSTRAINT `incident_resolutions_ibfk_1` FOREIGN KEY (`incident_id`) REFERENCES `blotter_incidents` (`incident_id`) ON DELETE CASCADE;

--
-- Constraints for table `project_budgets`
--
ALTER TABLE `project_budgets`
  ADD CONSTRAINT `project_budgets_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `barangay_projects` (`project_id`) ON DELETE CASCADE;

--
-- Constraints for table `residents`
--
ALTER TABLE `residents`
  ADD CONSTRAINT `fk_residents_household_id` FOREIGN KEY (`household_id`) REFERENCES `households` (`household_id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`);

--
-- Constraints for table `user_logs`
--
ALTER TABLE `user_logs`
  ADD CONSTRAINT `user_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

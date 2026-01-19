-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: spurfy
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `ai_recommend_history`
--

--
-- Dumping data for table `dogs`
--

/*!40000 ALTER TABLE `dogs` DISABLE KEYS */;
INSERT INTO `dogs` (`dog_id`, `user_id`, `name`, `breed`, `birth_date`, `gender`, `weight`, `notes`, `created_at`, `image_url`) VALUES ('0d71024f-0123-43dd-9452-e7c5106777e0','842fb9ed-b7ba-49fd-86d0-d7457348e327','탄이','치와와','2022-06-08','M',3.5,'겁이 많아요 ㅠ','2025-08-07 00:00:00','/dog-images/b42edcf7-ebcc-4f27-af8f-de12c5965481_20250807_233510527.jpg'),('42be78de-17af-4d48-85a1-e4814a3d990a','842fb9ed-b7ba-49fd-86d0-d7457348e327','미미','말티즈','2009-12-24','F',5.5,'피부가 안좋아요','2025-06-20 00:00:00','/dog-images/a8014373-3871-459b-bd70-7d838e060650_20250807_233518770.webp'),('995fac19-e53c-442e-a3b0-0ee739d2ea93','842fb9ed-b7ba-49fd-86d0-d7457348e327','뽀삐','치와와','2020-11-18','F',3.3,NULL,'2025-07-31 00:00:00',NULL);
/*!40000 ALTER TABLE `dogs` ENABLE KEYS */;

--
-- Dumping data for table `payments`
--

/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` (`payment_id`, `reservation_id`, `user_id`, `amount`, `payment_method`, `payment_status`, `created_at`) VALUES ('08cd4482-955a-4252-a7d5-7e3e1c683fa9','21f026f9-91a6-4618-9835-dbf3d1380c7f','842fb9ed-b7ba-49fd-86d0-d7457348e327',39000.00,'CARD','PAID','2025-07-28 18:52:03'),('2a65f056-bafd-4aa7-a31e-cb4b235e2375','27fd42f6-da08-4d43-8336-67d6764dc05d','842fb9ed-b7ba-49fd-86d0-d7457348e327',29000.00,'CARD','PAID','2025-09-05 01:02:00'),('32eccbaf-9791-432d-92cf-5c5e57c185e4','5f43bcdf-692b-49e9-873b-cb454ab04935','842fb9ed-b7ba-49fd-86d0-d7457348e327',35000.00,'CARD','PAID','2025-06-23 00:07:38'),('37526799-1539-4b58-bb8f-322e3f9fe126','6e5be938-0fd5-4d4d-83d8-4f627a9aa7d8','842fb9ed-b7ba-49fd-86d0-d7457348e327',29000.00,'CARD','PAID','2025-07-28 17:58:10'),('380d4496-a153-4bc8-82c2-bbc5ee5a9feb','89b323be-1e54-4c0e-b4b7-953f3322b879','842fb9ed-b7ba-49fd-86d0-d7457348e327',29000.00,'CARD','PAID','2025-08-07 21:43:10'),('38e0161d-0d2c-4e61-8096-5e41f6fed9c3','f2bdd049-daed-4809-b823-e9d87217357a','842fb9ed-b7ba-49fd-86d0-d7457348e327',29000.00,'CARD','PAID','2025-09-14 14:29:21'),('7a630303-e0c1-463c-93b0-817a27c787c9','d990ec83-9627-4fa0-95f6-3552f8b39084','842fb9ed-b7ba-49fd-86d0-d7457348e327',45900.00,'EASY_PAY','PAID','2025-07-29 18:38:12'),('a249d63f-6340-48bc-98b3-347e8ddfad5c','a12b3290-0101-44fc-a625-a32924a014de','842fb9ed-b7ba-49fd-86d0-d7457348e327',35000.00,'CARD','PAID','2025-06-22 23:08:30'),('d0debfa8-0838-4411-be6b-ab8c471e74f6','f6095101-0cd3-48bc-a7af-0ed44cee89c6','842fb9ed-b7ba-49fd-86d0-d7457348e327',35000.00,'CARD','PAID','2025-06-23 00:17:54');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;

--
-- Dumping data for table `refresh_tokens`
--

--
-- Dumping data for table `reservations`
--

/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` (`reservation_id`, `user_id`, `dog_id`, `service_id`, `reservation_date`, `reservation_time`, `reservation_status`, `refund_type`, `cancel_reason`, `refunded_at`, `created_at`, `updated_at`, `refund_status`, `payment_status`, `price`) VALUES ('21f026f9-91a6-4618-9835-dbf3d1380c7f','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_004','2025-07-30','13:00:00','COMPLETED','FULL','',NULL,'2025-07-28 18:52:03','2025-07-31 14:38:48','NONE',NULL,39000),('27fd42f6-da08-4d43-8336-67d6764dc05d','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_001','2025-09-12','13:00:00','COMPLETED','FULL','',NULL,'2025-09-05 01:02:00','2025-09-14 13:45:21','NONE',NULL,29000),('5f43bcdf-692b-49e9-873b-cb454ab04935','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_002','2025-06-25','17:00:00','CANCELED','AUTO','사용자 요청','2025-06-23 00:07:45','2025-06-23 00:07:38','2025-06-23 00:07:45','COMPLETED',NULL,35000),('6e5be938-0fd5-4d4d-83d8-4f627a9aa7d8','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_001','2025-07-29','13:00:00','CANCELED','AUTO','시간이 안될거 같아요','2025-07-28 18:25:11','2025-07-28 17:58:10','2025-07-28 18:25:11','COMPLETED',NULL,29000),('89b323be-1e54-4c0e-b4b7-953f3322b879','842fb9ed-b7ba-49fd-86d0-d7457348e327','0d71024f-0123-43dd-9452-e7c5106777e0','spa_001','2025-08-20','13:00:00','CANCELED','AUTO','사용자 요청 (사유 미입력)','2025-08-07 21:47:33','2025-08-07 21:43:10','2025-08-07 21:47:32','COMPLETED',NULL,29000),('a12b3290-0101-44fc-a625-a32924a014de','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_002','2025-06-27','15:00:00','CANCELED','AUTO','사용자 요청',NULL,'2025-06-22 23:08:30','2025-06-22 23:41:26','WAITING',NULL,35000),('d990ec83-9627-4fa0-95f6-3552f8b39084','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_002','2025-07-31','15:00:00','COMPLETED','FULL','',NULL,'2025-07-29 18:38:12','2025-08-01 15:02:11','NONE',NULL,45900),('f2bdd049-daed-4809-b823-e9d87217357a','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_001','2025-09-19','13:00:00','COMPLETED','FULL','',NULL,'2025-09-14 14:29:21','2025-09-20 15:13:31','NONE',NULL,29000),('f6095101-0cd3-48bc-a7af-0ed44cee89c6','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a','spa_002','2025-06-25','15:00:00','CANCELED','AUTO','시간이 안될거 같습니다 ㅠㅠ!','2025-06-23 00:18:40','2025-06-23 00:17:54','2025-06-23 00:18:39','COMPLETED',NULL,35000);
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;

--
-- Dumping data for table `reviews`
--

/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` (`review_id`, `reservation_id`, `user_id`, `dog_id`, `rating`, `content`, `image_url`, `is_blinded`, `created_at`, `updated_at`) VALUES ('14233c00-c036-40b7-9bde-5c5826c6aff1','27fd42f6-da08-4d43-8336-67d6764dc05d','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a',4,'웰컴 스파는 언제 이용해도 부담없이 받을 수 있어서 너무 만족해요!! >_<','',0,'2025-09-16 00:19:54',NULL),('550fa879-796e-4552-bf16-2f1c85c16d39','21f026f9-91a6-4618-9835-dbf3d1380c7f','842fb9ed-b7ba-49fd-86d0-d7457348e327','42be78de-17af-4d48-85a1-e4814a3d990a',5,'저희 미미 피부가 되게 예민한데 스파 받고 난 이후로 많이 진정됐어요! 강추합니당 ㅎ','',0,'2025-07-31 23:05:56',NULL);
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;

--
-- Dumping data for table `service_info`
--

/*!40000 ALTER TABLE `service_info` DISABLE KEYS */;
INSERT INTO `service_info` (`info_id`, `category`, `title`, `content`, `display_order`, `is_active`, `created_at`, `updated_at`) VALUES (1,'이용_안내','이용 전 안내','예약 시간 10분 전까지 도착해 주세요.\n도착 후 상담을 통해 스파 진행이 이루어집니다.\n예약 시간 기준 30분 이상 지연 시 예약이 자동 취소될 수 있습니다.',1,1,'2025-06-29 19:12:54','2025-06-29 19:12:54'),(2,'이용_안내','이용 시간 안내','평균 이용 시간: 40~60분\n 견종과 서비스 종류에 따라 달라질 수 있습니다.\n 반려견 상태에 따라 중단 또는 휴식이 제공될 수 있습니다.',2,1,'2025-06-29 19:13:15','2025-10-27 20:38:32'),(3,'서비스_소개','스파 서비스 소개','저희 Spurfy는 반려견이 편안히 쉴 수 있는 스파 공간을 제공합니다.\n피부 자극은 줄이고, 건강한 휴식을 돕는 순한 케어만을 담았습니다.\n반려견의 컨디션에 따라 유연하게 진행되어, 민감한 피부도 부담 없이 케어받을 수 있습니다.',3,1,'2025-06-29 19:15:23','2025-08-08 21:50:58');
/*!40000 ALTER TABLE `service_info` ENABLE KEYS */;

--
-- Dumping data for table `spa_service_tags`
--

/*!40000 ALTER TABLE `spa_service_tags` DISABLE KEYS */;
INSERT INTO `spa_service_tags` (`service_id`, `tag_id`) VALUES ('spa_001',1),('spa_001',2),('spa_001',3),('spa_004',5),('spa_004',6),('spa_004',8),('spa_003',10),('spa_003',12),('spa_002',14),('spa_002',16);
/*!40000 ALTER TABLE `spa_service_tags` ENABLE KEYS */;

--
-- Dumping data for table `spa_services`
--

/*!40000 ALTER TABLE `spa_services` DISABLE KEYS */;
INSERT INTO `spa_services` (`service_id`, `name`, `description`, `duration_minutes`, `price`, `is_active`, `created_at`, `updated_at`, `image_url`, `available_times`, `slug`) VALUES ('spa_001','웰컴 스파','스파 첫 경험을 위한 기본 케어입니다.\n순한 천연 샴푸와 편안한 마사지로 피부를 진정시키고,\n부드러운 브러싱으로 털을 깔끔하게 정리해 드립니다.\n처음 스파를 접하는 아이에게 추천드립니다.',50,29000,1,'2025-06-29 13:56:44','2025-07-28 17:40:30',NULL,'10:00,13:00,15:00,17:00','welcome-spa'),('spa_002','프리미엄 브러싱 스파','섬세한 브러싱과 고급 케어를 제공하는 프리미엄 스파입니다.\n엉킨 털을 부드럽게 풀고, 윤기 나는 모질로 관리해 드립니다.\n정기 케어나 특별한 날에 추천드립니다.',60,45900,1,'2025-06-06 20:59:31','2025-07-28 17:40:47',NULL,'10:00,13:00,15:00,17:00','premium-brushing-spa'),('spa_003','릴렉싱 테라피 스파','관절과 근육의 피로를 풀어주는 릴렉싱 스파입니다.\n따뜻한 온욕과 전문 마사지로 활동량이 많은 아이들의 회복을 돕고,\n편안한 휴식을 제공합니다.',60,45900,1,'2025-06-29 13:57:41','2025-07-28 17:41:04',NULL,'10:00,13:00,15:00,17:00','relaxing-therapy-spa'),('spa_004','카밍 스킨 스파','예민하고 민감한 피부를 위한 진정 스파입니다.\n저자극 천연 제품을 사용해 피부 자극을 최소화하며,\n피부를 편안하게 안정시켜 줍니다.',60,39000,1,'2025-06-29 13:58:11','2025-07-28 17:41:21',NULL,'10:00,13:00,15:00,17:00','calming-skin-spa');
/*!40000 ALTER TABLE `spa_services` ENABLE KEYS */;

--
-- Dumping data for table `tags`
--

/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` (`tag_id`, `tag_name`, `created_at`, `updated_at`) VALUES (1,'스테디','2025-06-29 19:04:29','2025-06-29 19:04:29'),(2,'기본케어','2025-06-29 19:04:31','2025-06-29 19:04:31'),(3,'첫스파추천','2025-06-29 19:04:32','2025-06-29 19:04:32'),(5,'저자극케어','2025-06-29 19:04:48','2025-06-29 19:04:48'),(6,'피부진정','2025-06-29 19:04:58','2025-06-29 19:04:58'),(8,'민감피부👌🏻','2025-06-29 19:05:36','2025-08-12 01:46:56'),(10,'노견케어','2025-06-29 19:06:06','2025-06-29 19:06:06'),(12,'관절이완','2025-06-29 19:06:15','2025-06-29 19:06:15'),(14,'고급케어','2025-06-29 19:06:49','2025-06-29 19:06:49'),(16,'보호자만족도👍🏻','2025-06-29 19:06:52','2025-08-12 01:46:55'),(20,'맞춤케어','2025-06-29 19:07:32','2025-06-29 19:07:32'),(22,'AI추천','2025-06-29 19:07:52','2025-06-29 19:07:52');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;

--
-- Dumping data for table `users`
--

/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`user_id`, `email`, `password`, `name`, `nickname`, `phone`, `profile_image`, `user_status`, `user_role`, `last_login_at`, `created_at`, `updated_at`, `withdrawal_date`, `withdrawal_reason`) VALUES ('71fa70a1-0b18-4cdc-abd9-d141f4d1a87b','t@t.com','$2a$10$wD6ikQJRSaxaRBbAR8UnleCyX/Id0XzjbvwibTPlDUYlPwEHl168G','돼지코','테스트용','01012345678',NULL,'ACTIVE','USER',NULL,'2025-08-27 20:51:54','2025-09-03 18:45:24',NULL,NULL),('842fb9ed-b7ba-49fd-86d0-d7457348e327','oyl582@naver.com','$2a$10$R3vTH.GCUDEYNhydMrcsAeWWF/ZRK/GicL8rS6kktp0DqfgswFy8S','오예림','예리미아님','01050768082',NULL,'ACTIVE','USER',NULL,'2025-06-20 22:57:41','2025-10-29 21:21:38',NULL,NULL),('c3f07130-ef51-4af9-af3a-0d6c921d4c8a','oyl991118@daum.net','$2a$10$8v6I0N/A17mjeZ7NIIgkI.0WVFIExarth6sB1PEUcGfE9mK6kLA/i','오예림','예리미짱','01050768082',NULL,'DEACTIVATED','USER',NULL,'2025-06-22 01:54:43','2025-09-03 18:51:33','2025-09-03 18:51:33.026723','그냥 그랬어요');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-16 20:33:41

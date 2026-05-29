# Inpatient Meal Management System

A hospital-based patient meal coordination system built with **Java Spring Boot** and **React TypeScript**, designed to streamline how admitted patients request meals and how dieticians manage personalized dietary plans.

---

## Table of Contents

- [Overview](#overview)
- [Problem Statement](#problem-statement)
- [Solution](#solution)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [User Roles](#user-roles)
- [Deployment](#deployment)
- [HIS Integration](#his-integration)
- [Future Improvements](#future-improvements)

---

## Overview

Inpatient Meal Management improves communication between patients, dieticians, kitchen staff, and hospital administrators. It integrates with the hospital's Hospital Information System (HIS) to automatically retrieve patient details and supports role-based access across all user types.

---

## Problem Statement

Hospitals often face inefficiencies in managing patient meals due to:

- Fragmented communication between patients, kitchen staff, and dieticians
- Limited integration with Hospital Information Systems (HIS)
- Delays and incorrect meal allocation
- Poor tracking of patient dietary requirements

---

## Solution

This application integrates with the hospital's HIS using a custom **scraper-based adapter** to retrieve patient details (UHID, name, room number). A **fallback mode** ensures continuous service availability when the HIS is unavailable, preventing disruption to meal operations.

---

## Features

  **Patient login** using UHID
  **Automatic patient data retrieval** from HIS
  **Dietician-managed meal plans** based on patient diagnosis
  **Role-based access control** for patients, dieticians, kitchen staff, and admins
  **Real-time popup notifications** for meal orders, approvals, and status changes
  **HIS scraper integration** with offline fallback mode
  **Fully containerized** with Docker and NGINX

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5 |
| Frontend | React 18, TypeScript |
| Containerization | Docker |
| Reverse Proxy | NGINX |
| HIS Integration | Custom scraper-based adapter |
| OS | Linux (Ubuntu) |

---

## System Architecture


HIS (Hospital Information System)
        │
        ▼
  Scraper Adapter  ──(fallback mode if HIS is offline)
        │
        ▼
Spring Boot REST API  (Java 21 / Spring Boot 3.5)
        │
        ▼
  React TypeScript Frontend
  (Served via NGINX)


**Data Flow:** Patient details are scraped from the HIS and fed into the backend, which exposes REST APIs consumed by the React frontend. NGINX handles routing and serves static frontend assets.



## User Roles

| Role | Responsibilities |
|---|---|
| **Patient** | Logs in using UHID, browses and requests meals |
| **Dietician** | Defines and assigns meal plans based on patient diagnosis |
| **Kitchen Staff** | Views assigned meals and updates preparation status |
| **Admin** | Manages system configuration, users, and monitoring |



## Deployment

The application is deployed on a production-ready Linux infrastructure:

**Containerization:** Docker isolates backend and frontend services
**Reverse Proxy:** NGINX handles external traffic routing and serves static frontend assets



## HIS Integration

**Note:** The HIS integration currently uses a scraper-based approach due to the absence of a dedicated API from the Hospital Information System.

### How it works

1. The scraper adapter authenticates with the HIS web interface
2. Patient details (UHID, name, room number, diagnosis) are extracted and synced to the backend
3. If the HIS is unreachable, the system switches to **fallback mode**, allowing staff to manually enter patient details without service interruption

### Planned improvement

Replace the scraper with an official HIS REST API integration once one becomes available, improving reliability and reducing maintenance overhead.

---

## Future Improvements

- [ ] Replace scraper adapter with official HIS API integration
- [ ] Introduce push notifications (web and mobile)
- [ ] Improve caching and offline resilience layer
- [ ] Strengthen real-time communication between kitchen and dieticians
- [ ] CI/CD pipeline setup for automated testing and deployment

---

## License

This project is licensed under the [MIT License](LICENSE).

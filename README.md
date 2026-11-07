# Splitwise Clone — Production-Ready Full Stack Platform

A production-quality, enterprise-grade **Splitwise Clone** built with **Java 21, Spring Boot 3.3+, Spring Security 6 + JWT, WebSocket (STOMP), MySQL 8, Redis 7, React 18, and Tailwind CSS**.

---

## 🌟 Key Features

1. **User Authentication & Authorization**:
   - Secure user registration and login with BCrypt password hashing.
   - Stateless JWT access tokens + HttpOnly refresh token rotation.
   - Fine-grained group-level ACL (`@GroupPermissionEvaluator`) eliminating Insecure Direct Object References (IDOR).

2. **Group Management & Invite Codes**:
   - Create and organize groups (Trip, Home/Roommates, Couple, Other).
   - Unique 8-character invitation codes with instant join capability.
   - Role-based membership control (`ADMIN` and `MEMBER`).

3. **Expense Management & Multi-Split Engine (Strategy Design Pattern)**:
   - **Equal Split**: Equal division with penny remainder distribution guaranteeing exact sum conservation down to the cent.
   - **Exact Split**: Explicit amounts per participant verified against total.
   - **Percentage Split**: Precise percentage division with penny reconciliation.
   - **Share-Based Split**: Proportional ratio division with fair remainder allocation.
   - **Multi-Payer Support**: Allows multiple members to co-fund an expense.

4. **Balance Calculation & Min-Cash-Flow Debt Simplification**:
   - Accurate net balance formula:
     $$\text{Net Balance} = (\text{Total Paid} - \text{Total Owed}) + (\text{Settlements Paid} - \text{Settlements Received})$$
   - Greedy Max-Heap Min-Cash-Flow algorithm reducing $O(K^2)$ debts down to at most $K - 1$ minimal cash transfers.

5. **Settlement Management**:
   - One-click "Settle Up" recording direct cash or digital payments.
   - Automatic balance reconciliation and historical logging.

6. **Real-Time Group Chat (WebSocket + STOMP)**:
   - Real-time group messaging with STOMP destination channels.
   - WebSocket authentication interceptor binding JWT identity to STOMP session.
   - Real-time typing indicators with auto-expiring timeouts.
   - Real-time read receipts and unread status.
   - Image & receipt attachment sharing.

7. **Expense Analytics**:
   - Visual category breakdown with spending percentages.
   - Monthly trend aggregation.

8. **Modern Fintech React UI**:
   - Responsive design with Tailwind CSS and Lucide icons.
   - Centralized API client with automatic token refreshing.

---

## 🏗️ Architecture & Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security 6, Spring WebSocket (STOMP) |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Zustand, @stomp/stompjs, Lucide Icons |
| **Database** | MySQL 8.0 (InnoDB) for production; H2 MySQL mode for local zero-dependency dev |
| **Cache & Broker** | Redis 7.0 for session cache and WebSocket Pub/Sub scaling |
| **Testing** | JUnit 5, AssertJ, Reflection test harness (19/19 unit tests passing) |
| **Deployment** | Docker & Docker Compose |

---

## 🚀 Getting Started

### 1. Prerequisites
- Java 21+
- Node.js 18+ and npm
- Docker & Docker Compose (optional, for full containerized stack)

---

### 2. Running Locally (Quick Start)

#### Backend (Spring Boot):
```bash
cd backend
mvn clean test-compile
mvn spring-boot:run
```
*The backend starts at `http://localhost:8080` (uses embedded in-memory database with MySQL dialect for instant zero-config startup).*

#### Frontend (React + Vite):
```bash
cd frontend
npm install
npm run dev
```
*The frontend starts at `http://localhost:3000` with hot module replacement and proxy configured to the backend.*

---

### 3. Running with Docker Compose (Production Stack)

To run the complete production environment including MySQL 8, Redis 7, Spring Boot, and Nginx:
```bash
docker-compose up --build
```
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api/v1`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

---

## 🧪 Running Algorithm Unit Tests

The core financial calculation strategies and Min-Cash-Flow debt simplification algorithms have been verified with 19 comprehensive test scenarios:

```bash
cd backend
mvn test-compile
/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home/bin/java -cp "target/classes:target/test-classes:$(find ~/.m2/repository -name '*.jar' | tr '\n' ':')" com.splitwise.TestRunner
```

**Test Results:**
```
=================================================
RUNNING SPLITWISE PRODUCTION ALGORITHM TEST SUITE
=================================================

Testing Class: SplitStrategyTest
  [PASS] testPercentageSplitPennyReconciliation
  [PASS] testEqualSplitWithPennyRemainder
  [PASS] testEqualSplitExactDivision
  [PASS] testEqualSplitEmptyParticipants
  [PASS] testExactSplitValid
  [PASS] testExactSplitMismatch
  [PASS] testPercentageSplitValid
  [PASS] testPercentageSplitInvalidSum
  [PASS] testShareSplitValid

Testing Class: DebtSimplificationServiceTest
  [PASS] testEmptyBalances
  [PASS] testAllZeroBalances
  [PASS] testTwoMembersDirectDebt
  [PASS] testTransitiveSimplification
  [PASS] testCircularDebtCancelsOut
  [PASS] testOneCreditorTwoDebtors
  [PASS] testTwoCreditorsOneDebtor
  [PASS] testFourMembersMatching
  [PASS] testFiveMembersComplexTrip
  [PASS] testOddPenniesPrecision

=================================================
TOTAL TESTS: 19 | PASSED: 19 | FAILED: 0
=================================================
```

---

## 🛡️ Security & Concurrency Mitigations

1. **Anti-IDOR Protection**: All resource accesses verify that the authenticated user belongs to the group of the requested resource using `GroupPermissionEvaluator`.
2. **WebSocket Channel Interceptor**: STOMP frames (`CONNECT`, `SUBSCRIBE`, `SEND`) validate JWT tokens and enforce group membership before allowing subscription to `/topic/groups/{groupId}/*`.
3. **Double Settlement Prevention**: Optimistic locking (`@Version`) prevents race conditions during settlement and expense modifications.
4. **Penny Remainder Distribution**: Financial operations use `BigDecimal` with strict penny distribution ensuring money conservation invariant:
   $$\sum \text{Paid} = \text{Total Expense} = \sum \text{Owed}$$


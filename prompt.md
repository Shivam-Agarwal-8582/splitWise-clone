Prompt 1 – Project planning and architecture
Start here
Act as a senior software architect and Java Spring Boot
engineer.

I want to build a production-quality SplitWise Clone.

Project requirements:
- Frontend: React + Tailwind CSS
- Backend: Java 21 + Spring Boot
- Database: MySQL
- Security: Spring Security + JWT
- ORM: Spring Data JPA + Hibernate
- Real-time communication: WebSocket + STOMP
- Caching: Redis
- File storage: AWS S3
- Testing: JUnit 5 + Mockito
- Deployment: Docker

Main features:
1. User authentication
2. Group creation and membership
3. Expense management
4. Equal, exact, percentage and share-based splitting
5. Balance calculation
6. Debt simplification algorithm
7. Settlement management
8. Real-time group chat
9. Typing indicators and online presence
10. Read receipts
11. Notifications
12. Receipt and image sharing
13. Expense analytics

First, DO NOT write implementation code.

Analyze the requirements and provide:

1. Complete system architecture
2. Backend module structure
3. Frontend folder structure
4. Database schema and ER relationships
5. REST API design
6. WebSocket architecture
7. Authentication and authorization flow
8. Expense calculation algorithm
9. Debt simplification algorithm
10. Development roadmap

Identify possible security, concurrency and data
consistency problems.

Wait for my approval before implementation.



Prompt 2 – Create Spring Boot backend
Act as a senior Java Spring Boot developer.

Set up the backend for my SplitWise Clone.

Technology:
- Java 21
- Spring Boot
- Maven
- MySQL
- Spring Data JPA
- Spring Security
- JWT
- Lombok
- Bean Validation

Requirements:

1. Create a clean layered architecture.
2. Create entity, repository, service, controller and DTO
   packages.
3. Configure MySQL.
4. Configure application properties.
5. Implement global exception handling.
6. Create a standard API response structure.
7. Add validation for incoming requests.
8. Configure CORS securely.
9. Use constructor injection.
10. Follow SOLID principles.

Do not implement every feature yet.

First create the project structure and configuration.
Explain every generated file and its responsibility.

Do not introduce unnecessary dependencies.
Do not duplicate existing classes.
Run the available tests and fix compilation errors.


Prompt 3 – Authentication and JWT
Implement authentication for my SplitWise Clone.

Requirements:

1. User registration.
2. User login.
3. BCrypt password encryption.
4. JWT access token generation.
5. JWT validation.
6. Spring Security configuration.
7. Custom UserDetailsService.
8. Authentication filter.
9. Current authenticated user retrieval.
10. Logout/token invalidation strategy.

Create:

- User entity
- UserRepository
- AuthController
- AuthService
- AuthRequest DTO
- AuthResponse DTO
- JWT utility
- Security configuration

Security requirements:

- Never store plain-text passwords.
- Never expose password hashes in API responses.
- Validate tokens on protected requests.
- Return appropriate HTTP status codes.
- Prevent duplicate email registrations.
- Validate user input.

Write unit tests using JUnit and Mockito.

Explain the authentication flow after implementation.


Prompt 4 – Group management
Implement the group management module.

Features:

1. Create group.
2. Update group.
3. Delete group with appropriate authorization.
4. Add group members.
5. Remove group members.
6. Generate invitation codes.
7. Accept invitations.
8. List groups belonging to authenticated user.
9. View group member details.

Database entities:

- Group
- GroupMember
- User

Requirements:

- A user can belong to multiple groups.
- A group can contain multiple users.
- Only authorized members can access group details.
- Only group admins can manage membership.
- Prevent duplicate group membership.
- Validate invitation codes.
- Handle invalid group IDs.

Use DTOs, service interfaces and repository layers.

Add unit and integration tests.


Prompt 5 – Expense management and splitting engine
This is one of the most important prompts.
Implement a robust expense management module.

Features:

1. Add expense.
2. Update expense.
3. Delete expense.
4. View expense details.
5. List group expenses.
6. Expense categories.
7. Multiple participants.
8. Multiple payers.

Implement the following splitting strategies:

- EqualSplitStrategy
- ExactSplitStrategy
- PercentageSplitStrategy
- ShareBasedSplitStrategy

Use the Strategy Design Pattern.

Create:

- Expense entity
- ExpenseSplit entity
- ExpenseRepository
- ExpenseSplitRepository
- ExpenseService
- ExpenseController
- ExpenseSplitStrategy interface
- Individual strategy classes

Important rules:

- Use BigDecimal for monetary calculations.
- Validate that the total of all splits equals the expense.
- Validate that percentages add up to 100%.
- Handle rounding to two decimal places.
- Distribute rounding remainder correctly.
- Do not allow unauthorized users to create expenses.
- Use database transactions.
- Preserve audit history when expenses are edited.

Write comprehensive tests for all splitting strategies,
including edge cases.

Explain the design pattern and business logic.


Prompt 6 – Balance engine and debt simplification
Implement the balance calculation engine.

Requirements:

1. Calculate total paid by each group member.
2. Calculate total owed by each member.
3. Calculate each member's net balance.
4. Account for recorded settlements.
5. Generate who-owes-whom information.
6. Implement debt simplification.

Use the formula:

Net Balance = Total Paid - Total Owed - Net Settlements Paid

A positive balance means the member should receive money.
A negative balance means the member owes money.

Debt simplification algorithm:

- Separate creditors and debtors.
- Match the largest outstanding debtor with a creditor.
- Transfer the minimum outstanding amount.
- Continue until balances are zero.

Requirements:

- Use BigDecimal.
- Do not use floating-point arithmetic.
- Handle rounding.
- Prevent inconsistent balances.
- Handle deleted and updated expenses.
- Ensure all group balances sum to zero.
- Optimize calculation performance.

Write unit tests with at least 10 different scenarios.

Explain time complexity and space complexity.


Prompt 7 – Real-time group chat
Important differentiating feature
Act as a senior Spring Boot WebSocket engineer.

Implement a real-time group chat module.

Technology:
- Spring Boot WebSocket
- STOMP protocol
- Spring Security
- MySQL
- React STOMP client

Requirements:

1. Users can send messages to group members.
2. Messages should appear instantly without refreshing.
3. Save every message in MySQL.
4. Retrieve previous message history.
5. Support pagination.
6. Display sender name and timestamp.
7. Implement typing indicators.
8. Implement online/offline status.
9. Implement read receipts.
10. Support message deletion and editing with permissions.
11. Support group-specific message channels.

WebSocket destinations:

Application:
- /app/groups/{groupId}/chat
- /app/groups/{groupId}/typing
- /app/groups/{groupId}/read

Subscriptions:
- /topic/groups/{groupId}/chat
- /topic/groups/{groupId}/typing
- /topic/groups/{groupId}/presence
- /topic/groups/{groupId}/read

Create:

- WebSocketConfig
- ChatMessage entity
- MessageRead entity
- ChatRepository
- ChatService
- ChatController
- ChatMessageRequest DTO
- ChatMessageResponse DTO
- WebSocket authentication interceptor

Security requirements:

- Authenticate WebSocket connections using JWT.
- Bind authenticated users to their WebSocket sessions.
- Never trust senderId supplied by the client.
- Only group members can send or subscribe to group messages.
- Validate message length and content.
- Prevent unauthorized access to other groups.
- Validate all subscriptions and destinations.
- Persist messages before broadcasting them.

Handle reconnection and duplicate message submissions.

Write tests for message authorization and persistence.

Explain the complete WebSocket lifecycle.


Important: ask Copilot to implement the WebSocket security interceptor before building the frontend chat. An insecure chat can expose private group conversations.
Prompt 8 – React frontend
Act as a senior React frontend engineer.

Build a professional SplitWise Clone frontend.

Technology:
- React
- Vite
- Tailwind CSS
- Axios
- React Router
- Context API
- @stomp/stompjs

Create pages:

1. Login
2. Register
3. Dashboard
4. Groups
5. Group details
6. Add expense
7. Expense history
8. Balances
9. Settlements
10. Group chat
11. User profile

Design requirements:

- Modern fintech dashboard.
- Responsive design.
- Sidebar navigation.
- Clean typography.
- Expense cards.
- Group balance visualization.
- Loading states.
- Error states.
- Empty states.
- Toast notifications.

Implement reusable components.

Integrate backend APIs using Axios.
Use a centralized API service.
Handle JWT authentication.
Protect private routes.

Do not use dummy data when real APIs are available.

Build each page separately and explain the folder structure.


Prompt 9 – Integrate real-time chat frontend
Implement the real-time chat frontend.

Use @stomp/stompjs.

Requirements:

1. Connect to the Spring Boot WebSocket endpoint.
2. Authenticate the connection using JWT.
3. Subscribe to the selected group's chat topic.
4. Send messages through STOMP.
5. Display incoming messages instantly.
6. Load previous messages using REST APIs.
7. Show sender names and timestamps.
8. Implement typing indicators.
9. Show online/offline status.
10. Implement read receipts.
11. Implement reconnection.
12. Handle duplicate messages.
13. Disconnect when the user logs out.
14. Clean up subscriptions when changing groups.

Design a modern chat interface with:

- Message bubbles.
- Sender avatars.
- Message timestamps.
- Chat input.
- Send button.
- Typing indicator.
- Unread message count.
- Scroll-to-latest behavior.

Keep WebSocket logic in a reusable custom hook.

Handle loading, connection and error states.


Prompt 10 – Testing and production readiness
Act as a senior QA and backend engineer.

Review my complete SplitWise Clone.

Focus on:

1. Authentication vulnerabilities.
2. Authorization vulnerabilities.
3. SQL query performance.
4. Database consistency.
5. Concurrent expense creation.
6. Duplicate settlements.
7. Expense splitting accuracy.
8. Debt simplification correctness.
9. WebSocket security.
10. Message persistence.
11. Frontend API errors.
12. WebSocket reconnection.

Add:

- JUnit tests.
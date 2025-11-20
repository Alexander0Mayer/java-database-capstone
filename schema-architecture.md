Section 1: Architecture summary
This Spring Boot application uses both MVC and REST controllers. Thymeleaf templates are used for the Admin and Doctor dashboards, while REST APIs serve all other modules. The application interacts with two databases—MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). All controllers route requests through a common service layer, which in turn delegates to the appropriate repositories. MySQL uses JPA entities while MongoDB uses document models.
Section 2: Flow of data and controll
1 User accesses AdminDashbord or Doctor Dashboard
2 The dashbords use Thymeleaf html pages provided by the Thymeleaf Controllers
3 The Thymeleaf controllers call the Service Layer which performs the business logic
4 The service layer draws data from the MySQL repositories or from the MongoDB repositories
5 The repositories access either the MySQL database or the MongoDB database
6 Data in the MySQL database is structured according to the MySQL models
7 The MySQL models are defined by JPA Entities

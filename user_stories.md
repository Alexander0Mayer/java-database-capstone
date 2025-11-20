# User Stories with Acceptance Criteria, Story Points & Priority

## Admin User Stories

### A1 - Logging in
**Priority:** High | **Story Points:** 3

As an administrator I want to log into the portal with my username and password to manage the platform securely.

**Acceptance Criteria:**
- Admin can access a login page with username and password fields
- Valid credentials grant access to the AdminDashboard
- Invalid credentials display an error message
- Password field masks input for security
- Login session persists across page navigation
- Admin cannot access dashboard without successful authentication

---

### A2 - Logging out
**Priority:** High | **Story Points:** 2

As an administrator I want to log out of the portal to protect system access.

**Acceptance Criteria:**
- Admin dashboard displays a logout button
- Clicking logout clears the session and redirects to login page
- User cannot access admin features after logout
- Logout button is accessible from all admin pages
- Session is invalidated server-side upon logout

---

### A3 - Adding doctors
**Priority:** High | **Story Points:** 5

As an administrator I want to add doctors to the portal.

**Acceptance Criteria:**
- Admin can access a "Add Doctor" form from the dashboard
- Form requires: name, email, phone, specialization, and qualification
- Email validation ensures unique doctor accounts
- Success message confirms doctor addition
- New doctor receives login credentials via email
- Doctor record is saved to MySQL database
- Admin can view the newly added doctor in the doctor list

---

### A4 - Deleting doctors
**Priority:** Medium | **Story Points:** 5

As an administrator I want to delete a doctor's profile from the portal.

**Acceptance Criteria:**
- Admin can select a doctor from the doctor list to delete
- Confirmation dialog appears before deletion
- Doctor with scheduled appointments cannot be deleted (error message displayed)
- Successful deletion removes doctor from the system
- Doctor record is removed from MySQL database
- Admin receives confirmation message after deletion
- Deleted doctor's account is deactivated immediately

---

### A5 - Usage statistics
**Priority:** Medium | **Story Points:** 5

As an administrator I want to run a stored procedure in MySQL CLI to get the number of appointments per month and track usage statistics.

**Acceptance Criteria:**
- Admin dashboard displays a "Generate Report" button
- Report shows appointment count aggregated by month
- Stored procedure executes without errors
- Results display in a table or chart format
- Report can be filtered by date range
- Data is retrieved from the MySQL Appointment table
- Report is downloadable in CSV or PDF format
- Statistics update in real-time as new appointments are added

---

## Patient User Stories

### P1 - Viewing doctors
**Priority:** High | **Story Points:** 3

As a patient I want to view a list of doctors without logging in to explore options before registering.

**Acceptance Criteria:**
- A public doctors list page is accessible without authentication
- List displays doctor name, specialization, and contact information
- List includes at least 10 doctors initially
- Patients can filter by specialization
- Doctor details include availability status
- List loads within 2 seconds
- No login prompt blocks access to the public list

---

### P2 - Signing up
**Priority:** High | **Story Points:** 5

As a patient I want to sign up using my email and password to book appointments.

**Acceptance Criteria:**
- Signup form is accessible from the login page
- Form requires: email, password, confirm password, full name, date of birth, phone number
- Email validation ensures format is correct and not already registered
- Password must be at least 8 characters with mixed case and numbers
- Confirmation email is sent upon successful signup
- Patient can log in after email confirmation
- Patient record is created in MySQL database
- Error messages display for duplicate emails or validation failures

---

### P3 - Logging in
**Priority:** High | **Story Points:** 3

As a patient I want to log into the portal to manage my bookings.

**Acceptance Criteria:**
- Patient can access a login page with email and password fields
- Valid credentials grant access to PatientDashboard
- Invalid credentials display an error message
- Password field masks input for security
- Login session persists across page navigation
- Patient cannot access booking features without authentication
- "Remember me" option is available (optional)

---

### P4 - Logging out
**Priority:** High | **Story Points:** 2

As a patient I want to log out of the portal to secure my account.

**Acceptance Criteria:**
- Logout button is visible in the patient dashboard
- Clicking logout clears the session and redirects to login page
- Patient cannot access personal bookings after logout
- Logout button is accessible from all patient pages
- Session is invalidated server-side upon logout

---

### P5 - Booking appointments
**Priority:** High | **Story Points:** 8

As a patient I want to log in and book an hour-long appointment to consult with a doctor.

**Acceptance Criteria:**
- Patient can search for doctors by specialization and availability
- Booking form shows available time slots (1-hour intervals)
- Calendar displays only available dates and times
- Patient can select preferred doctor, date, and time
- Appointment reason/notes field is optional
- Booking confirmation is displayed immediately
- Confirmation email is sent to patient and doctor
- Appointment is saved to MySQL database
- Patient cannot double-book the same time slot
- Doctor's calendar reflects the new appointment in real-time

---

### P6 - Viewing appointments
**Priority:** High | **Story Points:** 3

As a patient I want to view my upcoming appointments so that I can prepare accordingly.

**Acceptance Criteria:**
- Patient dashboard displays a list of upcoming appointments
- Each appointment shows: doctor name, date, time, and location/meeting link
- Appointments are sorted by date (nearest first)
- Past appointments are archived in a separate section
- Patient can see appointment details by clicking on an appointment
- Cancel appointment option is available for upcoming appointments
- List updates automatically when new bookings are made

---

## Doctor User Stories

### D1 - Logging in
**Priority:** High | **Story Points:** 3

As a doctor I want to log into the portal to manage my appointments.

**Acceptance Criteria:**
- Doctor can access a login page with email and password fields
- Valid credentials grant access to DoctorDashboard
- Invalid credentials display an error message
- Password field masks input for security
- Login session persists across page navigation
- Doctor cannot access appointment calendar without authentication
- Login attempts are logged for security audit

---

### D2 - Logging out
**Priority:** High | **Story Points:** 2

As a doctor I want to log out of the portal to protect my data.

**Acceptance Criteria:**
- Logout button is visible on the doctor dashboard
- Clicking logout clears the session and redirects to login page
- Doctor cannot access personal calendar after logout
- Logout button is accessible from all doctor pages
- Session is invalidated server-side upon logout

---

### D3 - Viewing appointment calendar
**Priority:** High | **Story Points:** 5

As a doctor I want to view my appointment calendar to stay organized.

**Acceptance Criteria:**
- Doctor dashboard displays a calendar view of appointments
- Calendar shows appointments for current and future months
- Each appointment displays: patient name, time, and reason (if provided)
- Calendar can be toggled between month, week, and day views
- Past appointments are visible but clearly marked as completed
- Doctor can click on an appointment for detailed patient information
- Calendar syncs with the database in real-time
- Unavailable time slots are visually distinguished

---

### D4 - Marking unavailability
**Priority:** Medium | **Story Points:** 5

As a doctor I want to mark my unavailability to inform patients only of the available slots.

**Acceptance Criteria:**
- Doctor can set unavailable time blocks from the calendar
- Unavailable slots can be marked for specific dates and times
- Unavailable slots can be recurring (daily, weekly, monthly)
- Patients cannot book during marked unavailable times
- Doctor can edit or delete unavailability blocks
- Unavailability reasons (vacation, meeting, etc.) can be added
- Changes are reflected immediately in the patient booking system
- Unavailability data is stored in MySQL database

---

### D5 - Update profile
**Priority:** Medium | **Story Points:** 4

As a doctor I want to update my profile with specialization and contact information so that patients have up-to-date information.

**Acceptance Criteria:**
- Doctor can access a profile edit page
- Editable fields include: name, email, phone, specialization, qualifications, bio, and profile picture
- Phone number and email are validated before saving
- Specialization can be updated from a predefined list
- Profile changes are saved to MySQL database immediately
- Confirmation message displays after successful update
- Updated information is visible to patients within 5 minutes
- Doctor can view their public profile before saving changes

---

### D6 - View patient details
**Priority:** High | **Story Points:** 4

As a doctor I want to view patient details for upcoming appointments so that I can be prepared.

**Acceptance Criteria:**
- Doctor can click on an appointment to view patient information
- Patient details include: name, age, contact number, medical history, and previous appointments with the doctor
- Patient details are retrieved from both MySQL and MongoDB (PatientRecord)
- Sensitive information is properly secured and logged
- Doctor can access patient notes from previous consultations
- Option to add or update notes for patient record
- Patient details load within 2 seconds
- Doctor can print or download patient summary before appointment
